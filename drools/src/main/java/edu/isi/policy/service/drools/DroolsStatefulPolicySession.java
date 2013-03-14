package edu.isi.policy.service.drools;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import edu.isi.policy.entity.Entity;
import edu.isi.policy.exception.EntityNotFoundException;
import edu.isi.policy.exception.GlobalVariableNotFoundException;
import edu.isi.policy.service.AbstractPolicySession;

/**
 * Drools stateful knowledge session implementation of a policy session.
 * 
 * @author David Smith
 * 
 */
public class DroolsStatefulPolicySession extends AbstractPolicySession
implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
            .getLogger(DroolsStatefulPolicySession.class);
    private final StatefulKnowledgeSession ksession;

    /**
     * Default constructor.
     * 
     * @param globalVariables
     *            a map of global variables to define in policy rules
     * @param policyRuleFiles
     *            the policy rule files to use
     */
    public DroolsStatefulPolicySession(Map<String, Object> globalVariables,
            Collection<String> policyRuleFiles) {
        super(globalVariables, policyRuleFiles);
        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder();
        for (String policyRuleFile : getPolicyRuleFiles()) {
            kbuilder.add(ResourceFactory.newClassPathResource(policyRuleFile),
                    ResourceType.DRL);
        }
        final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        if (!kbuilder.hasErrors()) {
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        } else {
            throw new RuntimeException("Error loading policy rules: "
                    + kbuilder.getErrors().toString());
        }
        ksession = kbase.newStatefulKnowledgeSession();
        for (String varName : getGlobalVariables().keySet()) {
            try {
                ksession.setGlobal(varName, globalVariables.get(varName));
                LOG.debug("Set global: " + varName + "="
                        + globalVariables.get(varName));
            } catch (Exception e) {
                // for now assume that the global was not defined in the
                // rules file
                LOG.warn("Could not set the global variable " + varName);
            }
        }

        if (LOG.isDebugEnabled()) {
            ksession.addEventListener(new DebugWorkingMemoryEventListener());
            ksession.addEventListener(new DebugAgendaEventListener());
            LOG.debug("Created new drools stateful policy session " + this);
        }
    }

    @Override
    public Object evaluateNewEntity(Object obj) {
        FactHandle handle = null;
        synchronized (this) {
            handle = ksession.insert(obj);
            ksession.fireAllRules();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inserted new fact into policy: " + handle + "=" + obj);
        }
        return obj;
    }

    @Override
    public Entity removeEntity(final String entityId)
            throws EntityNotFoundException {
        Entity entity = null;
        synchronized (this) {
            final Collection<Object> objects = ksession
                    .getObjects(new ObjectFilter() {
                        @Override
                        public boolean accept(Object arg0) {
                            return arg0 instanceof Entity
                                    && entityId.equals(((Entity) arg0)
                                            .getId());
                        }
                    });
            if (objects != null && objects.size() > 0) {
                entity = (Entity) objects.iterator().next();
                final FactHandle factHandle = ksession.getFactHandle(entity);
                if (factHandle != null) {
                    ksession.retract(factHandle);
                }
                // TODO: do we need to fire rules here?
            }
        }
        if (entity == null) {
            throw new EntityNotFoundException(entityId);
        }
        return entity;
    }

    @Override
    public Entity getEntity(final String entityId)
            throws EntityNotFoundException {
        Entity entity = null;
        Collection<Object> objects = null;
        synchronized (this) {
            objects = ksession.getObjects(new ObjectFilter() {
                @Override
                public boolean accept(Object arg0) {
                    return arg0 instanceof Entity
                            && entityId.equals(((Entity) arg0).getId());
                }
            });
        }
        if (objects != null && objects.size() > 0) {
            entity = (Entity) objects.iterator().next();
        }
        if (entity == null) {
            throw new EntityNotFoundException(entityId);
        }
        return entity;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<Object> getAllObjectInstances(final Class c) {
        Collection<Object> objects = null;
        synchronized(this) {
            objects = ksession.getObjects(new ObjectFilter() {

                @Override
                public boolean accept(Object object) {
                    return c.isInstance(object);
                }

            });
        }
        return objects;
    }

    @Override
    public Entity evaluateUpdatedEntity(final String id, Entity newEntity)
            throws EntityNotFoundException {
        Entity currentEntity = null;
        FactHandle fh = null;
        synchronized (this) {
            Collection<Object> objects = ksession
                    .getObjects(new ObjectFilter() {
                        @Override
                        public boolean accept(Object arg0) {
                            return arg0 instanceof Entity
                                    && id.equals(((Entity) arg0).getId());
                        }
                    });
            if (objects != null && objects.size() > 0) {
                currentEntity = (Entity) objects.iterator().next();
                fh = ksession.getFactHandle(currentEntity);
                ksession.update(fh, newEntity);
                ksession.fireAllRules();
            }
        }
        if (currentEntity == null) {
            throw new EntityNotFoundException(id);
        }
        return newEntity;
    }

    @Override
    public Map<String, Entity> evaluateUpdatedEntities(
            final Map<String, Entity> entities) {

        synchronized (this) {
            Collection<Object> objects = ksession
                    .getObjects(new ObjectFilter() {

                        @Override
                        public boolean accept(Object object) {
                            return object instanceof Entity
                                    && entities.containsKey(((Entity) object)
                                            .getId());
                        }
                    });
            if (objects != null && objects.size() > 0) {
                FactHandle fh = null;
                Entity newEntity = null;
                Entity currentEntity = null;
                for (Object obj : objects) {
                    currentEntity = (Entity) obj;
                    newEntity = entities.get(currentEntity.getId());
                    fh = ksession.getFactHandle(currentEntity);
                    ksession.update(fh, newEntity);
                }
                ksession.fireAllRules();

                // update all entity instances possibly returned to the client
                for (Object obj : objects) {
                    entities.put(((Entity) obj).getId(), (Entity) obj);
                }
            }
        }
        return entities;
    }

    @Override
    public Object getGlobalVariable(String variableName)
            throws GlobalVariableNotFoundException {
        Object value = null;
        synchronized (this) {
            value = ksession.getGlobal(variableName);
        }
        if (value == null) {
            throw new GlobalVariableNotFoundException(variableName);
        }
        return value;
    }

    @Override
    public void setGlobalVariable(String identifier, Object value) {
        synchronized (this) {
            ksession.setGlobal(identifier, value);
            ksession.fireAllRules();
        }
    }
}

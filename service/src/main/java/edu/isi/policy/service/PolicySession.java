package edu.isi.policy.service;

import java.util.Collection;
import java.util.Map;

import edu.isi.policy.entity.Entity;
import edu.isi.policy.exception.EntityNotFoundException;
import edu.isi.policy.exception.GlobalVariableNotFoundException;

/**
 * Interface for an active policy session.
 * 
 * @author David Smith
 * 
 */
public interface PolicySession {

    /**
     * Returns all instances in memory of an object class
     * 
     * @param c
     *            the object class
     * @return a collection of objects, or null if none were found
     */
    @SuppressWarnings("rawtypes")
    public Collection<Object> getAllObjectInstances(Class c);

    /**
     * Retrieves an entity from the session
     * 
     * @param entityId
     *            ID of the entity
     * @return the entity, or null if not found
     * @throws EntityNotFoundException
     *             if the entity cannot be found
     */
    public Entity getEntity(String entityId) throws EntityNotFoundException;

    /**
     * Removes an entity from the session
     * 
     * @param entityId
     *            ID of the entity
     * @return the entity that was removed
     * @throws EntityNotFoundException
     *             if the entity does not exist
     */
    public Entity removeEntity(String entityId) throws EntityNotFoundException;

    /**
     * Inserts a new entity into the session and runs policy rules over it
     * 
     * @param entity
     *            the entity
     */
    public Object evaluateNewEntity(Object entity);

    /**
     * Updates an existing entity in the session and runs policy rules over it
     * 
     * @param id
     *            ID of the existing entity
     * @param newEntity
     *            the new entity object to update
     * @return
     * @throws EntityNotFoundException
     *             if the entity does not exist
     */
    public Entity evaluateUpdatedEntity(String id, Entity newEntity)
            throws EntityNotFoundException;

    /**
     * Updates a collection of entities and runs policy rules over it
     * 
     * @param entities
     *            the entities to update, mapped by ID
     */
    public Map<String, Entity> evaluateUpdatedEntities(
            Map<String, Entity> entities);

    /**
     * 
     * @param variableName
     *            name of the global variable
     * @return the value of the global variable
     * @throws GlobalVariableNotFoundException
     *             if the global variable does not exist
     */
    public Object getGlobalVariable(String variableName)
            throws GlobalVariableNotFoundException;

    /**
     * Sets a global variable
     * 
     * @param identifier
     *            the name of the variable
     * @param value
     *            the value of the variable
     */
    public void setGlobalVariable(String identifier, Object value);
}

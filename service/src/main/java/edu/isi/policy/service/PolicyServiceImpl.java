package edu.isi.policy.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.policy.entity.Cleanup;
import edu.isi.policy.entity.Entity;
import edu.isi.policy.entity.Resource;
import edu.isi.policy.entity.Transfer;
import edu.isi.policy.exception.EntityNotFoundException;
import edu.isi.policy.exception.GlobalVariableNotFoundException;
import edu.isi.policy.util.CleanupList;
import edu.isi.policy.util.IdentityGenerator;
import edu.isi.policy.util.TransferList;

/**
 * Implementation of the Policy Service.
 * 
 * @author David Smith
 * 
 */
public class PolicyServiceImpl implements PolicyService {

    private PolicySession policySession;

    @Override
    public TransferList addTransfers(final TransferList transfers) {
        if (transfers == null || transfers.size() == 0) {
            throw new IllegalArgumentException("Transfers must be specified.");
        }
        for (final Transfer t : transfers) {
            if (!t.hasId()) {
                t.setId(IdentityGenerator.generateId());
            }
        }
        policySession.evaluateNewEntity(transfers);
        return transfers;
    }

    @Override
    public Transfer removeTransfer(final String transferId)
            throws EntityNotFoundException {
        if (transferId == null || transferId.length() == 0) {
            throw new IllegalArgumentException("Transfer ID must be specified.");
        }
        final Transfer transfer = (Transfer) policySession
                .removeEntity(transferId);
        return transfer;
    }

    @Override
    public TransferList getTransfers() {
        final Collection<Object> objs = policySession
                .getAllObjectInstances(Transfer.class);
        final TransferList l = new TransferList(objs.size());
        for (Object obj : objs) {
            l.add((Transfer) obj);
        }
        return l;
    }

    @Override
    public Transfer getTransfer(final String transferId)
            throws EntityNotFoundException {
        if (transferId == null || transferId.length() == 0) {
            throw new IllegalArgumentException("Transfer ID must be specified.");
        }
        return (Transfer) policySession.getEntity(transferId);
    }

    @Override
    public Cleanup getCleanup(final String cleanupId)
            throws EntityNotFoundException {
        if (cleanupId == null || cleanupId.length() == 0) {
            throw new IllegalArgumentException("Cleanup ID must be specified.");
        }
        return (Cleanup) policySession.getEntity(cleanupId);
    }

    @Override
    public Transfer updateTransfer(final String transferId,
            final Transfer newTransfer) throws EntityNotFoundException {
        if (transferId == null || transferId.length() == 0) {
            throw new IllegalArgumentException("Transfer ID must be specified.");
        }
        if (newTransfer == null) {
            throw new IllegalArgumentException(
                    "New Transfer must be specified.");
        }
        return (Transfer) policySession.evaluateUpdatedEntity(transferId,
                newTransfer);
    }

    @Override
    public PolicySession getPolicySession() {
        return policySession;
    }

    @Override
    public void setPolicySession(final PolicySession policySession) {
        if (policySession == null) {
            throw new IllegalArgumentException(
                    "Policy Session must be specified.");
        }
        this.policySession = policySession;
    }

    @Override
    public List<Resource> getResources() {
        List<Resource> resources = null;
        Collection<Object> objs = policySession
                .getAllObjectInstances(Resource.class);
        if (objs != null) {
            resources = new ArrayList<Resource>(objs.size());
            for (Object obj : objs) {
                resources.add((Resource) obj);
            }
        }
        return resources;
    }

    @Override
    public Cleanup addCleanup(Cleanup cleanup) {
        if (cleanup == null) {
            throw new IllegalArgumentException("Cleanup must be specified.");
        }
        policySession.evaluateNewEntity(cleanup);
        return cleanup;
    }

    @Override
    public CleanupList addCleanups(CleanupList cleanups) {
        if (cleanups == null || cleanups.size() == 0) {
            throw new IllegalArgumentException("Cleanups must be specified.");
        }
        for (final Cleanup rc : cleanups) {
            if (!rc.hasId()) {
                rc.setId(IdentityGenerator.generateId());
            }
        }
        cleanups = (CleanupList) policySession.evaluateNewEntity(cleanups);
        return cleanups;
    }

    @Override
    public Cleanup removeCleanup(String id) throws EntityNotFoundException {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("Cleanup ID must be specified.");
        }
        final Cleanup cleanup = (Cleanup) policySession.removeEntity(id);
        return cleanup;
    }

    @Override
    public Cleanup updateCleanup(String cleanupId, Cleanup newCleanup)
            throws EntityNotFoundException {
        if (cleanupId == null || cleanupId.length() == 0) {
            throw new IllegalArgumentException("Cleanup ID must be specified.");
        }
        if (newCleanup == null) {
            throw new IllegalArgumentException("New Cleanup must be specified.");
        }
        policySession.evaluateUpdatedEntity(cleanupId, newCleanup);
        return newCleanup;
    }

    @Override
    public CleanupList getCleanups() {
        final Collection<Object> objs = policySession
                .getAllObjectInstances(Cleanup.class);
        CleanupList cl = null;
        if (objs != null) {
            cl = new CleanupList(objs.size());
            for (Object o : objs) {
                cl.add((Cleanup) o);
            }
        }
        return cl;
    }

    @Override
    public Transfer addTransfer(Transfer transfer) {
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer must be specified.");
        }
        if (!transfer.hasId()) {
            transfer.setId(IdentityGenerator.generateId());
        }
        transfer = (Transfer) policySession.evaluateNewEntity(transfer);
        return transfer;
    }

    @Override
    public TransferList updateTransfers(TransferList transfers) {
        if (transfers == null || transfers.size() == 0) {
            throw new IllegalArgumentException("Transfers must be specified.");
        }

        Map<String, Entity> entities = new HashMap<String, Entity>(
                transfers.size());
        // remove any transfers that don't have an ID assigned
        for (Transfer t : transfers) {
            if (t.hasId()) {
                entities.put(t.getId(), t);
            }
        }
        entities = policySession.evaluateUpdatedEntities(entities);
        TransferList updatedTransfers = new TransferList(entities.size());
        for(Entity entity:entities.values()) {
            updatedTransfers.add((Transfer) entity);
        }
        return updatedTransfers;
    }

    @Override
    public CleanupList updateCleanups(CleanupList cleanups) {
        if (cleanups == null || cleanups.size() == 0) {
            throw new IllegalArgumentException("Cleanups must be specified.");
        }
        Map<String, Entity> entities = new HashMap<String, Entity>(
                cleanups.size());
        // remove any transfers that don't have an ID assigned
        for (Cleanup c : cleanups) {
            if (c.hasId()) {
                entities.put(c.getId(), c);
            }
        }
        entities = policySession.evaluateUpdatedEntities(entities);
        CleanupList updatedCleanups = new CleanupList(entities.size());
        for (Entity entity : entities.values()) {
            updatedCleanups.add((Cleanup) entity);
        }
        return updatedCleanups;
    }

    @Override
    public Object getGlobalVariable(String variableName)
            throws GlobalVariableNotFoundException {
        if (variableName == null || variableName.length() == 0) {
            throw new IllegalArgumentException(
                    "Variable Name must be specified.");
        }
        return policySession.getGlobalVariable(variableName);
    }

    @Override
    public void setGlobalVariable(String variableName, Object value) {
        if (variableName == null || variableName.length() == 0) {
            throw new IllegalArgumentException(
                    "Variable name must be specified.");
        }

        policySession.setGlobalVariable(variableName, value);
    }
}

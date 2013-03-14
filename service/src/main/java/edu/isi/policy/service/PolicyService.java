package edu.isi.policy.service;

import java.util.List;

import edu.isi.policy.entity.Cleanup;
import edu.isi.policy.entity.Resource;
import edu.isi.policy.entity.Transfer;
import edu.isi.policy.exception.EntityNotFoundException;
import edu.isi.policy.exception.GlobalVariableNotFoundException;
import edu.isi.policy.util.CleanupList;
import edu.isi.policy.util.TransferList;

/**
 * Service that manages transfer advice based on VO-level policies
 * 
 * @author David Smith
 * 
 */
public interface PolicyService {

    /**
     * Places new transfers in the policy knowledge session
     * 
     * @param transfers
     *            the new transfers to get advice from
     * @return the transfers that the requester should invoke, including any
     *         necessary URL or properties changes
     */
    public TransferList addTransfers(TransferList transfers);

    /**
     * Places a new transfer in the policy knowledge session
     * 
     * @param transfer
     *            the transfer to add
     * @return the updated transfer from the knowledge session, or nothing if
     *         the transfer was not placed in memory
     */
    public Transfer addTransfer(Transfer transfer);

    /**
     * Removes a transfer from the knowledge session.
     * 
     * @param transferId
     *            the ID of the transfer to remove
     * @return the transfer that was removed
     * @throws EntityNotFoundException
     */
    public Transfer removeTransfer(String transferId)
            throws EntityNotFoundException;

    /**
     * 
     * @return the transfers that are currently known in the knowledge session.
     */
    public TransferList getTransfers();

    /**
     * 
     * @param transferId
     *            ID of an existing transfer
     * @return the current version of the specified transfer
     * @throws EntityNotFoundException
     *             if the transfer cannot be found
     */
    public Transfer getTransfer(String transferId)
            throws EntityNotFoundException;

    /**
     * Updates the properties on a previously created transfer
     * 
     * @param transferId
     *            the ID of the transfer to update.
     * @return returns the new transfer object resulting from the policy rules
     *         running over the new properties
     * @throws EntityNotFoundException
     *             if the transfer cannot be found
     */
    public Transfer updateTransfer(String transferId, Transfer newTransfer)
            throws EntityNotFoundException;

    /**
     * 
     * @return a new or existing policy session
     */
    public PolicySession getPolicySession();

    /**
     * Sets the policy session
     * 
     * @param policySession
     *            the policy session
     */
    public void setPolicySession(PolicySession policySession);

    /**
     * 
     * @return the list of known resources in memory
     */
    public List<Resource> getResources();

    /**
     * Adds a new cleanup request to memory
     * 
     * @param cleanup
     */
    public Cleanup addCleanup(Cleanup cleanup);

    /**
     * Removes a cleanup from memory
     * 
     * @param id
     *            the ID of the cleanup
     * @return the cleanup that was removed
     * @throws EntityNotFoundException
     *             if the cleanup does not exist
     */
    public Cleanup removeCleanup(String id) throws EntityNotFoundException;

    /**
     * 
     * @return a list of all cleanups in the system
     */
    public CleanupList getCleanups();

    /**
     * Adds a list of cleanup requests to memory.
     * 
     * @param cleanups
     *            the list of cleanups
     * @return the resource cleanups that the client should invoke
     */
    public CleanupList addCleanups(CleanupList cleanups);

    /**
     * Updates an existing cleanup in the policy service
     * 
     * @param cleanupId
     *            ID of the cleanup
     * @param newCleanup
     *            the new cleanup to update with
     * @return the modified cleanup
     * @throws EntityNotFoundException
     *             if the cleanup does not exist
     */
    public Cleanup updateCleanup(String cleanupId, Cleanup newCleanup)
            throws EntityNotFoundException;

    /**
     * Updates a list of transfers in the policy service.
     * 
     * @param transfers
     *            the transfers to update
     * @return the updated transfers
     */
    public TransferList updateTransfers(TransferList transfers);

    /**
     * Updates a list of cleanups in the policy service.
     * 
     * @param cleanups
     *            the cleanups to update
     * @return the updated cleanups
     */
    public CleanupList updateCleanups(CleanupList cleanups);

    /**
     * 
     * @param cleanupId
     *            the ID of the cleanup
     * @return the cleanup in the policy service with the given ID
     * @throws EntityNotFoundException
     *             if the cleanup cannot be found
     */
    public Cleanup getCleanup(String cleanupId) throws EntityNotFoundException;

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
     * Sets a global variable in policy
     * 
     * @param variableName
     *            name of the variable
     * @param value
     *            global value
     */
    public void setGlobalVariable(String variableName, Object value);
}

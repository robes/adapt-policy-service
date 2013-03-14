package edu.isi.policy.adapt;

/**
 * Interface that defines the resource allocation log used to track transfer
 * allocations across multiple policy instances.
 * 
 * @author David Smith
 * 
 */
public interface ResourceAllocationLog {

    /**
     * Opens the resource allocation log
     * 
     * @throws ResourceAllocationException
     *             if the log cannot be opened
     */
    public void open() throws ResourceAllocationException;

    /**
     * Closes the resource allocation log
     */
    public void close();

    /**
     * Retrieves the resource allocation entry
     * 
     * @param transferId
     *            the ID of the resource allocation
     * @return the resource allocation entry, or null if it doesn't exist
     * @throws ResourceAllocationException
     */
    public ResourceAllocation getResourceAllocation(String transferId)
            throws ResourceAllocationException;

    /**
     * Inserts a new resource allocation entry
     * 
     * @param allocation
     *            the allocation entry
     * @throws ResourceAllocationException
     *             if the entry cannot be added
     */
    public void addResourceAllocation(ResourceAllocation allocation)
            throws ResourceAllocationException;

    /**
     * Updates an existing resource allocation entry
     * 
     * @param allocation
     *            the updated allocation entry
     * @throws ResourceAllocationException
     *             if the entry cannot be updated
     */
    public void updateResourceAllocation(ResourceAllocation allocation)
            throws ResourceAllocationException;

    /**
     * Removes an existing resource allocation entry
     * 
     * @param transferId
     *            the ID of the allocation entry
     * @return the resource allocation entry removed, or null if none was
     *         removed
     * @throws ResourceAllocationException
     *             if the resource allocation entry exists but couldn't be
     *             removed
     */
    public ResourceAllocation removeResourceAllocation(String transferId)
            throws ResourceAllocationException;

    /**
     * Aggregates streams from all transfers that have resource allocation
     * entries between two hosts on all instances
     * 
     * @param sourceHost
     *            the source hostname
     * @param destinationHost
     *            the destination hostname
     * @return the total number of transfer streams
     */
    public int getAggregatedTransferStreams(String sourceHost,
            String destinationHost);

    /**
     * Aggregates rate from all transfers that have resource allocation entries
     * between two hosts on all instances
     * 
     * @param sourceHost
     *            the source hostname
     * @param destinationHost
     *            the destination hostname
     * @return the total number of transfer rate
     */
    public float getAggregatedRate(String sourceHost, String destinationHost);

    /**
     * 
     * @param sourceHost
     *            source hostname
     * @param destinationHost
     *            destination hostname
     * @return the total transfers that are occuring between two hosts
     */
    public int getNumberOfTransfers(String sourceHost, String destinationHost);
}

package edu.isi.policy.util;

import edu.isi.policy.entity.Cleanup;
import edu.isi.policy.entity.Transfer;

/**
 * Simple interface for logging transfer statistics from policy
 * 
 * @author David Smith
 * 
 */
public interface TransferStatisticsLog {

    /**
     * Records that a transfer has started
     * 
     * @param transfer
     */
    public void recordNewTransfer(Transfer transfer);

    /**
     * Records that a transfer completed
     * 
     * @param transfer
     */
    public void recordTransferCompleted(Transfer transfer);

    /**
     * Records that a transfer failed
     * 
     * @param transfer
     */
    public void recordTransferFailed(Transfer transfer);

    /**
     * Records that a cleanup started
     * 
     * @param cleanup
     */
    public void recordNewCleanup(Cleanup cleanup);

    /**
     * Records that a cleanup completed
     * 
     * @param cleanup
     */
    public void recordCleanupCompleted(Cleanup cleanup);

    /**
     * 
     * @return the total number of transfers started
     */
    public long getNumberOfNewTransfers();

    /**
     * 
     * @return the total number of transfers completed
     */
    public long getNumberOfTransfersCompleted();

    /**
     * 
     * @return the total number of transfers failed
     */
    public long getNumberOfTransfersFailed();

    /**
     * 
     * @return the total number of duplicate transfers requested
     */
    public long getNumberOfDuplicateTransfersRequested();

    /**
     * 
     * @return the total number of cleanups started
     */
    public long getNumberOfNewCleanups();

    /**
     * 
     * @return the total number of cleanups completed
     */
    public long getNumberOfCleanupsCompleted();

    /**
     * Records a duplicate transfer request
     * 
     * @param transfer
     */
    public void recordDuplicateTransferRequested(Transfer transfer);

    /**
     * 
     * @return the total MB transferred
     */
    public double getMBTransferred();

    /**
     * 
     * @return the total MB avoided by excluding duplicate transfers
     */
    public double getDuplicateMBAvoided();
}

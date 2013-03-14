package edu.isi.policy.util;

import edu.isi.policy.entity.Cleanup;
import edu.isi.policy.entity.Transfer;

/**
 * Implementation of the TransferStatisticsLog interface.
 * 
 * @author David Smith
 * 
 */
public class TransferStatisticsLogImpl implements TransferStatisticsLog {

    private volatile long numberOfNewTransfers = 0L;
    private volatile long numberOfTransfersCompleted = 0L;
    private volatile long numberOfTransfersFailed = 0L;
    private volatile long numberOfDuplicateTransfersRequested = 0L;
    private volatile long numberOfNewCleanups = 0L;
    private volatile long numberOfCleanupsCompleted = 0L;
    private volatile double mbytesTransferred = 0;
    private volatile double duplicateMBytesAvoided = 0;

    @Override
    public void recordTransferCompleted(Transfer transfer) {
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer must be specified.");
        }
        synchronized (this) {
            numberOfTransfersCompleted++;

            // TODO: get bytes transferred
        }

    }

    @Override
    public void recordCleanupCompleted(Cleanup cleanup) {
        if (cleanup == null) {
            throw new IllegalArgumentException("Cleanup must be specified.");
        }
        synchronized (this) {
            numberOfCleanupsCompleted++;
        }

    }

    @Override
    public long getNumberOfTransfersCompleted() {
        return numberOfTransfersCompleted;
    }

    @Override
    public void recordDuplicateTransferRequested(Transfer transfer) {
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer must be specified.");
        }
        synchronized (this) {
            numberOfDuplicateTransfersRequested++;

            // TODO: get size of transfer
        }

    }

    @Override
    public long getNumberOfDuplicateTransfersRequested() {
        return numberOfDuplicateTransfersRequested;
    }

    @Override
    public long getNumberOfCleanupsCompleted() {
        return numberOfCleanupsCompleted;
    }

    @Override
    public double getMBTransferred() {
        return mbytesTransferred;
    }

    @Override
    public double getDuplicateMBAvoided() {
        return duplicateMBytesAvoided;
    }

    @Override
    public void recordTransferFailed(Transfer transfer) {
        if (transfer == null) {
            throw new IllegalArgumentException("Cleanup must be specified.");
        }
        synchronized (this) {
            numberOfTransfersFailed++;
        }

    }

    @Override
    public long getNumberOfTransfersFailed() {
        return numberOfTransfersFailed;
    }

    @Override
    public void recordNewTransfer(Transfer transfer) {
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer must be specified.");
        }
        synchronized (this) {
            numberOfNewTransfers++;
        }

    }

    @Override
    public void recordNewCleanup(Cleanup cleanup) {
        if (cleanup == null) {
            throw new IllegalArgumentException("Cleanup must be specified.");
        }
        synchronized (this) {
            numberOfNewCleanups++;
        }

    }

    @Override
    public long getNumberOfNewTransfers() {
        return numberOfNewTransfers;
    }

    @Override
    public long getNumberOfNewCleanups() {
        return numberOfNewCleanups;
    }
}

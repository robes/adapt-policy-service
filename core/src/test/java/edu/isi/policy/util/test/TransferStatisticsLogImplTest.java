package edu.isi.policy.util.test;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import edu.isi.policy.entity.Cleanup;
import edu.isi.policy.entity.Transfer;
import edu.isi.policy.util.IdentityGenerator;
import edu.isi.policy.util.TransferStatisticsLogImpl;

public class TransferStatisticsLogImplTest {

    private TransferStatisticsLogImpl log;

    @Before
    public void before() {
        log = new TransferStatisticsLogImpl();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecordNewTransfer_nullArg() {
        log.recordNewTransfer(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecordTransferCompleted_nullArg() {
        log.recordTransferCompleted(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecordTransferFailed_nullArg() {
        log.recordTransferFailed(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecordNewCleanup_nullArg() {
        log.recordNewCleanup(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecordCleanupCompleted_nullArg() {
        log.recordCleanupCompleted(null);
    }

    @Test
    public void testRecordTransfers() throws URISyntaxException {
        assertEquals(0, log.getNumberOfNewTransfers());
        assertEquals(0, log.getNumberOfDuplicateTransfersRequested());
        assertEquals(0, log.getNumberOfTransfersCompleted());
        assertEquals(0, log.getNumberOfTransfersFailed());

        log.recordNewTransfer(new Transfer(IdentityGenerator.generateId(), new URI("gsiftp://server1.isi.edu/tmp/test1/"), new URI("gsiftp://client1.isi.edu/tmp/test1/")));
        assertEquals(1, log.getNumberOfNewTransfers());
        log.recordDuplicateTransferRequested(new Transfer(IdentityGenerator.generateId(), new URI("gsiftp://server1.isi.edu/tmp/test1/"), new URI("gsiftp://client1.isi.edu/tmp/test1/")));
        assertEquals(1, log.getNumberOfNewTransfers());
        assertEquals(1, log.getNumberOfDuplicateTransfersRequested());
        log.recordNewTransfer(new Transfer(IdentityGenerator.generateId(),
                new URI("gsiftp://server2.isi.edu/tmp/test2/"), new URI(
                        "gsiftp://client2.isi.edu/tmp/test2/")));
        assertEquals(2, log.getNumberOfNewTransfers());
        assertEquals(1, log.getNumberOfDuplicateTransfersRequested());

        log.recordTransferCompleted(new Transfer(
                IdentityGenerator.generateId(), new URI(
                        "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                                "gsiftp://client1.isi.edu/tmp/test1/")));
        assertEquals(1, log.getNumberOfTransfersCompleted());
        assertEquals(0, log.getNumberOfTransfersFailed());

        log.recordTransferFailed(new Transfer(IdentityGenerator.generateId(),
                new URI("gsiftp://server2.isi.edu/tmp/test2/"), new URI(
                        "gsiftp://client2.isi.edu/tmp/test2/")));
        assertEquals(1, log.getNumberOfTransfersFailed());
    }

    @Test
    public void testRecordCleanups() throws URISyntaxException {
        assertEquals(0, log.getNumberOfNewCleanups());
        assertEquals(0, log.getNumberOfCleanupsCompleted());

        log.recordNewCleanup(new Cleanup(new URI(
                "gsiftp://server1.isi.edu/test/myfile.tif")));
        assertEquals(1, log.getNumberOfNewCleanups());
        assertEquals(0, log.getNumberOfCleanupsCompleted());

        log.recordNewCleanup(new Cleanup(new URI(
                "gsiftp://server2.isi.edu/test2/")));
        assertEquals(2, log.getNumberOfNewCleanups());

        log.recordCleanupCompleted(new Cleanup(new URI(
                "gsiftp://server1.isi.edu/test/myfile.tif")));
        assertEquals(1, log.getNumberOfCleanupsCompleted());
    }

}

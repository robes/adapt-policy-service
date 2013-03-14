package edu.isi.policy.drools.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import edu.isi.policy.entity.Transfer;
import edu.isi.policy.exception.EntityNotFoundException;
import edu.isi.policy.service.PolicyServiceImpl;
import edu.isi.policy.util.IdentityGenerator;
import edu.isi.policy.util.TransferList;

public class DroolsPolicyManager_PDPS_Test extends DroolsTest {

    @Test
    public void testHandleTransferRequests_submitRequestsToPDPS()
            throws URISyntaxException {
        final PolicyServiceImpl manager = createPolicyService("test-pdps-submission.drl");
        final TransferList transfers = new TransferList();

        // this transfer should be removed and submitted for the PDPS
        final URI s1 = new URI("file:///home/smithd/globus_test/");
        final URI d1 = new URI("gsiftp://jacoby.isi.edu/tmp/globus_test/");
        final Transfer t1 = new Transfer(IdentityGenerator.generateId(), s1, d1);

        // this transfer should remain
        final URI s2 = new URI("gsiftp://jacoby.isi.edu/tmp/globus_test/");
        final URI d2 = new URI("file:///tmp/globus_test/");
        final Transfer t2 = new Transfer(IdentityGenerator.generateId(), s2, d2);

        transfers.add(t1);
        transfers.add(t2);
        final TransferList results = manager.addTransfers(transfers);
        assertNotNull(results);
        assertEquals(results.size(), 1);
        Transfer result = results.iterator().next();
        assertEquals(result.getSource(), s2);
        assertEquals(result.getDestination(), d2);
    }

    @Test
    public void testGetCurrentTransfersAndNotifyTransferComplete_submitRequestsToPDPS()
            throws URISyntaxException, EntityNotFoundException {
        final PolicyServiceImpl manager = createPolicyService("test-check-complete.drl");
        final TransferList transfers = new TransferList();

        // create a few transfers
        final URI s1 = new URI("file:///home/smithd/globus_test2/");
        final URI d1 = new URI("gsiftp://jacoby.isi.edu/tmp/globus_test2/");
        final Transfer t1 = new Transfer(IdentityGenerator.generateId(), s1, d1);

        final URI s2 = new URI("file:///home/smithd/globus_test/");
        final URI d2 = new URI("gsiftp://jacoby.isi.edu/tmp/globus_test/");
        final Transfer t2 = new Transfer(IdentityGenerator.generateId(), s2, d2);

        final URI s3 = new URI("gsiftp://jacoby.isi.edu/tmp/globus_test2/");
        final URI d3 = new URI("file:///tmp/globus_test2/");
        final Transfer t3 = new Transfer(IdentityGenerator.generateId(), s3, d3);

        final URI s4 = new URI("gsiftp://jacoby.isi.edu/tmp/globus_test/");
        final URI d4 = new URI("file:///tmp/globus_test/");
        final Transfer t4 = new Transfer(IdentityGenerator.generateId(), s4, d4);

        transfers.add(t1);
        transfers.add(t2);
        transfers.add(t3);
        transfers.add(t4);

        TransferList results = manager.addTransfers(transfers);
        assertNotNull(results);
        assertEquals(results.size(), 4);

        // make sure getting current transfers returns them all
        results = manager.getTransfers();
        assertNotNull(results);
        assertEquals(results.size(), 4);

        // mark a single transfer as completed
        manager.removeTransfer(t1.getId());
        results = manager.getTransfers();
        assertNotNull(results);
        assertEquals(results.size(), 3);

        // make sure t1 is not in the results
        for (Transfer result : results) {
            assertFalse(result.equals(t1));
        }

        // mark the rest of the transfers as complete (including the one already
        // completed)
        manager.removeTransfer(t2.getId());
        manager.removeTransfer(t3.getId());
        manager.removeTransfer(t4.getId());
        results = manager.getTransfers();
        assertNotNull(results != null);
        assertEquals(results.size(), 0);
    }
}

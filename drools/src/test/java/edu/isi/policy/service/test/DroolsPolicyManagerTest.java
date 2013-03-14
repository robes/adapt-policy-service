package edu.isi.policy.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import edu.isi.policy.entity.Transfer;
import edu.isi.policy.exception.EntityNotFoundException;
import edu.isi.policy.service.PolicyService;
import edu.isi.policy.util.IdentityGenerator;
import edu.isi.policy.util.TransferList;

public class DroolsPolicyManagerTest extends DroolsTest {


    @Test
    public void testConstructor() {
        createPolicyService(null);
    }

    @Test
    public void testDefault() throws URISyntaxException {
        final PolicyService manager = createPolicyService(null);
        final URI u1 = new URI("file:///home/smithd/test1.txt");
        final URI u2 = new URI("gsiftp://jacoby.isi.edu/tests/test1.txt");
        final URI u3 = new URI("gsiftp://grid2.isi.edu/tests/test1.txt");
        final URI u4 = new URI("gsiftp://jacoby.isi.edu/tests/test2.txt");
        final URI u5 = new URI("file:///home/smithd/test2.txt");
        final Transfer t1 = new Transfer(u1, u2);
        final Transfer t2 = new Transfer(u2, u3);
        final Transfer t3 = new Transfer(u3, u1);
        final Transfer t4 = new Transfer(u4, u5);
        final Transfer t5 = new Transfer(u5, u4);

        final TransferList request = new TransferList();
        request.add(t1);
        request.add(t2);
        request.add(t3);
        request.add(t4);
        request.add(t5);

        final TransferList response = manager.addTransfers(request);
        final Transfer r1 = response.get(0);
        final Transfer r2 = response.get(1);
        final Transfer r3 = response.get(2);
        final Transfer r4 = response.get(3);
        final Transfer r5 = response.get(4);

        assertEquals(r1, t1);
        assertEquals(r2, t5);
        assertEquals(r3, t3);
        assertEquals(r4, t2);
        assertEquals(r5, t4);

        assertEquals(manager.getTransfers().size(), 5);

        String group_id = r1.getProperty("group_id");
        assertTrue(group_id != null && group_id.length() > 0
                && group_id.equals(r2
                        .getProperty("group_id")));

        assertEquals(r4.getProperty("parallel"), "4");
        assertNull(r1.getProperty("parallel"));
        assertNull(r2.getProperty("parallel"));
    }

    @Test
    public void testTransferRequests_noRules() throws URISyntaxException {
        final PolicyService manager = createPolicyService(null);
        final URI sourceResource = new URI("file:///home/smithd/globus_test/");
        final URI destinationResource = new URI(
                "gsiftp://jacoby.isi.edu/tmp/globus_test/");
        final Transfer testTransfer = new Transfer(sourceResource,
                destinationResource);
        final TransferList transfers = new TransferList();
        transfers.add(
                testTransfer
                );
        final TransferList results = manager.addTransfers(transfers);
        assertNotNull(results);
        assertEquals(results.size(), 1);
        final Transfer firstResult = results.iterator().next();
        assertEquals(firstResult, testTransfer);
        assertNotNull(firstResult.getId());
        assertTrue(firstResult.getId().length() > 0);
    }

    @Test
    public void testHandleTransferRequests_filterRule() throws URISyntaxException {
        final PolicyService manager = createPolicyService("test-filter.drl");
        final TransferList transfers = new TransferList();

        // this transfer should be removed
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
        final Transfer firstResult = results.iterator().next();
        assertEquals(firstResult, t2);
    }

    @Test
    public void testHandleTransferRequests_setPropertiesRule() throws URISyntaxException {
        final PolicyService manager = createPolicyService("test-setProperties.drl");
        final TransferList transfers = new TransferList();

        // this transfer should have priority set to 10
        final URI s1 = new URI("file:///home/smithd/globus_test/");
        final URI d1 = new URI("gsiftp://jacoby.isi.edu/tmp/globus_test/");
        final Transfer t1 = new Transfer(IdentityGenerator.generateId(), s1, d1);

        // this transfer should have its buffer property set to 2048 by rules
        final URI s2 = new URI("gsiftp://jacoby.isi.edu/tmp/globus_test/");
        final URI d2 = new URI("file:///tmp/globus_test/");
        final Transfer t2 = new Transfer(IdentityGenerator.generateId(), s2, d2);
        t2.setProperty("BUFFER_SIZE", "1024");

        transfers.add(t1);
        transfers.add(t2);
        final TransferList results = manager.addTransfers(transfers);
        assertNotNull(results);
        assertEquals(results.size(), 2);

        Transfer priorityTransfer = null;
        Transfer bufferTransfer = null;
        for(Transfer r:results) {
            if (r.equals(t1)) {
                priorityTransfer = r;
            }
            else if (r.equals(t2)) {
                bufferTransfer = r;
            }
        }

        assertNotNull(priorityTransfer);
        assertEquals("10", priorityTransfer.getProperty("PRIORITY"));
        assertNotNull(bufferTransfer);
        assertEquals("2048", bufferTransfer.getProperty("BUFFER_SIZE"));
    }

    @Test
    public void testGetTransfer() throws URISyntaxException,
    EntityNotFoundException {
        final PolicyService policyService = createPolicyService(null);
        try {
            policyService.getTransfer("5436436436346332");
            fail("Expected EntityNotFoundException");
        } catch (EntityNotFoundException e) {
        }
        final TransferList transfers = new TransferList();

        // create a few transfers
        final URI s1 = new URI("file:///home/smithd/globus_test2/");
        final URI d1 = new URI("gsiftp://jacoby.isi.edu/tmp/globus_test2/");
        final Transfer t1 = new Transfer(IdentityGenerator.generateId(), s1, d1);
        transfers.add(t1);

        final TransferList result = policyService.addTransfers(transfers);
        assertNotNull(result);
        assertEquals(result.size(), 1);
        final Transfer t = result.get(0);
        final Transfer r = policyService.getTransfer(t.getId());
        assertNotNull(r);
        assertEquals(r.getId(), t.getId());
        assertEquals(r.getSource(), t.getSource());
        assertEquals(r.getDestination(), t.getDestination());
    }
}

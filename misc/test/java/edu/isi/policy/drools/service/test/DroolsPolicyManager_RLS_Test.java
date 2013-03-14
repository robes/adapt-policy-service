package edu.isi.policy.drools.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;

import org.junit.Test;

import edu.isi.policy.entity.Transfer;
import edu.isi.policy.service.PolicyServiceImpl;
import edu.isi.policy.util.TransferList;

public class DroolsPolicyManager_RLS_Test extends DroolsTest {
    @Test
    public void testReplicaFunctionality() throws URISyntaxException {
        final PolicyServiceImpl policyService = createPolicyService("test-replica.drl");
        final Transfer t1 = new Transfer(
                "rls://jacoby.isi.edu/globus_test_directory",
                "file:///tmp/replica_download/");
        TransferList request = new TransferList();
        request.add(t1);
        TransferList results = policyService.addTransfers(request);
        assertNotNull(results);
        assertEquals(results.size(), 1);
        assertEquals("gsiftp://jacoby.isi.edu/home/smithd/globus_test/",
                results.get(0).getSource().toString());

        final Transfer t2 = new Transfer(
                "rls://jacoby.isi.edu/globus_test_2.txt",
                "file:///tmp/globus_test_2.txt");
        final Transfer t3 = new Transfer(
                "rls://jacoby.isi.edu/globus_test_3.txt",
                "file:///tmp/globus_test_3.txt");
        request.clear();
        request.add(t2);
        request.add(t3);

        results = policyService.addTransfers(request);
        assertNotNull(results);
        assertEquals(results.size(), 2);
        assertEquals(
                "gsiftp://high-bandwidth-server.isi.edu/home/smithd/test_2.txt",
                results.get(0).getSource().toString());
        assertEquals("gsissh://anothersite.isi.edu/home/smithd/test_3.txt",
                results.get(1).getSource().toString());

    }
}

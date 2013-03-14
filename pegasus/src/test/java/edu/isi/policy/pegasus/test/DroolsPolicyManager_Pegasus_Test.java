package edu.isi.policy.pegasus.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.isi.policy.entity.Cleanup;
import edu.isi.policy.entity.Resource;
import edu.isi.policy.entity.Transfer;
import edu.isi.policy.exception.EntityNotFoundException;
import edu.isi.policy.exception.GlobalVariableNotFoundException;
import edu.isi.policy.service.AbstractPolicySession;
import edu.isi.policy.service.PolicyService;
import edu.isi.policy.service.PolicyServiceImpl;
import edu.isi.policy.service.PolicySession;
import edu.isi.policy.service.drools.DroolsStatefulPolicySession;
import edu.isi.policy.util.CleanupList;
import edu.isi.policy.util.TransferList;
import edu.isi.policy.util.TransferStatisticsLog;

public class DroolsPolicyManager_Pegasus_Test {

    private PolicyService policyService;

    private static final String rulesFile = "pegasus.drl";

    @Before
    public void before() {
        policyService = createPolicyService(null);
    }

    private PolicyService createPolicyService(String[] rulesFiles) {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "/WEB-INF/policy-context.xml");
        policyService = (PolicyServiceImpl) context.getBean("policyService");
        if (rulesFiles != null) {
            PolicySession session = new DroolsStatefulPolicySession(
                    ((AbstractPolicySession) policyService.getPolicySession())
                    .getGlobalVariables(),
                    Arrays.asList(rulesFiles));
            policyService.setPolicySession(session);
        } else {
            PolicySession session = new DroolsStatefulPolicySession(
                    ((AbstractPolicySession) policyService.getPolicySession())
                    .getGlobalVariables(),
                    Arrays.asList(rulesFile));
            policyService.setPolicySession(session);
        }
        return policyService;
    }

    @Test
    public void testAdd3rdPartyTransfers() throws URISyntaxException {
        TransferList tl = new TransferList();
        tl.add(new Transfer("gsiftp:///server1.isi.edu/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));
        tl.add(new Transfer("gsiftp:///server2.isi.edu/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/"));
        tl.add(new Transfer("gsiftp://server3.isi.edu/test3/",
                "gsiftp://client1.isi.edu/tmp/test3/"));

        TransferList result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(3, result.size());

        // make sure we have 3 resources allocated
        List<Resource> resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(3, resources.size());
        for (Resource r : resources) {
            assertEquals(1, r.getNumberOfJobs());
        }

        tl.clear();
        tl.add(new Transfer("gsiftp:///server1.isi.edu/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));
        tl.add(new Transfer("gsiftp:///server2.isi.edu/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/"));
        tl.add(new Transfer("gsiftp:///server4.isi.edu/test4/",
                "gsiftp://client1.isi.edu/tmp/test4/"));

        // only 1 transfer should be sent back
        result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(1, result.size());

        resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(4, resources.size());
        for (Resource r : resources) {
            if ("/tmp/test4/".equals(r.getFile()
                    .toString())
                    || "/tmp/test3/".equals(r.getFile()
                            .toString())) {
                assertEquals(1, r.getNumberOfJobs());
            } else {
                assertEquals(2, r.getNumberOfJobs());
            }
        }
        tl.clear();
        tl.add(new Transfer("gsiftp://server5.isi.edu/tmp/test5/",
                "gsiftp://client1.isi.edu/tmp/test5/"));
        tl.add(new Transfer("gsiftp://server5.isi.edu/tmp/test5/",
                "gsiftp://client1.isi.edu/tmp/test5/"));
        result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(1, result.size());

        resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(5, resources.size());
        for (Resource r : resources) {
            if ("/tmp/test5/".equals(r.getFile().toString())) {
                assertEquals(2, r.getNumberOfJobs());
            }
        }
    }

    @Test
    public void testAddTwoPartyDownloads() throws URISyntaxException {
        TransferList tl = new TransferList();
        Transfer t = new Transfer("gsiftp:///server1.isi.edu/test1/",
                "file:///tmp/test1/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        t = new Transfer("gsiftp:///server2.isi.edu/test2/",
                "file:///tmp/test2/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        t = new Transfer("gsiftp://server3.isi.edu/test3/",
                "file:///tmp/test3/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        TransferList result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(3, result.size());

        // make sure we have 3 resources allocated
        List<Resource> resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(3, resources.size());
        for (Resource r : resources) {
            assertEquals(1, r.getNumberOfJobs());
        }

        tl.clear();
        t = new Transfer("gsiftp:///server1.isi.edu/test1/",
                "file:///tmp/test1/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);
        t = new Transfer("gsiftp:///server2.isi.edu/test2/",
                "file:///tmp/test2/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        t = new Transfer("gsiftp:///server4.isi.edu/test4/",
                "file:///tmp/test4/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        // only 1 transfer should be sent back
        result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(1, result.size());

        resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(4, resources.size());
        for (Resource r : resources) {
            if ("/tmp/test4/".equals(r.getFile()
                    .toString())
                    || "/tmp/test3/".equals(r.getFile()
                            .toString())) {
                assertEquals(1, r.getNumberOfJobs());
            } else {
                assertEquals(2, r.getNumberOfJobs());
            }
        }
        tl.clear();

        // duplicates on same list
        t = new Transfer("gsiftp:///server5.isi.edu/test5/",
                "file:///tmp/test5/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);
        t = new Transfer("gsiftp:///server5.isi.edu/test5/",
                "file:///tmp/test5/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);
        result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(1, result.size());

        resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(5, resources.size());
        for (Resource r : resources) {
            if ("/tmp/test5/".equals(r.getFile().toString())) {
                assertEquals(2, r.getNumberOfJobs());
            }
        }

        // insert downloads to two hosts, same path
        tl.clear();
        t = new Transfer("gsiftp://server6.isi.edu/test6/",
                "file:///tmp/test6/");
        t.setProperty("local_file_host", "clienta.isi.edu");
        tl.add(t);
        t = new Transfer("gsiftp://server6.isi.edu/test6/",
                "file:///tmp/test6/");
        t.setProperty("local_file_host", "clientb.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(2, result.size());
        for (Transfer r : result) {
            assertEquals("file:///tmp/test6/", r.getDestination().toString());
        }

        boolean foundT1 = false;
        boolean foundT2 = false;
        resources = policyService.getResources();
        for (Resource r : resources) {
            if ("/tmp/test6/".equals(r.getFile())
                    && "clienta.isi.edu".equals(r
                            .getHost())) {
                if (!foundT1) {
                    foundT1 = true;
                } else {
                    fail("Found unexpected resource: " + r.toString());
                }
            } else if ("/tmp/test6/".equals(r.getFile())
                    && "clientb.isi.edu".equals(r
                            .getHost())) {
                if (!foundT2) {
                    foundT2 = true;
                } else {
                    fail("Found unexpected resource: " + r.toString());
                }
            }
        }
        assertTrue(foundT1);
        assertTrue(foundT2);
    }

    @Test
    public void testAddTwoPartyUploads() throws URISyntaxException {
        TransferList tl = new TransferList();
        Transfer t = new Transfer("file:///tmp/test1/",
                "gsiftp://server1.isi.edu/test1/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        t = new Transfer("file:///tmp/test2/",
                "gsiftp://server2.isi.edu/test2/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        t = new Transfer("file:///tmp/test3/",
                "gsiftp://server3.isi.edu/test3/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        TransferList result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(3, result.size());

        // make sure we have 3 resources allocated
        List<Resource> resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(3, resources.size());
        for (Resource r : resources) {
            assertEquals(1, r.getNumberOfJobs());
        }

        tl.clear();
        t = new Transfer("file:///tmp/test1/",
                "gsiftp://server1.isi.edu/test1/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);
        t = new Transfer("file:///tmp/test2/",
                "gsiftp://server2.isi.edu/test2/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        t = new Transfer("file:///tmp/test4/",
                "gsiftp://server4.isi.edu/test4/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        // only 1 transfer should be sent back
        result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(1, result.size());

        resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(4, resources.size());
        for (Resource r : resources) {
            if ("/test4/".equals(r.getFile()) || "/test3/".equals(r.getFile())) {
                assertEquals(1, r.getNumberOfJobs());
            } else {
                assertEquals(2, r.getNumberOfJobs());
            }
        }
        tl.clear();

        // duplicates on same list
        t = new Transfer("file:///tmp/test5/",
                "gsiftp://server5.isi.edu/test5/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);
        t = new Transfer("file:///tmp/test5/",
                "gsiftp://server5.isi.edu/test5/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);
        result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(1, result.size());

        resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(5, resources.size());
        for (Resource r : resources) {
            if ("/test5/".equals(r.getFile())) {
                assertEquals(2, r.getNumberOfJobs());
            }
        }
    }

    @Test
    public void testAdd3rdPartyCleanup() throws URISyntaxException {
        TransferList tl = new TransferList();
        tl.add(new Transfer("gsiftp://server1.isi.edu/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));
        tl.add(new Transfer("gsiftp:///server2.isi.edu/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/"));
        tl.add(new Transfer("gsiftp://server3.isi.edu/test3/",
                "gsiftp://client1.isi.edu/tmp/test3/"));
        tl.add(new Transfer("gsiftp://server3.isi.edu/test3/",
                "gsiftp://client1.isi.edu/tmp/test3/"));

        TransferList result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(3, result.size());

        List<Resource> resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(3, resources.size());
        for (Resource r : resources) {
            if ("/tmp/test3/".equals(r.getFile()
                    .toString())) {
                assertEquals(2, r.getNumberOfJobs());
            } else {
                assertEquals(1, r.getNumberOfJobs());
            }
        }

        // add cleanups
        CleanupList cl = new CleanupList();
        cl.add(new Cleanup("gsiftp://client1.isi.edu/tmp/test1/"));
        cl.add(new Cleanup("gsiftp://client1.isi.edu/tmp/test3/"));
        CleanupList cleanups = policyService.addCleanups(cl);
        assertNotNull(cleanups);
        assertEquals(1, cleanups.size());
        assertEquals("gsiftp://client1.isi.edu/tmp/test1/", cleanups.get(0)
                .getUri().toString());

        resources = policyService.getResources();
        for (Resource r : resources) {
            assertEquals(1, r.getNumberOfJobs());
        }

        cl.clear();
        cl.add(new Cleanup("gsiftp://client1.isi.edu/tmp/test3/"));
        cleanups = policyService.addCleanups(cl);
        assertNotNull(cleanups);
        assertEquals(1, cleanups.size());
        assertEquals("gsiftp://client1.isi.edu/tmp/test3/", cl.get(0).getUri()
                .toString());

        resources = policyService.getResources();
        assertEquals(1, resources.size());
        assertEquals("/tmp/test2/", resources.get(0).getFile().toString());
        assertEquals(1, resources.get(0).getNumberOfJobs());

        cleanups = policyService.getCleanups();
        assertEquals(2, cleanups.size());
        boolean foundTest3 = false;
        boolean foundTest1 = false;
        for (Cleanup c : cleanups) {
            if ("gsiftp://client1.isi.edu/tmp/test1/".equals(c.getUri()
                    .toString())) {
                if (foundTest1) {
                    fail("Expected one cleanup instance for gsiftp://client1.isi.edu/tmp/test1/");
                }
                foundTest1 = true;
            } else if ("gsiftp://client1.isi.edu/tmp/test3/".equals(c.getUri()
                    .toString())) {
                if (foundTest3) {
                    fail("Expected one cleanup instance for gsiftp://client1.isi.edu/tmp/test3/");
                }
                foundTest3 = true;
            } else {
                fail("Unexpected cleanup instance " + c.getUri());
            }
        }

        assertTrue(foundTest1);
        assertTrue(foundTest3);

        cl.clear();
        cl.add(new Cleanup("gsiftp://client1.isi.edu/tmp/test2/"));
        cleanups = policyService.addCleanups(cl);
        assertNotNull(cleanups);
        assertEquals(1, cleanups.size());
        assertEquals("gsiftp://client1.isi.edu/tmp/test2/", cleanups.get(0)
                .getUri().toString());
    }

    @Test
    public void testNewTransferStatus() throws URISyntaxException {
        TransferList tl = new TransferList();
        Transfer t;
        t = new Transfer("gsiftp://server1.isi.edu/test1/",
                "file:///tmp/test1/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);
        tl.add(new Transfer("gsiftp://server2.isi.edu/test2/",
                "gsiftp://client2.isi.edu/tmp/test2/"));
        TransferList result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(2, result.size());
        for (Transfer tr : result) {
            assertEquals("START", tr.getProperty("STATUS"));
        }
    }

    @Test
    public void testNewCleanupStatus() throws URISyntaxException {
        TransferList tl = new TransferList();
        Transfer t;
        t = new Transfer("gsiftp://server1.isi.edu/test1/",
                "file:///tmp/test1/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);
        tl.add(new Transfer("gsiftp://server2.isi.edu/test2/",
                "gsiftp://client2.isi.edu/tmp/test2/"));
        policyService.addTransfers(tl);

        CleanupList cl = new CleanupList();
        Cleanup c;
        c = new Cleanup("file:///tmp/test1/");
        c.setProperty("local_file_host", "client1.isi.edu");
        cl.add(c);
        cl.add(new Cleanup("gsiftp://client2.isi.edu/tmp/test2/"));
        CleanupList cr = policyService.addCleanups(cl);
        assertNotNull(cl);
        assertEquals(2, cl.size());
        for (Cleanup r : cr) {
            assertEquals("START", r.getProperty("STATUS"));
        }
    }

    @Test
    public void testAddTwoPartyCleanup() throws URISyntaxException {
        TransferList tl = new TransferList();
        Transfer t;
        t = new Transfer("gsiftp://server1.isi.edu/test1/",
                "file:///tmp/test1/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);
        t = new Transfer("gsiftp://server2.isi.edu/test2/",
                "file:///tmp/test2/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);
        t = new Transfer("gsiftp://server3.isi.edu/test3/",
                "file:///tmp/test3/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        // two jobs on a single transfer
        t = new Transfer("gsiftp://server3.isi.edu/test3/",
                "file:///tmp/test3/");
        t.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t);

        TransferList result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(3, result.size());

        List<Resource> resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(3, resources.size());
        for (Resource r : resources) {
            if ("/tmp/test3/".equals(r.getFile())) {
                assertEquals(2, r.getNumberOfJobs());
            } else {
                assertEquals(1, r.getNumberOfJobs());
            }
        }

        // add cleanups
        CleanupList cl = new CleanupList();
        Cleanup c;
        c = new Cleanup("file:///tmp/test1/");
        c.setProperty("local_file_host", "client1.isi.edu");
        cl.add(c);
        c = new Cleanup("file:///tmp/test3/");
        c.setProperty("local_file_host", "client1.isi.edu");
        cl.add(c);

        CleanupList cleanups = policyService.addCleanups(cl);
        assertNotNull(cleanups);
        assertEquals(1, cleanups.size());
        assertEquals("file:///tmp/test1/", cleanups.get(0)
                .getUri().toString());

        resources = policyService.getResources();
        for (Resource r : resources) {
            assertEquals(1, r.getNumberOfJobs());
        }

        cl.clear();
        c = new Cleanup("file:///tmp/test3/");
        c.setProperty("local_file_host", "client1.isi.edu");
        cl.add(c);

        cleanups = policyService.addCleanups(cl);
        assertNotNull(cleanups);
        assertEquals(1, cleanups.size());
        assertEquals("file:///tmp/test3/", cl.get(0).getUri()
                .toString());

        resources = policyService.getResources();
        assertEquals(1, resources.size());
        assertEquals("/tmp/test2/", resources.get(0).getFile());
        assertEquals(1, resources.get(0).getNumberOfJobs());

        cleanups = policyService.getCleanups();
        assertEquals(2, cleanups.size());
        boolean foundTest3 = false;
        boolean foundTest1 = false;
        for (Cleanup s : cleanups) {
            if ("file:///tmp/test1/".equals(s.getUri().toString())
                    && s.getProperty("local_file_host").equals(
                            "client1.isi.edu")) {
                if (foundTest1) {
                    fail("Expected one cleanup instance for file:///tmp/test1/");
                }
                foundTest1 = true;
            } else if ("file:///tmp/test3/".equals(s.getUri().toString())
                    && s.getProperty("local_file_host").equals(
                            "client1.isi.edu")) {
                if (foundTest3) {
                    fail("Expected one cleanup instance for file:///tmp/test3/");
                }
                foundTest3 = true;
            } else {
                fail("Unexpected cleanup instance " + s.getUri());
            }
        }

        assertTrue(foundTest1);
        assertTrue(foundTest3);

        cl.clear();
        c = new Cleanup("file:///tmp/test2/");
        c.setProperty("local_file_host", "client1.isi.edu");
        cl.add(c);
        cleanups = policyService.addCleanups(cl);
        assertNotNull(cleanups);
        assertEquals(1, cleanups.size());
        assertEquals("file:///tmp/test2/", cleanups.get(0)
                .getUri().toString());
    }

    @Test
    public void testCompleteTransfer() throws URISyntaxException,
    EntityNotFoundException {
        TransferList tl = new TransferList();
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/"));
        TransferList result = policyService.addTransfers(tl);
        Transfer t1 = result.get(0);
        Transfer t2 = result.get(1);
        t1.setProperty("STATUS", "COMPLETED");
        policyService.updateTransfer(t1.getId(), t1);
        result = policyService.getTransfers();
        assertEquals(1, result.size());
        assertEquals(t2.getId(), result.get(0).getId());
        t2.setProperty("random", "123");
        t2.setProperty("STATUS", "SOMETHING RANDOM");
        policyService.updateTransfer(t2.getId(), t2);
        result = policyService.getTransfers();
        assertEquals(1, result.size());
        assertEquals(t2.getId(), result.get(0).getId());
        t2.setProperty("STATUS", "COMPLETED");
        policyService.updateTransfer(t2.getId(), t2);
        result = policyService.getTransfers();
        assertEquals(0, result.size());
    }

    @Test
    public void testCompleteTransferList() throws URISyntaxException {
        TransferList tl = new TransferList();
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/"));
        Transfer t1 = new Transfer("file:///tmp/test3/",
                "gsiftp://client1.isi.edu/tmp/test3/");
        t1.setProperty("local_file_host", "server1.isi.edu");
        tl.add(t1);
        Transfer t2 = new Transfer("gsiftp://server1.isi.edu/tmp/test4/",
                "file:///tmp/test4/");
        t2.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t2);
        TransferList result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(4, result.size());

        for (Transfer r : result) {
            r.setProperty("STATUS", "COMPLETED");
        }
        policyService.updateTransfers(tl);
        result = policyService.getTransfers();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testCompleteCleanup() throws URISyntaxException,
    EntityNotFoundException {
        TransferList tl = new TransferList();
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/"));
        TransferList result = policyService.addTransfers(tl);
        for (Transfer t : result) {
            t.setProperty("STATUS", "COMPLETED");
        }
        policyService.updateTransfers(result);

        List<Resource> resources = policyService.getResources();
        CleanupList cl = new CleanupList(resources.size());
        for (Resource r : resources) {
            cl.add(new Cleanup(new URI("gsiftp://" + r.getHost() + r.getFile())));
        }
        CleanupList cr = policyService.addCleanups(cl);
        assertEquals(0, policyService.getResources().size());
        Cleanup c1 = cr.get(0);
        Cleanup c2 = cr.get(1);
        c1.setProperty("STATUS", "COMPLETED");
        policyService.updateCleanup(c1.getId(), c1);
        cr = policyService.getCleanups();
        assertEquals(1, cr.size());
        assertEquals(c2.getId(), cr.get(0).getId());
        c2.setProperty("random", "123");
        c2.setProperty("STATUS", "SOMETHING RANDOM");
        policyService.updateCleanup(c2.getId(), c2);
        cr = policyService.getCleanups();
        assertEquals(1, cr.size());
        assertEquals(c2.getId(), cr.get(0).getId());
        c2.setProperty("STATUS", "COMPLETED");
        policyService.updateCleanup(c2.getId(), c2);
        cr = policyService.getCleanups();
        assertEquals(0, cr.size());

    }

    @Test
    public void testCompleteCleanupList() throws URISyntaxException {
        TransferList tl = new TransferList();
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/"));
        Transfer t1 = new Transfer("file:///tmp/test3/",
                "gsiftp://client1.isi.edu/tmp/test3/");
        t1.setProperty("local_file_host", "server1.isi.edu");
        tl.add(t1);
        Transfer t2 = new Transfer("gsiftp://server1.isi.edu/tmp/test4/",
                "file:///tmp/test4/");
        t2.setProperty("local_file_host", "client1.isi.edu");
        tl.add(t2);

        TransferList result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(4, result.size());

        for (Transfer r : result) {
            r.setProperty("STATUS", "COMPLETED");
        }
        policyService.updateTransfers(tl);
        result = policyService.getTransfers();
        assertNotNull(result);
        assertEquals(0, result.size());

        List<Resource> resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(4, resources.size());

        CleanupList cr = new CleanupList();
        cr.add(new Cleanup("gsiftp://client1.isi.edu/tmp/test1/"));
        cr.add(new Cleanup("gsiftp://client1.isi.edu/tmp/test2/"));
        cr.add(new Cleanup("gsiftp://client1.isi.edu/tmp/test3/"));
        Cleanup c1 = new Cleanup("file:///tmp/test4/");
        c1.setProperty("local_file_host", "client1.isi.edu");
        cr.add(c1);

        policyService.addCleanups(cr);

        cr = policyService.getCleanups();
        assertNotNull(cr);
        assertEquals(4, cr.size());

        for (Cleanup c : cr) {
            c.setProperty("STATUS", "COMPLETED");
        }
        policyService.updateCleanups(cr);

        cr = policyService.getCleanups();
        assertNotNull(cr);
        assertEquals(0, cr.size());

        resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(0, resources.size());
    }

    @Test
    public void testTransferStatisticsLog() throws URISyntaxException,
    GlobalVariableNotFoundException {
        TransferStatisticsLog transferStatisticsLog = (TransferStatisticsLog) policyService
                .getGlobalVariable("transferStatisticsLog");
        assertNotNull(transferStatisticsLog);
        assertEquals(0, transferStatisticsLog.getNumberOfTransfersCompleted());
        assertEquals(0, transferStatisticsLog.getNumberOfTransfersFailed());
        assertEquals(0, transferStatisticsLog.getNumberOfCleanupsCompleted());
        assertEquals(0,
                transferStatisticsLog.getNumberOfDuplicateTransfersRequested());

        TransferList tl = new TransferList();
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/"));

        policyService.addTransfers(tl);
        assertEquals(0, transferStatisticsLog.getNumberOfTransfersCompleted());

        tl.clear();
        tl.add(new Transfer("gsiftp://server1.isi.edu/tmp/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));
        policyService.addTransfers(tl);

        assertEquals(0, transferStatisticsLog.getNumberOfTransfersCompleted());
        assertEquals(1,
                transferStatisticsLog.getNumberOfDuplicateTransfersRequested());

        tl = policyService.getTransfers();
        for (Transfer t : tl) {
            t.setProperty("STATUS", "COMPLETED");
        }
        policyService.updateTransfers(tl);
        assertEquals(2, transferStatisticsLog.getNumberOfTransfersCompleted());

        List<Resource> rl = policyService.getResources();
        CleanupList cl = new CleanupList();
        for (Resource r : rl) {
            cl.add(new Cleanup("gsiftp://" + r.getHost() + r.getFile()));
        }
        cl = policyService.addCleanups(cl);
        assertEquals(0, transferStatisticsLog.getNumberOfCleanupsCompleted());

        for (Cleanup c : cl) {
            c.setProperty("STATUS", "COMPLETED");
        }
        policyService.updateCleanups(cl);
        assertEquals(1, transferStatisticsLog.getNumberOfCleanupsCompleted());
        cl.clear();
        cl.add(new Cleanup("gsiftp://client1.isi.edu/tmp/test1/"));
        cl = policyService.addCleanups(cl);
        for (Cleanup c : cl) {
            c.setProperty("STATUS", "COMPLETED");
        }
        policyService.updateCleanups(cl);
        assertEquals(2, transferStatisticsLog.getNumberOfCleanupsCompleted());
    }

    @Test
    public void testTransferProtocols() throws URISyntaxException {
        String[] protocols = new String[] { "gsiftp", "scp", "gsissh", "ftp",
                "sftp", "http", "https", "rcp" };
        TransferList tl = new TransferList();
        for (int i = 0; i < protocols.length; i++) {
            for (int j = 0; j < protocols.length; j++) {
                String testname = protocols[i] + "-" + protocols[j];
                tl.add(new Transfer(protocols[i]
                        + "://server1.isi.edu/tmp/test-" + testname + "/",
                        protocols[j] + "://client1.isi.edu/tmp/test-"
                                + testname + "/"));
            }
            Transfer t1 = new Transfer(protocols[i]
                    + "://server1.isi.edu/tmp/test-" + protocols[i] + "-file/",
                    "file:///tmp/test-" + protocols[i] + "-file/");
            t1.setProperty("local_file_host", "client1.isi.edu");
            tl.add(t1);
            t1 = new Transfer("file:///tmp/test-file-" + protocols[i] + "/",
                    protocols[i] + "://client1.isi.edu/tmp/test-file-"
                            + protocols[i] + "/");
            t1.setProperty("local_file_host", "server1.isi.edu");
            tl.add(t1);
        }
        int originalSize = tl.size();
        TransferList result = policyService.addTransfers(tl);
        assertNotNull(result);
        assertEquals(originalSize, result.size());

        List<Resource> resources = policyService.getResources();
        assertNotNull(resources);
        assertEquals(originalSize, resources.size());
        CleanupList cl = new CleanupList();
        for (Transfer t : result) {
            t.setProperty("STATUS", "COMPLETED");
            Cleanup c = new Cleanup(t.getDestination());
            String localFileHost = t.getProperty("local_file_host");
            if (localFileHost != null) {
                c.setProperty("local_file_host", localFileHost);
            }
            cl.add(c);
        }
        result = policyService.updateTransfers(result);
        result = policyService.getTransfers();
        assertNotNull(result);
        assertEquals(0, result.size());

        cl = policyService.addCleanups(cl);
        assertNotNull(cl);
        assertEquals(originalSize, cl.size());
        for (Cleanup c : cl) {
            c.setProperty("STATUS", "COMPLETED");
        }
        cl = policyService.updateCleanups(cl);
        cl = policyService.getCleanups();
        resources = policyService.getResources();
        assertNotNull(cl);
        assertEquals(0, cl.size());
        assertNotNull(resources);
        assertEquals(0, resources.size());
    }

    @Test
    public void testAddCleanupsUnknownResource() throws URISyntaxException,
    EntityNotFoundException {
        CleanupList cl = new CleanupList();
        cl.add(new Cleanup("gsiftp://client-7.isi.edu/not/known/"));
        cl = policyService.addCleanups(cl);
        assertNotNull(cl);
        assertEquals(1, cl.size());
        assertNotNull(policyService.getCleanup(cl.get(0).getId()));

        cl.clear();
        Cleanup cleanup = new Cleanup("file:///another/unknown/file/");
        cleanup.setProperty("local_file_host", "client-7.isi.edu");
        cl.add(cleanup);

        cl = policyService.addCleanups(cl);
        assertNotNull(cl);
        assertEquals(1, cl.size());
        assertNotNull(policyService.getCleanup(cl.get(0).getId()));
    }

    @Test
    public void testMaxParallelStreams() throws URISyntaxException {
        policyService = createPolicyService(new String[] { "pegasus.drl",
        "pegasus-enforce-max-parallel-streams.drl" });

        TransferList tl = new TransferList();
        tl.add(new Transfer("gsiftp://server1.isi.edu/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));

        TransferList result = policyService.addTransfers(tl);
        assertEquals("4", result.get(0).getProperty("parallel"));

        tl.clear();
        tl.add(new Transfer("gsiftp://server1.isi.edu/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/"));
        result = policyService.addTransfers(tl);
        assertEquals("4", result.get(0).getProperty("parallel"));

        tl.clear();
        tl.add(new Transfer("gsiftp://server1.isi.edu/test3/",
                "gsiftp://client1.isi.edu/tmp/test3/"));

        result = policyService.addTransfers(tl);
        assertEquals("2", result.get(0).getProperty("parallel"));

        tl.clear();
        tl.add(new Transfer("gsiftp://server1.isi.edu/test4/",
                "gsiftp://client1.isi.edu/tmp/test4/"));

        result = policyService.addTransfers(tl);
        assertEquals("1", result.get(0).getProperty("parallel"));

        // two party download
        tl.clear();
        Transfer t = new Transfer("gsiftp://server2.isi.edu/test1/",
                "file:///tmp/test1/");
        t.setProperty("local_file_host", "client2.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("4", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("gsiftp://server2.isi.edu/test2/",
                "file:///tmp/test2/");
        t.setProperty("local_file_host", "client2.isi.edu");
        tl.add(t);
        result = policyService.addTransfers(tl);
        assertEquals("4", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("gsiftp://server2.isi.edu/test3/",
                "file:///tmp/test3/");
        t.setProperty("local_file_host", "client2.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("2", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("gsiftp://server2.isi.edu/test4/",
                "file:///tmp/test4/");
        t.setProperty("local_file_host", "client2.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("1", result.get(0).getProperty("parallel"));

        // two party upload
        tl.clear();
        t = new Transfer("file:///tmp/test1/",
                "gsiftp://server3.isi.edu/test1/");
        t.setProperty("local_file_host", "client3.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("4", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("file:///tmp/test2/",
                "gsiftp://server3.isi.edu/test2/");
        t.setProperty("local_file_host", "client3.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("4", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("file:///tmp/test3/",
                "gsiftp://server3.isi.edu/test3/");
        t.setProperty("local_file_host", "client3.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("2", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("file:///tmp/test4/",
                "gsiftp://server3.isi.edu/test4/");
        t.setProperty("local_file_host", "client3.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("1", result.get(0).getProperty("parallel"));
    }

    @Test
    public void testClusterMaxParallelStreams() throws URISyntaxException {
        policyService = createPolicyService(new String[] { "pegasus.drl",
                "pegasus-enforce-max-parallel-streams.drl",
        "pegasus-enforce-cluster-max-parallel-streams.drl" });

        TransferList tl = new TransferList();
        tl.add(new Transfer("gsiftp://server1.isi.edu/test1/",
                "gsiftp://client1.isi.edu/tmp/test1/"));

        TransferList result = policyService.addTransfers(tl);
        assertEquals("3", result.get(0).getProperty("parallel"));

        tl.clear();
        tl.add(new Transfer("gsiftp://server1.isi.edu/test2/",
                "gsiftp://client1.isi.edu/tmp/test2/"));
        result = policyService.addTransfers(tl);
        assertEquals("3", result.get(0).getProperty("parallel"));

        tl.clear();
        tl.add(new Transfer("gsiftp://server1.isi.edu/test3/",
                "gsiftp://client1.isi.edu/tmp/test3/"));

        result = policyService.addTransfers(tl);
        assertEquals("3", result.get(0).getProperty("parallel"));

        tl.clear();
        tl.add(new Transfer("gsiftp://server1.isi.edu/test4/",
                "gsiftp://client1.isi.edu/tmp/test4/"));

        result = policyService.addTransfers(tl);
        assertEquals("1", result.get(0).getProperty("parallel"));

        tl.clear();
        tl.add(new Transfer("gsiftp://server1.isi.edu/test5/",
                "gsiftp://client1.isi.edu/tmp/test5/"));

        result = policyService.addTransfers(tl);
        assertEquals("1", result.get(0).getProperty("parallel"));

        // two party download
        tl.clear();
        Transfer t = new Transfer("gsiftp://server2.isi.edu/test1/",
                "file:///tmp/test1/");
        t.setProperty("local_file_host", "client2.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("3", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("gsiftp://server2.isi.edu/test2/",
                "file:///tmp/test2/");
        t.setProperty("local_file_host", "client2.isi.edu");
        tl.add(t);
        result = policyService.addTransfers(tl);
        assertEquals("3", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("gsiftp://server2.isi.edu/test3/",
                "file:///tmp/test3/");
        t.setProperty("local_file_host", "client2.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("3", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("gsiftp://server2.isi.edu/test4/",
                "file:///tmp/test4/");
        t.setProperty("local_file_host", "client2.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("1", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("gsiftp://server2.isi.edu/test5/",
                "file:///tmp/test5/");
        t.setProperty("local_file_host", "client2.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("1", result.get(0).getProperty("parallel"));

        // two party upload
        tl.clear();
        t = new Transfer("file:///tmp/test1/",
                "gsiftp://server3.isi.edu/test1/");
        t.setProperty("local_file_host", "client3.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("3", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("file:///tmp/test2/",
                "gsiftp://server3.isi.edu/test2/");
        t.setProperty("local_file_host", "client3.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("3", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("file:///tmp/test3/",
                "gsiftp://server3.isi.edu/test3/");
        t.setProperty("local_file_host", "client3.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("3", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("file:///tmp/test4/",
                "gsiftp://server3.isi.edu/test4/");
        t.setProperty("local_file_host", "client3.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("1", result.get(0).getProperty("parallel"));

        tl.clear();
        t = new Transfer("file:///tmp/test5/",
                "gsiftp://server3.isi.edu/test5/");
        t.setProperty("local_file_host", "client3.isi.edu");
        tl.add(t);

        result = policyService.addTransfers(tl);
        assertEquals("1", result.get(0).getProperty("parallel"));
    }
}

package edu.isi.policy.adapt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.policy.adapt.FileSystemResourceAllocationLogImpl;
import edu.isi.policy.adapt.ResourceAllocation;
import edu.isi.policy.adapt.ResourceAllocationException;
import edu.isi.policy.adapt.ResourceAllocationImpl;
import edu.isi.policy.adapt.ResourceAllocationLog;
import edu.isi.policy.util.IdentityGenerator;

public class FileSystemResourceAllocationLogImplTest {
    private static final String resourceAllocationDB = System
            .getProperty("user.home")
            + File.separator
            + "adapt_resource_allocation";
    private ResourceAllocationLog log1;
    private ResourceAllocationLog log2;

    @Before
    public void createResourceAllocationLogs() {
        Properties props = new Properties();
        props.setProperty(
                FileSystemResourceAllocationLogImpl.BASE_DIRECTORY_KEY,
                resourceAllocationDB);
        log1 = new FileSystemResourceAllocationLogImpl(props);
        log2 = new FileSystemResourceAllocationLogImpl(props);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArg() {
        new FileSystemResourceAllocationLogImpl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyArg() {
        new FileSystemResourceAllocationLogImpl(new Properties());
    }

    @Test
    public void testAddResourceAllocation() throws ResourceAllocationException,
    URISyntaxException {

        String id1 = IdentityGenerator.generateId();
        log1.addResourceAllocation(new ResourceAllocationImpl(id1, new URI(
                "gsiftp://server1.isi.edu/tmp/test1/"),
                new URI("gsiftp://client1.isi.edu/tmp/test2/"), 4, 2.0f));
        String id2 = IdentityGenerator.generateId();
        log2.addResourceAllocation(new ResourceAllocationImpl(id2, new URI(
                "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test2/"), 2, 2.5f));
        String id3 = IdentityGenerator.generateId();
        log1.addResourceAllocation(new ResourceAllocationImpl(id3, new URI(
                "gsiftp://server2.isi.edu/tmp/test3/"), new URI(
                        "gsiftp://client2.isi.edu/tmp/test3/"), 4, 2.0f));
    }

    @Test
    public void testGetResourceAllocation() throws ResourceAllocationException,
    URISyntaxException {
        String id1 = IdentityGenerator.generateId();
        ResourceAllocation r1 = new ResourceAllocationImpl(id1, new URI(
                "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test2/"), 4, 2.0f);
        log1.addResourceAllocation(r1);
        String id2 = IdentityGenerator.generateId();
        ResourceAllocation r2 = new ResourceAllocationImpl(id2, new URI(
                "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test2/"), 2, 2.5f);
        log2.addResourceAllocation(r2);
        String id3 = IdentityGenerator.generateId();
        ResourceAllocation r3 = new ResourceAllocationImpl(id3, new URI(
                "gsiftp://server2.isi.edu/tmp/test3/"), new URI(
                        "gsiftp://client2.isi.edu/tmp/test3/"), 4, 2.0f);
        log1.addResourceAllocation(r3);

        assertEquals(r1, log1.getResourceAllocation(id1));
        assertEquals(r2, log2.getResourceAllocation(id2));
        assertEquals(r3, log1.getResourceAllocation(id3));

        // make sure we can only get access to a record from its own allocation
        // log
        assertTrue(log1.getResourceAllocation(id2) == null);
    }

    @Test
    public void testUpdateResourceAllocation()
            throws ResourceAllocationException, URISyntaxException {
        String id1 = IdentityGenerator.generateId();
        ResourceAllocation r1 = new ResourceAllocationImpl(id1, new URI(
                "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test2/"), 4, 2.0f);
        log1.addResourceAllocation(r1);
        String id2 = IdentityGenerator.generateId();
        ResourceAllocation r2 = new ResourceAllocationImpl(id2, new URI(
                "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test2/"), 2, 2.5f);
        log2.addResourceAllocation(r2);

        assertEquals(r1, log1.getResourceAllocation(id1));
        assertEquals(r2, log2.getResourceAllocation(id2));

        // update some properties
        r1.setTransferStreams(8);
        log1.updateResourceAllocation(r1);
        assertEquals(8, log1.getResourceAllocation(id1).getTransferStreams());
        r2.setRate(4.0f);
        log2.updateResourceAllocation(r2);
        assertEquals(4.0f, log2.getResourceAllocation(id2).getRate(), 0);
    }

    @Test
    public void testRemoveResourceAllocation()
            throws ResourceAllocationException, URISyntaxException {
        String id1 = IdentityGenerator.generateId();
        ResourceAllocation r1 = new ResourceAllocationImpl(id1, new URI(
                "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test2/"), 4, 2.0f);
        log1.addResourceAllocation(r1);
        String id2 = IdentityGenerator.generateId();
        ResourceAllocation r2 = new ResourceAllocationImpl(id2, new URI(
                "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test2/"), 2, 2.5f);
        log2.addResourceAllocation(r2);
        String id3 = IdentityGenerator.generateId();
        ResourceAllocation r3 = new ResourceAllocationImpl(id3, new URI(
                "gsiftp://server2.isi.edu/tmp/test3/"), new URI(
                        "gsiftp://client2.isi.edu/tmp/test3/"), 4, 2.0f);
        log1.addResourceAllocation(r3);

        assertEquals(r1, log1.removeResourceAllocation(id1));
        try {
            log2.removeResourceAllocation(id3);
            fail("Removed record from wrong resource allocation log.");
        } catch (Exception e) {
        }

        assertEquals(r3, log1.removeResourceAllocation(id3));
        assertEquals(r2, log2.removeResourceAllocation(id2));

        assertTrue(log2.getResourceAllocation(id2) == null);
    }

    @Test
    public void testAggregateStreams() throws URISyntaxException,
    ResourceAllocationException {
        String id1 = IdentityGenerator.generateId();
        ResourceAllocation r1 = new ResourceAllocationImpl(id1, new URI(
                "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test1/"), 4, 2.0f);
        log1.addResourceAllocation(r1);
        String id2 = IdentityGenerator.generateId();
        ResourceAllocation r2 = new ResourceAllocationImpl(id2, new URI(
                "gsiftp://server1.isi.edu/tmp/test2/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test2/"), 2, 2.5f);
        log2.addResourceAllocation(r2);
        String id3 = IdentityGenerator.generateId();
        ResourceAllocation r3 = new ResourceAllocationImpl(id3, new URI(
                "gsiftp://server2.isi.edu/tmp/test3/"), new URI(
                        "gsiftp://client2.isi.edu/tmp/test3/"), 4, 2.0f);
        log1.addResourceAllocation(r3);

        // both should aggregate the same number
        assertEquals(6, log1.getAggregatedTransferStreams(r1.getSource()
                .getHost(), r1.getDestination().getHost()));
        assertEquals(6, log2.getAggregatedTransferStreams(r2.getSource()
                .getHost(), r2.getDestination().getHost()));

        // update
        r1.setTransferStreams(6);
        log1.updateResourceAllocation(r1);
        assertEquals(8, log1.getAggregatedTransferStreams(r1.getSource()
                .getHost(), r1.getDestination().getHost()));
        assertEquals(8, log2.getAggregatedTransferStreams(r2.getSource()
                .getHost(), r2.getDestination().getHost()));

        // add
        log2.addResourceAllocation(new ResourceAllocationImpl(IdentityGenerator
                .generateId(), new URI("gsiftp://server1.isi.edu/tmp/test4/"),
                new URI("gsiftp://client1.isi.edu/tmp/test4/"), 1, 1.0f));
        assertEquals(9, log1.getAggregatedTransferStreams(r1.getSource()
                .getHost(), r1.getDestination().getHost()));
        assertEquals(9, log2.getAggregatedTransferStreams(r1.getSource()
                .getHost(), r1.getDestination().getHost()));

        // remove
        log1.removeResourceAllocation(r1.getId());
        assertEquals(3, log1.getAggregatedTransferStreams(r1.getSource()
                .getHost(), r1.getDestination().getHost()));
        assertEquals(3, log2.getAggregatedTransferStreams(r1.getSource()
                .getHost(), r1.getDestination().getHost()));

        // same source, different dest
        String id4 = IdentityGenerator.generateId();
        String id5 = IdentityGenerator.generateId();
        log1.addResourceAllocation(new ResourceAllocationImpl(id4, new URI(
                "gsiftp://servera.isi.edu/tmp/testa"), new URI(
                        "gsiftp://clienta.isi.edu/tmp/testa"), 2, 1.0f));
        log1.addResourceAllocation(new ResourceAllocationImpl(id5, new URI(
                "gsiftp://servera.isi.edu/tmp/testa"), new URI(
                        "gsiftp://clientb.isi.edu/tmp/testa"), 3, 1.0f));
        assertEquals(2, log1.getAggregatedTransferStreams("servera.isi.edu",
                "clienta.isi.edu"));
        assertEquals(3, log1.getAggregatedTransferStreams("servera.isi.edu",
                "clientb.isi.edu"));

        // same dest, different source
        String id6 = IdentityGenerator.generateId();
        String id7 = IdentityGenerator.generateId();
        log1.addResourceAllocation(new ResourceAllocationImpl(id6, new URI(
                "gsiftp://serverc.isi.edu/tmp/testc"), new URI(
                        "gsiftp://clientc.isi.edu/tmp/testa"), 2, 1.0f));
        log1.addResourceAllocation(new ResourceAllocationImpl(id7, new URI(
                "gsiftp://serverd.isi.edu/tmp/testa"), new URI(
                        "gsiftp://clientc.isi.edu/tmp/testa"), 3, 1.0f));
        assertEquals(2, log1.getAggregatedTransferStreams("serverc.isi.edu",
                "clientc.isi.edu"));
        assertEquals(3, log1.getAggregatedTransferStreams("serverd.isi.edu",
                "clientc.isi.edu"));
    }

    @Test
    public void testAggregateRate() throws URISyntaxException,
    ResourceAllocationException {
        String id1 = IdentityGenerator.generateId();
        ResourceAllocation r1 = new ResourceAllocationImpl(id1, new URI(
                "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test1/"), 4, 2.0f);
        log1.addResourceAllocation(r1);
        String id2 = IdentityGenerator.generateId();
        ResourceAllocation r2 = new ResourceAllocationImpl(id2, new URI(
                "gsiftp://server1.isi.edu/tmp/test2/"), new URI(
                        "gsiftp://client1.isi.edu/tmp/test2/"), 2, 2.5f);
        log2.addResourceAllocation(r2);
        String id3 = IdentityGenerator.generateId();
        ResourceAllocation r3 = new ResourceAllocationImpl(id3, new URI(
                "gsiftp://server2.isi.edu/tmp/test3/"), new URI(
                        "gsiftp://client2.isi.edu/tmp/test3/"), 4, 2.0f);
        log1.addResourceAllocation(r3);

        // both should aggregate the same number
        assertEquals(4.5f, log1.getAggregatedRate(r1.getSource().getHost(), r1
                .getDestination().getHost()), 0);
        assertEquals(4.5f, log2.getAggregatedRate(r2.getSource().getHost(), r2
                .getDestination().getHost()), 0);

        // update
        r1.setRate(6.0f);
        log1.updateResourceAllocation(r1);
        assertEquals(8.5, log1.getAggregatedRate(r1.getSource()
                .getHost(), r1.getDestination().getHost()), 0);
        assertEquals(8.5, log2.getAggregatedRate(r2.getSource()
                .getHost(), r2.getDestination().getHost()), 0);

        // add
        log2.addResourceAllocation(new ResourceAllocationImpl(IdentityGenerator
                .generateId(), new URI("gsiftp://server1.isi.edu/tmp/test4/"),
                new URI("gsiftp://client1.isi.edu/tmp/test4/"), 1, 1.0f));
        assertEquals(9.5, log1.getAggregatedRate(r1.getSource().getHost(), r1
                .getDestination().getHost()), 0);
        assertEquals(9.5, log2.getAggregatedRate(r1.getSource().getHost(), r1
                .getDestination().getHost()), 0);

        // remove
        log1.removeResourceAllocation(r1.getId());
        assertEquals(3.5, log1.getAggregatedRate(r1.getSource().getHost(), r1
                .getDestination().getHost()), 0);
        assertEquals(3.5, log2.getAggregatedRate(r1.getSource().getHost(), r1
                .getDestination().getHost()), 0);

        // same source, different dest
        String id4 = IdentityGenerator.generateId();
        String id5 = IdentityGenerator.generateId();
        log1.addResourceAllocation(new ResourceAllocationImpl(id4, new URI(
                "gsiftp://servera.isi.edu/tmp/testa"), new URI(
                        "gsiftp://clienta.isi.edu/tmp/testa"), 1, 1.0f));
        log1.addResourceAllocation(new ResourceAllocationImpl(id5, new URI(
                "gsiftp://servera.isi.edu/tmp/testa"), new URI(
                        "gsiftp://clientb.isi.edu/tmp/testa"), 1, 2.0f));
        assertEquals(1.0,
                log1.getAggregatedRate("servera.isi.edu", "clienta.isi.edu"), 0);
        assertEquals(2.0,
                log1.getAggregatedRate("servera.isi.edu", "clientb.isi.edu"), 0);

        // same dest, different source
        String id6 = IdentityGenerator.generateId();
        String id7 = IdentityGenerator.generateId();
        log1.addResourceAllocation(new ResourceAllocationImpl(id6, new URI(
                "gsiftp://serverc.isi.edu/tmp/testc"), new URI(
                        "gsiftp://clientc.isi.edu/tmp/testa"), 2, 3.0f));
        log1.addResourceAllocation(new ResourceAllocationImpl(id7, new URI(
                "gsiftp://serverd.isi.edu/tmp/testa"), new URI(
                        "gsiftp://clientc.isi.edu/tmp/testa"), 2, 4.5f));
        assertEquals(3,
                log1.getAggregatedRate("serverc.isi.edu", "clientc.isi.edu"), 0);
        assertEquals(4.5,
                log1.getAggregatedRate("serverd.isi.edu", "clientc.isi.edu"), 0);
    }

    // cleans up resource allocation
    @After
    public void cleanupResourceAllocationDB() {
        recursiveRemove(new File(resourceAllocationDB));

    }

    private void recursiveRemove(File d) {
        if (d.isDirectory()) {
            File[] files = d.listFiles();
            for (int i = 0; i < files.length; i++) {
                recursiveRemove(files[i]);
            }
        }
        d.delete();
    }
}

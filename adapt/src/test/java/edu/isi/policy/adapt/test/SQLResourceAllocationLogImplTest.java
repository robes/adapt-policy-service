package edu.isi.policy.adapt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.policy.adapt.ResourceAllocation;
import edu.isi.policy.adapt.ResourceAllocationException;
import edu.isi.policy.adapt.ResourceAllocationImpl;
import edu.isi.policy.adapt.SQLResourceAllocationLogImpl;
import edu.isi.policy.util.IdentityGenerator;

public class SQLResourceAllocationLogImplTest {

    private SQLResourceAllocationLogImpl log = null;

    @Before
    public void initialize() throws ResourceAllocationException {
        Properties props = new Properties();
        props.setProperty(SQLResourceAllocationLogImpl.DATABASE_DRIVER_KEY,
                "org.hsqldb.jdbc.JDBCDriver");
        props.setProperty(SQLResourceAllocationLogImpl.DATABASE_URL_KEY,
                "jdbc:hsqldb:mem:ral");
        props.setProperty(SQLResourceAllocationLogImpl.DATABASE_USER_KEY, "SA");
        props.setProperty(SQLResourceAllocationLogImpl.DATABASE_PASSWORD_KEY,
                "");
        log = new SQLResourceAllocationLogImpl(props);
        log.open();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddResourceAllocation_nullArg()
            throws ResourceAllocationException {
        log.addResourceAllocation(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetResourceAllocation_nullArg()
            throws ResourceAllocationException {
        log.getResourceAllocation(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetResourceAllocation_emptyhArg()
            throws ResourceAllocationException {
        log.getResourceAllocation("");
    }

    @Test
    public void testGetAndAddResourceAllocation()
            throws ResourceAllocationException,
            URISyntaxException {
        ResourceAllocation allocation = new ResourceAllocationImpl(
                IdentityGenerator.generateId(), new URI(
                        "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                                "gsiftp://client1.isi.edu/tmp/test1/"), 0, 0);
        log.addResourceAllocation(allocation);
        ResourceAllocation r2 = log.getResourceAllocation(allocation.getId());
        assertEquals(allocation, r2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateResourceAllocation_nullArg()
            throws ResourceAllocationException {
        log.updateResourceAllocation(null);
    }

    @Test
    public void testUpdateResourceAllocation()
            throws ResourceAllocationException, URISyntaxException {
        ResourceAllocation allocation = new ResourceAllocationImpl(
                IdentityGenerator.generateId(), new URI(
                        "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                                "gsiftp://client1.isi.edu/tmp/test1/"), 0, 0);
        log.addResourceAllocation(allocation);
        allocation.setTransferStreams(4);
        allocation.setRate(200.0f);
        log.updateResourceAllocation(allocation);
        allocation = log.getResourceAllocation(allocation.getId());
        assertEquals(4, allocation.getTransferStreams());
        assertEquals(200, allocation.getRate(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveResourceAllocation_nullArg()
            throws ResourceAllocationException {
        log.removeResourceAllocation(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveResourceAllocation_emptyArg()
            throws ResourceAllocationException {
        log.removeResourceAllocation("");
    }

    @Test
    public void testRemoveResourceAllocation()
            throws ResourceAllocationException, URISyntaxException {
        ResourceAllocation allocation = new ResourceAllocationImpl(
                IdentityGenerator.generateId(), new URI(
                        "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                                "gsiftp://client1.isi.edu/tmp/test1/"), 0, 0);
        log.addResourceAllocation(allocation);
        log.removeResourceAllocation(allocation.getId());
        allocation = log.getResourceAllocation(allocation.getId());
        assertNull(allocation);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregratedTransferStreams_null1stArg() {
        log.getAggregatedTransferStreams(null, "client1.isi.edu");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregratedTransferStreams_empty1stArg() {
        log.getAggregatedTransferStreams("", "client1.isi.edu");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregratedTransferStreams_null2ndArg() {
        log.getAggregatedTransferStreams("server1.isi.edu", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregratedTransferStreams_empty2ndArg() {
        log.getAggregatedTransferStreams("server1.isi.edu", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregratedRate_null1stArg() {
        log.getAggregatedRate(null, "client1.isi.edu");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregratedRate_empty1stArg() {
        log.getAggregatedRate("", "client1.isi.edu");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregratedRate_null2ndArg() {
        log.getAggregatedRate("server1.isi.edu", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregratedRate_empty2ndArg() {
        log.getAggregatedRate("server1.isi.edu", "");
    }

    @Test
    public void testAggregatedTransferStreams()
            throws ResourceAllocationException, URISyntaxException {
        assertEquals(0, log.getAggregatedTransferStreams("server1.isi.edu",
                "client1.isi.edu"));
        ResourceAllocation allocation = new ResourceAllocationImpl(
                IdentityGenerator.generateId(), new URI(
                        "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                                "gsiftp://client1.isi.edu/tmp/test1/"), 0, 0);
        log.addResourceAllocation(allocation);
        assertEquals(0, log.getAggregatedTransferStreams("server1.isi.edu",
                "client1.isi.edu"));
        allocation.setTransferStreams(6);
        log.updateResourceAllocation(allocation);
        assertEquals(6, log.getAggregatedTransferStreams("server1.isi.edu",
                "client1.isi.edu"));
        log.addResourceAllocation(new ResourceAllocationImpl(IdentityGenerator
                .generateId(), new URI("gsiftp://server1.isi.edu/tmp/test2/"),
                new URI("gsiftp://client1.isi.edu/tmp/test2/"), 10, 0));
        assertEquals(16, log.getAggregatedTransferStreams("server1.isi.edu",
                "client1.isi.edu"));
        log.addResourceAllocation(new ResourceAllocationImpl(IdentityGenerator
                .generateId(), new URI("gsiftp://server2.isi.edu/tmp/test3/"),
                new URI("gsiftp://client2.isi.edu/tmp/test3/"), 2, 0));
        assertEquals(16, log.getAggregatedTransferStreams("server1.isi.edu",
                "client1.isi.edu"));
        assertEquals(2, log.getAggregatedTransferStreams("server2.isi.edu",
                "client2.isi.edu"));
    }

    @Test
    public void testAggregatedRate() throws ResourceAllocationException,
    URISyntaxException {
        assertEquals(0,
                log.getAggregatedRate("server1.isi.edu", "client1.isi.edu"), 0);
        ResourceAllocation allocation = new ResourceAllocationImpl(
                IdentityGenerator.generateId(), new URI(
                        "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                                "gsiftp://client1.isi.edu/tmp/test1/"), 0, 0);
        log.addResourceAllocation(allocation);
        assertEquals(0,
                log.getAggregatedRate("server1.isi.edu", "client1.isi.edu"), 0);
        allocation.setRate(100);
        log.updateResourceAllocation(allocation);
        assertEquals(100,
                log.getAggregatedRate("server1.isi.edu", "client1.isi.edu"), 0);
        log.addResourceAllocation(new ResourceAllocationImpl(IdentityGenerator
                .generateId(), new URI("gsiftp://server1.isi.edu/tmp/test2/"),
                new URI("gsiftp://client1.isi.edu/tmp/test2/"), 0, 50));
        assertEquals(150,
                log.getAggregatedRate("server1.isi.edu", "client1.isi.edu"), 0);
        log.addResourceAllocation(new ResourceAllocationImpl(IdentityGenerator
                .generateId(), new URI("gsiftp://server2.isi.edu/tmp/test3/"),
                new URI("gsiftp://client2.isi.edu/tmp/test3/"), 0, 200));
        assertEquals(150,
                log.getAggregatedRate("server1.isi.edu", "client1.isi.edu"), 0);
        assertEquals(200,
                log.getAggregatedRate("server2.isi.edu", "client2.isi.edu"), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNumberOfTransfers_null1stArg() {
        log.getNumberOfTransfers(null, "client1.isi.edu");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNumberOfTransfers_empty1stArg() {
        log.getNumberOfTransfers("", "client1.isi.edu");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNumberOfTransfers_null2ndArg() {
        log.getNumberOfTransfers("server1.isi.edu", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNumberOfTransfers_empty2ndArg() {
        log.getNumberOfTransfers("server1.isi.edu", "");
    }

    @Test
    public void testGetNumberOfTransfers() throws ResourceAllocationException,
    URISyntaxException {
        assertEquals(0,
                log.getNumberOfTransfers("server1.isi.edu", "client1.isi.edu"));
        ResourceAllocation allocation = new ResourceAllocationImpl(
                IdentityGenerator.generateId(), new URI(
                        "gsiftp://server1.isi.edu/tmp/test1/"), new URI(
                                "gsiftp://client1.isi.edu/tmp/test1/"), 0, 0);
        log.addResourceAllocation(allocation);
        assertEquals(1,
                log.getNumberOfTransfers("server1.isi.edu", "client1.isi.edu"));
        log.addResourceAllocation(new ResourceAllocationImpl(IdentityGenerator
                .generateId(), new URI("gsiftp://server1.isi.edu/tmp/test2/"),
                new URI("gsiftp://client1.isi.edu/tmp/test2/"), 0, 50));
        assertEquals(2,
                log.getNumberOfTransfers("server1.isi.edu", "client1.isi.edu"));
        log.addResourceAllocation(new ResourceAllocationImpl(IdentityGenerator
                .generateId(), new URI("gsiftp://server2.isi.edu/tmp/test3/"),
                new URI("gsiftp://client2.isi.edu/tmp/test3/"), 0, 200));
        assertEquals(2,
                log.getNumberOfTransfers("server1.isi.edu", "client1.isi.edu"));
        assertEquals(1,
                log.getNumberOfTransfers("server2.isi.edu", "client2.isi.edu"));
    }

    @After
    public void cleanupDB() {
        log.close();
    }

}

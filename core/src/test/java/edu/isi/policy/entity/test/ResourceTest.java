package edu.isi.policy.entity.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;

import org.junit.Test;

import edu.isi.policy.entity.Resource;

public class ResourceTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArg1() throws URISyntaxException {
        new Resource(null, "/tmp");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArg2() throws URISyntaxException {
        new Resource("myhost.isi.edu", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHost_nullArg() throws URISyntaxException {
        new Resource("myserver.isi.edu", "/tmp/file").setHost(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFile_nullArg() throws URISyntaxException {
        new Resource("myserver.isi.edu", "/tmp").setFile(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNumberOfJobs_negativeArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/tmp/test1/").setNumberOfJobs(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetJobs_nullArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/tmp/test1/").setJobs(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddJob_nullArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/tmp/test1/").addJob(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveJob_nullArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/tmp/test1/").removeJob(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProperties_nullArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/tmp/test1/").setProperties(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetProperty_nullArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/tmp/test1/").getProperty(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetProperty_emptyArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/tmp/test1/").getProperty("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProperty_nullArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/tmp/test1/")
                .setProperty(null,
                "true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProperty_emptyArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/tmp/test1/").setProperty("",
                "false");
    }

    @Test
    public void testGetAndSetHostAndFile() throws URISyntaxException {

        Resource r = new Resource("client1.isi.edu", "/tmp/test1/");
        assertEquals("client1.isi.edu", r.getHost());
        assertEquals("/tmp/test1/", r.getFile());
        r.setHost("client2.isi.edu");
        r.setFile("/tmp/test2/");

        assertEquals("client2.isi.edu", r.getHost());
        assertEquals("/tmp/test2/", r.getFile());
    }

    @Test
    public void testGetAndSetNumberOfJobs() throws URISyntaxException {
        int j1 = 4;
        int j2 = 6;
        Resource r = new Resource("client1.isi.edu", "/tmp/test2/");
        r.setNumberOfJobs(j1);
        assertEquals(j1, r.getNumberOfJobs());
        r.setNumberOfJobs(j2);
        assertEquals(j2, r.getNumberOfJobs());
    }

    @Test
    public void testAddAndRemoveJob() throws URISyntaxException {
        Resource r = new Resource("client1.isi.edu", "/tmp/test3/");
        String j1 = "43242342-4324234234";
        String j2 = "8768768678=8768778";
        r.addJob(j1);
        assertTrue(r.getJobs().contains(j1));
        r.addJob(j2);
        assertTrue(r.getJobs().contains(j2));
        r.removeJob(j1);
        assertTrue(!r.getJobs().contains(j1));
        assertTrue(r.getJobs().contains(j2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetId_nullArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/test1/").setId(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetId_emptyArg() throws URISyntaxException {
        new Resource("client1.isi.edu", "/test1/").setId("");
    }

}

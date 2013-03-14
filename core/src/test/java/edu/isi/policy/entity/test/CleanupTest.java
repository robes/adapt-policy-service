package edu.isi.policy.entity.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import edu.isi.policy.entity.Cleanup;

public class CleanupTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArg() throws URISyntaxException {
        new Cleanup((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2_nullArg() {
        new Cleanup((URI) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyArg() throws URISyntaxException {
        new Cleanup("");
    }

    @Test
    public void testConstructors() throws URISyntaxException {
        new Cleanup();
        new Cleanup("file:///tmp/myfile.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetId_nullID() {
        new Cleanup().setId(null);

    }

    @Test
    public void testGetAndSetId() {
        final Cleanup resourceCleanup = new Cleanup();
        assertNull(resourceCleanup.getId());
        String id = "5435345345";
        resourceCleanup.setId(id);
        assertEquals(resourceCleanup.getId(), id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetUrl_nullArg() {
        new Cleanup().setUri(null);
    }

    @Test
    public void testGetAndSetUrl() throws URISyntaxException {
        final Cleanup resourceCleanup = new Cleanup();
        assertNull(resourceCleanup.getUri());
        final URI uri = new URI("file:///tmp/myfile.txt");
        resourceCleanup.setUri(uri);
        assertEquals(resourceCleanup.getUri(), uri);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProperty_nullArg() {
        new Cleanup().setProperty(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProperty_emptyArg() {
        new Cleanup().setProperty("", null);
    }

    @Test
    public void testSetAndGetProperty() throws URISyntaxException {
        final Cleanup resourceCleanup = new Cleanup(
                "file:///tmp/myfile.txt");
        final String propName = "property1";
        final String propValue = "value1";

        assertNull(resourceCleanup.getProperty(propName));
        resourceCleanup.setProperty(propName, propValue);
        assertEquals(resourceCleanup.getProperty(propName), propValue);
    }

    @Test
    public void testCompareTo() throws URISyntaxException {
        final Cleanup rc1 = new Cleanup("file:///tmp/file1.txt");
        final Cleanup rc2 = new Cleanup("file:///tmp/file2.txt");
        final Cleanup rc3 = new Cleanup("file:///tmp/file1.txt");

        assertEquals(rc1.compareTo(rc3), 0);
        assertTrue(rc1.compareTo(rc2) != 0);
    }
}

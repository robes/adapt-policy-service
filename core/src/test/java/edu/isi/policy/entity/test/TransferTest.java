package edu.isi.policy.entity.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.isi.policy.entity.Transfer;
import edu.isi.policy.util.IdentityGenerator;

public class TransferTest {

    private static URI r1;
    private static URI r2;

    @BeforeClass
    public static void createResources() throws URISyntaxException {
        r1 = new URI("file:///tmp/myfile.txt");
        r2 = new URI("http://www.isi.edu");
    }

    @Test
    public void testGetId() {
        assertNotNull(new Transfer(IdentityGenerator.generateId(), r1, r2)
        .getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSource_nullArg() {
        final Transfer t = new Transfer(IdentityGenerator.generateId(), r1, r2);
        t.setSource(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDestination_nullArg() {
        final Transfer t = new Transfer(IdentityGenerator.generateId(), r1, r2);
        t.setDestination(null);
    }

    @Test
    public void testGetSetSource() {
        final Transfer t = new Transfer(IdentityGenerator.generateId(), r1, r2);
        assertEquals(t.getSource(), r1);
        t.setSource(r2);
        assertEquals(t.getSource(), r2);
    }

    @Test
    public void testGetSetDestination() {
        final Transfer t = new Transfer(IdentityGenerator.generateId(), r1, r2);
        assertEquals(t.getDestination(), r2);
        t.setDestination(r1);
        assertEquals(t.getDestination(), r1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProperty_nullArg() {
        new Transfer(IdentityGenerator.generateId(), r1, r1).setProperty(null,
                "some val");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetProperty_nullArg() {
        new Transfer(IdentityGenerator.generateId(), r1, r2).getProperty(null);
    }

    @Test
    public void testGetAndSetProperty() {
        final Transfer t = new Transfer(IdentityGenerator.generateId(), r1, r2);
        final String propertyName = "priority";
        final String propertyValue = "1";
        final String propertyValue2 = "2";
        assertEquals(t.getProperty(propertyName), null);
        t.setProperty(propertyName, propertyValue);
        assertEquals(t.getProperty(propertyName), propertyValue);
        t.setProperty(propertyName, propertyValue2);
        assertEquals(t.getProperty(propertyName), propertyValue2);

    }

    @Test
    public void testEquality() throws URISyntaxException {
        final Transfer t1 = new Transfer(IdentityGenerator.generateId(), r1, r2);
        final Transfer t2 = new Transfer(IdentityGenerator.generateId(), r1, r2);
        assertEquals(t1, t2);

        final Transfer t3 = new Transfer("gsiftp://jacoby.isi.edu/tmp",
                "gsiftp://grid2.isi.edu/tmp");
        final Transfer t4 = new Transfer("gsiftp://jacoby.isi.edu/tmp",
                "gsiftp://grid2.isi.edu/tmp");
        assertEquals(t3, t4);

        assertFalse(t1.equals(t3));
    }

    @Test
    public void testCompareTo() {
        final Transfer t1 = new Transfer(IdentityGenerator.generateId(), r1, r1);
        final Transfer t2 = new Transfer(IdentityGenerator.generateId(), r1, r2);
        final Transfer t3 = new Transfer(IdentityGenerator.generateId(), r2, r1);
        final Transfer t4 = new Transfer(IdentityGenerator.generateId(), r2, r2);
        final Transfer t5 = new Transfer(IdentityGenerator.generateId(), r1, r2);

        assertTrue(t1.compareTo(t2) < 0);
        assertTrue(t1.compareTo(t3) < 0);
        assertTrue(t1.compareTo(t4) < 0);

        assertTrue(t2.compareTo(t1) > 0);
        assertTrue(t2.compareTo(t3) < 0);
        assertTrue(t2.compareTo(t4) < 0);

        assertTrue(t2.compareTo(t5) == 0);
    }
}

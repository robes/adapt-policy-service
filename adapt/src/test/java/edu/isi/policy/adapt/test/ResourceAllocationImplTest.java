package edu.isi.policy.adapt.test;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import edu.isi.policy.adapt.ResourceAllocation;
import edu.isi.policy.adapt.ResourceAllocationException;
import edu.isi.policy.adapt.ResourceAllocationImpl;
import edu.isi.policy.util.IdentityGenerator;

public class ResourceAllocationImplTest {

    @Test
    public void testToAndFromString() throws URISyntaxException,
            ResourceAllocationException {
        String id = IdentityGenerator.generateId();
        URI source = new URI("gsiftp://server1.isi.edu/tmp/test1/");
        URI dest = new URI("gsiftp://client1.isi.edu/tmp/test2/");
        int streams = 4;
        float rate = 10.45f;
        ResourceAllocation ra1 = new ResourceAllocationImpl(id, source, dest,
                streams, rate);
        ResourceAllocation ra2 = ResourceAllocationImpl.fromString(ra1
                .toString());
        assertEquals(ra1, ra2);
    }
}

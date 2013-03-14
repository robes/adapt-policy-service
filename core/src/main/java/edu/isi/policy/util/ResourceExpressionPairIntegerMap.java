package edu.isi.policy.util;

import java.io.IOException;
import java.io.InputStream;

import edu.isi.policy.entity.ResourceExpressionPair;

/**
 * Integer implementation of ResourceExpressionPairMap
 * 
 * @author David Smith
 * 
 */
public class ResourceExpressionPairIntegerMap extends
ResourceExpressionPairMap<Integer> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ResourceExpressionPairIntegerMap(InputStream stream)
            throws IOException {
        super(stream);
    }

    @Override
    protected void addStringEntry(ResourceExpressionPair rep, String value) {
        put(rep, Integer.parseInt(value));
    }

}

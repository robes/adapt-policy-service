package edu.isi.policy.util;

import java.io.IOException;
import java.io.InputStream;

import edu.isi.policy.entity.ResourceExpressionPair;

/**
 * Float implementation of the ResourceExpressionPairMap
 * 
 * @author David Smith
 * 
 */
public class ResourceExpressionPairFloatMap extends
ResourceExpressionPairMap<Float> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ResourceExpressionPairFloatMap(InputStream stream)
            throws IOException {
        super(stream);
    }

    @Override
    protected void addStringEntry(ResourceExpressionPair rep, String value) {
        put(rep, Float.parseFloat(value));
    }

}

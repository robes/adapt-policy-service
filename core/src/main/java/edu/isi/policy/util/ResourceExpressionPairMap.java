package edu.isi.policy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.isi.policy.entity.ResourceExpressionPair;

/**
 * Container for Map<ResourceExpressionPair, String> type.
 * 
 * @author David Smith
 * @param <T>
 * 
 */
public abstract class ResourceExpressionPairMap<T> extends
HashMap<ResourceExpressionPair, T> {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
            .getLogger(ResourceExpressionPairMap.class
                    .getName());

    /**
     * Constructs a resource expression pair map from a stream
     * 
     * @param the
     *            stream of the map contents, with each entry on its own line
     *            and fields separated by tabs
     * @throws IOException
     */
    public ResourceExpressionPairMap(InputStream inputStream)
            throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException(
                    "Input stream must be specified.");
        }
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        String line = null;
        String[] pieces = null;
        while((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0 && !line.startsWith("#")) {
                pieces = line.split("\\s+");
                if (pieces.length != 3) {
                    LOG.warn("Invalid line format: " + line);
                }

                addStringEntry(
                        new ResourceExpressionPair(pieces[0], pieces[1]),
                        pieces[2]);
            }
        }
        reader.close();
    }

    /**
     * Adds a string value as the parameterized type
     * 
     * @param key
     * @param value
     */
    abstract protected void addStringEntry(ResourceExpressionPair key,
            String value);

}

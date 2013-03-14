package edu.isi.policy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Convenience class for a linked list of strings that represents a list of
 * resource expressions.
 * 
 * @author David Smith
 * 
 */
public class ResourceExpressionList extends LinkedList<String> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a resource expression list from a resource
     * 
     * @param resource
     * @throws IOException
     */
    public ResourceExpressionList(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException(
                    "Input stream must be specified.");
        }
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        String line = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0 && !line.startsWith("#")) {
                add(line);
            }
        }
        reader.close();
    }
}

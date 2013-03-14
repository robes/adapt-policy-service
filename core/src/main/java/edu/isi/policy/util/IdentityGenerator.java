package edu.isi.policy.util;

import java.util.UUID;

/**
 * Utility class for generating a unique ID
 * 
 * @author David Smith
 * 
 */
public final class IdentityGenerator {

    /**
     * Generates a new identifier
     * 
     * @return the new ID
     */
    public static final String generateId() {
        return UUID.randomUUID().toString();
    }
}

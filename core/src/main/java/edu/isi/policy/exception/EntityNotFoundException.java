package edu.isi.policy.exception;

/**
 * Exception thrown when an entity is not found in policy
 * 
 * @author David Smith
 * 
 */
public class EntityNotFoundException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String entityId) {
        super("Entity " + entityId + " does not exist.");
    }

}

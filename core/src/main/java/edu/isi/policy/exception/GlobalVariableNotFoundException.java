package edu.isi.policy.exception;

/**
 * Exception thrown when a global variable does not exist in policy
 * 
 * @author David Smith
 * 
 */
public class GlobalVariableNotFoundException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public GlobalVariableNotFoundException(String variableName) {
        super("Global variable " + variableName + " does not exist.");
    }
}

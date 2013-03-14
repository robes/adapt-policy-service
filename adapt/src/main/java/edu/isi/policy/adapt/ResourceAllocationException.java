package edu.isi.policy.adapt;

public class ResourceAllocationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ResourceAllocationException(String message) {
        super(message);
    }

    public ResourceAllocationException(Exception e) {
        super(e);
    }

}

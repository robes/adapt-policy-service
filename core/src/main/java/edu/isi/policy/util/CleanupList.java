package edu.isi.policy.util;

import edu.isi.policy.entity.Cleanup;

/**
 * Convenience class for passing the generic transfer type in JSON messages as
 * lists.
 * 
 * @author David Smith
 * 
 */
public class CleanupList extends EntityList<Cleanup> {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a list with a given initial size
     * 
     * @param size
     */
    public CleanupList(int size) {
        super(size);
    }

    /**
     * Default constructor
     */
    public CleanupList() {
        super();
    }





}

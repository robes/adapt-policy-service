package edu.isi.policy.util;

import edu.isi.policy.entity.Transfer;

/**
 * Convenience class for passing the generic transfer type in JSON messages as
 * lists.
 * 
 * @author David Smith
 * 
 */
public class TransferList extends EntityList<Transfer> {

    /**
     * Constructs a list with a given initial size
     * 
     * @param size
     */
    public TransferList(int size) {
        super(size);
    }

    /**
     * Default constructor
     */
    public TransferList() {
        super();
    }


    /**
     * 
     */
    private static final long serialVersionUID = 1L;


}

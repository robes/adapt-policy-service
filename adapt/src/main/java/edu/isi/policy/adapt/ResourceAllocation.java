package edu.isi.policy.adapt;

import java.net.URI;

/**
 * Defines a resource allocation entry
 * 
 * @author David Smith
 * 
 */
public interface ResourceAllocation {

    /**
     * 
     * @return the ID of the resource allocation
     */
    public String getId();

    /**
     * Sets the ID of the resource allocation
     * 
     * @param id
     */
    public void setId(String id);

    /**
     * 
     * @return the source URI of the resource allocation
     */
    public URI getSource();

    /**
     * Sets the source URI of the resource allocation
     * 
     * @param source
     */
    public void setSource(URI source);

    /**
     * 
     * @return the destination URI of the resource allocation
     */
    public URI getDestination();

    /**
     * 
     * @param destination
     *            sets the destination URI of the resource allocation
     */
    public void setDestination(URI destination);

    /**
     * 
     * @return the transfer streams used by this resource allocation
     */
    public int getTransferStreams();

    /**
     * Sets the transfer streams used by this resource allocation
     * 
     * @param streams
     */
    public void setTransferStreams(int streams);

    /**
     * 
     * @return the transfer rate used by this resource allocation
     */
    public float getRate();

    /**
     * Sets the transfer rate used by this resource allocation
     * 
     * @param rate
     */
    public void setRate(float rate);
}

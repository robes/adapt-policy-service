package edu.isi.policy.entity;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Defines a transfer that is being sent to the policy service for advice.
 * 
 * @author David Smith
 * 
 */
public final class Transfer extends AbstractEntity implements
Comparable<Transfer> {
    private URI source;
    private URI destination;


    /**
     * Default constructor.
     */
    public Transfer() {
        super();
    }

    /**
     * Constructs a new transfer object.
     * 
     * @param id
     *            the unique identifier of this transfer.
     * @param source
     *            the source of the transfer.
     * @param destination
     *            the destination of the transfer.
     */
    public Transfer(String id, URI source, URI destination) {
        this(source, destination);
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("ID must be specified.");
        }
        setId(id);
    }

    public Transfer(URI source, URI destination) {
        this();
        if (source == null || destination == null) {
            throw new IllegalArgumentException(
                    "Source and Destination must be specified.");
        }
        this.source = source;
        this.destination = destination;
    }

    public Transfer(String source, String destination)
            throws URISyntaxException {
        this();
        if (source == null || destination == null) {
            throw new IllegalArgumentException(
                    "Source and Destination must be specified.");
        }
        this.source = new URI(source);
        this.destination = new URI(destination);
    }

    /**
     * Sets the source of the transfer.
     * 
     * @param source
     */
    public void setSource(URI source) {
        if (source == null) {
            throw new IllegalArgumentException("Source must be specified.");
        }
        this.source = source;
    }

    /**
     * Sets the destination of the transfer.
     * 
     * @param destination
     */
    public void setDestination(URI destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination must be specified.");
        }
        this.destination = destination;
    }

    /**
     * 
     * @return the source
     */
    public URI getSource() {
        return source;
    }

    /**
     * 
     * @return the destination
     */
    public URI getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer();
        if(source != null) {
            buf.append(source.toString());
        }
        buf.append("=>");
        if(destination != null) {
            buf.append(destination.toString());
        }
        buf.append(", ");
        if(getId() != null) {
            buf.append("id=").append(getId());
        }
        buf.append(", properties=")
        .append(getProperties().toString());

        return buf.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Transfer) {
            final Transfer t = (Transfer) o;
            if (this.source != null && this.destination != null
                    && t.source != null && t.destination != null) {
                return (this.source.equals(t.source) && this.destination
                        .equals(t.destination));
            }
        }
        return false;
    }

    @Override
    public int compareTo(Transfer t) {
        if (this.source == null || this.destination == null) {
            throw new IllegalStateException(
                    "Cannot compare a transfer that is missing a source or destination.");
        }
        if (t.source == null || t.destination == null) {
            throw new IllegalArgumentException(
                    "Comparable transfer is missing a source or destination.");
        }
        if (this.source.compareTo(t.source) < 0) {
            return -1;
        } else if (this.source.compareTo(t.source) > 0) {
            return 1;
        } else {
            if (this.destination.compareTo(t.destination) < 0) {
                return -1;
            } else if (this.destination.compareTo(t.destination) > 0) {
                return 1;
            }
        }
        return 0;
    }
}

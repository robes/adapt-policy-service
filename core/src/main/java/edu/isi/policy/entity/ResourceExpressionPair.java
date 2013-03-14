package edu.isi.policy.entity;

/**
 * Represents a pair of resources in a transfer to use when specifying resource
 * limits between them.
 * 
 * @author David Smith
 * 
 */
public class ResourceExpressionPair {
    private String source = null;
    private String destination = null;

    /**
     * Default constructor
     * 
     * @param source
     *            the source resource name (or a regular expression)
     * @param destination
     *            the destination resource name (or a regular expression)
     */
    public ResourceExpressionPair(String source, String destination) {
        if (source == null || source.length() == 0 || destination == null
                || destination.length() == 0) {
            throw new IllegalArgumentException(
                    "Source and destination must be specified.");
        }
        this.source = source;
        this.destination = destination;
    }

    /**
     * 
     * @return the source resource (or regular expression)
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source resource.
     * 
     * @param source
     *            the source resource
     */
    public void setSource(String source) {
        if (source == null || source.length() == 0) {
            throw new IllegalArgumentException("Source must be specified.");
        }
        this.source = source;
    }

    /**
     * 
     * @return the destination resource (or regular expression)
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the destination resource.
     * 
     * @param destination
     *            the destination resource
     */
    public void setDestination(String destination) {
        if (destination == null || destination.length() == 0) {
            throw new IllegalArgumentException("Destination must be specified.");
        }
        this.destination = destination;
    }

    @Override
    public String toString() {
        return new StringBuffer(source).append("->").append(destination)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ResourceExpressionPair) {
            return (source.equals(((ResourceExpressionPair) o).source) && destination
                    .equals(((ResourceExpressionPair) o).destination));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}

package edu.isi.policy.adapt;

import java.net.URI;

/**
 * Implementation of Resource Allocation.
 * 
 * @author David Smith
 * 
 */
public class ResourceAllocationImpl implements ResourceAllocation {

    private String id;
    private URI source;
    private URI destination;
    private int streams = 0;
    private float rate = 0.0f;

    private static final String ID_KEY = "id";
    private static final String SOURCE_KEY = "source";
    private static final String DESTINATION_KEY = "destination";
    private static final String STREAMS_KEY = "streams";
    private static final String RATE_KEY = "rate";
    private static final String FIELD_SEP = ",";
    private static final String KEY_SEP = "=";

    /**
     * Constructs a new resource allocation entry
     * 
     * @param id
     *            the ID of the resource allocation
     * @param source
     *            the source URI of the resource allocation
     * @param destination
     *            the destination URI of the resource allocation
     * @param streams
     *            the number of streams used by this resource allocation
     * @param rate
     *            the rate used by this resource allocation
     */
    public ResourceAllocationImpl(String id, URI source, URI destination,
            int streams, float rate) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("ID must be specified");
        }
        if (source == null || destination == null) {
            throw new IllegalArgumentException(
                    "Source and destination URI must be specified.");
        }
        if (streams < 0) {
            throw new IllegalArgumentException("Streams must be >= 0");
        }
        if (rate < 0) {
            throw new IllegalArgumentException("Rate must be >= 0");
        }
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.streams = streams;
        this.rate = rate;
    }

    /**
     * Constructs a resource allocation entry from a serialized version
     * 
     * @param body
     *            the serialized record
     * @return a new ResourceAllocation instance
     * @throws ResourceAllocationException
     *             if the ResourceAllocation cannot be constructed
     */
    public static ResourceAllocation fromString(String body)
            throws ResourceAllocationException {
        if (body == null || body.length() == 0) {
            throw new IllegalArgumentException("Body must be specified.");
        }
        final String[] pieces = body.split(FIELD_SEP);
        String[] keyValue = null;

        String id = null;
        URI source = null;
        URI destination = null;
        int streams = 0;
        float rate = 0;
        try {
            for (int i = 0; i < pieces.length; i++) {
                keyValue = pieces[i].split(KEY_SEP);
                if (ID_KEY.equals(keyValue[0])) {
                    id = keyValue[1];
                } else if (SOURCE_KEY.equals(keyValue[0])) {
                    source = new URI(keyValue[1]);
                } else if (DESTINATION_KEY.equals(keyValue[0])) {
                    destination = new URI(keyValue[1]);
                } else if (STREAMS_KEY.equals(keyValue[0])) {
                    streams = Integer.parseInt(keyValue[1]);
                } else if (RATE_KEY.equals(keyValue[0])) {
                    rate = Float.parseFloat(keyValue[1]);
                }
            }
        } catch (Exception e) {
            throw new ResourceAllocationException(e);
        }
        return new ResourceAllocationImpl(id, source, destination, streams,
                rate);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public URI getSource() {
        return source;
    }

    @Override
    public void setSource(URI source) {
        this.source = source;
    }

    @Override
    public URI getDestination() {
        return this.destination;
    }

    @Override
    public void setDestination(URI destination) {
        this.destination = destination;
    }

    @Override
    public int getTransferStreams() {
        return streams;
    }

    @Override
    public void setTransferStreams(int streams) {
        this.streams = streams;

    }

    @Override
    public float getRate() {
        return rate;
    }

    @Override
    public void setRate(float rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(ID_KEY + KEY_SEP);
        buf.append(id);
        buf.append(FIELD_SEP + SOURCE_KEY + KEY_SEP);
        buf.append(source);
        buf.append(FIELD_SEP + DESTINATION_KEY + KEY_SEP);
        buf.append(destination);
        buf.append(FIELD_SEP + STREAMS_KEY + KEY_SEP);
        buf.append(streams);
        buf.append(FIELD_SEP + RATE_KEY + KEY_SEP);
        buf.append(rate);
        return buf.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ResourceAllocationImpl) {
            ResourceAllocationImpl ra = (ResourceAllocationImpl) o;
            return this.id.equals(ra.getId())
                    && this.source.equals(ra.getSource())
                    && this.destination.equals(ra.getDestination())
                    && this.streams == ra.getTransferStreams()
                    && this.rate == ra.getRate();
        }
        return false;
    }
}

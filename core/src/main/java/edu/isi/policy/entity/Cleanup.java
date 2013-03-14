package edu.isi.policy.entity;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Defines a cleanup request that is sent to the Policy service when a client
 * wants to cleanup a previously transferred resource.
 * 
 * @author David Smith
 * 
 */
public class Cleanup extends AbstractEntity implements Comparable<Cleanup> {

    private URI uri;

    /**
     * Default constructor.
     */
    public Cleanup() {
        super();
    }

    /**
     * Constructs a cleanup given a resource
     * 
     * @param url
     *            the resource
     * @throws URISyntaxException
     *             if the resource is not a valid URI
     */
    public Cleanup(String url) throws URISyntaxException {
        this();
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("URL must be specified.");
        }
        this.uri = new URI(url);
    }

    public Cleanup(URI uri) {
        this();
        if (uri == null) {
            throw new IllegalArgumentException("URI must be specified.");
        }
        this.uri = uri;
    }

    /**
     * Sets the uri to cleanup
     * 
     * @param uri
     *            the uri
     */
    public void setUri(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URi must be specified.");
        }
        this.uri = uri;
    }

    /**
     * 
     * @return the uri to cleanup
     */
    public URI getUri() {
        return uri;
    }

    @Override
    public int compareTo(Cleanup o) {
        if (uri == null || o.uri == null) {
            throw new IllegalStateException(
                    "Cleanup must have a URL assigned to compare.");
        }
        return uri.compareTo(o.uri);
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        if (this.uri != null) {
            str.append(this.uri.toString());
        }
        str.append(this.getProperties().toString());
        return str.toString();
    }
}

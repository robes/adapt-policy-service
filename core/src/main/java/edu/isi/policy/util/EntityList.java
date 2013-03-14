package edu.isi.policy.util;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Common implementation of lists of types of entities (needed for JSON
 * serialization in Spring)
 * 
 * @author David Smith
 * 
 * @param <E>
 *            The raw type of the list elements
 */
abstract public class EntityList<E> extends ArrayList<E> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Properties properties;

    protected EntityList() {
        super();
        properties = new Properties();
    }

    protected EntityList(int size) {
        super(size);
        properties = new Properties();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setProperty(String propertyName, String propertyValue) {
        if (propertyName == null || propertyName.length() == 0) {
            throw new IllegalArgumentException(
                    "Property name must be specified.");
        }
        properties.setProperty(propertyName, propertyValue);
    }

    public String getProperty(String propertyName) {
        if (propertyName == null || propertyName.length() == 0) {
            throw new IllegalArgumentException(
                    "Property name must be specified.");
        }
        return properties.getProperty(propertyName);
    }
}

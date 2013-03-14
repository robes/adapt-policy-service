package edu.isi.policy.entity;

import java.util.Properties;

/**
 * Implementation of the Entity interface
 * 
 * @author David Smith
 * 
 */
abstract public class AbstractEntity implements Entity {

    private String id = null;
    private Properties properties;

    /**
     * Default constructor
     */
    protected AbstractEntity() {
        properties = new Properties();
    }

    @Override
    public void setId(String id) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("ID must be specified.");
        }
        this.id = id;
    }

    /**
     * 
     * @return the properties associated with this entity
     */
    @Override
    public Properties getProperties() {
        return properties;
    }

    /**
     * sets the properties associated with this resource
     * 
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties must be specified.");
        }
        this.properties = properties;
    }

    /**
     * 
     * @param propertyName
     *            name of the property
     * @return the value of this property
     */
    @Override
    public String getProperty(String propertyName) {
        if (propertyName == null || propertyName.length() == 0) {
            throw new IllegalArgumentException(
                    "Property name must be specified.");
        }
        return properties.getProperty(propertyName);
    }

    /**
     * Sets the property
     * 
     * @param propertyName
     * @param value
     */
    @Override
    public void setProperty(String propertyName, String value) {
        if (propertyName == null || propertyName.length() == 0) {
            throw new IllegalArgumentException(
                    "Property name must be specified.");
        }
        properties.setProperty(propertyName, value);
    }

    /**
     * 
     * @return the ID of the entity
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * 
     * @return true if the entity has an ID assigned
     */
    @Override
    public boolean hasId() {
        return id != null;
    }
}

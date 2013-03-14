package edu.isi.policy.entity;

import java.util.Properties;

/**
 * Defines an entity that can be managed in the policy service.
 * 
 * @author David Smith
 * 
 */
public interface Entity {

    /**
     * Sets the ID of the entity
     * 
     * @param id
     *            the unique ID of the entity
     */
    public void setId(String id);

    /**
     * 
     * @return the unique ID of the entity
     */
    public String getId();

    /**
     * 
     * @return true if the entity has an ID
     */
    public boolean hasId();

    /**
     * Sets a property of the entity
     * 
     * @param propertyName
     *            property name
     * @param propertyValue
     *            property value
     */
    public void setProperty(String propertyName, String propertyValue);

    /**
     * Sets the properties of the entity
     * 
     * @param properties
     */
    public void setProperties(Properties properties);

    /**
     * 
     * @param propertyName
     *            name of the property
     * @return the property
     */
    public String getProperty(String propertyName);

    /**
     * 
     * @return the entity properties
     */
    public Properties getProperties();

}

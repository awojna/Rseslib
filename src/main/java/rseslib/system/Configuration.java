/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
 * 
 *  This file is part of Rseslib.
 *
 *  Rseslib is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rseslib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package rseslib.system;

import java.io.*;
import java.util.*;

/**
 * The instances of this class
 * provides access to properties
 * provided in a constructor
 * or defined in the properties file.
 * The properties file name is the name of the class
 * extending this abstract class.
 *
 * @author      Grzegorz G�ra, Arkadiusz Wojna
 */
public abstract class Configuration
{
    /** Properties file extension. */
    private static final String PROPERTIES_EXTENSION = ".properties";

    /** Map between property names and property values. */
    private Properties m_Properties;
    /** The set of modifiable properties. */
    private Set<String> m_Modifiable = new HashSet<String>();

    /**
     * Constructor that reads properties from properties file.
     *
     * @param prop Map between property names and property values.
     * @throws PropertyConfigurationException If an I/O error occurs while reading properties.
     */
    public Configuration(Properties prop) throws PropertyConfigurationException
    {
        if (prop!=null) m_Properties = (Properties)prop.clone();
        else m_Properties = loadDefaultProperties(getClass());
    }

    /**
     * Constructor used when loadind the object from a file.
     */
    public Configuration()
    {
    }

    /**
     * Returns the default properties for a given class.
     *
     * @param configurableClass       Class.
     * @return Properties             Default properties for a given class name.
     * @throws PropertyConfigurationException If the class is not a subclass of Configuration.
     */
    public static Properties loadDefaultProperties(Class configurableClass) throws PropertyConfigurationException
    {
        Class superclass = configurableClass;
        while (superclass!=null && superclass!=Configuration.class) superclass = superclass.getSuperclass();
        if (superclass==null) throw new PropertyConfigurationException(configurableClass.getName()+" not a subclass of rseslib.system.Configuration");
        Properties prop = new Properties();
        Class cls = configurableClass;
        while (cls!=null)
        {
        	String path = '/'+cls.getName().replace('.', '/')+PROPERTIES_EXTENSION;
        	InputStream in = cls.getResourceAsStream(path);
    		try
    		{
    			if (in!=null) prop.load(new BufferedInputStream(in));
        	}
            catch (IOException e)
            {
                throw new PropertyConfigurationException(e.getMessage());
            }
            cls = cls.getSuperclass();
        }
        
        return prop;
    }

    /**
     * Provides properties but only for internal use of a configurable class.
     * 
     * @return	Properties.
     */
    protected Properties getProperties()
    {
    	return m_Properties;
    }

    /**
     * Returns property value as a string.
     *
     * @param propertyName     Property name.
     * @return                 Property value.
     * @throws PropertyConfigurationException If property is not found.
     */
    public String getProperty(String propertyName) throws PropertyConfigurationException
    {
        String propertyValueString = m_Properties.getProperty(propertyName);
        if (propertyValueString==null) throw new PropertyConfigurationException("Property "+propertyName+" for the class "+this.getClass().getName()+" is not defined");
        return propertyValueString;
    }

    /**
     * Returns property value as a boolean.
     *
     * @param propertyName     Property name.
     * @return                 Property value.
     * @throws PropertyConfigurationException If boolean property is not found.
     */
    public boolean getBoolProperty(String propertyName) throws PropertyConfigurationException
    {
        boolean val = false;
        String propertyValueString = m_Properties.getProperty(propertyName);
        if (propertyValueString==null) throw new PropertyConfigurationException("Property "+propertyName+" for the class "+this.getClass().getName()+" is not defined");
        if (propertyValueString.equalsIgnoreCase("true")) val = true;
        else if (propertyValueString.equalsIgnoreCase("false")) val = false;
        else throw new PropertyConfigurationException("The value of the property "+propertyName+" is neither TRUE nor FALSE");
        return val;
    }

    /**
     * Returns property value as int.
     *
     * @param propertyName     Property name.
     * @return                 Property value.
     * @throws PropertyConfigurationException If integer property is not found.
     */
    public int getIntProperty(String propertyName) throws PropertyConfigurationException
    {
        String propertyValueString = m_Properties.getProperty(propertyName);
        if (propertyValueString==null) throw new PropertyConfigurationException("Property "+propertyName+" for the class "+this.getClass().getName()+" is not defined");
        return Integer.parseInt(propertyValueString);
    }

    /**
     * Returns property value as double.
     *
     * @param propertyName     Property name.
     * @return                 Property value.
     * @throws PropertyConfigurationException If double property is not found.
     */
    public double getDoubleProperty(String propertyName) throws PropertyConfigurationException
    {
        String propertyValueString = m_Properties.getProperty(propertyName);
        if (propertyValueString==null) throw new PropertyConfigurationException("Property "+propertyName+" for the class "+this.getClass().getName()+" is not defined");
        return Double.parseDouble(propertyValueString);
    }

    /**
     * A given property is allowed for modification.
     *
     * @param propertyName     Property name.
     */
    protected void makePropertyModifiable(String propertyName)
    {
        if (!m_Modifiable.contains(propertyName))
            m_Modifiable.add(propertyName);
    }

    /**
     * Checks whether a given property can be modified.
     *
     * @param propertyName  Property name.
     * @return              True if the property can be modified, false otherwise.
     */
    public boolean isModifiableProperty(String propertyName)
    {
        return (m_Modifiable.contains(propertyName));
    }

    /**
     * Sets property to a given value.
     *
     * @param propertyName     Property name.
     * @param propertyValue    New property value.
     * @throws PropertyConfigurationException If property can not be changed.
     */
    public void setProperty(String propertyName, String propertyValue) throws PropertyConfigurationException
    {
        if (!m_Modifiable.contains(propertyName)) throw new PropertyConfigurationException("Parameter "+propertyName+" can not be changed");
        m_Properties.setProperty(propertyName, propertyValue);
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    protected void writeConfiguration(ObjectOutputStream out) throws IOException
    {
    	out.writeObject(m_Properties);
    	out.writeObject(m_Modifiable);
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    protected void readConfiguration(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	m_Properties = (Properties)in.readObject();
    	m_Modifiable = (Set<String>)in.readObject();
    }
}

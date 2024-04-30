/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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
 * @author      Grzegorz Gora, Arkadiusz Wojna
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
     * Returns the dictionary of possible values for all string properties for a given class.
     *
     * @param configurableClass		Class.
     * @return						Dictionary of possible values for all string properties for a given class.
     * @throws PropertyConfigurationException If the class is not a subclass of Configuration or a property file has an invalid format.
     */
    public static HashMap<String, String[]> possibleValues(Class configurableClass) throws PropertyConfigurationException
    {
        Class superclass = configurableClass;
        while (superclass!=null && superclass!=Configuration.class)
        	superclass = superclass.getSuperclass();
        if (superclass==null)
        	throw new PropertyConfigurationException(configurableClass.getName()+" not a subclass of rseslib.system.Configuration");
        HashMap<String, String[]> possibleVals = new HashMap<String, String[]>();
        Class cls = configurableClass;
        while (cls!=null)
        {
        	String path = '/'+cls.getName().replace('.', '/')+PROPERTIES_EXTENSION;
        	InputStream in = cls.getResourceAsStream(path);
        	if (in == null)
        	{
                cls = cls.getSuperclass();
        		continue;
        	}
        	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        	String line = null;
        	try
        	{
        		if (reader.ready())
        			line = reader.readLine();
        		while (line != null)
        		{
        			int idx = 8;
        			if (line.startsWith("##VALUES") && idx < line.length() && Character.isWhitespace(line.charAt(idx++)))
        			{
        				ArrayList<String> values = new ArrayList<String>();
        				for (; idx < line.length() && Character.isWhitespace(line.charAt(idx)); ++idx);
        				while (idx < line.length())
        				{
        					int begin = idx;
        					if (line.charAt(idx) == ',')
        						throw new PropertyConfigurationException("Invalid format of possible values in the properties file of " + cls.getCanonicalName());
        					for (; idx < line.length() && !Character.isWhitespace(line.charAt(idx)) && line.charAt(idx) != ','; ++idx);
        					values.add(line.substring(begin, idx));
        					for (; idx < line.length() && Character.isWhitespace(line.charAt(idx)); ++idx);
        					if (idx < line.length() && line.charAt(idx++) != ',')
        						throw new PropertyConfigurationException("Invalid format of possible values in the properties file of " + cls.getCanonicalName());
        					for (; idx < line.length() && Character.isWhitespace(line.charAt(idx)); ++idx);
        				}
        				if (values.isEmpty())
        					throw new PropertyConfigurationException("No possible value found after ##VALUES keyword in the properties file of " + cls.getCanonicalName());
        				while (reader.ready())
        				{
        					line = reader.readLine();
        					idx = 0;
        					for (; idx < line.length() && Character.isWhitespace(line.charAt(idx)); ++idx);
        					if (idx < line.length() && line.charAt(idx) != '#' && line.charAt(idx) != '!')
        					{
        						int eq = line.indexOf('=');
        						int colon = line.indexOf(':');
        						int end = -1;
        						if (eq < 0)
        							end = colon;
        						else if (colon < 0)
        							end = eq;
        						else
        							end = Math.min(eq,  colon);
        						while (end > idx && Character.isWhitespace(line.charAt(end - 1)))
        							--end;
        						if (end == idx)
        							throw new PropertyConfigurationException("Invalid format of a property in the properties file of " + cls.getCanonicalName());
        						possibleVals.put(line.substring(idx, end), values.toArray(new String[0]));
        						break;
        					}
        				}
        			}
        			line = (reader.ready() ? reader.readLine() : null);
        		}
        	} catch (IOException e) {
                throw new PropertyConfigurationException(e.getMessage());
        	}
        	String[] boolVals = { "TRUE", "FALSE" };
        	Properties props = loadDefaultProperties(configurableClass);
        	for (String prop : props.stringPropertyNames())
        	{
        		String val = props.getProperty(prop).toLowerCase();
        		if((val.equals("true") || val.equals("false")) && !possibleVals.containsKey(prop))
        			possibleVals.put(prop, boolVals);
        	}
            cls = cls.getSuperclass();
        }
        
        return possibleVals;
    }

    /**
     * Returns the default properties for a given class.
     *
     * @param configurableClass		Class.
     * @return						Default properties for a given class.
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

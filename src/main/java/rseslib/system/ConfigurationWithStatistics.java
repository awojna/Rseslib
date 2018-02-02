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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

/**
 * The instances of this class
 * provides access to statistics
 * obtained from computation.
 *
 * @author      Arkadiusz Wojna
 */
public abstract class ConfigurationWithStatistics extends Configuration
{
    /** Map between statistics names and statistics values. */
    private Properties m_Statistics = new Properties();

    /**
     * Constructor.
     *
     * @param prop Map between property names and property values.
     * @throws PropertyConfigurationException If an I/O error occurs while reading properties.
     */
    public ConfigurationWithStatistics(Properties prop) throws PropertyConfigurationException
    {
        super(prop);
    }

    /**
     * Constructor used when loadind the object from a file.
     */
    public ConfigurationWithStatistics()
    {
    }

    /**
    * Add a statistical value to statistics about the performance.
    *
    * @param name  Name of the statistical value to be added.
    * @param value Statistical value to be added.
    */
    public void addToStatistics(String name, String value)
    {
        m_Statistics.setProperty(name, value);
    }

    /**
    * Returns statistics about the performance.
    *
    * @return Statistics about the performance.
    */
    public Properties getStatistics()
    {
        return m_Statistics;
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    protected void writeConfigurationAndStatistics(ObjectOutputStream out) throws IOException
    {
    	writeConfiguration(out);
    	out.writeObject(m_Statistics);
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    protected void readConfigurationAndStatistics(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readConfiguration(in);
    	m_Statistics = (Properties)in.readObject();
    }
}

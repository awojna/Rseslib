/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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


package rseslib.system.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import rseslib.system.Report;

/**
 * The interface for the reporting output
 * that writes to file.
 *
 * @author      Arkadiusz Wojna
 */
public class FileOutput implements Output
{
    /**
     * Writer for information messages.
     */
    private Writer m_InfoWriter = null;

    public FileOutput(File reportFile) throws IOException
    {
        m_InfoWriter = new BufferedWriter(new FileWriter(reportFile));
    }

    /**
     * Outputs the information about a given object.
     *
     * @param obj Object to be output.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void display(Object obj) throws IOException
    {
        m_InfoWriter.write(obj.toString());
        m_InfoWriter.flush();
    }

    /**
     * Outputs the end of line.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void nl() throws IOException
    {
        m_InfoWriter.write(Report.lineSeparator);
        m_InfoWriter.flush();
    }

    /**
     * Closes this output.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void close() throws IOException
    {
        m_InfoWriter.close();
    }
}

/*
    A provenance-aware spreadsheet library
    Copyright (C) 2021-2022 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.spreadsheet.relation;

import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * A {@link Row} that keeps track of the name of each column.
 * @author Sylvain Hallé
 */
public class NamedRow extends Row
{
	/**
	 * The name of each column.
	 */
	/*@ non_null @*/ protected final Object[] m_columnNames;
	
	/**
	 * Creates a new named row.
	 * @param col_names The name of each attribute
	 * @param values The value of each attribute
	 */
	public NamedRow(Object[] col_names, Object[] values)
	{
		super(values);
		m_columnNames = col_names;
	}
	
	/**
	 * Gets the value associated to a column name.
	 * @param col_name The name of the column
	 * @return The corresponding value, or <tt>null</tt> if no such column name
	 * exists
	 */
	public Object valueOf(Object col_name)
	{
		for (int i = 0; i < m_columnNames.length; i++)
		{
			if (Spreadsheet.same(m_columnNames[i], col_name))
			{
				return m_contents[i];
			}
		}
		return null;
	}
}

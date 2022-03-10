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

import java.util.ArrayList;
import java.util.List;

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
	 * Gets the numerical column indices in a spreadsheet corresponding to each
	 * column name present in an array.
	 * @param s The spreadsheet to look into
	 * @param col_names A list of column names
	 * @return An array of non-negative indices, or <tt>null</tt> if at least one
	 * column name could not be found in the input spreadsheet
	 */
	/*@ null @*/ public static int[] getColumnIndices(Spreadsheet s, Object ... col_names)
	{
		int[] indices = new int[col_names.length];
		for (int n_col = 0; n_col < col_names.length; n_col++)
		{
			boolean found = false;
			for (int col = 0; col < s.getWidth(); col++)
			{
				Object name = s.get(col, 0);
				if (Spreadsheet.same(name, col_names[n_col]))
				{
					indices[n_col] = col;
					found = true;
					break;
				}
			}
			if (!found)
			{
				return null;
			}
		}
		return indices;
	}

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

	public NamedRow(Object[] col_names, int[] col_indices, int row, Spreadsheet s)
	{
		super(getValues(col_indices, row, s));
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

	/**
	 * Gets the row indices in a spreadsheet whose cells have the same values as
	 * those in the named row.
	 * @param s The spreadsheet to look into
	 * @param col_indices The indices of the columns in s corresponding to each
	 * column mentioned in this named row
	 * @return A list of row indices in the spreadsheet with the same value
	 */
	public List<Integer> indicesOf(Spreadsheet s, int[] col_indices)
	{
		List<Integer> indices = new ArrayList<Integer>();
		if (col_indices == null)
		{
			return indices;
		}
		for (int row = 1; row < s.getHeight(); row++)
		{
			Object[] row_o = s.getRow(row);
			boolean matches = true;
			for (int col = 0; col < col_indices.length && matches; col++)
			{
				if (!Spreadsheet.same(m_contents[col], row_o[col_indices[col]]))
				{
					matches = false;
				}
			}
			if (matches)
			{
				indices.add(row);
			}
		}
		return indices;
	}

	/**
	 * Gets the row indices in a spreadsheet whose cells have the same values as
	 * those in the named row.
	 * @param s The spreadsheet to look into
	 * @return A list of row indices in the spreadsheet with the same value
	 */
	/*@ non_null @*/ public List<Integer> indicesOf(Spreadsheet s)
	{
		return indicesOf(s, getColumnIndices(s));
	}

	/**
	 * Gets the numerical column indices in a spreadsheet corresponding to each
	 * column name present in this named row.
	 * @param s The spreadsheet to look into
	 * @return An array of non-negative indices, or <tt>null</tt> if at least one
	 * column name could not be found in the input spreadsheet
	 */
	/*@ null @*/ protected int[] getColumnIndices(Spreadsheet s)
	{
		return getColumnIndices(s, m_columnNames);
	}
	
	/**
	 * Fetches a subset of the values in a row of a spreadsheet.
	 * @param col_indices The indices of the columns to fetch
	 * @param row The index of the row in the spreadsheet
	 * @param s The spreadsheet to fetch the values from
	 * @return The array of values at the corresponding indices in that
	 * spreadsheet's row
	 */
	/*@ non_null @*/ protected static Object[] getValues(int[] col_indices, int row, Spreadsheet s)
	{
		Object[] values = new Object[col_indices.length];
		Object[] s_row = s.getRow(row);
		for (int i = 0; i < col_indices.length; i++)
		{
			values[i] = s_row[col_indices[i]];
		}
		return values;
	}
}

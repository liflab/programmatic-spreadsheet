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
 * An array of objects corresponding to the contents of a specific row in a
 * spreadsheet. The {@link Row} class overrides the {@link #equals(Object)
 * equals()} and {@link #hashCode() hashCode()} methods so that rows with
 * identical contents in two spreadsheets can be detected. It is used in some
 * relational operators (e.g. {@link Union}) to avoid inserting duplicates in
 * their output.
 * 
 * @author Sylvain Hallé
 */
/* package */ class Row implements Comparable<Row>
{
	/**
	 * The contents of the row.
	 */
	protected final Object[] m_contents;
	
	/**
	 * Creates a new row with given contents.
	 * @param contents The contents of the row
	 */
	public Row(Object ... contents)
	{
		super();
		m_contents = contents;
	}
	
	/**
	 * Writes the contents of the row in a row of a spreadsheet.
	 * @param s The spreadsheet to write to
	 * @param row The row index where to write the contents
	 */
	public void set(/*@ non_null @*/ Spreadsheet s, int row)
	{
		for (int col = 0; col < m_contents.length; col++)
		{
			s.set(col, row, m_contents[col]);
		}
	}
	
	/**
	 * Gets the row of the spreadsheet that contains the current row.
	 * @param s The spreadsheet
	 * @return The non-negative row index if the row is contained in the
	 * spreadsheet; -1 if the spreadsheet does not contain that row
	 */
	public int rowOf(/*@ non_null @*/ Spreadsheet s)
	{
		for (int row = 1; row < s.getHeight(); row++)
		{
			if (Spreadsheet.equalRows(m_contents, s.getRow(row)))
			{
				return row;
			}
		}
		return -1;
	}
	
	@Override
	public int hashCode()
	{
		int h = 0;
		for (Object o : m_contents)
		{
			if (o != null)
			{
				h += o.hashCode();
			}
		}
		return h;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Row))
		{
			return false;
		}
		Row r = (Row) o;
		if (r.m_contents.length != m_contents.length)
		{
			return false;
		}
		for (int i = 0; i < m_contents.length; i++)
		{
			if (!Spreadsheet.same(m_contents[i], r.m_contents[i]))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(Row r)
	{
		return Spreadsheet.compareRows(m_contents, r.m_contents);
	}
}

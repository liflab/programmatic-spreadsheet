/*
    A provenance-aware spreadsheet library
    Copyright (C) 2021 Sylvain Hallé

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
package ca.uqac.lif.spreadsheet;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;

/**
 * A part pointing to a precise cell within a spreadsheet.
 * @author Sylvain Hallé
 */
public class Cell implements Part
{
	/**
	 * The row corresponding to the cell
	 */
	protected final int m_column;
	
	/**
	 * The column corresponding to the cell
	 */
	protected final int m_row;
	
	/**
	 * Creates a new cell.
	 * @param col The column corresponding to the cell
	 * @param row The row corresponding to the cell
	 * @return The cell
	 */
	/*@ non_null @*/ public static Cell get(int col, int row)
	{
		return new Cell(col, row);
	}
	
	/**
	 * Creates a new cell.
	 * @param col The column corresponding to the cell
	 * @param row The row corresponding to the cell
	 */
	protected Cell(int col, int row)
	{
		super();
		m_row = row;
		m_column = col;
	}
	
	/**
	 * Gets the row of the current cell.
	 * @return The row index
	 */
	public int getRow()
	{
		return m_row;
	}
	
	/**
	 * Gets the column of the current cell.
	 * @return The column index
	 */
	public int getColumn()
	{
		return m_column;
	}
	
	@Override
	public boolean appliesTo(Object o)
	{
		return o instanceof Spreadsheet;
	}

	@Override
	public Part head()
	{
		return this;
	}

	@Override
	public Part tail()
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		return "Cell " + m_column + ":" + m_row;
	}
	
	@Override
	public int hashCode()
	{
		return m_column + m_row;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof Cell))
		{
			return false;
		}
		Cell c = (Cell) o;
		return m_column == c.m_column && m_row == c.m_row;
	}
	
	/**
	 * Retrieves the spreadsheet cell mentioned in a designator.
	 * @param d The designator
	 * @return The cell, or {@code null} if no cell is mentioned
	 */
	/*@ null @*/ public static Cell mentionedCell(Part d)
	{
		if (d instanceof Cell)
		{
			return (Cell) d;
		}
		if (d instanceof ComposedPart)
		{
			ComposedPart cd = (ComposedPart) d;
			for (int i = 0; i < cd.size(); i++)
			{
				Part p = cd.get(i);
				if (p instanceof Cell)
				{
					return (Cell) p;
				}
			}
		}
		return null;
	}
	
	/**
	 * Replaces the first cell mentioned in a designator by another one.
	 * @param d The designator
	 * @param replacement The cell to replace with
	 * @return A new designator with the cell replaced
	 */
	/*@ null @*/ public static Part replaceCellBy(Part d, Cell replacement)
	{
		if (d instanceof Cell)
		{
			return (Cell) d;
		}
		if (d instanceof ComposedPart)
		{
			ComposedPart cd = (ComposedPart) d;
			ComposedPart new_cd = new ComposedPart();
			for (int i = 0; i < cd.size(); i++)
			{
				Part p = cd.get(i);
				if (p instanceof Cell)
				{
					new_cd.add(replacement);
				}
				else
				{
					new_cd.add(p);
				}
			}
			return new_cd;
		}
		return null;
	}
}

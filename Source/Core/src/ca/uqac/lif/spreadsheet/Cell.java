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
package ca.uqac.lif.spreadsheet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;

/**
 * A part pointing to a precise cell within a spreadsheet. A cell instance can
 * be obtained in various ways using variations of static method <tt>get()</tt>:
 * <ul>
 * <li>By directly specifying the column and row index of the cell with
 * {@link #get(int, int)}</li>
 * <li>By using a character string naming the cell similar to Excel or
 * LibreOffice (e.g. "B3") with {@link #get(String)}</li>  
 * </ul>
 * <p>Row and column indices start at 0.</p>
 * @author Sylvain Hallé
 */
public class Cell implements Part
{
	/**
	 * The regex pattern to decompose a string designating a cell.
	 */
	protected static final Pattern s_coordinatePattern = Pattern.compile("([A-Za-z]+):{0,1}(\\d+)");
	
	/**
	 * The row corresponding to the cell.
	 */
	protected final int m_column;
	
	/**
	 * The column corresponding to the cell.
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
	 * Gets a cell instance from an Excel-like cell name. Following conventions,
	 * column indices use letter sequences (e.g. A, B, AF, etc.) and row indices
	 * are expressed as numbers, starting at 1.
	 * @param name The cell name
	 * @return The cell instance
	 */
	/*@ non_null @*/ public static Cell get(String name)
	{
		Matcher mat = s_coordinatePattern.matcher(name);
		if (!mat.find())
		{
			throw new SpreadsheetCellNameException("Invalid cell name");
		}
		String col = mat.group(1);
		String row = mat.group(2);
		return get(col, row);
	}
	
	/*@ non_null @*/ public static Cell get(String col, String row)
	{
		int col_nb = getColumnNumber(col);
		int row_nb = Integer.parseInt(row.trim()) - 1;
		return get(col_nb, row_nb);
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
	public Cell head()
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
	
	/**
	 * Gets the column number associated to an Excel-like character string.
	 * It turns out that Excel has particular numbering convention: it it not
	 * exactly a base-26 encoding. Letters A, B, &hellip;, Z are mapped to
	 * values 1, &hellip;, 26, and their position corresponds to a power of 26.
	 * Since column numbers start at zero, 1 must be subtracted from the
	 * resulting value. Hence&hellip;
	 * <ul>
	 * <li>"Z" corresponds to 26&times;26⁰ &minus; 1 = 25</li>
	 * <li>"AA" (the next column according to the convention) corresponds to
	 * 1&times;26¹ + 1&times;26⁰ &minus; 1 = 26</li>
	 * <li>"ABF" corresponds to 1&times;26² + 2&times;26¹ + 6&times;26⁰ &minus;
	 * 1 = 733</li>
	 * </ul>
	 * 
	 * @param s The character string. 
	 * @return The column number
	 */
	protected static int getColumnNumber(String s)
	{
		int value = 0;
		int pow = 0;
		for (int i = s.length() - 1; i >= 0; i--)
		{
			char letter = s.charAt(i);
			value += ((int) letter - 64) * Math.pow(26, pow);
			pow++;
		}
		return value - 1;
	}
}

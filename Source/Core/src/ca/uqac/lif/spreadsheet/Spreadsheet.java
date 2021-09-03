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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import ca.uqac.lif.util.Duplicable;

/**
 * A two-dimensional array of cells, made of a fixed number of columns and
 * rows. A spreadsheet must be created by specifying its dimensions, and these
 * dimensions cannot be changed once it has been created.
 * <p>
 * Cells can be written to using {@link #set(int, int, Object)}, and their
 * contents can be retrieved using {@link #get(int, int)}. There is no
 * restriction to the type of the values inside cells (each can be an arbitrary
 * {@link Object}).
 * 
 * @author Sylvain Hallé
 */
public class Spreadsheet implements Duplicable, Comparable<Spreadsheet>
{
	/**
	 * A spreadsheet printer used by the method {@link #toString()}.
	 */
	/*@ non_null @*/ protected static final AnsiSpreadsheetPrinter s_printer = new AnsiSpreadsheetPrinter();
	
	/**
	 * The rows contained in the spreadsheet.
	 */
	/*@ non_null @*/ protected final Object[][] m_entries;
	
	/**
	 * 
	 * @param width The number of columns
	 * @param height The number of rows
	 * @param entries A list of values to insert into the spreadsheet
	 * @return The spreadsheet
	 * @throws SpreadsheetOutOfBoundsException If the size of the array is not 
   * equal to width &times; height
	 */
	/*@ non_null @*/ public static Spreadsheet read(int width, int height, Object ... entries) throws SpreadsheetOutOfBoundsException
	{
		if (entries.length != width * height)
		{
			throw new SpreadsheetOutOfBoundsException("Invalid size for entry array");
		}
		Spreadsheet s = new Spreadsheet(width, height);
		int index = 0;
		for (int row = 0; row < height; row++)
		{
			for (int col = 0; col < width; col++)
			{
				s.set(col, row, entries[index++]);
			}
		}
		return s;
	}
	
	/**
	 * Creates an empty spreadsheet with a given number of rows and columns.
	 * @param width The number of columns
	 * @param height The number of rows
	 * 
	 */
	public Spreadsheet(int width, int height)
	{
		super();
		m_entries = new Object[height][width];
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				m_entries[i][j] = null;
			}
		}
	}
	
	/**
	 * Creates a spreadsheet by copying the contents of another spreadsheet.
	 * @param s The spreadsheet to copy from
	 */
	protected Spreadsheet(/*@ non_null @*/ Spreadsheet s)
	{
		super();
		int height = s.getHeight();
		int width = s.getWidth();
		m_entries = new Object[height][width];
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				m_entries[i][j] = s.m_entries[i][j];
			}
		}
	}

	/**
	 * Gets the number of rows of the spreadsheet.
	 * @return The number of rows
	 */
	public int getHeight()
	{
		return m_entries.length;
	}

	/**
	 * Gets the number of columns of the spreadsheet.
	 * @return The number of columns
	 */
	public int getWidth()
	{
		return m_entries[0].length;
	}

	/**
	 * Gets the content of a cell in the spreadsheet.
	 * @param col The column of the cell
	 * @param row The row of the cell
	 * @return The contents of the cell, or {@code null} if the cell is empty
	 * @throws SpreadsheetOutOfBoundsException If the column or row index is
	 * outside the bounds of the spreadsheet
	 */
	/*@ null @*/ public Object get(int col, int row) throws SpreadsheetOutOfBoundsException
	{
		checkColumn(col);
		checkRow(row);
		return m_entries[row][col];
	}
	
	/**
	 * Gets a row of the spreadsheet.
	 * @param row The row index
	 * @return The contents of the corresponding row
	 * @throws SpreadsheetOutOfBoundsException If the row index is
	 * outside the bounds of the spreadsheet
	 */
	/*@ non_null @*/ public Object[] getRow(int row) throws SpreadsheetOutOfBoundsException
	{
		checkRow(row);
		return m_entries[row];
	}

	/**
	 * Sets the content of a cell in the spreadsheet.
	 * @param col The column of the cell
	 * @param row The row of the cell
	 * @param value The value to put in the cell. Use {@code null} to erase the
	 * current contents of the cell
	 * @return This spreadsheet
	 */
	public Spreadsheet set(int col, int row, /*@ null @*/ Object value)
	{
		checkColumn(col);
		checkRow(row);
		m_entries[row][col] = value;
		return this;
	}

	/**
	 * Checks if a row index corresponds to a valid row in the spreadsheet.
	 * @param row The row index
	 * @throws SpreadsheetOutOfBoundsException If the row index is
	 * outside the bounds of the spreadsheet
	 */
	protected void checkRow(int row) throws SpreadsheetOutOfBoundsException
	{
		if (row < 0 || row >= getHeight())
		{
			throw new SpreadsheetOutOfBoundsException("Invalid row index: " + row);
		}
	}

	/**
	 * Checks if a column index corresponds to a valid column in the spreadsheet.
	 * @param col The column index
	 * @throws SpreadsheetOutOfBoundsException If the column index is
	 * outside the bounds of the spreadsheet
	 */
	protected void checkColumn(int col) throws SpreadsheetOutOfBoundsException
	{
		if (col < 0 || col >= getWidth())
		{
			throw new SpreadsheetOutOfBoundsException("Invalid column index: " + col);
		}
	}

	@Override
	public final Spreadsheet duplicate()
	{
		return duplicate(false);
	}

	@Override
	public Spreadsheet duplicate(boolean with_state)
	{
		return new Spreadsheet(this);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof Spreadsheet))
		{
			return false;
		}
		Spreadsheet s = (Spreadsheet) o;
		if (s.getHeight() != getHeight() || s.getWidth() != getWidth())
		{
			return false;
		}
		for (int row = 0; row < getHeight(); row++)
		{
			for (int col = 0; col < getWidth(); col++)
			{
				if (!same(get(col, row), s.get(col, row)))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Determines if two objects are the same.
	 * @param o1 The first object
	 * @param o2 The second object
	 * @return {@code true} if the two objects are the same, {@code false}
	 * otherwise
	 */
	public static boolean same(Object o1, Object o2)
	{
		// If only one of the two is null, they are different
		if ((o1 == null) != (o2 == null))
		{
			return false;
		}
		return o1 == o2 || o1 == null || o1.equals(o2);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int compare(Object o1, Object o2)
	{
		if (o1 == null)
		{
			if (o2 == null)
			{
				return 0;
			}
			return -1;
		}
		else
		{
			if (o2 == null)
			{
				return 1;
			}
			if (o1 instanceof Comparable)
			{
				try
				{
					return ((Comparable) o1).compareTo(o2);
				}
				catch (ClassCastException e)
				{
					// Occurs if we attempt to compare two objects that are not comparable
				}
			}
		}
		return 0;
	}
	
	@Override
	public String toString()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		s_printer.print(this, ps);
		return baos.toString();
	}

	@Override
	public int compareTo(Spreadsheet s)
	{
		if (s == null)
		{
			return 1;
		}
		int h1 = getHeight(), h2 = s.getHeight();
		for (int row = 0; row < Math.max(h1, h2); row++)
		{
			Object[] r1 = row < h1 ? getRow(row) : null;
			Object[] r2 = row < h2 ? s.getRow(row) : null;
			int comparison = compareRows(r1, r2);
			if (comparison != 0)
			{
				return comparison;
			}
		}
		return 0;
	}
	
	protected static int compareRows(Object[] r1, Object[] r2)
	{
		int l1 = r1.length, l2 = r2.length;
		for (int col = 0; col < Math.max(l1, l2); col++)
		{
			Object o1 = col < l1 ? r1[col] : null;
			Object o2 = col < l2 ? r2[col] : null;
			int comparison = compare(o1, o2);
			if (comparison != 0)
			{
				return comparison;
			}
		}
		return 0;
	}
}

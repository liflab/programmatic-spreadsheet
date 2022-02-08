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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.strings.Position;
import ca.uqac.lif.petitpoucet.function.strings.PositionRange;
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
	 * The regex pattern to find leading spaces in a string.
	 */
	protected static final Pattern s_leadingPattern = Pattern.compile("^\\s+");
	
	/**
	 * The regex pattern to find trailing spaces in a string.
	 */
	protected static final Pattern s_trailingPattern = Pattern.compile("\\s+$");
	
	/**
	 * A spreadsheet printer used by the method {@link #toString()}.
	 */
	/*@ non_null @*/ protected static final AnsiSpreadsheetPrinter s_printer = new AnsiSpreadsheetPrinter();
	
	/**
	 * The rows contained in the spreadsheet.
	 */
	/*@ non_null @*/ protected final Object[][] m_entries;
	
	/**
	 * Creates a spreadsheet out of an enumeration of its cell values.
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
	 * Creates a spreadsheet out of a scanner. The scanner must point to the
	 * start of a text source following these formatting conventions:
	 * <ul>
	 * <li>Each line corresponds to a row of the spreadsheet</li>
	 * <li>Lines made of only whitespace, and lines starting with
	 * <tt>comment_marker</tt> are ignored</li>
	 * <li>Cells in a line are separated by <tt>separator</tt>, and each
	 * chunk is converted into an object following the rules of
	 * {@link #readValue(String)}</li>
	 * </ul>
	 * @param scanner A scanner pointing to the start of a text source
	 * @param comment_marker The string used to denote a comment line
	 * @param separator The separator used to split a line into cells
	 * @param mapping An optional empty map. If not set to null, this map
	 * will be filled with associations between cells of the spreadsheet and
	 * the character ranges in the input they have been derived from.
	 * @return The resulting spreadsheet
	 * @see #readValue(String)
	 */
	/*@ non_null @*/ public static Spreadsheet read(/*@ non_null @*/ Scanner scanner, /*@ non_null @*/ String comment_marker, /*@ non_null @*/ String separator, /*@ null @*/ Map<Cell,Part> mapping)
	{
		int cols = 0, in_line_nb = -1, out_line_nb = -1;
		List<List<Object>> rows = new ArrayList<List<Object>>();
		Pattern pat = Pattern.compile(separator);
		while (scanner.hasNextLine())
		{
			String original_line = scanner.nextLine();
			String line = stripLeading(original_line);
			int spaces = original_line.length() - line.length();
			line = stripTrailing(line);
			in_line_nb++;
			if (line.isEmpty() || line.startsWith(comment_marker))
			{
				continue;
			}
			out_line_nb++;
			Matcher mat = pat.matcher(line);
			List<Object> objs = new ArrayList<Object>();
			int current_col = -1, last_pos = 0;
			while (mat.find())
			{
				current_col++;
				objs.add(readValue(line.substring(last_pos, mat.start())));
				if (mapping != null)
				{
					mapping.put(Cell.get(current_col, out_line_nb), ComposedPart.compose(new PositionRange(new Position(in_line_nb, last_pos + spaces), new Position(in_line_nb, last_pos + spaces + mat.start() - 1)), NthInput.FIRST));
				}
				last_pos = mat.end();
			}
			if (last_pos < line.length())
			{
				current_col++;
				objs.add(readValue(line.substring(last_pos)));
				if (mapping != null)
				{
					mapping.put(Cell.get(current_col, out_line_nb), ComposedPart.compose(new PositionRange(new Position(in_line_nb, last_pos + spaces), new Position(in_line_nb, spaces + line.length() - 1)), NthInput.FIRST));
				}
			}
			cols = Math.max(cols, current_col + 1);
			rows.add(objs);
		}
		Spreadsheet out = new Spreadsheet(cols, rows.size());
		for (int x = 0; x < rows.size(); x++)
		{
			List<Object> objs = rows.get(x);
			for (int y = 0; y < cols; y++)
			{
				if (y < objs.size())
				{
					out.set(y, x, objs.get(y));
				}
				else
				{
					out.set(y, x, null);
				}
			}
		}
		return out;
	}
	
	/**
	 * Creates a spreadsheet out of a scanner, using "#" as the comment marker
	 * and any number of whitespace characters as the cell separator.
	 * @param scanner A scanner pointing to the start of a text source
	 * @return The resulting spreadsheet
	 * @see #read(Scanner, String, String, Map)
	 */
	/*@ non_null @*/ public static Spreadsheet read(Scanner scanner)
	{
		return read(scanner, "#", "\\s+", null);
	}
		
	/**
	 * Creates a primitive value out of a character string. The rules are as
	 * follows:
	 * <ul>
	 * <li>The string "null" is interpreted as the <tt>null</tt> value
	 * (insensitive to case)</li>
	 * <li>The strings "true" and "false" return their corresponding Boolean
	 * value (insensitive to case)</li>
	 * <li>A string that parses as an integer returns the corresponding
	 * integer</li>
	 * <li>A string that parses as a double returns the corresponding
	 * double</li>
	 * <li>Any other string is returned as is</li> 
	 * </ul>
	 * @param o The character string
	 * @return The object
	 */
	protected static Object readValue(String o)
	{
		if (o.compareToIgnoreCase("null") == 0)
		{
			return null;
		}
		if (o.compareToIgnoreCase("true") == 0)
		{
			return true;
		}
		if (o.compareToIgnoreCase("false") == 0)
		{
			return false;
		}
		try
		{
			int x = Integer.parseInt(o);
			return x;
		}
		catch (NumberFormatException e)
		{
			// Do nothing
		}
		try
		{
			double x = Double.parseDouble(o);
			return x;
		}
		catch (NumberFormatException e)
		{
			// Do nothing
		}
		return o;
	}
	
	/**
	 * Creates an empty spreadsheet with a given number of rows and columns.
	 * @param width The number of columns
	 * @param height The number of rows
	 * 
	 */
	public Spreadsheet(int width, int height)
	{
		this(width, height, null);
	}
	
	/**
	 * Creates an spreadsheet with a given number of rows and columns, and
	 * fills its cells with a given value.
	 * @param width The number of columns
	 * @param height The number of rows
	 * @param o The value to fill each cell with
	 */
	public Spreadsheet(int width, int height, Object o)
	{
		super();
		m_entries = new Object[height][width];
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				m_entries[i][j] = o;
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
	/*@ pure non_null @*/ public Object[] getRow(int row) throws SpreadsheetOutOfBoundsException
	{
		checkRow(row);
		return m_entries[row];
	}
	
	/**
	 * Gets a row of the spreadsheet, and attempts to turn its elements into
	 * numbers.
	 * @param row The row index
	 * @return The numerical contents of the corresponding row
	 * @throws SpreadsheetOutOfBoundsException If the row index is
	 * outside the bounds of the spreadsheet
	 */
	/*@ pure non_null @*/ public Double[] getRowNumerical(int row) throws SpreadsheetOutOfBoundsException
	{
		Object[] objs = getRow(row);
		Double[] nums = new Double[objs.length];
		for (int i = 0; i < objs.length; i++)
		{
			if (objs[i] instanceof Number)
			{
				nums[i] = ((Number) objs[i]).doubleValue();
			}
			else
			{
				nums[i] = null;
			}
		}
		return nums;
	}
	
	/**
	 * Gets a cell of the spreadsheet, and attempts to turn it into a number.
	 * @param col The column index
	 * @param row The row index
	 * @return A number, or <tt>null</tt> if the value in the cell is not a
	 * number 
	 * @throws SpreadsheetOutOfBoundsException If the location is
	 * outside the bounds of the spreadsheet
	 */
	/*@ pure null @*/public Double getNumerical(int col, int row)
	{
		Object o = get(col, row);
		if (o instanceof Number)
		{
			return ((Number) o).doubleValue();
		}
		return null;
	}
	
	/**
	 * Gets a column of the spreadsheet, and attempts to turn its elements into
	 * numbers.
	 * @param col The column index
	 * @return The numerical contents of the corresponding column
	 * @throws SpreadsheetOutOfBoundsException If the column index is
	 * outside the bounds of the spreadsheet
	 */
	/*@ pure non_null @*/ public Double[] getColumnNumerical(int col) throws SpreadsheetOutOfBoundsException
	{
		Object[] objs = getColumn(col);
		Double[] nums = new Double[objs.length];
		for (int i = 0; i < objs.length; i++)
		{
			if (objs[i] instanceof Number)
			{
				nums[i] = ((Number) objs[i]).doubleValue();
			}
			else
			{
				nums[i] = null;
			}
		}
		return nums;
	}
	
	/**
	 * Gets a column of the spreadsheet.
	 * @param column The column index
	 * @return The contents of the corresponding column
	 * @throws SpreadsheetOutOfBoundsException If the column index is
	 * outside the bounds of the spreadsheet
	 */
	/*@ non_null @*/ public Object[] getColumn(int column) throws SpreadsheetOutOfBoundsException
	{
		checkColumn(column);
		Object[] out = new Object[getHeight()];
		for (int i = 0; i < out.length; i++)
		{
			out[i] = get(column, i);
		}
		return out;
	}
	
	/**
	 * Gets the values of the first row of the spreadsheet, cast as character
	 * strings.
	 * @return The contents of the corresponding row
	 * @throws SpreadsheetOutOfBoundsException If the row 0 is
	 * outside the bounds of the spreadsheet
	 */
	/*@ non_null @*/ public String[] getColumnNames() throws SpreadsheetOutOfBoundsException
	{
		Object[] o_row = getRow(0);
		String[] s_row = new String[o_row.length];
		for (int i = 0; i < o_row.length; i++)
		{
			if (o_row[i] == null)
			{
				s_row[i] = "null";
			}
			else
			{
				s_row[i] = o_row[i].toString();
			}
		}
		return s_row;
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
	 * Gets the index of the column whose first row contains a given object.
	 * @param o The object
	 * @return The column index, or -1 if the object is not found on the first
	 * row
	 */
	public int getColumnIndex(Object o)
	{
		for (int i = 0; i < getWidth(); i++)
		{
			if (same(o, get(i, 0)))
			{
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Gets the first non-null class of objects in a column, stating from the
	 * second row. This method is used to guess the type of a column.
	 * @param col_index The index of the column
	 * @return The first class, or <tt>null</tt> if no non-null cell exists in
	 * that column
	 */
	/*@ null @*/ public Class<?> getColumnType(int col_index)
	{
		for (int row = 1; row < getHeight(); row++)
		{
			Object o = get(col_index, row);
			if (o != null)
			{
				return o.getClass();
			}
		}
		return null;
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
		if ((o1 == null) != (o2 == null))
		{
			// If only one of the two is null, they are different
			return false;
		}
		if (o1 == o2)
		{
			// If both are equal, they are equal
			return true;
		}
		if (o1 instanceof Number && o2 instanceof Number)
		{
			// If both are numbers, compare their value
			return ((Number) o1).doubleValue() == ((Number) o2).doubleValue();
		}
		// Otherwise, rely on method equals
		return o1.equals(o2);
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
	
	/**
	 * Compares two arrays of objects representing the contents of two rows in
	 * a spreadsheet.
	 * @param r1 The values of the first row
	 * @param r2 The values of the second row
	 * @return A negative value if r1 goes before r2, 0 if the two rows are
	 * identical, and a positive value otherwise
	 * @see #equalRows(Object[], Object[])
	 */
	public static int compareRows(Object[] r1, Object[] r2)
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
	
	/**
	 * Determines if two arrays of objects representing the contents of two rows
	 * in a spreadsheet are equal.
	 * @param r1 The values of the first row
	 * @param r2 The values of the second row
	 * @return <tt>true</tt> if they are equal, <tt>false</tt> otherwise
	 * @see #compareRows(Object[], Object[])
	 */
	public static boolean equalRows(Object[] r1, Object[] r2)
	{
		int l1 = r1.length, l2 = r2.length;
		for (int col = 0; col < Math.max(l1, l2); col++)
		{
			Object o1 = col < l1 ? r1[col] : null;
			Object o2 = col < l2 ? r2[col] : null;
			if (!same(o1, o2))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Retrieves the value of a cell and converts it to a string.
	 * @param col The cell's column
	 * @param row The cell's row
	 * @return The value at that location in the spreadsheet, converted to a
	 * string using the value's <tt>toString()</tt> method
	 */
	/*@ pure non_null @*/ public String getString(int col, int row)
	{
		Object o = get(col, row);
		if (o == null)
		{
			return "null";
		}
		return o.toString();
	}
	
	/**
	 * Strips a string of its leading whitespace characters.
	 * @param s The string
	 * @return The stripped string
	 */
	/*@ non_null @*/ protected static String stripLeading(String s)
	{
		Matcher mat = s_leadingPattern.matcher(s);
		if (!mat.find())
		{
			return s;
		}
		return s.substring(mat.end());
	}
	
	/**
	 * Strips a string of its trailing whitespace characters.
	 * @param s The string
	 * @return The stripped string
	 */
	/*@ non_null @*/ protected static String stripTrailing(String s)
	{
		Matcher mat = s_leadingPattern.matcher(s);
		if (!mat.find())
		{
			return s;
		}
		return s.substring(0, mat.start());
	}

}

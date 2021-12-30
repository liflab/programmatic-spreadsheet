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

import java.io.PrintStream;

/**
 * A simple printer that renders a spreadsheet using text symbols, suitable for
 * an output such as a console. The printer takes care of aligning columns and
 * adjusting column widths to their contents, and can be told not to exceed a
 * given total width, possibly truncating column contents to achieve this.
 * <p>
 * This printer is used by the {@link Spreadsheet} class itself for its method
 * {@link #toString()}, thus providing a crude way of examining its contents.
 * 
 * @author Sylvain Hallé
 */
public class AnsiSpreadsheetPrinter implements SpreadsheetPrinter
{
	/**
	 * The maximum width (in characters) the table is allowed to take. Use -1
	 * to set no limit.
	 */
	protected int m_maxWidth;

	/**
	 * A flag determining whether successive cells having the same value across
	 * multiple rows should be displayed as a single merged cell. 
	 */
	protected boolean m_groupCells;

	/**
	 * A flag determining whether the first row contains values that should be
	 * displayed as column headers. 
	 */
	protected boolean m_headers;
	
	/**
	 * A character inserted between each column in each row.
	 */
	/*@ non_null @*/ protected String m_columnSeparator;
	
	/**
	 * The string inserted for a repeated value when cells are grouped.
	 */
	/*@ non_null @*/ protected String m_repeatSymbol;

	/**
	 * Sets the maximum width the table is allowed to take. 
	 * @param width The width (in characters); use -1 to set no limit
	 * @return This renderer
	 */
	/*@ non_null @*/ public AnsiSpreadsheetPrinter setMaxWidth(int width)
	{
		m_maxWidth = width;
		return this;
	}

	/**
	 * Sets whether successive cells having the same value across multiple rows
	 * should be displayed as a single merged cell. 
	 * @param b Set to {@code true} to merge cells, {@code false} otherwise
	 * @return This renderer
	 */
	/*@ non_null @*/ public AnsiSpreadsheetPrinter setGroupCells(boolean b)
	{
		m_groupCells = b;
		return this;
	}

	/**
	 * Sets whether the first row contains values that should be displayed as
	 * column headers. 
	 * @param b Set to {@code true} to display as headers, {@code false}
	 * otherwise
	 * @return This renderer
	 */
	/*@ non_null @*/ public AnsiSpreadsheetPrinter setHeaders(boolean b)
	{
		m_headers = b;
		return this;
	}
	
	/**
	 * Sets the column separator. 
	 * @param separator A character inserted between each column in each row
	 * @return This renderer
	 */
	/*@ non_null @*/ public AnsiSpreadsheetPrinter setColumnSeparator(/*@ non_null @*/ String separator)
	{
		m_columnSeparator = separator;
		return this;
	}
	
	/**
	 * Sets the string inserted for a repeated value when cells are grouped. 
	 * @param symbol A character inserted between each column in each row
	 * @return This renderer
	 */
	/*@ non_null @*/ public AnsiSpreadsheetPrinter setRepeatSymbol(/*@ non_null @*/ String symbol)
	{
		m_repeatSymbol = symbol;
		return this;
	}
	
	/**
	 * Creates a new spreadsheet printer with default settings.
	 */
	public AnsiSpreadsheetPrinter()
	{
		super();
		m_columnSeparator = " ";
		m_groupCells = false;
		m_headers = false;
		m_maxWidth = -1;
		m_repeatSymbol = "-";
	}

	@Override
	public void print(Spreadsheet s, PrintStream ps)
	{
		String[][] string_contents = stringify(s);
		int[] widths = getColumnWidths(string_contents);
		String[] last_row = new String[widths.length];
		boolean first = true;
		for (int row = 0; row < string_contents.length; row++)
		{
			String[] current_row = string_contents[row];
			for (int col = 0; col < current_row.length; col++)
			{
				if (!first && m_groupCells && last_row[col].compareTo(current_row[col]) == 0)
				{
					printWidth(ps, m_repeatSymbol, widths[col]);
				}
				else
				{
					printWidth(ps, printCell(current_row[col], s, col, row), widths[col]);
				}
				if (col < current_row.length - 1)
				{
					ps.print(m_columnSeparator);
				}
			}
			first = false;
			last_row = current_row;
			ps.println();
		}
	}
	
	/**
	 * Converts the contents of a spreadsheet into a string rendition of each
	 * cell.
	 * @param s The spreadsheet
	 * @return The contents of the original spreadsheet, transformed into
	 * character strings
	 */
	protected String[][] stringify(Spreadsheet s)
	{
		String[][] contents = new String[s.getHeight()][s.getWidth()];
		for (int row = 0; row < s.getHeight(); row++)
		{
			for (int col = 0; col < s.getWidth(); col++)
			{
				contents[row][col] = printCell(s.get(col, row), s, col, row);
			}
		}
		return contents;
	}
	
	/**
	 * Prints the content of a cell. Descendants of this class can override this
	 * method to customize the way cell contents are rendered.
	 * @param content The cell content
	 * @param s The spreadsheet this cell is taken from
	 * @param col The column index of the cell
	 * @param row The row index of the cell
	 * @return The string version of the cell
	 */
	protected String printCell(Object content, Spreadsheet s, int col, int row)
	{
		if (content == null)
		{
			return "null";
		}
		if (content instanceof Number)
		{
			Number n = (Number) content;
			if (n.intValue() == n.doubleValue())
			{
				return n.intValue() + "";
			}
			return n.toString();
		}
		return content.toString();
	}

	/**
	 * Prints a string a given number of times.
	 * @param ps The stream to print to
	 * @param s The string to print
	 * @param times The number of times to repeat the string
	 */
	protected static void printRepeat(PrintStream ps, String s, int times)
	{
		for (int i = 0; i < times; i++)
		{
			ps.print(s);
		}
	}

	/**
	 * Prints a string with a given width, truncating or padding with spaces as
	 * needed.
	 * @param ps The stream to print to
	 * @param s The string to print
	 * @param width The width to occupy
	 */
	protected static void printWidth(PrintStream ps, String s, int width)
	{
		if (s.length() > width)
		{
			ps.print(s.substring(0, width));
		}
		else
		{
			ps.print(s);
			printRepeat(ps, " ", width - s.length());
		}
	}

	/**
	 * Calculates the width of each column. This is done by taking the largest
	 * number of characters for entries of each column. In addition, if the total
	 * width exceeds the maximum width allowed for the table, each column is
	 * narrowed in the same proportion.
	 *  
	 * @param contents The contents of the original spreadsheet, already
	 * transformed into character strings
	 * @return An array containing the calculated width of each column
	 */
	protected int[] getColumnWidths(String[][] contents)
	{
		int[] widths = new int[contents[0].length];
		for (int row = 0; row < contents.length; row++)
		{
			for (int col = 0; col < widths.length; col++)
			{
				widths[col] = Math.max(widths[col], contents[row][col].length());
			}
		}
		if (m_maxWidth > 0)
		{
			int col_sep_len = m_columnSeparator.length();
			int total_width = 0;
			for (int col = 0; col < widths.length; col++)
			{
				total_width += widths[col];
				if (col > 0)
				{
					total_width += col_sep_len;
				}
			}
			if (total_width > m_maxWidth)
			{
				// Decrease all column widths by the same factor
				float factor = (float) m_maxWidth / (float) total_width;
				for (int col = 0; col < widths.length; col++)
				{
					widths[col] *= factor;
				}
			}
		}
		return widths;
	}

}

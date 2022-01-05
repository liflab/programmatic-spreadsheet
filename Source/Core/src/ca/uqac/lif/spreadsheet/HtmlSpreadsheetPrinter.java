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

import java.io.PrintStream;

/**
 * A printer that renders a spreadsheet as HTML markup. The printer takes
 * care of escaping characters of the cell values that clash with HTML
 * reserved symboles (e.g. <tt>&amp;amp;</tt> for <tt>&amp;</tt>), and method
 * {@link #mergeCells(boolean)} can be used to instruct the printer to merge
 * adjacent cells with the same value into a single cell spanning
 * multiple rows or multiple columns.
 * <p>
 * The HTML output itself is fully configurable, by overriding the methods
 * responsible for printing the various elements of an HTML table (e.g.
 * {@link #printRowStart(Spreadsheet, PrintStream, int) printRowStart} and
 * {@link #printRowEnd(Spreadsheet, PrintStream, int) printRowEnd} to
 * print the start/end of a row, etc.).
 * 
 * @author Sylvain Hallé
 */
public class HtmlSpreadsheetPrinter implements SpreadsheetPrinter
{
	/**
	 * A flag indicating whether adjacent cells with identical values should be
	 * merged into a single cell.
	 */
	protected boolean m_mergeCells = false;
	
	/**
	 * Sets whether adjacent cells with identical values should be
	 * merged into a single cell in the rendered spreadsheet.
	 * @param b Set to <tt>true</tt> to merge cells, <tt>false</tt>
	 * otherwise
	 * @return This printer
	 */
	public HtmlSpreadsheetPrinter mergeCells(boolean b)
	{
		m_mergeCells = b;
		return this;
	}

	@Override
	public void print(Spreadsheet s, PrintStream ps)
	{
		printTableStart(s, ps);
		for (int row = 0; row < s.getHeight(); row++)
		{
			printRowStart(s, ps, row);
			for (int col = 0; col < s.getWidth(); col++)
			{
				int colspan = m_mergeCells ? getColSpan(s, col, row) : 1;
				int rowspan = m_mergeCells ? getRowSpan(s, col, row) : 1;
				// Check for conflicts between column and row merging
				if (colspan < 1 && rowspan > 1)
				{
					rowspan = 1;
				}
				else if (colspan > 1 && rowspan < 1)
				{
					colspan = 1;
				}
				else if (colspan > 1 && rowspan > 1)
				{
					// Favor column merging over row merging
					rowspan = 1;
				}
				if (colspan < 1 || rowspan < 1)
				{
					continue;
				}
				printCellStart(s, ps, col, row, colspan, rowspan);
				printValue(s, ps, col, row, s.get(col, row));
				printCellEnd(s, ps, col, row, colspan, rowspan);
			}
			printRowEnd(s, ps, row);
		}
		printTableEnd(s, ps);
		
	}

	protected void printTableStart(Spreadsheet s, PrintStream ps)
	{
		ps.println("<table border=\"1\">");
	}
	
	protected void printTableEnd(Spreadsheet s, PrintStream ps)
	{
		ps.println("</table>");
	}
	
	protected void printRowStart(Spreadsheet s, PrintStream ps, int row)
	{
		if (row == 0)
		{
			ps.println("<thead>");
		}
		ps.println("<tr>");
	}
	
	protected void printCellStart(Spreadsheet s, PrintStream ps, int col, int row, int colspan, int rowspan)
	{
		String celltype = (row == 0 ? "th" : "td");
		ps.print("<");
		ps.print(celltype);
		if (colspan > 1)
		{
			ps.print(" colspan=\"");
			ps.print(colspan);
			ps.print("\"");
		}
		if (rowspan > 1)
		{
			ps.print(" rowspan=\"");
			ps.print(rowspan);
			ps.print("\"");
		}
		ps.println(">");
	}
	
	protected void printValue(Spreadsheet s, PrintStream ps, int col, int row, Object o)
	{
		if (o == null)
		{
			return;
		}
		ps.print(escape(o.toString()));
	}
	
	protected void printCellEnd(Spreadsheet s, PrintStream ps, int col, int row, int colspan, int rowspan)
	{
		String celltype = (row == 0 ? "th" : "td");
		ps.print("</");
		ps.print(celltype);
		ps.println(">");
	}
	
	protected void printRowEnd(Spreadsheet s, PrintStream ps, int row)
	{
		ps.println("</tr>");
		if (row == 0)
		{
			ps.println("</thead>");
		}
	}
	
	protected static String escape(String s)
	{
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		return s;
	}
	
	protected static int getRowSpan(Spreadsheet s, int col, int row)
	{
		if (row == 0)
		{
			// The first row is not merged as it is the table's header
			return 1;
		}
		if (row > 0 && Spreadsheet.same(s.get(col, row), s.get(col, row - 1)))
		{
			return -1;
		}
		int i = row;
		for (; i < s.getHeight() - 1; i++)
		{
			if (!Spreadsheet.same(s.get(col, i), s.get(col, i + 1)))
			{
				break;
			}
		}
		return i - row + 1;
	}
	
	protected static int getColSpan(Spreadsheet s, int col, int row)
	{
		if (col > 0 && Spreadsheet.same(s.get(col, row), s.get(col - 1, row)))
		{
			return -1;
		}
		int i = col;
		for (; i < s.getWidth() - 1; i++)
		{
			if (!Spreadsheet.same(s.get(i, row), s.get(i + 1, row)) || (row > 1 && Spreadsheet.same(s.get(i + 1, row), s.get(i + 1, row - 1))))
			{
				break;
			}
		}
		return i - col + 1;
	}

}

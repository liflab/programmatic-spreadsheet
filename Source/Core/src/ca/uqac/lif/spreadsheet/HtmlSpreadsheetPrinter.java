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
 * reserved symbols (e.g. <tt>&amp;amp;</tt> for <tt>&amp;</tt>), and method
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
public class HtmlSpreadsheetPrinter extends StructuredSpreadsheetPrinter
{
	@Override
	public HtmlSpreadsheetPrinter mergeCells(boolean b)
	{
		super.mergeCells(b);
		return this;
	}

	@Override
	protected void printTableStart(Spreadsheet s, PrintStream ps)
	{
		ps.println("<table border=\"1\">");
	}
	
	@Override
	protected void printTableEnd(Spreadsheet s, PrintStream ps)
	{
		ps.println("</table>");
	}
	
	@Override
	protected void printRowStart(Spreadsheet s, PrintStream ps, int row)
	{
		if (row == 0)
		{
			ps.println("<thead>");
		}
		if (row == 1)
		{
			ps.println("<tbody>");
		}
		ps.println("<tr>");
	}
	
	@Override
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
	
	@Override
	protected void printValue(Spreadsheet s, PrintStream ps, int col, int row, Object o)
	{
		if (o == null)
		{
			return;
		}
		ps.print(escape(o.toString()));
	}
	
	@Override
	protected void printCellEnd(Spreadsheet s, PrintStream ps, int col, int row, int colspan, int rowspan)
	{
		String celltype = (row == 0 ? "th" : "td");
		ps.print("</");
		ps.print(celltype);
		ps.println(">");
	}
	
	@Override
	protected void printRowEnd(Spreadsheet s, PrintStream ps, int row)
	{
		ps.println("</tr>");
		if (row == 0)
		{
			ps.println("</thead>");
		}
		if (row == s.getHeight() - 1) // Last row
		{
			ps.println("</tbody>");
		}
	}
	
	protected static String escape(String s)
	{
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		return s;
	}
}

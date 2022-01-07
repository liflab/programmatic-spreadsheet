package ca.uqac.lif.spreadsheet;

import java.io.PrintStream;

/**
 * A printer that renders a spreadsheet into some form of markup. Method
 * {@link #mergeCells(boolean)} can be used to instruct the printer to merge
 * adjacent cells with the same value into a single cell spanning
 * multiple rows or multiple columns.
 * <p>
 * The markup output itself is fully configurable, by overriding the «methods
 * responsible for printing the various elements of a table (e.g.
 * {@link #printRowStart(Spreadsheet, PrintStream, int) printRowStart} and
 * {@link #printRowEnd(Spreadsheet, PrintStream, int) printRowEnd} to
 * print the start/end of a row, etc.).
 * 
 * @author Sylvain Hallé
 */
public abstract class StructuredSpreadsheetPrinter implements SpreadsheetPrinter
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
	public StructuredSpreadsheetPrinter mergeCells(boolean b)
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
	
	protected abstract void printTableStart(Spreadsheet s, PrintStream ps);
	
	protected abstract void printTableEnd(Spreadsheet s, PrintStream ps);
	
	protected abstract void printRowStart(Spreadsheet s, PrintStream ps, int row);
	
	protected abstract void printCellStart(Spreadsheet s, PrintStream ps, int col, int row, int colspan, int rowspan);
	
	protected abstract void printValue(Spreadsheet s, PrintStream ps, int col, int row, Object o);
	
	protected abstract void printCellEnd(Spreadsheet s, PrintStream ps, int col, int row, int colspan, int rowspan);
	
	protected abstract void printRowEnd(Spreadsheet s, PrintStream ps, int row);
	
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

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
package ca.uqac.lif.spreadsheet.functions;

import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Modifies the size of a spreadsheet, by either adding null rows and columns
 * or by truncating rows and columns. For instance, given the following
 * spreadsheet:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>a</td><td>3</td><td>null</td></tr>
 * <tr><td>b</td><td>1</td><td>true</td></tr>
 * <tr><td>c</td><td>4</td><td>false</td></tr>
 * </tbody>
 * </table>
 * <p>
 * resizing it to 3 rows and 4 columns would lead to:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th><th>null</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>a</td><td>3</td><td>null</td><td>null</td></tr>
 * <tr><td>b</td><td>1</td><td>true</td><td>null</td></tr>
 * </tbody>
 * </table>
 * @author Sylvain Hallé
 */
public class Resize extends AtomicFunction
{
	/**
	 * The number of rows in the output spreadsheet.
	 */
	protected final int m_rows;
	
	/**
	 * The number of columns in the output spreadsheet.
	 */
	protected final int m_cols;
	
	/**
	 * The number of rows in the last input spreadsheet.
	 */
	protected int m_lastHeight;
	
	/**
	 * The number of columns in the last input spreadsheet.
	 */
	protected int m_lastWidth;
	
	/**
	 * Creates a new instance of the function.
	 * @param cols The number of columns in the output spreadsheet
	 * @param rows The number of rows in the output spreadsheet
	 */
	public Resize(int cols, int rows)
	{
		super(1, 1);
		m_rows = rows;
		m_cols = cols;
		m_lastHeight = 0;
		m_lastWidth = 0;
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet s = (Spreadsheet) inputs[0];
		Spreadsheet out = new Spreadsheet(m_cols, m_rows);
		m_lastWidth = s.getWidth();
		m_lastHeight = s.getHeight();
		for (int row = 0; row < Math.min(s.getHeight(), m_rows); row++)
		{
			for (int col = 0; col < Math.min(s.getWidth(), m_cols); col++)
			{
				out.set(col, row, s.get(col, row));
			}
		}
		return new Object[] {out};
	}
	
	@Override
	public PartNode getExplanation(Part d, RelationNodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		if (NthOutput.mentionedOutput(d) != 0 || m_lastHeight < 1)
		{
			root.addChild(f.getUnknownNode());
			return root;
		}
		Cell c = Cell.mentionedCell(d);
		if (c == null)
		{
			root.addChild(f.getPartNode(NthOutput.replaceOutByIn(d, 0), this));
			return root;
		}
		int c_row = c.getRow();
		int c_col = c.getColumn();
		if (c_row < 0 || c_row >= m_lastHeight || c_col < 0 || c_col >= m_lastWidth)
		{
			root.addChild(f.getPartNode(Part.nothing, this));
			return root;
		}
		root.addChild(f.getPartNode(NthOutput.replaceOutByIn(d, 0), this));
		return root;
	}
	
	@Override
	public String toString()
	{
		return "Resize " + m_cols + "\u00d7" + m_rows;
	}
	
	@Override
	public Resize duplicate(boolean with_state)
	{
		Resize r = new Resize(m_cols, m_rows);
		if (with_state)
		{
			r.m_lastHeight = m_lastHeight;
			r.m_lastWidth = m_lastWidth;
		}
		return r;
	}
}

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

import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Pastes the content of a spreadsheet at a given location in another
 * spreadsheet. The function takes two arguments:
 * <ol>
 * <li>The first is the "source" spreadsheet</li>
 * <li>The second is the "pasted" spreadsheet</li>
 * </ol>
 * The resulting spreadsheet consists of the contents of the source
 * spreadsheet, onto which the contents of the pasted spreadsheet are
 * overwritten. A location is specified when instantiating the function, by
 * providing the cell corresponding to the top left corner where the pasted
 * region must be placed. If the original spreadsheet is too small to
 * accommodate the pasted region, the output spreadsheet is enlarged as
 * necessary. Any cells that do not correspond to a cell in either the
 * source or the pasted spreadsheet take the default value (which should be
 * <tt>null</tt> unless otherwise specified).
 * <p>
 * As an example, consider the following "source" and the "pasted"
 * spreadsheets:
 * <p>
 * <div style="display:flex">
 * <table border="1" style="background:yellow">
 * <tbody>
 * <tr><td>3</td><td>1</td><td>4</td></tr>
 * <tr><td>1</td><td>5</td><td>9</td></tr>
 * <tr><td>2</td><td>6</td><td>5</td></tr>
 * </tbody>
 * </table>
 * &nbsp;&nbsp;
 * <table border="1" style="background:lightgreen">
 * <tbody>
 * <tr><td>2</td><td>7</td></tr>
 * <tr><td>1</td><td>8</td></tr>
 * </tbody>
 * </table>
 * </div>
 * <p>
 * Pasting the second table over the first one at cell (2,1) results in:
 * <p>
 * <table border="1" style="background:yellow">
 * <tbody>
 * <tr><td>3</td><td>1</td><td>4</td><td style="background:white"></td></tr>
 * <tr><td>1</td><td>5</td><td style="background:lightgreen">2</td><td style="background:lightgreen">7</td></tr>
 * <tr><td>2</td><td>6</td><td style="background:lightgreen">1</td><td style="background:lightgreen">8</td></tr>
 * </tbody>
 * </table>
 * <p>
 * Notice in this case how the top-right cell has no mapping to any of the two
 * input spreadsheets.
 * @author Sylvain Hallé
 *
 */
public class PasteAt extends AtomicFunction
{
	/**
	 * The cell representing the top-left corner where the pasted spreadsheet
	 * should be placed.
	 */
	/*@ non_null @*/ protected final Cell m_topLeft;
	
	/**
	 * The width of the last source spreadsheet.
	 */
	protected int m_lastSourceWidth;
	
	/**
	 * The height of the last source spreadsheet.
	 */
	protected int m_lastSourceHeight;
	
	/**
	 * The width of the last pasted spreadsheet.
	 */
	protected int m_lastPastedWidth;
	
	/**
	 * The height of the last pasted spreadsheet.
	 */
	protected int m_lastPastedHeight;
	
	/**
	 * Creates a new instance of the function.
	 * @param c The cell representing the top-left corner where the pasted
	 * spreadsheet should be placed.
	 */
	public PasteAt(/*@ non_null @*/ Cell c)
	{
		super(2, 1);
		m_topLeft = c;
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException, InvalidArgumentTypeException
	{
		if (!(inputs[0] instanceof Spreadsheet && inputs[1] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Both arguments must be spreadsheets");
		}
		Spreadsheet source = (Spreadsheet) inputs[0];
		m_lastSourceWidth = source.getWidth();
		m_lastSourceHeight = source.getHeight();
		Spreadsheet pasted = (Spreadsheet) inputs[1];
		m_lastPastedWidth = pasted.getWidth();
		m_lastPastedHeight = pasted.getHeight();
		int c_row = m_topLeft.getRow();
		int c_col = m_topLeft.getColumn();
		int width = Math.max(c_col + m_lastPastedWidth, source.getWidth());
		int height = Math.max(c_row + m_lastPastedHeight, source.getHeight());		
		Spreadsheet target = new Spreadsheet(width, height);
		for (int row = 0; row < m_lastSourceHeight; row++)
		{
			for (int col = 0; col < m_lastSourceWidth; col++)
			{
				target.set(col, row, source.get(col, row));
			}
		}
		for (int row = c_row; row < c_row + m_lastPastedWidth; row++)
		{
			for (int col = c_col; col < c_col + m_lastPastedHeight; col++)
			{
				target.set(col, row, pasted.get(col - c_col, row - c_row));
			}
		}
		return new Object[] {target};
	}
	
	@Override
	public PartNode getExplanation(Part d, NodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		if (NthOutput.mentionedOutput(d) != 0)
		{
			root.addChild(f.getUnknownNode());
			return root;
		}
		Cell c = Cell.mentionedCell(d);
		if (c == null)
		{
			return super.getExplanation(d, f);
		}
		int c_row = c.getRow();
		int c_col = c.getColumn();
		if (inPasted(c_col, c_row)) 
		{
			Part new_p = NthOutput.replaceOutByIn(d, 1);
			new_p = Cell.replaceCellBy(new_p, Cell.get(c_col - m_topLeft.getColumn(), c_row - m_topLeft.getRow()));
			root.addChild(f.getPartNode(new_p, this));
		}
		else if (inSource(c_col, c_row))
		{
			Part new_p = NthOutput.replaceOutByIn(d, 0);
			root.addChild(f.getPartNode(new_p, this));
		}
		else
		{
			root.addChild(f.getPartNode(Part.nothing, this));
		}
		return root;
	}
	
	/**
	 * Checks if a cell lies in the rectangle of the output spreadsheet
	 * corresponding to the source spreadsheet.
	 * <p>
	 * <strong>Caveat emptor:</strong> this method works in tandem with
	 * {@link #inPasted(int, int)}, and both methods are called by
	 * {@link #getExplanation(Part, NodeFactory)}. Method {@link #inSource(int, int)}
	 * is only called if {@link #inPasted(int, int)} first returns false,
	 * which means that this method only checks if <tt>col</tt> and <tt>row</tt>
	 * are within the dimensions of the source spreadsheet (disregarding any
	 * overlap of this rectangle with the pasted spreadsheet).
	 * @param col The cell column
	 * @param row The cell row
	 * @return <tt>true</tt> if the cell lies in the pasted spreadsheet,
	 * <tt>false</tt> otherwise
	 */
	protected boolean inSource(int col, int row)
	{
		return col >=0 && col < m_lastSourceWidth && row >= 0 && row < m_lastSourceHeight;
	}
	
	/**
	 * Checks if a cell lies in the rectangle of the output spreadsheet
	 * corresponding to the pasted spreadsheet.
	 * @param col The cell column
	 * @param row The cell row
	 * @return <tt>true</tt> if the cell lies in the pasted spreadsheet,
	 * <tt>false</tt> otherwise
	 */
	protected boolean inPasted(int col, int row)
	{
		int tl_col = m_topLeft.getColumn();
		int tl_row = m_topLeft.getRow();
		return col >= tl_col && col < tl_col + m_lastPastedWidth && row >= tl_row && row < tl_row + m_lastPastedHeight;
	}
	
	@Override
	public String toString()
	{
		return "Paste at " + m_topLeft;
	}
	
	@Override
	public PasteAt duplicate(boolean with_state)
	{
		PasteAt p = new PasteAt(m_topLeft);
		if (with_state)
		{
			p.m_lastSourceHeight = m_lastSourceHeight;
			p.m_lastSourceWidth = m_lastSourceWidth;
			p.m_lastPastedHeight = m_lastPastedHeight;
			p.m_lastPastedWidth = m_lastPastedWidth;
		}
		return p;
	}
}

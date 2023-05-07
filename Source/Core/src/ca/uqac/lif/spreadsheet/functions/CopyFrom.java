/*
    A provenance-aware spreadsheet library
    Copyright (C) 2021-2023 Sylvain Hallé

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
import ca.uqac.lif.spreadsheet.SpreadsheetOutOfBoundsException;

/**
 * Creates a new spreadsheet by copying a rectangular region from another
 * spreadsheet. The rectangular region is defined by its top-left and
 * bottom-right cells.
 * @author Sylvain Hallé
 *
 */
public class CopyFrom extends AtomicFunction
{
	/**
	 * The top left cell of the region to copy from.
	 */
	/*@ non_null @*/ protected final Cell m_topLeft;
	
	/**
	 * The bottom right cell of the region to copy from.
	 */
	/*@ non_null @*/ protected final Cell m_bottomRight;
	
	/**
	 * The height of the last spreadsheet evaluated by the function.
	 */
	protected int m_lastHeight;
	
	/**
	 * The width of the last spreadsheet evaluated by the function.
	 */
	protected int m_lastWidth;
	
	/**
	 * Creates a new instance of the function.
	 * @param top_left The top left cell of the region to copy from
	 * @param bottom_right The bottom right cell of the region to copy from
	 * @throws SpreadsheetOutOfBoundsException If the bounds of the region are
	 * invalid (e.g. refer to indices &lt; 0, or are such that a coordinate of
	 * <tt>bottom_right</tt> is smaller than that of <tt>top_left</tt>) 
	 */
	public CopyFrom(/*@ non_null @*/ Cell top_left, /*@ non_null @*/ Cell bottom_right)
	{
		super(1, 1);
		m_topLeft = top_left;
		m_bottomRight = bottom_right;
		int r1 = m_topLeft.getRow();
		int c1 = m_topLeft.getColumn();
		int r2 = m_bottomRight.getRow();
		int c2 = m_bottomRight.getColumn();
		if (r1 < 0 || r1 > r2 || c1 < 0 || c1 > c2)
		{
			throw new SpreadsheetOutOfBoundsException("Invalid cell range");
		}		
	}
	
	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		int r1 = m_topLeft.getRow();
		int c1 = m_topLeft.getColumn();
		int r2 = m_bottomRight.getRow();
		int c2 = m_bottomRight.getColumn();
		Spreadsheet source = (Spreadsheet) inputs[0];
		m_lastWidth = source.getWidth();
		m_lastHeight = source.getHeight();
		Spreadsheet out = new Spreadsheet(c2 - c1 + 1, r2 - r1 + 1);
		for (int row = r1; row <= r2; row++)
		{
			for (int col = c1; col <= c2; col++)
			{
				if (row < m_lastHeight && col < m_lastWidth)
				{
					out.set(col - c1, row - r1, source.get(col, row));
				}
			}
		}
		return new Object[] {out};
	}
	
	@Override
	public PartNode getExplanation(Part d, RelationNodeFactory f)
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
		int cr = c.getRow();
		int cc = c.getColumn();
		int r1 = m_topLeft.getRow();
		int c1 = m_topLeft.getColumn();
		int r2 = m_topLeft.getRow();
		int c2 = m_topLeft.getColumn();
		if (cc < 0 || cr < 0)
		{
			// Nonsensical cell
			root.addChild(f.getUnknownNode());
			return root;
		}
		if (cc > c2 - c1 + 1 || cr > r2 - r1 + 1)
		{
			// Outside the copied region at the bottom or the right
			root.addChild(f.getPartNode(Part.nothing, this));
			return root;
		}
		if (cc > c2 + m_lastWidth || cr > r2 + m_lastHeight)
		{
			// Empty cells lying outside the input spreadsheet 
			root.addChild(f.getPartNode(Part.nothing, this));
			return root;
		}
		// We ask about a cell that was inside the input spreadsheet
		Part new_p = NthOutput.replaceOutByIn(d, 0);
		new_p = Cell.replaceCellBy(new_p, Cell.get(cc + c1, cr + r1));
		root.addChild(f.getPartNode(new_p, this));
		return root;
	}
	
	@Override
	public CopyFrom duplicate(boolean with_state)
	{
		CopyFrom cf = new CopyFrom(m_topLeft, m_bottomRight);
		if (with_state)
		{
			cf.m_lastHeight = m_lastHeight;
			cf.m_lastWidth = m_lastWidth;
		}
		return cf;
	}
	
	@Override
	public String toString()
	{
		return "Copy from " + m_topLeft + "-" + m_bottomRight;
	}
}

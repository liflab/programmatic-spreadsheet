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

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.NthOutput;

/**
 * A 1:1 atomic function that turns a spreadsheet into another one, and for
 * which there exists a one-to-many correspondence between cells of the input
 * and the output. This correspondence is stored in an internal array, which is
 * used to answer provenance queries.
 * 
 * @author Sylvain Hallé
 */
public abstract class SpreadsheetFunction extends AtomicFunction
{
	/**
	 * A mapping keeping the correspondence between cells of the input table
	 * and cells of the output table. This array is used to answer provenance
	 * queries.
	 */
	/*@ null @*/ protected Cell[][][] m_mapping;
	
	public SpreadsheetFunction()
	{
		super(1, 1);
	}
	
	@Override
	public void reset()
	{
		super.reset();
		m_mapping = null;
	}
	
	@Override
	public PartNode getExplanation(Part part, NodeFactory factory)
	{
		int output_nb = NthOutput.mentionedOutput(part);
		Cell c = Cell.mentionedCell(part);
		if (output_nb != 0 || c == null)
		{
			return super.getExplanation(part, factory);
		}
		PartNode root = factory.getPartNode(part, this);
		int row = c.getRow(), col = c.getColumn();
		if (m_mapping != null && row >= 0 && row < m_mapping.length && col >= 0 && col < m_mapping[0].length)
		{
			Cell[] new_cells = m_mapping[row][col];
			if (new_cells != null)
			{
				LabelledNode and = root;
				if (new_cells.length > 1)
				{
					AndNode an = factory.getAndNode();
					root.addChild(an);
					and = an;
				}
				for (Cell new_cell : new_cells)
				{
					Part new_part = NthOutput.replaceOutByIn(Cell.replaceCellBy(part, new_cell), 0);
					PartNode child = factory.getPartNode(new_part, this);
					and.addChild(child);		
				}
			}
		}
		return root;
	}
	
	
	/**
	 * A mapping between a value and a cell of the original input spreadsheet. 
	 */
	protected static class TrackedCell
	{
		/**
		 * The value of the cell.
		 */
		/*@ null @*/ protected final Object m_value;
		
		/**
		 * The cell of the original spreadsheet this value derives from.
		 */
		/*@ non_null @*/ protected final Cell m_origin;
		
		/**
		 * Creates a new tracked cell.
		 * @param value The value of the cell
		 * @param origin The cell of the original spreadsheet this value derives
		 * from
		 */
		public TrackedCell(/*@ null @*/ Object value, /*@ non_null @*/ Cell origin)
		{
			super();
			m_value = value;
			m_origin = origin;
		}
		
		/**
		 * Gets the cell of the original table this value derives from.
		 * @return The cell
		 */
		/*@ pure non_null @*/ public Cell getOrigin()
		{
			return m_origin;
		}
		
		/**
		 * Gets the value of the cell.
		 * @return The value
		 */
		/*@ pure null @*/ public Object getValue()
		{
			return m_value;
		}
		
		@Override
		public String toString()
		{
			if (m_value == null)
			{
				return "null";
			}
			return m_value.toString();
		}
	}
}

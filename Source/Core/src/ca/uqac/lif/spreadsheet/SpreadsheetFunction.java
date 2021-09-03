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
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;

/**
 * A n:1 atomic function that turns a spreadsheet into another one, and for
 * which there exists a one-to-many correspondence between cells of the input
 * and the output. This correspondence is stored in an internal array, which is
 * used to answer provenance queries.
 * 
 * @author Sylvain Hallé
 */
public abstract class SpreadsheetFunction extends AtomicFunction
{
	/**
	 * A flag that determines if the first row of each spreadsheet should be
	 * interpreted as column labels, and not a normal row of data.
	 */
	protected boolean m_excludeFirst;

	/**
	 * A mapping keeping the correspondence between cells of the input
	 * spreadsheet(s) and cells of the output spreadsheet. This array is used to
	 * answer provenance queries.
	 */
	/*@ null @*/ protected InputCell[][][] m_mapping;
	
	public SpreadsheetFunction(int in_arity)
	{
		super(in_arity, 1);
	}
	
	/**
	 * Sets whether the first row of each spreadsheet should be interpreted as
	 * column labels, and not a normal row of data.
	 * @param b Set to {@code true} to handle first row as headers, {@code false}
	 * otherwise.
	 * @return This function
	 */
	/*@ non_null @*/ public SpreadsheetFunction excludeFirst(boolean b)
	{
		m_excludeFirst = b;
		return this;
	}
	
	/**
	 * Sets the function so that the first row of each spreadsheet is be
	 * interpreted as column labels, and not a normal row of data. This is
	 * equivalent to the call {@code excludeFirst(true)}.
	 * @return This function
	 */
	/*@ non_null @*/ public SpreadsheetFunction excludeFirst()
	{
		return excludeFirst(true);
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
			InputCell[] new_cells = m_mapping[row][col];
			if (new_cells != null)
			{
				LabelledNode and = root;
				if (new_cells.length > 1)
				{
					AndNode an = factory.getAndNode();
					root.addChild(an);
					and = an;
				}
				for (InputCell new_cell : new_cells)
				{
					PartNode child = factory.getPartNode(new_cell.getPart(), this);
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
		/*@ non_null @*/ protected final InputCell m_origin;
		
		/**
		 * Creates a new tracked cell.
		 * @param value The value of the cell
		 * @param origin The cell of the original spreadsheet this value derives
		 * from
		 */
		public TrackedCell(/*@ null @*/ Object value, /*@ non_null @*/ InputCell origin)
		{
			super();
			m_value = value;
			m_origin = origin;
		}
		
		/**
		 * Gets the cell of the original table this value derives from.
		 * @return The cell
		 */
		/*@ pure non_null @*/ public InputCell getOrigin()
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
	
	/**
	 * A {@link Cell} carrying the index of the input spreadsheet it refers to.
	 */
	protected static class InputCell extends Cell
	{
		/**
		 * The index of the input argument this cell points to.
		 */
		protected int m_index;
		
		/**
		 * Creates a new input cell.
		 * @param col The column corresponding to the cell
		 * @param row The row corresponding to the cell
		 * @param index The index of the input argument this cell points to
		 */
		/*@ non_null @*/ public static InputCell get(int col, int row, int index)
		{
			return new InputCell(col, row, index);
		}
		
		/**
		 * Creates a new input cell referring to input at index 0.
		 * @param col The column corresponding to the cell
		 * @param row The row corresponding to the cell
		 */
		/*@ non_null @*/ public static InputCell get(int col, int row)
		{
			return get(col, row, 0);
		}

		/**
		 * Creates a new input cell.
		 * @param col The column corresponding to the cell
		 * @param row The row corresponding to the cell
		 * @param index The index of the input argument this cell points to
		 */
		public InputCell(int col, int row, int index)
		{
			super(col, row);
			m_index = index;
		}
		
		/**
		 * Gets the composed part designated by this input cell.
		 * @return The part
		 */
		/*@ non_null @*/ public Part getPart()
		{
			return ComposedPart.compose(Cell.get(m_column, m_row), new NthInput(m_index));
		}
		
		@Override
		public int hashCode()
		{
			return m_column + m_row + m_index;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o == null || !(o instanceof InputCell))
			{
				return false;
			}
			InputCell c = (InputCell) o;
			return m_column == c.m_column && m_row == c.m_row && m_index == c.m_index;
		}
		
		@Override
		public String toString()
		{
			return "Cell " + m_column + ":" + m_row + " of input " + m_index;
		}
	}
}

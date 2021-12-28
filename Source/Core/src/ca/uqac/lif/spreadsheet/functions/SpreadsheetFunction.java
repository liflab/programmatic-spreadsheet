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
package ca.uqac.lif.spreadsheet.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

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
	
	/*@ non_null @*/ protected Row findRow(Object[] row_contents, int row_index, List<Row> new_rows)
	{
		for (Row r : new_rows)
		{
			if (r.matches(row_contents))
			{
				return r;
			}
		}
		Row r = new Row(row_contents, row_index);
		new_rows.add(r);
		return r;
	}

	protected Spreadsheet fillSpreadsheet(List<Row> new_rows, List<TrackedCell> new_headers)
	{
		Spreadsheet out = new Spreadsheet(new_headers.size(), new_rows.size() + 1);
		m_mapping = new InputCell[new_rows.size() + 1][new_headers.size()][];
		for (int col = 0; col < new_headers.size(); col++)
		{
			out.set(col, 0, new_headers.get(col).getValue());
			m_mapping[0][col] = new InputCell[] {new_headers.get(col).getOrigin()};
		}
		for (int row = 0; row < new_rows.size(); row++)
		{
			Row r = new_rows.get(row);
			for (int col = 0; col < r.m_staticColumns.length; col++)
			{
				out.set(col, row + 1, r.m_staticColumns[col].getValue());
				m_mapping[row + 1][col] = new InputCell[] {r.m_staticColumns[col].getOrigin()};
			}
			for (int col = r.m_staticColumns.length; col < new_headers.size(); col++)
			{
				Object key = new_headers.get(col).getValue();
				TrackedCell tc = r.m_otherValues.get(key);
				if (tc != null)
				{
					out.set(col, row + 1, tc.getValue());
					m_mapping[row + 1][col] = new InputCell[] {tc.getOrigin()};
				}
			}
		}
		return out;
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

	protected class Row
	{
		/*@ non_null @*/ protected TrackedCell[] m_staticColumns;

		/*@ non_null @*/ protected Map<Object,TrackedCell> m_otherValues;
		
		protected Row()
		{
			super();
		}
		
		public Row(/*@ non_null @*/ Object[] row, int row_index)
		{
			super();
			m_staticColumns = new TrackedCell[row.length - 2];
			int index = 0;
			for (int i = 0; i < m_staticColumns.length; i++)
			{
				m_staticColumns[index++] = new TrackedCell(row[i], InputCell.get(i, row_index));
			}
			m_otherValues = new HashMap<Object,TrackedCell>();
		}

		public void add(Object key, TrackedCell value)
		{
			m_otherValues.put(key, value);
		}

		@Override
		public int hashCode()
		{
			int c = 0;
			for (Object o : m_staticColumns)
			{
				if (o != null)
				{
					c += o.hashCode();
				}
			}
			return c;
		}

		public boolean matches(Object[] row)
		{
			int index = 0;
			for (int i = 0; i < m_staticColumns.length; i++)
			{
				Object tv = m_staticColumns[index].getValue();
				if (((tv == null) != (row[i] == null)) ||
						(tv != null && !tv.equals(row[i])))
				{
					return false;
				}
				index++;
			}
			return true;
		}		
	}
}

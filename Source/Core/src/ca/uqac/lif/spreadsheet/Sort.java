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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;

/**
 * Sorts the rows of a spreadsheet.
 * @author Sylvain Hallé
 */
public class Sort extends SpreadsheetFunction
{
	/**
	 * A flag used to exclude the first row of a spreadsheet from the sort.
	 */
	protected boolean m_excludeFirst;
	
	/**
	 * A list of conditions used to sort the rows.
	 */
	/*@ non_null @*/ protected List<SortingCondition> m_conditions;
	
	public Sort()
	{
		super(1);
		m_conditions = new ArrayList<SortingCondition>();
	}
	
	/**
	 * Sets whether the first row of the spreadsheet is excluded from the
	 * sort.
	 * @param b Set to {@code true} to exclude first row, {@code false}
	 * otherwise
	 * @return This function
	 */
	/*@ non_null @*/ public Sort excludeFirst(boolean b)
	{
		m_excludeFirst = b;
		return this;
	}
	
	/**
	 * Sets the function to exclude the first row of the spreadsheet from the
	 * sort. This is equivalent to calling {@code excludeFirst(true)}.
	 * @return This function
	 */
	/*@ non_null @*/ public Sort excludeFirst()
	{
		return excludeFirst(true);
	}
	
	/**
	 * Adds a sorting condition to the current function instance.
	 * @param index The index of the column to be sorted
	 * @param ascending Set to {@code true} to sort by increasing values,
	 * {@code false} otherwise
	 * @return This function
	 */
	/*@ non_null @*/ public Sort by(int index, boolean ascending)
	{
		m_conditions.add(new SortingCondition(index, ascending));
		return this;
	}
	
	/**
	 * Adds an ascending sorting condition to the current function instance.
	 * This is equivalent to the call {@code by(index, true)}.
	 * @param index The index of the column to be sorted
	 * @return This function
	 */
	/*@ non_null @*/ public Sort by(int index)
	{
		return by(index, true);
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet s = (Spreadsheet) inputs[0];
		int height = s.getHeight(), width = s.getWidth();
		List<SortableRow> sorted_rows = new ArrayList<SortableRow>(height);
		int first_index = (m_excludeFirst ? 1 : 0);
		for (int i = first_index; i < height; i++)
		{
			sorted_rows.add(new SortableRow(i, s.getRow(i)));
		}
		Collections.sort(sorted_rows);
		Spreadsheet out = new Spreadsheet(width, height);
		m_mapping = new InputCell[height][width][];
		if (first_index == 1)
		{
			for (int col = 0; col < width; col++)
			{
				out.set(col, 0, s.get(col, 0));
				m_mapping[0][col] = new InputCell[] {InputCell.get(col, 0)};
			}
		}
		for (int i = first_index; i < height; i++)
		{
			SortableRow s_row = sorted_rows.get(i - first_index);
			int original_row_index = s_row.getOriginalIndex();
			Object[] contents = s_row.getContents();
			for (int col = 0; col < width; col++)
			{
				out.set(col, i, contents[col]);
				m_mapping[i][col] = new InputCell[] {InputCell.get(col, original_row_index)};
			}
		}
		return new Object[] {out};
	}
	
	/**
	 * Specification of a column index to be sorted, associated with the
	 * direction (ascending or descending) for that sort.
	 */
	protected static class SortingCondition
	{
		/**
		 * The index of the column to be sorted.
		 */
		protected final int m_columnIndex;
		
		/**
		 * A flag specifying the order in which column entries should be sorted.
		 */
		protected final boolean m_ascending;
		
		/**
		 * Creates a new instance of sorting condition.
		 * @param index The index of the column to be sorted
		 * @param ascending A flag specifying the order in which column entries
		 * should be sorted
		 */
		public SortingCondition(int index, boolean ascending)
		{
			super();
			m_columnIndex = index;
			m_ascending = ascending;
		}
		
		/**
		 * Gets the index of the column to be sorted.
		 * @return The index
		 */
		/*@ pure @*/ public int getColumnIndex()
		{
			return m_columnIndex;
		}
		
		/**
		 * Gets whether the sort for the column is ascending.
		 * @return {@code true} if the sort is ascending, {@code false} otherwise
		 */
		/*@ pure @*/ public boolean isAscending()
		{
			return m_ascending;
		}
		
		@Override
		public String toString()
		{
			return m_columnIndex + " " + (m_ascending ? "+" : "-");
		}
	}
	
	/**
	 * An encapsulation of the contents of a spreadsheet row that implements the
	 * {@link Comparable} interface so that it can be involved in a sort
	 * operation.
	 */
	protected class SortableRow implements Comparable<SortableRow>
	{
		/**
		 * The position of the row in the original spreadsheet.
		 */
		protected int m_originalIndex;
		
		/**
		 * The contents of the row in the original spreadsheet.
		 */
		/*@ non_null @*/ protected Object[] m_contents;
		
		/**
		 * Creates a new sortable row.
		 * @param original_index The position of the row in the original
		 * spreadsheet
		 * @param contents The contents of the row in the original spreadsheet
		 */
		public SortableRow(int original_index, /*@ non_null @*/ Object[] contents)
		{
			super();
			m_originalIndex = original_index;
			m_contents = contents;
		}
		
		/**
		 * Gets the position of the row in the original spreadsheet.
		 * @return The index
		 */
		/*@ pure @*/ public int getOriginalIndex()
		{
			return m_originalIndex;
		}
		
		/**
		 * Gets the contents of the row in the original spreadsheet.
		 * @return The contents
		 */
		/*@ pure non_null @*/ public Object[] getContents()
		{
			return m_contents;
		}

		@Override
		public int compareTo(SortableRow other)
		{
			for (SortingCondition condition : m_conditions)
			{
				int index = condition.getColumnIndex();
				Object o1 = m_contents[index];
				Object o2 = other.getContents()[index];
				int v = Spreadsheet.compare(o1, o2);
				if (v != 0)
				{
					if (condition.isAscending())
					{
						return v;
					}
					return -v;
				}
			}
			return 0;
		}
	}
}

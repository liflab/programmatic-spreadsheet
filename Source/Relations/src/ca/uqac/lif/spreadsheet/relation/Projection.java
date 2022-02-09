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
package ca.uqac.lif.spreadsheet.relation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Calculates the projection of a relation, by retaining only specific
 * columns from the input. For example, given the spreadsheet:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr><td style="background:yellow">3</td><td>f</td><td style="background:yellow">true</td></tr>
 * <tr><td>1</td><td>o</td><td></td></tr>
 * <tr><td style="background:yellow">3</td><td>o</td><td style="background:yellow">true</td></tr>
 * <tr><td style="background:cyan">2</td><td>f</td><td style="background:cyan">false</td></tr>
 * <tr><td style="background:yellow">3</td><td>g</td><td style="background:yellow">true</td></tr>
 * </tbody>
 * </table>
 * <p>
 * The projection on columns A and C results in the following output:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>A</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr style="background:yellow"><td>3</td><td>true</td></tr>
 * <tr><td>1</td><td></td></tr>
 * <tr style="background:cyan"><td>2</td><td>false</td></tr>
 * </tbody>
 * </table>
 * <p>
 * As with other relational operators, projection removes duplicates rows from
 * the output, and also keeps track of the relationship between input and output
 * cells. In the example above:
 * <ul>
 * <li>cells of the third row of the output are mapped to the cells in the
 * corresponding row of the input (highlighted in cyan)</li> 
 * <li>cells of the second row of the output are mapped to the corresponding
 * cells in <em>all</em> the rows that are projected into this row (highlighted
 * in yellow)</li>
 * </ul>
 * <p>
 * The projection can also be used to flip the ordering of columns; hence
 * projecting on C and A produces:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>C</th><th>A</th></tr>
 * </thead>
 * <tbody>
 * <tr style="background:yellow"><td>true</td><td>3</td></tr>
 * <tr><td></td><td>1</td></tr>
 * <tr style="background:cyan"><td>false</td><td>2</td></tr>
 * </tbody>
 * </table>
 * <p>
 * In such a case, explanation follows each cell to its appropriate position in
 * the new column ordering.
 * 
 * @author Sylvain Hallé
 */
public class Projection extends RelationalOperator
{
	/**
	 * The names of the columns to retain in the output spreadsheet.
	 */
	/*@ non_null @*/ protected final String[] m_columnNames;

	/**
	 * The index of each column with given name in the last processed input
	 * spreadsheet.
	 */
	protected final int[] m_originalIndices;

	/**
	 * Creates a new instance of the function.
	 * @param sort_output Set to <tt>true</tt> to sort rows of the output,
	 * <tt>false</tt> otherwise
	 * @param col_names The names of the columns to retain in the output
	 * spreadsheet
	 */
	public Projection(boolean sort_output, String ... col_names)
	{
		super(1);
		m_sortOutput = sort_output;
		m_columnNames = col_names;
		m_originalIndices = new int[col_names.length];
	}

	/**
	 * Creates a new instance of the function, assuming unsorted rows in the
	 * output.
	 * @param col_names The names of the columns to retain in the output spreadsheet
	 */
	public Projection(String ... col_names)
	{
		this(false, col_names);
	}

	@Override
	protected Object[] getValue(Object... inputs)
	{
		m_mapping.clear();
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet s = (Spreadsheet) inputs[0];
		for (int i = 0; i < m_columnNames.length; i++)
		{
			m_originalIndices[i] = s.getColumnIndex(m_columnNames[i]);
			if (m_originalIndices[i] < 0)
			{
				throw new RelationalException("Attribute " + m_columnNames[i] + " does not exist in input");
			}
		}
		// We use both a set and a list to store rows. The hashset is used to check
		// for the existence of a duplicate row; this avoids doing a linear search
		// in the list in case the row has never been seen before.
		Set<Row> row_set = new HashSet<Row>();
		List<Row> row_list = new ArrayList<Row>();
		Object[] headers = null;
		for (int s_row = 0; s_row < s.getHeight(); s_row++)
		{
			Object[] contents = new Object[m_columnNames.length];
			for (int i = 0; i < contents.length; i++)
			{
				contents[i] = s.get(m_originalIndices[i], s_row);
			}
			if (s_row == 0)
			{
				headers = contents;
				continue;
			}
			Row r = new Row(contents);
			if (row_set.contains(r))
			{
				// Duplicate of existing row in the output
				int index = row_list.indexOf(r);
				List<Integer[]> tuples = m_mapping.get(index);
				tuples.add(new Integer[] {0, s_row});
			}
			else
			{
				// New unique row
				row_set.add(r);
				List<Integer[]> tuples = new ArrayList<Integer[]>(1);
				tuples.add(new Integer[] {0, s_row});
				m_mapping.add(tuples);
				row_list.add(r);
			}
		}
		return new Object[] {createOutput(headers, row_list)};
	}

	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		out.append("\u03c0 ["); // 03c0 = pi
		for (int i = 0; i < m_columnNames.length; i++)
		{
			if (i > 0)
			{
				out.append(",");
			}
			out.append(m_columnNames[i]);
		}
		out.append("]");
		return out.toString();
	}

	@Override
	protected LabelledNode getConnectorNode(NodeFactory f)
	{
		return f.getOrNode();
	}
	
	@Override
	protected int getColumnOf(int col)
	{
		return m_originalIndices[col];
	}
	
	@Override
	public Projection duplicate(boolean with_state)
	{
		Projection p = new Projection(m_sortOutput, m_columnNames);
		copyInto(p, with_state);
		if (with_state)
		{
			for (int i = 0; i < m_originalIndices.length; i++)
			{
				p.m_originalIndices[i] = m_originalIndices[i];
			}
		}
		return p;
	}
}

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
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Calculates the union of multiple relations. For example, given these two
 * spreadsheets:
 * <p>
 * <div style="display:flex">
 * <table border="1" style="background:#f44;margin-right:12pt">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>3</td><td>f</td><td>true</td></tr>
 * <tr><td>1</td><td>o</td><td></td></tr>
 * <tr><td></td><td>o</td><td>false</td></tr>
 * </tbody>
 * </table>
 * <table border="1" style="background:yellow">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>1</td><td>o</td><td></td></tr>
 * <tr><td>5</td><td>f</td><td>true</td></tr>
 * <tr><td></td><td>o</td><td>true</td></tr>
 * </tbody>
 * </table>
 * </div>
 * <p>
 * their union is the spreadsheet:
 * <p>
 * <table border="1">
 * <thead>
 * <tr style="background:#8c0"><th>A</th><th>B</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr style="background:#f44"><td>3</td><td>f</td><td>true</td></tr>
 * <tr style="background:#f44"><td>1</td><td>o</td><td></td></tr>
 * <tr style="background:#8c0"><td></td><td>o</td><td>false</td></tr>
 * <tr style="background:yellow"><td>5</td><td>f</td><td>true</td></tr>
 * <tr style="background:yellow"><td></td><td>o</td><td>true</td></tr>
 * </tbody>
 * </table>
 * <p>
 * In line with the definition of a relation, rows with identical values
 * appearing in more than one spreadsheet occur only once in the result.
 * However, contrary to the definition of a relation, the rows of the result
 * are listed in a specific order: all rows of the first argument of the
 * function are first appended (in their order of occurrence), followed by all
 * rows of the second argument that are not duplicates, and so on.
 * <p>
 * The operator also tracks the provenance of each cell of the output. In
 * the output spreadsheet:
 * <ul>
 * <li>cells in red rows are associated to corresponding cells of the first
 * input spreadsheet</li>
 * <li>cells in yellow rows are associated to corresponding cells of the
 * second input spreadsheet</li>
 * <li>cells in green rows appear in both spreadsheets and are associated
 * to corresponding cells of both</li>
 * </ul> 
 * @author Sylvain Hallé
 *
 */
public class Union extends RelationalOperator
{
	/**
	 * Creates a new instance of the Union function, assuming that the rows
	 * of its output will be unsorted.
	 * @param in_arity The input arity of the function
	 */
	public Union(int in_arity)
	{
		super(in_arity);
	}

	/**
	 * Creates a new instance of the Union function.
	 * @param sort_output Set to <tt>true</tt> to sort rows of the output,
	 * <tt>false</tt> otherwise 
	 * @param in_arity The input arity of the function
	 */
	public Union(boolean sort_output, int in_arity)
	{
		super(in_arity, sort_output);
	}

	@Override
	/*@ non_null @*/ public Union sortOutput(boolean b)
	{
		super.sortOutput(b);
		return this;
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		m_mapping.clear();
		Spreadsheet[] s_inputs = new Spreadsheet[inputs.length];
		for (int i = 0; i < getInputArity(); i++)
		{
			if (!(inputs[i] instanceof Spreadsheet))
			{
				throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
			}
			s_inputs[i] = (Spreadsheet) inputs[i];
			if (i > 0 && !sameSignature(s_inputs[i - 1], s_inputs[i]))
			{
				throw new RelationalException("Arguments have incompatible signatures");
			}
		}
		// We use both a set and a list to store rows. The hashset is used to check
		// for the existence of a duplicate row; this avoids doing a linear search
		// in the list in case the row has never been seen before.
		Set<Row> row_set = new HashSet<Row>();
		List<Row> row_list = new ArrayList<Row>();
		for (int s_index = 0; s_index < s_inputs.length; s_index++)
		{
			Spreadsheet s = s_inputs[s_index];
			for (int s_row = 1; s_row < s.getHeight(); s_row++)
			{
				Row r = new Row(s.getRow(s_row));
				if (row_set.contains(r))
				{
					// Duplicate of existing row in the output
					int index = row_list.indexOf(r);
					List<Integer[]> tuples = m_mapping.get(index);
					tuples.add(new Integer[] {s_index, s_row});
				}
				else
				{
					// New unique row
					row_set.add(r);
					List<Integer[]> tuples = new ArrayList<Integer[]>(1);
					tuples.add(new Integer[] {s_index, s_row});
					m_mapping.add(tuples);
					row_list.add(r);
				}
			}
		}
		return new Object[] {createOutput(s_inputs[0].getRow(0), row_list)};
	}

	@Override
	protected LabelledNode getConnectorNode(NodeFactory f)
	{
		return f.getOrNode();
	}

	@Override
	public String toString()
	{
		return "\u222a";
	}
	
	@Override
	public Union duplicate(boolean with_state)
	{
		Union u = new Union(m_sortOutput, getInputArity());
		copyInto(u, with_state);
		return u;
	}
}

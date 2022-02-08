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
import java.util.List;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Calculates the intersection of multiple relations. For example, given these
 * two spreadsheets:
 * <p>
 * <div style="display:flex">
 * <table border="1" style="margin-right:12pt">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>3</td><td>f</td><td>true</td></tr>
 * <tr style="background:cyan"><td></td><td>o</td><td>true</td></tr>
 * <tr style="background:yellow"><td>1</td><td>o</td><td></td></tr>
 * </tbody>
 * </table>
 * <table border="1">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr style="background:yellow"><td>1</td><td>o</td><td></td></tr>
 * <tr><td>5</td><td>f</td><td>true</td></tr>
 * <tr style="background:cyan"><td></td><td>o</td><td>true</td></tr>
 * </tbody>
 * </table>
 * </div>
 * <p>
 * their union is the spreadsheet:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr style="background:cyan"><td></td><td>o</td><td>true</td></tr>
 * <tr style="background:yellow"><td>1</td><td>o</td><td></td></tr>
 * </tbody>
 * </table>
 * <p>
 * In line with the definition of a relation, rows with identical values
 * appearing in more than one spreadsheet occur only once in the result.
 * However, contrary to the definition of a relation, the rows of the result
 * are listed in their order of occurrence in the first input spreadsheet.
 * <p>
 * The operator also tracks the provenance of each cell of the output. For
 * example:
 * <ul>
 * <li>in the output spreadsheet, cells in the second row are associated
 * to corresponding cells in both the second row of the first input
 * spreadsheet, and the fourth row of the second input spreadsheet (highlighted
 * in cyan)</li>
 * <li>in the output spreadsheet, cells in the third row are associated
 * to corresponding cells in both the fourth row of the first input
 * spreadsheet, and the second row of the yellow spreadsheet (highlighted in
 * yellow)</li>
 * </ul>
 *  
 * @author Sylvain Hallé
 *
 */
public class Intersection extends RelationalOperator
{
	/**
	 * Creates a new instance of the Union function, assuming that the rows
	 * of its output will be unsorted.
	 * @param in_arity The input arity of the function
	 */
	public Intersection(int in_arity)
	{
		super(in_arity);
	}
	
	/**
	 * Creates a new instance of the Intersection function.
	 * @param in_arity The input arity of the function
	 * @param sort_output Set to <tt>true</tt> to sort rows of the output,
	 * <tt>false</tt> otherwise 
	 */
	public Intersection(int in_arity, boolean sort_output)
	{
		super(in_arity, sort_output);
	}
	
	@Override
	/*@ non_null @*/ public Intersection sortOutput(boolean b)
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
				throw new InvalidArgumentTypeException("Arguments have incompatible signatures");
			}
		}
		List<Row> row_list = new ArrayList<Row>();
		for (int row = 1; row < s_inputs[0].getHeight(); row++)
		{
			Row r = new Row(s_inputs[0].getRow(row));
			List<Integer[]> tuples = new ArrayList<Integer[]>(s_inputs.length);
			tuples.add(new Integer[] {0, row});
			boolean all_present = true;
			for (int s_index = 1; s_index < s_inputs.length && all_present; s_index++)
			{
				int row_index = r.rowOf(s_inputs[s_index]);
				if (row_index < 0)
				{
					all_present = false;
					continue;
				}
				tuples.add(new Integer[] {s_index, row_index});
			}
			if (all_present)
			{
				row_list.add(r);
				m_mapping.add(tuples);
			}
		}
		return new Object[] {createOutput(s_inputs[0].getRow(0), row_list)};
	}
	
	@Override
	protected LabelledNode getConnectorNode(NodeFactory f)
	{
		return f.getAndNode();
	}
	
	@Override
	public String toString()
	{
		return "\u2229";
	}
}

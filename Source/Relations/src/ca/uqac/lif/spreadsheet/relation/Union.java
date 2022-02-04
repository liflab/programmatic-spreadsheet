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
import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
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
	 * A mapping associating each row of the output spreadsheet with the row(s)
	 * of the input spreadsheet(s) where it is found.
	 */
	/*@ non_null @*/ protected final List<List<Integer[]>> m_mapping;
	
	public Union(int in_arity)
	{
		super(in_arity);
		m_mapping = new ArrayList<List<Integer[]>>();
	}
	
	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
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
		Spreadsheet out = new Spreadsheet(s_inputs[0].getWidth(), row_set.size() + 1);
		for (int col = 0; col < s_inputs[0].getWidth(); col++)
		{
			// First row
			out.set(col, 0, s_inputs[0].get(col, 0));
		}
		for (int i = 0; i < row_list.size(); i++)
		{
			Row r = row_list.get(i);
			r.set(out, i + 1);
		}
		return new Object[] {out};
	}
	
	@Override
	public PartNode getExplanation(Part d, NodeFactory f)
	{
		Cell c = Cell.mentionedCell(d);
		if (c == null)
		{
			return super.getExplanation(d, f);
		}
		int c_row = c.getRow();
		int c_col = c.getColumn();
		PartNode root = f.getPartNode(d, this);
		if (c_row == 0)
		{
			// First row is made of labels
			LabelledNode to_add = root;
			int in_arity = getInputArity();
			if (in_arity > 1)
			{
				OrNode or = f.getOrNode();
				to_add.addChild(or);
				to_add = or;
			}
			for (int s_index = 0; s_index < in_arity; s_index++)
			{
				Part p = NthOutput.replaceOutByIn(d, s_index);
				to_add.addChild(f.getPartNode(p, this));
			}
			return root;
		}
		if (c_row < 1 || c_row > m_mapping.size())
		{
			root.addChild(f.getUnknownNode());
			return root;
		}
		List<Integer[]> positions = m_mapping.get(c_row - 1);
		LabelledNode to_add = root;
		if (positions.size() > 1)
		{
			OrNode or = f.getOrNode();
			to_add.addChild(or);
			to_add = or;
		}
		for (Integer[] pos : positions)
		{
			Part p = NthOutput.replaceOutByIn(d, pos[0]);
			p = Cell.replaceCellBy(p, Cell.get(c_col, pos[1]));
			to_add.addChild(f.getPartNode(p, this));
		}
		return root;
	}
	
	@Override
	public String toString()
	{
		return "\u222a";
	}
}

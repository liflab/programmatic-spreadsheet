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
import java.util.Collections;
import java.util.List;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Common ancestor to functions specific to relational algebra. These functions
 * may have a varying input arity, but their output arity is always 1.
 * <p>
 * A relational operator <strong>assumes</strong> that its input arguments are
 * spreadsheets with no duplicate rows. In turn, it <strong>guarantees</strong>
 * that the spreadsheet it produces as its output contains no duplicate rows.
 * @author Sylvain Hallé
 */
public abstract class RelationalOperator extends AtomicFunction
{
	/**
	 * A mapping associating each row of the output spreadsheet with the row(s)
	 * of the input spreadsheet(s) where it is found.
	 */
	/*@ non_null @*/ protected final List<List<Integer[]>> m_mapping;
	
	/**
	 * A flag indicating whether the rows of the output spreadsheet should be
	 * sorted.
	 */
	protected boolean m_sortOutput = false;
	
	/**
	 * Creates a new instance of the relational operator, assuming that the rows
	 * of its output will be unsorted.
	 * @param in_arity The input arity of the function
	 */
	public RelationalOperator(int in_arity)
	{
		this(in_arity, false);
	}
	
	/**
	 * Creates a new instance of the relational operator.
	 * @param in_arity The input arity of the function
	 * @param sort_output Set to <tt>true</tt> to sort rows of the output,
	 * <tt>false</tt> otherwise 
	 */
	public RelationalOperator(int in_arity, boolean sort_output)
	{
		super(in_arity, 1);
		m_mapping = new ArrayList<List<Integer[]>>();
		m_sortOutput = sort_output;
	}
	
	/**
	 * Sets whether the the rows of the output spreadsheet should be sorted.
	 * @param b Set to <tt>true</tt> to sort rows, <tt>false</tt> otherwise
	 * @return This relational operator
	 */
	/*@ non_null @*/ public RelationalOperator sortOutput(boolean b)
	{
		m_sortOutput = b;
		return this;
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
			LabelledNode conn = getConnectorNode(f);
			to_add.addChild(conn);
			to_add = conn;
		}
		for (Integer[] pos : positions)
		{
			Part p = NthOutput.replaceOutByIn(d, pos[0]);
			p = Cell.replaceCellBy(p, Cell.get(c_col, pos[1]));
			to_add.addChild(f.getPartNode(p, this));
		}
		return root;
	}
	
	/**
	 * Creates the output spreadsheet out of a list of rows gathered from the
	 * input spreadsheets. This method is called by {@link #getValue(Object...)}
	 * in each of the descendant classes.
	 * @param top_row The values of the top row, containing the names of each
	 * column of the output spreadsheet. The size of this array determines the
	 * width of the output spreadsheet.
	 * @param row_list The list of rows to be appended to the output spreadsheet.
	 * The size of this list determines the height of the output spreadsheet
	 * (which is size of list + 1).
	 * @return The output spreadsheet
	 */
	protected Spreadsheet createOutput(Object[] top_row, List<Row> row_list)
	{
		Spreadsheet out = new Spreadsheet(top_row.length, row_list.size() + 1);
		for (int col = 0; col < top_row.length; col++)
		{
			// First row
			out.set(col, 0, top_row[col]);
		}
		if (m_sortOutput)
		{
			Collections.sort(row_list);
		}
		for (int i = 0; i < row_list.size(); i++)
		{
			Row r = row_list.get(i);
			r.set(out, i + 1);
		}
		return out;
	}
	
	/**
	 * Creates a labelled node for an explanation involving more than one
	 * spreadsheet. What type of labelled node this corresponds to depends on the
	 * actual operator providing the explanation. For example, {@link Union}
	 * produces an {@link OrNode}, while {@link Intersection} produces an
	 * {@link AndNode}. Classes where this method is not used can simply return
	 * <tt>null</tt>.
	 * @param f The node factory used to obtain a node instance
	 * @return The node (or null)
	 */
	/*@ null @*/ protected abstract LabelledNode getConnectorNode(NodeFactory f);
	
	/**
	 * Checks if two spreadsheets, when interpreted as relations, have the same
	 * signature. For two spreadsheets to have the same signature, they must have
	 * the same width, their first row should be identical, and the type of each
	 * column should be compatible.
	 * @param s1 The first spreadsheet
	 * @param s2 The second spreadsheet
	 * @return <tt>true</tt> if the two spreadsheets have the same signature,
	 * <tt>false</tt> otherwise
	 */
	protected static boolean sameSignature(Spreadsheet s1, Spreadsheet s2)
	{
		if (s1.getWidth() != s2.getWidth())
		{
			return false;
		}
		for (int col = 0; col < s1.getWidth(); col++)
		{
			if (!Spreadsheet.same(s1.get(col, 0), s2.get(col, 0)))
			{
				return false;
			}
			Class<?> c1 = s1.getColumnType(col);
			Class<?> c2 = s2.getColumnType(col);
			if (!c1.isAssignableFrom(c2) && !c2.isAssignableFrom(c1))
			{
				return false;
			}
		}
		return true;
	}
}

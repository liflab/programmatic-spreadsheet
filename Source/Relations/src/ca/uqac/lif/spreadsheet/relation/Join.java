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

import java.util.List;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.Merge;

public class Join extends RelationalOperator
{
	/**
	 * The names of the columns on which to apply the join condition.
	 */
	protected final Object[] m_columnNames;
	
	/**
	 * The indices in the first spreadsheet of the columns on which to
	 * perform the join.
	 */
	protected int[] m_firstJoinIndices;
	
	/**
	 * The indices in the second spreadsheet of the columns on which to
	 * perform the join.
	 */
	protected int[] m_secondJoinIndices;
	
	/**
	 * Creates a new instance of the join operator.
	 * @param columns The names of the columns on which to apply the join
	 * condition
	 */
	public Join(boolean sort_output, Object ... columns)
	{
		super(2);
		m_sortOutput = sort_output;
		m_columnNames = columns;
	}
	
	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet) || !(inputs[1] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("One of the arguments is not a spreadsheet");
		}
		Spreadsheet s1 = (Spreadsheet) inputs[0];
		Spreadsheet s2 = (Spreadsheet) inputs[0];
		m_firstJoinIndices = NamedRow.getColumnIndices(s1, m_columnNames);
		m_secondJoinIndices = NamedRow.getColumnIndices(s2, m_columnNames);
		for (int row_1 = 1; row_1 < s1.getHeight(); row_1++)
		{
			NamedRow nr = new NamedRow(m_columnNames, m_firstJoinIndices, row_1, s1);
			List<Integer> indices = nr.indicesOf(s2, m_secondJoinIndices);
		}
		return null;
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		out.append("\u2a1d ["); // bowtie
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AtomicFunction duplicate(boolean with_state)
	{
		// TODO Auto-generated method stub
		return null;
	}
}

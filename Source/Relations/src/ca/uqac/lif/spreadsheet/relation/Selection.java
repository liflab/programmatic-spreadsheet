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
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;

public class Selection extends RelationalOperator
{
	/*@ non_null @*/ protected final Function m_condition;
	
	/**
	 * Creates a new instance of the selection function.
	 * @param sort_output Set to <tt>true</tt> to sort rows of the output,
	 * <tt>false</tt> otherwise
	 * @param condition The condition to evaluate on each row
	 */
	public Selection(boolean sort_output, Function condition)
	{
		super(1);
		m_sortOutput = sort_output;
		m_condition = condition;
	}
	
	/**
	 * Creates a new instance of the selection function, assuming unsorted
	 * rows in the output.
	 * @param condition The condition to evaluate on each row
	 */
	public Selection(Function condition)
	{
		this(false, condition);
	}
	
	@Override
	protected LabelledNode getConnectorNode(NodeFactory f)
	{
		return f.getOrNode();
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		m_mapping.clear();
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet s = (Spreadsheet) inputs[0];
		Object[] headers = s.getRow(0);
		List<Row> row_list = new ArrayList<Row>();
		for (int s_row = 1; s_row < s.getHeight(); s_row++)
		{
			Function condition = m_condition.duplicate();
			NamedRow r = new NamedRow(headers, s.getRow(s_row));
			Object o;
			if (condition.getInputArity() == 0)
			{
				// We consider the case where the function is a constant
				o = condition.evaluate()[0];
			}
			else
			{
				o = condition.evaluate(r)[0];
			}
			if (Boolean.TRUE.equals(o))
			{
				// Keep row
				row_list.add(r);
				List<Integer[]> tuples = new ArrayList<Integer[]>(1);
				tuples.add(new Integer[] {0, s_row});
				m_mapping.add(tuples);
			}
		}
		return new Object[] {createOutput(headers, row_list)};
	}
	
	@Override
	public String toString()
	{
		return "\u03c3 [" + m_condition + "]";
	}
	
	@Override
	public Selection duplicate(boolean with_state)
	{
		Selection sel = new Selection(m_sortOutput, m_condition.duplicate(with_state));
		copyInto(sel, with_state);
		return sel;
	}
}

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
import java.util.Map;

import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.petitpoucet.function.FunctionException;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

public class CellFormula
{
	/**
	 * The cell where the output of the formula is to be written.
	 */
	/*@ non_null @*/ protected final Cell m_target;
	
	/**
	 * The function to apply to the target cell.
	 */
	/*@ non_null @*/ protected final Function m_formula;
	
	/**
	 * A map associating spreadsheet indices to input arguments of the function.
	 */
	/*@ non_null @*/ protected final Map<Integer,Integer> m_arguments;
	
	/**
	 * Creates a new cell formula.
	 * @param target The cell where the output of the formula is to be written
	 * @param formula The function to apply to the target cell
	 */
	public CellFormula(/*@ non_null @*/ Cell target, /*@ non_null @*/ Function formula)
	{
		super();
		m_target = target;
		m_formula = formula;
		m_arguments = new HashMap<Integer,Integer>();
		m_arguments.put(0, 0);
	}
	
	/**
	 * Associates a spreadsheet index to an input argument of the underlying
	 * formula.
	 * @param spreadsheet The spreadsheet index
	 * @param index The argument index of the formula
	 * @return This cell formula object
	 */
	public CellFormula associate(int spreadsheet, int index)
	{
		m_arguments.put(index, spreadsheet);
		return this;
	}
	
	/*@ non_null @*/ public Cell getTarget()
	{
		return m_target;
	}
	
	public void evaluate(Spreadsheet ... args) throws FunctionException
	{
		m_formula.reset();
		Object[] in_args = new Spreadsheet[m_formula.getInputArity()];
		for (int i = 0; i < in_args.length; i++)
		{
			if (!m_arguments.containsKey(i))
			{
				throw new FunctionException("No input defined for argument " + i);
			}
			int index = m_arguments.get(i);
			in_args[i] = args[index];
		}
		Object o = m_formula.evaluate(in_args)[0];
		args[0].set(m_target.getColumn(), m_target.getRow(), o);
	}
	
}

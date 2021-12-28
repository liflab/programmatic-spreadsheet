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

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.FunctionException;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.SpreadsheetOutOfBoundsException;

/**
 * Function that extracts the value of a single cell within a spreadsheet.
 * @author Sylvain Hallé
 */
public class ValueOf extends AtomicFunction
{
	/**
	 * The cell to get the value of.
	 */
	/*@ non_null @*/ protected Cell m_cell;
	
	/**
	 * Creates a new instance of the function.
	 * @param c The cell to get the value of
	 */
	public ValueOf(/*@ non_null @*/ Cell c)
	{
		super(1, 1);
		m_cell = c;
	}
	
	/**
	 * Creates a new instance of the function.
	 * @param col The column for the cell
	 * @param row The row for the cell
	 */
	public ValueOf(int col, int row)
	{
		this(Cell.get(col, row));
	}

	@Override
	protected Object[] getValue(Object ... args) throws InvalidNumberOfArgumentsException
	{
		if (!(args[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Expected a spreadsheet");
		}
		Spreadsheet s = (Spreadsheet) args[0];
		try
		{
			return new Object[] {s.get(m_cell.getColumn(), m_cell.getRow())};
		}
		catch (SpreadsheetOutOfBoundsException e)
		{
			throw new FunctionException(e);
		}
	}
	
	@Override
	public PartNode getExplanation(Part part, NodeFactory factory)
	{
		PartNode root = factory.getPartNode(part, this);
		int index = NthOutput.mentionedOutput(part);
		if (index >= 0)
		{
			PartNode in = factory.getPartNode(NthOutput.replaceOutBy(part, ComposedPart.compose(m_cell, NthInput.FIRST)), this);
			root.addChild(in);
		}
		return root;
	}
	
	@Override
	public String toString()
	{
		return "Value of " + m_cell.toString();
	}
}

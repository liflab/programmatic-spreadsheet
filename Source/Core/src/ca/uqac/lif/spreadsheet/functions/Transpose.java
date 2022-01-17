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
package ca.uqac.lif.spreadsheet.functions;

import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Transposes a spreadsheet.
 * @author Sylvain Hallé
 */
public class Transpose extends SpreadsheetFunction
{
	public Transpose()
	{
		super(1);
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet s = (Spreadsheet) inputs[0];
		Spreadsheet out = new Spreadsheet(s.getHeight(), s.getWidth());
		m_mapping = new InputCell[s.getWidth()][s.getHeight()][];
		for (int row = 0; row < s.getHeight(); row++)
		{
			for (int col = 0; col < s.getWidth(); col++)
			{
				out.set(row, col, s.get(col, row));
				m_mapping[col][row] = new InputCell[] {InputCell.get(col, row)};
			}
		}
		return new Object[] {out};
	}
	
	@Override
	public String toString()
	{
		return "^T";
	}
}

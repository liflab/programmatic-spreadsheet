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

import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Computes the sum of each column.
 * @author Sylvain Hallé
 */
public class ColumnSum extends SpreadsheetFunction
{
	public ColumnSum()
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
		Spreadsheet out = new Spreadsheet(s.getWidth(), 2);
		double[] totals = new double[s.getWidth()];
		m_mapping = new InputCell[2][s.getWidth()][];
		for (int col = 0; col < totals.length; col++)
		{
			out.set(col, 0, s.get(col, 0));
			m_mapping[0][col] = new InputCell[] {InputCell.get(col, 0)};
			m_mapping[1][col] = new InputCell[s.getHeight() - 1];
		}
		for (int row = 1; row < s.getHeight(); row++)
		{
			for (int col = 0; col < totals.length; col++)
			{
				Double n = s.getNumerical(col, row);
				if (n != null)
				{
					totals[col] += n;
				}
				m_mapping[1][col][row - 1] = InputCell.get(col, row);
			}
		}
		for (int col = 0; col < totals.length; col++)
		{
			out.set(col, 1, totals[col]);
		}
		return new Object[] {out};
	}
}

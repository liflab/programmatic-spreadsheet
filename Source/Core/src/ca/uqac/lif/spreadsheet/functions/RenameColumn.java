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
 * Renames the first cell of a column.
 * @author Sylvain Hallé
 */
public class RenameColumn extends SpreadsheetFunction
{
	/**
	 * The original column name.
	 */
	protected final String m_from;
	
	/**
	 * The new column name.
	 */
	protected final String m_to;
	
	public RenameColumn(String from, String to)
	{
		super(1);
		m_from = from;
		m_to = to;
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet s = (Spreadsheet) inputs[0];
		int index = s.getColumnIndex(m_from);
		if (index < 0)
		{
			return new Object[] {s};
		}
		Spreadsheet new_s = s.duplicate(true);
		new_s.set(index, 0, m_to);
		return new Object[] {new_s};
	}
}

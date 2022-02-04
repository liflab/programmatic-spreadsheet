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

import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Common ancestor to functions specific to relational algebra. These functions
 * may have a varying input arity, but their output arity is always 1.
 * @author Sylvain Hallé
 */
public abstract class RelationalOperator extends AtomicFunction
{
	/**
	 * Creates a new instance of the relational operator.
	 * @param in_arity The input arity of the function
	 */
	public RelationalOperator(int in_arity)
	{
		super(in_arity, 1);
	}
	
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

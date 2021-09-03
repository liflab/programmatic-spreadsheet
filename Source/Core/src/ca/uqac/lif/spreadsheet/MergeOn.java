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
package ca.uqac.lif.spreadsheet;

import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;

public class MergeOn extends SpreadsheetFunction
{
	/**
	 * The indices of the columns to be considered for the merge.
	 */
	/*@ non_null @*/ protected int[] m_keyColumns;
	
	/**
	 * Creates a new instance of the function.
	 * @param arity The input arity
	 * @param columns The columns to be considered for the merge
	 */
	public MergeOn(int arity, /*@ non_null @*/ int ... columns)
	{
		super(arity);
	}
	
	@Override
	public MergeOn excludeFirst(boolean b)
	{
		return (MergeOn) super.excludeFirst(b);
	}
	
	@Override
	public MergeOn excludeFirst()
	{
		return (MergeOn) super.excludeFirst();
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		// TODO Auto-generated method stub
		return null;
	}
}

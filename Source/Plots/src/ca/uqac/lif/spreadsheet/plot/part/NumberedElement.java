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
package ca.uqac.lif.spreadsheet.plot.part;

import ca.uqac.lif.petitpoucet.function.vector.NthElement;
import ca.uqac.lif.spreadsheet.plot.Plot;

/**
 * Designates a plot element by its position in some ordering.
 */
public class NumberedElement extends NthElement
{
	/**
	 * Creates a new numbered element part.
	 * @param index The index of the element
	 */
	public NumberedElement(int index)
	{
		super(index);
	}
	
	@Override
	public boolean appliesTo(Object o)
	{
		return super.appliesTo(o) || o instanceof Plot;
	}
	
	@Override
	public String toString()
	{
		return "Element #" + getIndex();
	}
	
}

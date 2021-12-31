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

/**
 * Designates a plot element by its name.
 */
public class NamedElement extends PlotPart
{
	/**
	 * The name of the element.
	 */
	/*@ non_null @*/ private final String m_name;

	/**
	 * Creates a new named element part.
	 * @param name The name of the element
	 */
	public NamedElement(/*@ non_null @*/ String name)
	{
		super();
		m_name = name;
	}

	/**
	 * Gets the name of the element.
	 * @return The name of the element
	 */
	/*@ pure non_null @*/ public String getName()
	{
		return m_name;
	}

	@Override
	public String toString()
	{
		return "Element " + m_name;
	}
}
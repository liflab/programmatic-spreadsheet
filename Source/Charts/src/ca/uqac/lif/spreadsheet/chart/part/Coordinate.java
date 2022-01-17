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
package ca.uqac.lif.spreadsheet.chart.part;

import ca.uqac.lif.spreadsheet.chart.Chart.Axis;

/**
 * Part that refers to one of the coordinates of a point.
 * @author Sylvain Hallé
 */
public class Coordinate extends ChartPart
{
	/**
	 * The axis designated by the part.
	 */
	/*@ non_null @*/ private final Axis m_axis;
	
	/**
	 * Creates a new plot axis part.
	 * @param a The axis designated by the part
	 */
	public Coordinate(/*@ non_null @*/ Axis a)
	{
		super();
		m_axis = a;
	}
	
	/**
	 * Gets the axis designated by this part.
	 * @return The axis
	 */
	/*@ pure non_null @*/ public Axis getAxis()
	{
		return m_axis;
	}
	
	@Override
	public String toString()
	{
		return "Coordinate " + m_axis;
	}
}

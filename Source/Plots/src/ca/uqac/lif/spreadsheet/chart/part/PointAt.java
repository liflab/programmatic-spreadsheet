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

import ca.uqac.lif.spreadsheet.chart.Scatterplot;

/**
 * Part that refers to a point drawn at a specific (x,y) coordinate in a
 * two-dimensional plot. This part can only refer to a {@link Scatterplot}.
 * @author Sylvain Hallé
 */
public class PointAt extends ChartPart
{
	/**
	 * The x coordinate of the point.
	 */
	private final double m_x;
	
	/**
	 * The y coordinate of the point.
	 */
	private final double m_y;
	
	/**
	 * Creates a new instance of the part.
	 * @param x The x coordinate of the point
	 * @param y The y coordinate of the point
	 */
	public PointAt(double x, double y)
	{
		super();
		m_x = x;
		m_y = y;
	}
	
	/**
	 * Gets the x-coordinate of the point.
	 * @return The coordinate
	 */
	/*@ pure @*/ public double getX()
	{
		return m_x;
	}
	
	/**
	 * Gets the y-coordinate of the point.
	 * @return The coordinate
	 */
	/*@ pure @*/ public double getY()
	{
		return m_y;
	}
	
	@Override
	public String toString()
	{
		return "Point at (" + m_x + "," + m_y + ")";
	}
	
	@Override
	public boolean appliesTo(Object o)
	{
		return o instanceof Scatterplot;
	}
	
	@Override
	public int hashCode()
	{
		return (int) (m_x + m_y);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null | !(o instanceof PointAt))
		{
			return false;
		}
		PointAt p = (PointAt) o;
		return m_x == p.m_x && m_y == p.m_y;
	}
}

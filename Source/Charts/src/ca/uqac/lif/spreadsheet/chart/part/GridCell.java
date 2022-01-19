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
package ca.uqac.lif.spreadsheet.chart.part;

import ca.uqac.lif.spreadsheet.chart.HeatMap;

/**
 * Part that designates a cell in a {@link HeatMap}'s two-dimensional grid.
 * A grid cell can either be:
 * <ul>
 * <li>a {@link CardinalGridCell}, designated by the coordinates on the x and
 * y axis (i.e. the cell at (2.5, 6.75))</li>
 * <li>an {@link OrdinalGridCell}, whose location is expressed in number of
 * cells from the left and the bottom of the plot (i.e. the 3rd cell from the
 * left, 2nd from the bottom)</li> 
 * </ul>
 * @author Sylvain Hallé
 */
public abstract class GridCell extends ChartPart
{
	/**
	 * The x position of the cell.
	 */
	protected final double m_x;
	
	/**
	 * The x position of the cell.
	 */
	protected final double m_y;
	
	/**
	 * Creates a new grid cell.
	 * @param x The x position of the cell
	 * @param y The y position of the cell
	 */
	protected GridCell(double x, double y)
	{
		super();
		m_x = x;
		m_y = y;
	}
	
	/**
	 * Gets the x-coordinate of the grid cell.
	 * @return The coordinate
	 */
	/*@ pure @*/ public double getX()
	{
		return m_x;
	}
	
	/**
	 * Gets the y-coordinate of the grid cell.
	 * @return The coordinate
	 */
	/*@ pure @*/ public double getY()
	{
		return m_y;
	}
	
	@Override
	public boolean appliesTo(Object o)
	{
		return o instanceof HeatMap;
	}
	
	@Override
	public String toString()
	{
		return "Cell (" + m_x + "," + m_y + ")";
	}
	
	/**
	 * A {@link GridCell} that is designated by its coordinates on the x and y
	 * axis of the heatmap.
	 */
	public static class CardinalGridCell extends GridCell
	{
		/**
		 * Creates a new grid cell.
		 * @param x The x position of the cell
		 * @param y The y position of the cell
		 */
		public CardinalGridCell(double x, double y)
		{
			super(x, y);
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o == null || !(o instanceof CardinalGridCell))
			{
				return false;
			}
			CardinalGridCell c = (CardinalGridCell) o;
			return c.m_x == m_x && c.m_y ==  m_y;
		}
		
		@Override
		public int hashCode()
		{
			return (int) (m_x + m_y);
		}
	}
	
	/**
	 * A {@link GridCell} that is designated by its ordering from the
	 * left and the bottom of the heatmap.
	 */
	public static class OrdinalGridCell extends GridCell
	{
		/**
		 * Creates a new grid cell.
		 * @param x The x position of the cell
		 * @param y The y position of the cell
		 */
		public OrdinalGridCell(double x, double y)
		{
			super(x, y);
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (o == null || !(o instanceof OrdinalGridCell))
			{
				return false;
			}
			OrdinalGridCell c = (OrdinalGridCell) o;
			return c.m_x == m_x && c.m_y ==  m_y;
		}
		
		@Override
		public int hashCode()
		{
			return (int) (m_x + m_y);
		}
	}
}

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
package ca.uqac.lif.spreadsheet.chart;

/**
 * A two-dimensional plot obtained from a frequency table. In such a plot,
 * each cell of the original table corresponds to a rectangular region of the
 * plot, and the numerical value inside that cell is converted to a color used
 * to fill this rectangle.
 *   
 * @author Sylvain Hallé
 *
 */
public interface HeatMap extends Chart
{
	/**
	 * Sets the caption for the color scale of the heatmap.
	 * @param caption The caption
	 * @return This heatmap
	 */
	public HeatMap setScaleCaption(String caption);
}

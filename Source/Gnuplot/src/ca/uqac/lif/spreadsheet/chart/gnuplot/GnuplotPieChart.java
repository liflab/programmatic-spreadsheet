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
package ca.uqac.lif.spreadsheet.chart.gnuplot;

import java.io.PrintStream;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.ChartFormat;
import ca.uqac.lif.spreadsheet.chart.PieChart;

/**
 * GnuPlot implementation of a pie chart.
 * @author Sylvain Hallé
 */
public class GnuplotPieChart extends Gnuplot implements PieChart
{
	/**
	 * Creates a new instance of the pie chart.
	 */
	public GnuplotPieChart()
	{
		super();
	}

	@Override
	public void toGnuplot(PrintStream out, Spreadsheet table, ChartFormat term, String lab_title,
			boolean with_caption)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public GnuplotPieChart duplicate()
	{
		return new GnuplotPieChart();
	}
}

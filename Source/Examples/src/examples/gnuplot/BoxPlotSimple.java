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
package examples.gnuplot;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.BoxPlot;
import ca.uqac.lif.spreadsheet.chart.Chart.Axis;
import ca.uqac.lif.spreadsheet.chart.ChartFormat;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotBoxPlot;
import examples.util.GraphViewer.BitmapJFrame;

/**
 * Draws a box plot from a table of quartile data. 
 */
public class BoxPlotSimple
{

	public static void main(String[] args)
	{
		/* Create a spreadsheet from hard-coded values using the read method.*/
		Spreadsheet s = Spreadsheet.read(6, 4, 
				"x", "Min", "Q1", "Q2", "Q3", "Max",
				"A", -1,    3,     5,    8,    9, 
				"B", 3,     3,     7,    10,   12,
				"C", 2,     6,     10,   11,   12);
		
		/* Create a scatterplot with a title and a caption for the y -axis. */
		BoxPlot plot = new GnuplotBoxPlot()
				.setTitle("Apples and oranges").setCaption(Axis.Y, "Fruits");
		((GnuplotBoxPlot) plot).toGnuplot(System.out, s, ChartFormat.PDF, true);
		
		/* Display the scatterplot in a window. */
		new BitmapJFrame(plot, s).display();

	}

}

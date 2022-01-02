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
package examples.gnuplot;

import java.io.IOException;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.Plot.Axis;
import ca.uqac.lif.spreadsheet.plot.Scatterplot;
import ca.uqac.lif.spreadsheet.plots.gnuplot.GnuplotScatterplot;
import examples.util.GraphViewer.BitmapJFrame;

public class ScatterplotSimple
{
	public static void main(String[] args) throws IOException
	{
		/* Create a spreadsheet from hard-coded values using the read method.*/
		Spreadsheet s = Spreadsheet.read(3, 11, 
				"x", "Apples", "Oranges",
				0,   0,        3, 
				1,   1,        1,
				2,   2,        4,
				3,   3,        1,
				4,   4,        5,
				5,   5,        9,
				6,   6,        2,
				7,   7,        6,
				8,   8,        5,
				9,   9,        3);
		
		/* Create a scatterplot with a title and a caption for the y -axis. */
		Scatterplot plot = new GnuplotScatterplot()
				.setTitle("Apples and oranges").setCaption(Axis.Y, "Fruits");
		
		/* Display the scatterplot in a window. */
		new BitmapJFrame(plot, s).display();
	}
}

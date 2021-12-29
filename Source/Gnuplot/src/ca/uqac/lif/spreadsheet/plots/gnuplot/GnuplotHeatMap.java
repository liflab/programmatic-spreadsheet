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
package ca.uqac.lif.spreadsheet.plots.gnuplot;

import java.io.PrintStream;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.HeatMap;
import ca.uqac.lif.spreadsheet.plot.PlotFormat;

/**
 * Generates a Gnuplot file from a 2D heatmap.
 * @author Sylvain Hallé
 */
public class GnuplotHeatMap extends Gnuplot implements HeatMap
{
	/**
	 * Creates a new heat map associated to no table
	 */
	public GnuplotHeatMap()
	{
		super();
	}

	@Override
	public void toGnuplot(PrintStream out, Spreadsheet table, PlotFormat term, String lab_title, boolean with_caption) 
	{		
		// Create Gnuplot output file from that data
		printHeader(out, term, lab_title, with_caption);
		//out.append("set xrange [").append(ft.getMinX() - ft.getXWidth() / 2).append(":").append(ft.getMaxX() - ft.getXWidth() / 2).append("]\n");
		//out.append("set yrange [").append(ft.getMinY() - ft.getYWidth() / 2).append(":").append(ft.getMaxY() - ft.getYWidth() / 2).append("]\n");
		out.println("set tic scale 0");
		out.println("unset cbtics");
		out.println("$map1 << EOD");
		//double[][] values = ft.getArray();
		Double[] scale_x = table.getRowNumerical(0);
		Double[] scale_y = table.getColumnNumerical(0);
		for (int j = 0; j < scale_x.length; j++)
		{
			out.print("," + scale_x[j]);
		}
		out.println();
		for (int i = 1; i < table.getHeight(); i++)
		{
			out.print(scale_y[i]);
			for (int j = 1; j < table.getWidth(); j++)
			{
				out.print("," + table.get(j, i));
			}
			out.println();
		}
		out.println("EOD");
		out.println();
		out.println("set view map");
		out.println("plot '$map1' matrix rowheaders columnheaders using 1:2:3 with image");
	}

	@Override
	public GnuplotHeatMap duplicate()
	{
		GnuplotHeatMap hm = new GnuplotHeatMap();
		copyInto(hm);
		return hm;
	}
}

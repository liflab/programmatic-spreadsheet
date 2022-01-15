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

import static ca.uqac.lif.spreadsheet.functions.GetFrequencies.createPair;

import java.util.Arrays;
import java.util.List;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.GetFrequencies;
import ca.uqac.lif.spreadsheet.chart.HeatMap;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotHeatMap;
import examples.util.GraphViewer.BitmapJFrame;

public class HeatmapSimple
{
	public static void main(String[] args)
	{
		GetFrequencies f = new GetFrequencies(0, 12, 4, 0, 6, 3);
		List<Double[]> list = Arrays.asList(
				createPair(1, 1),
				createPair(3, 5),
				createPair(2, 1),
				createPair(7, 3)
				);
		Spreadsheet s = (Spreadsheet) f.evaluate(list)[0];
		HeatMap plot = new GnuplotHeatMap().setTitle("A simple heat map").setScaleCaption("Score");
		BitmapJFrame window = new BitmapJFrame(plot, s);
		window.setVisible(true);
	}

}

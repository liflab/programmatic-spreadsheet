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

import static ca.uqac.lif.spreadsheet.functions.GetFrequencies.createPair;

import java.util.Arrays;
import java.util.List;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.GetFrequencies;
import ca.uqac.lif.spreadsheet.chart.HeatMap;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotHeatMap;
import examples.util.GraphViewer.BitmapJFrame;

/**
 * Creates and displays a heat map based on a list of pairs of numbers.
 * <p>
 * The program starts by creating the following list of pairs of
 * <tt>double</tt>: [(1,1), (3,5), (2,1), (7,3)]. The interval for the first
 * coordinate is set to [0,12] and is split into 4 regions. The interval for
 * the second coordinate is set to [0,6] and is split into 3 regions. Picking a
 * region for the first and second coordinate delineates a "cell". The goal is
 * to count how many pairs of the input list fall into each cell.
 * <p>
 * This is done with an instance of the {@link GetFrequencies} function, which
 * is evaluated on the list of pairs above. It produces the following
 * spreadsheet:
 * <p>
 * <table border="1">
 * <tr><th></th><th>0</th><th>3</th><th>6</th><th>9</th></tr>
 * <tr><th>0</th><td style="background:yellow">2</td><td>0</td><td>0</td><td>0</td></tr>
 * <tr><th>2</th><td>0</td><td>0</td><td style="background:lightgreen">1</td><td>0</td></tr>
 * <tr><th>4</th><td>0</td><td>1</td><td>0</td><td>0</td></tr>
 * </table>
 * <p>
 * Indeed, we can observe for example that two pairs of the list have their
 * first value in the interval [0,3) and their second value in the interval
 * [0,2) (yellow cell); one pair has its first value in the interval [6,9) and
 * its second value in the interval [2,4) (green cell); and so on.
 * <p>
 * Once in possession of this spreadsheet, it is possible to display its
 * contents as a two-dimensional plot using the {@link HeatMap} chart class.
 * This produces the following picture:
 * <p>
 * <img src="{@docRoot}/doc-files/gnuplot/HeatmapSimple-window.png" alt="Window" />
 * <p>
 * Notice how the vertical axis is reversed with respect to the contents of the
 * spreadsheet.
 * 
 * @see HeatmapExplanation
 */
public class HeatmapSimple
{
	public static void main(String[] args)
	{
		/* Create a list of pairs of numbers */
		List<Double[]> list = Arrays.asList(
				createPair(1, 1),
				createPair(3, 5),
				createPair(2, 1),
				createPair(7, 3)
				);
		
		/* Setup a frequency table with the interval [0,12] split in 4 regions
		 * horizontally, and the interval [0,6] split in 3 regions vertically. */
		GetFrequencies f = new GetFrequencies(0, 12, 4, 0, 6, 3);
		
		/* Create a frequency table out of the list of pairs by evaluating the
		 * GetFrequencies function on the list of pairs. */
		Spreadsheet s = (Spreadsheet) f.evaluate(list)[0];
		System.out.println(s);
		
		/* Display this table as a heat map. */
		HeatMap plot = new GnuplotHeatMap().setTitle("A simple heat map").setScaleCaption("Score");
		BitmapJFrame window = new BitmapJFrame(plot, s);
		window.setVisible(true);
	}

}

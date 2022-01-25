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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ca.uqac.lif.dag.NodeConnector;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.functions.GetFrequencies;
import ca.uqac.lif.spreadsheet.chart.DrawChart;
import ca.uqac.lif.spreadsheet.chart.part.GridCell.*;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotHeatMap;
import examples.util.GraphViewer;
import examples.util.GraphViewer.BitmapJFrame;

/**
 * Creates a heat map and answers a provenance query about an element of the
 * resulting chart.
 * <p>
 * This program is a follow-up to {@link HeatmapSimple}. It creates the same
 * heatmap, but uses the {@link DrawChart} function to obtain the picture:
 * <p>
 * <img src="{@docRoot}/doc-files/gnuplot/HeatmapSimple-window.png" alt="Window" />
 * <p>
 * The advantage is that this function can then be queried for an explanation
 * of plot elements in this picture. In the present case, we ask for the
 * explanation of the square containing coordinate (0,0) inside the heatmap;
 * the {@link CardinalGridCell} part is used to refer to this square. This
 * results in the following graph:
 * <p>
 * <img src="{@docRoot}/doc-files/gnuplot/HeatmapExplanation-exp.png" alt="Explanation graph" />
 * <p>
 * One can see that the explanation for this square in the picture is twofold:
 * it points both to the first (<tt>[0]</tt>) and the third (<tt>[2]</tt>)
 * elements of the input list. This corresponds to the pairs (1,1) and (2,1),
 * which are indeed the only two pairs that fall into this square in the
 * plot, and "explain" its color.
 * 
 * @see HeatmapSimple
 */
public class HeatmapExplanation
{
	public static void main(String[] args) throws IOException
	{
		/* Create the circuit that reads a file and draws a plot. */
		Circuit c = new Circuit(1, 1, "Read and draw");
		{
			// The ReadSpreadsheet function reads from a text source
			GetFrequencies f = new GetFrequencies(0, 12, 4, 0, 6, 3);
			
			// The DrawChart function is instructed to pass the resulting
			// spreadsheet to a heatmap and produce an image with it
			DrawChart d = new DrawChart(new GnuplotHeatMap()
					.setTitle("My heatmap"));
			
			// Connect these functions together and with the circuit
			NodeConnector.connect(f, 0, d, 0);
			c.associateInput(0, f.getInputPin(0));
			c.associateOutput(0, d.getOutputPin(0));
		}
		
		/* Evaluate the function by passing a list of number pairs and display the image. */
		List<Double[]> list = Arrays.asList(
				createPair(1, 1),
				createPair(3, 5),
				createPair(2, 1),
				createPair(7, 3));
		byte[] picture = (byte[]) c.evaluate(list)[0];
		new BitmapJFrame(picture).display();
		
		/* Ask the provenance of a part of the plot and display it. */
		Part part = ComposedPart.compose(new CardinalGridCell(0, 0), NthOutput.FIRST);
		PartNode graph = c.getExplanation(part);
		GraphViewer.display(graph);
	}
}

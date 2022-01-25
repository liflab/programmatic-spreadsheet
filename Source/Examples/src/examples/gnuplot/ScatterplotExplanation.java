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
import java.util.Scanner;

import ca.uqac.lif.dag.NodeConnector;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.functions.ReadSpreadsheet;
import ca.uqac.lif.spreadsheet.chart.DrawChart;
import ca.uqac.lif.spreadsheet.chart.Chart.Axis;
import ca.uqac.lif.spreadsheet.chart.part.ChartPart;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotScatterplot;
import examples.util.GraphViewer;
import examples.util.GraphViewer.BitmapJFrame;

/**
 * Displays a scatterplot from a spreadsheet extracted from a file, and answers
 * a provenance query on the contents of the plot.
 * <p>
 * The scatterplot is identical to that of {@link ScatterplotSimple}, except
 * that it is parsed from a text file instead of being hard-coded. Thus, the
 * following picture is displayed:
 * <p>
 * <img src="{@docRoot}/doc-files/gnuplot/ScatterplotSimple-window.png" alt="Plot" />
 * <p>
 * The program then asks an explanation for the legend of this plot (top-right
 * corner); this is done using the {@link ChartPart.Legend} part. 
 * <p>
 * <img src="{@docRoot}/doc-files/gnuplot/ScatterplotExplanation-exp.png" alt="Explanation graph" />
 * <p>
 * As one can see by examining the leaves of the graph, the plot's legend can
 * be traced to two character ranges inside the input text file:
 * <ul>
 * <li>Line 1, characters 11-16: the part of the first input line corresponding
 * to the word "Apples"</li>
 * <li>Line 1, characters 18-24: the part of the first input line corresponding
 * to the word "Oranges"</li>
 * </ul>
 * @see ScatterplotSimple
 */
public class ScatterplotExplanation
{
	public static void main(String[] args) throws IOException
	{
		/* Create the circuit that reads a file and draws a plot. */
		Circuit c = new Circuit(1, 1, "Read and draw");
		{
			// The ReadSpreadsheet function reads from a text source
			ReadSpreadsheet r = new ReadSpreadsheet();
			
			// The DrawChart function is instructed to pass the resulting
			// spreadsheet to a scatterplot and produce an image with it
			DrawChart d = new DrawChart(new GnuplotScatterplot()
					.setTitle("Apples and oranges").setCaption(Axis.Y, "Fruits"));
			
			// Connect these functions together and with the circuit
			NodeConnector.connect(r, 0, d, 0);
			c.associateInput(0, r.getInputPin(0));
			c.associateOutput(0, d.getOutputPin(0));
		}
		
		/* Evaluate the function from a scanner and display the image. */
		Scanner source = new Scanner(ScatterplotExplanation.class.getResourceAsStream("scatterplot1.csv"));
		byte[] picture = (byte[]) c.evaluate(source)[0];
		new BitmapJFrame(picture).display();
		
		/* Ask the provenance of a part of the plot and display it. */
		Part part = ComposedPart.compose(ChartPart.legend, NthOutput.FIRST);
		PartNode graph = c.getExplanation(part);
		GraphViewer.display(graph);
	}
}

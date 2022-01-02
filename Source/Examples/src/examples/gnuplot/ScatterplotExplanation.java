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
import ca.uqac.lif.spreadsheet.plot.DrawPlot;
import ca.uqac.lif.spreadsheet.plot.Plot.Axis;
import ca.uqac.lif.spreadsheet.plot.part.PlotPart;
import ca.uqac.lif.spreadsheet.plots.gnuplot.GnuplotScatterplot;
import examples.util.GraphViewer;
import examples.util.GraphViewer.BitmapJFrame;

public class ScatterplotExplanation
{
	public static void main(String[] args) throws IOException
	{
		/* Create the circuit that reads a file and draws a plot. */
		Circuit c = new Circuit(1, 1, "Read and draw");
		{
			// The ReadSpreadsheet function reads from a text source
			ReadSpreadsheet r = new ReadSpreadsheet();
			
			// The DrawPlot function is instructed to pass the resulting
			// spreadsheet to a scatterplot and produce an image with it
			DrawPlot d = new DrawPlot(new GnuplotScatterplot()
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
		Part part = ComposedPart.compose(PlotPart.legend, NthOutput.FIRST);
		PartNode graph = c.getExplanation(part);
		GraphViewer.display(graph);
	}
}

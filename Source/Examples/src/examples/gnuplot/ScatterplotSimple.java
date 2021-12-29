package examples.gnuplot;

import java.io.IOException;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.Scatterplot;
import ca.uqac.lif.spreadsheet.plots.gnuplot.Gnuplot;
import ca.uqac.lif.spreadsheet.plots.gnuplot.GnuplotScatterplot;

public class ScatterplotSimple
{
	public static void main(String[] args) throws IOException
	{
		Spreadsheet s = Spreadsheet.read(3, 11, 
				"x", "Apples", "Oranges",
				0,   5,        7, 
				1,   5,        7,
				2,   5,        7,
				3,   5,        7,
				4,   5,        7,
				5,   5,        7,
				6,   5,        7,
				7,   5,        7,
				8,   5,        7,
				9,   5,        7);
		Scatterplot plot = new GnuplotScatterplot().setTitle("Apples and oranges");
		plot.render(System.out, s);
	}
}

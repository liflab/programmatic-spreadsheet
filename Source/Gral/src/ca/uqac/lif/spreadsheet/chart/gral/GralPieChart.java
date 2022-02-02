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
package ca.uqac.lif.spreadsheet.chart.gral;

import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.PieChart;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.plots.PiePlot;
import de.erichseifert.gral.plots.Plot;

/**
 * GnuPlot implementation of a pie chart.
 * @author Sylvain Hallé
 */
public class GralPieChart extends GralPlot implements PieChart
{
	/**
	 * Creates a new instance of the pie chart.
	 */
	public GralPieChart()
	{
		super();
	}
	
	@Override
	public Plot getPlot(Spreadsheet source)
	{
		SpreadsheetDataSource gdt = new SpreadsheetDataSource(source);
		PiePlot plot = new PiePlot(gdt);
		plot.setInsets(new Insets2D.Double(20d, 60d, 60d, 40d));
		plot.getTitle().setText(getTitle());
		plot.setLegendVisible(true);
		customize(plot);
		return plot;
	}

	@Override
	public GralPieChart duplicate()
	{
		GralPieChart g = new GralPieChart();
		copyInto(g);
		return g;
	}

	@Override
	public PartNode getExplanation(Part part, NodeFactory factory)
	{
		// TODO Auto-generated method stub
		return null;
	}

}

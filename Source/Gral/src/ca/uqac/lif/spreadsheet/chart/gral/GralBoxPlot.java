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
import ca.uqac.lif.spreadsheet.chart.BoxPlot;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.plots.Plot;
import de.erichseifert.gral.plots.XYPlot;

/**
 * GRAL implementation of a boxplot.
 * @author Sylvain Hallé
 */
public class GralBoxPlot extends GralPlot implements BoxPlot
{
	/**
	 * Creates a new GRAL box plot.
	 */
	public GralBoxPlot()
	{
		super();
	}
		
	@Override
	public Plot getPlot(Spreadsheet source)
	{
		SpreadsheetDataSource gdt = new SpreadsheetDataSource(source);
		DataSource box_source = de.erichseifert.gral.plots.BoxPlot.createBoxData(gdt);
		de.erichseifert.gral.plots.BoxPlot plot = new de.erichseifert.gral.plots.BoxPlot(box_source);
		plot.setInsets(new Insets2D.Double(20d, 60d, 60d, 40d));
		plot.getTitle().setText(getTitle());
		if (!m_captionX.isEmpty())
		{
			plot.getAxisRenderer(XYPlot.AXIS_X).getLabel().setText(m_captionX);
		}
		else
		{
			plot.getAxisRenderer(XYPlot.AXIS_X).getLabel().setText(source.getString(0, 0));
		}
		plot.getAxisRenderer(XYPlot.AXIS_Y).getLabel().setText(m_captionY);
		customize(plot);
		return plot;
	}
	
	@Override
	public GralBoxPlot setTitle(String title)
	{
		super.setTitle(title);
		return this;
	}
	
	@Override
	public GralBoxPlot setCaption(Axis a, String caption)
	{
		super.setCaption(a, caption);
		return this;
	}

	@Override
	public GralBoxPlot duplicate()
	{
		GralBoxPlot g = new GralBoxPlot();
		copyInto(g);
		return g;
	}

	@Override
	public PartNode getExplanation(Part part, RelationNodeFactory factory)
	{
		// TODO Auto-generated method stub
		return null;
	}
}

/*
    A provenance-aware spreadsheet library
    Copyright (C) 2021-2023 Sylvain Hallé

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

import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.Scatterplot;
import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.plots.Plot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LogarithmicRenderer2D;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;

/**
 * Scatterplot with default settings. Given a table, this class will draw
 * an x-y scatterplot with the first column as the values for the "x" axis,
 * and every remaining column as a distinct data series plotted on the "y"
 * axis.
 *   
 * @author Sylvain Hallé
 */
public class GralScatterplot extends GralPlot implements Scatterplot
{	
	/**
	 * Whether to draw each data series with lines between each data point
	 */
	protected boolean m_withLines = true;

	/**
	 * Whether to draw each data series with marks for each data point
	 */
	protected boolean m_withPoints = true;
	
	/**
	 * Whether the first line of the spreadsheet is a header
	 */
	protected boolean m_hasHeaders = false;

	/**
	 * Creates an empty scatterplot with default settings
	 */
	public GralScatterplot()
	{
		super();
	}

	@Override
	public GralScatterplot withLines(boolean b)
	{
		m_withLines = b;
		return this;
	}

	@Override
	public GralScatterplot withLines()
	{
		m_withLines = true;
		return this;
	}

	@Override
	public GralScatterplot withPoints(boolean b)
	{
		m_withPoints = b;
		return this;
	}

	@Override
	public GralScatterplot withPoints()
	{
		m_withPoints = true;
		return this;
	}
	
	@Override
	public GralScatterplot hasHeaders(boolean b)
	{
		m_hasHeaders = b;
		return this;
	}
	
	@Override
	public GralScatterplot hasHeaders()
	{
		return hasHeaders(true);
	}
	
	@Override
	public GralScatterplot setTitle(String title)
	{
		super.setTitle(title);
		return this;
	}
	
	@Override
	public GralScatterplot setCaption(Axis a, String caption)
	{
		super.setCaption(a, caption);
		return this;
	}

	@Override
	public Plot getPlot(Spreadsheet source)
	{
		SpreadsheetDataSource sds = new SpreadsheetDataSource(source);
		int num_cols = source.getWidth();
		DataSeries[] series = new DataSeries[num_cols - 1];
		String col_0 = source.getString(0, 0);
		for (int col = 1; col < num_cols; col++)
		{
			series[col - 1] = sds.getCleanedDataSeries(0, col);
		}
		XYPlot plot = new XYPlot(series);
		for (int col = 1; col < num_cols; col++)
		{
			if (m_withPoints)
			{
				PointRenderer pr = new DefaultPointRenderer2D();
				plot.setPointRenderers(series[col - 1], pr);
				for (PointRenderer r : plot.getPointRenderers(series[col - 1]))
				{
					r.setColor(m_palette.getPaint(col - 1));
				}
			}
			if (m_withLines)
			{
				LineRenderer lr = new DefaultLineRenderer2D();
				plot.setLineRenderers(series[col - 1], lr);
				for (LineRenderer r : plot.getLineRenderers(series[col - 1]))
				{
					r.setColor(m_palette.getPaint(col - 1));
				}
			}
		}
		plot.setInsets(new Insets2D.Double(20d, 60d, 60d, 40d));
		plot.getTitle().setText(getTitle());
		if (series.length > 1)
		{
			// Put legend only if more than one data series
			plot.setLegendVisible(true);
		}
		if (m_logScaleX)
		{
			AxisRenderer rendererX = new LogarithmicRenderer2D();
			plot.setAxisRenderer(XYPlot.AXIS_X, rendererX);
		}
		if (m_logScaleY)
		{
			AxisRenderer rendererY = new LogarithmicRenderer2D();
			plot.setAxisRenderer(XYPlot.AXIS_Y, rendererY);
		}
		if (!m_captionX.isEmpty())
		{
			plot.getAxisRenderer(XYPlot.AXIS_X).getLabel().setText(m_captionX);
		}
		else
		{
			plot.getAxisRenderer(XYPlot.AXIS_X).getLabel().setText(col_0);
		}
		plot.getAxisRenderer(XYPlot.AXIS_Y).getLabel().setText(m_captionY);
		customize(plot);
		return plot;
	}

	@Override
	public GralScatterplot duplicate()
	{
		GralScatterplot g = new GralScatterplot();
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

/*
  MTNP: Manipulate Tables N'Plots
  Copyright (C) 2017 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.spreadsheet.plots.gnuplot;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.PlotFormat;
import ca.uqac.lif.spreadsheet.plot.Scatterplot;
import ca.uqac.lif.spreadsheet.plot.part.PlotAxis;
import ca.uqac.lif.spreadsheet.plot.part.PlotPart.Caption;
import ca.uqac.lif.spreadsheet.plot.part.PointAt;

/**
 * Scatterplot with default settings. Given a table, this class will draw
 * an x-y scatterplot with the first column as the values for the "x" axis,
 * and every remaining column as a distinct data series plotted on the "y"
 * axis.
 *   
 * @author Sylvain Hallé
 */
public class GnuplotScatterplot extends Gnuplot implements Scatterplot
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
	 * Creates an empty scatterplot
	 */
	public GnuplotScatterplot()
	{
		super();
	}

	@Override
	public GnuplotScatterplot withLines()
	{
		return withLines(true);
	}

	@Override
	public GnuplotScatterplot withPoints()
	{
		return withPoints(true);
	}

	@Override
	public GnuplotScatterplot withLines(boolean b)
	{
		m_withLines = b;
		return this;
	}

	@Override
	public GnuplotScatterplot withPoints(boolean b)
	{
		m_withPoints = b;
		return this;
	}

	@Override
	public GnuplotScatterplot setTitle(String title)
	{
		super.setTitle(title);
		return this;
	}

	@Override
	public GnuplotScatterplot setCaption(Axis a, String caption)
	{
		super.setCaption(a, caption);
		return this;
	}

	@Override
	public GnuplotScatterplot setFormat(PlotFormat f)
	{
		super.setFormat(f);
		return this;
	}

	@Override
	public void toGnuplot(PrintStream out, Spreadsheet table, PlotFormat term, String lab_title, boolean with_caption)
	{
		String[] columns = table.getColumnNames();
		String caption_x = m_captionX;
		if (caption_x.isEmpty())
		{
			caption_x = columns[0];
		}
		Vector<String> series = new Vector<String>();
		for (int i = 1; i < columns.length; i++)
		{
			series.add(columns[i]);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream p_baos = new PrintStream(baos);
		s_printer.print(table, p_baos);
		String csv_values = baos.toString();
		String point_string = " with points";
		if (m_withLines)
		{
			if (m_withPoints)
			{
				point_string = " with linespoints";
			}
			else
			{
				point_string = " with lines";
			}
		}
		// Build GP string from table
		printHeader(out, term, lab_title, with_caption);
		if (m_logScaleX)
		{
			out.append("set logscale x").append("\n");
		}
		if (m_logScaleY)
		{
			out.append("set logscale y").append("\n");
		}
		out.append("set xlabel \"").append(caption_x).append("\"\n");
		out.append("set ylabel \"").append(m_captionY).append("\"\n");
		if (!hasKey() || series.size() <= 1)
		{
			out.append("set key off\n");
		}
		out.append("plot");
		for (int i = 0; i < series.size(); i++)
		{
			if (i > 0)
			{
				out.append(", ");
			}
			String s_name = series.get(i);
			out.append(" '-' using 1:").append(i + 2 + "").append(" title '").append(s_name).append("'").append(point_string);
		}
		out.println();
		// In Gnuplot, if we use the special "-" filename, we must repeat
		// the data as many times as we use it in the plot command; it does not remember it
		for (int i = 0; i < series.size(); i++)
		{
			out.append(csv_values).append("end\n");
		}
	}

	protected void copyInto(GnuplotScatterplot sp)
	{
		super.copyInto(sp);
		sp.m_withLines = m_withLines;
		sp.m_withPoints = m_withPoints;
	}

	@Override
	public GnuplotScatterplot duplicate()
	{
		GnuplotScatterplot sp = new GnuplotScatterplot();
		copyInto(sp);
		return sp;
	}

	@Override
	protected void explainPlotPart(Part to_explain, Part suffix, PartNode root, NodeFactory f)
	{
		Part head = to_explain.head();
		if (head instanceof PlotAxis)
		{
			explainAxis((PlotAxis) head, to_explain.tail(), suffix, root, f);
		}
		else if (head instanceof PointAt)
		{
			explainPointAt((PointAt) head, to_explain.tail(), suffix, root, f);
		}
		else
		{
			root.addChild(f.getUnknownNode());
		}
	}

	protected void explainPointAt(PointAt p, Part to_explain, Part suffix, PartNode root, NodeFactory f)
	{
		double x = p.getX(), y = p.getY();
		List<Cell[]> pairs = new ArrayList<Cell[]>();
		int width = m_lastSpreadsheet.getWidth();
		boolean found_x = false;
		for (int i = 1; i < m_lastSpreadsheet.getHeight() && !found_x; i++)
		{
			Object s_x = m_lastSpreadsheet.get(0, i);
			if (!(s_x instanceof Number) || ((Number) s_x).doubleValue() != x)
			{
				continue;
			}
			found_x = true;
			for (int j = 1; j < width; j++)
			{
				Object s_y = m_lastSpreadsheet.get(j, i);
				if (!(s_y instanceof Number) || ((Number) s_y).doubleValue() != y)
				{
					continue;
				}
				// Found a candidate
				pairs.add(new Cell[] {Cell.get(0, i), Cell.get(j, i)});
			}
		}
		if (pairs.isEmpty())
		{
			root.addChild(f.getPartNode(Part.nothing, m_lastSpreadsheet));
		}
		else
		{
			LabelledNode to_add = root;
			if (pairs.size() > 1)
			{
				OrNode on = f.getOrNode();
				to_add.addChild(on);
				to_add = on;
			}
			for (Cell[] pair : pairs)
			{
				AndNode and = f.getAndNode();
				and.addChild(f.getPartNode(ComposedPart.compose(pair[0], Part.self), m_lastSpreadsheet));
				and.addChild(f.getPartNode(ComposedPart.compose(pair[1], Part.self), m_lastSpreadsheet));
				to_add.addChild(and);
			}
		}
	}

	protected void explainAxis(PlotAxis pa, Part to_explain, Part suffix, PartNode root, NodeFactory f)
	{
		Axis a = pa.getAxis();
		if (a == Axis.Z)
		{
			// There is no z-axis in a 2D plot
			root.addChild(f.getUnknownNode());
			return;
		}
		if (to_explain == null)
		{
			if (a == Axis.X)
			{
				// Explain the whole x-axis: first column of input spreadsheet
				int height = m_lastSpreadsheet.getHeight();
				if (height == 2)
				{
					PartNode child = f.getPartNode(ComposedPart.compose(suffix, Cell.get(0, 1), Part.self), m_lastSpreadsheet);
					root.addChild(child);
				}
				else
				{
					AndNode and = f.getAndNode();
					for (int i = 1; i < height; i++)
					{
						PartNode child = f.getPartNode(ComposedPart.compose(suffix, Cell.get(0, i), Part.self), m_lastSpreadsheet);
						and.addChild(child);
					}
					root.addChild(and);
				}
			}
			else
			{
				// Explain the whole y-axis: all cells except first column
			}
		}
		else if (to_explain.head() instanceof Caption)
		{
			if (a == Axis.X)
			{
				// Explain caption of the x-axis: top-left cell of the spreadsheet
				PartNode child = f.getPartNode(ComposedPart.compose(suffix, Cell.get(0, 0), Part.self), m_lastSpreadsheet);
				root.addChild(child);
			}
		}
		else
		{
			// Input part does not correspond to something we can explain for an axis
			root.addChild(f.getUnknownNode());
		}
	}
}

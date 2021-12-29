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
import java.util.Vector;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.PlotFormat;
import ca.uqac.lif.spreadsheet.plot.Scatterplot;

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
}

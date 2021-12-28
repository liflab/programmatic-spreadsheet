/*
  MTNP: Manipulate Tables N'Plots
  Copyright (C) 2017-2020 Sylvain Hallé

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

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.BoxPlot;
import ca.uqac.lif.spreadsheet.plot.PlotFormat;

/**
 * Gnuplot implementation of a boxplot.
 * @author Sylvain Hallé
 */
public class GnuBoxPlot extends GnuPlot implements BoxPlot
{
	/**
	 * Optional names given to the data series
	 */
	protected String[] m_seriesNames;
	
	public GnuBoxPlot(String ... series_names)
	{
		super();
		m_seriesNames = series_names;
	}

	@Override
	public void toGnuplot(PrintStream out, Spreadsheet table, PlotFormat term, String lab_title, boolean with_caption)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream p_baos = new PrintStream(baos);
		s_printer.print(table, p_baos);
		String csv_values = baos.toString();
		printHeader(out, term, lab_title, with_caption);
		out.println("set boxwidth 0.2 absolute");
		out.println("set offset 0.5,0.5,0,0");
		out.println("set ytics nomirror");
		out.println("set xlabel \"" + m_captionX + "\"");
		out.println("set ylabel \"" + m_captionY + "\"");
		if (!m_hasKey)
		{
			out.append("set key off\n");
		}
		int num_series = (table.getWidth() - 1) / 5;
		float offset = 0, offset_step = 0.3f;
		if (num_series % 2 == 0)
		{
			offset = -((((float) num_series) / 2f) - 1f) * offset_step - 0.15f;
		}
		else
		{
			offset = - ((float) num_series - 1f) / 2f * offset_step;
		}
		out.print("plot ");
		for (int s_count = 0; s_count < num_series; s_count++)
		{
			String signum = "+";
			if (offset < 0)
				signum = "-";
			out.print("'-' using ($1" + signum + Math.abs(offset) + "):" + (5 * s_count + 3) + ":" + (5 * s_count + 2) + (5 * s_count + 6) + ":" + (5 * s_count + 5) + ":xticlabels(" + (5 * s_count + 7) + ") with candlesticks title \"" +  getSeriesName(s_count) + "\" whiskerbars, ");
			out.print("'' using ($1" + signum + Math.abs(offset) + "):" + (5 * s_count + 4) + ":" + (5 * s_count + 4) + ":" + (5 * s_count + 4) + ":" + (5 * s_count + 4) + " with candlesticks linetype -1 linewidth 2 notitle, ");
			offset += offset_step;
		}
		out.println();
		// In Gnuplot, if we use the special "-" filename, we must repeat
		// the data as many times as we use it in the plot command; it does not remember it
		for (int i = 0; i < num_series + 1; i++)
		{
			out.println(csv_values + "end");
		}
	}
	
	protected String getSeriesName(int index)
	{
		if (m_seriesNames == null || index >= m_seriesNames.length)
		{
			return "Series " + (index + 1);
		}
		return m_seriesNames[index];
	}

	protected void copyInto(GnuBoxPlot sp)
	{
		super.copyInto(sp);
		sp.m_seriesNames = m_seriesNames;
	}

	@Override
	public GnuBoxPlot duplicate()
	{
		GnuBoxPlot sp = new GnuBoxPlot();
		copyInto(sp);
		return sp;
	}
}

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

import ca.uqac.lif.spreadsheet.plot.PlotFormat;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Two-dimensional bar diagram, also called a "clustered histogram".
 * <p>
 * <strong>Example usage.</strong> 
 * Suppose you have a set of experiments, each with three
 * parameters:
 * <ul>
 * <li><tt>name</tt> is the name of a web browser (Firefox, IE, etc.)</li>
 * <li><tt>market</tt> is the name of a market (video, audio, etc.)</li>
 * <li><tt>share</tt> is the market share (in %) for this browser in this market</li>
 * </ul>
 * We wish to create a bar diagram where each bar represents a market,
 * its height corresponds to the share, with one group of bars for each
 * browser. To is done by writing:
 * <pre>
 * BarPlot plot = new BarPlot();
 * ...
 * plot.useForX("browser").useForY("share").groupBy("market");
 * </pre>
 * This will create a histogram that looks like this:
 * <pre>
 * |                     # video
 * |                     $ audio
 * |                     @ text
 * |    $
 * |    $@         @
 * |   #$@        $@
 * |   #$@       #$@
 * +----+---------+-----&gt;
 *   Firefox     IE
 * </pre>
 * @author Sylvain Hallé
 *
 */
public class ClusteredHistogram extends GnuPlot
{
	/**
	 * Whether the histogram is of type "row stacked".
	 * (see {@link #rowStacked()})
	 */
	protected boolean m_rowStacked = false;
	
	/**
	 * The width of the box in the histogram. A value of -1 means the
	 * default setting will be used.
	 */
	protected float m_boxWidth = 0.75f;
	
	/**
	 * Creates a new bar plot
	 */
	public ClusteredHistogram()
	{
		super();
	}
	
	/**
	 * Sets whether the histogram is of type "row stacked".
	 * Using the example given above, the rowstacked setting will rather
	 * produce this plot:
	 * <pre>
   * |                     # video
   * |                     $ audio
   * |    @                @ text
   * |    @         @
   * |    $         @ 
   * |    $         $ 
   * |    #         # 
   * +----+---------+-----&gt;
   *   Firefox     IE
	 * </pre> 
	 * @return This plot
	 */
	public ClusteredHistogram rowStacked()
	{
		m_rowStacked = true;
		return this;
	}
	
	/**
	 * Sets the box width of the histogram. This is equivalent to the
	 * <tt>boxwidth</tt> setting of Gnuplot.
	 * @param w The width (generally a value between 0 and 1)
	 * @return This plot
	 */
	public ClusteredHistogram boxWidth(float w)
	{
		m_boxWidth = w;
		return this;
	}

	@Override
	public void toGnuplot(PrintStream out, Spreadsheet table, PlotFormat term, String lab_title, boolean with_caption)
	{
		String[] columns = table.getColumnNames();
		Vector<String> series = new Vector<String>();
		for (int i = 1; i < columns.length; i++)
		{
			series.add(columns[i]);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream p_baos = new PrintStream(baos);
		s_printer.print(table, p_baos);
		String csv_values = baos.toString();
		// Build GP string from table
		printHeader(out, term, lab_title, with_caption);
		out.println("set xtics rotate out");
		out.println("set style data histogram");
		out.println("set xlabel \"" + m_captionX + "\"\n");
		out.println("set ylabel \"" + m_captionY + "\"\n");
		if (m_rowStacked)
		{
			out.println("set style histogram rowstacked");
		}
		else
		{
			out.println("set style histogram clustered gap 1");
		}
		if (m_boxWidth > 0)
		{
			out.println("set boxwidth " + m_boxWidth);
		}
		out.println("set auto x");
		out.println("set yrange [0:*]");
		out.println("set style fill border rgb \"black\"");
		out.print("plot");
		for (int i = 0; i < series.size(); i++)
		{
			if (i > 0)
			{
				out.print(", ");
			}
			String s_name = series.get(i);
			out.print(" \"-\" using " + (i + 2) + ":xtic(1) title \"" + s_name + "\" " + getFillColor(i));
		}
		out.println();
		// In Gnuplot, if we use the special "-" filename, we must repeat
		// the data as many times as we use it in the plot command; it does not remember it
		for (int i = 0; i < series.size(); i++)
		{
			out.println(csv_values + "end");
		}
	}
	
	protected void copyInto(ClusteredHistogram ch)
	{
		super.copyInto(ch);
		ch.m_boxWidth = m_boxWidth;
		ch.m_rowStacked = m_rowStacked;
	}

	@Override
	public ClusteredHistogram duplicate()
	{
		ClusteredHistogram ch = new ClusteredHistogram();
		copyInto(ch);
		return ch;
	}
}
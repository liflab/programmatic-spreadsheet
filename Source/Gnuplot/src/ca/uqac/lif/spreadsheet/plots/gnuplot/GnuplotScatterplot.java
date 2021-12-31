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
import ca.uqac.lif.petitpoucet.Part.Nothing;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.PlotFormat;
import ca.uqac.lif.spreadsheet.plot.Scatterplot;
import ca.uqac.lif.spreadsheet.plot.part.Coordinate;
import ca.uqac.lif.spreadsheet.plot.part.NamedElement;
import ca.uqac.lif.spreadsheet.plot.part.NumberedElement;
import ca.uqac.lif.spreadsheet.plot.part.PlotAxis;
import ca.uqac.lif.spreadsheet.plot.part.PlotPart.Caption;
import ca.uqac.lif.spreadsheet.plot.part.PlotPart.DataSeries;
import ca.uqac.lif.spreadsheet.plot.part.PlotPart.Legend;
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
			explainPointAt((PointAt) head, -1, to_explain.tail(), suffix, root, f);
		}
		else if (head instanceof DataSeries)
		{
			explainDataSeries(to_explain.tail(), suffix, root, f);
		}
		else if (head instanceof Legend)
		{
			explainLegend(to_explain.tail(), suffix, root, f);
		}
		else
		{
			root.addChild(f.getUnknownNode());
		}
	}

	protected void explainDataSeries(Part to_explain, Part suffix, PartNode root, NodeFactory f)
	{
		int height = m_lastSpreadsheet.getHeight(), width = m_lastSpreadsheet.getWidth();
		if (to_explain == null || to_explain instanceof Nothing)
		{
			// All data series
			if (height == 2 && width == 2)
			{
				root.addChild(f.getPartNode(ComposedPart.compose(suffix, Cell.get(1, 1)), m_lastSpreadsheet));
			}
			else
			{
				AndNode and = f.getAndNode();
				root.addChild(and);
				for (int i = 1; i < height; i++)
				{
					for (int j = 1; j < width; j++)
					{
						root.addChild(f.getPartNode(ComposedPart.compose(suffix, Cell.get(j, i)), m_lastSpreadsheet));		
					}
				}
			}
			return;
		}
		Part head = to_explain.head();
		if (!(head instanceof NamedElement || head instanceof NumberedElement))
		{
			// Input part does not correspond to something we can explain 
			root.addChild(f.getUnknownNode());
			return;
		}
		int col_index = -1;
		if (head instanceof NamedElement)
		{
			String name = ((NamedElement) head).getName();
			for (int i = 1; i < width; i++)
			{
				Object o = m_lastSpreadsheet.get(i, 0);
				if (Spreadsheet.same(o, name))
				{
					col_index = i;
					break;
				}
			}
		}
		else if (head instanceof NumberedElement)
		{
			int index = ((NumberedElement) head).getIndex();
			if (index >= 0 && index < width - 1)
			{
				col_index = index + 1;
			}
		}
		if (col_index < 0)
		{
			// No data series with such name in the plot
			root.addChild(f.getUnknownNode());
			return;
		}
		Part h_tail = to_explain.tail();
		Part h_head = h_tail.head();
		if (h_head instanceof NumberedElement)
		{
			// Explain only one element of the data series
			explainNthElement((NumberedElement) h_head, col_index, h_tail, suffix, root, f);
		}
		else if (h_head instanceof PointAt)
		{
			// Explain a specific point in the data series
			explainPointAt((PointAt) h_head, col_index, h_tail, suffix, root, f);
		}
		else
		{
			// Explain the whole data series
			if (height == 2)
			{
				root.addChild(f.getPartNode(ComposedPart.compose(suffix, Cell.get(1, 1), Part.self), m_lastSpreadsheet));
			}
			else
			{
				AndNode and = f.getAndNode();
				root.addChild(and);
				for (int i = 1; i < height; i++)
				{
					and.addChild(f.getPartNode(ComposedPart.compose(suffix, Cell.get(col_index, i), Part.self), m_lastSpreadsheet));
				}
			}
		}
	}

	/**
	 * Computes the explanation for the legend of the plot. The part to be
	 * explained can take three forms:
	 * <ol>
	 * <li>The legend as a whole (in which case <tt>to_explain</tt> is either
	 * <tt>null</tt> or {@link Nothing}). The explanation is made of all cells
	 * of the first row of the spreadsheet corresponding to the name of a data
	 * series (i.e. all but the leftmost one).</li>
	 * <li>A single element of the legend designated by its position in the list
	 * (<tt>to_explain</tt> is an instance of {@link NumberedElement}). The
	 * explanation is made of the cell corresponding to that data series name
	 * in the top row of the spreadsheet, or to {@link Nothing} if the index
	 * does not correspond to an existing data series.</li>
	 * <li>A single element of the legend designated by the name of the data
	 * series (<tt>to_explain</tt> is an instance of {@link NamedElement}).
	 * The explanation is made of the cell corresponding to that name
	 * in the top row of the spreadsheet, or to {@link Nothing} if the index
	 * does not correspond to an existing data series.</li>
	 * </ol>
	 * @param to_explain The part of the legend to explain, if any
	 * @param suffix The part to be affixed to each part of the explanation
	 * @param root The root to which explanation nodes are to be appended
	 * @param f A factory to get node instances
	 */
	protected void explainLegend(Part to_explain, Part suffix, PartNode root, NodeFactory f)
	{
		int width = m_lastSpreadsheet.getWidth();
		if (to_explain == null || to_explain instanceof Nothing)
		{
			// Explain whole legend: made of first row except cell 0,0
			LabelledNode to_add = root;
			if (width > 2)
			{
				AndNode and = f.getAndNode();
				to_add.addChild(and);
				to_add = and;
			}
			for (int i = 1; i < width; i++)
			{
				to_add.addChild(f.getPartNode(ComposedPart.compose(suffix, Cell.get(i, 0), Part.self), m_lastSpreadsheet));
			}
			return;
		}
		Part head = to_explain.head();
		if (head instanceof NumberedElement)
		{
			int index = ((NumberedElement) head).getIndex();
			if (index < 0 || index > width - 1)
			{
				root.addChild(f.getUnknownNode());
			}
			else
			{
				root.addChild(f.getPartNode(ComposedPart.compose(suffix, Cell.get(index + 1, 0), Part.self), m_lastSpreadsheet));
			}
		}
		else if (head instanceof NamedElement)
		{
			String name = ((NamedElement) head).getName();
			boolean found = false;
			for (int i = 1; i < width; i++)
			{
				Object o = m_lastSpreadsheet.get(i, 0);
				if (Spreadsheet.same(o, name))
				{
					root.addChild(f.getPartNode(ComposedPart.compose(suffix, Cell.get(i, 0), Part.self), m_lastSpreadsheet));
					found = true;
					break;
				}
			}
			if (!found)
			{
				// No data series with such name in the plot
				root.addChild(f.getUnknownNode());
			}
		}
		else
		{
			// Input part does not correspond to something we can explain 
			root.addChild(f.getUnknownNode());
		}
	}

	/**
	 * Computes the explanation for a point at given coordinates in the plot.
	 * For a given point (x,y), the explanation can either be:
	 * <ul>
	 * <li>The conjunction of cells corresponding to the x value and the y
	 * value of the point in the spreadsheet, if a single such point exists</li>
	 * <li>The disjunction of the previous structure for each pair of cells,
	 * if more than one point exists with given coordinates</li>
	 * <li>{@link Nothing} otherwise</li>
	 * </ul>
	 * @param p The part designating the point
	 * @param col_index The column of the data series to look at for the y
	 * value; set to -1 to consider data series columns
	 * @param to_explain The part of the point to explain, if any
	 * @param suffix The part to be affixed to each part of the explanation
	 * @param root The root to which explanation nodes are to be appended
	 * @param f A factory to get node instances
	 */
	protected void explainPointAt(PointAt p, int col_index, Part to_explain, Part suffix, PartNode root, NodeFactory f)
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
			int j_min = 1, j_max = width;
			if (col_index >= 0)
			{
				j_min = col_index;
				j_max = col_index + 1;
			}
			for (int j = j_min; j < j_max; j++)
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
			// There is no point with such coordinates in the plot
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
			Part head = null;
			if (to_explain != null)
			{
				head = to_explain.tail();
			}
			for (Cell[] pair : pairs)
			{
				PartNode pn_x = f.getPartNode(ComposedPart.compose(pair[0], Part.self), m_lastSpreadsheet);
				PartNode pn_y = f.getPartNode(ComposedPart.compose(pair[1], Part.self), m_lastSpreadsheet);
				if (head != null && head.head() instanceof Coordinate)
				{
					switch (((Coordinate) head).getAxis())
					{
					case X:
						to_add.addChild(pn_x);
						break;
					case Y:
						to_add.addChild(pn_y);
						break;
					default:
						to_add.addChild(f.getUnknownNode());
						break;
					}
				}
				else
				{
					AndNode and = f.getAndNode();
					and.addChild(pn_x);
					and.addChild(pn_y);
					to_add.addChild(and);	
				}
			}
		}
	}
	
	/**
	 * Computes the explanation for the n-th point of a data series.
	 * For a given point, the explanation can either be:
	 * <ul>
	 * <li>The conjunction of cells corresponding to the x value and the y
	 * value of the point in the spreadsheet, if this point exists</li>
	 * <li>{@link Nothing} otherwise</li>
	 * </ul>
	 * @param p The part designating number of the element
	 * @param col_index The column of the data series to look at for the y
	 * value
	 * @param to_explain The part of the point to explain, if any
	 * @param suffix The part to be affixed to each part of the explanation
	 * @param root The root to which explanation nodes are to be appended
	 * @param f A factory to get node instances
	 */
	protected void explainNthElement(NumberedElement p, int col_index, Part to_explain, Part suffix, PartNode root, NodeFactory f)
	{
		int nb_to_look_for = p.getIndex(), current_nb = -1;
		for (int i = 0; i < m_lastSpreadsheet.getHeight(); i++)
		{
			Object x = m_lastSpreadsheet.get(0, i);
			Object y = m_lastSpreadsheet.get(col_index, i);
			if (x instanceof Number && y instanceof Number)
			{
				// This pair is a valid data point for the series
				current_nb++;
			}
			if (current_nb == nb_to_look_for)
			{
				// This is the element we are looking for
				explainPointAt(new PointAt(((Number) x).doubleValue(), ((Number) y).doubleValue()), col_index, to_explain, suffix, root, f);
				return;
			}
		}
		// There is no n-th element in this data series
		root.addChild(f.getUnknownNode());
	}

	/**
	 * Computes the explanation for an axis of the plot. The part to be
	 * explained can take three forms:
	 * <ol>
	 * <li>The x-axis as a whole (in which case <tt>to_explain</tt> is either
	 * <tt>null</tt> or {@link Nothing}). The explanation is made of all cells
	 * of the first column of the spreadsheet corresponding to the x-values
	 * on this axis (i.e. all but the topmost one).</li>
	 * <li>The y-axis as a whole (in which case <tt>to_explain</tt> is either
	 * <tt>null</tt> or {@link Nothing}). The explanation is made of all cells
	 * corresponding to y values in the plot (i.e. all except the first column
	 * and first row).</li>
	 * <li>The caption of the x-axis
	 * (<tt>to_explain</tt> is an instance of {@link Caption}). The
	 * explanation is made of the cell corresponding to that name
	 * in the spreadsheet, namely the top-left cell.</li>
	 * </ol>
	 * Note that no explanation can be given for the y-axis caption, as it is
	 * provided by the user and does not come from the spreadsheet.
	 * @param p The part designating the legend
	 * @param to_explain The part of the legend to explain, if any
	 * @param suffix The part to be affixed to each part of the explanation
	 * @param root The root to which explanation nodes are to be appended
	 * @param f A factory to get node instances
	 */
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

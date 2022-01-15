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
package ca.uqac.lif.spreadsheet.chart.gnuplot;

import java.io.PrintStream;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.HeatMap;
import ca.uqac.lif.spreadsheet.chart.ChartFormat;
import ca.uqac.lif.spreadsheet.chart.part.GridCell.CardinalGridCell;
import ca.uqac.lif.spreadsheet.chart.part.GridCell.OrdinalGridCell;

/**
 * Generates a Gnuplot file from a 2D heatmap.
 * @author Sylvain Hallé
 */
public class GnuplotHeatMap extends Gnuplot implements HeatMap
{
	/**
	 * The caption given to the color scale in the plot.
	 */
	protected String m_scaleCaption = "";
	
	/**
	 * Creates a new heat map associated to no table
	 */
	public GnuplotHeatMap()
	{
		super();
	}

	@Override
	public void toGnuplot(PrintStream out, Spreadsheet table, ChartFormat term, String lab_title, boolean with_caption) 
	{		
		// Create Gnuplot output file from that data
		printHeader(out, term, lab_title, with_caption);
		//out.append("set xrange [").append(ft.getMinX() - ft.getXWidth() / 2).append(":").append(ft.getMaxX() - ft.getXWidth() / 2).append("]\n");
		//out.append("set yrange [").append(ft.getMinY() - ft.getYWidth() / 2).append(":").append(ft.getMaxY() - ft.getYWidth() / 2).append("]\n");
		out.println("set tic scale 0");
		out.println("set cblabel \"" + m_scaleCaption + "\"");
		out.println("unset cbtics");
		out.println("$map1 << EOD");
		//double[][] values = ft.getArray();
		Double[] scale_x = table.getRowNumerical(0);
		Double[] scale_y = table.getColumnNumerical(0);
		for (int j = 1; j < scale_x.length; j++)
		{
			out.print("," + scale_x[j]);
		}
		out.println();
		for (int i = 1; i < table.getHeight(); i++)
		{
			out.print(scale_y[i]);
			for (int j = 1; j < table.getWidth(); j++)
			{
				out.print("," + table.get(j, i));
			}
			out.println();
		}
		out.println("EOD");
		out.println();
		out.println("set view map");
		out.println("plot '$map1' matrix rowheaders columnheaders using 1:2:3 with image");
	}
	
	@Override
	protected void explainChartPart(Part to_explain, Part suffix, PartNode root, NodeFactory f)
	{
		Part head = to_explain.head();
		if (head instanceof OrdinalGridCell)
		{
			OrdinalGridCell ogc = (OrdinalGridCell) head;
			explainOrdinalGridCell((int) ogc.getX(), (int) ogc.getY(), suffix, root, f);
		}
		else if (head instanceof CardinalGridCell)
		{
			CardinalGridCell ogc = (CardinalGridCell) head;
			explainCardinalGridCell(ogc.getX(), ogc.getY(), suffix, root, f);
		}
		else
		{
			root.addChild(f.getUnknownNode());
		}
	}
	
	/**
	 * Calculates the explanation of a heatmap cell expressed as an ordinal
	 * location.
	 * @param left The position of the cell, counting from the left of the
	 * heatmap
	 * @param bottom The position of the cell, counting from the bottom of the
	 * heatmap
	 * @param suffix The part suffix to add to the leaf nodes
	 * @param root The root to which nodes are to be added
	 * @param f A factory to get node instances
	 */
	protected void explainOrdinalGridCell(int left, int bottom, Part suffix, PartNode root, NodeFactory f)
	{
		Part new_p = ComposedPart.compose(suffix, Cell.get(left + 1, bottom + 1), Part.self);
		root.addChild(f.getPartNode(new_p, m_lastSpreadsheet));
	}
	
	/**
	 * Calculates the explanation of a heatmap cell expressed as an cardinal
	 * location.
	 * @param left The x value of the cell
	 * @param bottom The y value of the cell
	 * @param suffix The part suffix to add to the leaf nodes
	 * @param root The root to which nodes are to be added
	 * @param f A factory to get node instances
	 */
	protected void explainCardinalGridCell(double x, double y, Part suffix, PartNode root, NodeFactory f)
	{
		int left, top;
		Double last = null;
		for (left = 1; left < m_lastSpreadsheet.getWidth(); left++)
		{
			Double d = m_lastSpreadsheet.getNumerical(left, 0);
			if (d == null)
			{
				continue;
			}
			if (last == null && x < d)
			{
				// This value is not contained in any cell
				left = -1;
				break;
			}
			if (x < d && (last == null || x >= last))
			{
				break;
			}
			last = d;
		}
		left--;
		last = null;
		for (top = 1; top < m_lastSpreadsheet.getHeight(); top++)
		{
			Double d = m_lastSpreadsheet.getNumerical(0, top);
			if (d == null)
			{
				continue;
			}
			if (last == null && x < d)
			{
				// This value is not contained in any cell
				top = -1;
				break;
			}
			if (y < d && (last == null || y >= last))
			{
				break;
			}
			last = d;
		}
		top--;
		//int bottom = m_lastSpreadsheet.getHeight() - top;
		explainOrdinalGridCell(left - 1, top - 1, suffix, root, f);
	}
	
	@Override
	public GnuplotHeatMap setTitle(String title)
	{
		super.setTitle(title);
		return this;
	}
	
	@Override
	public GnuplotHeatMap setCaption(Axis a, String caption)
	{
		super.setCaption(a, caption);
		return this;
	}
	
	protected void copyInto(GnuplotHeatMap hm)
	{
		super.copyInto(hm);
		hm.m_scaleCaption = m_scaleCaption;
	}

	@Override
	public GnuplotHeatMap duplicate()
	{
		GnuplotHeatMap hm = new GnuplotHeatMap();
		copyInto(hm);
		return hm;
	}

	@Override
	public GnuplotHeatMap setScaleCaption(String caption)
	{
		m_scaleCaption = caption;
		return this;
	}
	
	@Override
	public String toString()
	{
		return "Heatmap";
	}
}

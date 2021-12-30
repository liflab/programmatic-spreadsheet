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

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.plot.UnsupportedPlotFormatException;
import ca.uqac.lif.spreadsheet.plot.part.PlotAxis;
import ca.uqac.lif.spreadsheet.plot.part.PlotPart;
import ca.uqac.lif.spreadsheet.plot.part.PointAt;
import ca.uqac.lif.spreadsheet.plot.Plot.Axis;

/**
 * Unit tests for {@link GnuplotScatterplot}.
 */
public class GnuplotScatterplotTest
{
	@Test
	public void testExplanation1() throws IllegalArgumentException, UnsupportedPlotFormatException, IOException
	{
		Spreadsheet s = Spreadsheet.read(3, 11, 
				"x", "Apples", "Oranges",
				0,   0,        3, 
				1,   1,        1,
				2,   2,        4,
				3,   3,        1,
				4,   4,        5,
				5,   5,        9,
				6,   6,        2,
				7,   7,        6,
				8,   8,        5,
				9,   9,        3);
		GnuplotScatterplot plot = new GnuplotScatterplot()
				.setTitle("Apples and oranges").setCaption(Axis.Y, "Fruits");
		plot.render(null, s);
		// The explanation of the whole plot is the whole spreadsheet
		PartNode root = plot.getExplanation(Part.self);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(Part.self, child.getPart());
		assertEquals(s, child.getSubject());
	}

	@Test
	public void testExplanation2() throws IllegalArgumentException, UnsupportedPlotFormatException, IOException
	{
		Spreadsheet s = Spreadsheet.read(3, 11, 
				"x", "Apples", "Oranges",
				0,   0,        3, 
				1,   1,        1,
				2,   2,        4,
				3,   3,        1,
				4,   4,        5,
				5,   5,        9,
				6,   6,        2,
				7,   7,        6,
				8,   8,        5,
				9,   9,        3);
		GnuplotScatterplot plot = new GnuplotScatterplot()
				.setTitle("Apples and oranges").setCaption(Axis.Y, "Fruits");
		plot.render(null, s);
		// The explanation of the x-axis caption is cell 0,0
		PartNode root = plot.getExplanation(ComposedPart.compose(PlotPart.caption, new PlotAxis(Axis.X), Part.self));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(0, 0), Part.self), child.getPart());
		assertEquals(s, child.getSubject());
	}

	@Test
	public void testExplanation3() throws IllegalArgumentException, UnsupportedPlotFormatException, IOException
	{
		Spreadsheet s = Spreadsheet.read(3, 11, 
				"x", "Apples", "Oranges",
				0,   0,        3, 
				1,   1,        1,
				2,   2,        4,
				3,   3,        1,
				4,   4,        5,
				5,   5,        9,
				6,   6,        2,
				7,   7,        6,
				8,   8,        5,
				9,   9,        3);
		GnuplotScatterplot plot = new GnuplotScatterplot()
				.setTitle("Apples and oranges").setCaption(Axis.Y, "Fruits");
		plot.render(null, s);
		// The explanation of the x-axis is all column 0 except cell 0,0
		PartNode root = plot.getExplanation(ComposedPart.compose(new PlotAxis(Axis.X), Part.self));
		assertEquals(1, root.getOutputLinks(0).size());
		AndNode and = (AndNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(10, and.getOutputLinks(0).size());
		for (int i = 0; i < 10; i++)
		{
			PartNode child = (PartNode) and.getOutputLinks(0).get(i).getNode();
			assertEquals(ComposedPart.compose(Cell.get(0, i + 1), Part.self), child.getPart());
			assertEquals(s, child.getSubject());
		}
	}

	@Test
	public void testExplanation4() throws IllegalArgumentException, UnsupportedPlotFormatException, IOException
	{
		Spreadsheet s = Spreadsheet.read(3, 11, 
				"Something", "Apples", "Oranges",
				0,   0,        3, 
				1,   1,        1,
				2,   2,        4,
				3,   3,        1,
				4,   4,        5,
				5,   5,        9,
				6,   6,        2,
				7,   7,        6,
				8,   8,        5,
				9,   9,        3);
		GnuplotScatterplot plot = new GnuplotScatterplot()
				.setTitle("Apples and oranges").setCaption(Axis.Y, "Fruits");
		plot.render(null, s);
		// The explanation of the characters at position 1-2 in the x-axis caption is the same range in cell 0,0
		PartNode root = plot.getExplanation(ComposedPart.compose(new Range(1, 2), PlotPart.caption, new PlotAxis(Axis.X), Part.self));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(new Range(1, 2), Cell.get(0, 0), Part.self), child.getPart());
		assertEquals(s, child.getSubject());
	}

	@Test
	public void testExplanation6() throws IllegalArgumentException, UnsupportedPlotFormatException, IOException
	{
		Spreadsheet s = Spreadsheet.read(3, 11, 
				"Something", "Apples", "Oranges",
				0,   0,        3, 
				1,   1,        1,
				2,   2,        4,
				3,   3,        1,
				4,   4,        5,
				5,   5,        9,
				6,   6,        2,
				7,   7,        6,
				8,   8,        5,
				9,   9,        3);
		GnuplotScatterplot plot = new GnuplotScatterplot()
				.setTitle("Apples and oranges").setCaption(Axis.Y, "Fruits");
		plot.render(null, s);
		// The explanation of the point at position 1,1 is double: either the pair of cells 0,2-1,2 or 0,2-2,2
		PartNode root = plot.getExplanation(ComposedPart.compose(new PointAt(1, 1), Part.self));
		assertEquals(1, root.getOutputLinks(0).size());
		OrNode on = (OrNode)root.getOutputLinks(0).get(0).getNode();
		assertEquals(2, on.getOutputLinks(0).size());
		{
			AndNode and = (AndNode) on.getOutputLinks(0).get(0).getNode();
			assertEquals(2, and.getOutputLinks(0).size());
			{
				PartNode child = (PartNode) and.getOutputLinks(0).get(0).getNode();
				assertEquals(ComposedPart.compose(Cell.get(0, 2), Part.self), child.getPart());
				assertEquals(s, child.getSubject());
			}
			{
				PartNode child = (PartNode) and.getOutputLinks(0).get(1).getNode();
				assertEquals(ComposedPart.compose(Cell.get(1, 2), Part.self), child.getPart());
				assertEquals(s, child.getSubject());
			}
		}
		{
			AndNode and = (AndNode) on.getOutputLinks(0).get(1).getNode();
			assertEquals(2, and.getOutputLinks(0).size());
			{
				PartNode child = (PartNode) and.getOutputLinks(0).get(0).getNode();
				assertEquals(ComposedPart.compose(Cell.get(0, 2), Part.self), child.getPart());
				assertEquals(s, child.getSubject());
			}
			{
				PartNode child = (PartNode) and.getOutputLinks(0).get(1).getNode();
				assertEquals(ComposedPart.compose(Cell.get(2, 2), Part.self), child.getPart());
				assertEquals(s, child.getSubject());
			}
		}
	}
}

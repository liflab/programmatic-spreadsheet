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
package ca.uqac.lif.spreadsheet.functions;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.ExpandAsColumns;
import ca.uqac.lif.spreadsheet.functions.SpreadsheetFunction.InputCell;

public class ExpandAsColumnsTest
{
	@Test
	public void test1()
	{
		Spreadsheet s = new Spreadsheet(3, 2);
		s.set(0, 0, "A");
		s.set(1, 0, "B");
		s.set(2, 0, "C");
		s.set(0, 1, 1);
		s.set(1, 1, "foo");
		s.set(2, 1, "bar");
		ExpandAsColumns f = new ExpandAsColumns(1, 2);
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(2, out.getWidth());
		assertEquals(2, out.getHeight());
		assertEquals("A", out.get(0, 0));
		assertEquals("foo", out.get(1, 0));
		assertEquals(1, out.get(0, 1));
		assertEquals("bar", out.get(1, 1));
		{
			PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(1, 1), NthOutput.FIRST));
			AndNode and = (AndNode) root.getOutputLinks(0).get(0).getNode();
			assertEquals(2, and.getOutputLinks(0).size());
			PartNode child1 = (PartNode) and.getOutputLinks(0).get(0).getNode();
			assertEquals(ComposedPart.compose(Cell.get(1, 1), NthInput.FIRST), child1.getPart());
			PartNode child2 = (PartNode) and.getOutputLinks(0).get(1).getNode();
			assertEquals(ComposedPart.compose(Cell.get(2, 1), NthInput.FIRST), child2.getPart());
		}
	}
	
	@Test
	public void test2()
	{
		Spreadsheet s = new Spreadsheet(3, 4);
		s.set(0, 0, "A");
		s.set(1, 0, "B");
		s.set(2, 0, "C");
		s.set(0, 1, 1);
		s.set(1, 1, "foo");
		s.set(2, 1, "bar");
		s.set(0, 2, 1);
		s.set(1, 2, "baz");
		s.set(2, 2, "abc");
		s.set(0, 3, 10);
		s.set(1, 3, "foo");
		s.set(2, 3, 55);
		ExpandAsColumns f = new ExpandAsColumns(1, 2);
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(3, out.getWidth());
		//assertEquals(3, out.getHeight());
		assertEquals("A", out.get(0, 0));
		assertEquals("foo", out.get(1, 0));
		assertEquals("baz", out.get(2, 0));
		assertEquals(1, out.get(0, 1));
		assertEquals("bar", out.get(1, 1));
		assertEquals("abc", out.get(2, 1));
		assertEquals(10, out.get(0, 2));
		assertEquals(55, out.get(1, 2));
		assertNull(out.get(2, 2));
	}
	
	@Test
	public void testExplain1()
	{
		Spreadsheet s = Spreadsheet.read(3, 6,
				"Size", "Method", "Time",
				1,      "A",      10,
				2,      "A",      20,
				3,      "A",      30,
				1,      "B",      5,
				2,      "B",      7
				);
		ExpandAsColumns f = new ExpandAsColumns("Method", "Time");
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(Spreadsheet.read(3, 4,
				"Size", "A", "B",
				1,      10,  5,
				2,      20,  7,
				3,      30,  null), out);
		InputCell[] cells = f.trackToInput(1, 2);
		assertEquals(2, cells.length);
		assertEquals(InputCell.get(1, 2), cells[0]);
		assertEquals(InputCell.get(2, 2), cells[1]);
	}
	
	@Test
	public void testExplain2()
	{
		Spreadsheet s = Spreadsheet.read(3, 6,
				"Method", "Size", "Time",
				"A",      1,      10,
				"A",      2,      20,
				"A",      3,      30,
				"B",      1,      5,
				"B",      2,      7
				);
		ExpandAsColumns f = new ExpandAsColumns("Method", "Time");
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(Spreadsheet.read(3, 4,
				"Size", "A", "B",
				1,      10,  5,
				2,      20,  7,
				3,      30,  null), out);
		InputCell[] cells = f.trackToInput(1, 2);
		assertEquals(2, cells.length);
		assertEquals(InputCell.get(0, 2), cells[0]);
		assertEquals(InputCell.get(2, 2), cells[1]);
	}
}

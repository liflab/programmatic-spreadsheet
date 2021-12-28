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
			assertEquals(ComposedPart.compose(Cell.get(2, 1), NthInput.FIRST), child1.getPart());
			PartNode child2 = (PartNode) and.getOutputLinks(0).get(1).getNode();
			assertEquals(ComposedPart.compose(Cell.get(1, 1), NthInput.FIRST), child2.getPart());
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
}

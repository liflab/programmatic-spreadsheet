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

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.ValueOf;

public class ValueOfTest
{
	@Test
	public void test1()
	{
		ValueOf f = new ValueOf(Cell.get(2, 3));
		Spreadsheet s = new Spreadsheet(5, 5);
		s.set(2, 3, "foo");
		Object o = f.evaluate(s)[0];
		assertTrue(o instanceof String);
		assertEquals("foo", (String) o);
		Part p = ComposedPart.compose(NthOutput.FIRST);
		PartNode root = f.getExplanation(p);
		assertNotNull(root);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(2, 3), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void test2()
	{
		ValueOf f = new ValueOf(Cell.get(2, 3));
		Spreadsheet s = new Spreadsheet(5, 5);
		s.set(2, 3, "foo");
		Object o = f.evaluate(s)[0];
		assertTrue(o instanceof String);
		assertEquals("foo", (String) o);
		Part p = ComposedPart.compose(new Range(0, 1), NthOutput.FIRST);
		PartNode root = f.getExplanation(p);
		assertNotNull(root);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(new Range(0, 1), Cell.get(2, 3), NthInput.FIRST), child.getPart());
	}
}

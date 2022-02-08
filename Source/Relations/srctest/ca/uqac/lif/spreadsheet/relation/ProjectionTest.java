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
package ca.uqac.lif.spreadsheet.relation;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Unit tests for {@link Projection}.
 */
public class ProjectionTest
{
	@Test
	public void test1()
	{
		Spreadsheet s = Spreadsheet.read(3, 6,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				3,    "o",  true,
				2,    "f",  false,
				3,    "g",  true);
		Projection f = new Projection("A", "C");
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(Spreadsheet.read(2, 4,
				"A",  "C",
				3,    true,
				1,    null,
				2,    false), out);
	}
	
	@Test
	public void test2()
	{
		Spreadsheet s = Spreadsheet.read(3, 6,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				3,    "o",  true,
				2,    "f",  false,
				3,    "g",  true);
		Projection f = new Projection("C", "A");
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(Spreadsheet.read(2, 4,
				"C",   "A",
				true,  3,
				null,  1,
				false, 2), out);
	}

	@Test (expected = RelationalException.class)
	public void testInvalid1()
	{
		// Invalid: column does not exist
		Spreadsheet s = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				null, "o",  false);
		Projection f = new Projection("A", "Z");
		f.evaluate(s);
	}

	@Test
	public void testExplain1()
	{
		Spreadsheet s = Spreadsheet.read(3, 6,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				3,    "o",  true,
				2,    "f",  false,
				3,    "g",  true);
		Projection f = new Projection("A", "C");
		f.evaluate(s);
		PartNode root = f.getExplanation(NthOutput.FIRST);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(NthInput.FIRST, child.getPart());
	}

	@Test
	public void testExplain2()
	{
		Spreadsheet s = Spreadsheet.read(3, 6,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				3,    "o",  true,
				2,    "f",  false,
				3,    "g",  true);
		Projection f = new Projection("A", "C");
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(0, 1), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		AndNode or = (AndNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(3, or.getOutputLinks(0).size());
		{
			PartNode child = (PartNode) or.getOutputLinks(0).get(0).getNode();
			assertEquals(ComposedPart.compose(Cell.get(0, 1), NthInput.FIRST), child.getPart());
		}
		{
			PartNode child = (PartNode) or.getOutputLinks(0).get(1).getNode();
			assertEquals(ComposedPart.compose(Cell.get(0, 3), NthInput.FIRST), child.getPart());
		}
		{
			PartNode child = (PartNode) or.getOutputLinks(0).get(2).getNode();
			assertEquals(ComposedPart.compose(Cell.get(0, 5), NthInput.FIRST), child.getPart());
		}
	}

	@Test
	public void testExplain3()
	{
		Spreadsheet s = Spreadsheet.read(3, 6,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				3,    "o",  true,
				2,    "f",  false,
				3,    "g",  true);
		Projection f = new Projection("A", "C");
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(0, 3), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(0, 4), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain4()
	{
		Spreadsheet s = Spreadsheet.read(3, 6,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				3,    "o",  true,
				2,    "f",  false,
				3,    "g",  true);
		Projection f = new Projection("C", "A");
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(0, 3), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(2, 4), NthInput.FIRST), child.getPart());
	}

	@Test
	public void testExplain5()
	{
		Spreadsheet s = Spreadsheet.read(3, 6,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				3,    "o",  true,
				2,    "f",  false,
				3,    "g",  true);
		Projection f = new Projection("A", "C");
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(1, 2), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(2, 2), NthInput.FIRST), child.getPart());
	}
}

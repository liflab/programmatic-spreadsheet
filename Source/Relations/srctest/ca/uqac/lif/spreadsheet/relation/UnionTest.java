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
import ca.uqac.lif.petitpoucet.OrNode;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Unit tests for {@link Union}.
 */
public class UnionTest
{
	@Test
	public void test1()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				null, "o",  false);
		Spreadsheet s2 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				5,    "f",  true,
				1,    "o",  null,
				null, "o",  true);
		Union f = new Union(2);
		Spreadsheet out = (Spreadsheet) f.evaluate(s1, s2)[0];
		assertEquals(Spreadsheet.read(3, 6,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				null, "o",  false,
				5,    "f",  true,
				null, "o",  true), out);
	}

	@Test (expected = RelationalException.class)
	public void testInvalid1()
	{
		// Invalid: columns have different names
		Spreadsheet s1 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				null, "o",  false);
		Spreadsheet s2 = Spreadsheet.read(3, 4,
				"A",  "B", "F",
				5,    "f",  true,
				1,    "o",  null,
				null, "o",  true);
		Union f = new Union(2);
		f.evaluate(s1, s2);
	}

	@Test (expected = RelationalException.class)
	public void testInvalid2()
	{
		// Invalid: different widths
		Spreadsheet s1 = Spreadsheet.read(2, 4,
				"A",  "B",
				3,    "f",
				1,    "o",
				null, "o");
		Spreadsheet s2 = Spreadsheet.read(3, 4,
				"A",  "B", "F",
				5,    "f",  true,
				1,    "o",  null,
				null, "o",  true);
		Union f = new Union(2);
		f.evaluate(s1, s2);
	}

	@Test (expected = RelationalException.class)
	public void testInvalid3()
	{
		// Invalid: incompatible column types
		Spreadsheet s1 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				"a",  "f",  true,
				"b",  "o",  null,
				null, "o",  false);
		Spreadsheet s2 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				5,    "f",  true,
				1,    "o",  null,
				null, "o",  true);
		Union f = new Union(2);
		f.evaluate(s1, s2);
	}

	@Test
	public void testExplain1()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				null, "o",  false);
		Spreadsheet s2 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				5,    "f",  true,
				1,    "o",  null,
				null, "o",  true);
		Union f = new Union(2);
		f.evaluate(s1, s2);
		PartNode root = f.getExplanation(NthOutput.FIRST);
		assertEquals(1, root.getOutputLinks(0).size());
		AndNode and = (AndNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(2, and.getOutputLinks(0).size());
		{
			PartNode child = (PartNode) and.getOutputLinks(0).get(0).getNode();
			assertEquals(NthInput.FIRST, child.getPart());
		}
		{
			PartNode child = (PartNode) and.getOutputLinks(0).get(1).getNode();
			assertEquals(NthInput.SECOND, child.getPart());
		}
	}

	@Test
	public void testExplain2()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				null, "o",  false);
		Spreadsheet s2 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				1,    "o",  null,
				5,    "f",  true,
				null, "o",  true);
		Union f = new Union(2);
		f.evaluate(s1, s2);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(1, 2), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		OrNode or = (OrNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(2, or.getOutputLinks(0).size());
		{
			PartNode child = (PartNode) or.getOutputLinks(0).get(0).getNode();
			assertEquals(ComposedPart.compose(Cell.get(1, 2), NthInput.FIRST), child.getPart());
		}
		{
			PartNode child = (PartNode) or.getOutputLinks(0).get(1).getNode();
			assertEquals(ComposedPart.compose(Cell.get(1, 1), NthInput.SECOND), child.getPart());
		}
	}

	@Test
	public void testExplain3()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				null, "o",  false);
		Spreadsheet s2 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				1,    "o",  null,
				5,    "f",  true,
				null, "o",  true);
		Union f = new Union(2);
		f.evaluate(s1, s2);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(1, 0), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		OrNode or = (OrNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(2, or.getOutputLinks(0).size());
		{
			PartNode child = (PartNode) or.getOutputLinks(0).get(0).getNode();
			assertEquals(ComposedPart.compose(Cell.get(1, 0), NthInput.FIRST), child.getPart());
		}
		{
			PartNode child = (PartNode) or.getOutputLinks(0).get(1).getNode();
			assertEquals(ComposedPart.compose(Cell.get(1, 0), NthInput.SECOND), child.getPart());
		}
	}

	@Test
	public void testExplain4()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				3,    "f",  true,
				1,    "o",  null,
				null, "o",  false);
		Spreadsheet s2 = Spreadsheet.read(3, 4,
				"A",  "B", "C",
				1,    "o",  null,
				5,    "f",  true,
				null, "o",  true);
		Union f = new Union(2);
		f.evaluate(s1, s2);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(0, 5), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(0, 3), NthInput.SECOND), child.getPart());
	}
}

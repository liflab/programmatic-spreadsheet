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

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.SpreadsheetOutOfBoundsException;

/**
 * Unit tests for {@link CopyFrom}.
 */
public class CopyFromTest
{
	@Test
	public void test1()
	{
		Object o = new Object();
		Spreadsheet s = Spreadsheet.read(3, 3,
				"a", "b", "c",
				1,   true, null,
				2.5, o,    6);
		CopyFrom f = new CopyFrom(Cell.get(1, 0), Cell.get(2, 3));
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(Spreadsheet.read(2, 4,
				"b",  "c",
				true, null,
				o,    6,
				null, null), out);
	}
	
	@Test(expected = SpreadsheetOutOfBoundsException.class)
	public void testInvalidBounds1()
	{
		new CopyFrom(Cell.get(-1, 0), Cell.get(2, 3));
	}
	
	@Test(expected = SpreadsheetOutOfBoundsException.class)
	public void testInvalidBounds2()
	{
		new CopyFrom(Cell.get(1, 0), Cell.get(0, 3));
	}
	
	@Test(expected = SpreadsheetOutOfBoundsException.class)
	public void testInvalidBounds3()
	{
		new CopyFrom(Cell.get(2, 2), Cell.get(3, 1));
	}
	
	@Test(expected = SpreadsheetOutOfBoundsException.class)
	public void testInvalidBounds4()
	{
		new CopyFrom(Cell.get(0, -1), Cell.get(2, 3));
	}
	
	@Test
	public void testExplain1()
	{
		Object o = new Object();
		Spreadsheet s = Spreadsheet.read(3, 3,
				"a", "b", "c",
				1,   true, null,
				2.5, o,    6);
		CopyFrom f = new CopyFrom(Cell.get(1, 0), Cell.get(2, 3));
		f.evaluate(s);
		PartNode root = f.getExplanation(NthOutput.FIRST);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(NthInput.FIRST, child.getPart());
	}
	
	@Test
	public void testExplain2()
	{
		Object o = new Object();
		Spreadsheet s = Spreadsheet.read(3, 3,
				"a", "b", "c",
				1,   true, null,
				2.5, o,    6);
		CopyFrom f = new CopyFrom(Cell.get(1, 0), Cell.get(2, 3));
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(0, 1), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(1, 1), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain3()
	{
		Object o = new Object();
		Spreadsheet s = Spreadsheet.read(3, 3,
				"a", "b", "c",
				1,   true, null,
				2.5, o,    6);
		CopyFrom f = new CopyFrom(Cell.get(1, 0), Cell.get(2, 3));
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(0, 3), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(Part.nothing, child.getPart());
	}
}

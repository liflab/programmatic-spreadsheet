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

/**
 * Unit tests for {@link Resize}.
 */
public class ResizeTest
{
	@Test
	public void test1()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"A", "B", "C",
				"a", 3,   null,
				"b", 1,   true,
				"c", 4,   false);
		Resize f = new Resize(4, 3);
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(Spreadsheet.read(4, 3, 
				"A", "B", "C",  null,
				"a", 3,   null, null,
				"b", 1,   true, null), out);
	}
	
	@Test
	public void testExplain1()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"A", "B", "C",
				"a", 3,   null,
				"b", 1,   true,
				"c", 4,   false);
		Resize f = new Resize(4, 3);
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(2, 2), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(2, 2), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain2()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"A", "B", "C",
				"a", 3,   null,
				"b", 1,   true,
				"c", 4,   false);
		Resize f = new Resize(4, 3);
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(3, 2), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(Part.nothing, child.getPart());
	}
}

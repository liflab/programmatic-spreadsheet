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
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Unit tests for {@link PasteAt}.
 */
public class PasteAtTest
{
	@Test
	public void test1()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 3,
				3, 1, 4,
				1, 5, 1,
				9, 2, 6);
		Spreadsheet s2 = Spreadsheet.read(2, 2,
				2, 7,
				1, 8);
		PasteAt f = new PasteAt(Cell.get(2, 1));
		Spreadsheet out = (Spreadsheet) f.evaluate(s1, s2)[0];
		assertEquals(Spreadsheet.read(4, 3,
				3, 1, 4, null,
				1, 5, 2, 7,
				9, 2, 1, 8), out);
	}
	
	@Test
	public void testExplain1()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 3,
				3, 1, 4,
				1, 5, 1,
				9, 2, 6);
		Spreadsheet s2 = Spreadsheet.read(2, 2,
				2, 7,
				1, 8);
		PasteAt f = new PasteAt(Cell.get(2, 1));
		f.evaluate(s1, s2);
		PartNode root = f.getExplanation(NthOutput.FIRST);
		assertNotNull(root);
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
		Spreadsheet s1 = Spreadsheet.read(3, 3,
				3, 1, 4,
				1, 5, 1,
				9, 2, 6);
		Spreadsheet s2 = Spreadsheet.read(2, 2,
				2, 7,
				1, 8);
		PasteAt f = new PasteAt(Cell.get(2, 1));
		f.evaluate(s1, s2);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(2, 0), NthOutput.FIRST));
		assertNotNull(root);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(2, 0), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain3()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 3,
				3, 1, 4,
				1, 5, 1,
				9, 2, 6);
		Spreadsheet s2 = Spreadsheet.read(2, 2,
				2, 7,
				1, 8);
		PasteAt f = new PasteAt(Cell.get(2, 1));
		f.evaluate(s1, s2);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(3, 1), NthOutput.FIRST));
		assertNotNull(root);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(1, 0), NthInput.SECOND), child.getPart());
	}
	
	@Test
	public void testExplain4()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 3,
				3, 1, 4,
				1, 5, 1,
				9, 2, 6);
		Spreadsheet s2 = Spreadsheet.read(2, 2,
				2, 7,
				1, 8);
		PasteAt f = new PasteAt(Cell.get(2, 1));
		f.evaluate(s1, s2);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(3, 0), NthOutput.FIRST));
		assertNotNull(root);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(Part.nothing, child.getPart());
	}
}

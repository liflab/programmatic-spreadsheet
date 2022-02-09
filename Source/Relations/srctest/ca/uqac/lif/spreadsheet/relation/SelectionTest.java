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

import static ca.uqac.lif.dag.NodeConnector.connect;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.Constant;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.number.IsGreaterThan;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Unit tests for {@link Selection}.
 */
public class SelectionTest
{
	@Test
	public void test1()
	{
		Spreadsheet s = Spreadsheet.read(3, 4,
				"A", "B", "C",
				3,   1,   "a",
				4,   1,   "b",
				5,   5,   "c");
		Selection f = new Selection(new Constant(true));
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(s, out);
	}
	
	@Test
	public void test2()
	{
		Spreadsheet s = Spreadsheet.read(3, 4,
				"A", "B", "C",
				3,   1,   "a",
				4,   1,   "b",
				5,   5,   "c");
		Circuit c = new Circuit(1, 1, "> 3");
		{
			AttributeValue a = AttributeValue.get("A");
			IsGreaterThan gt = new IsGreaterThan();
			Constant three = new Constant(3);
			connect(a, 0, gt, 0);
			connect(three, 0, gt, 1);
			c.associateInput(0, a.getInputPin(0));
			c.associateOutput(0, gt.getOutputPin(0));
			c.addNodes(a, gt, three);
		}
		Selection f = new Selection(c);
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(Spreadsheet.read(3, 3,
				"A", "B", "C",
				4,   1,   "b",
				5,   5,   "c"), out);
	}
	
	@Test
	public void testExplanation1()
	{
		Spreadsheet s = Spreadsheet.read(3, 4,
				"A", "B", "C",
				3,   1,   "a",
				4,   1,   "b",
				5,   5,   "c");
		Circuit c = new Circuit(1, 1, "> 3");
		{
			AttributeValue a = AttributeValue.get("A");
			IsGreaterThan gt = new IsGreaterThan();
			Constant three = new Constant(3);
			connect(a, 0, gt, 0);
			connect(three, 0, gt, 1);
			c.associateInput(0, a.getInputPin(0));
			c.associateOutput(0, gt.getOutputPin(0));
			c.addNodes(a, gt, three);
		}
		Selection f = new Selection(c);
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(1, 1), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(1, 2), NthInput.FIRST), child.getPart());
	}
}

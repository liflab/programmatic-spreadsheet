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

import static ca.uqac.lif.dag.NodeConnector.connect;

import ca.uqac.lif.dag.NestedNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.Constant;
import ca.uqac.lif.petitpoucet.function.Fork;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.number.Addition;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.ApplyFormula;
import ca.uqac.lif.spreadsheet.functions.CellFormula;
import ca.uqac.lif.spreadsheet.functions.ValueOf;

public class ApplyFormulaTest
{
	@Test
	public void test0()
	{
		// Sets cell 0:1 to take the value of cell 0:0
		Spreadsheet s = new Spreadsheet(2, 2);
		s.set(0, 0, -3);
		s.set(1, 0, 1);
		CellFormula cf = new CellFormula(Cell.get(0, 1), new ValueOf(Cell.get(0, 0)));
		ApplyFormula af = new ApplyFormula(1, cf);
		Spreadsheet out_s = (Spreadsheet) af.evaluate(s)[0];
		assertNotEquals(s, out_s);
		assertEquals(-3, s.get(0, 0));
		assertEquals(1, s.get(1, 0));
		assertEquals(null, s.get(0, 1));
		assertEquals(-3, out_s.get(0, 0));
		assertEquals(1, out_s.get(1, 0));
		assertEquals(-3, out_s.get(0, 1));
		{
			PartNode root = af.getExplanation(NthOutput.FIRST);
			assertEquals(1, root.getOutputLinks(0).size());
			PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
			Part child_part = child.getPart();
			assertEquals(NthInput.FIRST, child_part);
		}
	}

	@Test
	public void test1()
	{
		// Sets cell 0:1 to take the value of cell 0:0
		Spreadsheet s = new Spreadsheet(2, 2);
		s.set(0, 0, -3);
		s.set(1, 0, 1);
		CellFormula cf = new CellFormula(Cell.get(0, 1), new ValueOf(Cell.get(0, 0)));
		ApplyFormula af = new ApplyFormula(1, cf);
		Spreadsheet out_s = (Spreadsheet) af.evaluate(s)[0];
		assertNotEquals(s, out_s);
		assertEquals(-3, s.get(0, 0));
		assertEquals(1, s.get(1, 0));
		assertEquals(null, s.get(0, 1));
		assertEquals(-3, out_s.get(0, 0));
		assertEquals(1, out_s.get(1, 0));
		assertEquals(-3, out_s.get(0, 1));
		{
			PartNode root = af.getExplanation(ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST));
			assertEquals(1, root.getOutputLinks(0).size());
			PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
			Part child_part = child.getPart();
			assertEquals(ComposedPart.compose(Cell.get(0, 0), NthInput.FIRST), child_part);
		}
		{
			PartNode root = af.getExplanation(ComposedPart.compose(Cell.get(0, 1), NthOutput.FIRST));
			assertEquals(1, root.getOutputLinks(0).size());
			NestedNode nn = (NestedNode) root.getOutputLinks(0).get(0).getNode();
			assertEquals(1, nn.getOutputLinks(0).size());
			PartNode child_1 = (PartNode) nn.getOutputLinks(0).get(0).getNode();
			Part child_part = child_1.getPart();
			assertEquals(ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST), child_part);
		}
	}

	@Test
	public void test2()
	{
		// Sets cell 0:1 to take the value of cell 0:0 plus one
		Spreadsheet s = new Spreadsheet(2, 2);
		s.set(0, 0, -3);
		s.set(1, 0, 1);
		Circuit fc = new Circuit(1, 1);
		{
			ValueOf v = new ValueOf(Cell.get(0, 0));
			Constant one = new Constant(1);
			Addition add = new Addition(2);
			connect(v, 0, add, 0);
			connect(one, 0, add, 1);
			fc.associateInput(0, v.getInputPin(0));
			fc.associateOutput(0, add.getOutputPin(0));
		}
		CellFormula cf = new CellFormula(Cell.get(0, 1), fc);
		ApplyFormula af = new ApplyFormula(1, cf);
		Spreadsheet out_s = (Spreadsheet) af.evaluate(s)[0];
		assertEquals(-3, out_s.get(0, 0));
		assertEquals(1, out_s.get(1, 0));
		assertEquals(-2f, out_s.get(0, 1));
		{
			PartNode root = af.getExplanation(ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST));
			assertEquals(1, root.getOutputLinks(0).size());
			PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
			Part child_part = child.getPart();
			assertEquals(ComposedPart.compose(Cell.get(0, 0), NthInput.FIRST), child_part);
		}
		{
			PartNode root = af.getExplanation(ComposedPart.compose(Cell.get(0, 1), NthOutput.FIRST));
			assertEquals(1, root.getOutputLinks(0).size());
			NestedNode nn = (NestedNode) root.getOutputLinks(0).get(0).getNode();
			assertEquals(1, nn.getOutputArity());
			assertEquals(1, nn.getOutputLinks(0).size());
			PartNode child_1 = (PartNode) nn.getOutputLinks(0).get(0).getNode();			
			Part child_part1 = child_1.getPart();
			assertEquals(ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST), child_part1);
		}
	}

	@Test
	public void test3()
	{
		// Sets cell 0:1 to take the value of cell 0:0 plus cell 1:0
		Spreadsheet s = new Spreadsheet(2, 2);
		s.set(0, 0, -3);
		s.set(1, 0, 2);
		Circuit fc = new Circuit(1, 1);
		{
			Fork f = new Fork(2);
			ValueOf v1 = new ValueOf(Cell.get(0, 0));
			ValueOf v2 = new ValueOf(Cell.get(1, 0));
			connect(f, 0, v1, 0);
			connect(f, 1, v2, 0);
			Addition add = new Addition(2);
			connect(v1, 0, add, 0);
			connect(v2, 0, add, 1);
			fc.associateInput(0, f.getInputPin(0));
			fc.associateOutput(0, add.getOutputPin(0));
		}
		CellFormula cf = new CellFormula(Cell.get(0, 1), fc);
		ApplyFormula af = new ApplyFormula(1, cf);
		Spreadsheet out_s = (Spreadsheet) af.evaluate(s)[0];
		assertEquals(-3, out_s.get(0, 0));
		assertEquals(2, out_s.get(1, 0));
		assertEquals(-1f, out_s.get(0, 1));
		{
			PartNode root = af.getExplanation(ComposedPart.compose(Cell.get(0, 1), NthOutput.FIRST));
			assertEquals(1, root.getOutputLinks(0).size());
			NestedNode nn = (NestedNode) root.getOutputLinks(0).get(0).getNode();
			assertEquals(2, nn.getOutputArity());
			assertEquals(1, nn.getOutputLinks(0).size());
			PartNode child_1 = (PartNode) nn.getOutputLinks(0).get(0).getNode();
			assertEquals(1, nn.getOutputLinks(1).size());
			Part child_part1 = child_1.getPart();
			assertEquals(ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST), child_part1);
			PartNode child_2 = (PartNode) nn.getOutputLinks(1).get(0).getNode();
			Part child_part2 = child_2.getPart();
			assertEquals(ComposedPart.compose(Cell.get(1, 0), NthOutput.FIRST), child_part2);
		}
	}

	@Test
	public void test4()
	{
		// Sets cell 0:1 to take the value of cell 0:0 in spreadsheet 1
		// plus cell 0:0 in spreadsheet 2
		Spreadsheet s1 = new Spreadsheet(2, 2);
		s1.set(0, 0, -3);
		s1.set(1, 0, 2);
		Spreadsheet s2 = new Spreadsheet(2, 2);
		s2.set(0, 0, 5);
		s2.set(0, 1, 7);
		Circuit fc = new Circuit(2, 1);
		{
			ValueOf v1 = new ValueOf(Cell.get(0, 0));
			ValueOf v2 = new ValueOf(Cell.get(0, 0));
			Addition add = new Addition(2);
			connect(v1, 0, add, 0);
			connect(v2, 0, add, 1);
			fc.associateInput(0, v1.getInputPin(0));
			fc.associateInput(1, v2.getInputPin(0));
			fc.associateOutput(0, add.getOutputPin(0));
		}
		CellFormula cf = new CellFormula(Cell.get(0, 1), fc).associate(0, 0).associate(1, 1);
		ApplyFormula af = new ApplyFormula(2, cf);
		Spreadsheet out_s = (Spreadsheet) af.evaluate(s1, s2)[0];
		assertEquals(-3, out_s.get(0, 0));
		assertEquals(2, out_s.get(1, 0));
		assertEquals(2f, out_s.get(0, 1));
		{
			PartNode root = af.getExplanation(ComposedPart.compose(Cell.get(0, 1), NthOutput.FIRST));
			assertEquals(1, root.getOutputLinks(0).size());
			NestedNode nn = (NestedNode) root.getOutputLinks(0).get(0).getNode();
			assertEquals(2, nn.getOutputArity());
			assertEquals(1, nn.getOutputLinks(0).size());
			PartNode child_1 = (PartNode) nn.getOutputLinks(0).get(0).getNode();
			assertEquals(1, nn.getOutputLinks(1).size());
			Part child_part1 = child_1.getPart();
			assertEquals(ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST), child_part1);
			PartNode child_2 = (PartNode) nn.getOutputLinks(1).get(0).getNode();
			Part child_part2 = child_2.getPart();
			assertEquals(ComposedPart.compose(Cell.get(0, 0), NthInput.SECOND), child_part2);
		}
	}

	@Test
	public void test5()
	{
		// Sets cell 0:1 to take the value of cell 0:0
		// Sets cell 0:0 to take the value of cell 1:0
		Spreadsheet s1 = new Spreadsheet(2, 2);
		s1.set(1, 0, 2);
		CellFormula cf1 = new CellFormula(Cell.get(0, 0), new ValueOf(Cell.get(1, 0)));
		CellFormula cf2 = new CellFormula(Cell.get(0, 1), new ValueOf(Cell.get(0, 0)));
		ApplyFormula af = new ApplyFormula(1, cf1, cf2);
		Spreadsheet out_s = (Spreadsheet) af.evaluate(s1)[0];
		assertEquals(2, out_s.get(0, 0));
		assertEquals(2, out_s.get(1, 0));
		assertEquals(2, out_s.get(0, 1));
		{
			// 0:0 is explained by the value of 1:0
			PartNode root = af.getExplanation(ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST));
			assertEquals(1, root.getOutputLinks(0).size());
			NestedNode nn = (NestedNode) root.getOutputLinks(0).get(0).getNode();
			assertEquals(1, nn.getOutputArity());
			assertEquals(1, nn.getOutputLinks(0).size());
			PartNode child_1 = (PartNode) nn.getOutputLinks(0).get(0).getNode();
			Part child_part1 = child_1.getPart();
			assertEquals(ComposedPart.compose(Cell.get(1, 0), NthOutput.FIRST), child_part1);
		}
		{
			// 0:1 is explained by the value of 0:0, which in turn is explained
			// by the value of 1:0
			PartNode root = af.getExplanation(ComposedPart.compose(Cell.get(0, 1), NthOutput.FIRST));
			assertEquals(1, root.getOutputLinks(0).size());
			NestedNode nn1 = (NestedNode) root.getOutputLinks(0).get(0).getNode();
			assertEquals(1, nn1.getOutputArity());
			assertEquals(1, nn1.getOutputLinks(0).size());
			PartNode child_1 = (PartNode) nn1.getOutputLinks(0).get(0).getNode();
			Part child_part1 = child_1.getPart();
			assertEquals(ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST), child_part1);
			PartNode child_2 = (PartNode) nn1.getOutputLinks(0).get(0).getNode();
			Part child_part2 = child_2.getPart();
			assertEquals(ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST), child_part2);
			NestedNode nn2 = (NestedNode) child_2.getOutputLinks(0).get(0).getNode();
			assertEquals(1, nn2.getOutputArity());
			assertEquals(1, nn2.getOutputLinks(0).size());
			PartNode child_3 = (PartNode) nn2.getOutputLinks(0).get(0).getNode();
			Part child_part3 = child_3.getPart();
			assertEquals(ComposedPart.compose(Cell.get(1, 0), NthOutput.FIRST), child_part3);
		}
	}
}

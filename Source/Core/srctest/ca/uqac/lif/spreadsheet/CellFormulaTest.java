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
package ca.uqac.lif.spreadsheet;

import static org.junit.Assert.*;

import org.junit.Test;

import static ca.uqac.lif.dag.NodeConnector.connect;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.Constant;
import ca.uqac.lif.petitpoucet.function.Fork;
import ca.uqac.lif.petitpoucet.function.number.Addition;

public class CellFormulaTest
{
	@Test
	public void test1()
	{
		// Sets cell 0:1 to take the value of cell 0:0
		Spreadsheet s = new Spreadsheet(2, 2);
		s.set(0, 0, -3);
		s.set(1, 0, 1);
		CellFormula cf = new CellFormula(Cell.get(0, 1), new ValueOf(Cell.get(0, 0)));
		cf.evaluate(s);
		assertEquals(-3, s.get(0, 0));
		assertEquals(1, s.get(1, 0));
		assertEquals(-3, s.get(0, 1));
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
		cf.evaluate(s);
		assertEquals(-3, s.get(0, 0));
		assertEquals(1, s.get(1, 0));
		assertEquals(-2f, s.get(0, 1));
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
		cf.evaluate(s);
		assertEquals(-3, s.get(0, 0));
		assertEquals(2, s.get(1, 0));
		assertEquals(-1f, s.get(0, 1));
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
		cf.evaluate(s1, s2);
		assertEquals(-3, s1.get(0, 0));
		assertEquals(2, s1.get(1, 0));
		assertEquals(2f, s1.get(0, 1));
	}
}

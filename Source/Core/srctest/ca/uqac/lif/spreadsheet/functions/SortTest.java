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

import static ca.uqac.lif.spreadsheet.functions.SpreadsheetFunctionTest.assertExplains;
import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

public class SortTest
{
	@Test
	public void test1()
	{
		Spreadsheet s = Spreadsheet.read(2, 5,
				"A", "B",
				0, 0,
				0, 1,
				1, 0,
				1, 1);
		Sort f = new Sort().by(0);
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertFalse(s == out);
		assertTrue(s.equals(out));
	}

	@Test
	public void test2()
	{
		Spreadsheet s = Spreadsheet.read(2, 5,
				"A", "B",
				0, 0,
				0, 1,
				1, 0,
				1, 1);
		Sort f = new Sort().by(0).excludeFirst();
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertFalse(s == out);
		assertTrue(s.equals(out));
	}

	@Test
	public void test3()
	{
		Spreadsheet s = Spreadsheet.read(2, 5,
				"A", "B",
				3, "a",
				1, "b",
				4, "c",
				2, "d");
		Sort f = new Sort().by(0).excludeFirst();
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		Spreadsheet expected = Spreadsheet.read(2, 5, 
				"A", "B",
				1, "b",
				2, "d",
				3, "a",
				4, "c");
		assertFalse(s == out);
		assertEquals(expected, out);
	}

	@Test
	public void test4()
	{
		Spreadsheet s = Spreadsheet.read(2, 5,
				"A", "B",
				3, "a",
				1, "b",
				4, "c",
				2, "d");
		Sort f = new Sort().by(0, false).excludeFirst();
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		Spreadsheet expected = Spreadsheet.read(2, 5, 
				"A", "B",
				4, "c",
				3, "a",
				2, "d",
				1, "b");
		assertFalse(s == out);
		assertEquals(expected, out);
		assertExplains(f, ComposedPart.compose(Cell.get(0, 3), NthOutput.FIRST), ComposedPart.compose(Cell.get(0, 4), NthInput.FIRST));
		assertExplains(f, ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST), ComposedPart.compose(Cell.get(0, 0), NthInput.FIRST));
	}
	
	@Test
	public void test5()
	{
		Spreadsheet s = Spreadsheet.read(3, 7,
				"A", "B", "C",
				3, "a", 50,
				1, "b", 10,
				4, "c", 20,
				1, "a", 60,
				3, "f", 50,
				2, "d", 30);
		Sort f = new Sort().by(0).by(1).excludeFirst();
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		Spreadsheet expected = Spreadsheet.read(3, 7, 
				"A", "B", "C",
				1, "a", 60,
				1, "b", 10,
				2, "d", 30,
				3, "a", 50,
				3, "f", 50,
				4, "c", 20);
		assertFalse(s == out);
		assertEquals(expected, out);
		assertExplains(f, ComposedPart.compose(Cell.get(0, 3), NthOutput.FIRST), ComposedPart.compose(Cell.get(0, 6), NthInput.FIRST));
		assertExplains(f, ComposedPart.compose(Cell.get(0, 0), NthOutput.FIRST), ComposedPart.compose(Cell.get(0, 0), NthInput.FIRST));
	}
}

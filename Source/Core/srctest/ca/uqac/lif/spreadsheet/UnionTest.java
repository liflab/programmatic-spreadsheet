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

import static ca.uqac.lif.spreadsheet.SpreadsheetFunctionTest.assertExplains;
import static ca.uqac.lif.spreadsheet.SpreadsheetFunctionTest.assertNotExplains;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;

public class UnionTest
{
	@Test
	public void test1()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 3,
				3, 1, 4,
				1, 5, 9,
				2, 6, 5);
		Spreadsheet s2 = Spreadsheet.read(2, 4,
				2, 7,
				1, 8,
				2, 8,
				1, 8);
		Union f = new Union();
		Spreadsheet out = (Spreadsheet) f.evaluate(s1, s2)[0];
		assertEquals(Spreadsheet.read(3, 7,
				3, 1, 4,
				1, 5, 9,
				2, 6, 5,
				2, 7, null,
				1, 8, null,
				2, 8, null,
				1, 8, null), out);
		assertExplains(f, ComposedPart.compose(Cell.get(2, 1), NthOutput.FIRST), ComposedPart.compose(Cell.get(2, 1), NthInput.FIRST));
		assertExplains(f, ComposedPart.compose(Cell.get(1, 3), NthOutput.FIRST), ComposedPart.compose(Cell.get(1, 0), NthInput.SECOND));
		assertNotExplains(f, ComposedPart.compose(Cell.get(2, 3), NthOutput.FIRST));
	}
	
	@Test
	public void test2()
	{
		Spreadsheet s1 = Spreadsheet.read(2, 5,
				"A", "B",
				2, 7,
				1, 8,
				2, 8,
				1, 8);
		Spreadsheet s2 = Spreadsheet.read(3, 4,
				"C", "D", "E",
				3, 1, 4,
				1, 5, 9,
				2, 6, 5);
		Union f = new Union().excludeFirst();
		Spreadsheet out = (Spreadsheet) f.evaluate(s1, s2)[0];
		assertEquals(Spreadsheet.read(3, 8,
				"C", "D", "E",
				2, 7, null,
				1, 8, null,
				2, 8, null,
				1, 8, null,
				3, 1, 4,
				1, 5, 9,
				2, 6, 5), out);
		assertExplains(f, ComposedPart.compose(Cell.get(2, 0), NthOutput.FIRST), ComposedPart.compose(Cell.get(2, 0), NthInput.SECOND));
		assertExplains(f, ComposedPart.compose(Cell.get(0, 1), NthOutput.FIRST), ComposedPart.compose(Cell.get(0, 1), NthInput.FIRST));
		assertExplains(f, ComposedPart.compose(Cell.get(1, 5), NthOutput.FIRST), ComposedPart.compose(Cell.get(1, 1), NthInput.SECOND));
		assertNotExplains(f, ComposedPart.compose(Cell.get(2, 3), NthOutput.FIRST));
	}
}

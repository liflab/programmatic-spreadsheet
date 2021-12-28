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
import static ca.uqac.lif.spreadsheet.functions.SpreadsheetFunctionTest.assertNotExplains;
import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.Merge;

public class MergeTest
{
	@Test
	public void test1()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 4,
				"A", "B", "C",
				3, 1, 4,
				1, 5, 9,
				2, 6, 5
				);
		Spreadsheet s2 = Spreadsheet.read(3, 4,
				"A", "D", "E",
				3, 2, 7,
				1, 1, 8,
				2, 2, 8
				);
		Merge f = new Merge().excludeFirst();
		Spreadsheet out = (Spreadsheet) f.evaluate(s1, s2)[0];
		assertEquals(Spreadsheet.read(5, 4,
				"A", "B", "C", "D", "E",
				3, 1, 4, 2, 7,
				1, 5, 9, 1, 8,
				2, 6, 5, 2, 8
				), out);
		assertExplains(f, ComposedPart.compose(Cell.get(2, 0), NthOutput.FIRST), ComposedPart.compose(Cell.get(2, 0), NthInput.FIRST));
		assertExplains(f, ComposedPart.compose(Cell.get(3, 0), NthOutput.FIRST), ComposedPart.compose(Cell.get(1, 0), NthInput.SECOND));
		assertExplains(f, ComposedPart.compose(Cell.get(3, 1), NthOutput.FIRST), ComposedPart.compose(Cell.get(1, 1), NthInput.SECOND));
	}
	
	@Test
	public void test2()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 4,
				"A", "B", "C",
				3, 1, 4,
				1, 5, 9,
				2, 6, 5
				);
		Spreadsheet s2 = Spreadsheet.read(3, 3,
				"A", "D", "E",
				3, 2, 7,
				1, 1, 8
				);
		Merge f = new Merge().excludeFirst();
		Spreadsheet out = (Spreadsheet) f.evaluate(s1, s2)[0];
		assertEquals(Spreadsheet.read(5, 4,
				"A", "B", "C", "D", "E",
				3, 1, 4, 2, 7,
				1, 5, 9, 1, 8,
				2, 6, 5, null, null
				), out);
		assertNotExplains(f, ComposedPart.compose(Cell.get(3, 3), NthOutput.FIRST));
	}
}

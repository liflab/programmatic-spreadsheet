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

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import static ca.uqac.lif.spreadsheet.functions.SpreadsheetFunctionTest.assertExplains;

/**
 * Unit tests for {@link BoxStats}.
 */
public class BoxStatsTest
{
	@Test
	public void test1()
	{
		Spreadsheet s1 = Spreadsheet.read(3, 7,
				"A", "B", "C",
				3, 1, 4,
				1, 5, 9,
				2, 6, 5,
				5, 2, 7,
				3, 7, 12,
				6, 8, 9
				);
		BoxStats f = new BoxStats();
		Spreadsheet out = (Spreadsheet) f.evaluate(s1)[0];
		assertNotNull(out);
		assertEquals(7, out.getWidth());
		assertEquals(4, out.getHeight());
		assertEquals(Spreadsheet.read(7, 4, 
				"x", "Min", "Q1", "Q2", "Q3", "Max", "Label",
				0,   1,     1,    3,    3,    6,     "A",
				1,   1,     1,    5,    6,    8,     "B",
				2,   4,     4,    7,    9,    12,    "C"), out);
		assertExplains(f, ComposedPart.compose(Cell.get(1, 1), NthOutput.FIRST), ComposedPart.compose(Cell.get(0,2), NthInput.FIRST));
		assertExplains(f, ComposedPart.compose(Cell.get(5, 1), NthOutput.FIRST), ComposedPart.compose(Cell.get(0,6), NthInput.FIRST));
		assertExplains(f, ComposedPart.compose(Cell.get(4, 2), NthOutput.FIRST), ComposedPart.compose(Cell.get(1,3), NthInput.FIRST));
		assertExplains(f, ComposedPart.compose(Cell.get(6, 3), NthOutput.FIRST), ComposedPart.compose(Cell.get(2,0), NthInput.FIRST));
	}
}

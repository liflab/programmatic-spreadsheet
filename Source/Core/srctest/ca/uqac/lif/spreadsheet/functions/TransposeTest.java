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

import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Unit tests for {@link Transpose}.
 */
public class TransposeTest
{
	@Test
	public void test1()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"A", "B", "C",
				1,   2,   3,
				4,   5,   6,
				7,   8,   9);
		Transpose f = new Transpose();
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertNotNull(out);
		assertEquals(Spreadsheet.read(4, 3,
				"A", 1, 4, 7,
				"B", 2, 5, 8,
				"C", 3, 6, 9), out);
	}
}

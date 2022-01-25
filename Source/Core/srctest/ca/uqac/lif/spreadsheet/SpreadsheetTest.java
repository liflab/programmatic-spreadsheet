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

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.strings.Position;
import ca.uqac.lif.petitpoucet.function.strings.PositionRange;

/**
 * Unit tests for {@link Spreadsheet}.
 */
public class SpreadsheetTest
{
	/**
	 * The OS-dependent line separator.
	 */
	public static final String CRLF = System.lineSeparator();
	
	/**
	 * The length of CRLF.
	 */
	public static final int CRLF_S = CRLF.length();
	
	@Test
	public void testSetGet1()
	{
		Spreadsheet s = new Spreadsheet(10, 5);
		s.set(2, 3, "foo");
		assertEquals("foo", s.get(2, 3));
		assertEquals(null, s.get(1, 1));
	}
	
	@Test(expected = SpreadsheetOutOfBoundsException.class)
	public void testSetGet2()
	{
		Spreadsheet s = new Spreadsheet(10, 5);
		s.set(20, 3, "foo");
	}
	
	@Test(expected = SpreadsheetOutOfBoundsException.class)
	public void testSetGet3()
	{
		Spreadsheet s = new Spreadsheet(10, 5);
		s.set(2, 30, "foo");
	}
	
	@Test(expected = SpreadsheetOutOfBoundsException.class)
	public void testSetGet4()
	{
		Spreadsheet s = new Spreadsheet(10, 5);
		s.set(-10, 3, "foo");
	}
	
	@Test(expected = SpreadsheetOutOfBoundsException.class)
	public void testSetGet5()
	{
		Spreadsheet s = new Spreadsheet(10, 5);
		s.set(0, -10, "foo");
	}
	
	@Test
	public void testSetGet6()
	{
		Spreadsheet s = new Spreadsheet(2, 3);
		assertEquals(3, s.getHeight());
		assertEquals(2, s.getWidth());
		s.set(0, 0, "A");
		s.set(1, 0, "B");
		s.set(0, 1, 3);
		s.set(1, 1, 1);
		s.set(0, 2, 4);
		s.set(1, 2, 1);
	}
	
	@Test
	public void testCompare1()
	{
		assertTrue(Spreadsheet.compare(1, 2) < 0);
		assertTrue(Spreadsheet.compare("abc", "def") < 0);
		assertTrue(Spreadsheet.compare("abc", 12) == 0);
	}
	
	@Test
	public void testEquals1()
	{
		Spreadsheet s1 = Spreadsheet.read(2, 3,
				"foo", "bar",
				1, 2,
				null, null);
		Spreadsheet s2 = Spreadsheet.read(2, 3,
				"foo", "baz",
				1, 2,
				null, null);
		assertTrue(s1.equals(s1));
		assertTrue(s2.equals(s2));
		assertFalse(s1.equals(s2));
	}
	
	@Test
	public void testRead1()
	{
		Map<Cell,Part> mapping = new HashMap<Cell,Part>();
		Scanner scanner = new Scanner(
				"a  b   foo" + CRLF + 
				"0  1   true" + CRLF +
				" # Commented line to be ignored   " + CRLF +
				"  2  3.5 null" + CRLF);
		Spreadsheet out = Spreadsheet.read(scanner, "#", "\\s+", mapping);
		assertNotNull(out);
		assertEquals(Spreadsheet.read(3, 3,
				"a", "b", "foo",
				0, 1, true,
				2, 3.5, null), out);
		assertEquals(ComposedPart.compose(new PositionRange(new Position(0, 0), new Position(0, 0)), NthInput.FIRST), mapping.get(Cell.get(0, 0)));
		assertEquals(ComposedPart.compose(new PositionRange(new Position(0, 7), new Position(0, 9)), NthInput.FIRST), mapping.get(Cell.get(2, 0)));
		assertEquals(ComposedPart.compose(new PositionRange(new Position(3, 2), new Position(3, 2)), NthInput.FIRST), mapping.get(Cell.get(0, 2)));
		assertEquals(ComposedPart.compose(new PositionRange(new Position(3, 9), new Position(3, 12)), NthInput.FIRST), mapping.get(Cell.get(2, 2)));
	}
	
	@Test
	public void testRead2()
	{
		Map<Cell,Part> mapping = new HashMap<Cell,Part>();
		Scanner scanner = new Scanner(
				"a\tb\tfoo" + CRLF + 
				"0\t1\ttrue" + CRLF +
				" # Commented line to be ignored   " + CRLF +
				"  2\t3.5\tnull" + CRLF);
		Spreadsheet out = Spreadsheet.read(scanner, "#", "\\s+", mapping);
		assertNotNull(out);
		assertEquals(Spreadsheet.read(3, 3,
				"a", "b", "foo",
				0, 1, true,
				2, 3.5, null), out);
		assertEquals(ComposedPart.compose(new PositionRange(new Position(0, 0), new Position(0, 0)), NthInput.FIRST), mapping.get(Cell.get(0, 0)));
		assertEquals(ComposedPart.compose(new PositionRange(new Position(0, 7), new Position(0, 9)), NthInput.FIRST), mapping.get(Cell.get(2, 0)));
		assertEquals(ComposedPart.compose(new PositionRange(new Position(3, 2), new Position(3, 2)), NthInput.FIRST), mapping.get(Cell.get(0, 2)));
		assertEquals(ComposedPart.compose(new PositionRange(new Position(3, 9), new Position(3, 12)), NthInput.FIRST), mapping.get(Cell.get(2, 2)));
	}
}

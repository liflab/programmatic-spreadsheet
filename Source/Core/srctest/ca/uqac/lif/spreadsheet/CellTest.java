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
package ca.uqac.lif.spreadsheet;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for {@link Cell}.
 */
public class CellTest
{
	@Test
	public void testGetCell1()
	{
		Cell c = Cell.get("A1");
		assertNotNull(c);
		assertEquals(0, c.getColumn());
		assertEquals(0, c.getRow());
	}
	
	@Test
	public void testGetCell2()
	{
		Cell c = Cell.get("B5");
		assertNotNull(c);
		assertEquals(1, c.getColumn());
		assertEquals(4, c.getRow());
	}
	
	@Test
	public void testGetCell3()
	{
		Cell c = Cell.get("BC9");
		assertNotNull(c);
		assertEquals(54, c.getColumn());
		assertEquals(8, c.getRow());
	}
	
	@Test (expected = SpreadsheetCellNameException.class)
	public void testGetCell4()
	{
		Cell.get("BC");
	}
	
	@Test (expected = SpreadsheetCellNameException.class)
	public void testGetCell5()
	{
		Cell.get("34");
	}
	
	@Test
	public void testGetColumnNumber1()
	{
		assertEquals(1, Cell.getColumnNumber("B"));
	}
	
	@Test
	public void testGetColumnNumber2()
	{
		assertEquals(26, Cell.getColumnNumber("AA"));
	}
	
	@Test
	public void testGetColumnNumber3()
	{
		assertEquals(52, Cell.getColumnNumber("BA"));
	}
}

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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

public class AnsiSpreadsheetPrinterTest
{
	@Test
	public void test1()
	{
		Spreadsheet s = new Spreadsheet(2, 3);
		s.set(0, 0, "A");
		s.set(1, 0, "B");
		s.set(0, 1, 3);
		s.set(1, 1, 1);
		s.set(0, 2, 4);
		s.set(1, 2, 1);
		AnsiSpreadsheetPrinter p = new AnsiSpreadsheetPrinter();
		List<String> lines = getLines(s, p);
		assertEquals(3, lines.size());
		assertEquals("A B", lines.get(0));
		assertEquals("3 1", lines.get(1));
		assertEquals("4 1", lines.get(2));
	}
	
	@Test
	public void test2()
	{
		Spreadsheet s = new Spreadsheet(2, 3);
		s.set(0, 0, "A");
		s.set(1, 0, "B");
		s.set(0, 1, 3);
		s.set(1, 1, 1);
		s.set(0, 2, 4);
		s.set(1, 2, 1);
		AnsiSpreadsheetPrinter p = new AnsiSpreadsheetPrinter();
		p.setColumnSeparator(" | ");
		List<String> lines = getLines(s, p);
		assertEquals(3, lines.size());
		assertEquals("A | B", lines.get(0));
		assertEquals("3 | 1", lines.get(1));
		assertEquals("4 | 1", lines.get(2));
	}
	
	@Test
	public void test3()
	{
		Spreadsheet s = new Spreadsheet(2, 3);
		s.set(0, 0, "A");
		s.set(1, 0, "B");
		s.set(0, 1, 35);
		s.set(1, 1, 1);
		s.set(0, 2, 4);
		s.set(1, 2, 123);
		AnsiSpreadsheetPrinter p = new AnsiSpreadsheetPrinter();
		p.setColumnSeparator(" | ");
		List<String> lines = getLines(s, p);
		assertEquals(3, lines.size());
		assertEquals("A  | B  ", lines.get(0));
		assertEquals("35 | 1  ", lines.get(1));
		assertEquals("4  | 123", lines.get(2));
	}
	
	@Test
	public void test4()
	{
		Spreadsheet s = new Spreadsheet(2, 3);
		s.set(0, 0, "A");
		s.set(1, 0, "B");
		s.set(0, 1, "abcdefghij");
		s.set(1, 1, 1);
		s.set(0, 2, 4);
		s.set(1, 2, "klmnopqrstuvw");
		AnsiSpreadsheetPrinter p = new AnsiSpreadsheetPrinter();
		p.setColumnSeparator(" | ");
		p.setMaxWidth(20);
		List<String> lines = getLines(s, p);
		assertEquals(3, lines.size());
		assertEquals("A       | B         ", lines.get(0));
		assertEquals("abcdefg | 1         ", lines.get(1));
		assertEquals("4       | klmnopqrst", lines.get(2));
	}
	
	@Test
	public void test5()
	{
		Spreadsheet s = new Spreadsheet(3, 5);
		s.set(0, 0, "A");
		s.set(1, 0, "B");
		s.set(2, 0, "C");
		s.set(0, 1, 3);
		s.set(1, 1, 1);
		s.set(2, 1, 4);
		s.set(0, 2, 3);
		s.set(1, 2, 2);
		s.set(2, 2, 4);
		s.set(0, 3, 3);
		s.set(1, 3, 2);
		s.set(2, 3, 8);
		s.set(0, 4, 5);
		s.set(1, 4, 1);
		s.set(2, 4, 4);
		AnsiSpreadsheetPrinter p = new AnsiSpreadsheetPrinter();
		p.setColumnSeparator(" | ");
		p.setGroupCells(true);
		List<String> lines = getLines(s, p);
		assertEquals(5, lines.size());
		assertEquals("A | B | C", lines.get(0));
		assertEquals("3 | 1 | 4", lines.get(1));
		assertEquals("- | 2 | -", lines.get(2));
		assertEquals("- | - | 8", lines.get(3));
		assertEquals("5 | 1 | 4", lines.get(4));
	}
	
	public static List<String> getLines(Spreadsheet spreadsheet, SpreadsheetPrinter printer)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		printer.print(spreadsheet, ps);
		List<String> lines = new ArrayList<String>();
		Scanner scanner = new Scanner(baos.toString());
		while (scanner.hasNextLine())
		{
			lines.add(scanner.nextLine());
		}
		scanner.close();
		return lines;
	}
}

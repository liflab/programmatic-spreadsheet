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
package examples.spreadsheet;

import ca.uqac.lif.spreadsheet.AnsiSpreadsheetPrinter;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Demonstrates the ability of the {@link AnsiSpreadsheetPrinter} to
 * pretty-print the contents of a spreadsheet as clear text. In particular,
 * it shows how the printer takes care of giving the same horizontal space
 * to each column on each line.
 * <p>
 * The printed spreadsheet looks like this:
 * <p>
 * <pre> A    B    C D     E    
 * 1    2    3 4     5    
 * 1    3    5 6     7    
 * null null 3 4     a &lt; b
 * 6    7    8 9     a &lt; b
 * 7    8    9 a &lt; b a &lt; b</pre>
 */
public class AnsiPrint
{
	public static void main(String[] args)
	{
		Spreadsheet s = Spreadsheet.read(5, 6,
				"A",    "B",  "C", "D",     "E",
				1,      2,    3,   4,       5,
				1,      3,    5,   6,       7,
				null,   null, 3,   4,       "a < b",
				6,      7,    8,   9,       "a < b",
				7,      8,    9,   "a < b", "a < b"
				);
		AnsiSpreadsheetPrinter printer = new AnsiSpreadsheetPrinter();
		printer.print(s, System.out);
	}
}

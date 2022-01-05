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

import ca.uqac.lif.spreadsheet.HtmlSpreadsheetPrinter;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Demonstrates the ability of the {@link HtmlSpreadsheetPrinter} to
 * pretty-print the contents of a spreadsheet as HTML markup. In particular,
 * this example shows how the
 * {@link HtmlSpreadsheetPrinter#mergeCells(boolean) mergeCells} method
 * can group adjacent cells with the same value into a single cell spanning
 * multiple rows or multiple columns.
 * <p>
 * When displayed in a web browser, the printed spreadsheet looks like this:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th><th>D</th><th>E</th></tr>
 * </thead>
 * <tr><td rowspan="2">1</td><td>2</td><td>3</td><td>4</td><td>5</td></tr>
 * <tr><td>3</td><td>5</td><td>6</td><td>7</td></tr>
 * <tr><td colspan="2"></td><td>3</td><td>4</td><td rowspan="3">a &lt; b</td></tr>
 * <tr><td>6</td><td>7</td><td>8</td><td>9</td></tr>
 * <tr><td>7</td><td>8</td><td>9</td><td>a &lt; b</td></tr>
 * </table>
 */
public class HtmlPrint
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
		HtmlSpreadsheetPrinter printer = new HtmlSpreadsheetPrinter().mergeCells(true);
		printer.print(s, System.out);
	}
}

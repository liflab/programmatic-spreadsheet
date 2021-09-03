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

import java.io.PrintStream;

/**
 * Interface implemented by classes that can print the contents of a
 * spreadsheet in some way.
 * @author Sylvain Hallé
 *
 */
public interface SpreadsheetPrinter
{
	/**
	 * Prints the contents of a spreadsheet.
	 * @param s The spreadsheet to print
	 * @param ps The PrintStream where the spreadsheet is printed
	 */
	public void print(/*@ non_null @*/ Spreadsheet s, /*@ non_null @*/ PrintStream ps);
}

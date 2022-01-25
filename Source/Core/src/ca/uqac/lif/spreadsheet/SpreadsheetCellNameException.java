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

/**
 * Exception thrown when using a spreadsheet name that does not follow
 * syntactical conventions.
 * @author Sylvain Hallé
 * @see Cell#get(String)
 */
public class SpreadsheetCellNameException extends RuntimeException
{
	/**
	 * Dummy UID
	 */
	private static final long serialVersionUID = 1L;
	
	public SpreadsheetCellNameException(String message)
	{
		super(message);
	}
	
	public SpreadsheetCellNameException(Throwable t)
	{
		super(t);
	}

}

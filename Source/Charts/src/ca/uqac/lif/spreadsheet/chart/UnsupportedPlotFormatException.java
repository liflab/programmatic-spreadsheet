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
package ca.uqac.lif.spreadsheet.chart;

/**
 * Exception indicating that a plot is asked to be rendered in a format that it
 * does not support.
 */
public class UnsupportedPlotFormatException extends RuntimeException
{
	/**
	 * Dummy UID.
	 */
	private static final long serialVersionUID = 1L;
	
	public UnsupportedPlotFormatException(Throwable t)
	{
		super(t);
	}
	
	public UnsupportedPlotFormatException(String message)
	{
		super(message);
	}
}

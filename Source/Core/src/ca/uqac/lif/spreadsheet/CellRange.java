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

/**
 * Utility class for manipulating cell ranges.
 * @author Sylvain Hallé
 */
public class CellRange
{
	/**
	 * Creates an array of cells corresponding to a range of cells.
	 * @param col1 The column of the top-left cell in the range
	 * @param row1 The row of the top-left cell in the range
	 * @param col2 The column of the bottom-right cell in the range
	 * @param row2 The row of the bottom-right cell in the range
	 * @return An array of {@link Cell} objects corresponding to all the cells
	 * comprised in the specified range
	 */
	public static Cell[] get(int col1, int row1, int col2, int row2)
	{
		Cell[] out = new Cell[(col2 - col1 + 1) * (row2 - row1 + 1)];
		int index = 0;
		for (int col = col1; col <= col2; col++)
		{
			for (int row = row1; row <= row2; row++)
			{
				out[index++] = Cell.get(col, row);
			}
		}
		return out;
	}
	
	/**
	 * Creates an array of cells corresponding to a range of cells in a single
	 * row.
	 * @param row The row
	 * @param col1 The column of the leftmost cell in the range
	 * @param col2 The column of the rightmost cell in the range
	 * @return An array of {@link Cell} objects corresponding to all the cells
	 * comprised in the specified range
	 */
	public static Cell[] getRow(int row, int col1, int col2)
	{
		return get(col1, row, col2, row);
	}
	
	/**
	 * Creates an array of cells corresponding to a range of cells in a single
	 * column.
	 * @param col The column
	 * @param row1 The row of the topmost cell in the range
	 * @param row2 The row of the bottom-most cell in the range
	 * @return An array of {@link Cell} objects corresponding to all the cells
	 * comprised in the specified range
	 */
	public static Cell[] getColumn(int col, int row1, int row2)
	{
		return get(col, row1, col, row2);
	}
}

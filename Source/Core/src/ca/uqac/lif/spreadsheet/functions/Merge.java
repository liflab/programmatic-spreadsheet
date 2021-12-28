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
package ca.uqac.lif.spreadsheet.functions;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Merges multiple spreadsheets by creating a single row for each distinct
 * combination of values of a given set of columns. For example, given
 * this spreadsheet <i>S</i><sub>1</sub>:
 * <p>
 * <table border="1">
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * <tr><td>3</td><td>1</td><td>4</td></tr>
 * <tr><td>1</td><td>5</td><td>9</td></tr>
 * <tr><td>2</td><td>6</td><td>5</td></tr>
 * </table>
 * <p>
 * and <i>S</i><sub>2</sub>:
 * </p>
 * <table border="1">
 * <tr><th>A</th><th>D</th><th>E</th></tr>
 * <tr><td>3</td><td>2</td><td>7</td></tr>
 * <tr><td>1</td><td>1</td><td>8</td></tr>
 * <tr><td>2</td><td>2</td><td>8</td></tr>
 * </table>
 * <p>
 * the merge operation will produce the following spreadsheet:
 * <p>
 * <table border="1">
 * <tr><th>A</th><th>B</th><th>C</th><th>D</th><th>E</th></tr>
 * <tr><td>3</td><td>1</td><td>4</td><td>2</td><td>7</td></tr>
 * <tr><td>1</td><td>5</td><td>9</td><td>1</td><td>8</td></tr>
 * <tr><td>2</td><td>6</td><td>5</td><td>2</td><td>8</td></tr>
 * </table>
 * <p>
 * As one can see, the rows with the same values of their common
 * column ("A") in both spreadsheets have been merged.
 * <p>
 * In relational terms, this operation can be seen as the union
 * of the outer left and the outer right join of <i>S</i><sub>1</sub>
 * and <i>S</i><sub>2</sub>. This means that rows in either spreadsheet
 * with values not present in the other still appear in the result.
 * In the previous example, if we replace <i>S</i><sub>1</sub> by:
 * <p>
 *<table border="1">
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * <tr><td>1</td><td>5</td><td>9</td></tr>
 * <tr><td>2</td><td>6</td><td>5</td></tr>
 * </table>
 * <p>
 * and <i>S</i><sub>2</sub> by:
 * <p>
 * <table border="1">
 * <tr><th>A</th><th>D</th><th>E</th></tr>
 * <tr><td>3</td><td>2</td><td>7</td></tr>
 * <tr><td>1</td><td>1</td><td>8</td></tr>
 * </table>
 * <p>
 * the result becomes:
 * <p>
 * <table border="1">
 * <tr><th>A</th><th>B</th><th>C</th><th>D</th><th>E</th></tr>
 * <tr><td>1</td><td>5</td><td>9</td><td>1</td><td>8</td></tr>
 * <tr><td>2</td><td>6</td><td>5</td><td></td><td></td></tr>
 * <tr><td>3</td><td></td><td></td><td>2</td><td>7</td></tr>
 * </table>
 * <p>
 * Note how the ordering of the rows is affected, as the row with
 * A=3 is now only encountered when reading <i>S</i><sub>2</sub>.
 * 
 * @author Sylvain Hallé
 */
public class Merge extends SpreadsheetFunction
{
	/**
	 * The indices of the columns to be considered for the merge.
	 */
	/*@ non_null @*/ protected int[] m_keyColumns;
	
	/**
	 * Creates a new instance of the function.
	 * @param arity The input arity
	 * @param columns The columns to be considered for the merge
	 */
	public Merge(int arity, /*@ non_null @*/ int ... columns)
	{
		super(arity);
		m_keyColumns = columns;
	}
	
	/**
	 * Creates a new instance of the function of input arity 2.
	 * @param columns The columns to be considered for the merge
	 */
	public Merge(/*@ non_null @*/ int ... columns)
	{
		this(2, columns);
		m_excludeFirst = true;
	}
	
	@Override
	public Merge excludeFirst(boolean b)
	{
		if (!b)
		{
			throw new UnsupportedOperationException("This function must exclude the first row");
		}
		return (Merge) super.excludeFirst(b);
	}
	
	@Override
	public Merge excludeFirst()
	{
		return (Merge) super.excludeFirst();
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		Spreadsheet[] s_inputs = new Spreadsheet[inputs.length];
		List<Object> headers = new ArrayList<Object>();
		List<TrackedCell> tracked_headers = new ArrayList<TrackedCell>();
		List<Row> merged_rows = new ArrayList<Row>();
		for (int i = 0; i < inputs.length; i++)
		{
			if (!(inputs[i] instanceof Spreadsheet))
			{
				throw new InvalidArgumentTypeException("Argument " + i + " is not a spreadsheet");
			}
			Spreadsheet s = (Spreadsheet) inputs[i];
			s_inputs[i] = s;
			if (i == 0)
			{
				for (int col = 0; col < m_keyColumns.length; col++)
				{
					Object o = s.get(col, 0);
					headers.add(o);
					tracked_headers.add(new TrackedCell(o, InputCell.get(col, 0, i)));
				}
			}
			for (int col = 0; col < s_inputs[i].getWidth(); col++)
			{
				Object o = s.get(col, 0);
				if (!headers.contains(o))
				{
					headers.add(o);
					tracked_headers.add(new TrackedCell(o, InputCell.get(col, 0, i)));
				}
			}
			for (int r = 1; r < s.getHeight(); r++)
			{
				Object[] original_row = s.getRow(r);
				Row row = findRow(original_row, r, merged_rows);
				for (int col = 0; col < original_row.length; col++)
				{
					row.add(s.get(col, 0), new TrackedCell(s.get(col, r), InputCell.get(col, r, i)));
				}
			}
		}
		return new Object[] {fillSpreadsheet(merged_rows, tracked_headers)};
	}
}

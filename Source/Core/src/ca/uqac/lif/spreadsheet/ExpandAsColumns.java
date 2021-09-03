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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;

/**
 * Transforms a spreadsheet by expanding the values of one column as column
 * headers.
 * <p>
 * Take for example this table:
 * <p>
 * <table border="1">
 * <tr><th>Browser</th><th>Market</th><th>Share</th></tr>
 * <tr><td>Firefox</td><td>video</td><td>20</td></tr>
 * <tr><td>Firefox</td><td>audio</td><td>23</td></tr>
 * <tr><td>IE</td><td>video</td><td>10</td></tr>
 * <tr><td>IE</td><td>audio</td><td>13</td></tr>
 * </table>
 * <p>
 * One can replace column "Market", and create as many columns as there are
 * values for this attribute. We can set the value to put in each column
 * as that of the corresponding value of "Share". This will yield the
 * following spreadsheet:
 * <p>
 * <table border="1">
 * <tr><th>Browser</th><th>video</th><th>audio</th></tr>
 * <tr><td>Firefox</td><td>20</td><td>23</td></tr>
 * <tr><td>IE</td><td>10</td><td>13</td></tr>
 * </table>
 */
public class ExpandAsColumns extends SpreadsheetFunction
{
	protected int m_headerColumn;

	protected int m_valueColumn;

	public ExpandAsColumns(int header, int value)
	{
		super();
		m_headerColumn = header;
		m_valueColumn = value;
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet s = (Spreadsheet) inputs[0];
		// Create first line of output spreadsheet with column names of original
		List<TrackedCell> new_headers = new ArrayList<TrackedCell>();
		{
			Object[] first_row = s.getRow(0);
			for (int i = 0; i < s.getWidth(); i++)
			{
				if (i != m_headerColumn && i != m_valueColumn)
				{
					new_headers.add(new TrackedCell(first_row[i], Cell.get(i, 0)));
				}
			}
		}
		// Populate other rows by applying the transposition
		List<Row> new_rows = new ArrayList<Row>();
		for (int row_index = 1; row_index < s.getHeight(); row_index++)
		{
			Object[] original_row = s.getRow(row_index);
			Object key = original_row[m_headerColumn];
			if (!containsHeader(new_headers, key))
			{
				new_headers.add(new TrackedCell(key, Cell.get(m_headerColumn, row_index)));
			}
			Row r = findRow(original_row, row_index, new_rows);
			r.add(original_row[m_headerColumn],
					new TrackedCell(original_row[m_valueColumn], Cell.get(m_valueColumn, row_index)));
		}
		return new Object[] {fillSpreadsheet(s, new_rows, new_headers)};
	}

	protected boolean containsHeader(List<TrackedCell> headers, Object key)
	{
		if (key == null)
		{
			return true;
		}
		for (TrackedCell tc : headers)
		{
			if (key != null && key.equals(tc.getValue()))
			{
				return true;
			}
		}
		return false;
	}

	/*@ non_null @*/ protected Row findRow(Object[] row_contents, int row_index, List<Row> new_rows)
	{
		for (Row r : new_rows)
		{
			if (r.matches(row_contents))
			{
				return r;
			}
		}
		Row r = new Row(row_contents, row_index);
		new_rows.add(r);
		return r;
	}

	protected Spreadsheet fillSpreadsheet(Spreadsheet original, List<Row> new_rows, List<TrackedCell> new_headers)
	{
		Spreadsheet out = new Spreadsheet(new_headers.size(), new_rows.size() + 1);
		m_mapping = new Cell[new_rows.size() + 1][new_headers.size()][];
		for (int col = 0; col < new_headers.size(); col++)
		{
			out.set(col, 0, new_headers.get(col).getValue());
			m_mapping[0][col] = new Cell[] {new_headers.get(col).getOrigin()};
		}
		for (int row = 0; row < new_rows.size(); row++)
		{
			Row r = new_rows.get(row);
			for (int col = 0; col < r.m_staticColumns.length; col++)
			{
				out.set(col, row + 1, r.m_staticColumns[col].getValue());
				m_mapping[row + 1][col] = new Cell[] {r.m_staticColumns[col].getOrigin()};
			}
			for (int col = r.m_staticColumns.length; col < new_headers.size(); col++)
			{
				Object key = new_headers.get(col).getValue();
				TrackedCell tc = r.m_otherValues.get(key);
				if (tc != null)
				{
					out.set(col, row + 1, tc.getValue());
					m_mapping[row + 1][col] = new Cell[] {tc.getOrigin(), Cell.get(m_headerColumn, tc.getOrigin().getRow())};
				}
			}
		}
		return out;
	}

	protected class Row
	{
		/*@ non_null @*/ protected TrackedCell[] m_staticColumns;

		/*@ non_null @*/ protected Map<Object,TrackedCell> m_otherValues;

		public Row(/*@ non_null @*/ Object[] row, int row_index)
		{
			super();
			m_staticColumns = new TrackedCell[row.length - 2];
			int index = 0;
			for (int i = 0; i < m_staticColumns.length; i++)
			{
				if (i != m_headerColumn && i != m_valueColumn)
				{
					m_staticColumns[index++] = new TrackedCell(row[i], Cell.get(i, row_index));
				}
			}
			m_otherValues = new HashMap<Object,TrackedCell>();
		}

		public void add(Object key, TrackedCell value)
		{
			m_otherValues.put(key, value);
		}

		@Override
		public int hashCode()
		{
			int c = 0;
			for (Object o : m_staticColumns)
			{
				if (o != null)
				{
					c += o.hashCode();
				}
			}
			return c;
		}

		public boolean matches(Object[] row)
		{
			int index = 0;
			for (int i = 0; i < m_staticColumns.length; i++)
			{
				if (i != m_headerColumn && i != m_valueColumn)
				{
					Object tv = m_staticColumns[index].getValue();
					if (((tv == null) != (row[i] == null)) ||
							(tv != null && !tv.equals(row[i])))
					{
						return false;
					}
					index++;
				}
			}
			return true;
		}		
	}
}
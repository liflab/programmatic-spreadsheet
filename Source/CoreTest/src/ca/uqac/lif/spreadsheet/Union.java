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

import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;

/**
 * Function that performs the union of two spreadsheets by appending their
 * rows. Note that this function allows a union to be performed regardless of
 * the dimensions of the original spreadsheets. Hence, if <i>S</i><sub>1</sub>
 * is the following spreadsheet:
 * <p>
 * <table border="1">
 * <tr><td>3</td><td>1</td><td>4</td></tr>
 * <tr><td>1</td><td>5</td><td>9</td></tr>
 * <tr><td>2</td><td>6</td><td>5</td></tr>
 * </table>
 * <p>
 * and <i>S</i><sub>2</sub> is the following spreadsheet:
 * <p>
 * <table border="1">
 * <tr><td>2</td><td>7</td></tr>
 * <tr><td>1</td><td>8</td></tr>
 * <tr><td>2</td><td>8</td></tr>
 * <tr><td>1</td><td>8</td></tr>
 * </table>
 * <p>
 * the union of <i>S</i><sub>1</sub> and <i>S</i><sub>2</sub> will yield:
 * <p>
 * <table border="1">
 * <tr><td>3</td><td>1</td><td>4</td></tr>
 * <tr><td>1</td><td>5</td><td>9</td></tr>
 * <tr><td>2</td><td>6</td><td>5</td></tr>
 * <tr><td>2</td><td>7</td><td></td></tr>
 * <tr><td>1</td><td>8</td><td></td></tr>
 * <tr><td>2</td><td>8</td><td></td></tr>
 * <tr><td>1</td><td>8</td><td></td></tr>
 * </table>
 * <p>
 * Optionally, the function can be instructed to handle the first row of each
 * spreadsheet as if they are column labels. In such a case, the first row of
 * the output spreadsheet is the row of the input spreadsheet that has the
 * largest width (or the first such spreadsheet in the list of arguments, if
 * more than one has maximum width).
 * 
 * @author Sylvain Hallé
 *
 */
public class Union extends SpreadsheetFunction
{
	/**
	 * A flag that determines if the first row of each spreadsheet should be
	 * interpreted as column labels, and not a normal row of data.
	 */
	protected boolean m_excludeFirst;
	
	/**
	 * An array that keeps track of the number of rows in each spreadsheet passed
	 * to the function the last time it was called.
	 */
	/*@ non_null @*/ protected int[] m_heights;
	
	/**
	 * An array that keeps track of the number of columns in each spreadsheet
	 * passed to the function the last time it was called.
	 */
	/*@ non_null @*/ protected int[] m_widths;
	
	/**
	 * The index of the input spreadsheet that has been chosen as the source of
	 * column labels. This argument only has a meaning if {@link #m_excludeFirst}
	 * is true.
	 */
	protected int m_labelInput;
	
	/**
	 * Creates a new instance of the function with a given input arity.
	 * @param in_arity The input arity
	 */
	public Union(int in_arity)
	{
		super(in_arity);
		m_heights = new int[in_arity];
		m_widths = new int[in_arity];
		m_labelInput = 0;
		m_excludeFirst = false;
	}
	
	/**
	 * Creates a new instance of the function with an input arity of 2.
	 */
	public Union()
	{
		this(2);
	}
	
	/**
	 * Sets whether the first row of each spreadsheet should be interpreted as
	 * column labels, and not a normal row of data.
	 * @param b Set to {@code true} to handle first row as headers, {@code false}
	 * otherwise.
	 * @return This function
	 */
	/*@ non_null @*/ public Union excludeFirst(boolean b)
	{
		m_excludeFirst = b;
		return this;
	}
	
	/**
	 * Sets the function so that the first row of each spreadsheet is be
	 * interpreted as column labels, and not a normal row of data. This is
	 * equivalent to the call {@code excludeFirst(true)}.
	 * @return This function
	 */
	/*@ non_null @*/ public Union excludeFirst()
	{
		return excludeFirst(true);
	}
	
	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		Spreadsheet[] s_inputs = new Spreadsheet[inputs.length];
		int max_width = -1, total_height = 0;
		for (int i = 0; i < inputs.length; i++)
		{
			if (!(inputs[i] instanceof Spreadsheet))
			{
				throw new InvalidArgumentTypeException("Argument " + i + " is not a spreadsheet");
			}
			s_inputs[i] = (Spreadsheet) inputs[i];
			m_heights[i] = s_inputs[i].getHeight();
			if (m_excludeFirst)
			{
				total_height += m_heights[i] - 1;
			}
			else
			{
				total_height += m_heights[i];
			}
			m_widths[i] = s_inputs[i].getWidth();
			if (m_widths[i] > max_width)
			{
				m_labelInput = i;
				max_width = m_widths[i];
			}
		}
		if (m_excludeFirst)
		{
			total_height++; // To account for the extra header row
		}
		Spreadsheet out = new Spreadsheet(max_width, total_height);
		m_mapping = new InputCell[total_height][max_width][];
		int current_row = 0;
		if (m_excludeFirst)
		{
			for (int col = 0; col < max_width; col++)
			{
				out.set(col, 0, s_inputs[m_labelInput].get(col, 0));
				m_mapping[0][col] = new InputCell[] {InputCell.get(col, 0, m_labelInput)};
			}
			current_row = 1;
		}
		for (int i = 0; i < inputs.length; i++)
		{
			for (int row = (m_excludeFirst ? 1 : 0); row < m_heights[i]; row++)
			{
				for (int col = 0; col < m_widths[i]; col++)
				{
					out.set(col, current_row, s_inputs[i].get(col, row));
					m_mapping[current_row][col] = new InputCell[] {InputCell.get(col, row, i)};
				}
				current_row++;
			}
		}
		return new Object[] {out};
	}
	
	@Override
	public void reset()
	{
		super.reset();
		m_labelInput = 0;
		for (int i = 0; i < m_heights.length; i++)
		{
			m_heights[i] = 0;
			m_widths[i] = 0;
		}
	}
}

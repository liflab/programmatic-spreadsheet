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
import java.util.Collections;
import java.util.List;

import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Computes box-and-whiskers statistics from each column of an
 * input table. For example, given the following table:
 * <p>
 * <table border="1">
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * <tr><td>3</td><td>1</td><td>4</td></tr>
 * <tr><td>1</td><td>5</td><td>9</td></tr>
 * <tr><td>2</td><td>6</td><td>5</td></tr>
 * <tr><td>5</td><td>2</td><td>7</td></tr>
 * <tr><td>3</td><td>7</td><td>12</td></tr>
 * <tr><td>6</td><td>8</td><td>9</td></tr>
 * </table>
 * <p>
 * the box transformation will produce the following result:
 * <p>
 * <table border="1">
 * <tr><th>x</th><th>Min</th><th>Q1</th><th>Q2</th><th>Q3</th><th>Max</th><th>Label</th></tr>
 * <tr><td>0</td><td>1</td><td>1</td><td>3</td><td>3</td><td>6</td><td>A</td></tr>
 * <tr><td>1</td><td>1</td><td>1</td><td>5</td><td>6</td><td>8</td><td>B</td></tr>
 * <tr><td>2</td><td>4</td><td>4</td><td>7</td><td>9</td><td>12</td><td>C</td></tr>
 * </table>
 * <p>
 * The columns represent respectively:
 * <ol>
 * <li>A line counter</li>
 * <li>The minimum value of that column (Min)</li>
 * <li>The value of the first quartile (Q1)</li>
 * <li>The value of the second quartile (Q2)</li>
 * <li>The value of the third quartile (Q3)</li>
 * <li>The maximum value of that column (Max)</li>
 * <li>The name of the column header in the original table</li> 
 * </ol>
 * <p>
 * This function is called "box stats", because it produces a
 * table in a form that can be used by a
 * {@link ca.uqac.lif.spreadsheet.plots.BoxPlot BoxPlot}. Each cell of the output
 * is tracked to the coresponding cell in the input spreadsheet.
 *  
 * @author Sylvain Hallé
 */
public class BoxStats extends SpreadsheetFunction
{
	/**
	 * The caption given to the first column of the output spreadsheet.
	 */
	protected String m_captionX = "x";
	
	/**
	 * The caption given to the "min" column  of the output spreadsheet.
	 */
	protected String m_captionMin = "Min";
	
	/**
	 * The caption given to the "first quartile" column of the output
	 * spreadsheet.
	 */
	protected String m_captionQ1 = "Q1";
	
	/**
	 * The caption given to the "second quartile" column of the output
	 * spreadsheet.
	 */
	protected String m_captionQ2 = "Q2";
	
	/**
	 * The caption given to the "third quartile" column of the output
	 * spreadsheet.
	 */
	protected String m_captionQ3 = "Q3";
	
	/**
	 * The caption given to the "max" column of the output
	 * spreadsheet.
	 */
	protected String m_captionMax = "Max";
	
	/**
	 * The caption given to the "label" column of the output
	 * spreadsheet.
	 */
	protected String m_captionLabel = "Label";
	
	public BoxStats()
	{
		super(1);
	}
	
	public BoxStats(String x, String min, String q1, String q2, String q3, String max, String label)
	{
		super(1);
		m_captionX = x;
		m_captionMin = min;
		m_captionQ1 = q1;
		m_captionQ2 = q2;
		m_captionQ3 = q3;
		m_captionMax = max;
		m_captionLabel = label;
	}

	@Override
	protected Object[] getValue(Object ... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet table = (Spreadsheet) inputs[0];
		Spreadsheet new_table = new Spreadsheet(7, table.getWidth() + 1);
		m_mapping = new InputCell[table.getWidth() + 1][7][];
		new_table.set(0, 0, m_captionX);
		new_table.set(1, 0, m_captionMin);
		new_table.set(2, 0, m_captionQ1);
		new_table.set(3, 0, m_captionQ2);
		new_table.set(4, 0, m_captionQ3);
		new_table.set(5, 0, m_captionMax);
		new_table.set(6, 0, m_captionLabel);
		for (int col = 0; col < table.getWidth(); col++)
		{
			Object[] col_vs = table.getColumn(col);
			List<Float> values = new ArrayList<Float>(), sorted_values = new ArrayList<Float>();
			for (int i = 1; i < col_vs.length; i++) // 1 since first line is col name
			{
				if (col_vs[i] instanceof Number)
				{
					values.add(((Number) col_vs[i]).floatValue());
				}
			}
			sorted_values.addAll(values);
			Collections.sort(sorted_values);
			if (values.isEmpty())
			{
				// Nothing to do
				return new Object[] {new_table};
			}
			float v;
			float num_values = values.size();
			int min_index = 0;
			int q1_index = Math.max(0, (int)(num_values * 0.25) - 1);
			int q2_index = Math.max(0, (int)(num_values * 0.5) - 1);
			int q3_index = Math.max(0, (int)(num_values * 0.75) - 1);
			int max_index = Math.max(0, (int) num_values - 1);
			new_table.set(0, col + 1, col);
			m_mapping[col + 1][0] = null;
			v = sorted_values.get(min_index);
			new_table.set(1, col + 1, v);
			m_mapping[col + 1][1] = new InputCell[] {InputCell.get(col, values.indexOf(v) + 1)};
			v = sorted_values.get(q1_index);
			new_table.set(2, col + 1, v);
			m_mapping[col + 1][2] = new InputCell[] {InputCell.get(col, values.indexOf(v) + 1)};
			v = sorted_values.get(q2_index);
			new_table.set(3, col + 1, v);
			m_mapping[col + 1][3] = new InputCell[] {InputCell.get(col, values.indexOf(v) + 1)};
			v = sorted_values.get(q3_index);
			new_table.set(4, col + 1, v);
			m_mapping[col + 1][4] = new InputCell[] {InputCell.get(col, values.indexOf(v) + 1)};
			v = sorted_values.get(max_index);
			new_table.set(5, col + 1, v);
			m_mapping[col + 1][5] = new InputCell[] {InputCell.get(col, values.indexOf(v) + 1)};
			new_table.set(6, col + 1, table.get(col, 0));
			m_mapping[col + 1][6] = new InputCell[] {InputCell.get(col, 0)};
		}
		return new Object[] {new_table};
	}
}

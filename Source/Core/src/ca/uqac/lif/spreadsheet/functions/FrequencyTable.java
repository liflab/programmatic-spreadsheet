/*
  MTNP: Manipulate Tables N'Plots
  Copyright (C) 2017-2020 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.spreadsheet.functions;

import ca.uqac.lif.petitpoucet.function.AtomicFunction;

/**
 * Function that creates a two-dimensional frequency table out of an ordered
 * collection of 
 * Table that accumulates two-dimensional frequency values. Entries can be
 * added to the table in two ways:
 * <ul>
 * <li>By calling {@link #add(double, double, double)}, where the first two
 * arguments represent the x-y coordinates and the third represents the
 * value to increment in the corresponding bin of the frequency table</li>
 * <li>By calling {@link #add(TableEntry)}, and passing a tuple with three
 * attributes called "x", "y" and "v" whose value corresponds to the three
 * arguments that would be given to the previous method</li>
 * </ul>
 * @since 0.1.13
 * @author Sylvain Hallé
 */
public class FrequencyTable extends AtomicFunction
{
	/**
	 * A caption to denote the "x" value in a tuple given to the table
	 */
	public static final transient String CAPTION_X = "x";

	/**
	 * A caption to denote the "y" value in a tuple given to the table
	 */
	public static final transient String CAPTION_Y = "y";

	/**
	 * A caption to denote the value in a tuple given to the table
	 */
	public static final transient String CAPTION_V = "v";

	/**
	 * The preconfigured minimum value of the generated frequency table
	 * along the x axis
	 */
	protected double m_minX;

	/**
	 * The preconfigured maximum value of the generated frequency table
	 * along the x axis
	 */
	protected double m_maxX;

	/**
	 * The number of buckets that will divide the interval
	 * x<sub>max</sub> - x<sub>min</sub>
	 */
	protected int m_numBucketsX;

	/**
	 * The preconfigured minimum value of the generated frequency table
	 * along the y axis
	 */
	protected double m_minY;

	/**
	 * The preconfigured maximum value of the generated frequency table
	 * along the y axis
	 */
	protected double m_maxY;

	/**
	 * The number of buckets that will divide the interval
	 * y<sub>max</sub> - y<sub>min</sub>
	 */
	protected int m_numBucketsY;

	/**
	 * The actual values of the table
	 */
	protected double[][] m_values;

	/**
	 * The lower value of each bucket on the x axis
	 */
	protected double[] m_scaleX;

	/**
	 * The lower value of each bucket on the y axis
	 */
	protected double[] m_scaleY;

	/**
	 * An array containing the names given to the columns of the resulting
	 * table
	 */
	protected String[] m_columnNames;

	/**
	 * The width of each bucket on the x axis
	 */
	protected double m_widthX;

	/**
	 * The width of each bucket on the y axis
	 */
	protected double m_widthY;

	/**
	 * An optional default increment
	 */
	protected Double m_defaultIncrement = null;

	/**
	 * Creates a new frequency table.
	 * @param min_x  The preconfigured minimum value of the generated frequency table
	 * along the x axis
	 * @param max_x  The preconfigured maximum value of the generated frequency table
	 * along the x axis
	 * @param b_x The number of buckets that will divide the interval
	 * x<sub>max</sub> - x<sub>min</sub>
	 * @param min_y  The preconfigured minimum value of the generated frequency table
	 * along the y axis
	 * @param max_y  The preconfigured maximum value of the generated frequency table
	 * along the y axis
	 * @param b_y The number of buckets that will divide the interval
	 * y<sub>max</sub> - y<sub>min</sub>
	 * @param default_increment An optional default value to increment an entry
	 * when not specified; can be set to <tt>null</tt> (meaning no default increment)
	 */
	public FrequencyTable(double min_x, double max_x, int b_x, double min_y, double max_y, int b_y, Double default_increment)
	{
		super(1, 1);
		m_minX = min_x;
		m_minY = min_y;
		m_maxX = max_x;
		m_maxY = max_y;
		m_numBucketsX = b_x;
		m_numBucketsY = b_y;
		m_defaultIncrement = default_increment;
		m_values = new double[b_y][b_x];
		for (int i = 0; i < b_y; i++)
		{
			for (int j = 0; j < b_x; j++)
			{
				m_values[i][j] = 0;
			}
		}
		m_widthX = (max_x - min_x) / (double) b_x;
		m_widthY = (max_y - min_y) / (double) b_y;
		m_scaleX = new double[b_x];
		for (int i = 0; i < b_x; i++)
		{
			m_scaleX[i] = m_widthX * (double) i;
		}
		m_scaleY = new double[b_y];
		for (int i = 0; i < b_y; i++)
		{
			m_scaleY[i] = m_widthY * (double) i;
		}
		m_columnNames = new String[b_x + 1];
		m_columnNames[0] = "y";
		for (int i = 0; i < m_scaleX.length; i++)
		{
			m_columnNames[i + 1] = Double.toString(m_scaleX[i]);
		}
	}

	/**
	 * Creates a new frequency table.
	 * @param min_x  The preconfigured minimum value of the generated frequency table
	 * along the x axis
	 * @param max_x  The preconfigured maximum value of the generated frequency table
	 * along the x axis
	 * @param b_x The number of buckets that will divide the interval
	 * x<sub>max</sub> - x<sub>min</sub>
	 * @param min_y  The preconfigured minimum value of the generated frequency table
	 * along the y axis
	 * @param max_y  The preconfigured maximum value of the generated frequency table
	 * along the y axis
	 * @param b_y The number of buckets that will divide the interval
	 * y<sub>max</sub> - y<sub>min</sub>
	 */
	public FrequencyTable(double min_x, double max_x, int b_x, double min_y, double max_y, int b_y)
	{
		this(min_x, max_x, b_x, min_y, max_y, b_y, null);
	}

	/**
	 * Gets the minimum value on the x axis
	 * @return The value
	 */
	public double getMinX()
	{
		return m_minX;
	}

	/**
	 * Gets the minimum value on the y axis
	 * @return The value
	 */
	public double getMinY()
	{
		return m_minY;
	}

	/**
	 * Gets the maximum value on the x axis
	 * @return The value
	 */
	public double getMaxX()
	{
		return m_maxX;
	}	

	/**
	 * Gets the maximum value on the y axis
	 * @return The value
	 */
	public double getMaxY()
	{
		return m_maxY;
	}

	/**
	 * Gets the width of each bucket on the x axis
	 * @return The width
	 */
	public double getXWidth()
	{
		return (m_maxX - m_minX) / (double) m_numBucketsX;
	}

	/**
	 * Gets the width of each bucket on the y axis
	 * @return The width
	 */
	public double getYWidth()
	{
		return (m_maxY - m_minY) / (double) m_numBucketsY;
	}

	/**
	 * Gets the array of values composing the frequency table
	 * @return The array of values
	 */
	public double[][] getArray()
	{
		return m_values;
	}

	/**
	 * Gets the number of buckets that divide the interval
	 * x<sub>max</sub> - x<sub>min</sub>
	 * @return The number of buckets
	 */
	public int getNumBucketsX()
	{
		return m_numBucketsX;
	}

	/**
	 * Gets the number of buckets that divide the interval
	 * y<sub>max</sub> - y<sub>min</sub>
	 * @return The number of buckets
	 */
	public int getNumBucketsY()
	{
		return m_numBucketsY;
	}

	/**
	 * Gets the labels of the table's scale on the x axis
	 * @return The array of labels
	 */
	public double[] getScaleX()
	{
		return m_scaleX;
	}

	/**
	 * Gets the labels of the table's scale on the y axis
	 * @return The array of labels
	 */
	public double[] getScaleY()
	{
		return m_scaleY;
	}

	@Override
	public String[] getColumnNames()
	{
		if (m_defaultIncrement == null)
		{
			return new String[] {CAPTION_X, CAPTION_Y, CAPTION_V};
		}
		return new String[] {CAPTION_X, CAPTION_Y};
	}

	@Override
	public void add(TableEntry e)
	{
		Number x = e.get(CAPTION_X).numberValue();
		Number y = e.get(CAPTION_Y).numberValue();
		if (x == null || y == null)
		{
			// Invalid tuple: ignore
			return;
		}
		double d_x = x.doubleValue();
		double d_y = y.doubleValue();
		double d_v = 1;
		if (m_defaultIncrement != null)
		{
			d_v = m_defaultIncrement;
		}
		else if (e.containsKey(CAPTION_V))
		{
			Number v = e.get(CAPTION_V).numberValue();
			if (v != null)
			{
				d_v = v.doubleValue();
			}
		}
		add(d_x, d_y, d_v);
	}

	/**
	 * Adds a value to the frequency table
	 * @param x The x position
	 * @param y The y position
	 * @param v The value to add in the corresponding cell
	 * @return This table
	 */
	public FrequencyTable add(double x, double y, double v)
	{
		int bin_x = (int) Math.floor(((x - m_minX) / (m_maxX - m_minX)) * (double) m_numBucketsX);
		int bin_y = (int) Math.floor(((y - m_minY) / (m_maxY - m_minY)) * (double) m_numBucketsY);
		if (bin_x < 0 || bin_x >= m_numBucketsX || bin_y < 0 || bin_y >= m_numBucketsY)
		{
			// Out of bounds: ignore
			return this;
		}
		m_values[bin_y][bin_x] += v;
		return this;
	}
	
	/**
	 * Adds 1 or the default increment to the frequency table
	 * @param x The x position
	 * @param y The y position
	 * @return This table
	 */
	public FrequencyTable add(double x, double y)
	{
		if (m_defaultIncrement != null)
		{
			return add(x, y, m_defaultIncrement);
		}
		return add(x, y, 1);
	}

	@Override
	public TempTable getDataTable(boolean link_to_experiments, String... ordering)
	{
		// Ignore column ordering, as it is important in such a table
		return getDataTable(link_to_experiments);
	}

	@Override
	public TempTable getDataTable(boolean temporary)
	{
		TempTable tt = new TempTable(getId(), m_columnNames);
		for (int i = 0; i < m_numBucketsY; i++)
		{
			TableEntry te = new TableEntry();
			te.put("y", m_scaleY[i]);
			for (int j = 0; j < m_numBucketsX; j++)
			{
				te.put(Double.toString(m_scaleX[j]), m_values[j][i]);
			}
			tt.add(te);
		}
		return tt;
	}

	@Override
	public NodeFunction getDependency(int row, int col) 
	{
		// No dependency given
		return null;
	}

	@Override
	public void clear()
	{
		super.clear();
		for (int i = 0; i < m_numBucketsY; i++)
		{
			for (int j = 0; j < m_numBucketsX; j++)
			{
				m_values[i][j] = 0;
			}
		}
	}
	
	@Override
	public FrequencyTable duplicate(boolean with_state)
	{
		FrequencyTable ft = new FrequencyTable(m_minX, m_maxX, m_numBucketsX, m_minY, m_maxY, m_numBucketsY, m_defaultIncrement);
		copyInto(ft, with_state);
		return ft;
	}
	
	@Override
	protected void copyInto(Table t, boolean with_state)
	{
		super.copyInto(t, with_state);
		if (!(t instanceof FrequencyTable))
		{
			return;
		}
		FrequencyTable ft = (FrequencyTable) t;
		if (with_state)
		{
			for (int i = 0; i < m_numBucketsY; i++)
			{
				for (int j = 0; j < m_numBucketsX; j++)
				{
					ft.m_values[i][j] = m_values[i][j];
				}
			}
		}
	}
}

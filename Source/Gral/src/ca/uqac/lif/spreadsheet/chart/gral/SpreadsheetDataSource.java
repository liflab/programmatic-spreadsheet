/*
  MTNP: Manipulate Tables N'Plots
  Copyright (C) 2017 Sylvain Hallé

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
package ca.uqac.lif.spreadsheet.chart.gral;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import de.erichseifert.gral.data.Column;
import de.erichseifert.gral.data.DataListener;
import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.Row;
import de.erichseifert.gral.data.statistics.Statistics;

/**
 * An object acting as a bridge between a {@link Spreadsheet} and the
 * {@link DataSource} interface of the GRAL library. This object is necessary
 * to convert spreadsheets into objects that the GRAL library can handle.
 *  
 * @author Sylvain Hallé
 */
public class SpreadsheetDataSource implements DataSource
{
	/**
	 * The data listeners associated to this table.
	 */
	protected final Set<DataListener> m_dataListeners;
	
	/**
	 * The underlying spreadsheet used to feed the data source.
	 */
	protected final Spreadsheet m_spreadsheet;
	
	/**
	 * Creates a new GRAL data table out of an arbitrary table.
	 * @param t The table
	 */
	public SpreadsheetDataSource(Spreadsheet t)
	{
		super();
		m_spreadsheet = t;
		m_dataListeners = new HashSet<DataListener>();
	}
	
	@Override
	public void removeDataListener(DataListener dataListener)
	{
		m_dataListeners.remove(dataListener);
	}
	
	@Override
	public Row getRow(int row)
	{
		return new Row(this, row);
	}
	
	@Override
	public final Iterator<Comparable<?>> iterator()
	{
		return new RowIterator();
	}

	@Override
	public final void addDataListener(DataListener dataListener)
	{
		m_dataListeners.add(dataListener);
	}

	@Override
	public Statistics getStatistics()
	{
		return new Statistics(this);
	}
	
	@Override
	public final Column getColumn(int col)
	{
		return new Column(this, col);
	}

	@Override
	public Comparable<?> get(int col, int row)
	{
		Object o = m_spreadsheet.get(col, row);
		if (!(o instanceof Comparable))
		{
			return null;
		}
		return (Comparable<?>) o;
	}

	@Override
	public int getColumnCount() 
	{
		return m_spreadsheet.getWidth();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Comparable<?>>[] getColumnTypes() 
	{
		Class<? extends Comparable<?>>[] types = new Class[m_spreadsheet.getWidth()];
		for (int col = 0; col < types.length; col++)
		{
			types[col] = (Class<? extends Comparable<?>>) getColumnClass(col);
		}
		return types;
	}

	@Override
	public int getRowCount() 
	{
		return m_spreadsheet.getHeight() - 1; // First row are column headers
	}

	@Override
	public boolean isColumnNumeric(int col)
	{
		// Checks if the first non-null cell of a column (after the first row) is
		// a numerical value.
		// null or numerical values
		for (int row = 1; row < m_spreadsheet.getHeight(); row++)
		{
			Object o = m_spreadsheet.get(col, row);
			if (o != null)
			{
				return o instanceof Number;
			}
		}
		// All cells are null, we take a guess
		return true;
	}
	
	/**
	 * Creates a two-dimensional GRAL data series out of two columns of a
	 * spreadsheet. The resulting series will only contain lines of the
	 * original tables where the values in both columns are defined, i.e.
	 * all lines with missing values for these two columns will be filtered
	 * out. This is necessary since GRAL fails to draw a plot (i.e. throws
	 * a <tt>NullPointerException</tt>) when it contains
	 * data series with missing values.
	 *  
	 * @param col_index_x The index of the first column
	 * @param col_index_y The index of the second column. This name of this
	 * column will also be the name of the resulting GRAL data series 
	 * @return The GRAL data series
	 */
	public DataSeries getCleanedDataSeries(int col_index_x, int col_index_y)
	{
		List<Object[]> tuples = new ArrayList<Object[]>();
		for (int row = 1; row < m_spreadsheet.getHeight(); row++)
		{
			Object x = m_spreadsheet.get(col_index_x, row);
			Object y = m_spreadsheet.get(col_index_y, row);
			if (x != null && y != null)
			{
				tuples.add(new Object[] {x, y});
			}
		}
		Spreadsheet s_temp = new Spreadsheet(2, tuples.size());
		for (int i = 0; i < tuples.size(); i++)
		{
			Object[] tuple = tuples.get(i);
			s_temp.set(0, i, tuple[0]);
			s_temp.set(1, i, tuple[1]);
		}
		SpreadsheetDataSource gdt = new SpreadsheetDataSource(s_temp);
		DataSeries series = new DataSeries(m_spreadsheet.getString(col_index_y, 0), gdt, 0, 1);
		return series;
	}
	
	/**
	 * Gets the class of the first non-null cell of a column after the first row.
	 * @param col The column index
	 * @return The class, or <tt>null</tt> of all cells are null
	 */
	protected Class<?> getColumnClass(int col)
	{
		for (int row = 1; row < m_spreadsheet.getHeight(); row++)
		{
			Object o = m_spreadsheet.get(col, row);
			if (o != null)
			{
				return o.getClass();
			}
		}
		return null;
	}
	
	/**
	 * An iterator enumerating the values on a given row of the underlying
	 * spreadsheet.
	 */
	public class RowIterator implements Iterator<Comparable<?>>
	{
		/**
		 * Index of current column
		 */
		protected int col = 0;

		/**
		 * Index of current row
		 */
		protected int row = 1;

		public RowIterator()
		{
			super();
			col = 0;
			row = 1;
		}

		/**
		 * Returns {@code true} if the iteration has more elements.
		 * (In other words, returns {@code true} if {@code next}
		 * would return an element rather than throwing an exception.)
		 * @return {@code true} if the iterator has more elements.
		 */
		@Override
		public boolean hasNext()
		{
			return (col < m_spreadsheet.getWidth()) && (row < m_spreadsheet.getHeight());
		}

		/**
		 * Returns the next element in the iteration.
		 * @return the next element in the iteration.
		 * @exception NoSuchElementException iteration has no more elements.
		 */
		@Override
		public Comparable<?> next()
		{
			if (!hasNext()) 
			{
				throw new NoSuchElementException();
			}
			Comparable<?> value = (Comparable<?>) m_spreadsheet.get(col, row);
			if (++col >= m_spreadsheet.getWidth()) 
			{
				col = 0;
				++row;
			}
			return value;
		}

		/**
		 * Method that theoretically removes a cell from a data source.
		 * However, this is not supported.
		 */
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public String getName()
	{
		return "Spreadsheet";
	}
}

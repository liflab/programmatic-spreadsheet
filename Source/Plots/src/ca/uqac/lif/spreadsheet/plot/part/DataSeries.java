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
package ca.uqac.lif.spreadsheet.plot.part;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;

public class DataSeries extends PlotPart
{
	/**
	 * Designates a data series in a plot by its name.
	 */
	public static class NamedDataSeries extends DataSeries
	{
		/**
		 * The name of the data series.
		 */
		/*@ non_null @*/ private final String m_name;
		
		/**
		 * Creates a new named data series part.
		 * @param name The name of the data series
		 */
		public NamedDataSeries(/*@ non_null @*/ String name)
		{
			super();
			m_name = name;
		}
		
		/*@ pure non_null @*/ public String getName()
		{
			return m_name;
		}
		
		@Override
		public String toString()
		{
			return "Data series " + m_name;
		}
	}
	
	/**
	 * Designates a data series in a plot by its number.
	 */
	public static class NumberedDataSeries extends DataSeries
	{
		/**
		 * The number of the data series.
		 */
		/*@ non_null @*/ private final int m_number;
		
		/**
		 * Creates a new numbered data series part.
		 * @param name The number of the data series
		 */
		public NumberedDataSeries(int number)
		{
			super();
			m_number = number;
		}
		
		/*@ pure @*/ public int getNumber()
		{
			return m_number;
		}
		
		@Override
		public String toString()
		{
			return "Data series #" + m_number;
		}
	}
	
	/**
	 * Retrieves the data series mentioned in a designator.
	 * @param d The designator
	 * @return The data series, or null if no data series is mentioned
	 */
	public static DataSeries mentionedSeries(Part d)
	{
		if (d instanceof DataSeries)
		{
			return (DataSeries) d;
		}
		if (d instanceof ComposedPart)
		{
			ComposedPart cd = (ComposedPart) d;
			for (int i = 0; i < cd.size(); i++)
			{
				Part p = cd.get(i);
				if (p instanceof DataSeries)
				{
					return (DataSeries) p;
				}
			}
		}
		return null;
	}
}

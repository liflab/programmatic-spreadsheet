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

import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.spreadsheet.plot.Plot;

/**
 * A {@link Part} that refers to a {@link Plot}.
 * @author Sylvain Hallé
 */
public abstract class PlotPart implements Part
{
	/**
	 * A single visible instance of the {@link Caption} part.
	 */
	public static final Caption caption = new Caption();
	
	/**
	 * A single visible instance of the {@link Legend} part.
	 */
	public static final Legend legend = new Legend();
	
	/**
	 * A single visible instance of the {@link DataSeries} part.
	 */
	public static final DataSeries dataSeries = new DataSeries();
	
	@Override
	public boolean appliesTo(Object o)
	{
		return o instanceof Plot;
	}
	
	@Override
	public Part head()
	{
		return this;
	}
	
	@Override
	public Part tail()
	{
		return null;
	}
	
	/**
	 * Part referring to the caption of some plot element.
	 */
	public static class Caption extends PlotPart
	{
		/**
		 * Creates a new instance of the caption part.
		 */
		protected Caption()
		{
			super();
		}
		
		@Override
		public String toString()
		{
			return "Caption";
		}
	}
	
	/**
	 * Part referring to the listing of the data series in a plot.
	 */
	public static class Legend extends PlotPart
	{
		/**
		 * Creates a new instance of the legend part.
		 */
		protected Legend()
		{
			super();
		}
		
		@Override
		public String toString()
		{
			return "Legend";
		}
	}
	
	/**
	 * Part referring to the data series in a plot.
	 */
	public static class DataSeries extends PlotPart
	{
		/**
		 * Creates a new instance of the data series part.
		 */
		protected DataSeries()
		{
			super();
		}
		
		@Override
		public String toString()
		{
			return "Data series";
		}
	}
}

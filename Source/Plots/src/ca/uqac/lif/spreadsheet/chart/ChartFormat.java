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
 * A class whose instances represent formats in which plots can be drawn.
 * Typically, a plot format is an image format (e.g. PNG or JPG), but some
 * extensions of the library support other formats (notably Gnuplot and its
 * "dumb" format).
 *  
 * @author Sylvain Hallé
 */
public final class ChartFormat
{
	/**
	 * An instance of the plot format "PNG".
	 */
	public static final transient ChartFormat PNG = new ChartFormat("PNG", "png", "image/png");
	
	/**
	 * An instance of the plot format "JPEG".
	 */
	public static final transient ChartFormat JPEG = new ChartFormat("JPEG", "jpg", "image/jpeg");
	
	/**
	 * An instance of the plot format "SVG".
	 */
	public static final transient ChartFormat SVG = new ChartFormat("SVG", "svg", "image/svg+xml");
	
	/**
	 * An instance of the plot format "GIF".
	 */
	public static final transient ChartFormat GIF = new ChartFormat("GIF", "gif", "image/gif");
	
	/**
	 * An instance of the plot format "PDF".
	 */
	public static final transient ChartFormat PDF = new ChartFormat("PDF", "pdf", "application/pdf");
	
	/**
	 * The name of this plot format.
	 */
	private final String m_name;
	
	/**
	 * The file extension associated to this plot format.
	 */
	private final String m_extension;
	
	/**
	 * The MIME type associated to this plot format.
	 */
	private final String m_mimeType;
	
	/**
	 * Creates a new plot format of a given name.
	 * @param name The name of the plot format
	 * @param extension The file extension associated to this plot format
	 * @param mime_type The MIME type associated to this plot format
	 */
	public ChartFormat(String name, String extension, String mime_type)
	{
		super();
		m_name = name;
		m_extension = extension;
		m_mimeType = mime_type;
	}
	
	/**
	 * Returns the file extension associated to this plot format.
	 * @return The extension
	 */
	/*@ pure @*/ public String getExtension()
	{
		return m_extension;
	}
	
	/**
	 * Returns the MIME type associated to this plot format.
	 * @return The type
	 */
	/*@ pure @*/ public String getMimeType()
	{
		return m_mimeType;
	}
	
	@Override
	public String toString()
	{
		return m_name;
	}
	
	@Override
	public int hashCode()
	{
		return m_name.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof ChartFormat))
		{
			return false;
		}
		return m_name.compareTo(((ChartFormat) o).m_name) == 0;
	}
}

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
package ca.uqac.lif.spreadsheet.chart.gral;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.Chart;
import ca.uqac.lif.spreadsheet.chart.ChartFormat;
import ca.uqac.lif.spreadsheet.chart.DiscretePalette;
import ca.uqac.lif.spreadsheet.chart.Palette;
import ca.uqac.lif.spreadsheet.chart.UnsupportedPlotFormatException;
import ca.uqac.lif.spreadsheet.chart.UnsupportedSettingException;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.Plot;

/**
 * Top-level class for plots drawn using the GRAL library.
 * @author Sylvain Hallé
 */
public abstract class GralPlot implements Chart
{
	/**
	 * The plot's title.
	 */
	protected String m_title = "";
	
	/**
	 * The caption for the x-axis of the plot.
	 */
	protected String m_captionX = "";

	/**
	 * The caption for the y-axis of the plot.
	 */
	protected String m_captionY = "";

	/**
	 * The caption for the z-axis of the plot.
	 */
	protected String m_captionZ = "";

	/**
	 * Whether to use a logarithmic scale for the X axis
	 */
	protected boolean m_logScaleX = false;

	/**
	 * Whether to use a logarithmic scale for the Y axis
	 */
	protected boolean m_logScaleY = false;

	/**
	 * Whether the plot shows a key
	 */
	protected boolean m_hasKey = true;
	
	/**
	 * The palette used to render the plot.
	 */
	protected Palette m_palette;

	/**
	 * The last spreadsheet given to the plot's
	 * {@link #render(OutputStream, Spreadsheet, ChartFormat, boolean) render}
	 * method.
	 */
	protected Spreadsheet m_lastSpreadsheet = null;

	/**
	 * The format used to render the plot.
	 */
	protected ChartFormat m_format = ChartFormat.PNG;
	
	/**
	 * Creates an empty GRAL plot.
	 */
	public GralPlot()
	{
		super();
		m_palette = DiscretePalette.EGA;
	}

	/**
	 * Runs GRAL on a file and returns the resulting graph
	 * @param term The terminal (i.e. PNG, etc.) to use for the image.
	 * For GRAL plots, PDF is not supported.
	 * @param s The spreadsheet to turn into an image
	 * @param with_caption Set to true to ignore the plot's caption when
	 *   rendering
	 * @return The (binary) contents of the image produced by Gnuplot
	 */
	public final byte[] getImage(Spreadsheet s, ChartFormat term, boolean with_caption)
	{
		if (ChartFormat.PDF.equals(term))
		{
			// Exporting GRAL plots to PDF does not work due to this bug
			// https://github.com/eseifert/gral/issues/173
			return null;
		}
		Plot plot = getPlot(s);
		if (!with_caption)
		{
			// Override caption with empty string
			plot.getTitle().setText("");
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DrawableWriter wr = DrawableWriterFactory.getInstance().get(term.getMimeType());
		try
		{
			wr.write(plot, baos, 640, 480);
			baos.flush();
			byte[] bytes = baos.toByteArray();
			baos.close();
			return bytes;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Sets the color palette to be used to render the plot.
	 * @param p The palette
	 * @return This plot
	 */
	/*@ non_null @*/ public GralPlot setPalette(Palette p)
	{
		m_palette = p;
		return this;
	}

	/**
	 * Gets a Plot object from a spreadsheet.
	 * @param source The spreadsheet from which to obtain a plot
	 * @return The plot
	 */
	public abstract Plot getPlot(Spreadsheet source);

	/**
	 * Customize an existing plot. Override this method to tweak the settings
	 * of a stock plot.
	 * @param plot The plot
	 */
	public void customize(Plot plot)
	{
		// Do nothing
	}

	@Override
	public PartNode getExplanation(Part part)
	{
		return getExplanation(part, NodeFactory.getFactory());
	}

	@Override
	public abstract PartNode getExplanation(Part part, NodeFactory factory);

	@Override
	public GralPlot setTitle(String title)
	{
		m_title = title;
		return this;
	}

	@Override
	public String getTitle()
	{
		return m_title;
	}

	@Override
	public GralPlot setFormat(ChartFormat f)
	{
		if (!(f.equals(ChartFormat.GIF) || f.equals(ChartFormat.JPEG) 
				|| f.equals(ChartFormat.PDF) 
				|| f.equals(ChartFormat.PNG) || f.equals(ChartFormat.SVG)))
		{
			throw new UnsupportedPlotFormatException("Unsupported format " + f);
		}
		m_format = f;
		return this;
	}

	@Override
	public ChartFormat getFormat()
	{
		return m_format;
	}

	@Override
	public GralPlot setCaption(Axis a, String caption)
	{
		switch (a)
		{
		case X:
			m_captionX = caption;
			break;
		case Y:
			m_captionY = caption;
			break;
		case Z:
			m_captionZ = caption;
			break;
		}
		return this;
	}

	@Override
	public String getCaption(Axis a)
	{
		switch (a)
		{
		case X:
			return m_captionX;
		case Y:
			return m_captionY;
		default:
			return m_captionZ;
		}
	}

	@Override
	public GralPlot setLogscale(Axis axis)
	{
		if (axis == Axis.X)
		{
			m_logScaleX = true;
		}
		else
		{
			m_logScaleY = true;
		}
		return this;
	}

	@Override
	public GralPlot setKey(boolean b)
	{
		m_hasKey = b;
		return this;
	}

	@Override
	public boolean hasKey()
	{
		return m_hasKey;
	}

	@Override
	public Chart render(OutputStream out, Spreadsheet s) throws IOException, IllegalArgumentException, UnsupportedPlotFormatException
	{
		return render(out, s, m_format, true);
	}

	@Override
	public GralPlot render(OutputStream os, Spreadsheet table, ChartFormat term, boolean with_title)
			throws IOException, IllegalArgumentException, UnsupportedPlotFormatException
	{
		m_lastSpreadsheet = table;
		if (os == null)
		{
			// Dry run: don't do anything
			return this;
		}
		if (term == null)
		{
			term = m_format;
		}
		Plot plot = getPlot(table);
		if (!with_title)
		{
			// Override caption with empty string
			plot.getTitle().setText("");
		}
		DrawableWriter wr = DrawableWriterFactory.getInstance().get(term.getMimeType());
		try
		{
			wr.write(plot, os, 640, 480);
			os.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public GralPlot set(Object... objects) throws UnsupportedSettingException, IllegalArgumentException
	{
		return this;
	}
	
	/**
	 * Copies the state of the current plot into another plot.
	 * @param p The other plot
	 */
	protected void copyInto(GralPlot p)
	{
		p.m_palette = m_palette;
		p.m_captionX = m_captionX;
		p.m_captionY = m_captionY;
		p.m_captionZ = m_captionZ;
		p.m_hasKey = m_hasKey;
		p.m_logScaleX = m_logScaleX;
		p.m_logScaleY = m_logScaleY;
		p.m_title = m_title;
	}
}

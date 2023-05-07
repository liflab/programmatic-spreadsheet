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
package ca.uqac.lif.spreadsheet.chart.gnuplot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.spreadsheet.AnsiSpreadsheetPrinter;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.Palette;
import ca.uqac.lif.spreadsheet.chart.DiscretePalette;
import ca.uqac.lif.spreadsheet.chart.Chart;
import ca.uqac.lif.spreadsheet.chart.ChartFormat;
import ca.uqac.lif.spreadsheet.chart.UnsupportedPlotFormatException;
import ca.uqac.lif.spreadsheet.chart.UnsupportedSettingException;

/**
 * Top-level class for plots drawn using the GnuPlot software.
 * @author Sylvain Hallé
 */
public abstract class Gnuplot implements Chart, ExplanationQueryable
{
	/**
	 * The "dumb" plot format supported by GnuPlot.
	 */
	public static final transient ChartFormat DUMB = new ChartFormat("dumb", "txt", "text/plain");

	/**
	 * The "GP" plot format. This simply outputs the Gnuplot text file that
	 * Gnuplot uses to render a plot.
	 */
	public static final transient ChartFormat GP = new ChartFormat("Gnuplot", "gp", "text/plain");

	/**
	 * The symbol used to separate data values in a file
	 */
	public static final transient String s_datafileSeparator = ",";

	/**
	 * The symbol used to represent missing values in a file
	 */
	public static final transient String s_datafileMissing = "null";

	/**
	 * A printer to generate the values from a spreadsheet in the CSV format
	 * expected by Gnuplot.
	 */
	protected static final transient AnsiSpreadsheetPrinter s_printer;

	static
	{
		s_printer = new AnsiSpreadsheetPrinter();
		s_printer.setColumnSeparator(",");
		s_printer.setGroupCells(false);
		s_printer.setHeaders(false);
		s_printer.setPadColumns(false);
	}

	/**
	 * The path to launch GnuPlot
	 */
	protected static transient String s_path = "gnuplot";

	/**
	 * The version string obtained when checking if Gnuplot is present
	 */
	protected static transient String s_gnuplotVersionString = checkGnuplot();

	/**
	 * The bytes of a blank PNG image, used as a placeholder when no plot can
	 * be drawn
	 */
	public static final transient byte[] s_blankImagePng = FileHelper.internalFileToBytes(Gnuplot.class, "blank.png");

	/**
	 * The bytes of a blank PDF image, used as a placeholder when no plot can
	 * be drawn
	 */
	public static final transient byte[] s_blankImagePdf = FileHelper.internalFileToBytes(Gnuplot.class, "blank.pdf");

	/**
	 * The fill style used to draw the graph
	 */
	protected transient FillStyle m_fillStyle = FillStyle.SOLID;

	/**
	 * The fill style used for the plot
	 */
	public static enum FillStyle {SOLID, NONE, PATTERN};

	/**
	 * The plot's title.
	 */
	protected String m_title = "";

	/**
	 * The palette used to render the plot.
	 */
	protected Palette m_palette;

	/**
	 * A string defining the plot's borders.
	 */
	protected String m_border = "";

	/**
	 * An optional string containing custom parameters that will be put in
	 * the plot's header
	 */
	protected String m_customParameters = "";

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
	 * The time to wait before polling GnuPlot's result
	 */
	protected static transient long s_waitInterval = 100;

	/**
	 * Creates an empty GnuPlot
	 */
	public Gnuplot()
	{
		super();
		m_palette = DiscretePalette.EGA;
	}

	/**
	 * Sets the border settings for the plot.
	 * @param border A parameter string defining the border for the plot,
	 * according to the <a href="http://gnuplot.sourceforge.net/docs_4.2/node162.html">GnuPlot</a>
	 * syntax
	 * @return This plot
	 */
	public Gnuplot setBorder(String border)
	{
		m_border = border;
		return this;
	}

	/**
	 * Sets custom parameters to be added to the plot's header. This method can
	 * be used to define settings that are not directly handled through object
	 * methods.
	 * @param s The parameter string
	 * @return This plot
	 */
	public Gnuplot setCustomHeader(String s)
	{
		m_customParameters = s;
		return this;
	}

	/**
	 * Generates a stand-alone Gnuplot file for this plot, and prints it to a
	 * print stream.
	 * @param out The print stream where the plot will be printed
	 * @param table The spreadsheet to render
	 * @param term The terminal used to display the plot
	 * @param lab_title The title of the lab. This is only used in the 
	 *   auto-generated comments in the file's header
	 * @param with_caption Set to true to ignore the plot's caption when
	 *   rendering
	 */
	public abstract void toGnuplot(PrintStream out, Spreadsheet table, ChartFormat term, String lab_title, boolean with_caption);

	/**
	 * Generates a stand-alone Gnuplot file for this plot, and prints it to a
	 * print stream.
	 * @param out The print stream where the plot will be printed
	 * @param table The spreadsheet to render
	 * @param term The terminal used to display the plot
	 * @param with_caption Set to false to ignore the plot's caption when
	 *   rendering
	 */
	public final void toGnuplot(PrintStream out, Spreadsheet table, ChartFormat term, boolean with_caption)
	{
		toGnuplot(out, table, term, "", with_caption);
	}

	@Override
	public final Gnuplot render(OutputStream os, Spreadsheet table) throws IOException
	{
		return render(os, table, m_format, true);
	}

	@Override
	public final Gnuplot render(OutputStream os, Spreadsheet table, ChartFormat term, boolean with_caption) throws IOException
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream p_baos = new PrintStream(baos);
		toGnuplot(p_baos, table, term, with_caption);
		String instructions = baos.toString();
		byte[] image = null;
		String[] command = {s_path};
		CommandRunner runner = new CommandRunner(command, instructions);
		runner.start();
		// Wait until the command is done
		while (runner.isAlive())
		{
			// Wait 0.1 s and check again
			try
			{
				Thread.sleep(s_waitInterval);
			}
			catch (InterruptedException e)
			{
				// This happens if the user cancels the command manually
				runner.stopCommand();
				runner.interrupt();
				if (term.equals(ChartFormat.PDF))
				{
					os.write(s_blankImagePdf);
				}
				else if (term.equals(DUMB))
				{
					os.write(new byte[] {});
				}
				else
				{
					os.write(s_blankImagePng);
				}
				return this;
			}
		}
		image = runner.getBytes();
		if (runner.getErrorCode() != 0 || image == null || image.length == 0)
		{
			// Gnuplot could not produce a picture; return the blank image
			if (term.equals(ChartFormat.PDF))
			{
				image = s_blankImagePdf;
			}
			else if (term.equals(DUMB))
			{
				image = new byte[0];
			}
			else
			{
				image = s_blankImagePng;
			}
		}
		os.write(image);
		return this;
	}

	/**
	 * Checks if Gnuplot is present in the system
	 * @return true if Gnuplot is present, false otherwise
	 */
	public static boolean isGnuplotPresent()
	{
		return s_gnuplotVersionString.endsWith("exit code 0");
	}

	/**
	 * Gets a GnuPlot terminal name from an image type
	 * @param t The image type
	 * @return The terminal name
	 */
	public static String getTerminalName(ChartFormat t)
	{
		return t.toString().toLowerCase();
	}

	/**
	 * Produces a header that is common to all plots generated by the
	 * application
	 * @param out The print stream where to print the header
	 * @param term The terminal to display this plot
	 * @param comment_line A line to add in the header comments
	 * @param with_caption Set to true to ignore the plot's caption when
	 *   rendering
	 */
	protected void printHeader(PrintStream out, ChartFormat term, String comment_line, boolean with_caption)
	{
		out.println("# ----------------------------------------------------------------");
		out.println("# " + comment_line);
		out.println("# ----------------------------------------------------------------");
		if (with_caption)
		{
			out.println("set title \"" + m_title + "\"");
		}
		out.println("set datafile separator \"" + s_datafileSeparator + "\"");
		out.println("set datafile missing \"" + s_datafileMissing + "\"");
		out.println("set terminal " + getTerminalName(term));
		switch (m_fillStyle)
		{
		case PATTERN:
			out.println("set style fill pattern");
			break;
		case SOLID:
			out.println("set style fill solid");
			break;
		default:
			// Do nothing
		}
		if (m_border != null && !m_border.isEmpty())
		{
			out.println("set border " + m_border);
		}
		if (m_customParameters != null && m_customParameters.isEmpty())
		{
			out.println(m_customParameters);
		}
	}

	/**
	 * Sets the color palette to be used to render the plot.
	 * @param p The palette
	 * @return This plot
	 */
	/*@ non_null @*/ public Gnuplot setPalette(Palette p)
	{
		m_palette = p;
		return this;
	}

	@Override
	public Chart set(Object... objects)
			throws UnsupportedSettingException, IllegalArgumentException
	{
		if (objects[0] instanceof String)
		{
			m_customParameters = (String) objects[0];
		}
		return this;
	}

	@Override
	public Gnuplot setTitle(String title)
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
	public Gnuplot setFormat(ChartFormat f)
	{
		if (!(f.equals(ChartFormat.GIF) || f.equals(ChartFormat.JPEG) 
				|| f.equals(ChartFormat.PDF) || f.equals(ChartFormat.PNG)
				|| f.equals(ChartFormat.SVG) || f.equals(DUMB)))
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
	public Gnuplot setCaption(Axis a, String caption)
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
	public Gnuplot setLogscale(Axis axis)
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
	public Gnuplot setKey(boolean b)
	{
		m_hasKey = b;
		return this;
	}

	@Override
	public boolean hasKey()
	{
		return m_hasKey;
	}

	/**
	 * Copies the state of the current plot into another plot.
	 * @param p The other plot
	 */
	protected void copyInto(Gnuplot p)
	{
		p.m_border = m_border;
		p.m_captionX = m_captionX;
		p.m_captionY = m_captionY;
		p.m_captionZ = m_captionZ;
		p.m_customParameters = m_customParameters;
		p.m_fillStyle = m_fillStyle;
		p.m_hasKey = m_hasKey;
		p.m_logScaleX = m_logScaleX;
		p.m_logScaleY = m_logScaleY;
		p.m_palette = m_palette;
		p.m_title = m_title;
	}

	/**
	 * Gets the fill color associated with a number, based on the palette
	 * defined for this plot.
	 * @param color_nb The color number
	 * @return An empty string if no palette is defined, otherwise the
	 *   <tt>fillcolor</tt> expression corresponding to the color
	 */
	protected final String getFillColor(int color_nb)
	{
		if (m_palette == null || m_fillStyle != FillStyle.SOLID)
		{
			return "";
		}
		return "fillcolor rgb \"" + m_palette.getHexColor(color_nb) + "\"";
	}

	/**
	 * Gets the version string obtained when checking if Gnuplot is present 
	 * @return The version string
	 */
	public static String getGnuplotVersionString()
	{
		return s_gnuplotVersionString;
	}

	/**
	 * Checks if Gnuplot is present on the system
	 * @return A string with the version and exit code obtained when 
	 *   attempting to run Gnuplot
	 */
	protected static String checkGnuplot()
	{
		CommandRunner runner = new CommandRunner(new String[]{"gnuplot", "--version"});
		runner.execute();
		return runner.getString().trim() + ", exit code " + runner.getErrorCode();
	}

	@Override
	public PartNode getExplanation(Part part)
	{
		return getExplanation(part, RelationNodeFactory.getFactory());
	}

	@Override
	public PartNode getExplanation(Part d, RelationNodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		if (d instanceof Part.Self)
		{
			root.addChild(f.getPartNode(d, m_lastSpreadsheet));
			return root;
		}
		if (!(d instanceof ComposedPart))
		{
			root.addChild(f.getUnknownNode());
			return root;
		}
		ComposedPart cp = (ComposedPart) d;
		Part p1 = cp.get(cp.size() - 1);
		if (!(p1 instanceof Part.Self))
		{
			root.addChild(f.getUnknownNode());
			return root;
		}
		if (cp.size() == 1)
		{
			root.addChild(f.getPartNode(d, m_lastSpreadsheet));
			return root;
		}
		// Isolate the elements of d that apply to the plot
		Part to_explain = Part.self;
		int i = cp.size() - 2;
		for (; i >= 0; i--)
		{
			Part p = cp.get(i);
			if (p.appliesTo(this))
			{
				to_explain = ComposedPart.compose(p, to_explain); 
			}
			else
			{
				break;
			}
		}
		Part suffix = null;
		if (i >= 0)
		{
			suffix = cp.subPart(0, i + 1);
		}
		explainChartPart(to_explain.tail(), suffix, root, f);
		return root;
	}

	protected void explainChartPart(Part to_explain, Part suffix, PartNode root, RelationNodeFactory f)
	{
		root.addChild(f.getUnknownNode());
	}

	/**
	 * Gets the Gnuplot string corresponding to the definition of a
	 * discrete palette.
	 * @param out The print stream where to print the palette declaration
	 * @param p The palette
	 * @return The palette declaration
	 */
	public Gnuplot printPaletteDeclaration(PrintStream out, DiscretePalette p) 
	{
		out.println("# line styles");
		for (int i = 0; i < p.colorCount(); i++)
		{
			out.println("set style line " + (i + 1) + " lc rgb \"" + p.getHexColor(i) + "\"");
		}
		out.println("set palette maxcolors " + p.colorCount());
		out.print("set palette defined (");
		for (int i = 0; i < p.colorCount(); i++)
		{
			if (i > 0)
			{
				out.print(", ");
			}
			out.print(i + " \"" + p.getHexColor(i) + "\"");
		}
		out.println(")");
		return this;
	}
}

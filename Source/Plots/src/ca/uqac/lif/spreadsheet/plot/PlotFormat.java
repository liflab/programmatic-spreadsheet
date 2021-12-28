package ca.uqac.lif.spreadsheet.plot;

public class PlotFormat
{
	/**
	 * An instance of the plot format "PNG".
	 */
	public static final transient PlotFormat PNG = new PlotFormat("PNG");
	
	/**
	 * An instance of the plot format "JPEG".
	 */
	public static final transient PlotFormat JPEG = new PlotFormat("JPEG");
	
	/**
	 * An instance of the plot format "SVG".
	 */
	public static final transient PlotFormat SVG = new PlotFormat("SVG");
	
	/**
	 * An instance of the plot format "GIF".
	 */
	public static final transient PlotFormat GIF = new PlotFormat("GIF");
	
	/**
	 * An instance of the plot format "PDF".
	 */
	public static final transient PlotFormat PDF = new PlotFormat("PDF");
	
	/**
	 * The name of this plot format.
	 */
	private final String m_name;
	
	/**
	 * Creates a new plot format of a given name.
	 * @param name The name of the plot format
	 */
	public PlotFormat(String name)
	{
		super();
		m_name = name;
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
		if (o == null || !(o instanceof PlotFormat))
		{
			return false;
		}
		return m_name.compareTo(((PlotFormat) o).m_name) == 0;
	}
}

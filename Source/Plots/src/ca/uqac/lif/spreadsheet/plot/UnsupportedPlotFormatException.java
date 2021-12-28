package ca.uqac.lif.spreadsheet.plot;

/**
 * Exception indicating that a plot is asked to be rendered in a format that it
 * does not support.
 */
public class UnsupportedPlotFormatException extends Exception
{
	/**
	 * Dummy UID.
	 */
	private static final long serialVersionUID = 1L;
	
	public UnsupportedPlotFormatException(Throwable t)
	{
		super(t);
	}
	
	public UnsupportedPlotFormatException(String message)
	{
		super(message);
	}
}

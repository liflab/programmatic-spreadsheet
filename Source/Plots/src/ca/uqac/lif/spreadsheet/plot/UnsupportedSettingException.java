package ca.uqac.lif.spreadsheet.plot;

/**
 * Exception indicating that a plot has been given a setting that it does not
 * support.
 */
public class UnsupportedSettingException extends RuntimeException
{
	/**
	 * Dummy UID.
	 */
	private static final long serialVersionUID = 1L;
	
	public UnsupportedSettingException(Throwable t)
	{
		super(t);
	}
	
	public UnsupportedSettingException(String message)
	{
		super(message);
	}
}

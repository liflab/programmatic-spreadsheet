package ca.uqac.lif.spreadsheet.plot;

import java.io.IOException;
import java.io.OutputStream;

import ca.uqac.lif.spreadsheet.Spreadsheet;

public interface AbstractPlot
{
	/**
	 * The three possible axes of a plot.
	 */
	public enum Axis {X, Y, Z}
	
	/**
	 * Sets the plot's title.
	 * @param title The title
	 * @return This plot
	 */
	/*@ non_null @*/ public AbstractPlot setTitle(/*@ non_null @*/ String title);
	
	/**
	 * Gets the plot's title.
	 * @return The title
	 */
	/*@ non_null @*/ public String getTitle();
	
	/**
	 * Sets the plot's caption for a given axis.
	 * @param a The axis
	 * @param caption The caption
	 * @return This plot
	 */
	/*@ non_null @*/ public AbstractPlot setCaption(/*@ non_null @*/ Axis a, /*@ non_null @*/ String caption);
	
	/**
	 * Gets the plot's caption for a given axis.
	 * @param a The axis
	 * @return The caption
	 */
	/*@ non_null @*/ public String getCaption(/*@ non_null @*/ Axis a);
	
	/**
	 * Sets whether to use a log scale for one of the axes 
	 * @param a The axis
	 * @return This plot
	 */
	public AbstractPlot setLogscale(Axis a);
	
	/**
	 * Determines if this plot shows a key when it has multiple data series
	 * @return {@code true} if the key is enabled, {@code false} otherwise
	 */
	public boolean hasKey();
	
	/**
	 * Sets if this plot shows a key when it has multiple data series
	 * @param b Set to {@code true} to enable the key, {@code false}
	 * otherwise
	 * @return This plot
	 */
	public AbstractPlot setKey(boolean b);
	
	/**
	 * Renders (i.e. draws) a plot into an output stream.
	 * @param out The output stream where the plot is to be rendered
	 * @param s The spreadsheet data to render
	 * @param f The plot format to render
	 * @param with_title Whether to render the plot with its title
	 * @return This plot
	 * @throws IOException If the rendering could not be done due to an I/O issue
	 * @throws IllegalArgumentException If the input spreadsheet does not have
	 * the expected format for the type of plot to be rendered
	 * @throws UnsupportedPlotFormatException If the plot is aksed to be rendered
	 * in a format that it does not support
	 */
	public AbstractPlot render(OutputStream out, Spreadsheet s, PlotFormat f, boolean with_title) throws IOException, IllegalArgumentException, UnsupportedPlotFormatException;
	
	/**
	 * Defines settings for this plot. This method is intended as a "last resort"
	 * to define settings for a plot that are not supported by other explicit
	 * methods.
	 * @param objects The objects used to define the settings
	 * @return This plot
	 * @throws IllegalArgumentException If the input objects do not correspond
	 * to a valid setting
	 * @throws UnsupportedSettingException If the input objects correspond to a
	 * setting that is not supported by the plot
	 */
	public AbstractPlot set(Object ... objects) throws UnsupportedSettingException, IllegalArgumentException;
	
	/**
	 * Creates a deep copy of the current plot with all its settings.
	 * @return A copy of the plot
	 */
	public AbstractPlot duplicate();
}

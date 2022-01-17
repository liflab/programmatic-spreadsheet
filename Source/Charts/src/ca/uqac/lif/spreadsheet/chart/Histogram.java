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
package ca.uqac.lif.spreadsheet.chart;

/**
 * Two-dimensional bar diagram, also called a "clustered histogram".
 * <p>
 * <strong>Example usage.</strong> 
 * Suppose you have a set of experiments, each with three
 * parameters:
 * <ul>
 * <li><tt>name</tt> is the name of a web browser (Firefox, IE, etc.)</li>
 * <li><tt>market</tt> is the name of a market (video, audio, etc.)</li>
 * <li><tt>share</tt> is the market share (in %) for this browser in this market</li>
 * </ul>
 * We wish to create a bar diagram where each bar represents a market,
 * its height corresponds to the share, with one group of bars for each
 * browser. To is done by writing:
 * <pre>
 * BarPlot plot = new BarPlot();
 * ...
 * plot.useForX("browser").useForY("share").groupBy("market");
 * </pre>
 * This will create a histogram that looks like this:
 * <pre>
 * |                     # video
 * |                     $ audio
 * |                     @ text
 * |    $
 * |    $@         @
 * |   #$@        $@
 * |   #$@       #$@
 * +----+---------+-----&gt;
 *   Firefox     IE
 * </pre>
 * @author Sylvain Hallé
 *
 */
public interface Histogram
{
	/**
	 * Sets whether the histogram is of type "row stacked".
	 * Using the example given above, the rowstacked setting will rather
	 * produce this plot:
	 * <pre>
   * |                     # video
   * |                     $ audio
   * |    @                @ text
   * |    @         @
   * |    $         @ 
   * |    $         $ 
   * |    #         # 
   * +----+---------+-----&gt;
   *   Firefox     IE
	 * </pre> 
	 * @return This plot
	 */
	/*@ non_null @*/ public Histogram rowStacked();
	
	/**
	 * Sets the box width of the histogram.
	 * @param w The width (generally a value between 0 and 1)
	 * @return This plot
	 */
	/*@ non_null @*/ public Histogram boxWidth(float w);
}

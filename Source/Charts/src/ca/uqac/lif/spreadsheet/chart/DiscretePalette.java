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

import java.awt.Color;

/**
 * Palette with a fixed number of colors
 */
public class DiscretePalette implements Palette 
{
	/**
	 * An 8-color preset palette for qualitative data:
	 * <span style="color:#E41A1C">&#x25A0;</span>
	 * <span style="color:#377EB8">&#x25A0;</span>
	 * <span style="color:#4DAF4A">&#x25A0;</span>
	 * <span style="color:#984EA3">&#x25A0;</span>
	 * <span style="color:#FF7F00">&#x25A0;</span>
	 * <span style="color:#FFFF33">&#x25A0;</span>
	 * <span style="color:#A65628">&#x25A0;</span>
	 * <span style="color:#F781BF">&#x25A0;</span>
	 * <p>
	 * This palette corresponds to the preset <tt>Set1.ptl</tt> from
	 * <a href="https://github.com/aschn/gnuplot-colorbrewer">gnuplot-colorbrewer</a>.
	 * 
	 */
	public static final transient Palette QUALITATIVE_1;

	/**
	 * An 8-color preset palette for qualitative data:
	 * <span style="color:#66C2A5">&#x25A0;</span>
	 * <span style="color:#FC8D62">&#x25A0;</span>
	 * <span style="color:#8DA0CB">&#x25A0;</span>
	 * <span style="color:#E78AC3">&#x25A0;</span>
	 * <span style="color:#A6D854">&#x25A0;</span>
	 * <span style="color:#FFD92F">&#x25A0;</span>
	 * <span style="color:#E5C494">&#x25A0;</span>
	 * <span style="color:#B3B3B3">&#x25A0;</span>
	 * <p>
	 * This palette corresponds to the preset <tt>Set2.ptl</tt> from
	 * <a href="https://github.com/aschn/gnuplot-colorbrewer">gnuplot-colorbrewer</a>.
	 */
	public static final transient Palette QUALITATIVE_2; 

	/**
	 * An 8-color preset palette for qualitative data:
	 * <span style="color:#8DD3C7">&#x25A0;</span>
	 * <span style="color:#FFFFB3">&#x25A0;</span>
	 * <span style="color:#BEBADA">&#x25A0;</span>
	 * <span style="color:#FB8072">&#x25A0;</span>
	 * <span style="color:#80B1D3">&#x25A0;</span>
	 * <span style="color:#FDB462">&#x25A0;</span>
	 * <span style="color:#B3DE69">&#x25A0;</span>
	 * <span style="color:#FCCDE5">&#x25A0;</span>
	 * <p>
	 * This palette corresponds to the preset <tt>Set3.ptl</tt> from
	 * <a href="https://github.com/aschn/gnuplot-colorbrewer">gnuplot-colorbrewer</a>.
	 */
	public static final transient Palette QUALITATIVE_3;

	/**
	 * A 16-color preset palette for qualitative data, corresponding to the
	 * 16 EGA colors.
	 */
	public static final transient Palette EGA;

	static 
	{
		// Setup of discrete palettes
		// Found from https://github.com/aschn/gnuplot-colorbrewer
		QUALITATIVE_1 = new DiscretePalette("#E41A1C", "#377EB8", "#4DAF4A", "#984EA3", "#FF7F00", "#FFFF33", "#A65628", "#F781BF");
		QUALITATIVE_2 = new DiscretePalette("#66C2A5", "#FC8D62", "#8DA0CB", "#E78AC3", "#A6D854", "#FFD92F", "#E5C494", "#B3B3B3");
		QUALITATIVE_3 = new DiscretePalette("#8DD3C7", "#FFFFB3", "#BEBADA", "#FB8072", "#80B1D3", "#FDB462", "#B3DE69", "#FCCDE5");
		EGA = new DiscretePalette("#5555FF", "#55FF55", "#55FFFF", "#FF5555", "#FF55FF", "#FFFF55", "#0000AA", "#00AA00", "#00AAAA", "#AA0000", "#AA00AA", "#AA5500", "#AAAAAA", "#555555", "#FFFFFF", "#000000");
	}
	
	protected String[] m_colors;

	public DiscretePalette(String ... colors)
	{
		super();
		m_colors = colors;
	}

	/**
	 * Gets the number of colors in the palette.
	 * @return The number of colors
	 */
	public int colorCount()
	{
		return m_colors.length;
	}

	@Override
	public String getHexColor(int color_nb) 
	{
		return m_colors[color_nb % m_colors.length];
	}

	@Override
	public Color getPaint(int color_nb)
	{
		String color_hex = getHexColor(color_nb);
		return Color.decode(color_hex);
	}
}

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
package ca.uqac.lif.spreadsheet.chart.gral;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.chart.UnsupportedPlotFormatException;
import ca.uqac.lif.spreadsheet.chart.Chart.Axis;

/**
 * Unit tests for {@link GralScatterplot}.
 */
public class GralScatterplotTest
{
	@Test
	public void test1() throws IllegalArgumentException, UnsupportedPlotFormatException, IOException
	{
		Spreadsheet s = Spreadsheet.read(3, 11, 
				"x", "Apples", "Oranges",
				0,   0,        3, 
				1,   1,        1,
				2,   2,        4,
				3,   3,        1,
				4,   4,        5,
				5,   5,        9,
				6,   6,        2,
				7,   7,        6,
				8,   8,        5,
				9,   9,        3);
		GralScatterplot plot = new GralScatterplot()
				.setTitle("Apples and oranges").setCaption(Axis.Y, "Fruits");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		plot.render(baos, s);
		// We cannot really "test" the picture, so we check that render produces
		// a non-empty byte array
		byte[] bytes = baos.toByteArray();
		assertNotNull(bytes);
		assertTrue(bytes.length > 0);
	}
}

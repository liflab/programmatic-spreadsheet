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
package ca.uqac.lif.spreadsheet.functions;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ca.uqac.lif.spreadsheet.Spreadsheet;
import static ca.uqac.lif.spreadsheet.functions.GetFrequencies.createPair;

/**
 * Unit tests for {@link GetFrequencies}.
 */
public class GetFrequenciesTest
{
	@Test
	public void test1()
	{
		GetFrequencies f = new GetFrequencies(0, 12, 4, 0, 6, 3);
		List<Double[]> list = Arrays.asList(
				createPair(1, 1),
				createPair(3, 5),
				createPair(2, 1),
				createPair(7, 3)
				);
		Spreadsheet out = (Spreadsheet) f.evaluate(list)[0];
		assertNotNull(out);
		assertEquals(5, out.getWidth());
		assertEquals(4, out.getHeight());
		assertEquals(Spreadsheet.read(5, 4, 
				null, 0, 3, 6, 9,
				0, 2, 0, 0, 0,
				2, 0, 0, 1, 0,
				4, 0, 1, 0, 0), out);
	}
}

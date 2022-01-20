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
package ca.uqac.lif.spreadsheet.units;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ca.uqac.lif.numbers.FloatingPoint;
import ca.uqac.lif.numbers.Real;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.units.imperial.Inch;
import ca.uqac.lif.units.si.Centimeter;
import ca.uqac.lif.units.si.Second;

/**
 * Unit tests for {@link MoveUnitsToHeader}.
 */
public class MoveUnitsToHeaderTest
{
	@Test
	public void test1()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"n", "Time",      "Distance",
				0,   s(0),        cm(2.1, 0.1),
				1,   s(1, 0.5),   cm(3.25, 0.02),
				2,   s(1.3, 0.5), in(2.25, 0.25));
		MoveUnitsToHeader f = new MoveUnitsToHeader();
		Spreadsheet out = (Spreadsheet) f.evaluate(s)[0];
		assertEquals(Spreadsheet.read(3, 4, 
				"n", "Time (s)",   "Distance (cm)",
				0,   fp(0) ,       fp(2.1, 0.1),
				1,   fp(1, 0.5),   fp(3.25, 0.02),
				2,   fp(1.3, 0.5), fp(5.8, 0.8)), out);
	}
	
	protected static Centimeter cm(double v, double i)
	{
		return new Centimeter(FloatingPoint.get(v, i));
	}
	
	protected static Inch in(double v, double i)
	{
		return new Inch(FloatingPoint.get(v, i));
	}
	
	protected static Second s(double v, double i)
	{
		return new Second(FloatingPoint.get(v, i));
	}
	
	protected static Second s(double v)
	{
		return new Second(FloatingPoint.get(v));
	}
	
	protected static Real fp(double v, double i)
	{
		return FloatingPoint.get(v, i);
	}
	
	protected static Real fp(double v)
	{
		return FloatingPoint.get(v);
	}
}

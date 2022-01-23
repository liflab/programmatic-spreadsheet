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
import ca.uqac.lif.numbers.RealPart;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.units.DimensionValuePart;
import ca.uqac.lif.units.imperial.Inch;
import ca.uqac.lif.units.si.Centimeter;
import ca.uqac.lif.units.si.Second;

/**
 * Unit tests for {@link MoveUnitsToHeader}.
 */
public class MoveUnitsToHeaderTest
{
	@Test
	public void testOutput1()
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
	
	@Test
	public void testExplain1()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"n", "Time",      "Distance",
				0,   s(0),        cm(2.1, 0.1),
				1,   s(1, 0.5),   cm(3.25, 0.02),
				2,   s(1.3, 0.5), in(2.25, 0.25));
		MoveUnitsToHeader f = new MoveUnitsToHeader();
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(1, 2), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(1, 2), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain2()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"n", "Time",      "Distance",
				0,   s(0),        cm(2.1, 0.1),
				1,   s(1, 0.5),   cm(3.25, 0.02),
				2,   s(1.3, 0.5), in(2.25, 0.25));
		MoveUnitsToHeader f = new MoveUnitsToHeader();
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(DimensionValuePart.scalar, Cell.get(1, 2), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(DimensionValuePart.scalar, Cell.get(1, 2), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain3()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"n", "Time",      "Distance",
				0,   s(0),        cm(2.1, 0.1),
				1,   s(1, 0.5),   cm(3.25, 0.02),
				2,   s(1.3, 0.5), in(2.25, 0.25));
		MoveUnitsToHeader f = new MoveUnitsToHeader();
		f.evaluate(s);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(2, 0), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Cell.get(2, 0), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain4()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"n", "Time",      "Distance",
				0,   s(0),        cm(2.1, 0.1),
				1,   s(1, 0.5),   cm(3.25, 0.02),
				2,   s(1.3, 0.5), in(2.25, 0.25));
		MoveUnitsToHeader f = new MoveUnitsToHeader();
		f.evaluate(s);
		// Explanation of substring "Dist" in 2nd column
		PartNode root = f.getExplanation(ComposedPart.compose(new Range(0, 3), Cell.get(2, 0), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(new Range(0, 3), Cell.get(2, 0), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain5()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"n", "Time",      "Distance",
				0,   s(0),        cm(2.1, 0.1),
				1,   s(1, 0.5),   cm(3.25, 0.02),
				2,   s(1.3, 0.5), in(2.25, 0.25));
		MoveUnitsToHeader f = new MoveUnitsToHeader();
		f.evaluate(s);
		// Explanation of substring "cm" in 2nd column
		PartNode root = f.getExplanation(ComposedPart.compose(new Range(10, 11), Cell.get(2, 0), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(DimensionValuePart.unitName, Cell.get(2, 1), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain6()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"n", "Time",      "Distance",
				0,   s(0),        cm(2.1, 0.1),
				1,   s(1, 0.5),   cm(3.25, 0.02),
				2,   s(1.3, 0.5), in(2.25, 0.25));
		MoveUnitsToHeader f = new MoveUnitsToHeader();
		f.evaluate(s);
		// Explanation of substring "m" in 2nd column
		PartNode root = f.getExplanation(ComposedPart.compose(new Range(11, 11), Cell.get(2, 0), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(new Range(1, 1), DimensionValuePart.unitName, Cell.get(2, 1), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain7()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"n", "Time",      "Distance",
				0,   s(0),        null,
				1,   s(1, 0.5),   cm(3.25, 0.02),
				2,   s(1.3, 0.5), in(2.25, 0.25));
		MoveUnitsToHeader f = new MoveUnitsToHeader();
		f.evaluate(s);
		// Explanation of substring "cm" in 2nd column
		PartNode root = f.getExplanation(ComposedPart.compose(new Range(10, 11), Cell.get(2, 0), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(DimensionValuePart.unitName, Cell.get(2, 2), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplain8()
	{
		Spreadsheet s = Spreadsheet.read(3, 4, 
				"n", "Time",      "Distance",
				0,   s(0),        null,
				1,   s(1, 0.5),   cm(3.25, 0.02),
				2,   s(1.3, 0.5), in(2.25, 0.25));
		MoveUnitsToHeader f = new MoveUnitsToHeader();
		f.evaluate(s);
		// Explanation of the uncertainty of cell 2,3
		PartNode root = f.getExplanation(ComposedPart.compose(RealPart.uncertainty, Cell.get(2, 3), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(RealPart.uncertainty, Cell.get(2, 3), NthInput.FIRST), child.getPart());
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

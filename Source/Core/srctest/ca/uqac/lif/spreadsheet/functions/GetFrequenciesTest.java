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

import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;
import ca.uqac.lif.spreadsheet.Cell;
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
				0,    2, 0, 0, 0,
				2,    0, 0, 1, 0,
				4,    0, 1, 0, 0), out);
	}
	
	@Test
	public void testExplanation1()
	{
		GetFrequencies f = new GetFrequencies(0, 12, 4, 0, 6, 3);
		List<Double[]> list = Arrays.asList(
				createPair(1, 1),
				createPair(3, 5),
				createPair(2, 1),
				createPair(7, 3)
				);
		f.evaluate(list);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(2, 3), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(new NthElement(1), NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplanation2()
	{
		GetFrequencies f = new GetFrequencies(0, 12, 4, 0, 6, 3);
		List<Double[]> list = Arrays.asList(
				createPair(1, 1),
				createPair(3, 5),
				createPair(2, 1),
				createPair(7, 3)
				);
		f.evaluate(list);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(3, 1), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(ComposedPart.compose(Part.nothing, NthInput.FIRST), child.getPart());
	}
	
	@Test
	public void testExplanation3()
	{
		GetFrequencies f = new GetFrequencies(0, 12, 4, 0, 6, 3);
		List<Double[]> list = Arrays.asList(
				createPair(1, 1),
				createPair(3, 5),
				createPair(2, 1),
				createPair(7, 3)
				);
		f.evaluate(list);
		PartNode root = f.getExplanation(ComposedPart.compose(Cell.get(1, 1), NthOutput.FIRST));
		assertEquals(1, root.getOutputLinks(0).size());
		AndNode and = (AndNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(2, and.getOutputLinks(0).size());
		{
			PartNode child = (PartNode) and.getOutputLinks(0).get(0).getNode();
			assertEquals(ComposedPart.compose(new NthElement(0), NthInput.FIRST), child.getPart());	
		}
		{
			PartNode child = (PartNode) and.getOutputLinks(0).get(1).getNode();
			assertEquals(ComposedPart.compose(new NthElement(2), NthInput.FIRST), child.getPart());	
		}
	}
}

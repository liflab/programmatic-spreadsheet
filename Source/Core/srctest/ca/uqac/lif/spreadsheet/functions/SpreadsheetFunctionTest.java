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

import org.junit.Test;

import ca.uqac.lif.dag.Node;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;

public class SpreadsheetFunctionTest
{
	@Test
	public void dummyTest()
	{
		// A dummy test to avoid JUnit errors 
	}
	
	public static void assertExplains(AtomicFunction f, Part output, Part input)
	{
		PartNode root = f.getExplanation(output);
		assertEquals(1, root.getOutputLinks(0).size());
		Node n = root.getOutputLinks(0).get(0).getNode();
		assertTrue(n instanceof PartNode);
		PartNode child = (PartNode) n;
		assertEquals(input, child.getPart());
	}
	
	public static void assertNotExplains(AtomicFunction f, Part output)
	{
		PartNode root = f.getExplanation(output);
		assertEquals(0, root.getOutputLinks(0).size());
	}
}

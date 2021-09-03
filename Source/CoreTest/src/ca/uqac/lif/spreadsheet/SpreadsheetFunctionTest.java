package ca.uqac.lif.spreadsheet;

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

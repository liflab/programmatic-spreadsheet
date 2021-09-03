package ca.uqac.lif.spreadsheet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;

public class SpreadsheetFunctionTest
{
	@Test
	public void dummyTest()
	{
		// A dummy test to avoid JUnit errors 
	}
	
	public static void assertExplains(SpreadsheetFunction f, Part output, Part input)
	{
		PartNode root = f.getExplanation(output);
		assertEquals(1, root.getOutputLinks(0).size());
		PartNode child = (PartNode) root.getOutputLinks(0).get(0).getNode();
		assertEquals(input, child.getPart());
	}
}

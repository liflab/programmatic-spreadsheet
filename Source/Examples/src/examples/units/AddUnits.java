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
package examples.units;

import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.ApplyFormula;
import ca.uqac.lif.spreadsheet.units.MoveUnitsToHeader;
import ca.uqac.lif.units.DimensionValuePart;
import ca.uqac.lif.units.functions.UnitAdd;
import examples.util.GraphViewer;

import static ca.uqac.lif.dag.NodeConnector.connect;

import static ca.uqac.lif.units.util.UnitUtils.*;

import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.Circuit;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.strings.Range;

public class AddUnits
{
	public static void main(String[] args)
	{
		Spreadsheet s = Spreadsheet.read(3, 4,
				"A",        "B",        "Sum",
				cm(2, 0.5), in(1, 0.1), null,
				cm(3, 0.5), cm(3, 0.2), null,
				cm(2, 0.1), in(1, 0.2), null);
		Circuit c = new Circuit(1, 1);
		{
			ApplyFormula f = new ApplyFormula();
			c.associateInput(0, f.getInputPin(0));
			for (int row = 1; row < s.getHeight(); row++)
			{
				f.add(Cell.get(2, row), UnitAdd.add(Cell.get(0, row), Cell.get(1, row)));
			}
			MoveUnitsToHeader h = new MoveUnitsToHeader();
			connect(f, 0, h, 0);
			c.associateOutput(0, h.getOutputPin(0));
		}
		Spreadsheet result = (Spreadsheet) c.evaluate(s)[0];
		System.out.println(result);
		Part part = ComposedPart.compose(new Range(5, 6), Cell.get(2, 0), NthOutput.FIRST);
		PartNode graph = c.getExplanation(part);
		GraphViewer.display(graph);
	}
}

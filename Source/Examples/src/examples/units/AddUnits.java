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
import ca.uqac.lif.spreadsheet.CellRange;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.ApplyFormula;
import ca.uqac.lif.spreadsheet.units.MoveUnitsToHeader;
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

/**
 * Creates a spreadsheet containing dimensional values and performs operations
 * on these cells.
 * <p>
 * The program starts by creating this spreadsheet:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th><th>Sum</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>(2 ± 0.5) cm</td><td>(1 ± 0.2)"</td><td>(2 ± 0.2)"</td><td></td></tr>
 * <tr><td>(3 ± 0.5) cm</td><td>(3 ± 0.2) cm</td><td>(1 ± 0.2) cm</td><td></td></tr>
 * <tr><td>(2 ± 0.1) cm</td><td>(1 ± 0.2)"</td><td>(1.5 ± 0.1) cm</td><td></td></tr>
 * </tbody>
 * </table>
 * <p>
 * It then creates a function circuit composed of two successive operations:
 * <ol>
 * <li>Filling the last cell of each row with the <em>dimensional</em> sum of
 * the values in all cells to its left. This is done by creating an instance of
 * {@link ApplyFormula}, and passing to it a {@link UnitAdd} circuit for each
 * row</li>
 * <li>Uniformizing the units of each column and moving their name to the
 * column header. This is done by passing the spreadsheet to an instance of
 * {@link MoveUnitsToHeader}.</li>
 * </ol>
 * <p>
 * Passing the original spreadsheet to this circuit results in the following
 * output:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>A (cm)</th><th>B (")</th><th>C (")</th><th>Sum (<u>cm</u>)</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>2 ± 0.5</td><td>1 ± 0.2</td><td>2 ± 0.2</td><td>10 ± 3</td></tr>
 * <tr><td>3 ± 0.5</td><td>1.18 ± 0.08</td><td>0.39 ± 0.08</td><td>7 ± 1</td></tr>
 * <tr><td>2 ± 0.1</td><td>1 ± 0.3</td><td>0.59 ± 0.04</td><td>6 ± 0.8</td></tr>
 * </tbody>
 * </table>
 * <p>
 * In addition, the program makes a provenance query on the circuit, by
 * requesting the explanation for the range of characters 5-6 in cell D1
 * (the string "cm", underlined in the table above). This yields the following
 * explanation graph:
 * <p>
 * <img src="{@docRoot}/doc-files/units/AddUnits-exp.png" alt="Explanation graph" />
 * <p>
 * As one can see, "cm" can be traced to the name of the unit in the value of
 * cell A2 (or 0:1) in the original spreadsheet. Indeed, the sum of this row is
 * expressed in centimeters (since this is the unit of the first value of the
 * sum), resulting in column D being expressed in centimeters (since the first
 * value of this column is expressed in centimeters).  
 */
public class AddUnits
{
	public static void main(String[] args)
	{
		/* Create a spreadsheet with dimensional units and leave last column
		 * blank */
		Spreadsheet s = Spreadsheet.read(4, 4,
				"A",        "B",        "C",          "Sum",
				cm(2, 0.5), in(1, 0.1), in(2, 0.1),   null,
				cm(3, 0.5), cm(3, 0.2), cm(1, 0.2),   null,
				cm(2, 0.1), in(1, 0.2), cm(1.5, 0.1), null);
		
		/* Create a circuit applying two transformations:
		 * 1. Fill the value of each cell of the last column with the *dimensional*
		 *    sum of the cells to its left
		 * 2. Uniformize the units of each column and move their name to the column
		 *    header 
		 */
		Circuit c = new Circuit(1, 1, "Add units");
		{
			ApplyFormula f = new ApplyFormula();
			c.associateInput(0, f.getInputPin(0));
			for (int row = 1; row < s.getHeight(); row++)
			{
				f.add(Cell.get(3, row), UnitAdd.add((Object[]) CellRange.getRow(row, 0, 2)));
			}
			MoveUnitsToHeader h = new MoveUnitsToHeader();
			connect(f, 0, h, 0);
			c.associateOutput(0, h.getOutputPin(0));
		}
		
		/* Evaluate the circuit on the input spreadsheet and print the result */
		Spreadsheet result = (Spreadsheet) c.evaluate(s)[0];
		System.out.println(result);
		
		/* As a bonus: provenance query. Ask for the explanation of characters 5-6
		 * in the text of cell D1, and show it as a graph. */
		Part part = ComposedPart.compose(new Range(5, 6), Cell.get("D1"), NthOutput.FIRST);
		PartNode graph = c.getExplanation(part);
		GraphViewer.display(graph);
	}
}

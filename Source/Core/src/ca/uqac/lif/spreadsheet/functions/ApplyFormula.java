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
package ca.uqac.lif.spreadsheet.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.dag.NestedNode;
import ca.uqac.lif.dag.NodeConnector;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.Function;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Fills cells of an input spreadsheet by applying functions on other cells of
 * that same spreadsheet. This function resembles the low-level operation of
 * most spreadsheet software such as Excel, Gnumeric or LibreOffice Calc.
 * <p>
 * For example, consider the following input spreadsheet:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>1</td><td>3</td><td></td></tr>
 * <tr><td>2</td><td>1</td><td></td></tr>
 * <tr><td>3</td><td>4</td><td></td></tr>
 * </tbody>
 * </table>
 * <p>
 * If we associate to cell (2,1) a function that applies the sum of cells
 * (0,1) and (1,1) (using method {@link #add(Cell, Function) add()} , a call to
 * {@link #evaluate(Object...) evaluate()} will result in the following output:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>A</th><th>B</th><th>C</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>1</td><td>3</td><td><strong>4</strong></td></tr>
 * <tr><td>2</td><td>1</td><td></td></tr>
 * <tr><td>3</td><td>4</td><td></td></tr>
 * </tbody>
 * </table>
 * <p>
 * For convenience, it is possible to associate functions to multiple cells in
 * a single instance of {@link ApplyFormula} (through multiple calls to
 * <tt>add()</tt>).
 * <p>
 * <strong>Caveat emptor:</strong> the function does not check for eventual
 * circular dependencies between cells. If such dependencies are present in the
 * set of formulas to apply, a call to {@link #evaluate(Object...)} will result
 * in an infinite loop.
 * 
 * @author Sylvain Hallé
 */
public class ApplyFormula extends AtomicFunction
{
	
	/*@ non_null @*/ protected final List<CellFormula> m_formulas;
	
	/**
	 * A set that keeps track of the cells in the spreadsheet that are the result
	 * of the application of a function to other cells.
	 */
	/*@ non_null @*/ protected final Set<Cell> m_computedCells;
	
	public ApplyFormula(int in_arity, CellFormula ... formulas)
	{
		super(in_arity, 1);
		m_formulas = sort(formulas);
		m_computedCells = new HashSet<Cell>();
		for (CellFormula cf : formulas)
		{
			m_computedCells.add(cf.getTarget());
		}
	}
	
	public ApplyFormula(CellFormula ... formulas)
	{
		this(1, formulas);
	}
	
	public ApplyFormula(int in_arity, List<CellFormula> formulas)
	{
		super(in_arity, 1);
		m_formulas = sort(formulas);
		m_computedCells = new HashSet<Cell>();
		for (CellFormula cf : formulas)
		{
			m_computedCells.add(cf.getTarget());
		}
	}
	
	/**
	 * Adds a formula to the list of formulas to apply in the spreadsheet.
	 * @param formula The formula to add
	 * @return This function
	 */
	public ApplyFormula add(CellFormula formula)
	{
		m_formulas.add(formula);
		m_computedCells.add(formula.getTarget());
		return this;
	}
	
	/**
	 * Adds a formula to the list of formulas to apply in the spreadsheet.
	 * @param target The cell where the result of the formula is written
	 * @param f The function to apply
	 * @return This function
	 */
	public ApplyFormula add(Cell target, Function f)
	{
		m_formulas.add(new CellFormula(target, f));
		m_computedCells.add(target);
		return this;
	}
	
	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		Spreadsheet[] ins = new Spreadsheet[inputs.length];
		for (int i = 0; i < inputs.length; i++)
		{
			if (!(inputs[i] instanceof Spreadsheet))
			{
				throw new InvalidArgumentTypeException("Argument " + i + " is not a spreadsheet");
			}
			ins[i] = (Spreadsheet) inputs[i];
		}
		// Replace first spreadsheet by a copy of itself
		ins[0] = ins[0].duplicate();
		for (CellFormula formula : m_formulas)
		{
			formula.evaluate(ins);
		}
		return new Object[] {ins[0]};
	}
	
	@Override
	/*@ non_null @*/ public PartNode getExplanation(/*@ non_null @*/ Part part, /*@ non_null @*/ NodeFactory factory)
	{
		Cell c = Cell.mentionedCell(part);
		if (c == null)
		{
			return super.getExplanation(part, factory);
		}
		for (CellFormula formula : m_formulas)
		{
			if (formula.getTarget().equals(c))
			{
				PartNode root = factory.getPartNode(part, this);
				NodeFactory sub_factory = factory.getFactory(part, this);
				Part sub_part = ComposedPart.compose(part.tail().tail(), NthOutput.FIRST); // we remove nth-output + cell
				PartNode sub_root = ((ExplanationQueryable) formula.m_formula).getExplanation(sub_part, sub_factory);
				NestedNode nn = NestedNode.createFromTree(sub_root);
				NodeConnector.connect(root, 0, nn, 0);
				for (int i = 0; i < nn.getOutputArity(); i++)
				{
					PartNode pn = (PartNode) nn.getAssociatedOutput(i).getNode();
					Part inner_part = pn.getPart();
					Cell exp_c = Cell.mentionedCell(inner_part);
					int input_index = NthInput.mentionedInput(inner_part);
					if (m_computedCells.contains(exp_c))
					{
						// If the formula refers to other cells of the same spreadsheet,
						// we refer to the output spreadsheet and not the input one
						Part new_part = NthInput.replaceInBy(inner_part, NthOutput.FIRST);
						PartNode to_connect = factory.getPartNode(new_part, this);
						NodeConnector.connect(nn, i, to_connect, 0);
						// And restart the explanation on that node, since we are not done
						PartNode other = getExplanation(new_part, factory);
						NodeConnector.connect(to_connect, 0, other, 0);
					}
					else
					{
						// Re-plug input of inner function to input of function
						int spreadsheet_index = formula.m_arguments.get(input_index);
						PartNode to_connect = factory.getPartNode(NthInput.replaceInBy(inner_part, new NthInput(spreadsheet_index)), this);
						NodeConnector.connect(nn, i, to_connect, 0);
					}
				}
				return root;
			}
		}
		return super.getExplanation(part, factory);
	}
	
	@Override
	public String toString()
	{
		return "Apply formulas";
	}
	
	/*@ non_null @*/ protected static List<CellFormula> sort(/*@ non_null @*/ CellFormula[] formulas)
	{
		List<CellFormula> out = new ArrayList<CellFormula>(formulas.length);
		for (int i = 0; i < formulas.length; i++)
		{
			out.add(formulas[i]);
		}
		return out;
	}
	
	/*@ non_null @*/ protected static List<CellFormula> sort(/*@ non_null @*/ List<CellFormula> formulas)
	{
		return formulas;
	}
}

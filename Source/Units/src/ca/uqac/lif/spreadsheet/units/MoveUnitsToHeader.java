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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.FunctionException;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.strings.Range;
import ca.uqac.lif.petitpoucet.function.strings.RangeMapping;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.SpreadsheetFunction;
import ca.uqac.lif.units.DimensionValue;
import ca.uqac.lif.units.DimensionValuePart;
import ca.uqac.lif.units.NoSuchUnitException;

/**
 * Turns a spreadsheet containing
 * {@link ca.uqac.lif.units.DimensionValue DimensionValue}s into another where
 * units for each cell are moved to the column's header.
 * <p>
 * For example, consider the following spreadsheet:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>n</th><th>Time</th><th>Distance</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>0</td><td>0 s</td><td>(2.1 ± 0.1) cm</td></tr>
 * <tr><td>1</td><td>(1 ± 0.5) s</td><td>(3.25 ± 0.02) cm</td></tr>
 * <tr><td>2</td><td>(1.3 ± 0.5) s</td><td>(2 ¹/₄ ± ¹/₄) "</td></tr>
 * </tbody>
 * </table>
 * <p>
 * The result of applying the function is:
 * <p>
 * <table border="1">
 * <thead>
 * <tr><th>n</th><th>Time (s)</th><th>Distance (cm)</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>0</td><td>0</td><td>2.1 ± 0.1</td></tr>
 * <tr><td>1</td><td>1 ± 0.5</td><td>3.25 ± 0.02</td></tr>
 * <tr><td>2</td><td>1.3 ± 0.5</td><td>2 ¹/₄ ± ¹/₄</td></tr>
 * </tbody>
 * </table>
 * <p>
 * As one can see, the units of the second column are added to that column's
 * header ("s"), but are removed from each cell. The same applies for the
 * third column. A few notes concerning this function:
 * <ul>
 * <li>If a column contains values in different dimensions (e.g. mixing
 * lengths with speeds), a {@link FunctionException} will be thrown.</li>
 * <li>If a column contains values in the same dimension, but expressed
 * in different units (as in the example above, where column 3 has lengths in
 * centimeters and inches), all cells are converted to the unit of the first
 * cell.</li>
 * <li>Cells that do not contain descendants of {@link DimensionValue} are
 * ignored and left as they are.</li>
 * <li>All cells of the input spreadsheet that are {@link DimensionValue}s
 * become cells containing {@link ca.uqac.lif.numbers.Real Real}s in
 * the output. That is, all dimensional information is lost and the values
 * become "simple numbers". For this reason, it is expected that this function
 * be used primarily for <em>displaying</em> the contents of a spreadsheet,
 * and not as an intermediate step in a computation. If you need to simply
 * convert spreadsheet cells into a given unit, apply the function
 * {@link ca.uqac.lif.units.functions.ConvertTo ConvertTo} to the desired cells
 * instead.</li>
 * </ul>
 * @author Sylvain Hallé
 *
 */
public class MoveUnitsToHeader extends SpreadsheetFunction
{
	protected String[] m_lastHeaders;

	protected int[] m_unitRows;

	public MoveUnitsToHeader()
	{
		super(1);
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet table = (Spreadsheet) inputs[0];
		Spreadsheet out = new Spreadsheet(table.getWidth(), table.getHeight());
		m_mapping = new InputCell[table.getHeight()][table.getHeight()][];
		m_unitRows = new int[table.getWidth()];
		m_lastHeaders = new String[table.getWidth()];
		for (int col = 0; col < table.getWidth(); col++)
		{
			DimensionValue reference_unit = getColumnUnit(table, col);
			if (reference_unit == null)
			{
				// Column does not contain units; copy as is
				for (int row = 0; row < table.getHeight(); row++)
				{
					out.set(col, row, table.get(col, row));
					m_mapping[row][col] = new InputCell[] {InputCell.get(col, row)};
					if (row == 0)
					{
						m_lastHeaders[col] = table.getString(col, row);
					}
				}
			}
			else
			{
				m_lastHeaders[col] = table.get(col, 0).toString() + " (" + reference_unit.getUnitName() + ")";
				out.set(col, 0, m_lastHeaders[col]);
				for (int row = 1; row < table.getHeight(); row++)
				{
					Object o = table.get(col, row);
					if (o instanceof DimensionValue)
					{
						DimensionValue dv = (DimensionValue) o;
						try
						{
							DimensionValue target_dv = DimensionValue.instantiate(dv, reference_unit.getClass());
							out.set(col, row, target_dv.get());
						}
						catch (NoSuchUnitException e)
						{
							throw new FunctionException(e);
						}
					}
					else
					{
						out.set(col, row, o);
					}
					m_mapping[row][col] = new InputCell[] {InputCell.get(col, row)};
				}
			}
		}
		return new Object[] {out};
	}

	@Override
	public PartNode getExplanation(Part d, NodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		if (NthOutput.mentionedOutput(d) != 0)
		{
			// The part mentions no output or the incorrect output
			root.addChild(f.getUnknownNode());
			return root;
		}
		Part d_tail = d.tail();
		Cell c = Cell.mentionedCell(d_tail);
		if (c == null)
		{
			// No cell mentioned: return whole input table
			root.addChild(f.getPartNode(NthOutput.replaceOutByIn(d, 0), this));
			return root;
		}
		Part tail = d_tail.tail();
		if (tail == null)
		{
			// Same cell in the input
			root.addChild(f.getPartNode(NthOutput.replaceOutByIn(d, 0), this));
			return root;
		}
		root.addChild(explainPart(c, tail, d, f));
		return root;
	}

	/*@ pure non_null @*/ protected LabelledNode explainPart(Cell c, Part d, Part original, NodeFactory f)
	{
		Part head = d.head();
		int row = c.getRow();
		if (!(head instanceof Range && row == 0))
		{
			return f.getPartNode(NthOutput.replaceOutByIn(original, 0), this);
		}
		int col = c.getColumn();
		// Explain a character range in a cell of the first row
		if (m_unitRows[col] < 0)
		{
			// This header has not been changed: relay the part as is
			return f.getPartNode(NthOutput.replaceOutByIn(d, 0), this);
		}
		Range queried_r = (Range) head;
		int unit_start_index = m_lastHeaders[col].lastIndexOf("(") + 1;
		int unit_end_index = m_lastHeaders[col].lastIndexOf(")") - 1;
		Range unit_r = new Range(unit_start_index, unit_end_index);
		List<Range> fragments = RangeMapping.fragment(queried_r, unit_r);
		List<PartNode> children = new ArrayList<PartNode>(fragments.size());
		for (Range r : fragments)
		{
			if (!r.overlaps(queried_r))
			{
				continue;
			}
			if (r.getEnd() < unit_start_index)
			{
				// To the left of the unit name
				children.add(f.getPartNode(NthOutput.replaceOutByIn(original, 0), this));
			}
			else if (r.getStart() >= unit_start_index && r.getEnd() <= unit_end_index)
			{
				Part new_p = null;
				// Inside the unit name
				if (r.length() == unit_end_index - unit_start_index + 1)
				{
					// Whole unit name
					new_p = Range.replaceRangeBy(original, DimensionValuePart.unitName);
				}
				else
				{
					// Part of unit name
					Range new_r = r.shift(-unit_start_index);
					new_p = Range.replaceRangeBy(original, ComposedPart.compose(new_r, DimensionValuePart.unitName));
				}
				new_p = Cell.replaceCellBy(new_p, Cell.get(col, m_unitRows[col]));
				new_p = NthOutput.replaceOutByIn(new_p, 0);
				children.add(f.getPartNode(new_p, this));
			}
			else
			{
				// To the right of the unit name
				Range new_r = r.shift(-unit_end_index);
				Part new_p = Range.replaceRangeBy(d, new_r);
				new_p = NthOutput.replaceOutByIn(new_p, 0);
				children.add(f.getPartNode(new_p, this));
			}
		}
		if (children.size() == 1)
		{
			return children.get(0);
		}
		AndNode and = f.getAndNode();
		for (PartNode child : children)
		{
			and.addChild(child);
		}
		return and;
	}

	/**
	 * Gets the first non-null instance of {@link DimensionValue} of a column,
	 * stating from the second row. This method is used to guess the dimension
	 * and units of the values in a column.
	 * @param table The table to look into
	 * @param col_index The index of the column
	 * @return The first instance, or <tt>null</tt> if no instance exists in that
	 * column
	 */
	/*@ null @*/ protected DimensionValue getColumnUnit(Spreadsheet table, int col_index)
	{
		for (int row = 1; row < table.getHeight(); row++)
		{
			Object o = table.get(col_index, row);
			if (o instanceof DimensionValue)
			{
				m_unitRows[col_index] = row;
				return (DimensionValue) o;
			}
		}
		m_unitRows[col_index] = -1;
		return null;
	}
	
	@Override
	public String toString()
	{
		return "Move units to header";
	}
	
	@Override
	public MoveUnitsToHeader duplicate(boolean with_state)
	{
		MoveUnitsToHeader muth = new MoveUnitsToHeader();
		super.copyInto(muth, with_state);
		if (with_state)
		{
			muth.m_lastHeaders = Arrays.copyOf(m_lastHeaders, m_lastHeaders.length);
			muth.m_unitRows = Arrays.copyOf(m_unitRows, m_unitRows.length);
		}
		return muth;
	}
}

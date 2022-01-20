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

import ca.uqac.lif.petitpoucet.function.FunctionException;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;
import ca.uqac.lif.spreadsheet.functions.SpreadsheetFunction;
import ca.uqac.lif.units.DimensionValue;
import ca.uqac.lif.units.NoSuchUnitException;

/**
 * Turns a spreadsheet containing {@link DimensionValue}s into another where
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
 * become cells containing {@link Real}s in the output. That is, all
 * dimensional information is lost and the values become "simple numbers".
 * For this reason, it is expected that this function be used primarily for
 * <em>displaying</em> the contents of a spreadsheet, and not as an
 * intermediate step in a computation. If you need to simply convert
 * spreadsheet cells into a given unit, apply the function {@link ConvertTo}
 * to the desired cells instead.</li>
 * </ul>
 * @author Sylvain Hallé
 *
 */
public class MoveUnitsToHeader extends SpreadsheetFunction
{
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
				}
			}
			else
			{
				out.set(col, 0, table.get(col, 0).toString() + " (" + reference_unit.getUnitName() + ")");
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
	
	/*@ null @*/ protected static DimensionValue getColumnUnit(Spreadsheet table, int col_index)
	{
		for (int row = 1; row < table.getHeight(); row++)
		{
			Object o = table.get(col_index, row);
			if (o instanceof DimensionValue)
			{
				return (DimensionValue) o;
			}
		}
		return null;
	}
}

/*
    A provenance-aware spreadsheet library
    Copyright (C) 2021-2023 Sylvain Hallé

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

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Reads a spreadsheet from a character source, either a {@link Scanner} or a
 * {@link String}.
 * @author Sylvain Hallé
 */
public class ReadSpreadsheet extends AtomicFunction
{
	/**
	 * The mapping associating cells of the resulting spreadsheet to parts of
	 * the input string.
	 */
	/*@ non_null @*/ protected final Map<Cell,Part> m_mapping;

	/**
	 * Creates a new instance of the function.
	 */
	public ReadSpreadsheet()
	{
		super(1, 1);
		m_mapping = new HashMap<Cell,Part>();
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (inputs[0] instanceof Scanner)
		{
			m_mapping.clear();
			Spreadsheet s = Spreadsheet.read((Scanner) inputs[0], "#", "\\s+", m_mapping);
			return new Object[] {s};
		}
		else if (inputs[0] instanceof String)
		{
			m_mapping.clear();
			Spreadsheet s = Spreadsheet.read(new Scanner((String) inputs[0]), "#", "\\s+", m_mapping);
			return new Object[] {s};
		}
		throw new InvalidArgumentTypeException("Argument is not a scanner or a string");
	}

	@Override
	public PartNode getExplanation(Part d, RelationNodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		Cell c = Cell.mentionedCell(d);
		if (c == null)
		{
			// No specific cell is asked
			root.addChild(f.getPartNode(Part.all, "Input string"));
			return root;
		}
		// A specific cell is asked
		if (!m_mapping.containsKey(c))
		{
			// No mapping for this cell
			root.addChild(f.getPartNode(Part.nothing, null));
			return root;
		}
		Part p = m_mapping.get(c);
		root.addChild(f.getPartNode(p, "Input string"));
		return root;
	}

	@Override
	public String toString()
	{
		return "Read spreadsheet";
	}
	
	@Override
	public ReadSpreadsheet duplicate(boolean with_state)
	{
		ReadSpreadsheet rs = new ReadSpreadsheet();
		copyInto(rs, with_state);
		if (with_state)
		{
			rs.m_mapping.putAll(m_mapping);
		}
		return rs;
	}
}

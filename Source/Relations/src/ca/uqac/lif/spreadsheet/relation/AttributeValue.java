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
package ca.uqac.lif.spreadsheet.relation;

import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;

/**
 * A function taking as input a {@link NamedRow}, and returning the value of a
 * given attribute in this row.
 * @author Sylvain Hallé
 */
public class AttributeValue extends AtomicFunction
{
	/*@ non_null @*/ protected final Object m_attributeName;
	
	/**
	 * Obtains an instance of the function.
	 * @param attribute_name The name of the attribute to fetch inside a
	 * named row
	 * @return The instance
	 */
	/*@ non_null @*/ public static AttributeValue get(/*@ non_null @*/ Object attribute_name)
	{
		return new AttributeValue(attribute_name);
	}
	
	/**
	 * Creates a new instance of the function.
	 * @param attribute_name The name of the attribute to fetch inside a
	 * named row
	 */
	protected AttributeValue(/*@ non_null @*/ Object attribute_name)
	{
		super(1, 1);
		m_attributeName = attribute_name;
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof NamedRow))
		{
			throw new InvalidArgumentTypeException("Input argument must be a named row");
		}
		NamedRow r = (NamedRow) inputs[0];
		return new Object[] {r.valueOf(m_attributeName)};
	}
	
	@Override
	public String toString()
	{
		if (m_attributeName == null)
		{
			return "null";
		}
		return m_attributeName.toString();
	}
	
	@Override
	public AttributeValue duplicate(boolean with_state)
	{
		AttributeValue av = get(m_attributeName);
		super.copyInto(av, with_state);
		return av;
	}
}

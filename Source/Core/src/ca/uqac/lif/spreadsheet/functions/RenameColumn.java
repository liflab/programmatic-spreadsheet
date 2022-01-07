package ca.uqac.lif.spreadsheet.functions;

import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.spreadsheet.Spreadsheet;

public class RenameColumn extends SpreadsheetFunction
{
	/**
	 * The original column name.
	 */
	protected final String m_from;
	
	/**
	 * The new column name.
	 */
	protected final String m_to;
	
	public RenameColumn(String from, String to)
	{
		super(1);
		m_from = from;
		m_to = to;
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet s = (Spreadsheet) inputs[0];
		int index = s.getColumnIndex(m_from);
		if (index < 0)
		{
			return new Object[] {s};
		}
		Spreadsheet new_s = s.duplicate(true);
		new_s.set(index, 0, m_to);
		return new Object[] {new_s};
	}
}

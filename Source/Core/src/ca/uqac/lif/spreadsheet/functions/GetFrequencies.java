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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.petitpoucet.AndNode;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.RelationNodeFactory;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.spreadsheet.DataFormatter;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Calculates a two-dimensional frequency table out of a list of pairs of
 * numbers. The result of such a table can be displayed graphically in the
 * form of a {@link ca.uqac.lif.spreadsheet.chart.HeatMap HeatMap}.
 * <p>
 * For example, given a frequency table with the columns divided into 4
 * buckets spanning the range [0-12], and the rows divided into 3 buckets
 * spanning the range [0-6], the list of pairs (1,1), (3,5), (2,1), (7,3), and
 * would produce the following spreadsheet:
 * <p>
 * <table border="1">
 * <tr><th>&times;</th><th>0</th><th>3</th><th>6</th><th>9</th></tr>
 * <tr><th>0</th><td>2</td><td>0</td><td>0</td><td>0</td></tr>
 * <tr><th>2</th><td>0</td><td>0</td><td>1</td><td>0</td></tr>
 * <tr><th>4</th><td>0</td><td>1</td><td>0</td><td>0</td></tr>
 * </table>
 * <p>
 * The first row of the spreadsheet contains the lower bound of each bucket
 * on the x-axis; the first column contains the lower bound of each bucket
 * on the y-axis. Each cell contains the number of pairs in the input list
 * that fall within each bucket.
 * 
 * @author Sylvain Hallé
 */
public class GetFrequencies extends AtomicFunction
{
	/**
	 * The preconfigured minimum value of the generated frequency table
	 * along the x axis
	 */
	protected double m_minX;

	/**
	 * The preconfigured maximum value of the generated frequency table
	 * along the x axis
	 */
	protected double m_maxX;
	
	/**
	 * The number of buckets that will divide the interval
	 * x<sub>max</sub> - x<sub>min</sub>
	 */
	protected int m_numBucketsX;

	/**
	 * The preconfigured minimum value of the generated frequency table
	 * along the y axis
	 */
	protected double m_minY;

	/**
	 * The preconfigured maximum value of the generated frequency table
	 * along the y axis
	 */
	protected double m_maxY;

	/**
	 * The number of buckets that will divide the interval
	 * y<sub>max</sub> - y<sub>min</sub>
	 */
	protected int m_numBucketsY;
	
	/**
	 * An optional default increment
	 */
	protected Double m_defaultIncrement;
	
	/**
	 * The width of each bucket on the x axis
	 */
	protected double m_widthX;

	/**
	 * The width of each bucket on the y axis
	 */
	protected double m_widthY;
	
	/**
	 * An array associating each cell of the table with the number pairs that
	 * correspond to it.
	 */
	protected List<Integer>[][] m_pairs;
	
	/**
	 * Creates a new instance of the function.
	 * @param min_x  The preconfigured minimum value of the generated frequency table
	 * along the x axis
	 * @param max_x  The preconfigured maximum value of the generated frequency table
	 * along the x axis
	 * @param b_x The number of buckets that will divide the interval
	 * x<sub>max</sub> - x<sub>min</sub>
	 * @param min_y  The preconfigured minimum value of the generated frequency table
	 * along the y axis
	 * @param max_y  The preconfigured maximum value of the generated frequency table
	 * along the y axis
	 * @param b_y The number of buckets that will divide the interval
	 * y<sub>max</sub> - y<sub>min</sub>
	 * @param default_increment An optional default value to increment an entry
	 * when not specified; can be set to <tt>null</tt> (meaning no default increment)
	 */
	public GetFrequencies(double min_x, double max_x, int b_x, double min_y, double max_y, int b_y, Double default_increment)
	{
		super(1, 1);
		m_minX = min_x;
		m_minY = min_y;
		m_maxX = max_x;
		m_maxY = max_y;
		m_numBucketsX = b_x;
		m_numBucketsY = b_y;
		m_defaultIncrement = default_increment;
		m_widthX = (max_x - min_x) / (double) b_x;
		m_widthY = (max_y - min_y) / (double) b_y;
	}
	
	/**
	 * Creates a new frequency table, and sets a default increment for each entry
	 * to 1.
	 * @param min_x  The preconfigured minimum value of the generated frequency table
	 * along the x axis
	 * @param max_x  The preconfigured maximum value of the generated frequency table
	 * along the x axis
	 * @param b_x The number of buckets that will divide the interval
	 * x<sub>max</sub> - x<sub>min</sub>
	 * @param min_y  The preconfigured minimum value of the generated frequency table
	 * along the y axis
	 * @param max_y  The preconfigured maximum value of the generated frequency table
	 * along the y axis
	 * @param b_y The number of buckets that will divide the interval
	 * y<sub>max</sub> - y<sub>min</sub>
	 */
	public GetFrequencies(double min_x, double max_x, int b_x, double min_y, double max_y, int b_y)
	{
		this(min_x, max_x, b_x, min_y, max_y, b_y, 1d);
	}
	
	@Override
	public PartNode getExplanation(Part d, RelationNodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		int mentioned_output = NthOutput.mentionedOutput(d);
		if (mentioned_output != 0)
		{
			root.addChild(f.getUnknownNode());
			return root;
		}
		Part d_tail = d.tail();
		if (d_tail.head() instanceof Cell)
		{
			Cell c = (Cell) d_tail.head();
			int row = c.getRow(), col = c.getColumn();
			if (row < 0 || row > m_numBucketsY || col < 0 || col > m_numBucketsX)
			{
				// Unknown cell
				root.addChild(f.getUnknownNode());
			}
			else
			{
				List<Integer> indices = m_pairs[row - 1][col - 1];
				if (indices == null)
				{
					root.addChild(f.getPartNode(ComposedPart.compose(Part.nothing, NthInput.FIRST), this));	
				}
				else
				{
					LabelledNode to_add = root;
					if (indices.size() > 1)
					{
						AndNode and = f.getAndNode();
						to_add.addChild(and);
						to_add = and;
					}
					for (int index : indices)
					{
						to_add.addChild(f.getPartNode(ComposedPart.compose(new NthElement(index), NthInput.FIRST), this));
					}
				}
			}
		}
		else
		{
			root.addChild(f.getUnknownNode());
		}
		return root;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof List))
		{
			throw new InvalidArgumentTypeException("Argument is not a list");	
		}
		List<?> entries = (List<?>) inputs[0];
		Spreadsheet table = new Spreadsheet(m_numBucketsX + 1, m_numBucketsY + 1, 0);
		table.set(0, 0, null);
		m_pairs = new List[m_numBucketsY][m_numBucketsX];
		for (int i = 0; i < m_numBucketsX; i++)
		{
			table.set(i + 1, 0, m_widthX * i);
		}
		for (int i = 0; i < m_numBucketsY; i++)
		{
			table.set(0, i + 1, m_widthY * i);
		}
		for (int index = 0; index < entries.size(); index++)
		{
			Object o = entries.get(index);
			double[] pair = getPair(o);
			if (pair == null)
			{
				throw new InvalidArgumentTypeException("A pair of numbers could not be made out of one of the elements of the list.");
			}
			add(pair[0], pair[1], table, m_defaultIncrement, index);
		}
		return new Object[] {table};
	}
	
	/**
	 * Adds a value to a frequency table.
	 * @param x The x position
	 * @param y The y position
	 * @param table The table
	 * @param v The value to add in the corresponding cell
	 * @param index The index of the pair in the function's input list
	 */
	protected void add(double x, double y, Spreadsheet table, double v, int index)
	{
		int bin_x = (int) Math.floor(((x - m_minX) / (m_maxX - m_minX)) * (double) m_numBucketsX);
		int bin_y = (int) Math.floor(((y - m_minY) / (m_maxY - m_minY)) * (double) m_numBucketsY);
		if (bin_x < 0 || bin_x >= m_numBucketsX || bin_y < 0 || bin_y >= m_numBucketsY)
		{
			// Out of bounds: ignore
			return;
		}
		Double d = ((Number) table.get(bin_x + 1, bin_y + 1)).doubleValue();
		table.set(bin_x + 1, bin_y + 1, d + v);
		 
		if (m_pairs[bin_y][bin_x] == null)
		{
			List<Integer> indices = new ArrayList<Integer>();
			indices.add(index);
			m_pairs[bin_y][bin_x] = indices;
		}
		else
		{
			List<Integer> indices = m_pairs[bin_y][bin_x];
			indices.add(index);
		}
	}
	
	@Override
	public String toString()
	{
		return "Get frequencies";
	}
	
	@Override
	public GetFrequencies duplicate(boolean with_state)
	{
		GetFrequencies gf = new GetFrequencies(m_minX, m_maxX, m_numBucketsX, m_minY, m_maxY, m_numBucketsY, m_defaultIncrement);
		super.copyInto(gf, with_state);
		if (with_state)
		{
			gf.m_pairs = Arrays.copyOf(m_pairs, m_pairs.length);
		}
		return gf;
	}
	
	/**
	 * Attempts to extract a pair of numbers from an object.
	 * @param o The object
	 * @return An array of exactly two double values, or <tt>null</tt> if no
	 * such pair could be guessed from the input object
	 */
	/*@ null @*/ protected static double[] getPair(/*@ null @*/ Object o)
	{
		if (o == null)
		{
			return null;
		}
		Float f1 = null, f2 = null;
		if (o instanceof List)
		{
			List<?> l = (List<?>) o;
			if (l.size() != 2)
			{
				return null;
			}
			f1 = DataFormatter.readFloat(l.get(0));
			f2 = DataFormatter.readFloat(l.get(1));
		}
		else if (o.getClass().isArray())
		{
			Object[] a = (Object[]) o;
			f1 = DataFormatter.readFloat(a[0]);
			f2 = DataFormatter.readFloat(a[1]);
		}
		if (f1 == null || f2 == null)
		{
			return null;
		}
		return new double[] {f1, f2};
	}
	
	/**
	 * Creates a pair of values out of two numbers.
	 * @param x The first number
	 * @param y The second number
	 * @return The pair
	 */
	public static Double[] createPair(double x, double y)
	{
		return new Double[] {x, y};
	}
}

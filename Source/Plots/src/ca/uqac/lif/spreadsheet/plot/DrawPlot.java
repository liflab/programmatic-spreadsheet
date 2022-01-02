/*
    A provenance-aware spreadsheet library
    Copyright (C) 2021 Sylvain Hallé

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
package ca.uqac.lif.spreadsheet.plot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.dag.LeafCrawler;
import ca.uqac.lif.dag.Node;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.Part.Self;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.AtomicFunction;
import ca.uqac.lif.petitpoucet.function.FunctionException;
import ca.uqac.lif.petitpoucet.function.InvalidArgumentTypeException;
import ca.uqac.lif.petitpoucet.function.InvalidNumberOfArgumentsException;
import ca.uqac.lif.petitpoucet.function.NthInput;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.spreadsheet.Spreadsheet;

/**
 * Turns a spreadsheet into an array of bytes containing the graphical
 * representation of the spreadsheet into a plot.
 * @author Sylvain Hallé
 */
public class DrawPlot extends AtomicFunction
{
	/**
	 * The plot object used to produce the image.
	 */
	/*@ non_null @*/ protected final Plot m_plot;

	/**
	 * The image format to produce.
	 */
	/*@ non_null @*/ protected PlotFormat m_format;

	/**
	 * A flag indicating if the plot is to be displayed with its title.
	 */
	/*@ non_null @*/ protected boolean m_withTitle;

	/**
	 * Creates a new instance of the function.
	 * @param p The plot object used to produce the image
	 * @param format The image format to produce
	 * @param with_title Set to <tt>true</tt> if the plot is to be displayed
	 * with its title, <tt>false</tt> otherwise
	 */
	public DrawPlot(/*@ non_null @*/ Plot p, PlotFormat format, boolean with_title)
	{
		super(1, 1);
		m_plot = p;
		m_format = format;
		m_withTitle = with_title;
	}

	/**
	 * Creates a new instance of the function.
	 * @param p The plot object used to produce the image
	 */
	public DrawPlot(/*@ non_null @*/ Plot p)
	{
		this(p, null, true);
	}

	@Override
	protected Object[] getValue(Object... inputs) throws InvalidNumberOfArgumentsException
	{
		if (!(inputs[0] instanceof Spreadsheet))
		{
			throw new InvalidArgumentTypeException("Argument is not a spreadsheet");
		}
		Spreadsheet s = (Spreadsheet) inputs[0];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			m_plot.render(baos, s, m_format, m_withTitle);
		}
		catch (IOException e)
		{
			throw new FunctionException(e);
		}
		return new Object[] {baos.toByteArray()};
	}

	@Override
	public PartNode getExplanation(Part d, NodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		Part new_p = NthOutput.replaceOutBy(d, Part.self);
		PartNode sub_root = m_plot.getExplanation(new_p, f);
		root.addChild(sub_root);
		PlotLeafCrawler crawler = new PlotLeafCrawler(sub_root, f);
		crawler.crawl();
		return root;
	}

	@Override
	public String toString()
	{
		return "Draw " + m_plot;
	}

	/**
	 * A crawler that appends a new part node to each leaf of an explanation
	 * graph. This new node is a copy of the leaf, where the {@link Self}
	 * part of a {@link Plot} is replaced by a reference to the first input of
	 * the function.
	 */
	protected class PlotLeafCrawler extends LeafCrawler
	{
		/**
		 * A set of nodes that have already been visited. Since the nodes we
		 * add to the graph become leaves themselves, we keep track of them so
		 * that these added leaves are not appended with yet another leaf.
		 */
		private final Set<PartNode> m_visited = new HashSet<PartNode>();
		
		/**
		 * The factory used to obtain node instances.
		 */
		private final NodeFactory m_factory;

		/**
		 * Creates a new plot leaf crawler.
		 * @param n The start node of the exploration
		 * @param f The factory used to obtain node instances
		 */
		public PlotLeafCrawler(Node n, NodeFactory f)
		{
			super(n);
			m_factory = f;
		}

		@Override
		protected void visitLeaf(Node n)
		{
			if (n instanceof PartNode)
			{
				PartNode pn = (PartNode) n;
				if (!m_visited.contains(pn))
				{
					m_visited.add(pn);
					Part p = pn.getPart();
					Part new_p = Self.replaceSelfBy(p, NthInput.FIRST);
					PartNode new_leaf = m_factory.getPartNode(new_p, DrawPlot.this);
					pn.addChild(new_leaf);
					m_visited.add(new_leaf);
				}
			}
		}
	}
}

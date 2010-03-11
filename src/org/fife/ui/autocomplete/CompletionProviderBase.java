/*
 * 02/06/2010
 *
 * CompletionProviderBase.java - Base completion provider implementation.
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 */
package org.fife.ui.autocomplete;

import java.util.Collections;
import java.util.List;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;


/**
 * A base class for all standard completion providers.  This class implements
 * functionality that should be sharable across all <tt>CompletionProvider</tt>
 * implementations.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see AbstractCompletionProvider
 */
public abstract class CompletionProviderBase implements CompletionProvider {

	/**
	 * The parent completion provider.
	 */
	private CompletionProvider parent;

	/**
	 * The renderer to use for completions from this provider.  If this is
	 * <code>null</code>, a default renderer is used.
	 */
	private ListCellRenderer listCellRenderer;

	/**
	 * Text that marks the beginning of a parameter list, for example, '('.
	 */
	private char paramListStart;

	/**
	 * Text that marks the end of a parameter list, for example, ')'.
	 */
	private char paramListEnd;

	/**
	 * Text that separates items in a parameter list, for example, ", ".
	 */
	private String paramListSeparator;

	protected static final String EMPTY_STRING = "";


	/**
	 * {@inheritDoc}
	 */
	public void clearParameterizedCompletionParams() {
		paramListEnd = paramListStart = 0;
		paramListSeparator = null;
	}


	/**
	 * {@inheritDoc}
	 */
	public List getCompletions(JTextComponent comp) {
		List completions = getCompletionsImpl(comp);
		if (parent!=null) {
			completions.addAll(parent.getCompletions(comp));
			Collections.sort(completions);
		}
		return completions;
	}


	/**
	 * Does the dirty work of creating a list of completions.
	 *
	 * @param comp The text component to look in.
	 * @return The list of possible completions, or an empty list if there
	 *         are none.
	 */
	protected abstract List getCompletionsImpl(JTextComponent comp);


	/**
	 * {@inheritDoc}
	 */
	public ListCellRenderer getListCellRenderer() {
		return listCellRenderer;
	}


	/**
	 * {@inheritDoc}
	 */
	public char getParameterListEnd() {
		return paramListEnd;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getParameterListSeparator() {
		return paramListSeparator;
	}


	/**
	 * {@inheritDoc}
	 */
	public char getParameterListStart() {
		return paramListStart;
	}


	/**
	 * {@inheritDoc}
	 */
	public CompletionProvider getParent() {
		return parent;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setListCellRenderer(ListCellRenderer r) {
		listCellRenderer = r;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setParameterizedCompletionParams(char listStart,
										String separator, char listEnd) {
		if (listStart<0x20 || listStart==0x7F) {
			throw new IllegalArgumentException("Invalid listStart");
		}
		if (listEnd<0x20 || listEnd==0x7F) {
			throw new IllegalArgumentException("Invalid listEnd");
		}
		if (separator==null || separator.length()==0) {
			throw new IllegalArgumentException("Invalid separator");
		}
		paramListStart = listStart;
		paramListSeparator = separator;
		paramListEnd = listEnd;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setParent(CompletionProvider parent) {
		this.parent = parent;
	}


}
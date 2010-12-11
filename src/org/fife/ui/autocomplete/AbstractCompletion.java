/*
 * 12/21/2008
 *
 * AbstractCompletion.java - Base class for possible completions.
 * Copyright (C) 2008 Robert Futrell
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

import javax.swing.text.JTextComponent;


/**
 * Base class for possible completions.  Most, if not all, {@link Completion}
 * implementations can extend this class.  It remembers the
 * <tt>CompletionProvider</tt> that returns this completion, and also implements
 * <tt>Comparable</tt>, allowing such completions to be compared
 * lexicographically (ignoring case).<p>
 *
 * This implementation assumes the input text and replacement text are the
 * same value.  It also returns the input text from its {@link #toString()}
 * method (which is what <tt>DefaultListCellRenderer</tt> uses to render
 * objects).  Subclasses that wish to override any of this behavior can simply
 * override the corresponding method(s) needed to do so.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class AbstractCompletion implements Completion {

	/**
	 * The provider that created this completion;
	 */
	private CompletionProvider provider;

	/**
	 * The relevance of this completion.  Completion instances with higher
	 * "relevance" values are inserted higher into the list of possible
	 * completions than those with lower values.  Completion instances with
	 * equal relevance values are sorted alphabetically.
	 */
	private int relevance;


	/**
	 * Constructor.
	 *
	 * @param provider The provider that created this completion.
	 */
	public AbstractCompletion(CompletionProvider provider) {
		this.provider = provider;
	}


	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o) {
		if (o==this) {
			return 0;
		}
		else if (o instanceof Completion) {
			Completion c2 = (Completion)o;
			return toString().compareToIgnoreCase(c2.toString());
		}
		return -1;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getAlreadyEntered(JTextComponent comp) {
		return provider.getAlreadyEnteredText(comp);
	}


	/**
	 * Returns the text the user has to (start) typing for this completion
	 * to be offered.  The default implementation simply returns
	 * {@link #getReplacementText()}.
	 *
	 * @return The text the user has to (start) typing for this completion.
	 * @see #getReplacementText()
	 */
	public String getInputText() {
		return getReplacementText();
	}


	/**
	 * {@inheritDoc}
	 */
	public CompletionProvider getProvider() {
		return provider;
	}


	/**
	 * {@inheritDoc}
	 */
	public int getRelevance() {
		return relevance;
	}


	/**
	 * The default implementation returns <code>null</code>.  Subclasses
	 * can override this method.
	 *
	 * @return The tool tip text.
	 */
	public String getToolTipText() {
		return null;
	}


	/**
	 * Sets the relevance of this completion.
	 *
	 * @param relevance The new relevance of this completion.
	 * @see #getRelevance()
	 */
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}


	/**
	 * Returns a string representation of this completion.  The default
	 * implementation returns {@link #getInputText()}.
	 *
	 * @return A string representation of this completion.
	 */
	public String toString() {
		return getInputText();
	}


}
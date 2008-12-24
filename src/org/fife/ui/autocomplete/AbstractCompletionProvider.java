/*
 * 12/21/2008
 *
 * AbstractCompletionProvider.java - Base class for completion providers.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;


/**
 * A base class for completion providers.  {@link Completion}s are kept in
 * a sorted list.  To get the list of completions that match a given input,
 * a binary search is done to find the first matching completion, then all
 * succeeding completions that also match are also returned.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class AbstractCompletionProvider implements CompletionProvider {

	/**
	 * The parent completion provider.
	 */
	private CompletionProvider parent;

	/**
	 * The completions this provider is aware of.  Subclasses should ensure
	 * that this list is sorted alphabetically (case-insensitively).
	 */
	protected List completions;

	/**
	 * The renderer to use for completions from this provider.  If this is
	 * <code>null</code>, a default renderer is used.
	 */
	private ListCellRenderer listCellRenderer;

	/**
	 * The case-insensitive {@link Completion} comparator.
	 */
	private Comparator comparator;

	protected static final String EMPTY_STRING = "";


	/**
	 * Constructor.
	 */
	public AbstractCompletionProvider() {
		comparator = new CaseInsensitiveComparator();
	}


	/**
	 * Adds a single completion to this provider.  If you are adding multiple
	 * completions to this provider, for efficiency reasons please consider
	 * using {@link #addCompletions(List)} instead.
	 *
	 * @param c The completion to add.
	 * @throws IllegalArgumentException If the completion's provider isn't
	 *         this <tt>CompletionProvider</tt>.
	 * @see #addCompletions(List)
	 * @see #removeCompletion(Completion)
	 * @see #clear()
	 */
	public void addCompletion(Completion c) {
		completions.add(c);
		Collections.sort(completions);
	}


	/**
	 * Adds {@link Completion}s to this provider.
	 *
	 * @param completions The completions to add.  This cannot be
	 *        <code>null</code>.
	 * @throws IllegalArgumentException If the completion's provider isn't
	 *         this <tt>CompletionProvider</tt>.
	 * @see #addCompletion(Completion)
	 * @see #removeCompletion(Completion)
	 * @see #clear()
	 */
	public void addCompletions(List completions) {
		this.completions.addAll(completions);
		Collections.sort(this.completions);
	}


	/**
	 * Removes all completions from this provider.  This does not affect
	 * the parent <tt>CompletionProvider</tt>, if there is one.
	 *
	 * @see #addCompletion(Completion)
	 * @see #addCompletions(List)
	 * @see #removeCompletion(Completion)
	 */
	public void clear() {
		completions.clear();
	}


	/**
	 * Returns the first <tt>Completion</tt> in this provider with the
	 * specified input text.
	 *
	 * @param inputText The input text to search for.
	 * @return The {@link Completion}, or <code>null</code> if there is no such
	 *         <tt>Completion</tt>.
	 */
	public Completion getCompletionByInputText(String inputText) {
		// TODO: Do a binary search for performance
		for (int i=0; i<completions.size(); i++) {
			Completion c = (Completion)completions.get(i);
			if (c.getInputText().equals(inputText)) {
				return c;
			}
		}
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public final List getCompletions(JTextComponent comp) {
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
	protected List getCompletionsImpl(JTextComponent comp) {

		List retVal = new ArrayList();
		String text = getAlreadyEnteredText(comp);

		int index = Collections.binarySearch(completions, text, comparator);
		System.out.println(index + "(" + completions.size() + ")");
		if (index<0) {
			index = -index - 1;
		}

		while (index<completions.size()) {
			Completion c = (Completion)completions.get(index);
			if (startsWithIgnoreCase(c.getInputText(), text)) {
				retVal.add(c);
				index++;
			}
			else {
				break;
			}
		}

		return retVal;

	}


	/**
	 * {@inheritDoc}
	 */
	public ListCellRenderer getListCellRenderer() {
		return listCellRenderer;
	}


	/**
	 * {@inheritDoc}
	 */
	public CompletionProvider getParent() {
		return parent;
	}


	/**
	 * Removes the specified completion from this provider.  This method
	 * will not remove completions from the parent provider, if there is one.
	 *
	 * @param c The completion to remove.
	 * @return <code>true</code> if this provider contained the specified
	 *         completion.
	 * @see #clear()
	 * @see #addCompletion(Completion)
	 * @see #addCompletions(List)
	 */
	public boolean removeCompletion(Completion c) {
		// Don't just call completions.remove(c) as it'll be a linear search.
		int index = Collections.binarySearch(completions, c);
		if (index<0) {
			return false;
		}
		completions.remove(index);
		return true;
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
	public void setParent(CompletionProvider parent) {
		this.parent = parent;
	}


	/**
	 * Returns whether <code>str</code> starts with <code>start</code>,
	 * ignoring case.
	 *
	 * @param str The string to check.
	 * @param start The prefix to check for.
	 * @return Whether <code>str</code> starts with <code>start</code>,
	 *         ignoring case.
	 */
	protected boolean startsWithIgnoreCase(String str, String start) {
		int startLen = start.length();
		if (str.length()>=startLen) {
			for (int i=0; i<startLen; i++) {
				char c1 = str.charAt(i);
				char c2 = start.charAt(i);
				if (Character.toLowerCase(c1)!=Character.toLowerCase(c2)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}


	/**
	 * A comparator that compares the input text of two {@link Completion}s
	 * lexicographically, ignoring case.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private static class CaseInsensitiveComparator implements Comparator,
														Serializable {

		public int compare(Object o1, Object o2) {
			Completion c = (Completion)o1;
			return String.CASE_INSENSITIVE_ORDER.compare(c.getInputText(), o2);
		}

	}


}
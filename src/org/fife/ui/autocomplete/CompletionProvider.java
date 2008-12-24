/*
 * 12/21/2008
 *
 * CompletionProvider.java - Provides autocompletion values based on the
 * text currently in a text component.
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

import java.util.List;

import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;


/**
 * Provides autocompletion values to an {@link AutoCompletion}.<p>
 *
 * Completion providers can have an optional parent.  Parents are searched for
 * completions when their children are.  This allows for chaining of completion
 * providers.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface CompletionProvider {


	/**
	 * Returns the text just before the current caret position that could be
	 * the start of something auto-completable.
	 *
	 * @param comp The text component.
	 * @return The text.
	 */
	public String getAlreadyEnteredText(JTextComponent comp);


	/**
	 * Gets the possible completions for the text component at the current
	 * caret position.
	 *
	 * @param comp The text component.
	 * @return The list of {@link Completion}s.  If no completions are
	 *         available, an empty list is returned.
	 */
	public List getCompletions(JTextComponent comp);


	/**
	 * Returns the cell renderer for completions returned from this provider.
	 *
	 * @return The cell renderer, or <code>null</code> if the default should
	 *         be used.
	 * @see #setListCellRenderer(ListCellRenderer)
	 */
	public ListCellRenderer getListCellRenderer();


	/**
	 * Returns the parent completion provider.
	 *
	 * @return The parent completion provider.
	 * @see #setParent(CompletionProvider)
	 */
	public CompletionProvider getParent();


	/**
	 * Sets the renderer to use when displaying completion choices.
	 *
	 * @param r The renderer to use.
	 * @see #getListCellRenderer()
	 */
	public void setListCellRenderer(ListCellRenderer r);


	/**
	 * Sets the parent completion provider.
	 *
	 * @param parent The parent provider.  <code>null</code> means there will
	 *        be no parent provider.
	 * @see #getParent()
	 */
	public void setParent(CompletionProvider parent);


}
/*
 * 12/21/2008
 *
 * Completion.java - Represents a single completion choice.
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
 * Represents a completion choice.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see AbstractCompletion
 */
public interface Completion {


	/**
	 * Returns the portion of this completion that has already been entered
	 * into the text component.  The match is case-insensitive.<p>
	 *
	 * This is a convenience method for:
	 * <code>getProvider().getAlreadyEnteredText(comp)</code>.
	 *
	 * @param comp The text component.
	 * @return The already-entered portion of this completion.
	 */
	public String getAlreadyEntered(JTextComponent comp);


	/**
	 * Returns the text that the user has to (start) typing for this completion
	 * to be offered.  Note that this will usually be the same value as
	 * {@link #getReplacementText()}, but not always (a completion could be
	 * a way to implement shorthand, for example, "<code>sysout</code>" mapping
	 * to "<code>System.out.println(</code>").
	 *
	 * @return The text the user has to (start) typing for this completion to
	 *         be offered.
	 * @see #getReplacementText()
	 */
	public String getInputText();


	/**
	 * Returns the provider that returned this completion.
	 *
	 * @return The provider.
	 */
	public CompletionProvider getProvider();


	/**
	 * Returns the text to insert as the result of this auto-completion.  This
	 * is the "complete" text, including any text that replaces what the user
	 * has already typed.
	 *
	 * @return The replacement text.
	 * @see #getInputText()
	 */
	public String getReplacementText();


	/**
	 * Returns the description of this auto-complete choice.  This can be
	 * used in a popup "description window."
	 *
	 * @return This item's description.  This should be HTML.  It may be
	 *         <code>null</code> if there is no description for this
	 *         completion.
	 */
	public String getSummary();


}
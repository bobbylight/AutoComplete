/*
 * 01/03/2009
 *
 * LanguageAwareCompletionProvider.java - A completion provider that is aware
 * of the language it is working with.
 * Copyright (C) 2009 Robert Futrell
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.JTextComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;


/**
 * A completion provider that is aware of the programming language it is
 * providing auto-completion for.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class LanguageAwareCompletionProvider extends AbstractCompletionProvider{

	/**
	 * The provider to use when no provider is assigned to a particular token
	 * type.
	 */
	private CompletionProvider defaultProvider;

	/**
	 * The provider to use when completing a string.
	 */
	private CompletionProvider stringCompletionProvider;


	/**
	 * Constructor.
	 *
	 * @param defaultProvider The provider to use when no provider is assigned
	 *        to a particular token type.  This cannot be <code>null</code>.
	 */
	public LanguageAwareCompletionProvider(CompletionProvider defaultProvider) {
		setDefaultCompletionProvider(defaultProvider);
		completions = new java.util.ArrayList(0); // TODO: Remove me.
	}


	public String getAlreadyEnteredText(JTextComponent comp) {
		if (!(comp instanceof RSyntaxTextArea)) {
			return EMPTY_STRING;
		}
		CompletionProvider provider = getProviderFor(comp);
		return provider.getAlreadyEnteredText(comp);
	}


	/**
	 * Does the dirty work of creating a list of completions.
	 *
	 * @param comp The text component to look in.
	 * @return The list of possible completions, or an empty list if there
	 *         are none.
	 */
	protected List getCompletionsImpl(JTextComponent comp) {
		if (!(comp instanceof RSyntaxTextArea)) {
			return new ArrayList(0);
		}
		CompletionProvider provider = getProviderFor(comp);
		return provider.getCompletions(comp);
	}


	/**
	 * Returns the completion provider used when one isn't defined for a
	 * particular token type.
	 *
	 * @return The completion provider to use.
	 * @see #setDefaultCompletionProvider(CompletionProvider)
	 */
	public CompletionProvider getDefaultCompletionProvider() {
		return defaultProvider;
	}


	private CompletionProvider getProviderFor(JTextComponent comp) {

		RSyntaxTextArea rsta = (RSyntaxTextArea)comp;
		RSyntaxDocument doc = (RSyntaxDocument)rsta.getDocument();
		Token t = doc.getTokenListForLine(rsta.getCaretLineNumber());
		if (t==null) {
			return getDefaultCompletionProvider();
		}

		int dot = rsta.getCaretPosition();
		Token curToken = RSyntaxUtilities.getTokenAtOffset(t, dot);
		int type = 0;
		if (curToken==null) { // At end of the line
			Token temp = t.getLastPaintableToken();
			if (temp==null) {
				return getDefaultCompletionProvider();
			}
			type = temp.type;
		}
		else {
			type = curToken.type;
		}

		switch (type) {
			case Token.LITERAL_STRING_DOUBLE_QUOTE:
			case Token.ERROR_STRING_DOUBLE:
				return getStringCompletionProvider();
			default:
				return getDefaultCompletionProvider();
		}

	}


	/**
	 * Returns the completion provider to use for strings.  This may be the
	 * default provider if one isn't explicitly set for strings.
	 *
	 * @return The completion provider to use.
	 * @see #setStringCompletionProvider(CompletionProvider)
	 */
	public CompletionProvider getStringCompletionProvider() {
		return stringCompletionProvider==null ? defaultProvider :
									stringCompletionProvider;
	}


	/**
	 * Sets the default completion provider.
	 *
	 * @param provider The provider to use when no provider is assigned to a
	 *        particular token type.  This cannot be <code>null</code>.
	 * @see #getDefaultCompletionProvider()
	 */
	public void setDefaultCompletionProvider(CompletionProvider provider) {
		if (provider==null) {
			throw new IllegalArgumentException("provider cannot be null");
		}
		this.defaultProvider = provider;
	}


	/**
	 * Sets the completion provider to use while in a string.
	 *
	 * @param provider The provider to use.  If this is <code>null</code>, the
	 *        default completion provider will be used.
	 * @see #getStringCompletionProvider()
	 */
	public void setStringCompletionProvider(CompletionProvider provider) {
		stringCompletionProvider = provider;
	}


}
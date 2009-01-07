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
 * A completion provider for the C programming language (and other languages
 * with similar syntax).  This provider simply delegates to another provider,
 * depending on whether the caret is in:
 * 
 * <ul>
 *    <li>A string</li>
 *    <li>A comment</li>
 *    <li>A documentation comment</li>
 *    <li>Plain text</li>
 * </ul>
 *
 * This allows for different completion choices in comments than  in code,
 * for example.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class CCompletionProvider extends AbstractCompletionProvider{

	/**
	 * The provider to use when no provider is assigned to a particular token
	 * type.
	 */
	private CompletionProvider defaultProvider;

	/**
	 * The provider to use when completing in a string.
	 */
	private CompletionProvider stringCompletionProvider;

	/**
	 * The provider to use when completing in a comment.
	 */
	private CompletionProvider commentCompletionProvider;

	/**
	 * The provider to use while in documentation comments.
	 */
	private CompletionProvider docCommentCompletionProvider;


	/**
	 * Constructor.
	 *
	 * @param defaultProvider The provider to use when no provider is assigned
	 *        to a particular token type.  This cannot be <code>null</code>.
	 */
	public CCompletionProvider(CompletionProvider defaultProvider) {
		setDefaultCompletionProvider(defaultProvider);
		completions = new ArrayList(0); // TODO: Remove me.
	}


	public String getAlreadyEnteredText(JTextComponent comp) {
		if (!(comp instanceof RSyntaxTextArea)) {
			return EMPTY_STRING;
		}
		CompletionProvider provider = getProviderFor(comp);
		return provider.getAlreadyEnteredText(comp);
	}


	/**
	 * Returns the completion provider to use for comments.
	 *
	 * @return The completion provider to use.
	 * @see #setCommentCompletionProvider(CompletionProvider)
	 */
	public CompletionProvider getCommentCompletionProvider() {
		return commentCompletionProvider;
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
		return provider!=null ? provider.getCompletions(comp) :
					new ArrayList(0);
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


	/**
	 * Returns the completion provider to use for documentation comments.
	 *
	 * @return The completion provider to use.
	 * @see #setDocCommentCompletionProvider(CompletionProvider)
	 */
	public CompletionProvider getDocCommentCompletionProvider() {
		return docCommentCompletionProvider;
	}


	private CompletionProvider getProviderFor(JTextComponent comp) {

		RSyntaxTextArea rsta = (RSyntaxTextArea)comp;
		RSyntaxDocument doc = (RSyntaxDocument)rsta.getDocument();
		int line = rsta.getCaretLineNumber();
		Token t = doc.getTokenListForLine(line);
		if (t==null) {
			return getDefaultCompletionProvider();
		}

		int dot = rsta.getCaretPosition();
		Token curToken = RSyntaxUtilities.getTokenAtOffset(t, dot);

		if (curToken==null) { // At end of the line

			int type = doc.getLastTokenTypeOnLine(line);
			if (type==Token.NULL) {
				Token temp = t.getLastPaintableToken();
				if (temp==null) {
					return getDefaultCompletionProvider();
				}
				type = temp.type;
			}

			switch (type) {
				case Token.ERROR_STRING_DOUBLE:
					return getStringCompletionProvider();
				case Token.COMMENT_EOL:
				case Token.COMMENT_MULTILINE:
					return getCommentCompletionProvider();
				case Token.COMMENT_DOCUMENTATION:
					return getDocCommentCompletionProvider();
				default:
					return getDefaultCompletionProvider();
			}

		}

		// FIXME: This isn't always a safe assumption.
		if (dot==curToken.offset) { // At the very beginning of a new token
			// Need to check previous token for its type before deciding.
			// Previous token may also be on previous line!
			return getDefaultCompletionProvider();
		}

		switch (curToken.type) {
			case Token.LITERAL_STRING_DOUBLE_QUOTE:
			case Token.ERROR_STRING_DOUBLE:
				return getStringCompletionProvider();
			case Token.COMMENT_EOL:
			case Token.COMMENT_MULTILINE:
				return getCommentCompletionProvider();
			case Token.COMMENT_DOCUMENTATION:
				return getDocCommentCompletionProvider();
			case Token.WHITESPACE:
			case Token.IDENTIFIER:
			case Token.VARIABLE:
			case Token.PREPROCESSOR:
			case Token.DATA_TYPE:
			case Token.FUNCTION:
				return getDefaultCompletionProvider();
		}

		return null; // In a token type we can't auto-complete from.

	}


	/**
	 * Returns the completion provider to use for strings.
	 *
	 * @return The completion provider to use.
	 * @see #setStringCompletionProvider(CompletionProvider)
	 */
	public CompletionProvider getStringCompletionProvider() {
		return stringCompletionProvider;
	}


	/**
	 * Sets the comment completion provider.
	 *
	 * @param provider The provider to use in comments.
	 * @see #getCommentCompletionProvider()
	 */
	public void setCommentCompletionProvider(CompletionProvider provider) {
		if (provider==null) {
			throw new IllegalArgumentException("provider cannot be null");
		}
		this.commentCompletionProvider = provider;
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
	 * Sets the documentation comment completion provider.
	 *
	 * @param provider The provider to use in comments.
	 * @see #getDocCommentCompletionProvider()
	 */
	public void setDocCommentCompletionProvider(CompletionProvider provider) {
		if (provider==null) {
			throw new IllegalArgumentException("provider cannot be null");
		}
		this.docCommentCompletionProvider = provider;
	}


	/**
	 * Sets the completion provider to use while in a string.
	 *
	 * @param provider The provider to use.
	 * @see #getStringCompletionProvider()
	 */
	public void setStringCompletionProvider(CompletionProvider provider) {
		stringCompletionProvider = provider;
	}


}
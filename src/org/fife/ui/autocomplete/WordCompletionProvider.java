/*
 * 12/21/2008
 *
 * WordCompletionProvider.java - A simple provider that lets the user choose
 * from a list of words.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;


/**
 * A completion provider that simply uses a list of words.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class WordCompletionProvider extends AbstractCompletionProvider {

	protected Segment seg;


	/**
	 * Constructor.
	 *
	 * @param words The words to offer as completion suggestions.  This
	 *        cannot be <code>null</code>.
	 */
	public WordCompletionProvider(String[] words) {
		completions = createCompletions(words);
		seg = new Segment();
	}


	/**
	 * Creates the completion array for an array of words.
	 *
	 * @param words The words.
	 * @return The <tt>Completion</tt> list.  This will be sorted
	 *         alphabetically.
	 */
	protected List createCompletions(String[] words) {
		List completions = new ArrayList(words.length);
		for (int i=0; i<words.length; i++) {
			completions.add(new WordCompletion(this, words[i]));
		}
		Collections.sort(completions);
		return completions;
	}


	/**
	 * Returns the text just before the current caret position that could be
	 * the start of something auto-completable.<p>
	 *
	 * This method returns all characters before the caret that are metched
	 * by  {@link #isValidChar(char)}.
	 *
	 * @param comp The text component.
	 * @return The text.
	 */
	public String getAlreadyEnteredText(JTextComponent comp) {
		
		Document doc = comp.getDocument();

		int dot = comp.getCaretPosition();
		Element root = doc.getDefaultRootElement();
		int index = root.getElementIndex(dot);
		Element elem = root.getElement(index);
		int start = elem.getStartOffset();
		int len = dot-start;
		try {
			doc.getText(start, len, seg);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return EMPTY_STRING;
		}

		int segEnd = seg.offset + len;
		start = segEnd - 1;
		while (start>=seg.offset && isValidChar(seg.array[start])) {
			start--;
		}
		start++;

		len = segEnd - start;
		return len==0 ? EMPTY_STRING : new String(seg.array, start, len);

	}


	/**
	 * Returns whether the specified character is valid in an auto-completion.
	 * The default implementation is equivalent to
	 * "<code>Character.isLetterOrDigit(ch) || ch=='_'</code>".  Subclasses
	 * can override this method to change what characters are matched.
	 *
	 * @param ch The character.
	 * @return Whether the character is valid.
	 */
	protected boolean isValidChar(char ch) {
		return Character.isLetterOrDigit(ch) || ch=='_';
	}


}
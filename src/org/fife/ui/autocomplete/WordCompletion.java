/*
 * 12/21/2008
 *
 * WordCompletion.java - A completion for a single word.
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


/**
 * A completion for a single word.  Input that matches any number of the
 * leading characters of this completion will match it.<p>
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class WordCompletion extends AbstractCompletion {

	/**
	 * The word that can be auto-completed.
	 */
	private String replacementText;


	/**
	 * Constructor.
	 *
	 * @param provider The provider that returns this completion.
	 * @param replacementText The text to be made auto-completable.
	 */
	public WordCompletion(CompletionProvider provider, String replacementText) {
		super(provider);
		this.replacementText = replacementText;
	}


	/**
	 * Returns <code>null</code> always.  Subclasses can override this method
	 * if they wish to provide a description.
	 *
	 * @return This item's description.  This will always be
	 *         <code>null</code>.
	 */
	public String getSummary() {
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getReplacementText() {
		return replacementText;
	}


}
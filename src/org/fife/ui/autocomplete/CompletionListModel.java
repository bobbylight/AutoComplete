/*
 * 12/22/2008
 *
 * CompletionListModel.java - A model that allows bulk addition of elements.
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
import java.util.Collection;
import javax.swing.AbstractListModel;


/**
 * A list model implementation that allows the bulk addition of elements.
 * This is the only feature missing from <code>DefaultListModel</code> that
 * we need.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class CompletionListModel extends AbstractListModel {

	/**
	 * Container for items in this model.
	 */
	private ArrayList delegate;


	/**
	 * Constructor.
	 */
	public CompletionListModel() {
		delegate = new ArrayList();
	}


	/**
	 * Removes all of the elements from this list.  The list will
	 * be empty after this call returns (unless it throws an exception).
	 *
	 * @see #setContents(Collection)
	 */
	public void clear() {
		int end = delegate.size()-1;
		delegate.clear();
		if (end >= 0) {
			fireIntervalRemoved(this, 0, end);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public Object getElementAt(int index) {
		return delegate.get(index);
	}


	/**
	 * {@inheritDoc}
	 */
	public int getSize() {
		return delegate.size();
	}


	/**
	 * Sets the contents of this model.  All previous contents are removed.
	 *
	 * @param contents The new contents of this model.
	 */
	public void setContents(Collection contents) {
		clear();
		if (contents.size()>0) {
			delegate.addAll(contents);
			fireIntervalAdded(this, 0, contents.size());
		}
	}


}
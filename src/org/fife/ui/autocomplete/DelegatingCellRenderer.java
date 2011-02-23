/*
 * 12/22/2008
 *
 * DelegatingCellRenderer.java - A renderer for Completions that will delegate
 * to the Completion's provider's renderer, if there is one.
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

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * List cell renderer that delegates to a {@link CompletionProvider}'s
 * renderer, if it has one.  If it doesn't, it calls into a fallback renderer.
 * If a fallback renderer isn't specified, it simply renders
 * <code>(({@link Completion})value).toString()</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DelegatingCellRenderer extends DefaultListCellRenderer {

	/**
	 * The renderer to fall back on if one isn't specified by a provider.
	 * This is usually <tt>this</tt>.
	 */
	private ListCellRenderer fallback;


	/**
	 * Returns the fallback cell renderer.
	 *
	 * @return The fallback cell renderer.
	 * @see #setFallbackCellRenderer(ListCellRenderer)
	 */
	public ListCellRenderer getFallbackCellRenderer() {
		return fallback;
	}


	/**
	 * {@inheritDoc}
	 */
	public Component getListCellRendererComponent(JList list, Object value,
						int index, boolean selected, boolean hasFocus) {
		Completion c = (Completion)value;
		CompletionProvider p = c.getProvider();
		ListCellRenderer r = p.getListCellRenderer();
		if (r!=null) {
			return r.getListCellRendererComponent(list, value, index, selected,
													hasFocus);
		}
		if (fallback==null) {
			return super.getListCellRendererComponent(list, value, index,
												selected, hasFocus);
		}
		return fallback.getListCellRendererComponent(list, value, index,
													selected, hasFocus);
	}


	/**
	 * Sets the fallback cell renderer.
	 *
	 * @param fallback The fallback cell renderer.  If this is
	 *        <code>null</code>, <tt>this</tt> will be used.
	 * @see #getFallbackCellRenderer()
	 */
	public void setFallbackCellRenderer(ListCellRenderer fallback) {
		this.fallback = fallback;
	}


	/**
	 * {@inheritDoc}
	 */
	public void updateUI() {
		super.updateUI();
		if ((fallback instanceof JComponent) && fallback!=this) {
			((JComponent)fallback).updateUI();
		}
	}


}
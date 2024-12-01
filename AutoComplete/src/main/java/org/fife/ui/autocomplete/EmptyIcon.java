/*
 * 04/29/2010
 *
 * EmptyIcon.java - The canonical icon that paints nothing.
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;
import javax.swing.Icon;


/**
 * A standard icon that doesn't paint anything.  This can be used when some
 * <code>Completion</code>s have icons and others don't, to visually align the
 * text of all completions.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class EmptyIcon implements Icon, Serializable {

	/**
	 * The size of the icon.
	 */
	private int size;


	/**
	 * Constructor.
	 *
	 * @param size The size of this icon.
	 */
	public EmptyIcon(int size) {
		this.size = size;
	}


	@Override
	public int getIconHeight() {
		return size;
	}


	@Override
	public int getIconWidth() {
		return size;
	}


	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
	}


	/**
	 * Sets the size of this icon. The parent container will likely need
	 * to be revalidated if this is called after the UI is displayed.
	 *
	 * @param size The new icon size.
	 */
	public void setSize(int size) {
		this.size = size;
	}
}

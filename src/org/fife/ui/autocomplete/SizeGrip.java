/*
 * 12/23/2008
 *
 * SizeGrip.java - A size grip component that sits at the bottom of the window,
 * allowing the user to easily resize that window.
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

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


/**
 * A component that allows its parent window to be resizable, similar to the
 * size grip seen on status bars.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SizeGrip extends JPanel {


	public SizeGrip() {
		MouseHandler adapter = new MouseHandler();
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		possiblyFixCursor(ComponentOrientation.getOrientation(getLocale()));
		setPreferredSize(new Dimension(16, 16));
	}


	/**
	 * Paints this panel.
	 *
	 * @param g The graphics context.
	 */
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		Dimension dim = getSize();
		Color c1 = UIManager.getColor("Label.disabledShadow");
		Color c2 = UIManager.getColor("Label.disabledForeground");

		ComponentOrientation orientation = getComponentOrientation();

		if (orientation.isLeftToRight()) {
			int width = dim.width  -= 3;
			int height = dim.height -= 3;
			g.setColor(c1);
			g.fillRect(width-9,height-1, 3,3);
			g.fillRect(width-5,height-1, 3,3);
			g.fillRect(width-1,height-1, 3,3);
			g.fillRect(width-5,height-5, 3,3);
			g.fillRect(width-1,height-5, 3,3);
			g.fillRect(width-1,height-9, 3,3);
			g.setColor(c2);
			g.fillRect(width-9,height-1, 2,2);
			g.fillRect(width-5,height-1, 2,2);
			g.fillRect(width-1,height-1, 2,2);
			g.fillRect(width-5,height-5, 2,2);
			g.fillRect(width-1,height-5, 2,2);
			g.fillRect(width-1,height-9, 2,2);
		}
		else {
			int height = dim.height -= 3;
			g.setColor(c1);
			g.fillRect(10,height-1, 3,3);
			g.fillRect(6,height-1, 3,3);
			g.fillRect(2,height-1, 3,3);
			g.fillRect(6,height-5, 3,3);
			g.fillRect(2,height-5, 3,3);
			g.fillRect(2,height-9, 3,3);
			g.setColor(c2);
			g.fillRect(10,height-1, 2,2);
			g.fillRect(6,height-1, 2,2);
			g.fillRect(2,height-1, 2,2);
			g.fillRect(6,height-5, 2,2);
			g.fillRect(2,height-5, 2,2);
			g.fillRect(2,height-9, 2,2);
		}

	}


	/**
	 * Overridden to ensure that the cursor for this component is appropriate
	 * for the orientation.
	 *
	 * @param o The new orientation.
	 */
	public void applyComponentOrientation(ComponentOrientation o) {
		possiblyFixCursor(o);
		super.applyComponentOrientation(o);
	}


	/**
	 * Ensures that the cursor for this component is appropriate for the
	 * orientation.
	 *
	 * @param o The new orientation.
	 */
	protected void possiblyFixCursor(ComponentOrientation o) {
		int cursor = Cursor.NE_RESIZE_CURSOR;
		if (o.isLeftToRight()) {
			cursor = Cursor.NW_RESIZE_CURSOR;
		}
		if (cursor!=getCursor().getType()) {
			setCursor(Cursor.getPredefinedCursor(cursor));
		}
	}


	/**
	 * Listens for mouse events on this panel and resizes the parent window
	 * appropriately.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	/*
	 * NOTE: We use SwingUtilities.convertPointToScreen() instead of just using
	 * the locations relative to the corner component because the latter proved
	 * buggy - stretch the window too wide and some kind of arithmetic error
	 * started happening somewhere - our window would grow way too large.
	 */
	private class MouseHandler extends javax.swing.event.MouseInputAdapter {

		private Point origPos;

		public void mouseDragged(MouseEvent e) {
			Point newPos = e.getPoint();
			SwingUtilities.convertPointToScreen(newPos, SizeGrip.this);
			int xDelta = newPos.x - origPos.x;
			int yDelta = newPos.y - origPos.y;
			Window wind = SwingUtilities.getWindowAncestor(SizeGrip.this);
			if (wind!=null) { // Should always be true
				int w = wind.getWidth();
				if (newPos.x>=wind.getX()) {
					w += xDelta;
				}
				int h = wind.getHeight();
				if (newPos.y>=wind.getY()) {
					h += yDelta;
				}
				wind.setSize(w,h);
				// invalidate()/revalidate() needed pre-1.6.
				wind.invalidate();
				wind.validate();
			}
			origPos.setLocation(newPos);
		}

		public void mousePressed(MouseEvent e) {
			origPos = e.getPoint();
			SwingUtilities.convertPointToScreen(origPos, SizeGrip.this);
		}

		public void mouseReleased(MouseEvent e) {
			origPos = null;
		}

	}


}
/*
 * 12/21/2008
 *
 * AutoCompletePopupWindow.java - A window containing a list of auto-complete
 * choices.
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Caret;
import javax.swing.text.Keymap;
import javax.swing.text.JTextComponent;


/**
 * The actual popup window of choices.  When visible, this window intercepts
 * certain keystrokes in the parent text component and uses them to navigate
 * the completion choices instead.  If Enter or Escape is pressed, the window
 * hides itself and notifies the {@link AutoCompletion} to insert the selected
 * text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class AutoCompletePopupWindow extends JWindow implements CaretListener,
									ListSelectionListener, MouseListener {

	private AutoCompletion ac;
	private JList list;
	private /*DefaultListModel*/CompletionListModel model;
	private Action oldUpAction, oldDownAction, oldLeftAction, oldRightAction,
			oldEnterAction, oldTabAction, oldEscapeAction, oldHomeAction,
			oldEndAction, oldPageUpAction, oldPageDownAction;
	private Action upAction, downAction, leftAction ,rightAction, enterAction,
			escapeAction, homeAction, endAction, pageUpAction, pageDownAction;
	private int lastLine;

	private AutoCompleteDescWindow descWindow;
	private boolean aboveCaret;

	private static final boolean DEBUG = true;


	public AutoCompletePopupWindow(Window parent, AutoCompletion ac) {

		super(parent);

		this.ac = ac;
		model = new CompletionListModel();//DefaultListModel();
		list = new JList(model);
//list.setFixedCellWidth(300);
//list.setFixedCellHeight(29);
		list.setCellRenderer(new DelegatingCellRenderer());
		list.addListSelectionListener(this);
		list.addMouseListener(this);

		JPanel contentPane = new JPanel(new BorderLayout());
		JScrollPane sp = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// In 1.4, JScrollPane.setCorner() has a bug where it won't accept
		// JScrollPane.LOWER_TRAILING_CORNER, even though that constant is
		// defined.  So we have to put the logic added in 1.5 to handle it
		// here.
		JPanel corner = new SizeGrip();
		//sp.setCorner(JScrollPane.LOWER_TRAILING_CORNER, corner);
		boolean isLeftToRight = getComponentOrientation().isLeftToRight();
	    String str = isLeftToRight ? JScrollPane.LOWER_RIGHT_CORNER :
	    								JScrollPane.LOWER_LEFT_CORNER;
	    sp.setCorner(str, corner);

		contentPane.add(sp);
		setContentPane(contentPane);
		pack();

		setFocusableWindowState(false);

		lastLine = -1;

	}


//	public void addItem(Completion item) {
//		model.addElement(item);
//	}
public void setCompletions(List completions) {
	model.setContents(completions);
}

	public void caretUpdate(CaretEvent e) {
		if (isVisible()) { // Should always be true
			int line = ac.getLineOfCaret();
			if (line!=lastLine) {
lastLine = -1;
				setVisible(false);
			}
			else {
				doAutocomplete();
			}
		}
		else if (DEBUG) {
			Thread.dumpStack();
		}
	}


//	public void clear() {
//		model.clear();
//	}


	private void createActions() {
		escapeAction = new EscapeAction();
		upAction = new UpAction();
		downAction = new DownAction();
		leftAction = new LeftAction();
		rightAction = new RightAction();
		enterAction = new EnterAction();
		homeAction = new HomeAction();
		endAction = new EndAction();
		pageUpAction = new PageUpAction();
		pageDownAction = new PageDownAction();
	}


	protected void doAutocomplete() {
		lastLine = ac.refreshPopupWindow();
	}


	/**
	 * Returns the selected value, or <code>null</code> if nothing is selected.
	 *
	 * @return The selected value.
	 */
	public Completion getSelection() {
		return (Completion)list.getSelectedValue();
	}


	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount()==2) {
			ac.doCompletion();
		}
	}


	public void mouseEntered(MouseEvent e) {
	}


	public void mouseExited(MouseEvent e) {
	}


	public void mousePressed(MouseEvent e) {
	}


	public void mouseReleased(MouseEvent e) {
	}


	/**
	 * Positions the description window relative to the comletion choices
	 * window.
	 */
	private void positionDescWindow() {

		boolean showDescWindow = descWindow!=null && ac.getShowDescWindow();
		if (!showDescWindow) {
			return;
		}

		Dimension screenSize = getToolkit().getScreenSize();
		//int totalH = Math.max(getHeight(), descWindow.getHeight());

		// Try to position to the right first.
		int x = getX() + getWidth() + 5;
		if (x+descWindow.getWidth()>screenSize.width) { // doesn't fit
			x = getX() - 5 - descWindow.getWidth();
		}

		int y = getY();
		if (aboveCaret) {
			y = y + getHeight() - descWindow.getHeight();
		}

		if (x!=descWindow.getX() || y!=descWindow.getY()) { 
			descWindow.setLocation(x, y);
		}

	}


	/**
	 * Registers keyboard actions to listen for in the text component and
	 * intercept.
	 *
	 * @see #unregisterActions()
	 */
	private void registerActions() {
		System.err.println("Registering actions");

		if (escapeAction == null) {
			createActions();
		}

		JTextComponent comp = ac.getTextComponent();
		Keymap km = comp.getKeymap();

		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		oldEscapeAction = km.getAction(ks);
		if (DEBUG && oldEscapeAction==escapeAction) {
			Thread.dumpStack();
			return;
		}
		km.addActionForKeyStroke(ks, escapeAction);

		oldUpAction = replaceAction(km, KeyEvent.VK_UP, upAction);
		oldDownAction = replaceAction(km, KeyEvent.VK_DOWN, downAction);
		oldLeftAction = replaceAction(km, KeyEvent.VK_LEFT, leftAction);
		oldRightAction = replaceAction(km, KeyEvent.VK_RIGHT, rightAction);
		oldEnterAction = replaceAction(km, KeyEvent.VK_ENTER, enterAction);
		oldTabAction = replaceAction(km, KeyEvent.VK_TAB, enterAction);
		oldHomeAction = replaceAction(km, KeyEvent.VK_HOME, homeAction);
		oldEndAction = replaceAction(km, KeyEvent.VK_END, endAction);
		oldPageUpAction = replaceAction(km, KeyEvent.VK_PAGE_UP, pageUpAction);
		oldPageDownAction = replaceAction(km, KeyEvent.VK_PAGE_DOWN, pageDownAction);

		comp.addCaretListener(this);

	}


	/**
	 * Replaces the action binded to a keystroke.
	 *
	 * @param km The map in which to replace the action.
	 * @param key The keystroke whose action to replace.
	 * @param a The action to associate with <code>key</code>.  If this is
	 *        <code>null</code>, no keystroke will be associated with the key.
	 * @return The previous action associated with the key, or <code>null</code>
	 *         if there was none.
	 */
	private Action replaceAction(Keymap km, int key, Action a) {
		KeyStroke ks = KeyStroke.getKeyStroke(key, 0);
		Action old = km.getAction(ks);
		if (a!=null) {
			km.addActionForKeyStroke(ks, a);
		} else {
			km.removeKeyStrokeBinding(ks);
		}
		return old;
	}


	public void selectFirstItem() {
		if (model.getSize() > 0) {
			list.setSelectedIndex(0);
			list.ensureIndexIsVisible(0);
		}
	}


	public void selectLastItem() {
		int index = model.getSize() - 1;
		if (index > -1) {
			list.setSelectedIndex(index);
			list.ensureIndexIsVisible(index);
		}
	}


	public void selectNextItem() {
		int index = list.getSelectedIndex();
		if (index > -1) {
			index = (index + 1) % model.getSize();
			list.setSelectedIndex(index);
			list.ensureIndexIsVisible(index);
		}
	}


	public void selectPageDownItem() {
		int visibleRowCount = list.getVisibleRowCount();
		int i = Math.min(list.getModel().getSize()-1,
						list.getSelectedIndex()+visibleRowCount);
		list.setSelectedIndex(i);
		list.ensureIndexIsVisible(i);
	}


	public void selectPageUpItem() {
		int visibleRowCount = list.getVisibleRowCount();
		int i = Math.max(0, list.getSelectedIndex()-visibleRowCount);
		list.setSelectedIndex(i);
		list.ensureIndexIsVisible(i);
	}


	public void selectPreviousItem() {
		int index = list.getSelectedIndex();
		switch (index) {
		case 0:
			index = list.getModel().getSize() - 1;
			break;
		case -1: // Check for an empty list (would be an error)
			index = list.getModel().getSize() - 1;
			if (index == -1) {
				return;
			}
			break;
		default:
			index = index - 1;
			break;
		}
		list.setSelectedIndex(index);
		list.ensureIndexIsVisible(index);
	}


	public void setLocationRelativeTo(Rectangle r) {

		boolean showDescWindow = descWindow!=null && ac.getShowDescWindow();
		Dimension screenSize = getToolkit().getScreenSize();
		int totalH = getHeight();
		if (showDescWindow) {
			totalH = Math.max(totalH, descWindow.getHeight());
		}

		// Try putting our stuff "below" the caret first.  We assume that the
		// entire height of our stuff fits on the screen one way or the other.
		aboveCaret = false;
		int y = r.y + r.height + 10;
		if (y+totalH>screenSize.height) {
			y = r.y - 10 - getHeight();
			aboveCaret = true;
		}

		// Get x-coordinate of completions.  Try to align left edge with the
		// caret first.
		int x = r.x;
		if (x<0) {
			x = 0;
		}
		else if (x+getWidth()>screenSize.width) { // completions don't fit
			x = screenSize.width - getWidth();
		}

		setLocation(x, y);

		// Position the description window, if neessary.
		if (showDescWindow) {
			positionDescWindow();
		}

	}


	/**
	 * Toggles the visibility of this popup window.
	 *
	 * @param visible Whether this window should be visible.
	 */
	public void setVisible(boolean visible) {
		if (visible!=isVisible()) {
			if (visible) {
				registerActions();
				lastLine = ac.getLineOfCaret();
				selectFirstItem();
				if (descWindow==null && ac.getShowDescWindow()) {
					descWindow = new AutoCompleteDescWindow(this, ac);
					descWindow.setSize(getSize());
					descWindow.setLocation(getX()+getWidth()+5, getY());
					// descWindow needs a kick-start the first time it's
					// displayed.
					Completion c = (Completion)list.getSelectedValue();
					descWindow.setDescriptionFor(c);
				}
			}
			else {
				unregisterActions();
			}
			super.setVisible(visible);
			if (descWindow!=null && ac.getShowDescWindow()) {
				descWindow.setVisible(visible);
			}
		}
	}


	/**
	 * Stops intercepting certain keystrokes from the text component.
	 *
	 * @see #registerActions()
	 */
	public void unregisterActions() {

		System.err.println("Unregistering actions");
		JTextComponent comp = ac.getTextComponent();
		Keymap km = comp.getKeymap();

		replaceAction(km, KeyEvent.VK_ESCAPE, oldEscapeAction);
		replaceAction(km, KeyEvent.VK_UP, oldUpAction);
		replaceAction(km, KeyEvent.VK_DOWN, oldDownAction);
		replaceAction(km, KeyEvent.VK_LEFT, oldLeftAction);
		replaceAction(km, KeyEvent.VK_RIGHT, oldRightAction);
		replaceAction(km, KeyEvent.VK_ENTER, oldEnterAction);
		replaceAction(km, KeyEvent.VK_TAB, oldTabAction);
		replaceAction(km, KeyEvent.VK_HOME, oldHomeAction);
		replaceAction(km, KeyEvent.VK_END, oldEndAction);
		replaceAction(km, KeyEvent.VK_PAGE_UP, oldPageUpAction);
		replaceAction(km, KeyEvent.VK_PAGE_DOWN, oldPageDownAction);

		comp.removeCaretListener(this);

	}


	/**
	 * Updates the <tt>LookAndFeel</tt> of this window and the description
	 * window.
	 */
	public void updateUI() {
		SwingUtilities.updateComponentTreeUI(this);
		if (descWindow!=null) {
			SwingUtilities.updateComponentTreeUI(descWindow);
		}
	}


	/**
	 * Called when a new item is selected in the popup list.
	 *
	 * @param e The event.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			Object value = list.getSelectedValue();
			if (value!=null && descWindow!=null) {
				descWindow.setDescriptionFor((Completion)value);
				positionDescWindow();
			}
		}
	}


	class DownAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isVisible()) {
				selectNextItem();
			}
		}

	}


	class EndAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isVisible()) {
				selectLastItem();
			}
		}

	}


	class EnterAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isVisible()) {
				ac.doCompletion();
			}
		}

	}


	class EscapeAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isVisible()) {
				setVisible(false);
			}
		}

	}


	class HomeAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isVisible()) {
				selectFirstItem();
			}
		}

	}


	class LeftAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isVisible()) {
				JTextComponent comp = ac.getTextComponent();
				Caret c = comp.getCaret();
				int dot = c.getDot();
				if (dot > 0) {
					c.setDot(--dot);
					// Ensure moving left hasn't moved us up a line, thus
					// hiding the popup window.
					if (comp.isVisible()) {
if (lastLine!=-1)						doAutocomplete();
					}
				}
			}
		}

	}


	class PageDownAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isVisible()) {
				selectPageDownItem();
			}
		}

	}


	class PageUpAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isVisible()) {
				selectPageUpItem();
			}
		}

	}


	class RightAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isVisible()) {
				JTextComponent comp = ac.getTextComponent();
				Caret c = comp.getCaret();
				int dot = c.getDot();
				if (dot < comp.getDocument().getLength()) {
					c.setDot(++dot);
					// Ensure moving right hasn't moved us up a line, thus
					// hiding the popup window.
					if (comp.isVisible()) {
if (lastLine!=-1)						doAutocomplete();
					}
				}
			}
		}

	}


	class UpAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			if (isVisible()) {
				selectPreviousItem();
			}
		}

	}


}
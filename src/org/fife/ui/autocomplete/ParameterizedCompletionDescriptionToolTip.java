/*
 * 12/21/2008
 *
 * AutoCompleteDescWindow.java - A window containing a description of the
 * currently selected completion.
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
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Highlighter.Highlight;

import org.fife.ui.rsyntaxtextarea.PopupWindowDecorator;


/**
 * A "tool tip" that displays information on the function or method currently
 * being entered.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ParameterizedCompletionDescriptionToolTip {

	/**
	 * The actual tool tip.
	 */
	private JWindow tooltip;

	/**
	 * The painter to paint borders around the variables.
	 */
	Highlighter.HighlightPainter p;

	/**
	 * The tags for the highlights around parameters.
	 */
	List tags;

	/**
	 * The parent AutoCompletion instance.
	 */
	private AutoCompletion ac;

	/**
	 * The label that holds the description.
	 */
	private JLabel descLabel;

	/**
	 * The completion being described.
	 */
	private ParameterizedCompletion pc;

	/**
	 * Listens for events in the text component while this window is visible.
	 */
	private Listener listener;

	/**
	 * The minimum offset into the document that the caret can move to
	 * before this tool tip disappears.
	 */
	private int minPos;

	/**
	 * The maximum offset into the document that the caret can move to
	 * before this tool tip disappears.
	 */
	private Position maxPos; // Moves with text inserted.

	/**
	 * A small popup window giving likely choices for parameterized completions.
	 */
	private ParameterizedCompletionChoicesWindow paramChoicesWindow;

	/**
	 * The text before the caret for the current parameter.  If
	 * {@link #paramChoicesWindow} is non-<code>null</code>, this is used to
	 * determine what parameter choices to actually show.
	 */
	private String paramPrefix;

	/**
	 * The currently "selected" parameter in the displayed text.
	 */
	private int lastSelectedParam;

	private Object oldTabKey;
	private Action oldTabAction;
	private Object oldShiftTabKey;
	private Action oldShiftTabAction;
	private Object oldUpKey;
	private Action oldUpAction;
	private Object oldDownKey;
	private Action oldDownAction;
	private Object oldEnterKey;
	private Action oldEnterAction;
	private Object oldEscapeKey;
	private Action oldEscapeAction;
	private Object oldClosingKey;
	private Action oldClosingAction;

	private static final String IM_KEY_TAB = "ParamCompDescTip.Tab";
	private static final String IM_KEY_SHIFT_TAB = "ParamCompDescTip.ShiftTab";
	private static final String IM_KEY_UP = "ParamCompDescTip.Up";
	private static final String IM_KEY_DOWN = "ParamCompDescTip.Down";
	private static final String IM_KEY_ESCAPE = "ParamCompDescTip.Escape";
	private static final String IM_KEY_ENTER = "ParamCompDescTip.Enter";
	private static final String IM_KEY_CLOSING = "ParamCompDescTip.Closing";


	/**
	 * Constructor.
	 *
	 * @param owner The parent window.
	 * @param ac The parent auto-completion.
	 * @param pc The completion being described.
	 */
	public ParameterizedCompletionDescriptionToolTip(Window owner,
						AutoCompletion ac, ParameterizedCompletion pc) {

		tooltip = new JWindow(owner);

		this.ac = ac;
		this.pc = pc;

		descLabel = new JLabel();
		descLabel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(Color.BLACK),
					BorderFactory.createEmptyBorder(2, 5, 2, 5)));
		descLabel.setOpaque(true);
		descLabel.setBackground(TipUtil.getToolTipBackground());
		// It appears that if a JLabel is set as a content pane directly, when
		// using the JDK's opacity API's, it won't paint its background, even
		// if label.setOpaque(true) is called.  You have to have a container
		// underneath it for it to paint its background.  Thus, we embed our
		// label in a parent JPanel to handle this case.
		//tooltip.setContentPane(descLabel);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(descLabel);
		tooltip.setContentPane(panel);

		// Give apps a chance to decorate us with drop shadows, etc.
		PopupWindowDecorator decorator = PopupWindowDecorator.get();
		if (decorator!=null) {
			decorator.decorate(tooltip);
		}

		lastSelectedParam = -1;
		updateText(0);

		tooltip.setFocusableWindowState(false);
		listener = new Listener();

		p = new OutlineHighlightPainter(Color.GRAY);
		tags = new ArrayList(1); // Usually small

		paramChoicesWindow = createParamChoicesWindow();

	}


	/**
	 * Creates the completion window offering suggestions for parameters.
	 *
	 * @return The window.
	 */
	private ParameterizedCompletionChoicesWindow createParamChoicesWindow() {
		ParameterizedCompletionChoicesWindow pcw =
			new ParameterizedCompletionChoicesWindow(tooltip.getOwner(),
														ac, this);
		pcw.initialize(pc);
		return pcw;
	}


	/**
	 * Returns the highlight of the current parameter.
	 *
	 * @return The current parameter's highlight, or <code>null</code> if
	 *         the caret is not in a parameter's bounds.
	 * @see #getCurrentParameterStartOffset()
	 */
	private Highlight getCurrentParameterHighlight() {

		JTextComponent tc = ac.getTextComponent();
		int dot = tc.getCaretPosition();
		if (dot>0) {
			dot--; // Workaround for Java Highlight issues
		}

		List paramHighlights = getParameterHighlights();
		for (int i=0; i<paramHighlights.size(); i++) {
			Highlight h = (Highlight)paramHighlights.get(i);
			if (dot>=h.getStartOffset() && dot<h.getEndOffset()) {
				return h;
			}
		}

		return null;

	}


	/**
	 * Returns the starting offset of the current parameter.
	 *
	 * @return The current parameter's starting offset, or <code>-1</code> if
	 *         the caret is not in a parameter's bounds.
	 * @see #getCurrentParameterHighlight()
	 */
	private int getCurrentParameterStartOffset() {
		Highlight h = getCurrentParameterHighlight();
		return h!=null ? h.getStartOffset()+1 : -1;
	}


	private List getParameterHighlights() {
		List paramHighlights = new ArrayList(1);
		JTextComponent tc = ac.getTextComponent();
		Highlight[] highlights = tc.getHighlighter().getHighlights();
		for (int i=0; i<highlights.length; i++) {
			if (highlights[i].getPainter()==p) {
				paramHighlights.add(highlights[i]);
			}
		}
		return paramHighlights;
	}


	/**
	 * Inserts the choice selected in the parameter choices window.
	 *
	 * @return Whether the choice was inserted.  This will be <code>false</code>
	 *         if the window is not visible, or no choice is selected.
	 */
	boolean insertSelectedChoice() {
		if (paramChoicesWindow!=null && paramChoicesWindow.isVisible()) {
			String choice = paramChoicesWindow.getSelectedChoice();
			if (choice!=null) {
				JTextComponent tc = ac.getTextComponent();
				Highlight h = getCurrentParameterHighlight();
				if (h!=null) {
					 // "+1" is a workaround for Java Highlight issues.
					tc.setSelectionStart(h.getStartOffset()+1);
					tc.setSelectionEnd(h.getEndOffset());
					tc.replaceSelection(choice);
					moveToNextParam();
				}
				else {
					UIManager.getLookAndFeel().provideErrorFeedback(tc);
				}
				return true;
			}
		}
		return false;
	}


	/**
	 * Installs key bindings on the text component that facilitate the user
	 * editing this completion's parameters.
	 *
	 * @see #uninstallKeyBindings()
	 */
	private void installKeyBindings() {

		if (AutoCompletion.getDebug()) {
			System.out.println("ToolTip: Installing keybindings");
		}

		JTextComponent tc = ac.getTextComponent();
		InputMap im = tc.getInputMap();
		ActionMap am = tc.getActionMap();

		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		oldTabKey = im.get(ks);
		im.put(ks, IM_KEY_TAB);
		oldTabAction = am.get(IM_KEY_TAB);
		am.put(IM_KEY_TAB, new NextParamAction());

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
		oldShiftTabKey = im.get(ks);
		im.put(ks, IM_KEY_SHIFT_TAB);
		oldShiftTabAction = am.get(IM_KEY_SHIFT_TAB);
		am.put(IM_KEY_SHIFT_TAB, new PrevParamAction());

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
		oldUpKey = im.get(ks);
		im.put(ks, IM_KEY_UP);
		oldUpAction = am.get(IM_KEY_UP);
		am.put(IM_KEY_UP, new NextChoiceAction(-1, oldUpAction));

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		oldDownKey = im.get(ks);
		im.put(ks, IM_KEY_DOWN);
		oldDownAction = am.get(IM_KEY_DOWN);
		am.put(IM_KEY_DOWN, new NextChoiceAction(1, oldDownAction));

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		oldEnterKey = im.get(ks);
		im.put(ks, IM_KEY_ENTER);
		oldEnterAction = am.get(IM_KEY_ENTER);
		am.put(IM_KEY_ENTER, new GotoEndAction());

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		oldEscapeKey = im.get(ks);
		im.put(ks, IM_KEY_ESCAPE);
		oldEscapeAction = am.get(IM_KEY_ESCAPE);
		am.put(IM_KEY_ESCAPE, new HideAction());

		char end = pc.getProvider().getParameterListEnd();
		ks = KeyStroke.getKeyStroke(end);
		oldClosingKey = im.get(ks);
		im.put(ks, IM_KEY_CLOSING);
		oldClosingAction = am.get(IM_KEY_CLOSING);
		am.put(IM_KEY_CLOSING, new ClosingAction());

	}


	/**
	 * Moves to and selects the next parameter.
	 *
	 * @see #moveToPreviousParam()
	 */
	private void moveToNextParam() {

		JTextComponent tc = ac.getTextComponent();
		int dot = tc.getCaretPosition();

		int tagCount = tags.size();
		if (tagCount==0) {
			tc.setCaretPosition(maxPos.getOffset());
			setVisible(false, false);
		}

		Highlight currentNext = null;
		int pos = -1;
		List highlights = getParameterHighlights();
		for (int i=0; i<highlights.size(); i++) {
			Highlight hl = (Highlight)highlights.get(i);
			// Check "< dot", not "<= dot" as OutlineHighlightPainter paints
			// starting at one char AFTER the highlight starts, to work around
			// Java issue.  Thanks to Matthew Adereth!
			if (currentNext==null || currentNext.getStartOffset()</*=*/dot ||
					(hl.getStartOffset()>dot &&
					hl.getStartOffset()<=currentNext.getStartOffset())) {
				currentNext = hl;
				pos = i;
			}
		}

		if (currentNext!=null && dot<=currentNext.getStartOffset()) {
			 // "+1" is a workaround for Java Highlight issues.
			tc.setSelectionStart(currentNext.getStartOffset()+1);
			tc.setSelectionEnd(currentNext.getEndOffset());
			updateText(pos);
		}
		else {
			tc.setCaretPosition(maxPos.getOffset());
			setVisible(false, false);
		}

	}


	/**
	 * Moves to and selects the previous parameter.
	 *
	 * @see #moveToNextParam()
	 */
	private void moveToPreviousParam() {

		JTextComponent tc = ac.getTextComponent();

		int tagCount = tags.size();
		if (tagCount==0) { // Should never happen
			tc.setCaretPosition(maxPos.getOffset());
			setVisible(false, false);
		}

		int dot = tc.getCaretPosition();
		int selStart = tc.getSelectionStart()-1; // Workaround for Java Highlight issues.
		Highlight currentPrev = null;
		int pos = 0;
		List highlights = getParameterHighlights();

		for (int i=0; i<highlights.size(); i++) {
			Highlight h = (Highlight)highlights.get(i);
			if (currentPrev==null || currentPrev.getStartOffset()>=dot ||
					(h.getStartOffset()<selStart &&
					h.getStartOffset()>currentPrev.getStartOffset())) {
				currentPrev = h;
				pos = i;
			}
		}

		// Loop back from param 0 to last param.
		if (pos==0 && lastSelectedParam==0 && highlights.size()>1) {
			pos = highlights.size() - 1;
			currentPrev = (Highlight)highlights.get(pos);
			 // "+1" is a workaround for Java Highlight issues.
			tc.setSelectionStart(currentPrev.getStartOffset()+1);
			tc.setSelectionEnd(currentPrev.getEndOffset());
			updateText(pos);
		}
		else if (currentPrev!=null && dot>currentPrev.getStartOffset()) {
			 // "+1" is a workaround for Java Highlight issues.
			tc.setSelectionStart(currentPrev.getStartOffset()+1);
			tc.setSelectionEnd(currentPrev.getEndOffset());
			updateText(pos);
		}
		else {
			tc.setCaretPosition(maxPos.getOffset());
			setVisible(false, false);
		}

	}


	/**
	 * Updates the optional window listing likely completion choices,
	 */
	private void prepareParamChoicesWindow() {

		// If this window was set to null, the user pressed Escape to hide it
		if (paramChoicesWindow!=null) {

			int offs = getCurrentParameterStartOffset();
			if (offs==-1) {
				paramChoicesWindow.setVisible(false);
				return;
			}

			JTextComponent tc = ac.getTextComponent();
			try {
				Rectangle r = tc.modelToView(offs);
				Point p = new Point(r.x, r.y);
				SwingUtilities.convertPointToScreen(p, tc);
				r.x = p.x;
				r.y = p.y;
				paramChoicesWindow.setLocationRelativeTo(r);
			} catch (BadLocationException ble) { // Should never happen
				UIManager.getLookAndFeel().provideErrorFeedback(tc);
				ble.printStackTrace();
			}

			// Toggles visibility, if necessary.
			paramChoicesWindow.setParameter(lastSelectedParam, paramPrefix);

		}

	}


	/**
	 * Removes the bounding boxes around parameters.
	 */
	private void removeParameterHighlights() {
		JTextComponent tc = ac.getTextComponent();
		Highlighter h = tc.getHighlighter();
		for (int i=0; i<tags.size(); i++) {
			h.removeHighlight(tags.get(i));
		}
		tags.clear();
	}


	/**
	 * Sets the location of this tool tip relative to the given rectangle.
	 *
	 * @param r The visual position of the caret (in screen coordinates).
	 */
	public void setLocationRelativeTo(Rectangle r) {

		// Multi-monitor support - make sure the completion window (and
		// description window, if applicable) both fit in the same window in
		// a multi-monitor environment.  To do this, we decide which monitor
		// the rectangle "r" is in, and use that one (just pick top-left corner
		// as the defining point).
		Rectangle screenBounds = Util.getScreenBoundsForPoint(r.x, r.y);
		//Dimension screenSize = tooltip.getToolkit().getScreenSize();

		// Try putting our stuff "above" the caret first.
		int y = r.y - 5 - tooltip.getHeight();
		if (y<0) {
			y = r.y + r.height + 5;
		}

		// Get x-coordinate of completions.  Try to align left edge with the
		// caret first.
		int x = r.x;
		if (x<screenBounds.x) {
			x = screenBounds.x;
		}
		else if (x+tooltip.getWidth()>screenBounds.x+screenBounds.width) { // completions don't fit
			x = screenBounds.x + screenBounds.width - tooltip.getWidth();
		}

		tooltip.setLocation(x, y);

	}


	/**
	 * Toggles the visibility of this tool tip.
	 *
	 * @param visible Whether the tool tip should be visible.
	 * @param addParamListStart Whether or not
	 *        {@link CompletionProvider#getParameterListStart()} should be
	 *        added to the text component.  If <code>visible</code> is
	 *        <code>false</code>, this parameter is ignored.
	 */
	public void setVisible(boolean visible, boolean addParamListStart) {

		if (visible!=tooltip.isVisible()) {

			JTextComponent tc = ac.getTextComponent();

			if (visible) {
				listener.install(tc, addParamListStart);
				// First time through, we'll need to create this window.
				if (paramChoicesWindow==null) {
					paramChoicesWindow = createParamChoicesWindow();
				}
				prepareParamChoicesWindow();
			}
			else {
				listener.uninstall();
			}

			tooltip.setVisible(visible);
			if (paramChoicesWindow!=null) {
				// Only really needed to hide the window (i.e. visible==false)
				paramChoicesWindow.setVisible(visible);
			}

		}

	}


	/**
	 * Removes the key bindings we installed.
	 *
	 * @see #installKeyBindings()
	 */
	private void uninstallKeyBindings() {

		if (AutoCompletion.getDebug()) {
			System.out.println("ToolTip: Uninstalling keybindings");
		}


		JTextComponent tc = ac.getTextComponent();
		InputMap im = tc.getInputMap();
		ActionMap am = tc.getActionMap();

		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		im.put(ks, oldTabKey);
		am.put(IM_KEY_TAB, oldTabAction);

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
		im.put(ks, oldShiftTabKey);
		am.put(IM_KEY_SHIFT_TAB, oldShiftTabAction);

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
		im.put(ks, oldUpKey);
		am.put(IM_KEY_UP, oldUpAction);

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		im.put(ks, oldDownKey);
		am.put(IM_KEY_DOWN, oldDownAction);

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		im.put(ks, oldEnterKey);
		am.put(IM_KEY_ENTER, oldEnterAction);

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		im.put(ks, oldEscapeKey);
		am.put(IM_KEY_ESCAPE, oldEscapeAction);

		char end = pc.getProvider().getParameterListEnd();
		ks = KeyStroke.getKeyStroke(end);
		im.put(ks, oldClosingKey);
		am.put(IM_KEY_CLOSING, oldClosingAction);

	}


	/**
	 * Updates the text in the tool tip to have the current parameter
	 * displayed in bold.  The "current parameter" is determined from the
	 * current caret position.
	 *
	 * @return Whether the text needed to be updated.
	 */
	private boolean updateText() {

		JTextComponent tc = ac.getTextComponent();
		int dot = tc.getSelectionStart();
		int mark = tc.getSelectionEnd();
		int index = -1;
		paramPrefix = null;

		List paramHighlights = getParameterHighlights();
		for (int i=0; i<paramHighlights.size(); i++) {
			Highlight h = (Highlight)paramHighlights.get(i);
			// "+1" because of param hack - see OutlineHighlightPainter
			int start = h.getStartOffset()+1;
			if (dot>=start && dot<=h.getEndOffset()) {
				try {
					// All text selected => offer all suggestions
					if (dot==start && mark==h.getEndOffset()) {
						paramPrefix = null;
					}
					// Not everything selected => use prefix before selection
					else {
						paramPrefix = tc.getText(start, dot-start);
					}
				} catch (BadLocationException ble) {
					ble.printStackTrace();
					paramPrefix = null;
				}
				index = i;
				break;
			}
		}

		return updateText(index);

	}


	/**
	 * Updates the text in the tool tip to have the current parameter
	 * displayed in bold.
	 *
	 * @param selectedParam The index of the selected parameter.
	 * @return Whether the text needed to be updated.
	 */
	private boolean updateText(int selectedParam) {

		// Don't redo everything if they're just using the arrow keys to move
		// through each char of a single parameter, for example.
		if (selectedParam==lastSelectedParam) {
			return false;
		}
		lastSelectedParam = selectedParam;

		StringBuffer sb = new StringBuffer("<html>");
		int paramCount = pc.getParamCount();
		for (int i=0; i<paramCount; i++) {

			if (i==selectedParam) {
				sb.append("<b>");
			}

			// Some parameter types may have chars in them unfriendly to HTML
			// (such as type parameters in Java).  We need to take care to
			// escape these.
			String temp = pc.getParam(i).toString();
			int lt = temp.indexOf('<');
			if (lt>-1) {
				sb.append(temp.substring(0, lt));
				sb.append("&lt;");
				for (int j=lt+1; j<temp.length(); j++) {
					char ch = temp.charAt(j);
					switch (ch) {
						case '<':
							sb.append("&lt;");
							break;
						case '>':
							sb.append("&gt;");
							break;
						default:
							sb.append(ch);
							break;
					}
				}
			}
			else {
				sb.append(temp);
			}

			if (i==selectedParam) {
				sb.append("</b>");
			}
			if (i<paramCount-1) {
				sb.append(pc.getProvider().getParameterListSeparator());
			}

		}

		if (selectedParam>=0 && selectedParam<paramCount) {
			ParameterizedCompletion.Parameter param =
							pc.getParam(selectedParam);
			String desc = param.getDescription();
			if (desc!=null) {
				sb.append("<br>");
				sb.append(desc);
			}
		}

		descLabel.setText(sb.toString());
		tooltip.pack();

		return true;

	}


	/**
	 * Updates the <tt>LookAndFeel</tt> of this window and the description
	 * window.
	 */
	public void updateUI() {
		SwingUtilities.updateComponentTreeUI(tooltip);
		if (paramChoicesWindow!=null) {
			paramChoicesWindow.updateUI();
		}
	}


	/**
	 * Called when the user presses Enter while entering parameters.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class GotoEndAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {

			// If the param choices window is visible and something is chosen,
			// replace the parameter with it and move to the next one.
			if (paramChoicesWindow!=null && paramChoicesWindow.isVisible()) {
				if (insertSelectedChoice()) {
					return;
				}
			}

			// Otherwise, just move to the end.
			JTextComponent tc = ac.getTextComponent();
			tc.setCaretPosition(maxPos.getOffset());
			setVisible(false, false);

		}

	}


	/**
	 * Called when the user types the character marking the closing of the
	 * parameter list, such as '<code>)</code>'.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class ClosingAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {

			JTextComponent tc = ac.getTextComponent();
			int dot = tc.getCaretPosition();
			char end = pc.getProvider().getParameterListEnd();

			// Are they at or past the end of the parameters?
			if (dot>=maxPos.getOffset()-1) { // ">=" for overwrite mode

				if (dot==maxPos.getOffset()) { // Happens in overwrite mode
					tc.replaceSelection(Character.toString(end));
				}

				else { // Typical case.
					// Try to decide if we're closing a paren that is a part
					// of the (last) arg being typed.
					String text = getArgumentText(dot);
					if (text!=null) {
						char start = pc.getProvider().getParameterListStart();
						int startCount = getCount(text, start);
						int endCount = getCount(text, end);
						if (startCount>endCount) { // Just closing a paren
							tc.replaceSelection(Character.toString(end));
							return;
						}
					}
					tc.setCaretPosition(maxPos.getOffset());
				}

				setVisible(false, false);

			}

			// If not (in the middle of parameters), just insert the paren.
			else {
				tc.replaceSelection(Character.toString(end));
			}

		}

		public String getArgumentText(int offs) {
			List paramHighlights = getParameterHighlights();
			if (paramHighlights==null || paramHighlights.size()==0) {
				return null;
			}
			for (int i=0; i<paramHighlights.size(); i++) {
				Highlight h = (Highlight)paramHighlights.get(i);
				if (offs>=h.getStartOffset() && offs<=h.getEndOffset()) {
					int start = h.getStartOffset() + 1;
					int len = h.getEndOffset() - start;
					JTextComponent tc = ac.getTextComponent();
					Document doc = tc.getDocument();
					try {
						return doc.getText(start, len);
					} catch (BadLocationException ble) {
						UIManager.getLookAndFeel().provideErrorFeedback(tc);
						ble.printStackTrace();
						return null;
					}
				}
			}
			return null;
		}

		public int getCount(String text, char ch) {
			int count = 0;
			int old = 0;
			int pos = 0;
			while ((pos=text.indexOf(ch, old))>-1) {
				count++;
				old = pos + 1;
			}
			
			return count;
		}

	}


	/**
	 * Action performed when the user hits the escape key.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class HideAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			// On first escape press, if the param choices window is visible,
			// just remove it, but keep ability to tab through params.  If
			// param choices window isn't visible, or second escape press,
			// exit tabbing through params entirely.
			if (paramChoicesWindow!=null && paramChoicesWindow.isVisible()) {
				paramChoicesWindow.setVisible(false);
				paramChoicesWindow = null;
			}
			else {
				setVisible(false, false);
			}
		}

	}


	/**
	 * Listens for various events in the text component while this tool tip
	 * is visible.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class Listener implements FocusListener, CaretListener {

		/**
		 * Called when the text component's caret moves.
		 *
		 * @param e The event.
		 */
		public void caretUpdate(CaretEvent e) {
			if (maxPos==null) { // Sanity check
				setVisible(false, false);
				return;
			}
			int dot = e.getDot();
			if (dot<minPos || dot>=maxPos.getOffset()) {
				setVisible(false, false);
				return;
			}
			/*boolean updated = */updateText();
			if (tooltip.isVisible()) {
				prepareParamChoicesWindow();
			}
		}


		/**
		 * Called when the text component gains focus.
		 *
		 * @param e The event.
		 */
		public void focusGained(FocusEvent e) {
			// Do nothing
		}


		/**
		 * Called when the text component loses focus.
		 *
		 * @param e The event.
		 */
		public void focusLost(FocusEvent e) {
			setVisible(false, false);
		}


		/**
		 * Returns the text to insert for a parameter.
		 *
		 * @param param The parameter.
		 * @return The text.
		 */
		private String getParamText(ParameterizedCompletion.Parameter param) {
			String text = param.getName();
			if (text==null) {
				text = param.getType();
				if (text==null) { // Shouldn't ever happen
					text = "arg";
				}
			}
			return text;
		}


		/**
		 * Installs this listener onto a text component.
		 *
		 * @param tc The text component to install onto.
		 * @param addParamListStart Whether or not
		 *        {@link CompletionProvider#getParameterListStart()} should be
		 *        added to the text component.
		 * @see #uninstall()
		 */
		public void install(JTextComponent tc, boolean addParamStartList) {

			// Add listeners to the text component.
			tc.addCaretListener(this);
			tc.addFocusListener(this);
			installKeyBindings();

			StringBuffer sb = new StringBuffer();
			if (addParamStartList) {
				sb.append(pc.getProvider().getParameterListStart());
			}
			int dot = tc.getCaretPosition() + sb.length();
			int paramCount = pc.getParamCount();
			List paramLocs = null;
			if (paramCount>0) {
				paramLocs = new ArrayList(paramCount);
			}
			Highlighter h = tc.getHighlighter();

			try {

				// Get the range in which the caret can move before we hide
				// this tooltip.
				minPos = dot;
				maxPos = tc.getDocument().createPosition(dot-sb.length());
				int firstParamLen = 0;

				// Create the text to insert (keep it one completion for
				// performance and simplicity of undo/redo).
				int start = dot;
				for (int i=0; i<paramCount; i++) {
					FunctionCompletion.Parameter param = pc.getParam(i);
					String paramText = getParamText(param);
					if (i==0) {
						firstParamLen = paramText.length();
					}
					sb.append(paramText);
					int end = start + paramText.length();
					paramLocs.add(new Point(start, end));
					// Patch for param. list separators with length > 2 -
					// thanks to Matthew Adereth!
					String sep = pc.getProvider().getParameterListSeparator();
					if (i<paramCount-1 && sep!=null) {
						sb.append(sep);
						start = end + sep.length();
					}
				}
				sb.append(pc.getProvider().getParameterListEnd());

				// Insert the parameter text and add highlights around the
				// parameters.
				tc.replaceSelection(sb.toString());
				for (int i=0; i<paramCount; i++) {
					Point pt = (Point)paramLocs.get(i);
					 // "-1" is a workaround for Java Highlight issues.
					tags.add(h.addHighlight(pt.x-1, pt.y, p));
				}

				// Go back and start at the first parameter.
				tc.setCaretPosition(dot);
				if (pc.getParamCount()>0) {
					tc.moveCaretPosition(dot+firstParamLen);
				}

			} catch (BadLocationException ble) {
				ble.printStackTrace(); // Never happens
			}

		}


		/**
		 * Uninstalls this listener from the current text component.
		 * 
		 */
		public void uninstall() {

			JTextComponent tc = ac.getTextComponent();
			tc.removeCaretListener(this);
			tc.removeFocusListener(this);
			uninstallKeyBindings();

			// Remove WeakReferences in javax.swing.text.
			maxPos = null;
			minPos = -1;
			removeParameterHighlights();

		}


	}


	/**
	 * Action performed when the user presses the up or down arrow keys and
	 * the parameter completion choices popup is visible.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class NextChoiceAction extends AbstractAction {

		private Action oldAction;
		private int amount;

		public NextChoiceAction(int amount, Action oldAction) {
			this.amount = amount;
			this.oldAction = oldAction;
		}

		public void actionPerformed(ActionEvent e) {
			if (paramChoicesWindow!=null && paramChoicesWindow.isVisible()) {
				paramChoicesWindow.incSelection(amount);
			}
			else if (oldAction!=null) {
				oldAction.actionPerformed(e);
			}
			else {
				setVisible(false, false);
			}
		}

	}


	/**
	 * Action performed when the user hits the tab key.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class NextParamAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			moveToNextParam();
		}

	}


	/**
	 * Action performed when the user hits shift+tab.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class PrevParamAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			moveToPreviousParam();
		}

	}


}
/*
 * 12/21/2008
 *
 * ParameterizedCompletionDescriptionToolTip.java - A "tool tip" displaying
 * information on the function or method currently being entered.
 * 
 * This library is distributed under a modified BSD license.  See the included
 * RSyntaxTextArea.License.txt file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.Highlight;

import org.fife.ui.rsyntaxtextarea.PopupWindowDecorator;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;


/**
 * A "tool tip" that displays information on the function or method currently
 * being entered.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ParameterizedCompletionDescriptionToolTip {

	/**
	 * The parent context.
	 */
	private ParameterizedCompletionContext context;

	/**
	 * The actual tool tip.
	 */
	private JWindow tooltip;

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
	 * The currently "selected" parameter in the displayed text.
	 */
	private int lastSelectedParam;


	/**
	 * Constructor.
	 *
	 * @param owner The parent window.
	 * @param ac The parent auto-completion.
	 * @param pc The completion being described.
	 */
	public ParameterizedCompletionDescriptionToolTip(Window owner,
			ParameterizedCompletionContext context,
			AutoCompletion ac, ParameterizedCompletion pc) {

		tooltip = new JWindow(owner);

		this.context = context;
		this.ac = ac;
		this.pc = pc;

		descLabel = new JLabel();
		descLabel.setBorder(BorderFactory.createCompoundBorder(
					TipUtil.getToolTipBorder(),
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

	}


	public int getLastSelectedParam() {
		return lastSelectedParam;
	}


	/**
	 * Returns whether this tool tip is visible.
	 *
	 * @return Whether this tool tip is visible.
	 * @see #setVisible(boolean)
	 */
	public boolean isVisible() {
		return tooltip.isVisible();
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
	 * @param visible Whether this tool tip should be visible.
	 * @see #isVisible()
	 */
	public void setVisible(boolean visible) {
		tooltip.setVisible(visible);
	}


	/**
	 * Updates the text in the tool tip to have the current parameter
	 * displayed in bold.  The "current parameter" is determined from the
	 * current caret position.
	 *
	 * @return The "prefix" of text in the caret's parameter before the caret.
	 */
	public String updateText() {

		JTextComponent tc = ac.getTextComponent();
		int dot = tc.getSelectionStart();
		int mark = tc.getSelectionEnd();
		int index = -1;
		String paramPrefix = null;

		List paramHighlights = context.getParameterHighlights();
		for (int i=0; i<paramHighlights.size(); i++) {
			Highlight h = (Highlight)paramHighlights.get(i);
			// "+1" because of param hack - see OutlineHighlightPainter
			int start = h.getStartOffset()+1;
			if (dot>=start && dot<=h.getEndOffset()) {
				try {
					// All text selected => offer all suggestions, otherwise
					// use prefix before selection
					if (dot!=start || mark!=h.getEndOffset()) {
						paramPrefix = tc.getText(start, dot-start);
					}
				} catch (BadLocationException ble) {
					ble.printStackTrace();
				}
				index = i;
				break;
			}
		}

		updateText(index);
		return paramPrefix;

	}


	/**
	 * Updates the text in the tool tip to have the current parameter
	 * displayed in bold.
	 *
	 * @param selectedParam The index of the selected parameter.
	 * @return Whether the text needed to be updated.
	 */
	public boolean updateText(int selectedParam) {

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
			sb.append(RSyntaxUtilities.escapeForHtml(temp, "<br>", false));

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
	}


}
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
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;


/**
 * The optional "description" window that describes the currently selected
 * item in the auto-completion window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class AutoCompleteDescWindow extends JWindow implements HyperlinkListener {

	/**
	 * The parent AutoCompletion instance.
	 */
	private AutoCompletion ac;

	/**
	 * Renders the HTML description.
	 */
	private JEditorPane descArea;

	/**
	 * The scroll pane that {@link #descArea} is in.
	 */
	private JScrollPane scrollPane;

	/**
	 * The bottom panel, containing the toolbar and size grip.
	 */
	private JPanel bottomPanel;

	/**
	 * The toolbar with "back" and "forward" buttons.
	 */
	private JToolBar descWindowNavBar;

	/**
	 * Action that goes to the previous description displayed.
	 */
	private Action backAction;

	/**
	 * Action that goes to the next description displayed.
	 */
	private Action forwardAction;

	/**
	 * History of descriptions displayed.
	 */
	private List history;

	/**
	 * The current position in {@link #history}.
	 */
	private int historyPos;

	/**
	 * The resource bundle for this window.
	 */
	private ResourceBundle bundle;

	/**
	 * The resource bundle name.
	 */
	private static final String MSG =
					"org.fife.ui.autocomplete.AutoCompleteDescWindow";


	/**
	 * Constructor.
	 *
	 * @param owner The parent window.
	 * @param ac The parent auto-completion.
	 */
	public AutoCompleteDescWindow(Window owner, AutoCompletion ac) {

		super(owner);
		this.ac = ac;

		JPanel cp = new JPanel(new BorderLayout());
//		cp.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		descArea = new JEditorPane("text/html", null);
		tweakDescArea();
		descArea.addHyperlinkListener(this);
		scrollPane = new JScrollPane(descArea);
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		scrollPane.setBackground(descArea.getBackground());
		scrollPane.getViewport().setBackground(descArea.getBackground());
		cp.add(scrollPane);

		descWindowNavBar = new JToolBar();
		backAction = new ToolBarBackAction();
		forwardAction = new ToolBarForwardAction();
		descWindowNavBar.setFloatable(false);
		descWindowNavBar.add(new JButton(backAction));
		descWindowNavBar.add(new JButton(forwardAction));

		bottomPanel = new JPanel(new BorderLayout());
		SizeGrip rp = new SizeGrip();
		bottomPanel.add(descWindowNavBar, BorderLayout.LINE_START);
		bottomPanel.add(rp, BorderLayout.LINE_END);
		cp.add(bottomPanel, BorderLayout.SOUTH);
		setContentPane(cp);

		setFocusableWindowState(false);

		history = new ArrayList(1); // Usually small
		historyPos = -1;

	}


	/**
	 * Sets the currently displayed description and updates the history.
	 *
	 * @param html The new description.
	 */
	private void addToHistory(String html) {
		history.add(++historyPos, html);
		clearHistoryAfterCurrentPos();
		setActionStates();
	}


	/**
	 * Clears the history of viewed descriptions.
	 */
	private void clearHistory() {
		history.clear(); // Try to free some memory.
		historyPos = -1;
		if (descWindowNavBar!=null) {
			setActionStates();
		}
	}


	/**
	 * Makes the current history page the last one in the history.
	 */
	private void clearHistoryAfterCurrentPos() {
		for (int i=history.size()-1; i>historyPos; i--) {
			history.remove(i);
		}
		setActionStates();
	}


	/**
	 * Copies from the description text area, if it is visible and there is
	 * a selection.
	 *
	 * @return Whether a copy occurred.
	 */
	public boolean copy() {
		if (isVisible() &&
				descArea.getSelectionStart()!=descArea.getSelectionEnd()) {
			descArea.copy();
			return true;
		}
		return false;
	}


	/**
	 * Returns the default background color to use for the description
	 * window.
	 *
	 * @return The default background color.
	 */
	protected Color getDefaultBackground() {
		Color c = UIManager.getColor("ToolTip.background");
		if (c==null) { // Some LookAndFeels like Nimbus
			c = UIManager.getColor("info"); // Used by Nimbus (and others)
			if (c==null) {
				c = SystemColor.infoText; // System default
			}
		}
		return c;
	}


	/**
	 * Returns the localized message for the specified key.
	 *
	 * @param key The key.
	 * @return The localized message.
	 */
	private String getString(String key) {
		if (bundle==null) {
			bundle = ResourceBundle.getBundle(MSG);
		}
		return bundle.getString(key);
	}


	/**
	 * Called when a hyperlink is clicked.
	 *
	 * @param e The event.
	 */
	public void hyperlinkUpdate(HyperlinkEvent e) {

		HyperlinkEvent.EventType type = e.getEventType();

		if (type.equals(HyperlinkEvent.EventType.ACTIVATED)) {
			URL url = e.getURL();
			if (url!=null) {
				ExternalURLHandler handler = ac.getExternalURLHandler();
				if (handler!=null) {
					handler.urlClicked(url);
					return;
				}
				// No handler - try loading in external browser (Java 6+ only).
				try {
					Util.browse(new URI(url.toString()));
				} catch (/*IO*/URISyntaxException ioe) {
					UIManager.getLookAndFeel().provideErrorFeedback(descArea);
					ioe.printStackTrace();
				}
			}
			else { // Simple function name text, like in c.xml
				// FIXME: This is really a hack, and we assume we can find the
				// linked-to item in the same CompletionProvider.
				AutoCompletePopupWindow parent =
								(AutoCompletePopupWindow)getParent();
				CompletionProvider p = parent.getSelection().getProvider();
				if (p instanceof AbstractCompletionProvider) {
					String name = e.getDescription();
					List l = ((AbstractCompletionProvider)p).
										getCompletionByInputText(name);
					if (l!=null && !l.isEmpty()) {
						// Just use the 1st one if there's more than 1
						Completion c = (Completion)l.get(0);
						setDescriptionFor(c, true);
					}
					else {
						UIManager.getLookAndFeel().provideErrorFeedback(descArea);
					}
				}
			}
		}

	}


	/**
	 * Enables or disables the back and forward actions as appropriate.
	 */
	private void setActionStates() {
		backAction.setEnabled(historyPos>0);
		forwardAction.setEnabled(historyPos>-1 && historyPos<history.size()-1);
	}


	/**
	 * Sets the description displayed in this window.  This clears the
	 * history.
	 *
	 * @param item The item whose description you want to display.
	 */
	public void setDescriptionFor(Completion item) {
		setDescriptionFor(item, false);
	}


	/**
	 * Sets the description displayed in this window.
	 *
	 * @param item The item whose description you want to display.
	 * @param addToHistory Whether to add this page to the page history
	 *        (as opposed to clearing it and starting anew).
	 */
	protected void setDescriptionFor(Completion item, boolean addToHistory) {
		String desc = item==null ? null : item.getSummary();
		if (desc==null) {
			desc = "<html><em>" + getString("NoDescAvailable") + "</em>";
		}
		descArea.setText(desc);
		descArea.setCaretPosition(0); // In case of scrolling
		if (!addToHistory) {
			// Remove everything first if this is going to be the only
			// thing in history.
			clearHistory();
		}
		addToHistory(desc);
	}


	/**
	 * {@inheritDoc} 
	 */
	public void setVisible(boolean visible) {
		if (!visible) {
			clearHistory();
		}
		super.setVisible(visible);
	}

	/**
	 * Tweaks the description text area to look good in the current Look
	 * and Feel.
	 */
	private void tweakDescArea() {

		// Jump through a few hoops to get things looking nice in Nimbus
		if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
			Color selBG = descArea.getSelectionColor();
			Color selFG = descArea.getSelectedTextColor();
			descArea.setUI(new javax.swing.plaf.basic.BasicEditorPaneUI());
			descArea.setSelectedTextColor(selFG);
			descArea.setSelectionColor(selBG);
		}

		descArea.setEditable(false); // Required for links to work!

		// Make selection visible even though we are not focusable.
		descArea.getCaret().setSelectionVisible(true);

		// Make it use "tool tip" background color.
		descArea.setBackground(getDefaultBackground());

		// Force JEditorPane to use a certain font even in HTML.
		// All standard LookAndFeels, even Nimbus (!), define Label.font.
		Font font = UIManager.getFont("Label.font");
		if (font==null) { // Try to make a sensible default
			font = new Font("SansSerif", Font.PLAIN, 12);
		}
		HTMLDocument doc = (HTMLDocument)descArea.getDocument();
		doc.getStyleSheet().addRule("body { font-family: " + font.getFamily() +
				"; font-size: " + font.getSize() + "pt; }");

	}


	/**
	 * Called by the parent completion popup window the LookAndFeel is updated.
	 */
	public void updateUI() {
		SwingUtilities.updateComponentTreeUI(this);
		tweakDescArea(); // Update the editor pane (font in HTML, etc.)
		scrollPane.setBackground(descArea.getBackground());
		scrollPane.getViewport().setBackground(descArea.getBackground());
	}


	/**
	 * Action that moves to the previous description displayed.
	 */
	class ToolBarBackAction extends AbstractAction {

		public ToolBarBackAction() {
			ClassLoader cl = getClass().getClassLoader();
			URL url = cl.getResource("org/fife/ui/autocomplete/arrow_left.png");
			try {
				Icon icon = new ImageIcon(ImageIO.read(url));
				putValue(Action.SMALL_ICON, icon);
			} catch (IOException ioe) { // Never happens
				ioe.printStackTrace();
				putValue(Action.SHORT_DESCRIPTION, "Back");
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (historyPos>0) {
				descArea.setText((String)history.get(--historyPos));
				descArea.setCaretPosition(0);
				setActionStates();
			}
		}

	}


	/**
	 * Action that moves to the previous description displayed.
	 */
	class ToolBarForwardAction extends AbstractAction {

		public ToolBarForwardAction() {
			ClassLoader cl = getClass().getClassLoader();
			URL url = cl.getResource("org/fife/ui/autocomplete/arrow_right.png");
			try {
				Icon icon = new ImageIcon(ImageIO.read(url));
				putValue(Action.SMALL_ICON, icon);
			} catch (IOException ioe) { // Never happens
				ioe.printStackTrace();
				putValue(Action.SHORT_DESCRIPTION, "Forward");
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (history!=null && historyPos<history.size()-1) {
				descArea.setText((String)history.get(++historyPos));
				descArea.setCaretPosition(0);
				setActionStates();
			}
		}

	}


}
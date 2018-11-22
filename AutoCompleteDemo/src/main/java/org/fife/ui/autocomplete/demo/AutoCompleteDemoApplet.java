/*
 * 01/13/2009
 *
 * AutoCompleteDemoApplet.java - An applet that demos the autocompletion
 * library.
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSyntaxTextArea.License.txt file for details.
 */
package org.fife.ui.autocomplete.demo;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


/**
 * An applet version of the autocompletion demo.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class AutoCompleteDemoApplet extends JApplet {


	/**
	 * Initializes this applet.
	 */
	@Override
	public void init() {
		super.init();
		SwingUtilities.invokeLater(() -> {
			String laf = UIManager.getSystemLookAndFeelClassName();
			try {
				UIManager.setLookAndFeel(laf);
			} catch (Exception e) {
				e.printStackTrace();
			}
			setRootPane(new DemoRootPane());
		});
	}


	/**
	 * Called when this applet is made visible.  Here we request that the
	 * {@link RSyntaxTextArea} is given focus.  I tried putting this code in
	 * {@link #start()} (wrapped in SwingUtilities.invokeLater()), but it
	 * didn't seem to work there.
	 *
	 * @param visible Whether this applet should be visible.
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			((DemoRootPane)getRootPane()).focusEditor();
		}
	}


}

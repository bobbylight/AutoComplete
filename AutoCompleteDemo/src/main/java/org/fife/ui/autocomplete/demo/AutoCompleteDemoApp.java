/*
 * 12/21/2008
 *
 * AutoCompleteDemoApp.java - A demo program for the auto-completion library.
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSyntaxTextArea.License.txt file for details.
 */
package org.fife.ui.autocomplete.demo;

import java.awt.*;
import javax.swing.*;

import org.fife.ui.autocomplete.*;


/**
 * A program that demonstrates use of auto-completion.  It creates a simple
 * C source editor with context sensitive auto-completion.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class AutoCompleteDemoApp extends JFrame {


	/**
	 * Constructor.
	 */
	public AutoCompleteDemoApp() {
		this(null);
	}


	/**
	 * Constructor.
	 *
	 * @param provider The completion provider for the editor to use.
	 */
	public AutoCompleteDemoApp(CompletionProvider provider) {
		setRootPane(new DemoRootPane(provider));
		setTitle("AutoCompletion Demo");
		setSize(new Dimension(500,600));//pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}


	/**
	 * Program entry point.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			String laf = UIManager.getSystemLookAndFeelClassName();
			//laf = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
			try {
				UIManager.setLookAndFeel(laf);
			} catch (Exception e) {
				e.printStackTrace();
			}
			AutoCompleteDemoApp frame = new AutoCompleteDemoApp();
			frame.getToolkit().setDynamicLayout(true);
			frame.setVisible(true);
		});

	}


}

/*
 * 01/13/2009
 *
 * DemoRootPane.java - Root pane for the demo applet and standalone application.
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSyntaxTextArea.License.txt file for details.
 */
package org.fife.ui.autocomplete.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.TextAction;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.ToolTipSupplier;


/**
 * The root pane for the demo application.  This is all code shared between
 * the applet and standalone versions of the demo.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DemoRootPane extends JRootPane {

	private RSyntaxTextArea textArea;
	private AutoCompletion ac;
	private JCheckBoxMenuItem cellRenderingItem;
	private JCheckBoxMenuItem alternateRowColorsItem;
	private JCheckBoxMenuItem showDescWindowItem;
	private JCheckBoxMenuItem paramAssistanceItem;
	private JEditorPane ep;

	private static final Color ALT_BG_COLOR = new Color(0xf0f0f0);


	/**
	 * Constructor.
	 */
	DemoRootPane() {
		this(null);
	}


	/**
	 * Constructor.
	 *
	 * @param provider The completion provider for the editor to use.
	 */
	DemoRootPane(CompletionProvider provider) {

		JPanel contentPane = new JPanel(new BorderLayout());

		ep = new JEditorPane("text/html", null);
		updateEditorPane(); // Nimbus doesn't always clean up after itself.
		contentPane.add(ep, BorderLayout.NORTH);

		textArea = new RSyntaxTextArea(25, 40);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);

		if (provider==null) {
			provider = createCompletionProvider();
		}

		// Install auto-completion onto our text area.
		ac = new AutoCompletion(provider);
		ac.setListCellRenderer(new CCellRenderer());
		ac.setShowDescWindow(true);
		ac.setParameterAssistanceEnabled(true);
		ac.install(textArea);
		contentPane.add(new RTextScrollPane(textArea, true));

		textArea.setToolTipSupplier((ToolTipSupplier)provider);
		ToolTipManager.sharedInstance().registerComponent(textArea);

		setJMenuBar(createMenuBar());

		// Get ready to go.
		setContentPane(contentPane);

		// Put the focus into the text area, not the "label" JEditorPane.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				textArea.requestFocusInWindow();
			}
		});

	}


	/**
	 * Returns the provider to use when editing code.
	 *
	 * @return The provider.
	 * @see #createCommentCompletionProvider()
	 * @see #createStringCompletionProvider()
	 */
	private CompletionProvider createCodeCompletionProvider() {

		// Add completions for the C standard library.
		DefaultCompletionProvider cp = new DefaultCompletionProvider();

		// First try loading resource (running from demo jar), then try
		// accessing file (debugging in Eclipse).
		ClassLoader cl = getClass().getClassLoader();
		InputStream in = cl.getResourceAsStream("c.xml");
		try {
			if (in!=null) {
				cp.loadFromXML(in);
				in.close();
			}
			else {
				cp.loadFromXML(new File("c.xml"));
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// Add some handy shorthand completions.
		cp.addCompletion(new ShorthandCompletion(cp, "main",
							"int main(int argc, char **argv)"));

		return cp;

	}


	/**
	 * Returns the provider to use when in a comment.
	 *
	 * @return The provider.
	 * @see #createCodeCompletionProvider()
	 * @see #createStringCompletionProvider()
	 */
	private CompletionProvider createCommentCompletionProvider() {
		DefaultCompletionProvider cp = new DefaultCompletionProvider();
		cp.addCompletion(new BasicCompletion(cp, "TODO:", "A to-do reminder"));
		cp.addCompletion(new BasicCompletion(cp, "FIXME:", "A bug that needs to be fixed"));
		return cp;
	}


	/**
	 * Creates the completion provider for a C editor.  This provider can be
	 * shared among multiple editors.
	 *
	 * @return The provider.
	 */
	private CompletionProvider createCompletionProvider() {

		// Create the provider used when typing code.
		CompletionProvider codeCP = createCodeCompletionProvider();

		// The provider used when typing a string.
		CompletionProvider stringCP = createStringCompletionProvider();

		// The provider used when typing a comment.
		CompletionProvider commentCP = createCommentCompletionProvider();

		// Create the "parent" completion provider.
		LanguageAwareCompletionProvider provider = new
								LanguageAwareCompletionProvider(codeCP);
		provider.setStringCompletionProvider(stringCP);
		provider.setCommentCompletionProvider(commentCP);

		return provider;

	}


	/**
	 * Returns the menu bar for the demo application.
	 *
	 * @return The menu bar.
	 */
	private JMenuBar createMenuBar() {

		JMenuBar mb = new JMenuBar();

		JMenu menu = new JMenu("File");
		Action newAction = new TextAction("New") {
			@Override
			public void actionPerformed(ActionEvent e) {
				AutoCompleteDemoApp app2 = new AutoCompleteDemoApp(
												ac.getCompletionProvider());
				app2.setVisible(true);
			}
		};
		JMenuItem item = new JMenuItem(newAction);
		menu.add(item);
		mb.add(menu);

		menu = new JMenu("View");
		Action renderAction = new FancyCellRenderingAction();
		cellRenderingItem = new JCheckBoxMenuItem(renderAction);
		cellRenderingItem.setSelected(true);
		menu.add(cellRenderingItem);
		Action alternateRowColorsAction = new AlternateRowColorsAction();
		alternateRowColorsItem = new JCheckBoxMenuItem(alternateRowColorsAction);
		menu.add(alternateRowColorsItem);
		Action descWindowAction = new ShowDescWindowAction();
		showDescWindowItem = new JCheckBoxMenuItem(descWindowAction);
		showDescWindowItem.setSelected(true);
		menu.add(showDescWindowItem);
		Action paramAssistanceAction = new ParameterAssistanceAction();
		paramAssistanceItem = new JCheckBoxMenuItem(paramAssistanceAction);
		paramAssistanceItem.setSelected(true);
		menu.add(paramAssistanceItem);
		mb.add(menu);

		ButtonGroup bg = new ButtonGroup();
		menu = new JMenu("LookAndFeel");
		Action lafAction = new LafAction("System", UIManager.getSystemLookAndFeelClassName());
		JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem(lafAction);
		rbmi.setSelected(true);
		menu.add(rbmi);
		bg.add(rbmi);
		lafAction = new LafAction("Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		rbmi = new JRadioButtonMenuItem(lafAction);
		menu.add(rbmi);
		bg.add(rbmi);
		lafAction = new LafAction("Ocean", "javax.swing.plaf.metal.MetalLookAndFeel");
		rbmi = new JRadioButtonMenuItem(lafAction);
		menu.add(rbmi);
		bg.add(rbmi);
		lafAction = new LafAction("Nimbus", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		rbmi = new JRadioButtonMenuItem(lafAction);
		menu.add(rbmi);
		bg.add(rbmi);
		mb.add(menu);

		return mb;

	}


	/**
	 * Returns the completion provider to use when the caret is in a string.
	 *
	 * @return The provider.
	 * @see #createCodeCompletionProvider()
	 * @see #createCommentCompletionProvider()
	 */
	private CompletionProvider createStringCompletionProvider() {
		DefaultCompletionProvider cp = new DefaultCompletionProvider();
		cp.addCompletion(new BasicCompletion(cp, "%c", "char", "Prints a character"));
		cp.addCompletion(new BasicCompletion(cp, "%i", "signed int", "Prints a signed integer"));
		cp.addCompletion(new BasicCompletion(cp, "%f", "float", "Prints a float"));
		cp.addCompletion(new BasicCompletion(cp, "%s", "string", "Prints a string"));
		cp.addCompletion(new BasicCompletion(cp, "%u", "unsigned int", "Prints an unsigned integer"));
		cp.addCompletion(new BasicCompletion(cp, "\\n", "Newline", "Prints a newline"));
		return cp;
	}


	/**
	 * Focuses the text area.
	 */
	public void focusEditor() {
		textArea.requestFocusInWindow();
	}


	/**
	 * Updates the font used in the HTML, as well as the background color, of
	 * the "label" editor pane.  The font would always have to be done (since
	 * HTMLEditorKit doesn't use the editor pane's font by default), but the
	 * background only has to be modified because Nimbus doesn't clean up the
	 * colors it installs after itself.
	 */
	private void updateEditorPane() {
		Font f = UIManager.getFont("Label.font");
		String fontTag = "<body style=\"font-family: " + f.getFamily() +
					"; font-size: " + f.getSize() + "pt; \">";
		String text = "<html>" + fontTag + "" +
			"The text area below provides simple code completion for the C " +
			"programming language as you type. Simply type <b>Ctrl+Space</b> " +
			"at any time to see a list of completion choices (function names, "+
			"for example). If there is only one possible completion, it will " +
			"be automatically inserted.<p>" +
			"Also, completions are context-sensitive.  If you type Ctrl+Space" +
			"in a comment or in the middle of a string, you will get " +
			"different completion choices than if you are in code.";
		ep.setText(text);
		ep.setBorder(BorderFactory.createEmptyBorder(5,5,10,5));
		ep.setEditable(false);
		ep.setBackground(UIManager.getColor("Panel.background"));
	}


	/**
	 * Toggles whether row backgrounds use alternate colors.
	 */
	private class AlternateRowColorsAction extends AbstractAction {

		AlternateRowColorsAction() {
			putValue(NAME, "Alternate Row Colors in completion list");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean alternate = alternateRowColorsItem.isSelected();
			CompletionCellRenderer.setAlternateBackground(
					alternate ? ALT_BG_COLOR: null);
		}

	}


	/**
	 * Toggles whether the completion window uses "fancy" rendering.
	 */
	private class FancyCellRenderingAction extends AbstractAction {

		FancyCellRenderingAction() {
			putValue(NAME, "Fancy Cell Rendering");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean fancy = cellRenderingItem.isSelected();
			ac.setListCellRenderer(fancy ? new CCellRenderer() : null);
		}

	}


	/**
	 * Sets a new look and feel.
	 */
	private class LafAction extends AbstractAction {

		private String laf;

		LafAction(String name, String laf) {
			putValue(NAME, name);
			this.laf = laf;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				UIManager.setLookAndFeel(laf);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(DemoRootPane.this,
					"Error setting LookAndFeel", "Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			Component parent = SwingUtilities.getRoot(DemoRootPane.this);
			SwingUtilities.updateComponentTreeUI(parent);
			updateEditorPane();
		}

	}


	/**
	 * Toggles whether parameter assistance is enabled.
	 */
	private class ParameterAssistanceAction extends AbstractAction {

		ParameterAssistanceAction() {
			putValue(NAME, "Function Parameter Assistance");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean enabled = paramAssistanceItem.isSelected();
			ac.setParameterAssistanceEnabled(enabled);
		}

	}


	/**
	 * Toggles whether the description window is visible.
	 */
	private class ShowDescWindowAction extends AbstractAction {

		ShowDescWindowAction() {
			putValue(NAME, "Show Description Window");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean show = showDescWindowItem.isSelected();
			ac.setShowDescWindow(show);
		}

	}


}

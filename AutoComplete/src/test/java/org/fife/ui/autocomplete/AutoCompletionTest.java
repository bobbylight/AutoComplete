/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class AutoCompletionTest {

	private JFrame frame;


	@BeforeEach
	void setUp() {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
	}


	@AfterEach
	void tearDown() {
		if (frame != null) {
			frame.dispose();
			frame = null;
		}
	}


	/**
	 * Regression test for https://github.com/bobbylight/AutoComplete/issues/84 -
	 * merely hiding the description window when it's toggled off could leave
	 * a blank "ghost" window on screen on some Linux/X11 window managers.
	 * The fix disposes of the description window's native peer instead of
	 * just hiding it, so it must be lazily recreated the next time it's
	 * shown.
	 */
	@Test
	void setShowDescWindow_false_disposesExistingDescWindow() {

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo", "foo's summary"));
		provider.addCompletion(new BasicCompletion(provider, "foobar", "foobar's summary"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setShowDescWindow(true);
		ac.setAutoCompleteEnabled(true);

		JTextArea textArea = new JTextArea();
		ac.install(textArea);

		frame = new JFrame();
		frame.add(textArea);
		frame.pack();
		frame.setVisible(true);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();

		AutoCompletePopupWindow popupWindow = ac.getPopupWindow();
		Assertions.assertNotNull(popupWindow,
				"Popup window should have been created by doCompletion()");
		AutoCompleteDescWindow descWindow = popupWindow.getDescWindow();
		Assertions.assertNotNull(descWindow,
				"Description window should have been created since showDescWindow was true");
		Assertions.assertTrue(descWindow.isDisplayable(),
				"Description window's native peer should exist while showing");

		ac.setShowDescWindow(false);

		Assertions.assertNull(popupWindow.getDescWindow(),
				"Description window should be disposed and discarded, not just hidden");
		Assertions.assertFalse(descWindow.isDisplayable(),
				"Description window's native peer should be destroyed by dispose()");

	}


}

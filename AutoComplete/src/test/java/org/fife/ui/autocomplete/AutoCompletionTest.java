/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
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
	 * Regression test for <a href="https://github.com/bobbylight/AutoComplete/issues/84">Issue #84</a> -
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


	/**
	 * Regression test for <a href="https://github.com/bobbylight/AutoComplete/issues/77">Issue #77</a> -
	 * {@code isAutoActivateOkay()} used to look at {@code JTextComponent#getCaretPosition()}
	 * to find the just-typed character, but the caret is not guaranteed to have been
	 * updated yet when {@code insertUpdate()} fires; it depends on the order in which
	 * document listeners were registered.  This test simulates the "default" ordering,
	 * where the caret has *not yet* been moved to reflect the inserted character by the
	 * time {@code AutoCompletion}'s listener runs (this is the ordering that always
	 * worked, even before the fix, since the stale caret position happened to coincide
	 * with the newly-inserted character's offset).
	 */
	@Test
	void insertUpdate_autoActivateOkay_correctWhenCaretListenerFiresAfterAutoCompletion() {

		RecordingCompletionProvider recordingProvider = new RecordingCompletionProvider();
		recordingProvider.setAutoActivationRules(false, ".");

		AutoCompletion ac = new AutoCompletion(recordingProvider);
		ac.setAutoActivationEnabled(true);

		// Note - the default Caret will be installed here, and thus before the AutoCompletion.
		// Events are fired last-first so the AC will receive the {@code insertEvent} before
		// the caret's offset is updated. This is the OOTB default behavior.
		JTextArea textArea = new JTextArea();

		ac.install(textArea);

		textArea.setCaretPosition(0);
		textArea.replaceSelection(".");

		Assertions.assertEquals(0, recordingProvider.lastOffset,
				"isAutoActivateOkay() should receive the offset of the inserted character");
		Assertions.assertEquals(0, recordingProvider.caretPositionAtCallTime,
				"Caret should still be stale (unmoved) in this listener ordering");
		Assertions.assertTrue(recordingProvider.lastResult,
				"Auto-activation should trigger for '.' regardless of caret staleness");

	}


	/**
	 * Regression test for <a href="https://github.com/bobbylight/AutoComplete/issues/77">Issue #77</a> -
	 * this simulates the "other" ordering reported in the issue (e.g. after
	 * {@code TextEditorPane#load()} swaps in a new {@code Document}), where the caret
	 * *has already* been moved to reflect the inserted character by the time
	 * {@code AutoCompletion}'s listener runs.  Before the fix, {@code isAutoActivateOkay()}
	 * would read {@code doc.getText(tc.getCaretPosition(), 1)}, which in this ordering
	 * points one character past the just-typed character (i.e. off the end of the
	 * document in this test), so auto-activation would incorrectly fail to trigger.
	 * <p>
	 * Example real-world reproduction case: Install an {@code AutoCompletion} on a text area, then
	 * call {@code setCaret(new DefaultCaret())}. This reinstalls the Caret's listeners and thus flips
	 * the notification ordering between it and the AutoCompletion.
	 * <p>
	 * Note: This is only reproducible when run on the EDT (which a well-behaved app should always do!).
	 */
	@Test
	void insertUpdate_autoActivateOkay_correctWhenCaretListenerFiresBeforeAutoCompletion() {

		RecordingCompletionProvider recordingProvider = new RecordingCompletionProvider();
		recordingProvider.setAutoActivationRules(false, ".");

		AutoCompletion ac = new AutoCompletion(recordingProvider);
		ac.setAutoActivationEnabled(true);

		JTextArea textArea = new JTextArea();
		ac.install(textArea);

		// Register a new caret so the installed caret's listener order will be *after* the AutoCompletion's.
		// This in turn makes the Caret notified before the AC (listeners are notified in reverse order),
		// allowing us to test that our code is agnostic to listener ordering.
		textArea.setCaret(new DefaultCaret());

		textArea.setCaretPosition(0);
		textArea.replaceSelection(".");

		Assertions.assertEquals(0, recordingProvider.lastOffset,
				"isAutoActivateOkay() should receive the offset of the inserted character");
		Assertions.assertEquals(1, recordingProvider.caretPositionAtCallTime,
				"Caret should already be advanced past the inserted character in this ordering");
		Assertions.assertTrue(recordingProvider.lastResult,
				"Auto-activation should trigger for '.' regardless of caret staleness");

	}


	/**
	 * A completion provider that records the arguments and return value of the most
	 * recent call to {@code isAutoActivateOkay()}.
	 */
	private static final class RecordingCompletionProvider extends DefaultCompletionProvider {

		int lastOffset = -1;
		int caretPositionAtCallTime = -1;
		boolean lastResult;

		@Override
		public boolean isAutoActivateOkay(JTextComponent tc, int offs) {
			lastOffset = offs;
			caretPositionAtCallTime = tc.getCaretPosition();
			lastResult = super.isAutoActivateOkay(tc, offs);
			return lastResult;
		}

	}


}

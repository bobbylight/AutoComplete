/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
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
		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
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

		ac.setDescWindowVisibility(DescWindowVisibility.NEVER);

		Assertions.assertNull(popupWindow.getDescWindow(),
				"Description window should be disposed and discarded, not just hidden");
		Assertions.assertFalse(descWindow.isDisplayable(),
				"Description window's native peer should be destroyed by dispose()");

	}


	@Test
	void getShowDescWindow_defaultsToNever() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		Assertions.assertEquals(DescWindowVisibility.NEVER, ac.getDescWindowVisibility());
	}


	/**
	 * Regression test for a bug where a disposed description window could be
	 * silently resurrected by the JDK. {@code java.awt.Window#hide()} has an
	 * internal cascade: any owned window that's still visible when its owner
	 * is hidden gets hidden too and flagged (via a package-private
	 * {@code showWithParent} field) to be automatically re-shown - by calling
	 * {@code show()} directly, bypassing all of our code - the next time the
	 * owner is shown again, even if that owned window was disposed of in the
	 * meantime. If the description window is still visible at the moment the
	 * choices popup is hidden (e.g. the text component loses focus), that
	 * flag gets set; disposing the description window afterward (by
	 * switching to {@code NEVER}) doesn't clear it, so the next time the
	 * choices popup reopens, the JDK recreates the disposed window's native
	 * peer and shows it again with its last (now stale) content - even
	 * though {@code AutoCompletePopupWindow}'s own {@code descWindow} field
	 * is {@code null}. The fix hides the description window *before* hiding
	 * the choices popup, so it's already invisible by the time the JDK's
	 * cascade runs and never sets that flag.
	 */
	@Test
	void showDescWindow_disposedWhileHidden_doesNotReappearOnNextShow() {

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo", "foo's summary"));
		provider.addCompletion(new BasicCompletion(provider, "foobar", "foobar's summary"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
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
		AutoCompleteDescWindow descWindow = popupWindow.getDescWindow();
		Assertions.assertNotNull(descWindow);
		Assertions.assertTrue(descWindow.isVisible());

		// Hide the choices popup while the desc window is still visible, as
		// happens e.g. when the text component loses focus. This is what
		// causes the JDK to internally flag the desc window to be
		// auto-restored the next time the popup is shown again.
		popupWindow.setVisible(false);

		// Now dispose the desc window entirely, as setDescWindowVisibility()
		// does when switching away from ALWAYS.
		ac.setDescWindowVisibility(DescWindowVisibility.NEVER);
		Assertions.assertNull(popupWindow.getDescWindow());
		Assertions.assertFalse(descWindow.isDisplayable());

		// Re-trigger completion; the choices popup reopens, but the disposed
		// desc window must not be silently resurrected by the JDK.
		ac.doCompletion();

		Assertions.assertNull(popupWindow.getDescWindow(),
				"No new description window should have been created (NEVER)");
		Assertions.assertFalse(descWindow.isVisible(),
				"The disposed description window must not be resurrected by the JDK");
		Assertions.assertFalse(descWindow.isDisplayable(),
				"The disposed description window's peer must not be silently recreated");

	}


	@Test
	void showDescWindow_never_descWindowNeverCreated() {

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo", "foo's summary"));
		provider.addCompletion(new BasicCompletion(provider, "foobar", "foobar's summary"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setDescWindowVisibility(DescWindowVisibility.NEVER);
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
		Assertions.assertNotNull(popupWindow, "Choices popup window should still be shown");
		Assertions.assertNull(popupWindow.getDescWindow(),
				"Description window should never be created when visibility is NEVER");

	}


	@Test
	void showDescWindow_onDemand_toggleKeyShowsAndHidesDescWindow() {

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo", "foo's summary"));
		provider.addCompletion(new BasicCompletion(provider, "foobar", "foobar's summary"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setDescWindowVisibility(DescWindowVisibility.ON_DEMAND);
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
		Assertions.assertNotNull(popupWindow);
		Assertions.assertNull(popupWindow.getDescWindow(),
				"Description window should not be created until toggled on, in ON_DEMAND mode");

		fireKeyAction(textArea, ac.getDescWindowToggleKey());
		AutoCompleteDescWindow descWindow = popupWindow.getDescWindow();
		Assertions.assertNotNull(descWindow,
				"Description window should be created the first time it's toggled on");
		Assertions.assertTrue(descWindow.isVisible(),
				"Description window should be visible after toggling on");

		fireKeyAction(textArea, ac.getDescWindowToggleKey());
		Assertions.assertFalse(descWindow.isVisible(),
				"Description window should be hidden after toggling back off");

	}


	@Test
	void showDescWindow_onDemand_toggleKeyIgnoredWhenNotOnDemand() {

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo", "foo's summary"));
		provider.addCompletion(new BasicCompletion(provider, "foobar", "foobar's summary"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
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
		AutoCompleteDescWindow descWindow = popupWindow.getDescWindow();
		Assertions.assertNotNull(descWindow, "Description window should already be shown (ALWAYS)");

		fireKeyAction(textArea, ac.getDescWindowToggleKey());
		Assertions.assertTrue(descWindow.isVisible(),
				"Toggle key should have no effect when visibility isn't ON_DEMAND");

	}


	@Test
	void setDescWindowToggleKey_customKeystrokeIsInstalledAndUsable() {

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo", "foo's summary"));
		provider.addCompletion(new BasicCompletion(provider, "foobar", "foobar's summary"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setDescWindowVisibility(DescWindowVisibility.ON_DEMAND);
		ac.setAutoCompleteEnabled(true);

		KeyStroke customKey = KeyStroke.getKeyStroke("ctrl alt D");
		ac.setDescWindowToggleKey(customKey);

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
		Assertions.assertNull(popupWindow.getDescWindow());

		fireKeyAction(textArea, customKey);
		Assertions.assertNotNull(popupWindow.getDescWindow(),
				"Description window should toggle on via the custom keystroke");

	}


	@Test
	void setDescWindowToggleKey_null_removesInstalledKeystroke() {

		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		JTextArea textArea = new JTextArea();
		ac.install(textArea);

		KeyStroke defaultKey = AutoCompletion.getDefaultDescWindowToggleKey();
		InputMap im = textArea.getInputMap();
		Assertions.assertNotNull(im.get(defaultKey),
				"Default toggle key should be installed on install()");

		ac.setDescWindowToggleKey(null);
		Assertions.assertNull(ac.getDescWindowToggleKey());

	}


	/**
	 * Fires the action bound to {@code ks} in {@code textArea}'s input/action maps,
	 * simulating the user pressing that keystroke.
	 */
	private static void fireKeyAction(JTextArea textArea, KeyStroke ks) {
		InputMap im = textArea.getInputMap();
		ActionMap am = textArea.getActionMap();
		Object key = im.get(ks);
		Assertions.assertNotNull(key, "No action bound to keystroke " + ks);
		Action action = am.get(key);
		Assertions.assertNotNull(action, "No action found for key " + key);
		action.actionPerformed(new ActionEvent(textArea, ActionEvent.ACTION_PERFORMED, ""));
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

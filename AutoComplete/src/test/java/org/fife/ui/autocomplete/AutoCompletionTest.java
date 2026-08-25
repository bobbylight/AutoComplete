/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
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
		AbstractDescWindow descWindow = popupWindow.getDescWindow();
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
		AbstractDescWindow descWindow = popupWindow.getDescWindow();
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
		AbstractDescWindow descWindow = popupWindow.getDescWindow();
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
		AbstractDescWindow descWindow = popupWindow.getDescWindow();
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


	/**
	 * Realizes {@code textArea} in a visible, packed {@code JFrame}, which is required for any
	 * code path that calls {@code JTextComponent#modelToView2D(int)} (e.g. showing the popup or
	 * starting parameterized completion assistance).  The frame is torn down in {@link #tearDown()}.
	 */
	private void realize(JTextArea textArea) {
		frame = new JFrame();
		frame.add(textArea);
		frame.pack();
		frame.setVisible(true);
	}


	@Test
	void constructor_setsExpectedDefaults() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		Assertions.assertTrue(ac.isAutoCompleteEnabled());
		Assertions.assertTrue(ac.getAutoCompleteSingleChoices());
		Assertions.assertFalse(ac.isAutoActivationEnabled());
		Assertions.assertFalse(ac.isParameterAssistanceEnabled());
		Assertions.assertTrue(ac.isHideOnCompletionProviderChange());
		Assertions.assertTrue(ac.isHideOnNoText());
		Assertions.assertEquals(300, ac.getParameterDescriptionTruncateThreshold());
		Assertions.assertEquals(AutoCompletion.getDefaultTriggerKey(), ac.getTriggerKey());
		Assertions.assertEquals(AutoCompletion.getDefaultDescWindowToggleKey(), ac.getDescWindowToggleKey());
	}


	@Test
	void getDefaultTriggerKey_isCtrlSpace() {
		KeyStroke expected = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE,
			java.awt.event.InputEvent.CTRL_DOWN_MASK);
		Assertions.assertEquals(expected, AutoCompletion.getDefaultTriggerKey());
	}


	@Test
	void getDefaultDescWindowToggleKey_isCtrlShiftSpace() {
		KeyStroke expected = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE,
			java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
		Assertions.assertEquals(expected, AutoCompletion.getDefaultDescWindowToggleKey());
	}


	@Test
	void getStyleContext_returnsSameSingletonAcrossInstances() {
		Assertions.assertSame(AutoCompletion.getStyleContext(), AutoCompletion.getStyleContext());
	}


	@Test
	void getSetAutoActivationDelay_roundTrips() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		ac.setAutoActivationDelay(500);
		Assertions.assertEquals(500, ac.getAutoActivationDelay());
	}


	@Test
	void setAutoActivationDelay_negative_clampsToZero() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		ac.setAutoActivationDelay(-100);
		Assertions.assertEquals(0, ac.getAutoActivationDelay());
	}


	@Test
	void getSetExternalURLHandler_roundTrips() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		Assertions.assertNull(ac.getExternalURLHandler());
		ExternalURLHandler handler = (e, c, callback) -> { };
		ac.setExternalURLHandler(handler);
		Assertions.assertSame(handler, ac.getExternalURLHandler());
	}


	@Test
	void getSetLinkRedirector_staticRoundTrips() {
		Assertions.assertNull(AutoCompletion.getLinkRedirector());
		try {
			LinkRedirector redirector = url -> url;
			AutoCompletion.setLinkRedirector(redirector);
			Assertions.assertSame(redirector, AutoCompletion.getLinkRedirector());
		} finally {
			AutoCompletion.setLinkRedirector(null);
		}
	}


	@Test
	void getSetParamChoicesRenderer_roundTrips() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		Assertions.assertNull(ac.getParamChoicesRenderer());
		javax.swing.DefaultListCellRenderer renderer = new javax.swing.DefaultListCellRenderer();
		ac.setParamChoicesRenderer(renderer);
		Assertions.assertSame(renderer, ac.getParamChoicesRenderer());
	}


	@Test
	void getSetParameterDescriptionTruncateThreshold_roundTrips() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		ac.setParameterDescriptionTruncateThreshold(42);
		Assertions.assertEquals(42, ac.getParameterDescriptionTruncateThreshold());
	}


	@Test
	void isSetParameterAssistanceEnabled_roundTrips() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		ac.setParameterAssistanceEnabled(true);
		Assertions.assertTrue(ac.isParameterAssistanceEnabled());
		ac.setParameterAssistanceEnabled(false);
		Assertions.assertFalse(ac.isParameterAssistanceEnabled());
	}


	@Test
	void isSetHideOnCompletionProviderChangeAndHideOnNoText_roundTrip() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		ac.setHideOnCompletionProviderChange(false);
		Assertions.assertFalse(ac.isHideOnCompletionProviderChange());
		ac.setHideOnNoText(false);
		Assertions.assertFalse(ac.isHideOnNoText());
	}


	@Test
	void getTextComponent_lifecycleAcrossInstallAndUninstall() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		Assertions.assertNull(ac.getTextComponent());

		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		Assertions.assertSame(textArea, ac.getTextComponent());

		ac.uninstall();
		Assertions.assertNull(ac.getTextComponent());
	}


	@Test
	void getTextComponentOrientation_nullBeforeInstall_matchesAfterInstall() throws ReflectiveOperationException {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		java.lang.reflect.Method m = AutoCompletion.class.getDeclaredMethod("getTextComponentOrientation");
		m.setAccessible(true);
		Assertions.assertNull(m.invoke(ac));

		JTextArea textArea = new JTextArea();
		textArea.applyComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
		ac.install(textArea);
		Assertions.assertEquals(java.awt.ComponentOrientation.RIGHT_TO_LEFT, m.invoke(ac));
	}


	@Test
	void getLineOfCaret_returnsZeroBasedLineIndex() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		JTextArea textArea = new JTextArea("line0\nline1\nline2");
		ac.install(textArea);

		textArea.setCaretPosition(0);
		Assertions.assertEquals(0, ac.refreshPopupWindow());

		textArea.setCaretPosition(textArea.getText().indexOf("line2"));
		Assertions.assertEquals(2, ac.refreshPopupWindow());
	}


	@Test
	void setCompletionProvider_null_throwsException() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		Assertions.assertThrows(NullPointerException.class, () -> ac.setCompletionProvider(null));
	}


	@Test
	void setTriggerKey_null_throwsException() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		Assertions.assertThrows(NullPointerException.class, () -> ac.setTriggerKey(null));
	}


	@Test
	void setDescWindowVisibility_null_throwsException() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		Assertions.assertThrows(NullPointerException.class, () -> ac.setDescWindowVisibility(null));
	}


	@Test
	void setCompletionProvider_defaultBehavior_hidesVisiblePopup() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();
		Assertions.assertTrue(ac.isPopupVisible());

		ac.setCompletionProvider(new DefaultCompletionProvider());
		Assertions.assertFalse(ac.isPopupVisible());
	}


	@Test
	void setCompletionProvider_hideOnCompletionProviderChangeFalse_leavesPopupVisible() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setHideOnCompletionProviderChange(false);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();
		Assertions.assertTrue(ac.isPopupVisible());

		ac.setCompletionProvider(new DefaultCompletionProvider());
		Assertions.assertTrue(ac.isPopupVisible());
	}


	@Test
	void setAutoCompleteEnabled_false_hidesVisiblePopup() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();
		Assertions.assertTrue(ac.isPopupVisible());

		ac.setAutoCompleteEnabled(false);
		Assertions.assertFalse(ac.isPopupVisible());
	}


	@Test
	void setListCellRenderer_appliesToExistingPopupAndHidesIt() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();
		Assertions.assertTrue(ac.isPopupVisible());

		CompletionCellRenderer renderer = new CompletionCellRenderer();
		ac.setListCellRenderer(renderer);

		Assertions.assertSame(renderer, ac.getPopupWindow().getListCellRenderer());
		Assertions.assertFalse(ac.isPopupVisible());
	}


	@Test
	void setChoicesWindowSize_resizesExistingPopup() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();

		ac.setChoicesWindowSize(500, 300);
		Assertions.assertEquals(new Dimension(500, 300), ac.getPopupWindow().getSize());
	}


	@Test
	void setDescriptionWindowColor_appliesToExistingDescWindow() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo", "foo's summary"));
		provider.addCompletion(new BasicCompletion(provider, "foobar", "foobar's summary"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();

		Color color = new Color(1, 2, 3);
		ac.setDescriptionWindowColor(color);

		Assertions.assertEquals(color, ac.getDescWindowColor());
		Assertions.assertEquals(color, ac.getPopupWindow().getDescWindow().getBackground());
	}


	@Test
	void refreshPopupWindow_multipleMatches_showsPopup() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();

		Assertions.assertTrue(ac.isPopupVisible());
	}


	@Test
	void refreshPopupWindow_singleMatch_autoCompleteSingleChoicesDisabled_showsPopup() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setAutoCompleteSingleChoices(false);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();

		Assertions.assertTrue(ac.isPopupVisible());
	}


	@Test
	void refreshPopupWindow_singleMatch_autoCompleteSingleChoicesEnabled_doesNotShowPopup() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();

		// A single choice with autoCompleteSingleChoices enabled (the default) is inserted
		// asynchronously via SwingUtilities.invokeLater() rather than shown in a popup; see
		// the class-level note on hard-to-test sections for why we don't assert the insertion itself.
		Assertions.assertFalse(ac.isPopupVisible());
	}


	@Test
	void refreshPopupWindow_noMatches_hidesVisiblePopup() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();
		Assertions.assertTrue(ac.isPopupVisible());

		textArea.setText("zzz");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();

		Assertions.assertFalse(ac.isPopupVisible());
	}


	@Test
	void refreshPopupWindow_emptyTextWithHideOnNoText_hidesVisiblePopup() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();
		Assertions.assertTrue(ac.isPopupVisible());

		// Moving the caret back to the start of the (single-line) word leaves the popup's own
		// CaretListener on the same line, so it re-triggers a completion refresh on its own -
		// with the "already entered" text now empty, hideOnNoText kicks in and hides the popup
		// without needing an explicit doCompletion() call (which would otherwise treat "" as
		// "show all completions" and reopen the popup).
		textArea.setCaretPosition(0);

		Assertions.assertFalse(ac.isPopupVisible());
	}


	@Test
	void hideChildWindows_falseWhenNothingVisible_trueWhenPopupVisible() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		Assertions.assertFalse(ac.hideChildWindows());

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();
		Assertions.assertTrue(ac.isPopupVisible());

		Assertions.assertTrue(ac.hideChildWindows());
		Assertions.assertFalse(ac.isPopupVisible());
	}


	@Test
	void insertCompletion_nonParameterized_replacesAlreadyEnteredText() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foobar");
		provider.addCompletion(completion);

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.insertCompletion(completion);

		Assertions.assertEquals("foobar", textArea.getText());
		Assertions.assertFalse(ac.hideChildWindows());
	}


	@Test
	void insertCompletion_parameterizedWithParams_parameterAssistanceEnabled_startsAssistance() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "void");
		List<Parameter> params = new ArrayList<>();
		params.add(new Parameter("int", "a"));
		completion.setParams(params);
		provider.addCompletion(completion);

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setParameterAssistanceEnabled(true);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.insertCompletion(completion);

		Assertions.assertTrue(ac.hideChildWindows(),
			"Parameter completion assistance should have activated for a completion with params");
	}


	@Test
	void insertCompletion_parameterizedWithParams_parameterAssistanceDisabled_doesNotStartAssistance() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "void");
		List<Parameter> params = new ArrayList<>();
		params.add(new Parameter("int", "a"));
		completion.setParams(params);
		provider.addCompletion(completion);

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.insertCompletion(completion);

		Assertions.assertFalse(ac.hideChildWindows());
	}


	// NOTE: In real usage, AutoCompletionEvents are fired from PopupWindowListener's
	// componentShown()/componentHidden() callbacks, which the AWT toolkit dispatches
	// asynchronously (even though the underlying Window's visibility state changes
	// synchronously). Reliably observing those in a test requires pumping the shared,
	// JVM-wide AWT event queue, which also picks up unrelated events (e.g. window
	// focus/activation changes from realizing a JFrame) that can trigger other listeners
	// (like hiding the popup on focus loss) with results that vary by platform and window
	// manager. See the class-level note on hard-to-test sections. These tests instead call
	// the protected fireAutoCompletionEvent() hook directly to deterministically test
	// listener registration/removal/fan-out - the part of this feature that isn't tied to
	// AWT event timing.
	@Test
	void addAutoCompletionListener_receivesFiredEvents() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		List<AutoCompletionEvent.Type> received = new ArrayList<>();
		ac.addAutoCompletionListener(e -> received.add(e.getEventType()));

		ac.fireAutoCompletionEvent(AutoCompletionEvent.Type.POPUP_SHOWN);
		ac.fireAutoCompletionEvent(AutoCompletionEvent.Type.POPUP_HIDDEN);

		Assertions.assertEquals(
			List.of(AutoCompletionEvent.Type.POPUP_SHOWN, AutoCompletionEvent.Type.POPUP_HIDDEN),
			received);
	}


	@Test
	void addAutoCompletionListener_eventSourceIsTheAutoCompletionInstance() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		List<AutoCompletionEvent> received = new ArrayList<>();
		ac.addAutoCompletionListener(received::add);

		ac.fireAutoCompletionEvent(AutoCompletionEvent.Type.POPUP_SHOWN);

		Assertions.assertEquals(1, received.size());
		Assertions.assertSame(ac, received.get(0).getSource());
	}


	@Test
	void addAutoCompletionListener_multipleListeners_allNotified() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		List<String> calls = new ArrayList<>();
		ac.addAutoCompletionListener(e -> calls.add("first"));
		ac.addAutoCompletionListener(e -> calls.add("second"));

		ac.fireAutoCompletionEvent(AutoCompletionEvent.Type.POPUP_SHOWN);

		Assertions.assertEquals(2, calls.size());
	}


	@Test
	void removeAutoCompletionListener_stopsNotifications() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		List<AutoCompletionEvent.Type> received = new ArrayList<>();
		AutoCompletionListener listener = e -> received.add(e.getEventType());
		ac.addAutoCompletionListener(listener);
		ac.removeAutoCompletionListener(listener);

		ac.fireAutoCompletionEvent(AutoCompletionEvent.Type.POPUP_SHOWN);

		Assertions.assertTrue(received.isEmpty());
	}


	@Test
	void install_bindsTriggerKeyThatShowsPopup() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		fireKeyAction(textArea, ac.getTriggerKey());

		Assertions.assertTrue(ac.isPopupVisible());
	}


	@Test
	void install_secondComponent_uninstallsFromFirst() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		JTextArea textArea1 = new JTextArea();
		KeyStroke trigger = ac.getTriggerKey();
		Object originalBinding = textArea1.getInputMap().get(trigger);

		ac.install(textArea1);
		Assertions.assertSame(textArea1, ac.getTextComponent());

		JTextArea textArea2 = new JTextArea();
		ac.install(textArea2);

		Assertions.assertSame(textArea2, ac.getTextComponent());
		Assertions.assertEquals(originalBinding, textArea1.getInputMap().get(trigger),
			"Old text component's trigger key binding should be restored when moving to a new component");
	}


	@Test
	void uninstall_whenNotInstalled_doesNothing() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		Assertions.assertDoesNotThrow(ac::uninstall);
		Assertions.assertNull(ac.getTextComponent());
	}


	@Test
	void setTriggerKey_whileInstalled_movesBindingToNewKey() {
		AutoCompletion ac = new AutoCompletion(new DefaultCompletionProvider());
		JTextArea textArea = new JTextArea();
		KeyStroke originalTrigger = ac.getTriggerKey();
		KeyStroke newTrigger = KeyStroke.getKeyStroke("ctrl alt X");

		// Capture pre-install state for both keys so we can confirm install()/setTriggerKey()
		// correctly restore whatever was there before, rather than assuming it was null.
		Object originalBindingAtOldKeyPreInstall = textArea.getInputMap().get(originalTrigger);
		Object originalBindingAtNewKeyPreInstall = textArea.getInputMap().get(newTrigger);

		ac.install(textArea);
		Assertions.assertNotEquals(originalBindingAtOldKeyPreInstall, textArea.getInputMap().get(originalTrigger),
			"install() should rebind the trigger key");

		ac.setTriggerKey(newTrigger);

		Assertions.assertEquals(newTrigger, ac.getTriggerKey());
		Assertions.assertNotEquals(originalBindingAtNewKeyPreInstall, textArea.getInputMap().get(newTrigger),
			"New trigger key should now be bound to the AutoComplete action");
		Assertions.assertEquals(originalBindingAtOldKeyPreInstall, textArea.getInputMap().get(originalTrigger),
			"Old trigger key's original (pre-install) binding should be restored immediately"
				+ " when the trigger key changes");
	}


	@Test
	void parameterizedCompletionStartAction_popupNotVisible_insertsCharLiterally() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');

		AutoCompletion ac = new AutoCompletion(provider);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);

		fireKeyAction(textArea, KeyStroke.getKeyStroke('('));

		Assertions.assertEquals("(", textArea.getText());
	}


	@Test
	void parameterizedCompletionStartAction_popupVisibleWithParamCompletionSelected_startsAssistance() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');

		FunctionCompletion fc = new FunctionCompletion(provider, "foo", "void");
		List<Parameter> params = new ArrayList<>();
		params.add(new Parameter("int", "a"));
		fc.setParams(params);
		provider.addCompletion(fc);
		provider.addCompletion(new BasicCompletion(provider, "fooZ"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setParameterAssistanceEnabled(true);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();
		Assertions.assertTrue(ac.isPopupVisible());
		Assertions.assertSame(fc, ac.getPopupWindow().getSelection(),
			"Sanity check: the function completion should be selected first");

		fireKeyAction(textArea, KeyStroke.getKeyStroke('('));

		Assertions.assertFalse(ac.isPopupVisible(), "Choices popup should be hidden once assistance starts");
		Assertions.assertTrue(ac.hideChildWindows(),
			"Parameter completion assistance should have activated");
	}


	@Test
	void autoCompleteAction_disabled_doesNotShowPopup() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));
		provider.addCompletion(new BasicCompletion(provider, "foobar"));

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setAutoCompleteEnabled(false);
		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		fireKeyAction(textArea, ac.getTriggerKey());

		Assertions.assertFalse(ac.isPopupVisible());
		Assertions.assertNull(ac.getPopupWindow());
	}


}

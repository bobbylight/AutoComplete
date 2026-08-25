/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class AutoCompleteDescWindowTest {

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


	private static Object getField(Object target, String name) throws ReflectiveOperationException {
		Field field = AutoCompleteDescWindow.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}


	private static JEditorPane getDescArea(AutoCompleteDescWindow window) throws ReflectiveOperationException {
		return (JEditorPane) getField(window, "descArea");
	}


	/**
	 * {@code setDescriptionFor()} doesn't update the displayed description synchronously -
	 * it queues the completion on a 120ms {@code Timer} (to avoid bogging down fast scrolling
	 * through completions with slow-to-compute summaries). Rather than sleeping past that
	 * delay and pumping the AWT event queue, this fires the timer's action directly, which is
	 * deterministic and avoids any real-time flakiness.
	 */
	private static void fireDescriptionTimer(AutoCompleteDescWindow window) throws ReflectiveOperationException {
		Timer timer = (Timer) getField(window, "timer");
		timer.stop();
		ActionListener action = (ActionListener) getField(window, "timerAction");
		action.actionPerformed(null);
	}


	private static AutoCompletion newInstalledAutoCompletion(CompletionProvider provider) {
		AutoCompletion ac = new AutoCompletion(provider);
		ac.install(new JTextArea());
		return ac;
	}


	@Test
	void constructor_initialHistoryStateHasNoBackOrForward() {
		AutoCompletion ac = newInstalledAutoCompletion(new DefaultCompletionProvider());
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);

		Assertions.assertFalse(window.copy(), "Nothing should be visible/selected yet");
	}


	@Test
	void copy_notVisible_returnsFalse() throws ReflectiveOperationException {
		AutoCompletion ac = newInstalledAutoCompletion(new DefaultCompletionProvider());
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);
		getDescArea(window).selectAll();

		Assertions.assertFalse(window.copy());
	}


	@Test
	void copy_visibleNoSelection_returnsFalse() {
		AutoCompletion ac = newInstalledAutoCompletion(new DefaultCompletionProvider());
		frame = new JFrame();
		frame.setVisible(true);
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(frame, ac);

		window.setVisible(true);
		Assertions.assertFalse(window.copy());
	}


	@Test
	void copy_visibleWithSelection_returnsTrue() throws ReflectiveOperationException {
		AutoCompletion ac = newInstalledAutoCompletion(new DefaultCompletionProvider());
		frame = new JFrame();
		frame.setVisible(true);
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(frame, ac);

		window.setVisible(true);
		getDescArea(window).setText("some description text");
		getDescArea(window).selectAll();

		Assertions.assertTrue(window.copy());
	}


	@Test
	void setDescriptionFor_noSummaryAvailable_showsFallbackMessage() throws ReflectiveOperationException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		AutoCompletion ac = newInstalledAutoCompletion(provider);
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);

		BasicCompletion completion = new BasicCompletion(provider, "foo"); // no summary set
		window.setDescriptionFor(completion);
		fireDescriptionTimer(window);

		Assertions.assertTrue(getDescArea(window).getText().contains("<em>"));
	}


	@Test
	void setDescriptionFor_withSummary_showsSummaryText() throws ReflectiveOperationException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		AutoCompletion ac = newInstalledAutoCompletion(provider);
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);

		BasicCompletion completion = new BasicCompletion(provider, "foo", "short", "the full summary");
		window.setDescriptionFor(completion);
		fireDescriptionTimer(window);

		Assertions.assertTrue(getDescArea(window).getText().contains("the full summary"));
	}


	@Test
	void showSummaryFor_addsToHistoryLikeSetDescriptionFor() throws ReflectiveOperationException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		AutoCompletion ac = newInstalledAutoCompletion(provider);
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);

		BasicCompletion completion = new BasicCompletion(provider, "foo", "short", "summary one");
		window.showSummaryFor(completion, null);
		fireDescriptionTimer(window);

		Assertions.assertTrue(getDescArea(window).getText().contains("summary one"));
	}


	@Test
	void backAndForwardActions_navigateHistory() throws ReflectiveOperationException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		AutoCompletion ac = newInstalledAutoCompletion(provider);
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);

		Action backAction = (Action) getField(window, "backAction");
		Action forwardAction = (Action) getField(window, "forwardAction");

		BasicCompletion first = new BasicCompletion(provider, "foo", "short", "summary one");
		BasicCompletion second = new BasicCompletion(provider, "bar", "short", "summary two");

		window.setDescriptionFor(first); // addToHistory=false -> clears history, becomes entry 0
		fireDescriptionTimer(window);
		Assertions.assertFalse(backAction.isEnabled());
		Assertions.assertFalse(forwardAction.isEnabled());

		window.setDescriptionFor(second, true); // addToHistory=true -> becomes entry 1
		fireDescriptionTimer(window);
		Assertions.assertTrue(getDescArea(window).getText().contains("summary two"));
		Assertions.assertTrue(backAction.isEnabled());
		Assertions.assertFalse(forwardAction.isEnabled());

		backAction.actionPerformed(null);
		Assertions.assertTrue(getDescArea(window).getText().contains("summary one"));
		Assertions.assertFalse(backAction.isEnabled());
		Assertions.assertTrue(forwardAction.isEnabled());

		forwardAction.actionPerformed(null);
		Assertions.assertTrue(getDescArea(window).getText().contains("summary two"));
		Assertions.assertTrue(backAction.isEnabled());
		Assertions.assertFalse(forwardAction.isEnabled());
	}


	@Test
	void addToHistory_afterGoingBack_truncatesForwardHistory() throws ReflectiveOperationException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		AutoCompletion ac = newInstalledAutoCompletion(provider);
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);

		Action backAction = (Action) getField(window, "backAction");
		Action forwardAction = (Action) getField(window, "forwardAction");

		BasicCompletion first = new BasicCompletion(provider, "foo", "short", "summary one");
		BasicCompletion second = new BasicCompletion(provider, "bar", "short", "summary two");
		BasicCompletion third = new BasicCompletion(provider, "baz", "short", "summary three");

		window.setDescriptionFor(first);
		fireDescriptionTimer(window);
		window.setDescriptionFor(second, true);
		fireDescriptionTimer(window);

		backAction.actionPerformed(null); // back to "first"
		Assertions.assertTrue(forwardAction.isEnabled(), "Sanity check: forward history should exist before truncation");

		window.setDescriptionFor(third, true); // should discard "second" from forward history

		fireDescriptionTimer(window);
		Assertions.assertTrue(getDescArea(window).getText().contains("summary three"));
		Assertions.assertFalse(forwardAction.isEnabled(), "Forward history should have been truncated");
		Assertions.assertTrue(backAction.isEnabled());

		backAction.actionPerformed(null);
		Assertions.assertTrue(getDescArea(window).getText().contains("summary one"),
			"\"second\" should no longer be reachable in history");
	}


	@Test
	void setVisible_false_clearsHistoryAndDisablesActions() throws ReflectiveOperationException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		AutoCompletion ac = newInstalledAutoCompletion(provider);
		frame = new JFrame();
		frame.setVisible(true);
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(frame, ac);

		Action backAction = (Action) getField(window, "backAction");
		Action forwardAction = (Action) getField(window, "forwardAction");

		window.setVisible(true);
		window.setDescriptionFor(new BasicCompletion(provider, "foo", "short", "summary one"));
		fireDescriptionTimer(window);
		window.setDescriptionFor(new BasicCompletion(provider, "bar", "short", "summary two"), true);
		fireDescriptionTimer(window);
		Assertions.assertTrue(backAction.isEnabled(), "Sanity check: history should be populated");

		window.setVisible(false);

		Assertions.assertFalse(backAction.isEnabled());
		Assertions.assertFalse(forwardAction.isEnabled());
	}


	@Test
	void updateUI_doesNotThrowAndSyncsScrollPaneBackground() throws ReflectiveOperationException {
		AutoCompletion ac = newInstalledAutoCompletion(new DefaultCompletionProvider());
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);

		Assertions.assertDoesNotThrow(window::updateUI);

		javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) getField(window, "scrollPane");
		Assertions.assertEquals(getDescArea(window).getBackground(), scrollPane.getBackground());
	}


	@Test
	void hyperlinkUpdate_nonActivatedEventType_doesNothing() throws MalformedURLException {
		AutoCompletion ac = newInstalledAutoCompletion(new DefaultCompletionProvider());
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);

		URL url = URI.create("https://example.com").toURL();
		HyperlinkEvent e = new HyperlinkEvent(window, HyperlinkEvent.EventType.ENTERED, url);
		Assertions.assertDoesNotThrow(() -> window.hyperlinkUpdate(e));
	}


	@Test
	void hyperlinkUpdate_activatedWithUrl_invokesExternalUrlHandler() throws ReflectiveOperationException,
			MalformedURLException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		AutoCompletion ac = newInstalledAutoCompletion(provider);
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);

		java.util.concurrent.atomic.AtomicReference<HyperlinkEvent> received = new java.util.concurrent.atomic.AtomicReference<>();
		ac.setExternalURLHandler((event, completion, callback) -> received.set(event));

		// hyperlinkUpdate() looks up the currently-displayed history entry, so a description
		// must actually have finished displaying (i.e. the timer fired) before a link click.
		BasicCompletion completion = new BasicCompletion(provider, "foo", "short", "summary");
		window.setDescriptionFor(completion);
		fireDescriptionTimer(window);

		URL url = URI.create("https://example.com/docs").toURL();
		HyperlinkEvent e = new HyperlinkEvent(window, HyperlinkEvent.EventType.ACTIVATED, url);
		window.hyperlinkUpdate(e);

		Assertions.assertNotNull(received.get());
		Assertions.assertEquals(url, received.get().getURL());
	}


	@Test
	void hyperlinkUpdate_activatedWithUrl_linkRedirectorRewritesUrlBeforeHandler()
			throws ReflectiveOperationException, MalformedURLException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		AutoCompletion ac = newInstalledAutoCompletion(provider);
		AutoCompleteDescWindow window = new AutoCompleteDescWindow(new JFrame(), ac);

		java.util.concurrent.atomic.AtomicReference<URL> received = new java.util.concurrent.atomic.AtomicReference<>();
		ac.setExternalURLHandler((event, completion, callback) -> received.set(event.getURL()));

		BasicCompletion completion = new BasicCompletion(provider, "foo", "short", "summary");
		window.setDescriptionFor(completion);
		fireDescriptionTimer(window);

		URL original = URI.create("https://example.com/original").toURL();
		URL redirected = URI.create("https://example.com/redirected").toURL();
		try {
			AutoCompletion.setLinkRedirector(url -> redirected);

			HyperlinkEvent e = new HyperlinkEvent(window, HyperlinkEvent.EventType.ACTIVATED, original);
			window.hyperlinkUpdate(e);

			Assertions.assertEquals(redirected, received.get());
		} finally {
			AutoCompletion.setLinkRedirector(null);
		}
	}


	/**
	 * The url==null branch (used for "in-doc" links, e.g. from XML-defined completions per
	 * c.xml) assumes it's parented by a real {@code AutoCompletePopupWindow} with a current
	 * selection - it casts {@code getParent()} directly. Driving that through the full
	 * {@code AutoCompletion}/popup pipeline (rather than constructing this window standalone)
	 * is the only realistic way to exercise it.
	 */
	@Test
	void hyperlinkUpdate_activatedNoUrl_looksUpCompletionByInputTextViaParentPopup()
			throws ReflectiveOperationException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion source = new BasicCompletion(provider, "foo", "short", "links to <a href=''>bar</a>");
		// Purely to force >1 match on the "foo" prefix below, so the popup (and thus the desc
		// window) actually opens instead of auto-inserting the lone match.
		BasicCompletion filler = new BasicCompletion(provider, "fooZ");
		BasicCompletion target = new BasicCompletion(provider, "bar", "short", "the bar summary");
		provider.addCompletion(source);
		provider.addCompletion(filler);
		provider.addCompletion(target);

		AutoCompletion ac = new AutoCompletion(provider);
		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
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
		Assertions.assertSame(source, popupWindow.getSelection(), "Sanity check on which completion is selected");
		AutoCompleteDescWindow window = (AutoCompleteDescWindow) popupWindow.getDescWindow();
		fireDescriptionTimer(window); // let the initial description ("source") finish displaying

		HyperlinkEvent e = new HyperlinkEvent(window, HyperlinkEvent.EventType.ACTIVATED, null, "bar");
		window.hyperlinkUpdate(e);
		fireDescriptionTimer(window); // the resulting setDescriptionFor() call is itself timer-based

		Assertions.assertTrue(getDescArea(window).getText().contains("the bar summary"));
	}


}

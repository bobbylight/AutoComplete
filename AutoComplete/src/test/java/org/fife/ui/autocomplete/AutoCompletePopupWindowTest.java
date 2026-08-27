/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class AutoCompletePopupWindowTest {

	private JFrame frame;
	private JTextArea textArea;
	private DefaultCompletionProvider provider;
	private AutoCompletion ac;
	private AutoCompletePopupWindow popupWindow;


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
	 * Realizes a text area with an active, visible popup window containing three matches
	 * for "foo". Called explicitly at the start of each test, rather than from
	 * {@code @BeforeEach}, since {@link SwingRunnerExtension} only runs {@code @Test} methods
	 * on the EDT - window creation/visibility changes made here need to happen on the EDT too,
	 * alongside the assertions that depend on them.
	 */
	private void showPopup() {

		provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo", "foo's summary"));
		provider.addCompletion(new BasicCompletion(provider, "foobar", "foobar's summary"));
		provider.addCompletion(new BasicCompletion(provider, "foobaz", "foobaz's summary"));

		ac = new AutoCompletion(provider);
		ac.setAutoCompleteEnabled(true);
		ac.setAutoCompleteSingleChoices(false);

		textArea = new JTextArea();
		ac.install(textArea);

		frame = new JFrame();
		frame.add(textArea);
		frame.pack();
		frame.setVisible(true);

		textArea.setText("foo");
		textArea.setCaretPosition(textArea.getText().length());
		ac.doCompletion();

		popupWindow = ac.getPopupWindow();
		Assertions.assertNotNull(popupWindow, "Sanity check: popup should be visible with 3 matches");
	}


	private static void fireKeyAction(JTextArea textArea, KeyStroke ks) {
		InputMap im = textArea.getInputMap();
		ActionMap am = textArea.getActionMap();
		Object key = im.get(ks);
		Assertions.assertNotNull(key, "No action bound to keystroke " + ks);
		Action action = am.get(key);
		Assertions.assertNotNull(action, "No action found for key " + key);
		action.actionPerformed(new ActionEvent(textArea, ActionEvent.ACTION_PERFORMED, ""));
	}


	@Test
	void getSelection_returnsFirstItemSelectedByDefault() {
		showPopup();

		Assertions.assertEquals("foo", popupWindow.getSelection().getReplacementText());
	}


	@Test
	void downAction_movesSelectionToNextItemAndWrapsAround() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		Assertions.assertEquals("foobar", popupWindow.getSelection().getReplacementText());

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		Assertions.assertEquals("foobaz", popupWindow.getSelection().getReplacementText());

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		Assertions.assertEquals("foo", popupWindow.getSelection().getReplacementText(),
			"Selection should wrap back around to the first item");
	}


	@Test
	void upAction_wrapsToLastItemFromFirst() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
		Assertions.assertEquals("foobaz", popupWindow.getSelection().getReplacementText(),
			"Selection should wrap to the last item when moving up from the first");
	}


	@Test
	void endAction_selectsLastItem() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));
		Assertions.assertEquals("foobaz", popupWindow.getSelection().getReplacementText());
	}


	@Test
	void homeAction_selectsFirstItem() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));
		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));
		Assertions.assertEquals("foo", popupWindow.getSelection().getReplacementText());
	}


	@Test
	void pageDownAction_movesToLastItemWhenVisibleRowsExceedRemaining() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
		Assertions.assertEquals("foobaz", popupWindow.getSelection().getReplacementText());
	}


	@Test
	void pageUpAction_movesToFirstItemWhenVisibleRowsExceedIndex() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));
		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
		Assertions.assertEquals("foo", popupWindow.getSelection().getReplacementText());
	}


	@Test
	void enterAction_insertsSelectedCompletionAndHidesPopup() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

		Assertions.assertEquals("foobar", textArea.getText());
		Assertions.assertFalse(popupWindow.isVisible());
	}


	@Test
	void tabAction_alsoInsertsSelectedCompletion() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
		Assertions.assertEquals("foo", textArea.getText());
	}


	@Test
	void escapeAction_hidesPopupWithoutInsertingText() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		Assertions.assertFalse(popupWindow.isVisible());
		Assertions.assertEquals("foo", textArea.getText());
	}


	@Test
	void getSelection_afterHiding_returnsLastSelection() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

		Assertions.assertFalse(popupWindow.isVisible());
		Assertions.assertEquals("foobar", popupWindow.getSelection().getReplacementText(),
			"getSelection() should remember the last selection even once hidden");
	}


	@Test
	void mouseClicked_doubleClick_insertsSelectedCompletion() {
		showPopup();

		MouseEvent doubleClick = new MouseEvent(popupWindow, MouseEvent.MOUSE_CLICKED,
			System.currentTimeMillis(), 0, 0, 0, 2, false);
		popupWindow.mouseClicked(doubleClick);

		Assertions.assertEquals("foo", textArea.getText());
	}


	@Test
	void mouseClicked_singleClick_doesNotInsertCompletion() {
		showPopup();

		MouseEvent singleClick = new MouseEvent(popupWindow, MouseEvent.MOUSE_CLICKED,
			System.currentTimeMillis(), 0, 0, 0, 1, false);
		popupWindow.mouseClicked(singleClick);

		Assertions.assertEquals("foo", textArea.getText());
		Assertions.assertTrue(popupWindow.isVisible());
	}


	@Test
	void setCompletions_selectsFirstItem() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		Assertions.assertEquals("foobar", popupWindow.getSelection().getReplacementText());

		popupWindow.setCompletions(java.util.List.of(
			new BasicCompletion(provider, "zzz"),
			new BasicCompletion(provider, "zzzTop")));

		Assertions.assertEquals("zzz", popupWindow.getSelection().getReplacementText());
	}


	@Test
	void getSetListCellRenderer_roundTrips() {
		showPopup();

		ListCellRenderer<Object> renderer = new DefaultListCellRenderer();
		popupWindow.setListCellRenderer(renderer);
		Assertions.assertSame(renderer, popupWindow.getListCellRenderer());
	}


	@Test
	void getDescriptionWindowColor_beforeDescWindowCreated_returnsNullByDefault() {
		showPopup();

		Assertions.assertNull(popupWindow.getDescriptionWindowColor());
	}


	@Test
	void setDescriptionWindowColor_beforeDescWindowCreated_appliedWhenCreated() {
		showPopup();

		Color color = new Color(10, 20, 30);
		popupWindow.setDescriptionWindowColor(color);
		Assertions.assertEquals(color, popupWindow.getDescriptionWindowColor());

		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
		ac.doCompletion();

		Assertions.assertEquals(color, popupWindow.getDescWindow().getBackground());
	}


	@Test
	void setDescriptionWindowSize_beforeDescWindowCreated_appliedWhenCreated() {
		showPopup();

		Dimension size = new Dimension(444, 111);
		popupWindow.setDescriptionWindowSize(size);

		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
		ac.doCompletion();

		Assertions.assertEquals(size, popupWindow.getDescWindow().getSize());
	}


	@Test
	void setDescriptionWindowSize_afterDescWindowCreated_resizesImmediately() {
		showPopup();

		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
		ac.doCompletion();
		Assertions.assertNotNull(popupWindow.getDescWindow());

		Dimension size = new Dimension(321, 123);
		popupWindow.setDescriptionWindowSize(size);

		Assertions.assertEquals(size, popupWindow.getDescWindow().getSize());
	}


	@Test
	void getDescWindow_nullUntilShown() {
		showPopup();

		Assertions.assertNull(popupWindow.getDescWindow());
	}


	@Test
	void disposeDescWindow_whenNoneCreated_doesNotThrow() {
		showPopup();

		Assertions.assertDoesNotThrow(popupWindow::disposeDescWindow);
	}


	@Test
	void disposeDescWindow_disposesAndClearsReference() {
		showPopup();

		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
		ac.doCompletion();
		AbstractDescWindow descWindow = popupWindow.getDescWindow();
		Assertions.assertNotNull(descWindow);

		popupWindow.disposeDescWindow();

		Assertions.assertNull(popupWindow.getDescWindow());
		Assertions.assertFalse(descWindow.isDisplayable());
	}


	@Test
	void toggleDescriptionWindow_notOnDemand_doesNothing() {
		showPopup();

		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
		ac.doCompletion();

		popupWindow.toggleDescriptionWindow();
		Assertions.assertNotNull(popupWindow.getDescWindow());
		Assertions.assertTrue(popupWindow.getDescWindow().isVisible());
	}


	@Test
	void toggleDescriptionWindow_notVisible_doesNothing() {
		showPopup();

		ac.setDescWindowVisibility(DescWindowVisibility.ON_DEMAND);
		popupWindow.setVisible(false);

		Assertions.assertDoesNotThrow(popupWindow::toggleDescriptionWindow);
		Assertions.assertNull(popupWindow.getDescWindow());
	}


	@Test
	void installAndUninstallDescWindowToggleKey_addsAndRemovesBinding() {
		showPopup();

		JTextArea otherTextArea = new JTextArea();
		KeyStroke ks = KeyStroke.getKeyStroke("ctrl alt T");
		InputMap im = otherTextArea.getInputMap();
		ActionMap am = otherTextArea.getActionMap();

		Assertions.assertNull(im.get(ks));

		AutoCompletePopupWindow.installDescWindowToggleKey(ac, otherTextArea, ks);
		Assertions.assertNotNull(im.get(ks));
		Assertions.assertNotNull(am.get(im.get(ks)));

		AutoCompletePopupWindow.uninstallDescWindowToggleKey(otherTextArea, ks);
		Assertions.assertNull(im.get(ks));
	}


	@Test
	void updateUI_doesNotThrow() {
		showPopup();

		Assertions.assertDoesNotThrow(popupWindow::updateUI);
	}


	@Test
	void updateUI_withDescWindow_doesNotThrow() {
		showPopup();

		ac.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
		ac.doCompletion();
		Assertions.assertDoesNotThrow(popupWindow::updateUI);
	}


	@Test
	void setVisible_false_clearsListModelAndRemembersSelection() {
		showPopup();

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		Completion expected = popupWindow.getSelection();

		popupWindow.setVisible(false);

		Assertions.assertFalse(popupWindow.isVisible());
		Assertions.assertEquals(expected, popupWindow.getSelection());
	}


	@Test
	void setVisible_true_reInstallsKeyBindingsAndSelectsFirstItem() {
		showPopup();

		popupWindow.setVisible(false);
		Assertions.assertFalse(popupWindow.isVisible());

		ac.doCompletion();
		AutoCompletePopupWindow newPopupWindow = ac.getPopupWindow();
		Assertions.assertTrue(newPopupWindow.isVisible());
		Assertions.assertEquals("foo", newPopupWindow.getSelection().getReplacementText());
	}


	@Test
	void copyAction_noDescWindowVisible_doesNotThrow() {
		showPopup();

		Assertions.assertDoesNotThrow(() ->
			fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_C,
				java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())));
	}


	@Test
	void leftRightActions_atCaretBounds_doNotThrow() {
		showPopup();

		textArea.setCaretPosition(0);
		Assertions.assertDoesNotThrow(() ->
			fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)));

		textArea.setCaretPosition(textArea.getDocument().getLength());
		Assertions.assertDoesNotThrow(() ->
			fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)));
	}

}

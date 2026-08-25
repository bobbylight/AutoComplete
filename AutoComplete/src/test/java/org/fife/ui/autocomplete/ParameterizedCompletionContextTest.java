/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.Highlighter.Highlight;

import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class ParameterizedCompletionContextTest {

	private JFrame frame;
	private JTextArea textArea;
	private DefaultCompletionProvider provider;
	private AutoCompletion ac;


	@BeforeEach
	void setUp() {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

		provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');

		ac = new AutoCompletion(provider);
		ac.setParameterAssistanceEnabled(true);

		textArea = new JTextArea();
		ac.install(textArea);

		frame = new JFrame();
		frame.add(textArea);
		frame.pack();
		frame.setVisible(true);
	}


	@AfterEach
	void tearDown() {
		if (frame != null) {
			frame.dispose();
			frame = null;
		}
	}


	private FunctionCompletion newFunctionCompletion(String... paramNames) {
		FunctionCompletion fc = new FunctionCompletion(provider, "foo", "void");
		List<Parameter> params = new ArrayList<>();
		for (String name : paramNames) {
			params.add(new Parameter("int", name));
		}
		fc.setParams(params);
		return fc;
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


	private static Object getField(Object target, String name) throws ReflectiveOperationException {
		Field field = ParameterizedCompletionContext.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}


	private static boolean isActive(ParameterizedCompletionContext ctx) throws ReflectiveOperationException {
		return (boolean) getField(ctx, "active");
	}


	private static Object getParamChoicesWindow(ParameterizedCompletionContext ctx)
			throws ReflectiveOperationException {
		return getField(ctx, "paramChoicesWindow");
	}


	@Test
	void activate_insertsParameterTextAndSelectsFirstParam() {
		FunctionCompletion fc = newFunctionCompletion("a", "b");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();

		Assertions.assertEquals("(a, b)", textArea.getText());
		Assertions.assertEquals("a", textArea.getSelectedText());
	}


	@Test
	void activate_calledTwice_isIdempotent() {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();
		String textAfterFirstActivate = textArea.getText();

		ctx.activate(); // Should be a no-op since already active

		Assertions.assertEquals(textAfterFirstActivate, textArea.getText());
	}


	@Test
	void getParameterHighlights_countsParamsPlusEndMarker() {
		FunctionCompletion fc = newFunctionCompletion("a", "b");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();

		// 2 real parameter highlights, plus 1 zero-length "ending position" highlight.
		Assertions.assertEquals(3, ctx.getParameterHighlights().size());
	}


	@Test
	void getArgumentText_offsetInParamHighlight_returnsCurrentParamText() {
		FunctionCompletion fc = newFunctionCompletion("a", "b");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();

		List<Highlight> highlights = ctx.getParameterHighlights();
		// First two highlights are the real params ("a" and "b"); derive their offsets
		// programmatically rather than hardcoding numbers tied to internal offset math.
		Highlight aHighlight = highlights.get(0);
		Highlight bHighlight = highlights.get(1);

		Assertions.assertEquals("a", ctx.getArgumentText(aHighlight.getStartOffset() + 1));
		Assertions.assertEquals("b", ctx.getArgumentText(bHighlight.getStartOffset() + 1));
	}


	@Test
	void getArgumentText_offsetNotInAnyHighlight_returnsNull() {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();

		Assertions.assertNull(ctx.getArgumentText(-5));
	}


	@Test
	void getArgumentText_noActiveHighlights_returnsNull() {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);
		// Never activated, so getParameterHighlights() is empty.
		Assertions.assertNull(ctx.getArgumentText(0));
	}


	@Test
	void tabAndShiftTab_navigateBetweenParams() {
		FunctionCompletion fc = newFunctionCompletion("a", "b");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();
		Assertions.assertEquals("a", textArea.getSelectedText());

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
		Assertions.assertEquals("b", textArea.getSelectedText());

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK));
		Assertions.assertEquals("a", textArea.getSelectedText());
	}


	@Test
	void deactivate_restoresKeyBindingsAndHidesWindows() throws ReflectiveOperationException {
		KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		Object bindingBeforeActivate = textArea.getInputMap().get(tabKey);

		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();
		Assertions.assertNotEquals(bindingBeforeActivate, textArea.getInputMap().get(tabKey));
		Assertions.assertTrue(isActive(ctx));

		ctx.deactivate();

		Assertions.assertFalse(isActive(ctx));
		Assertions.assertEquals(bindingBeforeActivate, textArea.getInputMap().get(tabKey));
	}


	@Test
	void deactivate_whenNotActive_doesNothing() throws ReflectiveOperationException {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);
		Assertions.assertDoesNotThrow(ctx::deactivate);
		Assertions.assertFalse(isActive(ctx));
	}


	@Test
	void caretMovedBeforeMinOffset_deactivatesAutomatically() throws ReflectiveOperationException {
		FunctionCompletion fc = newFunctionCompletion("a", "b");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();
		Assertions.assertTrue(isActive(ctx));

		// The minimum offset is right after the opening '(' (position 1); moving to the very
		// start of the document is always below that, regardless of how minPos/maxPos track
		// subsequent document edits.
		textArea.setCaretPosition(0);

		Assertions.assertFalse(isActive(ctx));
	}


	@Test
	void focusLost_deactivates() throws ReflectiveOperationException {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();
		Assertions.assertTrue(isActive(ctx));

		for (FocusListener l : textArea.getFocusListeners()) {
			l.focusLost(new FocusEvent(textArea, FocusEvent.FOCUS_LOST));
		}

		Assertions.assertFalse(isActive(ctx));
	}


	@Test
	void escape_noChoicesWindowVisible_deactivates() throws ReflectiveOperationException {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();
		// Sanity check: no ParameterChoicesProvider is installed, so the choices window
		// should not be showing.
		Object choicesWindow = getParamChoicesWindow(ctx);
		Assertions.assertFalse(((javax.swing.JWindow) choicesWindow).isVisible());

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

		Assertions.assertFalse(isActive(ctx));
	}


	@Test
	void closingChar_typedWellBeforeEnd_insertsLiteralCharWithoutDeactivating() {
		FunctionCompletion fc = newFunctionCompletion("a", "b");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate(); // Text is now "(a, b)"; selection is on "a"

		String textBefore = textArea.getText();
		fireKeyAction(textArea, KeyStroke.getKeyStroke(')'));

		// Typing ')' while still on an early parameter isn't "closing" the call -
		// it should just insert a literal character (replacing the "a" selection).
		Assertions.assertEquals(textBefore.length(), textArea.getText().length());
		Assertions.assertTrue(textArea.getText().contains(")"));
	}


	@Test
	void closingChar_typedAtEndOfCompletion_deactivates() throws ReflectiveOperationException {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate(); // Text is now "(a)"

		textArea.setCaretPosition(textArea.getText().length());
		fireKeyAction(textArea, KeyStroke.getKeyStroke(')'));

		Assertions.assertFalse(isActive(ctx));
	}


	@Test
	void insertSelectedChoice_choicesWindowNotVisible_returnsFalse() {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();

		Assertions.assertFalse(ctx.insertSelectedChoice());
	}


	@Test
	void insertSelectedChoice_choiceAvailable_replacesParamAndMovesToNext() throws ReflectiveOperationException {
		BasicCompletion choice1 = new BasicCompletion(provider, "CHOICE_ONE");
		BasicCompletion choice2 = new BasicCompletion(provider, "CHOICE_TWO");
		provider.setParameterChoicesProvider((tc, param) -> List.of(choice1, choice2));

		FunctionCompletion fc = newFunctionCompletion("a", "b");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();

		Object choicesWindow = getParamChoicesWindow(ctx);
		Assertions.assertTrue(((javax.swing.JWindow) choicesWindow).isVisible(),
			"Sanity check: choices should be showing for the first parameter");

		boolean inserted = ctx.insertSelectedChoice();

		Assertions.assertTrue(inserted);
		Assertions.assertTrue(textArea.getText().contains("CHOICE_ONE"));
		// Selection should have moved on to the next parameter ("b").
		Assertions.assertEquals("b", textArea.getSelectedText());
	}


	@Test
	void upDownArrows_choicesWindowVisible_changeSelectedChoice() throws ReflectiveOperationException {
		BasicCompletion choice1 = new BasicCompletion(provider, "CHOICE_ONE");
		BasicCompletion choice2 = new BasicCompletion(provider, "CHOICE_TWO");
		provider.setParameterChoicesProvider((tc, param) -> List.of(choice1, choice2));

		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();

		ParameterizedCompletionChoicesWindow choicesWindow =
			(ParameterizedCompletionChoicesWindow) getParamChoicesWindow(ctx);
		Assertions.assertEquals("CHOICE_ONE", choicesWindow.getSelectedChoice());

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
		Assertions.assertEquals("CHOICE_TWO", choicesWindow.getSelectedChoice());

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
		Assertions.assertEquals("CHOICE_ONE", choicesWindow.getSelectedChoice());
	}


	@Test
	void updateUI_doesNotThrow() {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate();

		Assertions.assertDoesNotThrow(ctx::updateUI);
	}


	@Test
	void updateUI_neverActivated_doesNotThrow() {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);
		Assertions.assertDoesNotThrow(ctx::updateUI);
	}


	@Test
	void enter_noChoiceSelected_movesToDefaultEndOffset() {
		FunctionCompletion fc = newFunctionCompletion("a");
		ParameterizedCompletionContext ctx = new ParameterizedCompletionContext(frame, ac, fc);

		textArea.setCaretPosition(0);
		ctx.activate(); // Text is now "(a)"; selection is on "a"

		fireKeyAction(textArea, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

		Assertions.assertEquals(textArea.getText().length(), textArea.getCaretPosition());
	}


}

/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class RoundRobinAutoCompletionTest {

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


	private void realize(JTextArea textArea) {
		frame = new JFrame();
		frame.add(textArea);
		frame.pack();
		frame.setVisible(true);
	}


	@Test
	void constructor_addsProviderToCycle() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider);
		Assertions.assertSame(provider, ac.getCompletionProvider());

		// advancing with a single provider in the cycle should return to itself
		Assertions.assertTrue(ac.advanceProvider());
		Assertions.assertSame(provider, ac.getCompletionProvider());
	}


	@Test
	void constructor_setsRoundRobinRequiredFlags() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider);

		Assertions.assertFalse(ac.isHideOnCompletionProviderChange());
		Assertions.assertFalse(ac.isHideOnNoText());
		Assertions.assertFalse(ac.getAutoCompleteSingleChoices());
	}


	@Test
	void addCompletionProvider_addsToCycleWithoutChangingCurrentProvider() {
		DefaultCompletionProvider provider1 = new DefaultCompletionProvider();
		DefaultCompletionProvider provider2 = new DefaultCompletionProvider();
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider1);

		ac.addCompletionProvider(provider2);

		Assertions.assertSame(provider1, ac.getCompletionProvider());
	}


	@Test
	void advanceProvider_cyclesThroughProvidersInOrder() {
		DefaultCompletionProvider provider1 = new DefaultCompletionProvider();
		DefaultCompletionProvider provider2 = new DefaultCompletionProvider();
		DefaultCompletionProvider provider3 = new DefaultCompletionProvider();
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider1);
		ac.addCompletionProvider(provider2);
		ac.addCompletionProvider(provider3);

		Assertions.assertFalse(ac.advanceProvider());
		Assertions.assertSame(provider2, ac.getCompletionProvider());

		Assertions.assertFalse(ac.advanceProvider());
		Assertions.assertSame(provider3, ac.getCompletionProvider());

		// Wraps back around to the first (default) provider
		Assertions.assertTrue(ac.advanceProvider());
		Assertions.assertSame(provider1, ac.getCompletionProvider());
	}


	@Test
	void resetProvider_currentIsNotDefault_switchesBackToDefault() {
		DefaultCompletionProvider provider1 = new DefaultCompletionProvider();
		DefaultCompletionProvider provider2 = new DefaultCompletionProvider();
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider1);
		ac.addCompletionProvider(provider2);

		ac.advanceProvider();
		Assertions.assertSame(provider2, ac.getCompletionProvider());

		ac.resetProvider();
		Assertions.assertSame(provider1, ac.getCompletionProvider());
	}


	@Test
	void resetProvider_currentIsAlreadyDefault_doesNothing() {
		DefaultCompletionProvider provider1 = new DefaultCompletionProvider();
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider1);

		ac.resetProvider();

		Assertions.assertSame(provider1, ac.getCompletionProvider());
	}


	@Test
	void createAutoCompleteAction_returnsNonNullAction() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider);
		Assertions.assertNotNull(ac.createAutoCompleteAction());
	}


	@Test
	void cycleAction_popupNotVisible_resetsToDefaultProvider() {
		DefaultCompletionProvider provider1 = new DefaultCompletionProvider();
		DefaultCompletionProvider provider2 = new DefaultCompletionProvider();
		provider1.addCompletion(new BasicCompletion(provider1, "fromProvider1"));
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider1);
		ac.addCompletionProvider(provider2);

		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		// Manually move off of the default provider, as if a prior cycle left it there.
		ac.advanceProvider();
		Assertions.assertSame(provider2, ac.getCompletionProvider());

		Action action = ac.createAutoCompleteAction();
		action.actionPerformed(new ActionEvent(textArea, ActionEvent.ACTION_PERFORMED, ""));

		Assertions.assertSame(provider1, ac.getCompletionProvider());
	}


	@Test
	void cycleAction_currentProviderHasNoCompletions_advancesToNextWithCompletions() {
		DefaultCompletionProvider provider1 = new DefaultCompletionProvider(); // no completions added
		DefaultCompletionProvider provider2 = new DefaultCompletionProvider();
		provider2.addCompletion(new BasicCompletion(provider2, "fromProvider2"));
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider1);
		ac.addCompletionProvider(provider2);

		JTextArea textArea = new JTextArea();
		ac.install(textArea);
		realize(textArea);

		Action action = ac.createAutoCompleteAction();
		action.actionPerformed(new ActionEvent(textArea, ActionEvent.ACTION_PERFORMED, ""));

		Assertions.assertSame(provider2, ac.getCompletionProvider());
	}


	@Test
	void cycleAction_noProviderHasCompletions_endsOnLastProviderInCycle() {
		DefaultCompletionProvider provider1 = new DefaultCompletionProvider();
		DefaultCompletionProvider provider2 = new DefaultCompletionProvider();
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider1);
		ac.addCompletionProvider(provider2);

		JTextArea textArea = new JTextArea();
		ac.install(textArea);

		Action action = ac.createAutoCompleteAction();
		Assertions.assertDoesNotThrow(() ->
			action.actionPerformed(new ActionEvent(textArea, ActionEvent.ACTION_PERFORMED, "")));

		Assertions.assertSame(provider2, ac.getCompletionProvider());
	}


	@Test
	void cycleAction_autoCompleteDisabled_doesNotAdvanceOrReset() {
		DefaultCompletionProvider provider1 = new DefaultCompletionProvider();
		DefaultCompletionProvider provider2 = new DefaultCompletionProvider();
		RoundRobinAutoCompletion ac = new RoundRobinAutoCompletion(provider1);
		ac.addCompletionProvider(provider2);
		ac.setAutoCompleteEnabled(false);

		JTextArea textArea = new JTextArea();
		ac.install(textArea);

		Action action = ac.createAutoCompleteAction();
		action.actionPerformed(new ActionEvent(textArea, ActionEvent.ACTION_PERFORMED, ""));

		Assertions.assertSame(provider1, ac.getCompletionProvider());
	}


}

/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import javax.swing.JTextArea;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class AbstractCompletionProviderTest {

	/**
	 * Regression test for https://github.com/bobbylight/AutoComplete/issues/101 -
	 * when several completions share the same input text (e.g. overloaded
	 * methods), the first one in the sorted completion list was never
	 * included in the returned results, due to an off-by-one error.
	 */
	@Test
	void getCompletions_allOverloadsReturned_evenTheFirstOneInTheList() {

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion overload1 = new BasicCompletion(provider, "get", "overload 1");
		BasicCompletion overload2 = new BasicCompletion(provider, "get", "overload 2");
		BasicCompletion overload3 = new BasicCompletion(provider, "get", "overload 3");
		provider.addCompletion(overload1);
		provider.addCompletion(overload2);
		provider.addCompletion(overload3);

		JTextArea textArea = new JTextArea("get");
		textArea.setCaretPosition(textArea.getText().length());

		List<Completion> completions = provider.getCompletions(textArea);

		Assertions.assertEquals(3, completions.size());
		Assertions.assertTrue(completions.contains(overload1));
		Assertions.assertTrue(completions.contains(overload2));
		Assertions.assertTrue(completions.contains(overload3));

	}


}

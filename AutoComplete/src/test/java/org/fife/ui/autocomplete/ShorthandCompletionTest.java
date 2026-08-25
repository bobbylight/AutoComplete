/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class ShorthandCompletionTest {


	@Test
	void constructor_twoArg() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		ShorthandCompletion completion = new ShorthandCompletion(provider,
			"sysout", "System.out.println(");
		Assertions.assertEquals("sysout", completion.getInputText());
		Assertions.assertEquals("System.out.println(", completion.getReplacementText());
		Assertions.assertNull(completion.getShortDescription());
	}


	@Test
	void constructor_withShortDesc() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		ShorthandCompletion completion = new ShorthandCompletion(provider,
			"sysout", "System.out.println(", "short desc");
		Assertions.assertEquals("sysout", completion.getInputText());
		Assertions.assertEquals("System.out.println(", completion.getReplacementText());
		Assertions.assertEquals("short desc", completion.getShortDescription());
	}


	@Test
	void constructor_withShortDescAndSummary() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		ShorthandCompletion completion = new ShorthandCompletion(provider,
			"sysout", "System.out.println(", "short desc", "summary");
		Assertions.assertEquals("sysout", completion.getInputText());
		Assertions.assertEquals("System.out.println(", completion.getReplacementText());
		Assertions.assertEquals("short desc", completion.getShortDescription());
		Assertions.assertEquals("summary", completion.getSummary());
	}


	@Test
	void getSummary_returnsExplicitSummaryIfSet() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		ShorthandCompletion completion = new ShorthandCompletion(provider,
			"sysout", "System.out.println(", "short desc", "explicit summary");
		Assertions.assertEquals("explicit summary", completion.getSummary());
	}


	@Test
	void getSummary_generatedFromReplacementTextWhenNoSummarySet() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		ShorthandCompletion completion = new ShorthandCompletion(provider,
			"sysout", "System.out.println(");
		String expected = "<html><body><code>System.out.println(";
		Assertions.assertEquals(expected, completion.getSummary());
	}


	@Test
	void getSummaryBody() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		ShorthandCompletion completion = new ShorthandCompletion(provider,
			"sysout", "System.out.println(");
		Assertions.assertEquals("<code>System.out.println(", completion.getSummaryBody());
	}


	@Test
	void toString_usesInputTextNotReplacementText() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		ShorthandCompletion completion = new ShorthandCompletion(provider,
			"sysout", "System.out.println(");
		Assertions.assertEquals("sysout", completion.toString());
	}


}

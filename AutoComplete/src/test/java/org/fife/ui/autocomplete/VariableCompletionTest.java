/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class VariableCompletionTest {


	@Test
	void constructor() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "foo", "int");
		Assertions.assertEquals("foo", completion.getName());
		Assertions.assertEquals("int", completion.getType());
		Assertions.assertNull(completion.getDefinedIn());
	}


	@Test
	void getDefinitionString_withType() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "foo", "int");
		Assertions.assertEquals("int foo", completion.getDefinitionString());
	}


	@Test
	void getDefinitionString_noType() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "NULL", null);
		Assertions.assertEquals("NULL", completion.getDefinitionString());
	}


	@Test
	void getName_returnsReplacementText() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "foo", "int");
		Assertions.assertEquals(completion.getReplacementText(), completion.getName());
	}


	@Test
	void getSetDefinedIn() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "foo", "int");
		completion.setDefinedIn("SomeClass");
		Assertions.assertEquals("SomeClass", completion.getDefinedIn());
	}


	@Test
	void getSummary_minimal() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "foo", "int");
		String summary = completion.getSummary();
		Assertions.assertEquals("<html><b>int foo</b>", summary);
	}


	@Test
	void getSummary_withDescriptionAndDefinedIn() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "foo", "int");
		completion.setShortDescription("a description");
		completion.setDefinedIn("SomeClass");
		String summary = completion.getSummary();
		Assertions.assertTrue(summary.contains("a description"));
		Assertions.assertTrue(summary.contains("Defined in:"));
		Assertions.assertTrue(summary.contains("SomeClass"));
	}


	@Test
	void getToolTipText_returnsDefinitionString() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "foo", "int");
		Assertions.assertEquals(completion.getDefinitionString(), completion.getToolTipText());
	}


	@Test
	void possiblyAddDescription_returnsFalseWhenNoDescription() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "foo", "int");
		StringBuilder sb = new StringBuilder();
		Assertions.assertFalse(completion.possiblyAddDescription(sb));
		Assertions.assertEquals(0, sb.length());
	}


	@Test
	void possiblyAddDescription_returnsTrueWhenDescriptionSet() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "foo", "int");
		completion.setShortDescription("a description");
		StringBuilder sb = new StringBuilder();
		Assertions.assertTrue(completion.possiblyAddDescription(sb));
		Assertions.assertTrue(sb.toString().contains("a description"));
	}


	@Test
	void toString_returnsName() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(provider, "foo", "int");
		Assertions.assertEquals("foo", completion.toString());
	}


}

/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.util.ArrayList;
import java.util.List;

import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class MarkupTagCompletionTest {


	@Test
	void constructor() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		MarkupTagCompletion completion = new MarkupTagCompletion(provider, "div");
		Assertions.assertEquals("div", completion.getName());
		Assertions.assertEquals(0, completion.getAttributeCount());
		Assertions.assertNull(completion.getDescription());
		Assertions.assertNull(completion.getDefinedIn());
	}


	@Test
	void getReplacementText_returnsName() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		MarkupTagCompletion completion = new MarkupTagCompletion(provider, "div");
		Assertions.assertEquals("div", completion.getReplacementText());
	}


	@Test
	void getSetDescription() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		MarkupTagCompletion completion = new MarkupTagCompletion(provider, "div");
		completion.setDescription("a div element");
		Assertions.assertEquals("a div element", completion.getDescription());
	}


	@Test
	void getSetDefinedIn() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		MarkupTagCompletion completion = new MarkupTagCompletion(provider, "div");
		completion.setDefinedIn("HTML5");
		Assertions.assertEquals("HTML5", completion.getDefinedIn());
	}


	@Test
	void getSetAttributes() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		MarkupTagCompletion completion = new MarkupTagCompletion(provider, "div");

		List<Parameter> attrs = new ArrayList<>();
		attrs.add(new Parameter("string", "id"));
		attrs.add(new Parameter("string", "class"));
		completion.setAttributes(attrs);

		Assertions.assertEquals(2, completion.getAttributeCount());
		Assertions.assertEquals("id", completion.getAttribute(0).getName());
		Assertions.assertEquals("class", completion.getAttribute(1).getName());
		Assertions.assertEquals(attrs.size(), completion.getAttributes().size());
	}


	@Test
	void setAttributes_isDeepCopy() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		MarkupTagCompletion completion = new MarkupTagCompletion(provider, "div");

		List<Parameter> attrs = new ArrayList<>();
		attrs.add(new Parameter("string", "id"));
		completion.setAttributes(attrs);

		attrs.add(new Parameter("string", "class"));
		Assertions.assertEquals(1, completion.getAttributeCount());
	}


	@Test
	void getSummary_minimal() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		MarkupTagCompletion completion = new MarkupTagCompletion(provider, "div");
		String summary = completion.getSummary();
		Assertions.assertEquals("<html><b>div</b>", summary);
	}


	@Test
	void getSummary_withDescriptionAttributesAndDefinedIn() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		MarkupTagCompletion completion = new MarkupTagCompletion(provider, "div");
		completion.setDescription("a div element");
		completion.setDefinedIn("HTML5");

		List<Parameter> attrs = new ArrayList<>();
		Parameter attr = new Parameter("string", "id");
		attr.setDescription("the id attribute");
		attrs.add(attr);
		completion.setAttributes(attrs);

		String summary = completion.getSummary();
		Assertions.assertTrue(summary.contains("a div element"));
		Assertions.assertTrue(summary.contains("Attributes:"));
		Assertions.assertTrue(summary.contains("the id attribute"));
		Assertions.assertTrue(summary.contains("Defined in:"));
		Assertions.assertTrue(summary.contains("HTML5"));
	}


}

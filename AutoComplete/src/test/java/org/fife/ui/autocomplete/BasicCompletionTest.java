/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class BasicCompletionTest {


	@Test
	void constructor_replacementTextOnly() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		Assertions.assertEquals("foo", completion.getReplacementText());
		Assertions.assertNull(completion.getShortDescription());
		Assertions.assertNull(completion.getSummary());
	}


	@Test
	void constructor_withShortDesc() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo", "short desc");
		Assertions.assertEquals("foo", completion.getReplacementText());
		Assertions.assertEquals("short desc", completion.getShortDescription());
		Assertions.assertNull(completion.getSummary());
	}


	@Test
	void constructor_withShortDescAndSummary() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo", "short desc", "summary");
		Assertions.assertEquals("foo", completion.getReplacementText());
		Assertions.assertEquals("short desc", completion.getShortDescription());
		Assertions.assertEquals("summary", completion.getSummary());
	}


	@Test
	void getInputText_returnsReplacementText() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		Assertions.assertEquals("foo", completion.getInputText());
	}


	@Test
	void getProvider() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		Assertions.assertSame(provider, completion.getProvider());
	}


	@Test
	void getSetIcon() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		Assertions.assertNull(completion.getIcon());
		completion.setIcon(null);
		Assertions.assertNull(completion.getIcon());
	}


	@Test
	void getSetRelevance() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		Assertions.assertEquals(0, completion.getRelevance());
		completion.setRelevance(7);
		Assertions.assertEquals(7, completion.getRelevance());
	}


	@Test
	void setShortDescription() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		completion.setShortDescription("new desc");
		Assertions.assertEquals("new desc", completion.getShortDescription());
	}


	@Test
	void setSummary() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		completion.setSummary("new summary");
		Assertions.assertEquals("new summary", completion.getSummary());
	}


	@Test
	void toString_noShortDescription() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		Assertions.assertEquals("foo", completion.toString());
	}


	@Test
	void toString_withShortDescription() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo", "short desc");
		Assertions.assertEquals("foo - short desc", completion.toString());
	}


	@Test
	void compareTo_sameInstance() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		Assertions.assertEquals(0, completion.compareTo(completion));
	}


	@Test
	void compareTo_null() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		Assertions.assertEquals(-1, completion.compareTo(null));
	}


	@Test
	void compareTo_ignoresCase() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion1 = new BasicCompletion(provider, "foo");
		BasicCompletion completion2 = new BasicCompletion(provider, "FOO");
		Assertions.assertEquals(0, completion1.compareTo(completion2));
	}


	@Test
	void getToolTipText_defaultsToNull() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		Assertions.assertNull(completion.getToolTipText());
	}


}

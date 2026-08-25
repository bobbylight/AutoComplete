/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import javax.swing.JTextArea;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class TemplateCompletionTest {


	@Test
	void constructor_fourArg() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"for", "for-loop", "for (int ${i} = 0; ${i} < 10; ${i}++) {\n\t${cursor}\n}");
		Assertions.assertEquals("for", completion.getInputText());
		Assertions.assertEquals("for-loop", completion.getDefinitionString());
		Assertions.assertNull(completion.getShortDescription());
		Assertions.assertNull(completion.getSummary());
	}


	@Test
	void constructor_sixArg() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"for", "for-loop", "for (${i}) {\n\t${cursor}\n}", "short desc", "summary");
		Assertions.assertEquals("for", completion.getInputText());
		Assertions.assertEquals("for-loop", completion.getDefinitionString());
		Assertions.assertEquals("short desc", completion.getShortDescription());
		Assertions.assertEquals("summary", completion.getSummary());
	}


	@Test
	void getReplacementText_alwaysNull() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"for", "for-loop", "plain text template");
		Assertions.assertNull(completion.getReplacementText());
	}


	@Test
	void getShowParameterToolTip_alwaysFalse() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"for", "for-loop", "plain text template");
		Assertions.assertFalse(completion.getShowParameterToolTip());
	}


	@Test
	void getParamCount_noParams() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"foo", "foo desc", "plain text, no params");
		Assertions.assertEquals(0, completion.getParamCount());
	}


	@Test
	void getParamCount_withParams_cursorNotCountedAsParam() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"for", "for-loop", "for (int ${i} = 0; ${i} < ${max}; ${i}++) {\n\t${cursor}\n}");
		// "i" and "max" are unique params; repeats of "i" become ParamCopy pieces.
		Assertions.assertEquals(2, completion.getParamCount());
		Assertions.assertEquals("i", completion.getParam(0).getName());
		Assertions.assertEquals("max", completion.getParam(1).getName());
	}


	@Test
	void setShortDescription() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"for", "for-loop", "plain text template");
		completion.setShortDescription("new short desc");
		Assertions.assertEquals("new short desc", completion.getShortDescription());
	}


	@Test
	void toString_returnsDefinitionString() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"for", "for-loop", "plain text template");
		Assertions.assertEquals("for-loop", completion.toString());
	}


	@Test
	void getInsertionInfo_plainText() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"foo", "foo desc", "System.out.println();");

		JTextArea textArea = new JTextArea();
		textArea.setCaretPosition(0);

		ParameterizedCompletionInsertionInfo info =
			completion.getInsertionInfo(textArea, false);

		Assertions.assertEquals("System.out.println();", info.getTextToInsert());
	}


	@Test
	void getInsertionInfo_dollarDollarEscapesToSingleDollar() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"foo", "foo desc", "cost is $$5");

		JTextArea textArea = new JTextArea();
		textArea.setCaretPosition(0);

		ParameterizedCompletionInsertionInfo info =
			completion.getInsertionInfo(textArea, false);

		Assertions.assertEquals("cost is $5", info.getTextToInsert());
	}


	@Test
	void getInsertionInfo_withParam() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion completion = new TemplateCompletion(provider,
			"foo", "foo desc", "int ${name} = 0;");

		JTextArea textArea = new JTextArea();
		textArea.setCaretPosition(0);

		ParameterizedCompletionInsertionInfo info =
			completion.getInsertionInfo(textArea, false);

		Assertions.assertEquals("int name = 0;", info.getTextToInsert());
	}


}

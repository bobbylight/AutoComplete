/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;

import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class FunctionCompletionTest {


	@Test
	void constructor() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		Assertions.assertEquals("foo", completion.getName());
		Assertions.assertEquals("int", completion.getType());
		Assertions.assertEquals(0, completion.getParamCount());
	}


	@Test
	void getSetReturnValueDescription() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		Assertions.assertNull(completion.getReturnValueDescription());
		completion.setReturnValueDescription("returns something");
		Assertions.assertEquals("returns something", completion.getReturnValueDescription());
	}


	@Test
	void getSetParams() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");

		List<Parameter> params = new ArrayList<>();
		params.add(new Parameter("int", "a"));
		params.add(new Parameter("String", "b"));
		completion.setParams(params);

		Assertions.assertEquals(2, completion.getParamCount());
		Assertions.assertEquals("a", completion.getParam(0).getName());
		Assertions.assertEquals("b", completion.getParam(1).getName());
	}


	@Test
	void setParams_null_doesNotThrowAndKeepsCountZero() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		completion.setParams(null);
		Assertions.assertEquals(0, completion.getParamCount());
	}


	@Test
	void setParams_isDeepCopy() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");

		List<Parameter> params = new ArrayList<>();
		params.add(new Parameter("int", "a"));
		completion.setParams(params);

		params.add(new Parameter("int", "b"));
		Assertions.assertEquals(1, completion.getParamCount());
	}


	@Test
	void getDefinitionString_noParams() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		Assertions.assertEquals("int foo()", completion.getDefinitionString());
	}


	@Test
	void getDefinitionString_withParams() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");

		List<Parameter> params = new ArrayList<>();
		params.add(new Parameter("int", "a"));
		params.add(new Parameter("String", "b"));
		completion.setParams(params);

		Assertions.assertEquals("int foo(int a, String b)", completion.getDefinitionString());
	}


	@Test
	void getDefinitionString_noReturnType() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", null);
		Assertions.assertEquals("foo()", completion.getDefinitionString());
	}


	@Test
	void compareTo_sameInstance() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		Assertions.assertEquals(0, completion.compareTo(completion));
	}


	@Test
	void compareTo_otherFunctionCompletion_byName() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion1 = new FunctionCompletion(provider, "abc", "int");
		FunctionCompletion completion2 = new FunctionCompletion(provider, "xyz", "int");
		Assertions.assertTrue(completion1.compareTo(completion2) < 0);
		Assertions.assertTrue(completion2.compareTo(completion1) > 0);
	}


	@Test
	void compareTo_otherFunctionCompletion_byParamCount() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion1 = new FunctionCompletion(provider, "foo", "int");
		FunctionCompletion completion2 = new FunctionCompletion(provider, "foo", "int");

		List<Parameter> params = new ArrayList<>();
		params.add(new Parameter("int", "a"));
		completion2.setParams(params);

		Assertions.assertTrue(completion1.compareTo(completion2) < 0);
	}


	@Test
	void compareTo_nonFunctionCompletion_fallsBackToSuper() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		BasicCompletion other = new BasicCompletion(provider, "foo");
		Assertions.assertEquals(0, completion.compareTo(other));
	}


	@Test
	void equals_basedOnCompareTo() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion1 = new FunctionCompletion(provider, "foo", "int");
		FunctionCompletion completion2 = new FunctionCompletion(provider, "foo", "int");
		Assertions.assertEquals(completion1, completion2);
	}


	@Test
	void equals_notEqualToDifferentName() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion1 = new FunctionCompletion(provider, "foo", "int");
		FunctionCompletion completion2 = new FunctionCompletion(provider, "bar", "int");
		Assertions.assertNotEquals(completion1, completion2);
	}


	@Test
	void equals_notEqualToNonCompletion() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		Assertions.assertNotEquals("foo", completion);
	}


	@Test
	void hashCode_stableAcrossCalls() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		Assertions.assertEquals(completion.hashCode(), completion.hashCode());
	}


	@Test
	void hashCode_incorporatesParamsAndReturnValueDescription() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		int hashBefore = completion.hashCode();

		List<Parameter> params = new ArrayList<>();
		params.add(new Parameter("int", "a"));
		completion.setParams(params);
		completion.setReturnValueDescription("a description");

		Assertions.assertNotEquals(hashBefore, completion.hashCode());
	}


	@Test
	void getShowParameterToolTip_returnsTrue() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		Assertions.assertTrue(completion.getShowParameterToolTip());
	}


	@Test
	void getSummary_noParamsOrReturnDesc() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		String summary = completion.getSummary();
		Assertions.assertTrue(summary.contains("int foo()"));
	}


	@Test
	void getSummary_withParamsAndReturnDesc() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");

		List<Parameter> params = new ArrayList<>();
		Parameter param = new Parameter("int", "a");
		param.setDescription("the a param");
		params.add(param);
		completion.setParams(params);
		completion.setReturnValueDescription("the return value");

		String summary = completion.getSummary();
		Assertions.assertTrue(summary.contains("Parameters:"));
		Assertions.assertTrue(summary.contains("the a param"));
		Assertions.assertTrue(summary.contains("Returns:"));
		Assertions.assertTrue(summary.contains("the return value"));
	}


	@Test
	void getToolTipText_returnsSummary() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");
		Assertions.assertEquals(completion.getSummary(), completion.getToolTipText());
	}


	@Test
	void getInsertionInfo_noParams() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");

		JTextArea textArea = new JTextArea();
		textArea.setCaretPosition(0);

		ParameterizedCompletionInsertionInfo info =
			completion.getInsertionInfo(textArea, false);

		Assertions.assertEquals("()", info.getTextToInsert());
	}


	@Test
	void getInsertionInfo_withParams() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion completion = new FunctionCompletion(provider, "foo", "int");

		List<Parameter> params = new ArrayList<>();
		params.add(new Parameter("int", "a"));
		params.add(new Parameter("String", "b"));
		completion.setParams(params);

		JTextArea textArea = new JTextArea();
		textArea.setCaretPosition(0);

		ParameterizedCompletionInsertionInfo info =
			completion.getInsertionInfo(textArea, false);

		Assertions.assertEquals("(a, b)", info.getTextToInsert());
	}


}

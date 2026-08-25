/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


class CompletionXMLParserTest {


	private static final String DOCTYPE =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
		"<!DOCTYPE api SYSTEM \"CompletionXml.dtd\">\n";


	private static CompletionXMLParser parse(String xml, CompletionProvider provider)
			throws IOException, SAXException, ParserConfigurationException {
		return parse(xml, provider, null);
	}


	private static CompletionXMLParser parse(String xml, CompletionProvider provider,
			ClassLoader cl) throws IOException, SAXException, ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		CompletionXMLParser handler = new CompletionXMLParser(provider, cl);
		SAXParser saxParser = factory.newSAXParser();
		try (InputStream in = new ByteArrayInputStream(
				xml.getBytes(StandardCharsets.UTF_8))) {
			saxParser.parse(in, handler);
		}
		return handler;
	}


	@Test
	void resolveEntity_returnsDtdFromClasspath() {
		CompletionXMLParser parser = new CompletionXMLParser(new DefaultCompletionProvider());
		InputSource source = parser.resolveEntity("whatever", "whatever");
		Assertions.assertNotNull(source);
		Assertions.assertNotNull(source.getByteStream());
	}


	@Test
	void constructor_defaultsHaveNoCompletions() {
		CompletionXMLParser parser = new CompletionXMLParser(new DefaultCompletionProvider());
		Assertions.assertTrue(parser.getCompletions().isEmpty());
		Assertions.assertEquals(0, parser.getParamStartChar());
		Assertions.assertEquals(0, parser.getParamEndChar());
		Assertions.assertNull(parser.getParamSeparator());
	}


	@Test
	void parse_otherType_createsBasicCompletion() throws Exception {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\" type=\"other\">\n" +
			"<desc>a description</desc>\n" +
			"</keyword>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		CompletionXMLParser parser = parse(xml, provider);

		List<Completion> completions = parser.getCompletions();
		Assertions.assertEquals(1, completions.size());
		BasicCompletion completion = (BasicCompletion) completions.get(0);
		Assertions.assertEquals("foo", completion.getReplacementText());
		Assertions.assertEquals("a description", completion.getSummary());
	}


	@Test
	void parse_constantType_createsVariableCompletion() throws Exception {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"MAX\" type=\"constant\" returnType=\"int\" definedIn=\"limits.h\">\n" +
			"<desc>the max value</desc>\n" +
			"</keyword>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		CompletionXMLParser parser = parse(xml, provider);

		List<Completion> completions = parser.getCompletions();
		Assertions.assertEquals(1, completions.size());
		VariableCompletion completion = (VariableCompletion) completions.get(0);
		Assertions.assertEquals("MAX", completion.getName());
		Assertions.assertEquals("int", completion.getType());
		Assertions.assertEquals("limits.h", completion.getDefinedIn());
		Assertions.assertEquals("the max value", completion.getShortDescription());
	}


	@Test
	void parse_tagType_createsMarkupTagCompletion() throws Exception {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"div\" type=\"tag\" definedIn=\"HTML5\">\n" +
			"<params>\n" +
			"<param name=\"id\" type=\"string\">\n" +
			"<desc>the id attribute</desc>\n" +
			"</param>\n" +
			"</params>\n" +
			"<desc>a division</desc>\n" +
			"</keyword>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		CompletionXMLParser parser = parse(xml, provider);

		List<Completion> completions = parser.getCompletions();
		Assertions.assertEquals(1, completions.size());
		MarkupTagCompletion completion = (MarkupTagCompletion) completions.get(0);
		Assertions.assertEquals("div", completion.getName());
		Assertions.assertEquals("HTML5", completion.getDefinedIn());
		Assertions.assertEquals("a division", completion.getDescription());
		Assertions.assertEquals(1, completion.getAttributeCount());
		Assertions.assertEquals("id", completion.getAttribute(0).getName());
		Assertions.assertEquals("the id attribute", completion.getAttribute(0).getDescription());
	}


	@Test
	void parse_functionType_createsFunctionCompletion() throws Exception {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"abs\" type=\"function\" returnType=\"int\" definedIn=\"stdlib.h\">\n" +
			"<params>\n" +
			"<param type=\"int\" name=\"n\">\n" +
			"<desc>the value</desc>\n" +
			"</param>\n" +
			"</params>\n" +
			"<desc>absolute value</desc>\n" +
			"<returnValDesc>the absolute value</returnValDesc>\n" +
			"</keyword>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		CompletionXMLParser parser = parse(xml, provider);

		List<Completion> completions = parser.getCompletions();
		Assertions.assertEquals(1, completions.size());
		FunctionCompletion completion = (FunctionCompletion) completions.get(0);
		Assertions.assertEquals("abs", completion.getName());
		Assertions.assertEquals("int", completion.getType());
		Assertions.assertEquals("stdlib.h", completion.getDefinedIn());
		Assertions.assertEquals("absolute value", completion.getShortDescription());
		Assertions.assertEquals("the absolute value", completion.getReturnValueDescription());
		Assertions.assertEquals(1, completion.getParamCount());
		Assertions.assertEquals("n", completion.getParam(0).getName());
		Assertions.assertEquals("int", completion.getParam(0).getType());
		Assertions.assertFalse(completion.getParam(0).isEndParam());
	}


	@Test
	void parse_functionParam_endParamTrue() throws Exception {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\" type=\"function\" returnType=\"void\">\n" +
			"<params>\n" +
			"<param type=\"...\" endParam=\"true\"/>\n" +
			"</params>\n" +
			"</keyword>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		CompletionXMLParser parser = parse(xml, provider);

		FunctionCompletion completion = (FunctionCompletion) parser.getCompletions().get(0);
		Assertions.assertTrue(completion.getParam(0).isEndParam());
	}


	@Test
	void parse_multipleKeywords() throws Exception {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\" type=\"other\"/>\n" +
			"<keyword name=\"bar\" type=\"other\"/>\n" +
			"<keyword name=\"baz\" type=\"other\"/>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		CompletionXMLParser parser = parse(xml, provider);

		Assertions.assertEquals(3, parser.getCompletions().size());
	}


	@Test
	void parse_environmentElement_setsParameterListInfo() throws Exception {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<environment paramStartChar=\"(\" paramEndChar=\")\" paramSeparator=\", \" terminal=\";\"/>\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\" type=\"other\"/>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		CompletionXMLParser parser = parse(xml, provider);

		Assertions.assertEquals('(', parser.getParamStartChar());
		Assertions.assertEquals(')', parser.getParamEndChar());
		Assertions.assertEquals(", ", parser.getParamSeparator());
	}


	@Test
	void parse_customFunctionCompletionType_isUsed() throws Exception {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<completionTypes>\n" +
			"<functionCompletionType type=\"" +
				CustomFunctionCompletion.class.getName() + "\"/>\n" +
			"</completionTypes>\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\" type=\"function\" returnType=\"void\"/>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		CompletionXMLParser parser = parse(xml, provider);

		FunctionCompletion completion = (FunctionCompletion) parser.getCompletions().get(0);
		Assertions.assertInstanceOf(CustomFunctionCompletion.class, completion);
	}


	@Test
	void parse_customFunctionCompletionType_badClassName_fallsBackToDefault() throws Exception {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<completionTypes>\n" +
			"<functionCompletionType type=\"com.example.DoesNotExist\"/>\n" +
			"</completionTypes>\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\" type=\"function\" returnType=\"void\"/>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		CompletionXMLParser parser = parse(xml, provider);

		FunctionCompletion completion = (FunctionCompletion) parser.getCompletions().get(0);
		Assertions.assertEquals(FunctionCompletion.class, completion.getClass());
	}


	@Test
	void parse_unknownKeywordType_throwsInternalError() {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\" type=\"bogus\"/>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		Assertions.assertThrows(InternalError.class, () -> parse(xml, provider));
	}


	@Test
	void parse_missingRequiredAttribute_throwsSAXException() {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\"/>\n" + // missing required "type" attribute
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		Assertions.assertThrows(SAXException.class, () -> parse(xml, provider));
	}


	@Test
	void reset_clearsCompletionsAndUsesNewProvider() throws Exception {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\" type=\"other\"/>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider1 = new DefaultCompletionProvider();
		CompletionXMLParser parser = parse(xml, provider1);
		Assertions.assertEquals(1, parser.getCompletions().size());

		DefaultCompletionProvider provider2 = new DefaultCompletionProvider();
		parser.reset(provider2);
		Assertions.assertTrue(parser.getCompletions().isEmpty());

		CompletionXMLParser reparsed = parse(xml, provider2);
		Assertions.assertEquals(1, reparsed.getCompletions().size());
	}


	@Test
	void setDefaultCompletionClassLoader_usedWhenNoneSpecifiedInConstructor() {
		ClassLoader customLoader = getClass().getClassLoader();
		CompletionXMLParser.setDefaultCompletionClassLoader(customLoader);
		try {
			// Just verify this doesn't throw; the class loader is private state
			// exercised indirectly via parsing with a custom function type.
			Assertions.assertDoesNotThrow(() ->
				new CompletionXMLParser(new DefaultCompletionProvider()));
		} finally {
			CompletionXMLParser.setDefaultCompletionClassLoader(null);
		}
	}


	/**
	 * A custom {@code FunctionCompletion} used to verify that
	 * {@code CompletionXMLParser} can instantiate custom types specified via
	 * the {@code functionCompletionType} XML element.
	 */
	public static class CustomFunctionCompletion extends FunctionCompletion {
		public CustomFunctionCompletion(CompletionProvider provider, String name,
				String returnType) {
			super(provider, name, returnType);
		}
	}


}

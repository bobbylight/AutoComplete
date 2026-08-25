/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;


@ExtendWith(SwingRunnerExtension.class)
class DefaultCompletionProviderTest {


	private static final String DOCTYPE =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
		"<!DOCTYPE api SYSTEM \"CompletionXml.dtd\">\n";


	@TempDir
	private Path tempDir;


	@Test
	void constructor_noArg_hasNoCompletions() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		JTextArea textArea = new JTextArea("foo");
		textArea.setCaretPosition(3);
		Assertions.assertTrue(provider.getCompletions(textArea).isEmpty());
	}


	@Test
	void constructor_wordArray_addsWordCompletions() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider(
			new String[] { "foo", "bar" });
		JTextArea textArea = new JTextArea("f");
		textArea.setCaretPosition(1);
		List<Completion> completions = provider.getCompletions(textArea);
		Assertions.assertEquals(1, completions.size());
		Assertions.assertEquals("foo", completions.get(0).getReplacementText());
	}


	@Test
	void constructor_nullWordArray_hasNoCompletions() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider(null);
		JTextArea textArea = new JTextArea("foo");
		textArea.setCaretPosition(3);
		Assertions.assertTrue(provider.getCompletions(textArea).isEmpty());
	}


	@Test
	void getAlreadyEnteredText_returnsWordCharsBeforeCaret() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		JTextArea textArea = new JTextArea("foo bar");
		textArea.setCaretPosition(7); // end of "bar"
		Assertions.assertEquals("bar", provider.getAlreadyEnteredText(textArea));
	}


	@Test
	void getAlreadyEnteredText_stopsAtNonWordChar() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		JTextArea textArea = new JTextArea("foo.bar");
		textArea.setCaretPosition(7); // end of "bar"
		Assertions.assertEquals("bar", provider.getAlreadyEnteredText(textArea));
	}


	@Test
	void getAlreadyEnteredText_includesUnderscores() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		JTextArea textArea = new JTextArea("my_var");
		textArea.setCaretPosition(6);
		Assertions.assertEquals("my_var", provider.getAlreadyEnteredText(textArea));
	}


	@Test
	void getAlreadyEnteredText_atStartOfLine_returnsEmptyString() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		JTextArea textArea = new JTextArea("foo");
		textArea.setCaretPosition(0);
		Assertions.assertEquals("", provider.getAlreadyEnteredText(textArea));
	}


	@Test
	void isValidChar_lettersDigitsAndUnderscoreAreValid() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		JTextArea textArea = new JTextArea("a1_ .");
		// Indirectly verified via getAlreadyEnteredText, since isValidChar is protected.
		textArea.setCaretPosition(3); // after "a1_"
		Assertions.assertEquals("a1_", provider.getAlreadyEnteredText(textArea));
	}


	@Test
	void getCompletionsAt_returnsMatchingCompletions() throws BadLocationException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		provider.addCompletion(completion);

		JTextArea textArea = new JTextArea("foo bar");
		textArea.setSize(300, 100);

		Point p = pointForOffset(textArea, 1); // inside "foo"
		List<Completion> results = provider.getCompletionsAt(textArea, p);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(1, results.size());
		Assertions.assertSame(completion, results.get(0));
	}


	@Test
	void getCompletionsAt_noMatch_returnsNull() throws BadLocationException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));

		JTextArea textArea = new JTextArea("foo bar");
		textArea.setSize(300, 100);

		Point p = pointForOffset(textArea, 5); // inside "bar", no completion defined
		Assertions.assertNull(provider.getCompletionsAt(textArea, p));
	}


	// NOTE: getCompletionsAt()'s "offset < 0 || offset >= document length" branch
	// isn't exercised here, since JTextArea's viewToModel2D() always clamps
	// out-of-bounds points to the nearest valid offset rather than returning
	// one outside the document.


	@Test
	void getCompletionsAt_repeatedCallsAtSameOffset_returnCachedList()
			throws BadLocationException {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new BasicCompletion(provider, "foo"));

		JTextArea textArea = new JTextArea("foo bar");
		textArea.setSize(300, 100);
		Point p = pointForOffset(textArea, 1);

		List<Completion> first = provider.getCompletionsAt(textArea, p);
		List<Completion> second = provider.getCompletionsAt(textArea, p);

		Assertions.assertSame(first, second);
	}


	@Test
	void getParameterizedCompletions_parameterListNotConfigured_returnsNull() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		JTextArea textArea = new JTextArea("foo(");
		textArea.setCaretPosition(4);
		Assertions.assertNull(provider.getParameterizedCompletions(textArea));
	}


	@Test
	void getParameterizedCompletions_notEnoughCharsOnLine_returnsNull() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		JTextArea textArea = new JTextArea("(");
		textArea.setCaretPosition(1);
		Assertions.assertNull(provider.getParameterizedCompletions(textArea));
	}


	@Test
	void getParameterizedCompletions_matchingFunctionCompletion_isReturned() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion fc = new FunctionCompletion(provider, "foo", "void");
		provider.addCompletion(fc);

		JTextArea textArea = new JTextArea("foo(");
		textArea.setCaretPosition(4); // right after '('

		List<ParameterizedCompletion> results = provider.getParameterizedCompletions(textArea);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(1, results.size());
		Assertions.assertSame(fc, results.get(0));
	}


	@Test
	void getParameterizedCompletions_matchingNonParameterizedCompletion_returnsNull() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		provider.addCompletion(new BasicCompletion(provider, "foo"));

		JTextArea textArea = new JTextArea("foo(");
		textArea.setCaretPosition(4);

		Assertions.assertNull(provider.getParameterizedCompletions(textArea));
	}


	@Test
	void loadFromXML_inputStream_addsCompletionsAndParamListSettings() throws IOException {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<environment paramStartChar=\"(\" paramEndChar=\")\" paramSeparator=\", \" terminal=\";\"/>\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\" type=\"other\">\n" +
			"<desc>a description</desc>\n" +
			"</keyword>\n" +
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.loadFromXML(
			new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

		JTextArea textArea = new JTextArea("f");
		textArea.setCaretPosition(1);
		List<Completion> completions = provider.getCompletions(textArea);
		Assertions.assertEquals(1, completions.size());
		Assertions.assertEquals("foo", completions.get(0).getReplacementText());

		Assertions.assertEquals('(', provider.getParameterListStart());
		Assertions.assertEquals(')', provider.getParameterListEnd());
		Assertions.assertEquals(", ", provider.getParameterListSeparator());
	}


	@Test
	void loadFromXML_file_addsCompletions() throws IOException {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"bar\" type=\"other\"/>\n" +
			"</keywords>\n" +
			"</api>\n";

		Path file = tempDir.resolve("test.xml");
		Files.writeString(file, xml, StandardCharsets.UTF_8);

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.loadFromXML(file.toFile());

		JTextArea textArea = new JTextArea("b");
		textArea.setCaretPosition(1);
		List<Completion> completions = provider.getCompletions(textArea);
		Assertions.assertEquals(1, completions.size());
		Assertions.assertEquals("bar", completions.get(0).getReplacementText());
	}


	@Test
	void loadFromXML_resourceString_fallsBackToFileSystemPath() throws IOException {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"baz\" type=\"other\"/>\n" +
			"</keywords>\n" +
			"</api>\n";

		Path file = tempDir.resolve("test2.xml");
		Files.writeString(file, xml, StandardCharsets.UTF_8);

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.loadFromXML(file.toAbsolutePath().toString());

		JTextArea textArea = new JTextArea("b");
		textArea.setCaretPosition(1);
		List<Completion> completions = provider.getCompletions(textArea);
		Assertions.assertEquals(1, completions.size());
		Assertions.assertEquals("baz", completions.get(0).getReplacementText());
	}


	@Test
	void loadFromXML_resourceString_noSuchResource_throwsIOException() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		Assertions.assertThrows(IOException.class,
			() -> provider.loadFromXML("this/does/not/exist.xml"));
	}


	@Test
	void loadFromXML_malformedXml_throwsIOException() {
		String xml = DOCTYPE +
			"<api language=\"Test\">\n" +
			"<keywords>\n" +
			"<keyword name=\"foo\"/>\n" + // missing required "type" attribute
			"</keywords>\n" +
			"</api>\n";

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		Assertions.assertThrows(IOException.class, () -> provider.loadFromXML(
			new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
	}


	private static Point pointForOffset(JTextArea textArea, int offset)
			throws BadLocationException {
		Rectangle2D r = textArea.modelToView2D(offset);
		return new Point((int) r.getCenterX(), (int) r.getCenterY());
	}


}

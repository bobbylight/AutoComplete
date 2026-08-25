/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 * Tests {@link LanguageAwareCompletionProvider} using a real
 * {@code RSyntaxTextArea} configured for Java, so token-type-based provider
 * selection is exercised against a real tokenizer rather than a mock.  The
 * sample text and caret offsets below were chosen by inspecting the real
 * Java {@code TokenMaker}'s output for this exact text, since token
 * boundaries aren't obvious from the source text alone.
 */
@ExtendWith(SwingRunnerExtension.class)
class LanguageAwareCompletionProviderTest {

	/*
	 * Token layout for SAMPLE_TEXT (0-indexed offsets), as reported by the
	 * real Java TokenMaker:
	 *
	 *   offs=0   RESERVED_WORD    "class"
	 *   offs=14  COMMENT_EOL      "// line comment"     (line ends at 29)
	 *   offs=32  COMMENT_MULTILINE "/* multi"            (spans lines 2-3)
	 *   offs=56  COMMENT_DOCUMENTATION "/** doc comment *\/" (line ends at 74)
	 *   offs=88  LITERAL_STRING_DOUBLE_QUOTE "\"hello\""
	 *   offs=103 IDENTIFIER       "fo"
	 */
	private static final String SAMPLE_TEXT =
		"class Foo {\n" +
		"  // line comment\n" +
		"  /* multi\n" +
		"     line */\n" +
		"  /** doc comment */\n" +
		"  String s = \"hello\";\n" +
		"  int fo = 1;\n" +
		"}\n";

	private static final int OFFS_RESERVED_WORD_MID = 2;   // inside "class"
	private static final int OFFS_TOKEN_START = 0;          // start of "class"
	private static final int OFFS_LINE_COMMENT_MID = 20;    // inside "// line comment"
	private static final int OFFS_LINE_COMMENT_EOL = 29;    // end of that line (curToken == null)
	private static final int OFFS_MULTILINE_COMMENT_MID = 35;
	private static final int OFFS_DOC_COMMENT_MID = 60;
	private static final int OFFS_DOC_COMMENT_EOL = 74;      // end of doc comment line
	private static final int OFFS_STRING_MID = 90;           // inside "hello"
	private static final int OFFS_IDENTIFIER_MID = 104;      // inside "fo"


	private static RSyntaxTextArea createTextArea() {
		RSyntaxTextArea rsta = new RSyntaxTextArea();
		rsta.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		rsta.setText(SAMPLE_TEXT);
		rsta.setSize(600, 400);
		return rsta;
	}


	private static LanguageAwareCompletionProvider createFullyConfiguredProvider(
			DefaultCompletionProvider defaultProvider) {
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(defaultProvider);
		provider.setStringCompletionProvider(new DefaultCompletionProvider());
		provider.setCommentCompletionProvider(new DefaultCompletionProvider());
		provider.setDocCommentCompletionProvider(new DefaultCompletionProvider());
		return provider;
	}


	@Test
	void constructor_setsDefaultProvider() {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(defaultProvider);
		Assertions.assertSame(defaultProvider, provider.getDefaultCompletionProvider());
	}


	@Test
	void constructor_nullDefaultProvider_throwsNullPointerException() {
		Assertions.assertThrows(NullPointerException.class,
			() -> new LanguageAwareCompletionProvider(null));
	}


	@Test
	void protectedConstructor_subclassCanDeferSettingDefaultProvider() {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		LanguageAwareCompletionProvider provider = new LanguageAwareCompletionProvider() {
			{
				setDefaultCompletionProvider(defaultProvider);
			}
		};
		Assertions.assertSame(defaultProvider, provider.getDefaultCompletionProvider());
	}


	@Test
	void setDefaultCompletionProvider_null_throwsNullPointerException() {
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(new DefaultCompletionProvider());
		Assertions.assertThrows(NullPointerException.class,
			() -> provider.setDefaultCompletionProvider(null));
	}


	@Test
	void clearParameterizedCompletionParams_alwaysThrows() {
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(new DefaultCompletionProvider());
		Assertions.assertThrows(UnsupportedOperationException.class,
			provider::clearParameterizedCompletionParams);
	}


	@Test
	void setParameterizedCompletionParams_alwaysThrows() {
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(new DefaultCompletionProvider());
		Assertions.assertThrows(UnsupportedOperationException.class,
			() -> provider.setParameterizedCompletionParams('(', ", ", ')'));
	}


	@Test
	void parameterListGetters_delegateToDefaultProvider() {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		defaultProvider.setParameterizedCompletionParams('(', ", ", ')');
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(defaultProvider);

		Assertions.assertEquals('(', provider.getParameterListStart());
		Assertions.assertEquals(')', provider.getParameterListEnd());
		Assertions.assertEquals(", ", provider.getParameterListSeparator());
	}


	@Test
	void getSetCommentCompletionProvider() {
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(new DefaultCompletionProvider());
		Assertions.assertNull(provider.getCommentCompletionProvider());
		DefaultCompletionProvider commentProvider = new DefaultCompletionProvider();
		provider.setCommentCompletionProvider(commentProvider);
		Assertions.assertSame(commentProvider, provider.getCommentCompletionProvider());
	}


	@Test
	void getSetDocCommentCompletionProvider() {
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(new DefaultCompletionProvider());
		Assertions.assertNull(provider.getDocCommentCompletionProvider());
		DefaultCompletionProvider docProvider = new DefaultCompletionProvider();
		provider.setDocCommentCompletionProvider(docProvider);
		Assertions.assertSame(docProvider, provider.getDocCommentCompletionProvider());
	}


	@Test
	void getSetStringCompletionProvider() {
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(new DefaultCompletionProvider());
		Assertions.assertNull(provider.getStringCompletionProvider());
		DefaultCompletionProvider stringProvider = new DefaultCompletionProvider();
		provider.setStringCompletionProvider(stringProvider);
		Assertions.assertSame(stringProvider, provider.getStringCompletionProvider());
	}


	@Test
	void getAlreadyEnteredText_nonRSyntaxTextArea_returnsEmptyString() {
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(new DefaultCompletionProvider());
		JTextArea plainTextArea = new JTextArea("foo");
		plainTextArea.setCaretPosition(3);
		Assertions.assertEquals("", provider.getAlreadyEnteredText(plainTextArea));
	}


	@Test
	void getAlreadyEnteredText_codeRegion_delegatesToDefaultProvider() {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(defaultProvider);

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_IDENTIFIER_MID);

		String expected = defaultProvider.getAlreadyEnteredText(rsta);
		Assertions.assertEquals(expected, provider.getAlreadyEnteredText(rsta));
		Assertions.assertFalse(expected.isEmpty());
	}


	@Test
	void getCompletionsAt_noDefaultProvider_returnsNull() {
		LanguageAwareCompletionProvider provider = new LanguageAwareCompletionProvider() {
			// Deliberately never sets a default provider.
		};
		RSyntaxTextArea rsta = createTextArea();
		Assertions.assertNull(provider.getCompletionsAt(rsta, new Point(0, 0)));
	}


	@Test
	void getCompletionsAt_delegatesToDefaultProviderRegardlessOfTokenType()
			throws BadLocationException {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(defaultProvider, "fo");
		defaultProvider.addCompletion(completion);
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);

		RSyntaxTextArea rsta = createTextArea();
		Point p = pointForOffset(rsta, OFFS_IDENTIFIER_MID);

		List<Completion> results = provider.getCompletionsAt(rsta, p);
		Assertions.assertNotNull(results);
		Assertions.assertEquals(1, results.size());
		Assertions.assertSame(completion, results.get(0));
	}


	@Test
	void getCompletionsImpl_nonRSyntaxTextArea_returnsEmptyList() {
		LanguageAwareCompletionProvider provider =
			new LanguageAwareCompletionProvider(new DefaultCompletionProvider());
		JTextArea plainTextArea = new JTextArea("foo");
		plainTextArea.setCaretPosition(3);
		Assertions.assertTrue(provider.getCompletions(plainTextArea).isEmpty());
	}


	@Test
	void getCompletionsImpl_codeRegion_usesDefaultProvider() {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		defaultProvider.addCompletion(new BasicCompletion(defaultProvider, "foo"));
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_IDENTIFIER_MID);

		List<Completion> results = provider.getCompletions(rsta);
		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals("foo", results.get(0).getReplacementText());
	}


	@Test
	void getCompletionsImpl_stringRegion_usesStringProvider() {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		defaultProvider.addCompletion(new BasicCompletion(defaultProvider, "shouldNotMatch"));
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);
		DefaultCompletionProvider stringProvider =
			(DefaultCompletionProvider) provider.getStringCompletionProvider();
		stringProvider.addCompletion(new BasicCompletion(stringProvider, "hello"));

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_STRING_MID);

		List<Completion> results = provider.getCompletions(rsta);
		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals("hello", results.get(0).getReplacementText());
	}


	@Test
	void getCompletionsImpl_lineCommentRegion_usesCommentProvider() {
		LanguageAwareCompletionProvider provider =
			createFullyConfiguredProvider(new DefaultCompletionProvider());
		DefaultCompletionProvider commentProvider =
			(DefaultCompletionProvider) provider.getCommentCompletionProvider();
		commentProvider.addCompletion(new BasicCompletion(commentProvider, "line"));

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_LINE_COMMENT_MID);

		List<Completion> results = provider.getCompletions(rsta);
		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals("line", results.get(0).getReplacementText());
	}


	@Test
	void getCompletionsImpl_endOfLineCommentToken_usesCommentProvider() {
		// curToken is null here (end of line); getProviderFor() must fall back
		// to doc.getLastTokenTypeOnLine() to figure out we're still in a comment.
		LanguageAwareCompletionProvider provider =
			createFullyConfiguredProvider(new DefaultCompletionProvider());
		DefaultCompletionProvider commentProvider =
			(DefaultCompletionProvider) provider.getCommentCompletionProvider();
		// At the end of the line, the already-entered text is "comment"
		// (the last word before the caret), not "line".
		commentProvider.addCompletion(new BasicCompletion(commentProvider, "comment"));

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_LINE_COMMENT_EOL);

		List<Completion> results = provider.getCompletions(rsta);
		Assertions.assertEquals(1, results.size());
	}


	@Test
	void getCompletionsImpl_multilineCommentRegion_usesCommentProvider() {
		LanguageAwareCompletionProvider provider =
			createFullyConfiguredProvider(new DefaultCompletionProvider());
		DefaultCompletionProvider commentProvider =
			(DefaultCompletionProvider) provider.getCommentCompletionProvider();
		commentProvider.addCompletion(new BasicCompletion(commentProvider, "multi"));

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_MULTILINE_COMMENT_MID);

		List<Completion> results = provider.getCompletions(rsta);
		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals("multi", results.get(0).getReplacementText());
	}


	@Test
	void getCompletionsImpl_docCommentRegion_usesDocCommentProvider() {
		LanguageAwareCompletionProvider provider =
			createFullyConfiguredProvider(new DefaultCompletionProvider());
		DefaultCompletionProvider docProvider =
			(DefaultCompletionProvider) provider.getDocCommentCompletionProvider();
		docProvider.addCompletion(new BasicCompletion(docProvider, "doc"));

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_DOC_COMMENT_MID);

		List<Completion> results = provider.getCompletions(rsta);
		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals("doc", results.get(0).getReplacementText());
	}


	@Test
	void getCompletionsImpl_endOfLineDocComment_usesDocCommentProvider() {
		LanguageAwareCompletionProvider provider =
			createFullyConfiguredProvider(new DefaultCompletionProvider());
		DefaultCompletionProvider docProvider =
			(DefaultCompletionProvider) provider.getDocCommentCompletionProvider();
		docProvider.addCompletion(new BasicCompletion(docProvider, "doc"));

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_DOC_COMMENT_EOL);

		List<Completion> results = provider.getCompletions(rsta);
		Assertions.assertEquals(1, results.size());
	}


	@Test
	void getCompletionsImpl_unsupportedTokenType_returnsEmptyList() {
		// Caret is inside "class", a RESERVED_WORD - a token type
		// getProviderFor() doesn't know how to map to any provider.
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		defaultProvider.addCompletion(new BasicCompletion(defaultProvider, "class"));
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_RESERVED_WORD_MID);

		Assertions.assertTrue(provider.getCompletions(rsta).isEmpty());
	}


	@Test
	void getCompletionsImpl_startOfToken_usesDefaultProvider() {
		// dot == curToken.getOffset(): the "beginning of a new token" special case.
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		defaultProvider.addCompletion(new BasicCompletion(defaultProvider, "class"));
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_TOKEN_START);

		List<Completion> results = provider.getCompletions(rsta);
		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals("class", results.get(0).getReplacementText());
	}


	@Test
	void getParameterizedCompletions_codeRegion_delegatesToDefaultProvider() {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		defaultProvider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion fc = new FunctionCompletion(defaultProvider, "foo", "void");
		defaultProvider.addCompletion(fc);
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);

		RSyntaxTextArea rsta = new RSyntaxTextArea();
		rsta.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		rsta.setText("foo(");
		rsta.setCaretPosition(4); // right after '('

		List<ParameterizedCompletion> results = provider.getParameterizedCompletions(rsta);
		Assertions.assertNotNull(results);
		Assertions.assertEquals(1, results.size());
		Assertions.assertSame(fc, results.get(0));
	}


	@Test
	void getParameterizedCompletions_nonCodeRegion_returnsNull() {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		defaultProvider.setParameterizedCompletionParams('(', ", ", ')');
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_STRING_MID);

		Assertions.assertNull(provider.getParameterizedCompletions(rsta));
	}


	@Test
	void isAutoActivateOkay_codeRegion_delegatesToDefaultProvider() {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		defaultProvider.setAutoActivationRules(true, null);
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_IDENTIFIER_MID);

		Assertions.assertTrue(provider.isAutoActivateOkay(rsta, OFFS_IDENTIFIER_MID));
	}

	@Test
	void isAutoActivateOkay_unsupportedTokenType_returnsFalse() {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		defaultProvider.setAutoActivationRules(true, null);
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);

		RSyntaxTextArea rsta = createTextArea();
		rsta.setCaretPosition(OFFS_RESERVED_WORD_MID);

		Assertions.assertFalse(provider.isAutoActivateOkay(rsta, OFFS_RESERVED_WORD_MID));
	}


	@Test
	void getToolTipText_matchingCompletion_returnsItsToolTipText() throws BadLocationException {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		VariableCompletion completion = new VariableCompletion(defaultProvider, "fo", "int");
		defaultProvider.addCompletion(completion);
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);

		RSyntaxTextArea rsta = createTextArea();
		Point p = pointForOffset(rsta, OFFS_IDENTIFIER_MID);
		MouseEvent event = new MouseEvent(rsta, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),
			0, p.x, p.y, 0, false);

		Assertions.assertEquals(completion.getToolTipText(), provider.getToolTipText(rsta, event));
	}


	@Test
	void getToolTipText_noMatch_returnsNull() throws BadLocationException {
		DefaultCompletionProvider defaultProvider = new DefaultCompletionProvider();
		LanguageAwareCompletionProvider provider = createFullyConfiguredProvider(defaultProvider);

		RSyntaxTextArea rsta = createTextArea();
		Point p = pointForOffset(rsta, OFFS_IDENTIFIER_MID);
		MouseEvent event = new MouseEvent(rsta, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),
			0, p.x, p.y, 0, false);

		Assertions.assertNull(provider.getToolTipText(rsta, event));
	}


	private static Point pointForOffset(RSyntaxTextArea textArea, int offset)
			throws BadLocationException {
		Rectangle2D r = textArea.modelToView2D(offset);
		return new Point((int) r.getCenterX(), (int) r.getCenterY());
	}


}

/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class CompletionCellRendererTest {


	@AfterEach
	void tearDown() {
		CompletionCellRenderer.setAlternateBackground(null);
	}


	@Test
	void constructor_noArg_showsTypesByDefault() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		Assertions.assertTrue(renderer.getShowTypes());
		Assertions.assertNull(renderer.getDelegateRenderer());
	}


	@Test
	void constructor_withDelegate_setsDelegateRenderer() {
		DefaultListCellRenderer delegate = new DefaultListCellRenderer();
		CompletionCellRenderer renderer = new CompletionCellRenderer(delegate);
		Assertions.assertSame(delegate, renderer.getDelegateRenderer());
	}


	@Test
	void delegateToSubstanceRenderer_substanceNotOnClasspath_throws() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		Assertions.assertThrows(Exception.class, renderer::delegateToSubstanceRenderer);
	}


	@Test
	void getSetAlternateBackground_roundTrips() {
		Assertions.assertNull(CompletionCellRenderer.getAlternateBackground());
		Color color = new Color(1, 2, 3);
		CompletionCellRenderer.setAlternateBackground(color);
		Assertions.assertEquals(color, CompletionCellRenderer.getAlternateBackground());
	}


	@Test
	void getSetDelegateRenderer_roundTrips() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		DefaultListCellRenderer delegate = new DefaultListCellRenderer();
		renderer.setDelegateRenderer(delegate);
		Assertions.assertSame(delegate, renderer.getDelegateRenderer());
	}


	@Test
	void getSetDisplayFont_roundTrips() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		Assertions.assertNull(renderer.getDisplayFont());
		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);
		renderer.setDisplayFont(font);
		Assertions.assertEquals(font, renderer.getDisplayFont());
	}


	@Test
	void getSetShowTypes_roundTrips() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		renderer.setShowTypes(false);
		Assertions.assertFalse(renderer.getShowTypes());
		renderer.setShowTypes(true);
		Assertions.assertTrue(renderer.getShowTypes());
	}


	@Test
	void setParamColor_null_isIgnored() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion fc = new FunctionCompletion(provider, "foo", "int");
		fc.setParams(java.util.List.of(new FunctionCompletion.Parameter("int", "a")));

		renderer.setParamColor(null);

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, fc, 0, false, false);
		Assertions.assertTrue(renderer.getText().contains("#aa0077")
			|| renderer.getText().contains("color='#"));
	}


	@Test
	void setTypeColor_appliesGivenColorToTypeText() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		renderer.setTypeColor(new Color(0x123456));

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion vc = new VariableCompletion(provider, "myVar", "int");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, vc, 0, false, false);

		Assertions.assertTrue(renderer.getText().contains("#123456"));
	}


	@Test
	void setTypeColor_null_isIgnored() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		renderer.setTypeColor(null); // Should not throw or change default

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion vc = new VariableCompletion(provider, "myVar", "int");

		JList<Object> list = new JList<>();
		Assertions.assertDoesNotThrow(() ->
			renderer.getListCellRendererComponent(list, vc, 0, false, false));
	}


	@Test
	void getListCellRendererComponent_functionCompletion_rendersNameAndParams() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion fc = new FunctionCompletion(provider, "foo", "int");
		fc.setParams(java.util.List.of(new FunctionCompletion.Parameter("int", "a")));

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, fc, 0, false, false);

		String text = renderer.getText();
		Assertions.assertTrue(text.contains("foo("));
		Assertions.assertTrue(text.contains(">int</font> a)"));
		Assertions.assertTrue(text.contains(" : ")); // return type appended
	}


	@Test
	void getListCellRendererComponent_functionCompletion_showTypesFalse_omitsReturnType() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		renderer.setShowTypes(false);
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", ", ')');
		FunctionCompletion fc = new FunctionCompletion(provider, "foo", "int");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, fc, 0, false, false);

		Assertions.assertFalse(renderer.getText().contains(" : "));
	}


	@Test
	void getListCellRendererComponent_variableCompletion_rendersName() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		VariableCompletion vc = new VariableCompletion(provider, "myVar", "int");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, vc, 0, false, false);

		Assertions.assertTrue(renderer.getText().contains("myVar"));
	}


	@Test
	void getListCellRendererComponent_templateCompletion_rendersInputTextAndDescription() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		TemplateCompletion tc = new TemplateCompletion(provider, "for", "for-loop", "for (${i}) {}",
			"a for loop", "summary");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, tc, 0, false, false);

		Assertions.assertTrue(renderer.getText().contains("for"));
		Assertions.assertTrue(renderer.getText().contains("a for loop"));
	}


	@Test
	void getListCellRendererComponent_markupTagCompletion_rendersName() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		MarkupTagCompletion mtc = new MarkupTagCompletion(provider, "div");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, mtc, 0, false, false);

		Assertions.assertTrue(renderer.getText().contains("div"));
	}


	@Test
	void getListCellRendererComponent_basicCompletion_withShortDescription_rendersDescription() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion bc = new BasicCompletion(provider, "foo", "a short description");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, bc, 0, false, false);

		Assertions.assertTrue(renderer.getText().contains("foo"));
		Assertions.assertTrue(renderer.getText().contains("a short description"));
	}


	@Test
	void getListCellRendererComponent_basicCompletion_noShortDescription_rendersOnlyInputText() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion bc = new BasicCompletion(provider, "foo");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, bc, 0, false, false);

		Assertions.assertTrue(renderer.getText().contains("foo"));
		Assertions.assertFalse(renderer.getText().contains(" - "));
	}


	@Test
	void getListCellRendererComponent_customFontSet_overridesListFont() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 20);
		renderer.setDisplayFont(font);

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion bc = new BasicCompletion(provider, "foo");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, bc, 0, false, false);

		Assertions.assertEquals(font, renderer.getFont());
	}


	@Test
	void getListCellRendererComponent_oddIndexWithAlternateBackground_setsAlternateBackground() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		Color altBg = new Color(5, 6, 7);
		CompletionCellRenderer.setAlternateBackground(altBg);

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion bc = new BasicCompletion(provider, "foo");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, bc, 1, false, false);

		Assertions.assertEquals(altBg, renderer.getBackground());
	}


	@Test
	void getListCellRendererComponent_selected_doesNotUseAlternateBackground() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		Color altBg = new Color(5, 6, 7);
		CompletionCellRenderer.setAlternateBackground(altBg);

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion bc = new BasicCompletion(provider, "foo");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, bc, 1, true, false);

		Assertions.assertNotEquals(altBg, renderer.getBackground());
	}


	@Test
	void getListCellRendererComponent_withDelegate_returnsDelegateWithMatchingTextAndIcon() {
		DefaultListCellRenderer delegate = new DefaultListCellRenderer();
		CompletionCellRenderer renderer = new CompletionCellRenderer(delegate);

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion bc = new BasicCompletion(provider, "foo");

		JList<Object> list = new JList<>();
		java.awt.Component result = renderer.getListCellRendererComponent(list, bc, 0, false, false);

		Assertions.assertSame(delegate, result);
		Assertions.assertEquals(renderer.getText(), delegate.getText());
	}


	@Test
	void getEmptyIcon_isCachedAcrossCalls() throws ReflectiveOperationException {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		java.lang.reflect.Method m = CompletionCellRenderer.class.getDeclaredMethod("getEmptyIcon");
		m.setAccessible(true);
		Icon first = (Icon) m.invoke(renderer);
		Icon second = (Icon) m.invoke(renderer);
		Assertions.assertNotNull(first);
		Assertions.assertSame(first, second);
	}


	@Test
	void getIcon_classpathResourceFound_returnsIcon() throws ReflectiveOperationException {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		java.lang.reflect.Method m = CompletionCellRenderer.class.getDeclaredMethod("getIcon", String.class);
		m.setAccessible(true);
		Icon icon = (Icon) m.invoke(renderer, "arrow_left.png");
		Assertions.assertNotNull(icon);
	}


	@Test
	void setIconWithDefault_completionHasIcon_usesCompletionIcon() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		Icon icon = new EmptyIcon(8);
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion bc = new BasicCompletion(provider, "foo");
		bc.setIcon(icon);

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, bc, 0, false, false);

		Assertions.assertSame(icon, renderer.getIcon());
	}


	@Test
	void updateUI_withDelegate_doesNotThrow() {
		DefaultListCellRenderer delegate = new DefaultListCellRenderer();
		CompletionCellRenderer renderer = new CompletionCellRenderer(delegate);
		Assertions.assertDoesNotThrow(renderer::updateUI);
	}


	@Test
	void updateUI_withoutDelegate_doesNotThrow() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		Assertions.assertDoesNotThrow(renderer::updateUI);
	}


	@Test
	void paintComponent_noIcon_doesNotThrow() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion bc = new BasicCompletion(provider, "foo");

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, bc, 0, false, false);
		renderer.setSize(100, 20);

		BufferedImage image = new BufferedImage(100, 20, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		try {
			Assertions.assertDoesNotThrow(() -> renderer.paint(g));
		} finally {
			g.dispose();
		}
	}


	@Test
	void paintComponent_selectedWithIcon_doesNotThrow() {
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion bc = new BasicCompletion(provider, "foo");
		bc.setIcon(new EmptyIcon(16));

		JList<Object> list = new JList<>();
		renderer.getListCellRendererComponent(list, bc, 0, true, false);
		renderer.setSize(100, 20);

		BufferedImage image = new BufferedImage(100, 20, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		try {
			Assertions.assertDoesNotThrow(() -> renderer.paint(g));
		} finally {
			g.dispose();
		}
	}


}

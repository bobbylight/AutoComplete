/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JToolTip;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicEditorPaneUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTMLDocument;

import org.fife.ui.rsyntaxtextarea.HtmlUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


/**
 * Tests {@link TipUtil}.  Most of {@code TipUtil}'s behavior branches on the
 * current {@code LookAndFeel} and various {@code UIManager} defaults, so
 * these tests mock {@code UIManager} (with real methods as the default
 * answer, so only explicitly stubbed keys are overridden) to exercise each
 * branch deterministically, independent of whatever Look and Feel happens to
 * be installed on the machine running the tests.
 */
@ExtendWith(SwingRunnerExtension.class)
class TipUtilTest {


	private static LookAndFeel mockLookAndFeel(String name) {
		LookAndFeel laf = Mockito.mock(LookAndFeel.class);
		Mockito.when(laf.getName()).thenReturn(name);
		return laf;
	}


	private static JEditorPane createHtmlEditorPane() {
		JEditorPane pane = new JEditorPane();
		pane.setContentType("text/html");
		pane.setText("<html><body>Hi</body></html>");
		return pane;
	}


	// ---- getToolTipBackground() ----


	@Test
	void getToolTipBackground_nonNimbus_convertsColorUIResourceToPlainColor() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getColor("ToolTip.background"))
				.thenReturn(new ColorUIResource(Color.RED));

			Color result = TipUtil.getToolTipBackground();
			Assertions.assertEquals(Color.RED.getRGB(), result.getRGB());
			Assertions.assertFalse(result instanceof ColorUIResource);
		}
	}


	@Test
	void getToolTipBackground_nimbus_usesInfoColorRegardlessOfToolTipBackground() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Nimbus");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getColor("ToolTip.background")).thenReturn(Color.RED);
			ui.when(() -> UIManager.getColor("info")).thenReturn(new ColorUIResource(Color.GREEN));

			Color result = TipUtil.getToolTipBackground();
			Assertions.assertEquals(Color.GREEN.getRGB(), result.getRGB());
		}
	}


	@Test
	void getToolTipBackground_toolTipAndInfoBothNull_fallsBackToSystemColorInfo() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getColor("ToolTip.background")).thenReturn(null);
			ui.when(() -> UIManager.getColor("info")).thenReturn(null);

			Assertions.assertEquals(SystemColor.info, TipUtil.getToolTipBackground());
		}
	}


	// ---- getToolTipBorder() ----


	@Test
	void getToolTipBorder_nonNimbus_returnsToolTipBorder() {
		Border expected = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getBorder("ToolTip.border")).thenReturn(expected);

			Assertions.assertSame(expected, TipUtil.getToolTipBorder());
		}
	}


	@Test
	void getToolTipBorder_toolTipBorderNull_usesNimbusBorder() {
		Border nimbusBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getBorder("ToolTip.border")).thenReturn(null);
			ui.when(() -> UIManager.getBorder("nimbusBorder")).thenReturn(nimbusBorder);

			Assertions.assertSame(nimbusBorder, TipUtil.getToolTipBorder());
		}
	}


	@Test
	void getToolTipBorder_nimbus_usesNimbusBorderRegardlessOfToolTipBorder() {
		Border toolTipBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		Border nimbusBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Nimbus");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getBorder("ToolTip.border")).thenReturn(toolTipBorder);
			ui.when(() -> UIManager.getBorder("nimbusBorder")).thenReturn(nimbusBorder);

			Assertions.assertSame(nimbusBorder, TipUtil.getToolTipBorder());
		}
	}


	@Test
	void getToolTipBorder_bothNull_fallsBackToLineBorder() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getBorder("ToolTip.border")).thenReturn(null);
			ui.when(() -> UIManager.getBorder("nimbusBorder")).thenReturn(null);

			Border result = TipUtil.getToolTipBorder();
			Assertions.assertInstanceOf(LineBorder.class, result);
			Assertions.assertEquals(SystemColor.controlDkShadow, ((LineBorder) result).getLineColor());
		}
	}


	// ---- getToolTipHyperlinkForeground() ----


	@Test
	void getToolTipHyperlinkForeground_nonNimbus_darkForeground_returnsBlue() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getColor("ToolTip.foreground")).thenReturn(Color.BLACK);

			Assertions.assertEquals(Color.BLUE, TipUtil.getToolTipHyperlinkForeground());
		}
	}


	@Test
	void getToolTipHyperlinkForeground_nonNimbus_lightForeground_returnsLightHyperlinkColor() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getColor("ToolTip.foreground")).thenReturn(Color.WHITE);

			Assertions.assertEquals(Util.LIGHT_HYPERLINK_FG, TipUtil.getToolTipHyperlinkForeground());
		}
	}


	@Test
	void getToolTipHyperlinkForeground_nullToolTipForeground_fallsBackToJToolTipDefault() {
		// The method's fallback (new JToolTip().getForeground()) itself queries
		// "ToolTip.foreground" internally during UI installation, so the mocked
		// key must return something non-null on that second lookup too - we
		// don't care what, since we only care that the fallback path is taken
		// and its result correctly feeds into isLightForeground().
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getColor("ToolTip.foreground")).thenReturn(null, Color.BLACK);

			Assertions.assertEquals(Color.BLUE, TipUtil.getToolTipHyperlinkForeground());
		}
	}


	@Test
	void getToolTipHyperlinkForeground_nimbus_usesJToolTipDefaultRegardlessOfToolTipForeground() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Nimbus");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			// First call (the method's own) returns non-null RED, but Nimbus
			// forces the JToolTip fallback anyway; that fallback's own internal
			// lookup (second call) resolves to WHITE.
			ui.when(() -> UIManager.getColor("ToolTip.foreground")).thenReturn(Color.RED, Color.WHITE);

			Assertions.assertEquals(Util.LIGHT_HYPERLINK_FG, TipUtil.getToolTipHyperlinkForeground());
		}
	}


	// ---- tweakTipEditorPane() ----


	@Test
	void tweakTipEditorPane_makesPaneNonEditableWithPaddingAndVisibleSelection() {
		JEditorPane pane = createHtmlEditorPane();

		TipUtil.tweakTipEditorPane(pane);

		Assertions.assertFalse(pane.isEditable());
		Assertions.assertInstanceOf(EmptyBorder.class, pane.getBorder());
		Assertions.assertEquals(new java.awt.Insets(5, 5, 5, 5),
			((EmptyBorder) pane.getBorder()).getBorderInsets());
		Assertions.assertTrue(pane.getCaret().isSelectionVisible());
	}


	@Test
	void tweakTipEditorPane_foreground_usesToolTipForegroundWhenNonNull() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getColor("ToolTip.foreground")).thenReturn(Color.RED);

			JEditorPane pane = createHtmlEditorPane();
			TipUtil.tweakTipEditorPane(pane);

			Assertions.assertEquals(Color.RED, pane.getForeground());
		}
	}


	@Test
	void tweakTipEditorPane_foreground_fallsBackToLabelForegroundWhenToolTipForegroundNull() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			// 1st call is the pane's own foreground lookup (null -> triggers
			// the Label.foreground fallback); the 2nd is made internally by the
			// getToolTipHyperlinkForeground() call later in the same method, and
			// just needs to be non-null to avoid an unrelated NPE in JToolTip.
			ui.when(() -> UIManager.getColor("ToolTip.foreground")).thenReturn(null, Color.BLACK);
			ui.when(() -> UIManager.getColor("Label.foreground")).thenReturn(Color.GREEN);

			JEditorPane pane = createHtmlEditorPane();
			TipUtil.tweakTipEditorPane(pane);

			Assertions.assertEquals(Color.GREEN, pane.getForeground());
		}
	}


	@Test
	void tweakTipEditorPane_foreground_fallsBackToSystemColorTextTextWhenBothNull() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getColor("ToolTip.foreground")).thenReturn(null, Color.BLACK);
			ui.when(() -> UIManager.getColor("Label.foreground")).thenReturn(null);

			JEditorPane pane = createHtmlEditorPane();
			TipUtil.tweakTipEditorPane(pane);

			Assertions.assertEquals(SystemColor.textText, pane.getForeground());
		}
	}


	@Test
	void tweakTipEditorPane_background_usesToolTipBackground() {
		Color expectedBg = TipUtil.getToolTipBackground();

		JEditorPane pane = createHtmlEditorPane();
		TipUtil.tweakTipEditorPane(pane);

		Assertions.assertEquals(expectedBg, pane.getBackground());
	}


	@Test
	void tweakTipEditorPane_font_usesLabelFontWhenNonNull() {
		// tweakTipEditorPane() never calls textArea.setFont(); it only adds a
		// "body" CSS rule to the HTMLDocument's stylesheet reflecting the font,
		// so that's what must be inspected here, not pane.getFont().
		Font customFont = new Font("Monospaced", Font.BOLD, 20);
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getFont("Label.font")).thenReturn(customFont);

			JEditorPane pane = createHtmlEditorPane();
			TipUtil.tweakTipEditorPane(pane);

			AttributeSet bodyRule = ((HTMLDocument) pane.getDocument()).getStyleSheet().getRule("body");
			Assertions.assertEquals("Monospaced", bodyRule.getAttribute(CSS.Attribute.FONT_FAMILY).toString());
			Assertions.assertEquals("20pt", bodyRule.getAttribute(CSS.Attribute.FONT_SIZE).toString());
		}
	}


	@Test
	void tweakTipEditorPane_font_fallsBackToSansSerif12WhenLabelFontNull() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Metal");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);
			ui.when(() -> UIManager.getFont("Label.font")).thenReturn(null);

			JEditorPane pane = createHtmlEditorPane();
			TipUtil.tweakTipEditorPane(pane);

			AttributeSet bodyRule = ((HTMLDocument) pane.getDocument()).getStyleSheet().getRule("body");
			Assertions.assertEquals("SansSerif", bodyRule.getAttribute(CSS.Attribute.FONT_FAMILY).toString());
			Assertions.assertEquals("12pt", bodyRule.getAttribute(CSS.Attribute.FONT_SIZE).toString());
		}
	}


	@Test
	void tweakTipEditorPane_nimbus_swapsUiAndPreservesSelectionColors() {
		try (MockedStatic<UIManager> ui =
				Mockito.mockStatic(UIManager.class, Mockito.CALLS_REAL_METHODS)) {
			LookAndFeel laf = mockLookAndFeel("Nimbus");
			ui.when(UIManager::getLookAndFeel).thenReturn(laf);

			JEditorPane pane = createHtmlEditorPane();
			pane.setSelectionColor(Color.CYAN);
			pane.setSelectedTextColor(Color.MAGENTA);

			TipUtil.tweakTipEditorPane(pane);

			Assertions.assertInstanceOf(BasicEditorPaneUI.class, pane.getUI());
			Assertions.assertEquals(Color.CYAN, pane.getSelectionColor());
			Assertions.assertEquals(Color.MAGENTA, pane.getSelectedTextColor());
		}
	}


	@Test
	void tweakTipEditorPane_addsBodyAndLinkStyleRulesReflectingComputedColorsAndFont() {
		JEditorPane pane = createHtmlEditorPane();

		TipUtil.tweakTipEditorPane(pane);

		HTMLDocument doc = (HTMLDocument) pane.getDocument();

		AttributeSet bodyRule = doc.getStyleSheet().getRule("body");
		Assertions.assertNotNull(bodyRule);
		Assertions.assertEquals(HtmlUtil.getHexString(pane.getForeground()),
			bodyRule.getAttribute(CSS.Attribute.COLOR).toString());

		// tweakTipEditorPane() never calls textArea.setFont(); the "body" rule's
		// font instead mirrors "Label.font" (or its SansSerif/12 fallback), so
		// that's what must be compared against here, not pane.getFont().
		Font expectedFont = UIManager.getFont("Label.font");
		if (expectedFont == null) {
			expectedFont = new Font("SansSerif", Font.PLAIN, 12);
		}
		Assertions.assertEquals(expectedFont.getFamily(),
			bodyRule.getAttribute(CSS.Attribute.FONT_FAMILY).toString());

		AttributeSet linkRule = doc.getStyleSheet().getRule("a");
		Assertions.assertNotNull(linkRule);
		Assertions.assertEquals(HtmlUtil.getHexString(TipUtil.getToolTipHyperlinkForeground()),
			linkRule.getAttribute(CSS.Attribute.COLOR).toString());
	}


	@Test
	void tweakTipEditorPane_bulletResourceExists_addsListStyleRule() {
		// Sanity check that the "ul { list-style-image }" rule is added when
		// the bundled bullet_black.png resource is on the classpath, which it
		// always is for this module's own build.
		JEditorPane pane = createHtmlEditorPane();

		TipUtil.tweakTipEditorPane(pane);

		HTMLDocument doc = (HTMLDocument) pane.getDocument();
		Assertions.assertNotNull(doc.getStyleSheet().getRule("ul"));
	}


}

/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class OutlineHighlightPainterTest {


	@Test
	void constructor_nonNullColor() {
		OutlineHighlightPainter painter = new OutlineHighlightPainter(Color.RED);
		Assertions.assertEquals(Color.RED, painter.getColor());
	}


	@Test
	void constructor_nullColor_defaultsToBlack() {
		OutlineHighlightPainter painter = new OutlineHighlightPainter(null);
		Assertions.assertEquals(Color.BLACK, painter.getColor());
	}


	@Test
	void setColor_updatesColor() {
		OutlineHighlightPainter painter = new OutlineHighlightPainter(Color.RED);
		painter.setColor(Color.BLUE);
		Assertions.assertEquals(Color.BLUE, painter.getColor());
	}


	@Test
	void setColor_null_throwsNullPointerException() {
		OutlineHighlightPainter painter = new OutlineHighlightPainter(Color.RED);
		Assertions.assertThrows(NullPointerException.class, () -> painter.setColor(null));
	}


	@Test
	void paintLayer_singleOffset_doesNotThrow() throws BadLocationException {
		JTextArea textArea = new JTextArea("Hello world");
		textArea.setSize(200, 100);
		OutlineHighlightPainter painter = new OutlineHighlightPainter(Color.RED);

		Highlighter highlighter = textArea.getHighlighter();
		Assertions.assertDoesNotThrow(() -> highlighter.addHighlight(2, 2, painter));

		BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		try {
			Assertions.assertDoesNotThrow(() -> textArea.paint(g));
		} finally {
			g.dispose();
		}
	}


	@Test
	void paintLayer_range_doesNotThrow() throws BadLocationException {
		JTextArea textArea = new JTextArea("Hello world");
		textArea.setSize(200, 100);
		OutlineHighlightPainter painter = new OutlineHighlightPainter(Color.RED);

		Highlighter highlighter = textArea.getHighlighter();
		Assertions.assertDoesNotThrow(() -> highlighter.addHighlight(0, 5, painter));

		BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		try {
			Assertions.assertDoesNotThrow(() -> textArea.paint(g));
		} finally {
			g.dispose();
		}
	}


}

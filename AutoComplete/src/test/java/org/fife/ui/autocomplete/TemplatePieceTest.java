/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class TemplatePieceTest {


	@Test
	void text_getText_returnsConstructorValue() {
		TemplatePiece.Text piece = new TemplatePiece.Text("foo");
		Assertions.assertEquals("foo", piece.getText());
	}


	@Test
	void text_toString_includesText() {
		TemplatePiece.Text piece = new TemplatePiece.Text("foo");
		Assertions.assertEquals("[TemplatePiece.Text: text=foo]", piece.toString());
	}


	@Test
	void param_getText_returnsConstructorValue() {
		TemplatePiece.Param piece = new TemplatePiece.Param("bar");
		Assertions.assertEquals("bar", piece.getText());
	}


	@Test
	void param_toString_includesText() {
		TemplatePiece.Param piece = new TemplatePiece.Param("bar");
		Assertions.assertEquals("[TemplatePiece.Param: param=bar]", piece.toString());
	}


	@Test
	void paramCopy_getText_returnsConstructorValue() {
		TemplatePiece.ParamCopy piece = new TemplatePiece.ParamCopy("baz");
		Assertions.assertEquals("baz", piece.getText());
	}


	@Test
	void paramCopy_toString_includesText() {
		TemplatePiece.ParamCopy piece = new TemplatePiece.ParamCopy("baz");
		Assertions.assertEquals("[TemplatePiece.ParamCopy: param=baz]", piece.toString());
	}


}

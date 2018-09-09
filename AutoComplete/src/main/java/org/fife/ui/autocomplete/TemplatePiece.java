/*
 * 06/17/2012
 *
 * TemplatePiece.java - A logical piece of a template completion.
 *
 * This library is distributed under a modified BSD license.  See the included
 * AutoComplete.License.txt file for details.
 */
package org.fife.ui.autocomplete;


/**
 * A piece of a <code>TemplateCompletion</code>.  You add instances of this
 * class to template completions to define them.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see TemplateCompletion
 */
interface TemplatePiece {


	String getText();


	/**
	 * A plain text template piece.
	 */
	class Text implements TemplatePiece {

		private String text;

		Text(String text) {
			this.text = text;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return "[TemplatePiece.Text: text=" + text + "]";
		}

	}


	/**
	 * A parameter template piece.
	 */
	class Param implements TemplatePiece {

		String text;

		Param(String text) {
			this.text = text;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return "[TemplatePiece.Param: param=" + text + "]";
		}

	}


	/**
	 * A copy of a parameter template piece.
	 */
	class ParamCopy implements TemplatePiece {

		private String text;

		ParamCopy(String text) {
			this.text = text;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return "[TemplatePiece.ParamCopy: param=" + text + "]";
		}

	}


}

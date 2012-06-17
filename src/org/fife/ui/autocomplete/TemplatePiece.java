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


	public class Text implements TemplatePiece {

		private String text;

		public Text(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

	}


	public class Param implements TemplatePiece {

		String text;

		public Param(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

	}


	public class ParamCopy implements TemplatePiece {

		private String text;

		public ParamCopy(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

	}


}
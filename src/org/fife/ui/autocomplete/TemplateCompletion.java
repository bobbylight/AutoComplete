/*
 * 05/26/2012
 *
 * TemplateCompletion.java - A completion used to insert boilerplate code
 * snippets that have arbitrary sections the user will want to change, such as
 * for-loops.
 * 
 * This library is distributed under a modified BSD license.  See the included
 * RSyntaxTextArea.License.txt file for details.
 */
package org.fife.ui.autocomplete;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;


/**
 * A completion made up of a template with arbitrary parameters that the user
 * can tab through and fill in.  This completion type is useful for inserting
 * common boilerplate code, such as for-loops.<p>
 *
 * This class is a work in progress and currently should not be used.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class TemplateCompletion extends AbstractCompletion
								implements ParameterizedCompletion {

	private List pieces;

	private String replacementText;

	private String definitionString;

	/**
	 * The template's parameters.
	 */
	private List params;



	public TemplateCompletion(CompletionProvider provider, String replacementText,
			String definitionString) {
		super(provider);
		this.replacementText = replacementText;
		this.definitionString = definitionString;
		pieces = new ArrayList(3);
		params = new ArrayList(3);
	}


	public void addTemplatePiece(TemplatePiece piece) {
		pieces.add(piece);
		if (piece.isParam) {
			final String type = null; // TODO
			Parameter param = new Parameter(type, piece.text);
			params.add(param);
		}
	}


	private String getPieceText(int index, String leadingWS) {
		String text = null;
		TemplatePiece piece = (TemplatePiece)pieces.get(index);
		if (piece.id!=null) {
			final String id = piece.id;
			for (int i=0; i<pieces.size(); i++) {
				piece = (TemplatePiece)pieces.get(i);
				if (id.equals(piece.id)) {
					text = piece.text;
					break;
				}
			}
		}
		else {
			text = piece.text;
		}
		if (text.indexOf('\n')>-1) {
			text = text.replaceAll("\n", "\n" + leadingWS);
		}
		return text;
	}


	/**
	 * For template completions, the "replacement text" is really just the
	 * first piece of the template, not the entire thing.  You should add
	 * template pieces so that the rest of the template is dynamically
	 * generated.
	 */
	public String getReplacementText() {
		return replacementText;
	}


	public String getSummary() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDefinitionString() {
		return definitionString;
	}


	public ParameterizedCompletionInsertionInfo getInsertionInfo(
			JTextComponent tc, boolean addParamStartList) {

		ParameterizedCompletionInsertionInfo info =
			new ParameterizedCompletionInsertionInfo();

		StringBuffer sb = new StringBuffer();
		int dot = tc.getCaretPosition();
		int paramCount = getParamCount();

		// Get the range in which the caret can move before we hide
		// this tool tip.
		int minPos = dot;
		Position maxPos = null;
		try {
			maxPos = tc.getDocument().createPosition(dot);
		} catch (BadLocationException ble) {
			ble.printStackTrace(); // Never happens
		}
		info.setCaretRange(minPos, maxPos);
		int firstParamLen = 0;

		Document doc = tc.getDocument();
		String leadingWS = null;
		try {
			leadingWS = RSyntaxUtilities.getLeadingWhitespace(doc, dot);
		} catch (BadLocationException ble) { // Never happens
			ble.printStackTrace();
			leadingWS = "";
		}

		// Create the text to insert (keep it one completion for
		// performance and simplicity of undo/redo).
		int start = dot;
		for (int i=0; i<pieces.size(); i++) {
			TemplatePiece piece = (TemplatePiece)pieces.get(i);
			String text = getPieceText(i, leadingWS);
			sb.append(text);
			int end = start + text.length();
			if (piece.isParam) {
				info.addReplacementLocation(start, end);
				if (firstParamLen==0) {
					firstParamLen = text.length();
				}
			}
			start = end;
		}
		int selectionEnd = paramCount>0 ? (dot+firstParamLen) : dot;
		info.setInitialSelection(dot, selectionEnd);
		info.setTextToInsert(sb.toString());
		return info;

	}


	/**
	 * {@inheritDoc}
	 */
	public Parameter getParam(int index) {
		return (Parameter)params.get(index);
	}


	/**
	 * {@inheritDoc}
	 */
	public int getParamCount() {
		return params==null ? 0 : params.size();
	}


	public String toString() {
		return getDefinitionString();
	}


	public static class TemplatePiece {

		private String id;
		private String text;
		private boolean isParam;

		public TemplatePiece(String id, String text, boolean isParam) {
			this.id = id;
			this.text = text;
			this.isParam = isParam;
		}

	}


}
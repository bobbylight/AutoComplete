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
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;

import org.fife.ui.autocomplete.TemplatePiece.Param;
import org.fife.ui.autocomplete.TemplatePiece.ParamCopy;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;


/**
 * A completion made up of a template with arbitrary parameters that the user
 * can tab through and fill in.  This completion type is useful for inserting
 * common boilerplate code, such as for-loops.<p>
 *
 * The format of a template is similar to those in Eclipse.  The following
 * example would be the format for a for-loop template:
 * 
 * <pre>
 * for (int ${i} = 0; ${i} &lt; ${array}.length; ${i}++) {
 *    ${cursor}
 * }
 * </pre>
 *
 * In the above example, the first <code>${i}</code> is a parameter for the
 * user to type into; all the other <code>${i}</code> instances are
 * automatically changed to what the user types in the first one.  The parameter
 * named <code>${cursor}</code> is the "ending position" of the template.  It's
 * where the caret moves after it cycles through all other parameters.  If the
 * user types into it, template mode terminates.  If more than one
 * <code>${cursor}</code> parameter is specified, behavior is undefined.<p>
 * 
 * Leading whitespace is automatically added to lines if the template spans
 * more than one line, and if used with a text component using a
 * <code>PlainDocument</code>, tabs will be converted to spaces if requested.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class TemplateCompletion extends AbstractCompletion
								implements ParameterizedCompletion {

	private List pieces;

	private String inputText;

	private String definitionString;

	/**
	 * The template's parameters.
	 */
	private List params;


	public TemplateCompletion(CompletionProvider provider,
				String inputText, String definitionString, String template) {
		super(provider);
		this.inputText = inputText;
		this.definitionString = definitionString;
		pieces = new ArrayList(3);
		params = new ArrayList(3);
		parse(template);
	}


	private void addTemplatePiece(TemplatePiece piece) {
		pieces.add(piece);
		if (piece instanceof Param && !"cursor".equals(piece.getText())) {
			final String type = null; // TODO
			Parameter param = new Parameter(type, piece.getText());
			params.add(param);
		}
	}


	public String getInputText() {
		return inputText;
	}


	private String getPieceText(int index, String leadingWS) {
		TemplatePiece piece = (TemplatePiece)pieces.get(index);
		String text = piece.getText();
		if (text.indexOf('\n')>-1) {
			text = text.replaceAll("\n", "\n" + leadingWS);
		}
		return text;
	}


	/**
	 * Returns <code>null</code>; template completions insert all of their
	 * text via <code>getInsertionInfo()</code>.
	 *
	 * @return <code>null</code> always.
	 */
	public String getReplacementText() {
		return null;
	}


	public String getSummary() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDefinitionString() {
		return definitionString;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean getShowParameterToolTip() {
		return false;
	}


	public ParameterizedCompletionInsertionInfo getInsertionInfo(
			JTextComponent tc, boolean addParamStartList,
			boolean replaceTabsWithSpaces) {

		ParameterizedCompletionInsertionInfo info =
			new ParameterizedCompletionInsertionInfo();

		StringBuffer sb = new StringBuffer();
		int dot = tc.getCaretPosition();

		// Get the range in which the caret can move before we hide
		// this tool tip.
		int minPos = dot;
		Position maxPos = null;
		int defaultEndOffs = -1;
		try {
			maxPos = tc.getDocument().createPosition(dot);
		} catch (BadLocationException ble) {
			ble.printStackTrace(); // Never happens
		}
		info.setCaretRange(minPos, maxPos);
		int firstParamLen = 0;
		int selStart = dot; // Default value
		int selEnd = selStart;

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
			if (piece instanceof Param && "cursor".equals(text)) {
				if (replaceTabsWithSpaces) {
					start = possiblyReplaceTabsWithSpaces(sb, tc, start);
				}
				defaultEndOffs = start;
			}
			else {
				int end = start + text.length();
				sb.append(text);
				if (piece instanceof Param) {
					info.addReplacementLocation(start, end);
					if (firstParamLen==0) {
						firstParamLen = text.length();
						selStart = start;
						selEnd = selStart + firstParamLen;
					}
				}
				else if (piece instanceof ParamCopy) {
					info.addReplacementCopy(piece.getText(), start, end);
				}
				start = end;
			}
		}

		info.setInitialSelection(selStart, selEnd);
		if (defaultEndOffs>-1) {
			// Keep this location "after" all others when tabbing
			info.addReplacementLocation(defaultEndOffs, defaultEndOffs);
		}
		info.setDefaultEndOffs(defaultEndOffs);
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


	/**
	 * Returns whether a parameter is already defined with a specific name.
	 *
	 * @param name The name.
	 * @return Whether a parameter is defined with that name.
	 */
	private boolean isParamDefined(String name) {
		for (int i=0; i<getParamCount(); i++) {
			Parameter param = getParam(i);
			if (name.equals(param.getName())) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Parses a template string into logical pieces used by this class.
	 *
	 * @param template The template to parse.
	 */
	private void parse(String template) {

		int offs = 0;
		int lastOffs = 0;

		while ((offs=template.indexOf('$', lastOffs))>-1 && offs<template.length()-1) {

			char next = template.charAt(offs+1);
			switch (next) {
				case '$': // "$$" => escaped single dollar sign
					addTemplatePiece(new TemplatePiece.Text(
							template.substring(lastOffs, offs+1)));
					lastOffs = offs += 2;
					break;
				case '{': // "${...}" => variable
					int closingCurly = template.indexOf('}', offs+2);
					if (closingCurly>-1) {
						addTemplatePiece(new TemplatePiece.Text(
								template.substring(lastOffs, offs)));
						String varName = template.substring(offs+2, closingCurly);
						if (!"cursor".equals(varName) && isParamDefined(varName)) {
							addTemplatePiece(new TemplatePiece.ParamCopy(varName));
						}
						else {
							addTemplatePiece(new TemplatePiece.Param(varName));
						}
						lastOffs = offs = closingCurly + 1;
					}
					break;
			}

		}

		if (lastOffs<template.length()) {
			String text = template.substring(lastOffs);
			addTemplatePiece(new TemplatePiece.Text(text));
		}

	}


	private int possiblyReplaceTabsWithSpaces(StringBuffer sb, JTextComponent tc,
											int start) {

		int size = 4;
		Document doc = tc.getDocument();
		if (doc != null) {
			Integer i = (Integer) doc.getProperty(PlainDocument.tabSizeAttribute);
			if (i != null) {
				size = i.intValue();
			}
		}
		String tab = "";
		for (int i=0; i<size; i++) {
			tab += " ";
		}

		int lastNewline = sb.lastIndexOf("\n");
		int lineOffs = 0;
		for (int j=lastNewline+1; j<sb.length(); j++) {
			if (sb.charAt(j)=='\t') {
				int count = size - (lineOffs%size);
				sb.replace(j, j+1, tab.substring(0, count));
				start += count - 1;
			}
		}

		return start;

	}


	public String toString() {
		return getDefinitionString();
	}


}
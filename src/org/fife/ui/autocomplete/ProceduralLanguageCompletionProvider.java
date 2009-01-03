/*
 * 12/21/2008
 *
 * ProceduralLanguageCompletionProvider.java - A provider useful for procedural
 * languages, such as C.
 * Copyright (C) 2008 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * A completion provider that reads an XML file for a procedural language API
 * (such as C).  That XML file can define the following things:
 * 
 * <ul>
 *    <li>Functions, their parameters, and return types</li>
 *    <li>Constants (such as <tt>#define</tt>s)</li>
 *    <li>(Global) variables</li>
 * </ul>
 *
 * @author Robert Futrell
 * @version 1.0
 * 
 */
/*
 * TODO: Make chars '(', ')' and ',' configurable.
 */
public class ProceduralLanguageCompletionProvider
									extends AbstractCompletionProvider {

	private Segment seg;

	private String[] dataTypes;
	private String dataTypeFG;
	private boolean colorizeHeader;


	public ProceduralLanguageCompletionProvider(InputStream in) {
		try {
			this.completions = loadXMLFromStream(in);
			Collections.sort(this.completions);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		init();
	}


	public ProceduralLanguageCompletionProvider(String fileName) {
		try {
			InputStream in = null;
			File file = new File(fileName);
			if (file.isFile()) {
				in = new FileInputStream(file);
			}
			else {
				ClassLoader cl = getClass().getClassLoader();
				in = cl.getResourceAsStream(fileName);
			}
			this.completions = loadXMLFromStream(in);
			Collections.sort(this.completions);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		init();
	}


	/**
	 * {@inheritDoc}
	 */
	public String getAlreadyEnteredText(JTextComponent comp) {

		Document doc = comp.getDocument();

		int dot = comp.getCaretPosition();
		Element root = doc.getDefaultRootElement();
		int index = root.getElementIndex(dot);
		Element elem = root.getElement(index);
		int start = elem.getStartOffset();
		int len = dot-start;
		try {
			doc.getText(start, len, seg);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return EMPTY_STRING;
		}

		int segEnd = seg.offset + len;
		start = segEnd - 1;
		while (start>=seg.offset && isValidChar(seg.array[start])) {
			start--;
		}
		start++;

		len = segEnd - start;
		return len==0 ? EMPTY_STRING : new String(seg.array, start, len);

	}


	/**
	 * Returns whether the description area should have its header information
	 * syntax highlighted.
	 *
	 * @return Whether to color the description area's header.
	 * @see #setColorizeHeader(boolean)
	 */
	public boolean getColorizeHeader() {
		return colorizeHeader;
	}


	/**
	 * Does initialization common to various constructors.
	 */
	private void init() {
		seg = new Segment();
		setColorizeHeader(false);
		setListCellRenderer(new ProceduralLanguageCellRenderer());
	}


	public String isDataType(String str) {
		return Arrays.binarySearch(dataTypes, str)>=0 ?
				dataTypeFG : null;
	}


	private List loadXMLFromStream(InputStream in) throws IOException {

		long start = System.currentTimeMillis();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		XMLParser handler = new XMLParser();
		BufferedInputStream bin = new BufferedInputStream(in);
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(bin, handler);
			return handler.getCompletions();
		} catch (SAXException se) {
			throw new IOException(se.toString());
		} catch (ParserConfigurationException pce) {
			throw new IOException(pce.toString());
		} finally {
			long time = System.currentTimeMillis() - start;
			System.out.println("XML loaded in: " + time + "ms");
			bin.close();
		}

	}


	/**
	 * Sets whether the header text of the description area should be
	 * syntax highlighted.
	 *
	 * @param colorize Whether to colorize the header information in the
	 *        description area.
	 * @see #getColorizeHeader()
	 */
	public void setColorizeHeader(boolean colorize) {
		colorizeHeader = colorize;
	}


	/**
	 * Sets the color to use for data types in the header of a description
	 * window.
	 *
	 * @param fg The foreground color to use.
	 * @see #setDataTypes(String[])
	 * @see #getColorizeHeader()
	 */
	public void setDataTypeForeground(Color fg) {
		dataTypeFG = Util.getHexString(fg);
	}


	/**
	 * Sets the identifiers to color as data types in the header of a
	 * description window.
	 *
	 * @param types The identifiers for data types.  If this is
	 *        <code>null</code>, nothing will be colorized as a data type.
	 * @see #setDataTypeForeground(Color)
	 * @see #getColorizeHeader()
	 */
	public void setDataTypes(String[] types) {
		if (types==null) {
			dataTypes = null;
		}
		else {
			dataTypes = (String[])types.clone();
			Arrays.sort(dataTypes, String.CASE_INSENSITIVE_ORDER);
		}
	}


	/**
	 * Returns whether the specified character is valid in an auto-completion.
	 * Subclasses can override this method if their language supports a
	 * different set of valid chars for auto-completable items.
	 *
	 * @param ch The character.
	 * @return Whether the character is valid.
	 */
	protected boolean isValidChar(char ch) { 
		return Character.isLetterOrDigit(ch) || ch=='_';
	}


	/**
	 * Parser for an XMl file describing a procedural language such as C.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class XMLParser extends DefaultHandler {

		private List completions;

		private String name;
		private String type;
		private String returnType;
		private StringBuffer desc;
		private String paramName;
		private String paramType;
		private StringBuffer paramDesc;
		private List params;
		private String definedIn;
		private boolean doingKeywords;
		private boolean inKeyword;
		private boolean gettingDesc;
		private boolean gettingParams;
		private boolean inParam;
		private boolean gettingParamDesc;

		public XMLParser() {
			completions = new ArrayList();
			params = new ArrayList(1);
			desc = new StringBuffer();
			paramDesc = new StringBuffer();
		}

		public void characters(char[] ch, int start, int length) {
			if (gettingDesc) {
				desc.append(ch, start, length);
			}
			else if (gettingParamDesc) {
				paramDesc.append(ch, start, length);
			}
		}

		public void endElement(String uri, String localName, String qName) {

			if ("keywords".equals(qName)) {
				doingKeywords = false;
			}

			else if (doingKeywords) {

				if ("keyword".equals(qName)) {
					Completion c = null;
					if ("function".equals(type)) {
						FunctionCompletion fc = new FunctionCompletion(
								ProceduralLanguageCompletionProvider.this, name, returnType);
						if (desc.length()>0) {
							fc.setDescription(desc.toString());
							desc.setLength(0);
						}
						fc.setParams(params);
						fc.setDefinedIn(definedIn);
						c = fc;
					}
					else if ("constant".equals(type)) {
						VariableCompletion vc = new VariableCompletion(
								ProceduralLanguageCompletionProvider.this, name, returnType);
						if (desc.length()>0) {
							vc.setDescription(desc.toString());
							desc.setLength(0);
						}
						vc.setDefinedIn(definedIn);
						c = vc;
					}
					else {
						throw new InternalError("Unexpected type: " + type);
					}
					completions.add(c);
					inKeyword = false;
				}
				else if (inKeyword) {
					if ("desc".equals(qName)) {
						gettingDesc = false;
					}
					else if (gettingParams) {
						if ("params".equals(qName)) {
							gettingParams = false;
						}
						else if ("param".equals(qName)) {
							FunctionCompletion.Parameter param =
								new FunctionCompletion.Parameter(paramType,
															paramName);
							if (paramDesc.length()>0) {
								param.setDescription(paramDesc.toString());
								paramDesc.setLength(0);
							}
							params.add(param);
							inParam = false;
						}
						else if (inParam) {
							if ("desc".equals(qName)) {
								gettingParamDesc = false;
							}
						}
					}
				}

			}

		}

		public List getCompletions() {
			return completions;
		}

		public void startElement(String uri, String localName, String qName, Attributes attrs) { 
			if ("keywords".equals(qName)) {
				doingKeywords = true;
			}
			else if (doingKeywords) {
				if ("keyword".equals(qName)) {
					name = attrs.getValue("name");
					type = attrs.getValue("type");
					returnType = attrs.getValue("returnType");
					params.clear();
					definedIn = attrs.getValue("definedIn");
					inKeyword = true;
				}
				else if (inKeyword) {
					if ("params".equals(qName)) {
						gettingParams = true;
					}
					else if (gettingParams) {
						if ("param".equals(qName)) {
							paramName = attrs.getValue("name");
							paramType = attrs.getValue("type");
							inParam = true;
						}
						if (inParam) {
							if ("desc".equals(qName)) {
								gettingParamDesc = true;
							}
						}
					}
					else if ("desc".equals(qName)) {
						gettingDesc = true;
					}
				}
			}
		}

	}


}
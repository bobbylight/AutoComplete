package org.fife.ui.autocomplete;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Parser for an XML file describing a procedural language such as C.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class CompletionXMLParser extends DefaultHandler {

	/**
	 * The completions found after parsing the XML.
	 */
	private List completions;

	/**
	 * The provider we're getting completions for.
	 */
	private CompletionProvider provider;

	private String name;
	private String type;
	private String returnType;
	private StringBuffer returnValDesc;
	private StringBuffer desc;
	private String paramName;
	private String paramType;
	private StringBuffer paramDesc;
	private List params;
	private String definedIn;
	private boolean doingKeywords;
	private boolean inKeyword;
	private boolean gettingReturnValDesc;
	private boolean gettingDesc;
	private boolean gettingParams;
	private boolean inParam;
	private boolean gettingParamDesc;


	/**
	 * Constructor.
	 *
	 * @param provider The provider to get completions for.
	 * @see #reset(CompletionProvider)
	 */
	public CompletionXMLParser(CompletionProvider provider) {
		this.provider = provider;
		completions = new ArrayList();
		params = new ArrayList(1);
		desc = new StringBuffer();
		paramDesc = new StringBuffer();
		returnValDesc = new StringBuffer();
	}


	/**
	 * Called when character data inside an element is found.
	 */
	public void characters(char[] ch, int start, int length) {
		if (gettingDesc) {
			desc.append(ch, start, length);
		}
		else if (gettingParamDesc) {
			paramDesc.append(ch, start, length);
		}
		else if (gettingReturnValDesc) {
			returnValDesc.append(ch, start, length);
		}
	}


	private FunctionCompletion createFunctionCompletion() {
		FunctionCompletion fc = new FunctionCompletion(provider,
				name, returnType);
		if (desc.length()>0) {
			fc.setDescription(desc.toString());
			desc.setLength(0);
		}
		fc.setParams(params);
		fc.setDefinedIn(definedIn);
		if (returnValDesc.length()>0) {
			fc.setReturnValueDescription(returnValDesc.toString());
			returnValDesc.setLength(0);
		}
		return fc;
	}


	private MarkupTagCompletion createMarkupTagCompletion() {
		MarkupTagCompletion mc = new MarkupTagCompletion(provider,
				name);
		if (desc.length()>0) {
			mc.setDescription(desc.toString());
			desc.setLength(0);
		}
		mc.setAttributes(params);
		mc.setDefinedIn(definedIn);
		return mc;
	}


	private VariableCompletion createVariableCompletion() {
		VariableCompletion vc = new VariableCompletion(provider,
				name, returnType);
		if (desc.length()>0) {
			vc.setDescription(desc.toString());
			desc.setLength(0);
		}
		vc.setDefinedIn(definedIn);
		return vc;
	}


	/**
	 * Called when an element is closed.
	 */
	public void endElement(String uri, String localName, String qName) {

		if ("keywords".equals(qName)) {
			doingKeywords = false;
		}

		else if (doingKeywords) {

			if ("keyword".equals(qName)) {
				Completion c = null;
				if ("function".equals(type)) {
					c = createFunctionCompletion();
				}
				else if ("constant".equals(type)) {
					c = createVariableCompletion();
				}
				else if ("tag".equals(type)) { // Markup tag, such as HTML
					c = createMarkupTagCompletion();
				}
				else {
					throw new InternalError("Unexpected type: " + type);
				}
				completions.add(c);
				inKeyword = false;
			}
			else if (inKeyword) {
				if ("returnValDesc".equals(qName)) {
					gettingReturnValDesc = false;
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
				else if ("desc".equals(qName)) {
					gettingDesc = false;
				}
			}

		}

	}


	/**
	 * Returns the completions found after parsing the XML.
	 *
	 * @return The completions.
	 */
	public List getCompletions() {
		return completions;
	}


	/**
	 * Resets this parser to grab more completions.
	 *
	 * @param provider The new provider to get completions for.
	 */
	public void reset(CompletionProvider provider) {
		this.provider = provider;
		completions.clear();
		doingKeywords = inKeyword = gettingDesc = gettingParams =  inParam = 
				gettingParamDesc = false;
	}


	/**
	 * Called when an element starts.
	 */
	public void startElement(String uri, String localName, String qName,
							Attributes attrs) { 
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
				if ("returnValDesc".equals(qName)) {
					gettingReturnValDesc = true;
				}
				else if ("params".equals(qName)) {
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
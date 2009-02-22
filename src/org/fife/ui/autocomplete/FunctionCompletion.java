/*
 * 12/22/2008
 *
 * FunctionCompletion.java - A completion representing a function.
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.JTextComponent;


/**
 * A completion choice representing a function.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FunctionCompletion extends VariableCompletion
								implements ParameterizedCompletion {

	/**
	 * Parameters to the function.
	 */
	private List params;

	/**
	 * A description of the return value of this function.
	 */
	private String returnValDesc;


	/**
	 * Constructor.
	 *
	 * @param provider The parent provider.
	 * @param name The name of this function.
	 * @param returnType The return type of this function.
	 */
	public FunctionCompletion(CompletionProvider provider, String name,
								String returnType) {
		super(provider, name, returnType);
	}


	protected void addDefinitionString(StringBuffer sb) {
		sb.append("<html><b>");
		sb.append(getDefinitionString());
		sb.append("</b>");
	}


	/**
	 * Adds HTML describing the parameters to this function to a buffer.
	 *
	 * @param sb The buffer to append to.
	 */
	protected void addParameters(StringBuffer sb) {

		// TODO: Localize me

		int paramCount = getParamCount();
		if (paramCount>0) {
			sb.append("<b>Parameters:</b><br>");
			sb.append("<center><table width='90%'><tr><td>");
			for (int i=0; i<paramCount; i++) {
				Parameter param = getParam(i);
				sb.append("<b>");
				sb.append(param.getName()!=null ? param.getName() :
							param.getType());
				sb.append("</b>&nbsp;");
				String desc = param.getDescription();
				if (desc!=null) {
					sb.append(desc);
				}
				sb.append("<br>");
			}
			sb.append("</td></tr></table></center><br><br>");
		}

		if (returnValDesc!=null) {
			sb.append("<b>Returns:</b><br><center><table width='90%'><tr><td>");
			sb.append(returnValDesc);
			sb.append("</td></tr></table></center><br><br>");
		}

	}


	/**
	 * Returns the "definition string" for this function completion.  For
	 * example, for the C "<code>printf</code>" function, this would return
	 * "<code>int printf(const char *, ...)</code>".
	 * 
	 * @return The definition string.
	 */
	public String getDefinitionString() {

		StringBuffer sb = new StringBuffer();

		// Add the return type if applicable (C macros like NULL have no type).
		String type = getType();
		if (type!=null) {
			sb.append(type).append(' ');
		}

		// Add the item being described's name.
		sb.append(getName());

		// Add parameters for functions.
		CompletionProvider provider = getProvider();
		sb.append(provider.getParameterListStart());
		for (int i=0; i<getParamCount(); i++) {
			Parameter param = getParam(i);
			type = param.getType();
			String name = param.getName();
			if (type!=null) {
				sb.append(type);
				if (name!=null) {
					sb.append(' ');
				}
			}
			if (name!=null) {
				sb.append(name);
			}
			if (i<params.size()-1) {
				sb.append(provider.getParameterListSeparator());
			}
		}
		sb.append(provider.getParameterListEnd());

		return sb.toString();

	}


	/**
	 * Returns the specified {@link ParameterizedCompletion.Parameter}.
	 *
	 * @param index The index of the parameter to retrieve.
	 * @return The parameter.
	 * @see #getParamCount()
	 */
	public Parameter getParam(int index) {
		return (Parameter)params.get(index);
	}


	/**
	 * Returns the number of parameters to this function.
	 *
	 * @return The number of parameters to this function.
	 * @see #getParam(int)
	 */
	public int getParamCount() {
		return params==null ? 0 : params.size();
	}


	/**
	 * Returns the description of the return value of this function.
	 *
	 * @return The description, or <code>null</code> if there is none.
	 * @see #setReturnValueDescription(String)
	 */
	public String getReturnValueDescription() {
		return returnValDesc;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getSummary() {
		StringBuffer sb = new StringBuffer();
		addDefinitionString(sb);
		possiblyAddDescription(sb);
		addParameters(sb);
		possiblyAddDefinedIn(sb);
		return sb.toString();
	}


	/**
	 * Returns the tool tip text to display for mouse hovers over this
	 * completion.<p>
	 *
	 * Note that for this functionality to be enabled, a
	 * <tt>JTextComponent</tt> must be registered with the
	 * <tt>ToolTipManager</tt>, and the text component must know to search
	 * for this value.  In the case of an
	 * <a href="http://fifesoft.com/rsyntaxtextarea">RSyntaxTextArea</a>, this
	 * can be done with a <tt>org.fife.ui.rtextarea.ToolTipSupplier</tt> that
	 * calls into
	 * {@link CompletionProvider#getCompletionsAt(JTextComponent, java.awt.Point)}.
	 *
	 * @return The tool tip text for this completion, or <code>null</code> if
	 *         none.
	 */
	public String getToolTipText() {
		return getDefinitionString();
	}


	/**
	 * Sets the parameters to this function.
	 *
	 * @param params The parameters.  This should be a list of
	 *        {@link ParameterizedCompletion.Parameter}s.
	 * @see #getParam(int)
	 * @see #getParamCount()
	 */
	public void setParams(List params) {
		// Deep copy so parsing can re-use its array.
		this.params = new ArrayList(params);
	}


	/**
	 * Sets the description of the return value of this function.
	 *
	 * @param desc The description.
	 * @see #getReturnValueDescription()
	 */
	public void setReturnValueDescription(String desc) {
		this.returnValDesc = desc;
	}


}
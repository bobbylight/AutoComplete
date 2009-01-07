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


/**
 * A completion choice representing a function.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FunctionCompletion extends VariableCompletion {

	/**
	 * Parameters to the function.
	 */
	private List params;


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

		// Add the return type if applicable (C macros like NULL have no type).
		String type = getType();
		if (type!=null) {
			appendPossibleDataType(sb, type);
			sb.append(' ');
		}

		// Add the item being described's name.
		sb.append(getName());

		// Add parameters for functions.
		sb.append('(');
		for (int i=0; i<getParamCount(); i++) {
			Parameter param = getParam(i);
			type = param.getType();
			String name = param.getName();
			if (type!=null) {
				appendPossibleDataType(sb, type);
				if (name!=null) {
					sb.append(' ');
				}
			}
			if (name!=null) {
				sb.append(name);
			}
			if (i<params.size()-1) {
				sb.append(", ");
			}
		}
		sb.append(')');

		sb.append("</b>");

	}


	/**
	 * Adds HTML describing the parameters to this function to a buffer.
	 *
	 * @param sb The buffer to append to.
	 */
	protected void addParameters(StringBuffer sb) {

		if (params!=null && params.size()>0) {
			sb.append("<b>Parameters:</b><br>"); // TODO: Localize me
			for (int i=0; i<getParamCount(); i++) {
				Parameter param = getParam(i);
				sb.append("&nbsp;&nbsp;&nbsp;<b>");
				sb.append(param.getName()!=null ? param.getName() :
							param.getType());
				sb.append("</b>&nbsp;");
				String desc = param.getDescription();
				if (desc!=null) {
					sb.append(desc);
				}
				sb.append("<br>");
			}
			sb.append("<br><br>");
		}

		// TODO: Add description of return type.

	}


	/**
	 * Returns the specified {@link Parameter}.
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
	 * Sets the parameters to this function.
	 *
	 * @param params The parameters.  This should be a list of
	 *        {@link Parameter}s.
	 * @see #getParam(int)
	 * @see #getParamCount()
	 */
	public void setParams(List params) {
		// Deep copy so parsing can re-use its array.
		this.params = new ArrayList(params);
	}


	/**
	 * A parameter passed to a function.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	public static class Parameter {

		private String name;
		private String type;
		private String desc;

		public Parameter(String type, String name) {
			this.name = name;
			this.type = type;
		}

		public String getDescription() {
			return desc;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public void setDescription(String desc) {
			this.desc = desc;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (type!=null) {
				sb.append(type);
			}
			if (name!=null) {
				if (type!=null) {
					sb.append(' ');
					sb.append(name);
				}
			}
			return sb.toString();
		}

	}


}
/*
 * 12/11/2010
 *
 * ParameterizedCompletion.java - A completion option.
 * Copyright (C) 2010 Robert Futrell
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


/**
 * A completion option that takes parameters, such as a function or method.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface ParameterizedCompletion extends Completion {


	/**
	 * Returns the "definition string" for this completion.  For example,
	 * for the C "<code>printf</code>" function, this would return
	 * "<code>int printf(const char *, ...)</code>".
	 * 
	 * @return The definition string.
	 */
	public String getDefinitionString();


	/**
	 * Returns the specified {@link Parameter}.
	 *
	 * @param index The index of the parameter to retrieve.
	 * @return The parameter.
	 * @see #getParamCount()
	 */
	public Parameter getParam(int index);


	/**
	 * Returns the number of parameters this completion takes.
	 *
	 * @return The number of parameters this completion takes.
	 * @see #getParam(int)
	 */
	public int getParamCount();


	/**
	 * A parameter passed to a parameterized {@link Completion}.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	public static class Parameter {

		private String name;
		private Object type;
		private String desc;

		/**
		 * Constructor.
		 *
		 * @param type The type of this parameter.  This may be
		 *        <code>null</code> for languages without specific types,
		 *        dynamic typing, etc.  Usually you'll pass a String for this
		 *        value, but you may pass any object representing a type in
		 *        your language, as long as its <code>toString()</code> method
		 *        returns a string representation of the type.
		 * @param name The name of the parameter.
		 */
		public Parameter(Object type, String name) {
			this.name = name;
			this.type = type;
		}

		public String getDescription() {
			return desc;
		}

		public String getName() {
			return name;
		}

		/**
		 * Returns the type of this parameter, as a string.
		 *
		 * @return The type of the parameter, or <code>null</code> for none.
		 */
		public String getType() {
			return type==null ? null : type.toString();
		}

		/**
		 * Returns the object used to describe the type of this parameter.
		 *
		 * @return The type object, or <code>null</code> for none.
		 */
		public Object getTypeObject() {
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
				}
				sb.append(name);
			}
			return sb.toString();
		}

	}


}
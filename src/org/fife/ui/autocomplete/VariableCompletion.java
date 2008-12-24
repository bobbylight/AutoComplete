/*
 * 12/22/2008
 *
 * VariableCompletion.java - A completion for a variable.
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


/**
 * A completion for a variable (or constant) in a programming language.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class VariableCompletion extends AbstractCompletion {

	private String name;
	private String type;
	private String desc;
	private String definedIn;


	/**
	 * Constructor.
	 *
	 * @param provider The parent provider.
	 * @param name The name of this variable.
	 * @param type The type of this variable (e.g. "<code>int</code>",
	 *        "<code>String</code>", etc.).
	 */
	public VariableCompletion(CompletionProvider provider, String name,
							String type) {
		super(provider);
		this.name = name;
		this.type = type;
	}


	protected void addDefinitionString(StringBuffer sb) {

		sb.append("<html><b>");

		// Add the return type if applicable (C macros like NULL have no type).
		if (type!=null) {
			sb.append(type);
			sb.append(' ');
		}

		// Add the item being described's name.
		sb.append(name);

		sb.append("</b>");

	}


	/**
	 * Returns where this variable is defined.
	 *
	 * @return Where this variable is defined.
	 * @see #setDefinedIn(String)
	 */
	public String getDefinedIn() {
		return definedIn;
	}


	/**
	 * Returns a short description of this variable.  This should be an
	 * HTML snippet.
	 *
	 * @return A short description of this variable.  This may be
	 *         <code>null</code>.
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return desc;
	}


	/**
	 * Returns the name of this variable.
	 *
	 * @return The name.
	 */
	public String getName() {
		return name;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getSummary() {
		StringBuffer sb = new StringBuffer();
		addDefinitionString(sb);
		possiblyAddDescription(sb);
		possiblyAddDefinedIn(sb);
		return sb.toString();
	}


	/**
	 * Returns the type of this variable.
	 *
	 * @return The type.
	 */
	public String getType() {
		return type;
	}


	/**
	 * Returns the name of this variable.
	 *
	 * @return The text to autocomplete with.
	 */
	public String getReplacementText() {
		return getName();
	}


	/**
	 * Adds some HTML describing where this variable is defined, if this
	 * information is known.
	 *
	 * @param sb The buffer to append to.
	 */
	protected void possiblyAddDefinedIn(StringBuffer sb) {
		if (definedIn!=null) {
			sb.append("<hr>Defined in:"); // TODO: Localize me
			sb.append(" <em>").append(definedIn).append("</em>");
		}
	}


	/**
	 * Adds the description text as HTML to a buffer, if a description is
	 * defined.
	 *
	 * @param sb The buffer to append to.
	 */
	protected void possiblyAddDescription(StringBuffer sb) {
		if (desc!=null) {
			sb.append("<hr><br>");
			sb.append(desc);
			sb.append("<br><br><br>");
		}
	}


	/**
	 * Sets where this variable is defined.
	 *
	 * @param definedIn Where this variable is defined.
	 * @see #getDefinedIn()
	 */
	public void setDefinedIn(String definedIn) {
		this.definedIn = definedIn;
	}


	/**
	 * Sets the short description of this variable.  This should be an
	 * HTML snippet.
	 *
	 * @param desc A short description of this variable.  This may be
	 *        <code>null</code>.
	 * @see #getDescription()
	 */
	public void setDescription(String desc) {
		this.desc = desc;
	}


}
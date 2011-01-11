/*
 * 12/14/2010
 *
 * ParameterChoicesProvider.java - Provides completions for a
 * ParameterizedCompletion's parameters.
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

import java.util.List;
import javax.swing.text.JTextComponent;


/**
 * Provides completions for a {@link ParameterizedCompletion}'s parameters.
 * So, for example, if the user code-completes a function or method, if
 * a <code>ParameterChoicesProvider</code> is installed, it can return possible
 * completions for the parameters to that function or method.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface ParameterChoicesProvider {


	/**
	 * Returns a list of choices for a specific parameter.
	 *
	 * @param tc The text component.
	 * @param param The currently focused parameter.
	 * @return The list of parameters.  This may be <code>null</code> for
	 *         "no parameters," but might also be an empty list.
	 */
	public List getParameterChoices(JTextComponent tc,
								ParameterizedCompletion.Parameter param);


}
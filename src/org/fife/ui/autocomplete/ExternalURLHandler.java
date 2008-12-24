/*
 * 12/23/2008
 *
 * ExternalURLHandler.java - Implementations can be registered as a callback
 * to handle the user clicking on external URL's.
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

import java.net.URL;


/**
 * A callback for when an external URL is clicked in the description window.
 * If no handler is installed, and if running in Java 6, the system default
 * web browser is used to open the URL.  If not running Java 6, nothing will
 * happen.  If you want browser support for pre-Java 6 JRE's, you will need
 * to register one of these callbacks on your {@link AutoCompletion}.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see AutoCompletion#setExternalURLHandler(ExternalURLHandler)
 */
public interface ExternalURLHandler {


	/**
	 * Called when an external URL is clicked in the description window.
	 *
	 * @param url The URL.
	 */
	public void urlClicked(URL url);


}
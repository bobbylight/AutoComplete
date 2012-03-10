/*
 * 12/23/2008
 *
 * ExternalURLHandler.java - Implementations can be registered as a callback
 * to handle the user clicking on external URL's.
 * 
 * This library is distributed under a modified BSD license.  See the included
 * RSyntaxTextArea.License.txt file for details.
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
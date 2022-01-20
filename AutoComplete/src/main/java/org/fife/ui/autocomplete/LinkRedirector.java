/*
 * 05/16/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.net.URL;


/**
 * Possibly redirects one URL to another.  Useful if you want "external" URL's
 * in code completion documentation to point to a local copy instead, for
 * example.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface LinkRedirector {


	/**
	 * Hook to return an alternate URL to navigate to when a URL is clicked in
	 * an {@code RSyntaxTextArea} instance.
	 *
	 * @param original The original URL, e.g. from a {@code HyperlinkEvent}.
	 * @return The link to redirect to, or {@code null} if the original URL
	 *         should still be used.
	 */
	URL possiblyRedirect(URL original);


}

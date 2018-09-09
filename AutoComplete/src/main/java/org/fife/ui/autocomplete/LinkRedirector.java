/*
 * 05/16/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * AutoComplete.License.txt file for details.
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


	URL possiblyRedirect(URL original);


}

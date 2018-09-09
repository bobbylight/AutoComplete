/*
 * 05/11/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * AutoComplete.License.txt file for details.
 */
package org.fife.ui.autocomplete;


/**
 * Passed to {@link ExternalURLHandler}s as a way for them to display a summary
 * for a new completion in response to a link event.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see ExternalURLHandler
 */
public interface DescWindowCallback {


	/**
	 * Callback allowing a new code completion's description to be displayed
	 * in the description window.
	 *
	 * @param completion The new completion.
	 * @param anchor The anchor to scroll to, or <code>null</code> if none.
	 */
	void showSummaryFor(Completion completion, String anchor);


}

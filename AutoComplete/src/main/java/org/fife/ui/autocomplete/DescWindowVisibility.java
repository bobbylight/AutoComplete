/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;


/**
 * Controls when the "description" window (the popup that shows documentation
 * for the currently selected completion choice) is displayed alongside the
 * completion choices window.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see AutoCompletion#setDescWindowVisibility(DescWindowVisibility)
 */
public enum DescWindowVisibility {

	/**
	 * The description window is shown automatically whenever the completion
	 * choices window is showing and a description is available. This is the
	 * default (legacy) behavior.
	 */
	ALWAYS,

	/**
	 * The description window is only shown when the user explicitly requests
	 * it, via the keystroke configured by
	 * {@link AutoCompletion#setDescWindowToggleKey(javax.swing.KeyStroke)}.
	 */
	ON_DEMAND,

	/**
	 * The description window is never shown.
	 */
	NEVER

}

/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;


/**
 * Manages the colors shared across the library.  Default colors may be
 * overridden by a {@code LookAndFeel} by registering a {@link Color} in
 * {@link UIManager} under the relevant key.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class AutoCompletionStyleContext {

	/**
	 * The {@link UIManager} key for the color used to denote the ending
	 * caret position for parameterized completions.
	 */
	public static final String PROPERTY_PARAMETERIZED_COMPLETION_CURSOR_POSITION_COLOR =
		"autocomplete.parameterizedCompletionCursorPositionColor";

	/**
	 * The {@link UIManager} key for the color used to highlight copies of
	 * editable parameters in parameterized completions.
	 */
	public static final String PROPERTY_PARAMETER_COPY_COLOR = "autocomplete.parameterCopyColor";

	/**
	 * The {@link UIManager} key for the color of the outline highlight used
	 * to denote editable parameters in parameterized completions.
	 */
	public static final String PROPERTY_PARAMETER_OUTLINE_COLOR = "autocomplete.parameterOutlineColor";

	/**
	 * The color used to denote the ending caret position for parameterized
	 * completions.
	 */
	private Color parameterizedCompletionCursorPositionColor;

	/**
	 * The color used to highlight copies of editable parameters in
	 * parameterized completions.
	 */
	private Color parameterCopyColor;

	/**
	 * The color of the outline highlight used to denote editable parameters
	 * in parameterized completions.
	 */
	private Color parameterOutlineColor;


	/**
	 * Constructor.
	 */
	public AutoCompletionStyleContext() {
		setParameterOutlineColor(getDefaultColor(PROPERTY_PARAMETER_OUTLINE_COLOR, Color.GRAY));
		setParameterCopyColor(getDefaultColor(PROPERTY_PARAMETER_COPY_COLOR, new Color(0xb4d7ff)));
		setParameterizedCompletionCursorPositionColor(
			getDefaultColor(PROPERTY_PARAMETERIZED_COMPLETION_CURSOR_POSITION_COLOR, new Color(0x00b400)));
	}


	/**
	 * Returns the color registered in {@link UIManager} under the specified
	 * key, or the specified default if none is registered.  If no color is
	 * registered, {@code fallback} is cached in {@code UIManager} under
	 * {@code key} for future lookups.
	 *
	 * @param key The {@link UIManager} key to check.
	 * @param fallback The color to use, and cache, if none is registered.
	 * @return The color to use.
	 */
	private static Color getDefaultColor(String key, Color fallback) {
		Color color = UIManager.getColor(key);
		if (color == null) {
			color = new ColorUIResource(fallback);
			UIManager.put(key, color);
		}
		return color;
	}


	/**
	 * Returns the color of the highlight painted on copies of editable
	 * parameters in parameterized completions.
	 *
	 * @return The color used.
	 * @see #setParameterCopyColor(Color)
	 */
	public Color getParameterCopyColor() {
		return parameterCopyColor;
	}


	/**
	 * Returns the color used to denote the ending caret position for
	 * parameterized completions.
	 *
	 * @return The color used.
	 * @see #setParameterizedCompletionCursorPositionColor(Color)
	 */
	public Color getParameterizedCompletionCursorPositionColor() {
		return parameterizedCompletionCursorPositionColor;
	}


	/**
	 * Returns the color of the outline highlight used to denote editable
	 * parameters in parameterized completions.
	 *
	 * @return The color used.
	 * @see #setParameterOutlineColor(Color)
	 */
	public Color getParameterOutlineColor() {
		return parameterOutlineColor;
	}


	/**
	 * Sets the color of the highlight painted on copies of editable
	 * parameters in parameterized completions.
	 *
	 * @param color The color to use.
	 * @see #setParameterCopyColor(Color)
	 */
	public void setParameterCopyColor(Color color) {
		this.parameterCopyColor = color;
	}


	/**
	 * Sets the color used to denote the ending caret position for
	 * parameterized completions.
	 *
	 * @param color The color to use.
	 * @see #getParameterizedCompletionCursorPositionColor()
	 */
	public void setParameterizedCompletionCursorPositionColor(Color color) {
		this.parameterizedCompletionCursorPositionColor = color;
	}


	/**
	 * Sets the color of the outline highlight used to denote editable
	 * parameters in parameterized completions.
	 *
	 * @param color The color to use.
	 * @see #getParameterOutlineColor()
	 */
	public void setParameterOutlineColor(Color color) {
		this.parameterOutlineColor = color;
	}


}

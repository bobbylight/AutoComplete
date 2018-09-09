package org.fife.ui.autocomplete;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Used to suppress FindBugs warnings. It should be used instead of SuppressWarnings to
 * avoid conflicts with SuppressWarnings.
 */
@Retention(value = CLASS)
public @interface SuppressFBWarnings {

	/**
	 * The set of FindBugs warnings that are to be suppressed in annotated element.
	 * The value can be a bug category, kind or pattern.
	 *
	 * @return The FindBugs warnings to ignore.
	 */
	String[] value() default {};

	/**
	 * Optional documentation of the reason why the warning is suppressed.
	 *
	 * @return The reason the FindBugs warnings were ignored.
	 */
	String justification() default "";
}

/*
 * 12/21/2008
 *
 * Util.java - Utility methods for the autocompletion package.
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.AccessControlException;
import java.util.regex.Pattern;

import javax.swing.*;

import org.fife.ui.rsyntaxtextarea.PopupWindowDecorator;


/**
 * Utility methods for the auto-complete framework.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class Util {

	/**
	 * If a system property is defined with this name and set, ignoring case,
	 * to <code>true</code>, this library will not attempt to use Substance
	 * renderers.  Otherwise, if a Substance Look and Feel is installed, we
	 * will attempt to use Substance cell renderers in all of our dropdowns.<p>
	 *
	 * Note that we do not have a build dependency on Substance, so all access
	 * to Substance stuff is done via reflection.  We will fall back onto
	 * default renderers if something goes horribly wrong.
	 */
	public static final String PROPERTY_DONT_USE_SUBSTANCE_RENDERERS =
			"org.fife.ui.autocomplete.DontUseSubstanceRenderers";

	/**
	 * If this system property is <code>true</code>, then even the "main" two
	 * auto-complete windows will allow window decorations via
	 * {@link PopupWindowDecorator}.  If this property is undefined or
	 * <code>false</code>, they won't honor such decorations.  This is due to
	 * certain performance issues with translucent windows (used for drop
	 * shadows), even as of Java 7u2.
	 */
	public static final String PROPERTY_ALLOW_DECORATED_AUTOCOMPLETE_WINDOWS =
		"org.fife.ui.autocomplete.allowDecoratedAutoCompleteWindows";

	/**
	 * Used for the color of hyperlinks when a LookAndFeel uses light text
	 * against a dark background.
	 */
	public static final Color LIGHT_HYPERLINK_FG = new Color(0xd8ffff);

	private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]*>");

	private static final boolean USE_SUBSTANCE_RENDERERS;
	private static boolean desktopCreationAttempted;
	private static Object desktop;
	private static final Object LOCK_DESKTOP_CREATION = new Object();


	private Util() {
	}

	/**
	 * Attempts to open a web browser to the specified URI.
	 *
	 * @param uri The URI to open.  If this is <code>null</code>, nothing
	 *        happens and this method returns <code>false</code>.
	 * @return Whether the operation was successful.  This will be
	 *         <code>false</code> on JRE's older than 1.6.
	 */
	public static boolean browse(URI uri) {

		boolean success = false;

		if (uri!=null) {
			Object desktop = getDesktop();
			if (desktop!=null) {
				try {
					Method m = desktop.getClass().getDeclaredMethod(
								"browse", URI.class);
					m.invoke(desktop, uri);
					success = true;
				} catch (RuntimeException re) {
					throw re; // Keep FindBugs happy
				} catch (Exception e) {
					// Ignore, just return "false" below.
				}
			}
		}

		return success;

	}


	/**
	 * Returns the singleton <code>java.awt.Desktop</code> instance, or
	 * <code>null</code> if it is unsupported on this platform (or the JRE
	 * is older than 1.6).
	 *
	 * @return The desktop, as an {@link Object}.
	 */
	private static Object getDesktop() {

		synchronized (LOCK_DESKTOP_CREATION) {

			if (!desktopCreationAttempted) {

				desktopCreationAttempted = true;

				try {
					Class<?> desktopClazz = Class.forName("java.awt.Desktop");
					Method m = desktopClazz.
						getDeclaredMethod("isDesktopSupported");

					boolean supported= (Boolean) m.invoke(null);
					if (supported) {
						m = desktopClazz.getDeclaredMethod("getDesktop");
						desktop = m.invoke(null);
					}

				} catch (RuntimeException re) {
					throw re; // Keep FindBugs happy
				} catch (Exception e) {
					// Ignore; keeps desktop as null.
				}

			}

		}

		return desktop;

	}


	/**
	 * Returns the color to use for hyperlink-style components.  This method
	 * will return <code>Color.blue</code> unless it appears that the current
	 * LookAndFeel uses light text on a dark background, in which case a
	 * brighter alternative is returned.
	 *
	 * @return The color to use for hyperlinks.
	 * @see TipUtil#getToolTipHyperlinkForeground()
	 */
	static Color getHyperlinkForeground() {

		// This property is defined by all standard LaFs, even Nimbus (!),
		// but you never know what crazy LaFs there are...
		Color fg = UIManager.getColor("Label.foreground");
		if (fg==null) {
			fg = new JLabel().getForeground();
		}

		return isLightForeground(fg) ? LIGHT_HYPERLINK_FG : Color.blue;

	}


	/**
	 * Returns the screen coordinates for the monitor that contains the
	 * specified point.  This is useful for setups with multiple monitors,
	 * to ensure that popup windows are positioned properly.
	 *
	 * @param x The x-coordinate, in screen coordinates.
	 * @param y The y-coordinate, in screen coordinates.
	 * @return The bounds of the monitor that contains the specified point.
	 */
	public static Rectangle getScreenBoundsForPoint(int x, int y) {
		GraphicsEnvironment env = GraphicsEnvironment.
										getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = env.getScreenDevices();
		for (GraphicsDevice device : devices) {
			GraphicsConfiguration config = device.getDefaultConfiguration();
			Rectangle gcBounds = config.getBounds();
			if (gcBounds.contains(x, y)) {
				return gcBounds;
			}
		}
		// If point is outside all monitors, default to default monitor (?)
		return env.getMaximumWindowBounds();
	}


	/**
	 * Give apps a chance to decorate us with drop shadows, etc. Since very
	 * scrolly things such as lists (of e.g. completions) are *very* slow when
	 * in per-pixel translucent windows, even as of Java 7u2, we force the user
	 * to specify an extra option for the two "main" auto-complete windows.
	 *
	 * @return Whether to allow decorating the main auto-complete windows.
	 * @see #PROPERTY_ALLOW_DECORATED_AUTOCOMPLETE_WINDOWS
	 */
	public static boolean getShouldAllowDecoratingMainAutoCompleteWindows() {
		try {
			return Boolean.getBoolean(
					PROPERTY_ALLOW_DECORATED_AUTOCOMPLETE_WINDOWS);
		} catch (AccessControlException ace) { // We're in an applet.
			return false;
		}
	}


	/**
	 * Returns whether we should attempt to use Substance cell renderers and
	 * styles for things such as completion choices, if a Substance Look and
	 * Feel is installed.  If this is <code>false</code>, we'll use our
	 * standard rendering for completions, even when Substance is being used.
	 *
	 * @return Whether to use Substance renderers if Substance is installed.
	 */
	public static boolean getUseSubstanceRenderers() {
		return USE_SUBSTANCE_RENDERERS;
	}


	/**
	 * Returns whether the specified color is "light" to use as a foreground.
	 * Colors that return <code>true</code> indicate that the current Look and
	 * Feel probably uses light text colors on a dark background.
	 *
	 * @param fg The foreground color.
	 * @return Whether it is a "light" foreground color.
	 */
	public static boolean isLightForeground(Color fg) {
		return fg.getRed()>0xa0 && fg.getGreen()>0xa0 && fg.getBlue()>0xa0;
	}


	/**
	 * Returns whether a string starts with a specified prefix, ignoring case.
	 * This method does not support characters outside of the BMP.
	 *
	 * @param str The string to check.  This cannot be {@code null}.
	 * @param prefix The prefix to check for.  This cannot be {@code null}.
	 * @return Whether {@code str} starts with {@code prefix}, ignoring case.
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		int prefixLength = prefix.length();
		if (str.length() >= prefixLength) {
			return str.regionMatches(true, 0, prefix, 0, prefixLength);
		}
		return false;
	}


	/**
	 * Strips any HTML from a string.  The string must start with
	 * "<code>&lt;html&gt;</code>" for markup tags to be stripped.
	 *
	 * @param text The string.
	 * @return The string, with any HTML stripped.
	 */
	public static String stripHtml(String text) {
		if (text==null || !text.startsWith("<html>")) {
			return text;
		}
		// TODO: Micro-optimize me, might be called in renderers and loops
		return TAG_PATTERN.matcher(text).replaceAll("");
	}


	static {

		boolean use;
		try {
			use = !Boolean.getBoolean(PROPERTY_DONT_USE_SUBSTANCE_RENDERERS);
		} catch (AccessControlException ace) { // We're in an applet.
			use = true;
		}
		USE_SUBSTANCE_RENDERERS = use;

	}


}

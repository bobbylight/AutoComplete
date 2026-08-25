/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URI;
import javax.swing.UIManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


class UtilTest {

	private static final URI VALID_URI = URI.create("https://example.com/");

	/**
	 * The {@code Desktop} instance {@code Util.browse()} will use for the
	 * remainder of the JVM's lifetime, once installed by
	 * {@link #primeDesktopCache()}.
	 */
	private static final Desktop MOCK_DESKTOP = Mockito.mock(Desktop.class);


	/**
	 * {@code Util.browse()} lazily caches the result of
	 * {@code Desktop.isDesktopSupported()} / {@code Desktop.getDesktop()} in
	 * private static fields the very first time it's called, and never
	 * re-checks afterward.  To keep every {@code browse()} test
	 * deterministic (and to make sure we never launch a real browser during
	 * a test run), we install a mock {@code Desktop} before any test in this
	 * class runs, priming that cache while {@code Desktop} is mocked.  All
	 * {@code browse()} tests below then reuse this same cached mock.
	 */
	@BeforeAll
	static void primeDesktopCache() {
		try (MockedStatic<Desktop> desktop = Mockito.mockStatic(Desktop.class)) {
			desktop.when(Desktop::isDesktopSupported).thenReturn(true);
			desktop.when(Desktop::getDesktop).thenReturn(MOCK_DESKTOP);
			Util.browse(URI.create("about:blank")); // Triggers the one-time caching.
		}
	}


	@Test
	void startsWithIgnoreCase_happyPath() {

		Assertions.assertTrue(Util.startsWithIgnoreCase("a", "a"));
		Assertions.assertTrue(Util.startsWithIgnoreCase("a", "A"));

		Assertions.assertTrue(Util.startsWithIgnoreCase("Hello world", "Hello"));
		Assertions.assertTrue(Util.startsWithIgnoreCase("Hello world", "hello"));
		Assertions.assertTrue(Util.startsWithIgnoreCase("Hello world", "HELLO"));

	}


	@Test
	void startsWithIgnoreCase_tricky_iWithoutDot() {
		Assertions.assertTrue(Util.startsWithIgnoreCase("ı", "i"));
		Assertions.assertTrue(Util.startsWithIgnoreCase("ı", "I"));
		Assertions.assertTrue(Util.startsWithIgnoreCase("i", "ı"));
		Assertions.assertTrue(Util.startsWithIgnoreCase("I", "ı"));
	}


	@Test
	void startsWithIgnoreCase_strShorterThanPrefix_returnsFalse() {
		Assertions.assertFalse(Util.startsWithIgnoreCase("Hi", "Hello"));
	}


	@Test
	void browse_nullUri_returnsFalse() {
		Assertions.assertFalse(Util.browse(null));
	}


	@Test
	void browse_success_returnsTrue() throws IOException {
		URI uri = URI.create("https://example.com/success");
		Mockito.doNothing().when(MOCK_DESKTOP).browse(uri);
		Assertions.assertTrue(Util.browse(uri));
		Mockito.verify(MOCK_DESKTOP).browse(uri);
	}


	@Test
	void browse_checkedExceptionFromDesktop_isSwallowed_returnsFalse() throws IOException {
		URI uri = URI.create("https://example.com/checked-exception");
		Mockito.doThrow(new IOException("boom")).when(MOCK_DESKTOP).browse(uri);
		Assertions.assertFalse(Util.browse(uri));
	}


	@Test
	void browse_runtimeExceptionFromDesktop_isRethrown() throws IOException {
		URI uri = URI.create("https://example.com/runtime-exception");
		Mockito.doThrow(new IllegalStateException("boom")).when(MOCK_DESKTOP).browse(uri);
		Assertions.assertThrows(IllegalStateException.class, () -> Util.browse(uri));
	}


	@Test
	void getScreenBoundsForPoint_pointOnAMonitor_returnsThatMonitorsBounds() {
		Rectangle bounds = Util.getScreenBoundsForPoint(0, 0);
		Assertions.assertNotNull(bounds);
		Assertions.assertTrue(bounds.contains(0, 0));
	}


	@Test
	void getScreenBoundsForPoint_pointOffAllMonitors_fallsBackToMaximumWindowBounds() {
		// No real display configuration should contain a point this far out.
		Rectangle bounds = Util.getScreenBoundsForPoint(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
		Assertions.assertNotNull(bounds);
	}


	@Test
	void getShouldAllowDecoratingMainAutoCompleteWindows_unset_returnsFalse() {
		String propName = Util.PROPERTY_ALLOW_DECORATED_AUTOCOMPLETE_WINDOWS;
		String orig = System.clearProperty(propName);
		try {
			Assertions.assertFalse(Util.getShouldAllowDecoratingMainAutoCompleteWindows());
		} finally {
			if (orig != null) {
				System.setProperty(propName, orig);
			}
		}
	}


	@Test
	void getShouldAllowDecoratingMainAutoCompleteWindows_setTrue_returnsTrue() {
		String propName = Util.PROPERTY_ALLOW_DECORATED_AUTOCOMPLETE_WINDOWS;
		String orig = System.getProperty(propName);
		try {
			System.setProperty(propName, "true");
			Assertions.assertTrue(Util.getShouldAllowDecoratingMainAutoCompleteWindows());
		} finally {
			if (orig == null) {
				System.clearProperty(propName);
			} else {
				System.setProperty(propName, orig);
			}
		}
	}


	@Test
	void getUseSubstanceRenderers_defaultsToTrue() {
		// USE_SUBSTANCE_RENDERERS is computed once in a static initializer
		// from a system property, so only the value observed at class-load
		// time (with the property unset, in this build) can be verified here.
		Assertions.assertTrue(Util.getUseSubstanceRenderers());
	}


	@Test
	void isLightForeground_allChannelsBright_returnsTrue() {
		Assertions.assertTrue(Util.isLightForeground(Color.WHITE));
	}


	@Test
	void isLightForeground_allChannelsDark_returnsFalse() {
		Assertions.assertFalse(Util.isLightForeground(Color.BLACK));
	}


	@Test
	void isLightForeground_oneChannelDark_returnsFalse() {
		Assertions.assertFalse(Util.isLightForeground(new Color(0xFF, 0xFF, 0x00)));
	}


	@Test
	void isLightForeground_boundaryValue_isExclusive() {
		// isLightForeground() requires each channel to be strictly > 0xa0.
		Assertions.assertFalse(Util.isLightForeground(new Color(0xa0, 0xa0, 0xa0)));
		Assertions.assertTrue(Util.isLightForeground(new Color(0xa1, 0xa1, 0xa1)));
	}


	@Test
	void stripHtml_null_returnsNull() {
		Assertions.assertNull(Util.stripHtml(null));
	}


	@Test
	void stripHtml_doesNotStartWithHtmlTag_returnsUnchanged() {
		String text = "<b>bold</b> but no leading <html> tag";
		Assertions.assertEquals(text, Util.stripHtml(text));
	}


	@Test
	void stripHtml_startsWithHtmlTag_stripsAllTags() {
		String text = "<html>Hello <b>world</b>!</html>";
		Assertions.assertEquals("Hello world!", Util.stripHtml(text));
	}


	@Test
	void stripHtml_noTagsAfterHtmlPrefix_returnsTextUnchangedMinusPrefixTag() {
		Assertions.assertEquals("Hello world", Util.stripHtml("<html>Hello world"));
	}


	private static final class UiManagerOverride implements AutoCloseable {

		private final Object original;

		UiManagerOverride(Color labelForeground) {
			original = UIManager.get("Label.foreground");
			UIManager.put("Label.foreground", labelForeground);
		}

		@Override
		public void close() {
			UIManager.put("Label.foreground", original);
		}
	}


	@Test
	void getHyperlinkForeground_lightLabelForeground_returnsLightHyperlinkColor() {
		try (UiManagerOverride ignored = new UiManagerOverride(Color.WHITE)) {
			Assertions.assertEquals(Util.LIGHT_HYPERLINK_FG, Util.getHyperlinkForeground());
		}
	}


	@Test
	void getHyperlinkForeground_darkLabelForeground_returnsBlue() {
		try (UiManagerOverride ignored = new UiManagerOverride(Color.BLACK)) {
			Assertions.assertEquals(Color.BLUE, Util.getHyperlinkForeground());
		}
	}


}

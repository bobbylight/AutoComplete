/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class UtilTest {

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
		Assertions.assertTrue(Util.startsWithIgnoreCase("\u0131", "i"));
		Assertions.assertTrue(Util.startsWithIgnoreCase("\u0131", "I"));
		Assertions.assertTrue(Util.startsWithIgnoreCase("i", "\u0131"));
		Assertions.assertTrue(Util.startsWithIgnoreCase("I", "\u0131"));
	}


}

/*
 * This library is distributed under a modified BSD license.  See the included
 * AutoComplete.License.txt file for details.
 */
package org.fife.ui.autocomplete;

import org.junit.Assert;
import org.junit.Test;


public class UtilTest {

	@Test
	public void startsWithIgnoreCase_happyPath() {

		Assert.assertTrue(Util.startsWithIgnoreCase("a", "a"));
		Assert.assertTrue(Util.startsWithIgnoreCase("a", "A"));

		Assert.assertTrue(Util.startsWithIgnoreCase("Hello world", "Hello"));
		Assert.assertTrue(Util.startsWithIgnoreCase("Hello world", "hello"));
		Assert.assertTrue(Util.startsWithIgnoreCase("Hello world", "HELLO"));

	}


	@Test
	public void startsWithIgnoreCase_tricky_iWithoutDot() {
		Assert.assertTrue(Util.startsWithIgnoreCase("\u0131", "i"));
		Assert.assertTrue(Util.startsWithIgnoreCase("\u0131", "I"));
		Assert.assertTrue(Util.startsWithIgnoreCase("i", "\u0131"));
		Assert.assertTrue(Util.startsWithIgnoreCase("I", "\u0131"));
	}


}
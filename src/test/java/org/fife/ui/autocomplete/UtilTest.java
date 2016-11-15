/*
 * This library is distributed under a modified BSD license.  See the included
 * AutoComplete.License.txt file for details.
 */
package org.fife.ui.autocomplete;

import org.junit.Assert;
import org.junit.runners.MethodSorters;

import org.junit.FixMethodOrder;
import org.junit.Test;


/**
 * Test Util.java
 *
 * @author Robert Futrell
 * @author Thomas Wang
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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

    @Test
    public void testCaseSensitiveCompare()
    {
        Assert.assertTrue(Util.cmpString("abc", "abcd") < 0);
        Assert.assertTrue(Util.cmpString("abcd", "abc") > 0);
        Assert.assertTrue(Util.cmpString("abcd", "abcd") == 0);
        Assert.assertTrue(Util.cmpString("", "") == 0);
        Assert.assertTrue(Util.cmpString("x", "") > 0);
        Assert.assertTrue(Util.cmpString("", "y") < 0);
        Assert.assertTrue(Util.cmpString("\u0131", "\u0130") > 0);
    }


    @Test
    public void testCaseInsensitiveCompare()
    {
        Assert.assertEquals(0, Util.cmpIgnoreCase("abd", "aBd"));
        Assert.assertTrue(Util.cmpIgnoreCase("abd", "Abc") > 0);
        Assert.assertTrue(Util.cmpIgnoreCase("Abc", "abd") < 0);
        Assert.assertTrue(Util.cmpIgnoreCase("", "") == 0);
        Assert.assertTrue(Util.cmpIgnoreCase("x", "") > 0);
        Assert.assertTrue(Util.cmpIgnoreCase("", "y") < 0);
        Assert.assertTrue(Util.cmpIgnoreCase("\u0131", "\u0130") == 0);
    }


    @Test
    public void testUniqueCaseInsensitiveCompare()
    {
        Assert.assertEquals(0, Util.cmpUniqueIgnoreCase("abd", "abd"));
        Assert.assertTrue(Util.cmpUniqueIgnoreCase("abd", "Abc") > 0);
        Assert.assertTrue(Util.cmpUniqueIgnoreCase("Abc", "abd") < 0);
        Assert.assertTrue(Util.cmpUniqueIgnoreCase("", "") == 0);
        Assert.assertTrue(Util.cmpUniqueIgnoreCase("x", "") > 0);
        Assert.assertTrue(Util.cmpUniqueIgnoreCase("", "y") < 0);
        Assert.assertTrue(Util.cmpUniqueIgnoreCase("\u0131", "\u0130") > 0);
    }

}
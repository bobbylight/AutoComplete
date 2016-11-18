/*
 * 11/17/2016
 *
 * TestDefaultCompletionProvider.java - test basic completion provider implementation.
 *
 * This library is distributed under a modified BSD license.  See the included
 * AutoComplete.License.txt file for details.
 */

package org.fife.ui.autocomplete;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.junit.Assert;
import org.junit.runners.MethodSorters;

import org.junit.FixMethodOrder;
import org.junit.Test;


/**
 * Test cases for DefaultCompletionProvider
 *
 * @author Thomas Wang
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDefaultCompletionProvider extends DefaultCompletionProvider
{

	private static String myAlreadyEnteredText;

	/**
	 * Artifical result construction method
	 */
	public static void setAlreadyEnteredText(String txt)
	{
		myAlreadyEnteredText = txt;
	}

	/**
	 * Constructor
	 */
	public TestDefaultCompletionProvider()
	{
	}

	/**
	 * part of test scafold
	 */
	@Override
	public String getAlreadyEnteredText(JTextComponent comp)
	{
		return myAlreadyEnteredText;
	}

	/**
	 * basic tests to check the inputText is a prefix in toString()
	 */
	@Test
	public void testBasicPrefixVerification()
	{
		final DefaultCompletionProvider provider =
			new DefaultCompletionProvider();
		final Completion c1 = new BasicCompletion(provider, "for", "(keyword)");
		Assert.assertTrue(c1.toString().startsWith("for"));
		final Completion c2 = new ShorthandCompletion(
			provider, "soda", "sugar", "(sweet)");
		Assert.assertTrue(c2.toString().startsWith("soda"));
		final Completion c3 = new VariableCompletion(provider, "flag", "bool");
		Assert.assertTrue(c3.toString().startsWith("flag"));
		final Completion c4 = new MarkupTagCompletion(provider, "ExtCss");
		Assert.assertTrue(c4.toString().startsWith("ExtCss"));
		final Completion c5 = new FunctionCompletion(provider, "print", "void");
		Assert.assertTrue(c5.toString().startsWith("print"));
		final Completion c6 = new TemplateCompletion(
			provider, "function()", "function() ...", "f() {}");
		Assert.assertTrue(c6.toString().startsWith("function()"));
	}

	// Test case sensitivity of completion removal
	@Test
	public void testCompletionRemoval()
	{
		final DefaultCompletionProvider provider1 =
			new DefaultCompletionProvider();
		final ArrayList<Completion> box1 = new ArrayList<Completion>();
		final Completion c1 = new BasicCompletion(provider1, "BAAA");
		final Completion c2 = new VariableCompletion(provider1, "beta", "int");
		final Completion c3 = new ShorthandCompletion(provider1, "BETA", "BETA");
		box1.add(c1);
		box1.add(c2);
		box1.add(c3);
		provider1.addCompletions(box1);
		final DefaultCompletionProvider provider2 =
			new DefaultCompletionProvider();
		final ArrayList<Completion> box2 = new ArrayList<Completion>();
		final Completion c4 = new ShorthandCompletion(provider2, "alpha", "alpha");
		final Completion c5 = new BasicCompletion(provider2, "ALPHA");
		final Completion c6 = new BasicCompletion(provider2, "ZZZZZ");
		box2.add(c4);
		box2.add(c5);
		box2.add(c6);
		provider2.addCompletions(box2);
		// Check we can remove the "BETA" completion.
		Assert.assertTrue(provider1.removeCompletion(c3));
		// Check that the "beta" completion should remain.
		List<Completion> betaList = provider1.getCompletionByInputText("beta");
		Assert.assertEquals(1, betaList.size());
		for (Completion comp : betaList)
		{
			Assert.assertEquals("beta", comp.getInputText());
		}
		// Check we can remove the "ALPHA" completion.
		Assert.assertTrue(provider2.removeCompletion(c5));
		// Check that the "alpha" completion should remain.
		List<Completion> alphaList = provider2.getCompletionByInputText("alpha");
		Assert.assertEquals(1, alphaList.size());
		for (Completion comp : alphaList)
		{
			Assert.assertEquals("alpha", comp.getInputText());
		}
	}

	// a few matching tests
	@Test
	public void testPrefixMatching1()
	{
		TestDefaultCompletionProvider provider1 = new TestDefaultCompletionProvider();
		// Set up scafold.
		TestDefaultCompletionProvider.setAlreadyEnteredText("");
		List<Completion> emptyList = provider1.getCompletions(new JTextArea());
		Assert.assertEquals(0, emptyList.size());
		final Completion c1 = new BasicCompletion(provider1, "MICRO");
		final Completion c2 = new BasicCompletion(provider1, "Ma");
		final Completion c3 = new BasicCompletion(provider1, "Mi");
		final Completion c4 = new BasicCompletion(provider1, "mI");
		final Completion c5 = new BasicCompletion(provider1, "micro");
		final ArrayList<Completion> box1 = new ArrayList<Completion>();
		box1.add(c2);
		box1.add(c5);
		box1.add(c4);
		box1.add(c1);
		box1.add(c3);
		provider1.addCompletions(box1);
		// Set up scafold.
		TestDefaultCompletionProvider.setAlreadyEnteredText("mic");
		List<Completion> microList = provider1.getCompletions(new JTextArea());
		// "mic" is a prefix of "MICRO" and "micro".
		Assert.assertEquals(2, microList.size());
		for (Completion comp : microList)
		{
			Assert.assertTrue("micro".equalsIgnoreCase(comp.getInputText()));
		}
		// Set up scafold.
		TestDefaultCompletionProvider.setAlreadyEnteredText("mac");
		List<Completion> macroList = provider1.getCompletions(new JTextArea());
		// No hit - "mac" is not a prefix of c1 through c5
		Assert.assertEquals(0, macroList.size());
		// Set up scafold.
		TestDefaultCompletionProvider.setAlreadyEnteredText("");
		// c1 through c5 match
		List<Completion> allList = provider1.getCompletions(new JTextArea());
		Assert.assertEquals(5, allList.size());
	}

	// Test through each addition permutation.
	@Test
	public void testAdditionAndMatching()
	{
		TestDefaultCompletionProvider provider = new TestDefaultCompletionProvider();
		JTextArea area = new JTextArea();
		final Completion c1 = new BasicCompletion(provider, "AllGood");
		final Completion c2 = new BasicCompletion(provider, "Allgood");
		final Completion c3 = new VariableCompletion(provider, "Allgood", "int");
		// Set up scafold.
		TestDefaultCompletionProvider.setAlreadyEnteredText("Allgood");
		provider.addCompletion(c1);
		provider.addCompletion(c2);
		provider.addCompletion(c3);
		Assert.assertEquals(3, provider.getCompletions(area).size());
		provider.clear();
		provider.addCompletion(c3);
		provider.addCompletion(c2);
		provider.addCompletion(c1);
		Assert.assertEquals(3, provider.getCompletions(area).size());
		provider.clear();
		provider.addCompletion(c1);
		provider.addCompletion(c3);
		provider.addCompletion(c2);
		Assert.assertEquals(3, provider.getCompletions(area).size());
		provider.clear();
		provider.addCompletion(c3);
		provider.addCompletion(c1);
		provider.addCompletion(c2);
		Assert.assertEquals(3, provider.getCompletions(area).size());
		provider.clear();
		provider.addCompletion(c2);
		provider.addCompletion(c1);
		provider.addCompletion(c3);
		Assert.assertEquals(3, provider.getCompletions(area).size());
		provider.clear();
		provider.addCompletion(c2);
		provider.addCompletion(c3);
		provider.addCompletion(c1);
		Assert.assertEquals(3, provider.getCompletions(area).size());
		provider.clear();
	}

	@Test
	public void testCompletionOrdering1()
	{
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		final Completion c1 = new FunctionCompletion(provider, "add", "int");
		final Completion c2 = new VariableCompletion(provider, "indx", "int");
		final Completion c3 = new FunctionCompletion(provider, "Sub", "int");
		Assert.assertTrue(c1.compareTo(c2) < 0);
		Assert.assertTrue(c2.compareTo(c3) < 0);
		// (c1 < c2) and (c2 < c3), then it is implied that (c1 < c3)
		Assert.assertTrue(c1.compareTo(c3) < 0);
	}

	@Test
	public void testGetCompletionByInputText()
	{
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		// Test no completions in provider.
		List<Completion> deltaList = provider.getCompletionByInputText("delta");
		Assert.assertNull(deltaList);
		final Completion c1 = new BasicCompletion(provider, "BETA");
		final Completion c2 = new BasicCompletion(provider, "beta");
		final Completion c3 = new BasicCompletion(provider, "ALPHA");
		final Completion c4 = new BasicCompletion(provider, "alpha");
		final Completion c5 = new BasicCompletion(provider, "GAMMA");
		final Completion c6 = new BasicCompletion(provider, "gamma");
		provider.addCompletion(c1);
		provider.addCompletion(c2);
		List<Completion> betaList = provider.getCompletionByInputText("beta");
		// Check for correct properties of returned list.
		Assert.assertEquals(2, betaList.size());
		for (Completion comp : betaList)
		{
			Assert.assertTrue("beta".equalsIgnoreCase(comp.getInputText()));
		}
		provider.addCompletion(c3);
		provider.addCompletion(c4);
		provider.addCompletion(c5);
		provider.addCompletion(c6);
		// Check for correct properties of returned list.
		Assert.assertEquals(2, betaList.size());
		for (Completion comp : betaList)
		{
			Assert.assertTrue("beta".equalsIgnoreCase(comp.getInputText()));
		}
	}

}

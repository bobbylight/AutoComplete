/*
 * 12/21/2008
 *
 * AbstractCompletionProvider.java - Base class for completion providers.
 *
 * This library is distributed under a modified BSD license.  See the included
 * AutoComplete.License.txt file for details.
 */
package org.fife.ui.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.text.JTextComponent;


/**
 * A base class for completion providers.  {@link Completion}s are kept in
 * a sorted list.  To get the list of completions that match a given input,
 * a binary search is done to find the first matching completion, then all
 * succeeding completions that also match are also returned.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class AbstractCompletionProvider
								extends CompletionProviderBase {

	/**
	 * The completions this provider is aware of.
     * This field is now read-only.
	 */
	protected List<Completion> completions;

	/**
	 * Compares a {@link Completion} against a String case insensitively.
	 */
	protected CaseInsensitiveComparator comparator;


    protected static final Comparator<Completion> myNonRawComparator =
        new IgnoreCaseComparator<Completion>();

    /**
     * Declared private to prevent mis-use by sub-classes
     */
    private ArrayList<Completion> myCompletionBox;

	/**
	 * Constructor.
	 */
	public AbstractCompletionProvider() {
		comparator = new CaseInsensitiveComparator();
		clearParameterizedCompletionParams();
		final ArrayList<Completion> completionBox = new ArrayList<Completion>();
        myCompletionBox = completionBox;
        completions = Collections.unmodifiableList(completionBox);
	}

    /**
     * A wrapper Completion class used for input text comparison, not insertion
     */
    protected static final class DummyCompletionWrapper implements Completion {

        private final String myInputText;

        /**
         * Constructor
         *
         * @param inputText input text attribute
         */
        public DummyCompletionWrapper(final String inputText)
        {
            myInputText = inputText;
        }

        /**
         * Compares this completion to another one lexicographically, ignoring
         * case.
         *
         * @param other Another completion instance.
         * @return How this completion compares to the other one.
         */
        public int compareTo(Completion other) {
            String s1 = this.getInputText();
            String s2 = other.getInputText();
            return Util.cmpUniqueIgnoreCase(s1, s2);
        }

        /**
         * Returns the portion of this completion that has already been entered
         * into the text component.  The match is case-insensitive.<p>
         *
         * This is a convenience method for:
         * <code>getProvider().getAlreadyEnteredText(comp)</code>.
         *
         * @param comp The text component.
         * @return The already-entered portion of this completion.
         */
        public String getAlreadyEntered(JTextComponent comp) {
            return "";
        }

        /**
         * Returns the icon to use for this completion.
         *
         * @return The icon, or <code>null</code> for none.
         */
        public Icon getIcon() {
            return null;
        }

        /**
         * Returns the text that the user has to (start) typing for this completion
         * to be offered.  Note that this will usually be the same value as
         * {@link #getReplacementText()}, but not always (a completion could be
         * a way to implement shorthand, for example, "<code>sysout</code>" mapping
         * to "<code>System.out.println(</code>").
         *
         * @return The text the user has to (start) typing for this completion to
         *         be offered.
         * @see #getReplacementText()
         */
        public String getInputText() {
            return myInputText;
        }

        /**
         * Returns the provider that returned this completion.
         *
         * @return The provider.
         */
        public CompletionProvider getProvider() {
            return null;
        }

        /**
         * Returns the "relevance" of this completion.  This is used when sorting
         * completions by their relevance.  It is an abstract concept that may
         * mean different things to different languages, and may depend on the
         * context of the completion.<p>
         *
         * By default, all completions have a relevance of <code>0</code>.  The
         * higher the value returned by this method, the higher up in the list
         * this completion will be; the lower the value returned, the lower it will
         * be.  <code>Completion</code>s with equal relevance values will be
         * sorted alphabetically.
         *
         * @return The relevance of this completion.
         */
        public int getRelevance() {
            return 0;
        }

        /**
         * Returns the text to insert as the result of this auto-completion.  This
         * is the "complete" text, including any text that replaces what the user
         * has already typed.
         *
         * @return The replacement text.
         * @see #getInputText()
         */
        public String getReplacementText() {
            return "";
        }

        /**
         * Returns the description of this auto-complete choice.  This can be
         * used in a popup "description window."
         *
         * @return This item's description.  This should be HTML.  It may be
         *         <code>null</code> if there is no description for this
         *         completion.
         */
        public String getSummary() {
            return null;
        }

        /**
         * Returns the tool tip text to display for mouse hovers over this
         * completion.<p>
         *
         * Note that for this functionality to be enabled, a
         * <tt>JTextComponent</tt> must be registered with the
         * <tt>ToolTipManager</tt>, and the text component must know to search
         * for this value.  In the case of an
         * <a href="http://fifesoft.com/rsyntaxtextarea">RSyntaxTextArea</a>, this
         * can be done with a <tt>org.fife.ui.rtextarea.ToolTipSupplier</tt> that
         * calls into
         * {@link CompletionProvider#getCompletionsAt(JTextComponent, java.awt.Point)}.
         *
         * @return The tool tip text for this completion, or <code>null</code> if
         *         none.
         */
        public String getToolTipText() {
            return null;
        }
    }

	/**
	 * Adds a single completion to this provider.  If you are adding multiple
	 * completions to this provider, for efficiency reasons please consider
	 * using {@link #addCompletions(List)} instead.
	 *
	 * @param c The completion to add.
	 * @throws IllegalArgumentException If the completion's provider isn't
	 *         this <tt>CompletionProvider</tt>.
	 * @see #addCompletions(List)
	 * @see #removeCompletion(Completion)
	 * @see #clear()
	 */
	public void addCompletion(Completion c) {
		checkProviderAndAdd(c);
		// Collections.sort(completions);
	}


	/**
	 * Adds {@link Completion}s to this provider.
	 *
	 * @param completions The completions to add.  This cannot be
	 *        <code>null</code>.
	 * @throws IllegalArgumentException If a completion's provider isn't
	 *         this <tt>CompletionProvider</tt>.
	 * @see #addCompletion(Completion)
	 * @see #removeCompletion(Completion)
	 * @see #clear()
	 */
	public void addCompletions(List<Completion> completions) {
		for (Completion c : completions) {
			checkProviderAndAdd(c);
		}
		// Collections.sort(this.completions);
	}


	/**
	 * Adds simple completions for a list of words.
	 *
	 * @param words The words.
	 * @see BasicCompletion
	 */
	protected void addWordCompletions(String[] words) {
		int count = words==null ? 0 : words.length;
		for (int i=0; i<count; i++) {
			completions.add(new BasicCompletion(this, words[i]));
		}
		// Collections.sort(completions);
	}


	protected void checkProviderAndAdd(Completion c) {
		if (c.getProvider()!=this) {
			throw new IllegalArgumentException("Invalid CompletionProvider");
		}
        final ArrayList<Completion> box = myCompletionBox;
        // Perform binary search to search
		int ptr = Collections.binarySearch(box, c);
		if (ptr >= 0) {
            // CHECKME: duplicates are allowed
            box.add(ptr, c);
		}
        else
        {
            // Find the insertion point.
            ptr = (-ptr) - 1;
		    box.add(ptr, c);
        }
	}


	/**
	 * Removes all completions from this provider.  This does not affect
	 * the parent <tt>CompletionProvider</tt>, if there is one.
	 *
	 * @see #addCompletion(Completion)
	 * @see #addCompletions(List)
	 * @see #removeCompletion(Completion)
	 */
	public void clear() {
        final ArrayList<Completion> box = myCompletionBox;
        box.clear();
	}


	/**
	 * Returns a list of <tt>Completion</tt>s in this provider with the
	 * specified input text.
	 *
	 * @param inputText The input text to search for.
	 * @return A list of {@link Completion}s, or <code>null</code> if there
	 *         are no matching <tt>Completion</tt>s.
	 */
	public List<Completion> getCompletionByInputText(String inputText) {
        final ArrayList<Completion> box = myCompletionBox;
		// Find the target item using binary seach.
		int ptr = Collections.binarySearch(
            box, new DummyCompletionWrapper(inputText), myNonRawComparator);
		if (ptr < 0) { // No exact match
            return null;
		}
        final ArrayList<Completion> retVal = new ArrayList<Completion>();
        // Perform backward search prior to the target.
        for (int indx = ptr - 1; indx >= 0; --indx) {
            final Completion c = box.get(indx);
            if (Util.cmpIgnoreCase(c.getInputText(), inputText) == 0) {
                retVal.add(c); // a match
            }
            else {
                break; // range exceeded
            }
        }
        // Perform forward searc at the target.
        for (int indx = ptr; indx < box.size(); ++indx) {
            final Completion c = box.get(indx);
            if (Util.cmpIgnoreCase(c.getInputText(), inputText) == 0) {
                retVal.add(c); // a match
            }
            else {
                break; // range exceeded
            }
        }
        return (retVal.size() > 0) ? retVal : null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<Completion> getCompletionsImpl(JTextComponent comp) {
		final ArrayList<Completion> retVal = new ArrayList<Completion>();
		final String text = getAlreadyEnteredText(comp);
		if (text!=null) {
            final ArrayList<Completion> box = myCompletionBox;
            // Find the target item using binary seach.
            int ptr = Collections.binarySearch(
                box, new DummyCompletionWrapper(text), myNonRawComparator);
            if (ptr < 0) { // No exact match
                ptr = (-ptr) - 1;
            }
            else
            {
                // Perform backward search prior to the target.
                for (int indx = ptr - 1; indx >= 0; --indx) {
                    final Completion c = box.get(indx);
                    if (Util.startsWithIgnoreCase(c.getInputText(), text)) {
                        retVal.add(c); // a match
                    }
                    else {
                        break; // range exceeded
                    }
                }
            }
            // Perform forward searc at the target.
            for (int indx = ptr; indx < box.size(); ++indx) {
                final Completion c = box.get(indx);
                if (Util.startsWithIgnoreCase(c.getInputText(), text)) {
                    retVal.add(c); // a match
                }
                else {
                    break; // range exceeded
                }
            }
		}
		return retVal;
	}


	/**
	 * Removes the specified completion from this provider.  This method
	 * will not remove completions from the parent provider, if there is one.
	 *
	 * @param c The completion to remove.
	 * @return <code>true</code> if this provider contained the specified
	 *         completion.
	 * @see #clear()
	 * @see #addCompletion(Completion)
	 * @see #addCompletions(List)
	 */
	public boolean removeCompletion(Completion c) {
		// Don't just call completions.remove(c) as it'll be a linear search.
        final ArrayList<Completion> box = myCompletionBox;
        // Find the target item using binary seach.
        int ptr = Collections.binarySearch(box, c);
        if (ptr < 0) {
            return false; // not found
        }
        box.remove(ptr);
        return true;
	}

    /**
     * A non-raw type comparator with case insensitivity property
     * (should not be used for completion update - only retrieval)
     */
    public static final class IgnoreCaseComparator<T extends Completion>
    implements Comparator<T> {
        public int compare(T o1, T o2) {
            String s1 = o1.getInputText();
            String s2 = o2.getInputText();
            return Util.cmpIgnoreCase(s1, s2);
        }
    }

	/**
	 * A comparator that compares the input text of a {@link Completion}
	 * against a String lexicographically, ignoring case.
	 */
	@SuppressWarnings("rawtypes")
	public static class CaseInsensitiveComparator implements Comparator,
														Serializable {

		@Override
		public int compare(Object o1, Object o2) {
			String s1 = o1 instanceof String ? (String)o1 :
							((Completion)o1).getInputText();
			String s2 = o2 instanceof String ? (String)o2 :
							((Completion)o2).getInputText();
			return Util.cmpIgnoreCase(s1, s2);
		}

	}


}
package org.fife.ui.autocomplete;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;


/**
 * The beginnings of a provider that uses reflection to offer completions for
 * all classes in a jar.<p>
 *
 * This class is unfinished and should not be used.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class JarCompletionProvider extends AbstractCompletionProvider {

	private Segment seg;
	private boolean loaded;
	private TreeMap packageSet;


	public JarCompletionProvider() {
		seg = new Segment();
		completions = new ArrayList(0);
		packageSet = new TreeMap();
	}


	public String getAlreadyEnteredText(JTextComponent comp) {

		Document doc = comp.getDocument();

		int dot = comp.getCaretPosition();
		Element root = doc.getDefaultRootElement();
		int index = root.getElementIndex(dot);
		Element elem = root.getElement(index);
		int start = elem.getStartOffset();
		int len = dot-start;
		try {
			doc.getText(start, len, seg);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return EMPTY_STRING;
		}

		int segEnd = seg.offset + len;
		start = segEnd - 1;
		while (start>=seg.offset && isValidChar(seg.array[start])) {
			start--;
		}
		start++;

		len = segEnd - start;
		return len==0 ? EMPTY_STRING : new String(seg.array, start, len);

	}


	private String getAlreadyEnteredText2(JTextComponent comp) {

		Document doc = comp.getDocument();

		int dot = comp.getCaretPosition();
		Element root = doc.getDefaultRootElement();
		int index = root.getElementIndex(dot);
		Element elem = root.getElement(index);
		int start = elem.getStartOffset();
		int len = dot-start;
		try {
			doc.getText(start, len, seg);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return EMPTY_STRING;
		}

		int segEnd = seg.offset + len;
		start = segEnd - 1;
		while (start>=seg.offset && isValidChar2(seg.array[start])) {
			start--;
		}
		start++;

		len = segEnd - start;
		return len==0 ? EMPTY_STRING : new String(seg.array, start, len);

	}


	/**
	 * Does the dirty work of creating a list of completions.
	 *
	 * @param comp The text component to look in.
	 * @return The list of possible completions, or an empty list if there
	 *         are none.
	 */
	protected List getCompletionsImpl(JTextComponent comp) {

		if (!loaded) {
			loadCompletions();
		}

		List retVal = new ArrayList();

		String text2 = getAlreadyEnteredText2(comp);
		String[] pkgNames = splitOnChar(text2, '.');

		TreeMap map = packageSet;
		for (int i=0; i<pkgNames.length-1; i++) {

			Object obj = map.get(pkgNames[i]);

			if (obj==null) {
				return retVal; // empty
			}

			if (obj instanceof TreeMap) {
				map = (TreeMap)obj;
			}
			else {
				return retVal; // Will be empty
			}

		}

		String fromKey = pkgNames[pkgNames.length-1];
		String toKey = fromKey + '{'; // Ascii char > largest valid class char
		SortedMap sm = map.subMap(fromKey, toKey);

		for (Iterator i=sm.keySet().iterator(); i.hasNext(); ) {
			Object obj = i.next();
			retVal.add(new BasicCompletion(this, obj.toString()));
		}

		return retVal;

	}


	/**
	 * {@inheritDoc}
	 */
	public List getParameterizedCompletionsAt(JTextComponent tc) {
		return null; // This provider knows no functions or methods.
	}


	/**
	 * Returns whether the specified character is valid in an auto-completion.
	 *
	 * @param ch The character.
	 * @return Whether the character is valid.
	 */
	protected boolean isValidChar(char ch) {
		return Character.isLetterOrDigit(ch) || ch=='_';
	}


	/**
	 * Returns whether the specified character is valid in an auto-completion.
	 *
	 * @param ch The character.
	 * @return Whether the character is valid.
	 */
	protected boolean isValidChar2(char ch) {
		return Character.isLetterOrDigit(ch) || ch=='.' || ch=='_';
	}


	protected void loadCompletions() {

		loaded = true;

		String javaHome = System.getProperty("java.home");
		//System.out.println(javaHome);
		JarFile jar = null;
		try {
			jar = new JarFile(new File(javaHome, "lib/rt.jar"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		java.util.Enumeration e = jar.entries();
		while (e.hasMoreElements()) {
			ZipEntry entry = (ZipEntry)e.nextElement();
			String entryName = entry.getName();
			if (entryName.endsWith(".class")) {
				entryName = entryName.substring(0, entryName.length()-6);
				String[] items = splitOnChar(entryName, '/');
				TreeMap m = packageSet;
				for (int i=0; i<items.length-1; i++) {
					TreeMap submap = (TreeMap)m.get(items[i]);
					if (submap==null) {
						submap = new TreeMap();
						m.put(items[i], submap);
					}
					m = submap;
				}
				String className = items[items.length-1];
				m.put(className, null);
			}
		}

	}


	/**
	 * A faster way to split on a single char than String#split(), since
	 * we'll be doing this in a tight loop possibly thousands of times (rt.jar).
	 *
	 * @param str The string to split.
	 * @return The string, split on '<tt>/</tt>'.
	 */
	private String[] splitOnChar(String str, int ch) {
		List list = new ArrayList(3);
		int pos = 0;
		int old = 0;
		while ((pos=str.indexOf(ch, old))>-1) {
			list.add(str.substring(old, pos));
			old = pos+1;
		}
		// If str ends in ch, this adds an empty item to the end of the list.
		// This is what we want.
		list.add(str.substring(old));
		String[] array = new String[list.size()];
		return (String[])list.toArray(array);
	}

/*
	private class ClassOrInterfaceCompletion extends AbstractCompletion {

		private String replacementText;

		public ClassOrInterfaceCompletion(String name) {
			super(JarCompletionProvider.this);
			this.replacementText = name;
		}

		public String getReplacementText() {
			return replacementText;
		}

		public String getSummary() {
			return null;
		}
		
	}


	private class PackageCompletion extends AbstractCompletion {

		private String replacementText;

		public PackageCompletion(String name) {
			super(JarCompletionProvider.this);
			this.replacementText = name;
		}

		public String getReplacementText() {
			return replacementText;
		}

		public String getSummary() {
			return null;
		}
		
	}
*/

}
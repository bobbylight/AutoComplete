package org.fife.ui.autocomplete;

import java.util.Collections;
import java.util.List;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;


/**
 * A base class for all standard completion providers.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class CompletionProviderBase implements CompletionProvider {

	/**
	 * The parent completion provider.
	 */
	private CompletionProvider parent;

	/**
	 * The renderer to use for completions from this provider.  If this is
	 * <code>null</code>, a default renderer is used.
	 */
	private ListCellRenderer listCellRenderer;

	protected static final String EMPTY_STRING = "";


	/**
	 * {@inheritDoc}
	 */
	public List getCompletions(JTextComponent comp) {
		List completions = getCompletionsImpl(comp);
		if (parent!=null) {
			completions.addAll(parent.getCompletions(comp));
			Collections.sort(completions);
		}
		return completions;
	}


	/**
	 * Does the dirty work of creating a list of completions.
	 *
	 * @param comp The text component to look in.
	 * @return The list of possible completions, or an empty list if there
	 *         are none.
	 */
	protected abstract List getCompletionsImpl(JTextComponent comp);


	/**
	 * {@inheritDoc}
	 */
	public ListCellRenderer getListCellRenderer() {
		return listCellRenderer;
	}


	/**
	 * {@inheritDoc}
	 */
	public CompletionProvider getParent() {
		return parent;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setListCellRenderer(ListCellRenderer r) {
		listCellRenderer = r;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setParent(CompletionProvider parent) {
		this.parent = parent;
	}


}
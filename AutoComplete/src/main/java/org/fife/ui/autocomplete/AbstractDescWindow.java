/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import org.fife.ui.rsyntaxtextarea.PopupWindowDecorator;

import javax.swing.*;
import java.awt.*;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import java.awt.Window;

/**
 * An abstract base class for description windows used to display completion documentation.
 * <p>
 * This class provides standard window initialization, decoration setup, and LookAndFeel
 * update mechanisms while delegating content rendering and text copying actions to concrete subclasses.
 * </p>
 *
 * @author Mattia Marelli
 * @since 2026
 */
public abstract class AbstractDescWindow extends JWindow {

	/**
	 * The parent {@link AutoCompletion} instance managing this description window.
	 */
	protected final AutoCompletion ac;

	/**
	 * Constructs a new description window.
	 *
	 * @param owner the parent {@link Window} owner for this dialog, typically the completion popup window.
	 * @param ac the parent {@link AutoCompletion} instance.
	 */
	public AbstractDescWindow(Window owner, AutoCompletion ac) {
		super(owner);

		this.ac = ac;
		this.setFocusableWindowState(false);

		if (Util.getShouldAllowDecoratingMainAutoCompleteWindows()) {
			PopupWindowDecorator decorator = PopupWindowDecorator.get();
			if (decorator != null) {
				decorator.decorate(this);
			}
		}
	}

	/**
	 * Creates a {@link SizeGrip} component styled with the background color defined by
	 * the parent {@link AutoCompletion} instance.
	 *
	 * @return a new {@link JPanel} acting as a resize grip.
	 */
	protected final JPanel createSizeGrip() {
		SizeGrip rp = new SizeGrip();
		rp.setBackground(ac.getDescWindowColor());
		return rp;
	}

	/**
	 * Updates the UI hierarchy of this window when the {@link LookAndFeel} changes.
	 * <p>
	 * Typically invoked by the parent completion popup window to ensure color schemes
	 * and component styles remain consistent across LookAndFeel shifts.
	 * </p>
	 */
	public void updateUI() {
		SwingUtilities.updateComponentTreeUI(this);
	}

	/**
	 * Copies the currently selected text from the description component to the system clipboard.
	 * <p>
	 * This method performs a copy operation only if the description window is visible
	 * and a non-empty selection exists within the description area.
	 * </p>
	 *
	 * @return {@code true} if text was successfully copied to the clipboard; {@code false} otherwise.
	 */
	public abstract boolean copy();

	/**
	 * Sets the completion item whose description should be displayed in this window.
	 * <p>
	 * Implementing classes should update the UI content accordingly based on the provided
	 * {@link Completion} instance (or clear/hide the content if {@code item} is {@code null}).
	 * </p>
	 *
	 * @param item the {@link Completion} instance to render, or {@code null} to clear the description.
	 */
	public abstract void setDescriptionFor(Completion item);
}
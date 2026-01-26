package org.fife.ui.autocomplete;

import org.fife.ui.rsyntaxtextarea.PopupWindowDecorator;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractDescriptionWindow extends JWindow {

	public AbstractDescriptionWindow(Window owner) {
		super(owner);

		assert owner instanceof AutoCompletePopupWindow;

		setFocusableWindowState(false);

		if (Util.getShouldAllowDecoratingMainAutoCompleteWindows()) {
			PopupWindowDecorator decorator = PopupWindowDecorator.get();
			if (decorator != null) {
				decorator.decorate(this);
			}
		}
	}

	/**
	 * Creates the {@link SizeGrip} with the background specified in the {@link AutoCompletion}
	 * @return A new {@link SizeGrip}
	 */
	public JPanel createSizeGrip() {
		SizeGrip rp = new SizeGrip();
		rp.setBackground(getAutoCompletion().getDescWindowColor());
		return rp;
	}

	public AutoCompletion getAutoCompletion() {
		if (getOwner() instanceof AutoCompletePopupWindow) {
			AutoCompletePopupWindow popupWindow = (AutoCompletePopupWindow) getOwner();
			return popupWindow.getAutoCompletion();
		} else {
			throw new IllegalStateException("The owner is not a AutoCompletePopupWindow");
		}
	}

	/**
	 * Called by the parent completion popup window the {@link LookAndFeel} is updated.
	 */
	public void updateUI() {
		SwingUtilities.updateComponentTreeUI(this);
	}

	/**
	 * Copies from the description text area, if it is visible and there is	a selection.
	 *
	 * @return Whether a copy occurred.
	 */
	public abstract boolean copy();

	/**
	 * Sets the description displayed in this window.
	 *
	 * @param item The item whose description you want to display.
	 */
	public abstract void setDescriptionFor(Completion item);
}

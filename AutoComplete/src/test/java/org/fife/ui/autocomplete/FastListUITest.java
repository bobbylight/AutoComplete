/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class FastListUITest {


	/**
	 * A renderer whose preferred height differs for the first item versus
	 * all others, so tests can tell whether {@code FastListUI} estimated
	 * cell sizes from the first item alone, or computed them for real.
	 */
	private static final class VariableHeightRenderer extends JLabel
			implements ListCellRenderer<Object> {

		private static final int FIRST_ITEM_HEIGHT = 20;
		private static final int OTHER_ITEM_HEIGHT = 60;

		private int preferredHeight = FIRST_ITEM_HEIGHT;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			setText(String.valueOf(value));
			preferredHeight = index == 0 ? FIRST_ITEM_HEIGHT : OTHER_ITEM_HEIGHT;
			return this;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(100, preferredHeight);
		}
	}


	private static JList<String> createList(int itemCount) {
		DefaultListModel<String> model = new DefaultListModel<>();
		for (int i = 0; i < itemCount; i++) {
			model.addElement("item " + i);
		}
		JList<String> list = new JList<>(model);
		list.setCellRenderer(new VariableHeightRenderer());
		return list;
	}


	@Test
	void installUI_doesNotThrow() {
		JList<String> list = createList(5);
		Assertions.assertDoesNotThrow(() -> list.setUI(new FastListUI()));
	}


	/**
	 * Reads {@code FastListUI}'s private {@code overriddenBackground}/{@code overriddenForeground}
	 * flags via reflection. These are the only direct evidence that the {@code if} branches in
	 * {@code installDefaults()} actually ran: {@code BasicListUI.installDefaults()} (called first,
	 * via {@code super}) backfills {@code list.getSelectionBackground()}/{@code Foreground()} from
	 * {@code UIManager} whenever the current value is {@code null} <em>or</em> a {@code UIResource} -
	 * which is true for a freshly-constructed {@code JList} even if you explicitly set the color to
	 * {@code null} beforehand. So on every real-world L&F that defines "List.selectionBackground"/
	 * "List.selectionForeground" (i.e. everything except Nimbus), the value is never actually still
	 * {@code null} by the time {@code FastListUI}'s own check runs - asserting the resulting color is
	 * non-null doesn't prove {@code FastListUI}'s branch fired; it only proves {@code super} did its
	 * job. To genuinely exercise these branches, the relevant {@code UIManager} key must be removed
	 * first, simulating a L&F that doesn't define it (the Nimbus scenario this class's Javadoc
	 * describes).
	 */
	private static boolean getOverriddenFlag(FastListUI ui, String fieldName) throws ReflectiveOperationException {
		java.lang.reflect.Field field = FastListUI.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.getBoolean(ui);
	}


	/**
	 * Removes {@code key} from the current Look and Feel's own {@code UIDefaults} table, so that
	 * {@code UIManager.getColor(key)} resolves to {@code null}. Note that {@code UIManager.put(key,
	 * null)} does *not* achieve this: per its docs, a {@code null} value there just removes any
	 * *override* sitting on top of the L&F's table, and the lookup then falls through to the L&F's
	 * own (still-present) value underneath - which is exactly the trap that produced the flawed
	 * tests this method replaces.
	 */
	private static Object removeLookAndFeelColor(String key) {
		Object saved = UIManager.getLookAndFeelDefaults().get(key);
		UIManager.getLookAndFeelDefaults().remove(key);
		return saved;
	}


	private static void restoreLookAndFeelColor(String key, Object saved) {
		UIManager.getLookAndFeelDefaults().put(key, saved);
	}


	@Test
	void installDefaults_doesNotOverrideExistingSelectionColors() {
		JList<String> list = createList(5);
		list.setSelectionBackground(Color.RED);
		list.setSelectionForeground(Color.BLUE);

		list.setUI(new FastListUI());

		Assertions.assertEquals(Color.RED, list.getSelectionBackground());
		Assertions.assertEquals(Color.BLUE, list.getSelectionForeground());
	}


	@Test
	void installDefaults_lookAndFeelDefinesNeitherSelectionColor_overridesBoth() throws ReflectiveOperationException {
		JList<String> list = createList(5);

		Object savedBg = removeLookAndFeelColor("List.selectionBackground");
		Object savedFg = removeLookAndFeelColor("List.selectionForeground");
		try {
			FastListUI ui = new FastListUI();
			list.setUI(ui);

			Assertions.assertTrue(getOverriddenFlag(ui, "overriddenBackground"));
			Assertions.assertTrue(getOverriddenFlag(ui, "overriddenForeground"));
			// determineSelectionBackground()/Foreground() must return plain Colors, not
			// ColorUIResources, so a later LAF switch can't silently wipe them back out
			// (see the class-level comment on determineSelectionBackground()).
			Assertions.assertFalse(list.getSelectionBackground() instanceof javax.swing.plaf.UIResource);
			Assertions.assertFalse(list.getSelectionForeground() instanceof javax.swing.plaf.UIResource);
		} finally {
			restoreLookAndFeelColor("List.selectionBackground", savedBg);
			restoreLookAndFeelColor("List.selectionForeground", savedFg);
		}
	}


	@Test
	void installDefaults_lookAndFeelDefinesOnlySelectionForeground_overridesOnlyBackground()
			throws ReflectiveOperationException {
		JList<String> list = createList(5);

		Object savedBg = removeLookAndFeelColor("List.selectionBackground");
		try {
			FastListUI ui = new FastListUI();
			list.setUI(ui);

			Assertions.assertTrue(getOverriddenFlag(ui, "overriddenBackground"));
			Assertions.assertFalse(getOverriddenFlag(ui, "overriddenForeground"));
			Assertions.assertNotNull(list.getSelectionBackground());
		} finally {
			restoreLookAndFeelColor("List.selectionBackground", savedBg);
		}
	}


	@Test
	void installDefaults_lookAndFeelDefinesOnlySelectionBackground_overridesOnlyForeground()
			throws ReflectiveOperationException {
		JList<String> list = createList(5);

		Object savedFg = removeLookAndFeelColor("List.selectionForeground");
		try {
			FastListUI ui = new FastListUI();
			list.setUI(ui);

			Assertions.assertFalse(getOverriddenFlag(ui, "overriddenBackground"));
			Assertions.assertTrue(getOverriddenFlag(ui, "overriddenForeground"));
			Assertions.assertNotNull(list.getSelectionForeground());
		} finally {
			restoreLookAndFeelColor("List.selectionForeground", savedFg);
		}
	}


	/**
	 * {@code BasicListUI.uninstallDefaults()} (called via {@code super}) only clears
	 * {@code selectionBackground}/{@code Foreground} when they're {@code UIResource} instances - but
	 * the colors {@code FastListUI} installs are deliberately plain {@code Color}s (see
	 * {@code determineSelectionBackground()}/{@code Foreground()}), so {@code super} can't clean
	 * them up on its own. This is exactly why {@code FastListUI} needs its own
	 * {@code overriddenBackground}/{@code overriddenForeground}-guarded clearing logic.
	 */
	@Test
	void uninstallDefaults_clearsColorsThisUiOverrode() {
		JList<String> list = createList(5);

		Object savedBg = removeLookAndFeelColor("List.selectionBackground");
		Object savedFg = removeLookAndFeelColor("List.selectionForeground");
		try {
			FastListUI ui = new FastListUI();
			list.setUI(ui);
			Assertions.assertNotNull(list.getSelectionBackground());
			Assertions.assertNotNull(list.getSelectionForeground());

			list.setUI(null); // triggers uninstallUI() -> uninstallDefaults()

			Assertions.assertNull(list.getSelectionBackground());
			Assertions.assertNull(list.getSelectionForeground());
		} finally {
			restoreLookAndFeelColor("List.selectionBackground", savedBg);
			restoreLookAndFeelColor("List.selectionForeground", savedFg);
		}
	}


	@Test
	void uninstallDefaults_doesNotClearUserSuppliedColors() {
		JList<String> list = createList(5);
		list.setSelectionBackground(Color.RED);
		list.setSelectionForeground(Color.BLUE);

		list.setUI(new FastListUI());
		list.setUI(null); // triggers uninstallUI() -> uninstallDefaults()

		Assertions.assertEquals(Color.RED, list.getSelectionBackground());
		Assertions.assertEquals(Color.BLUE, list.getSelectionForeground());
	}


	@Test
	void updateLayoutState_belowEstimationThreshold_computesActualCellHeights() {
		// FastListUI.ESTIMATION_THRESHOLD is 200; stay comfortably under it.
		JList<String> list = createList(10);
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setSize(300, 300);
		list.setUI(new FastListUI());

		Rectangle row0Bounds = list.getUI().getCellBounds(list, 0, 0);
		Rectangle row1Bounds = list.getUI().getCellBounds(list, 1, 1);

		Assertions.assertEquals(VariableHeightRenderer.FIRST_ITEM_HEIGHT, row0Bounds.height);
		Assertions.assertEquals(VariableHeightRenderer.OTHER_ITEM_HEIGHT, row1Bounds.height);
	}


	@Test
	void updateLayoutState_atOrAboveEstimationThreshold_estimatesUniformCellHeightFromFirstItem() {
		JList<String> list = createList(250);
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setSize(300, 300);
		list.setUI(new FastListUI());

		Rectangle row0Bounds = list.getUI().getCellBounds(list, 0, 0);
		Rectangle row1Bounds = list.getUI().getCellBounds(list, 1, 1);

		// Estimation assumes every row is as tall as the first row, even
		// though row 1's renderer reports a taller preferred size.
		Assertions.assertEquals(VariableHeightRenderer.FIRST_ITEM_HEIGHT, row0Bounds.height);
		Assertions.assertEquals(VariableHeightRenderer.FIRST_ITEM_HEIGHT, row1Bounds.height);
	}


	@Test
	void updateLayoutState_atOrAboveEstimationThreshold_usesViewportWidthWhenAvailable() {
		JList<String> list = createList(250);
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setSize(300, 300);
		scrollPane.getViewport().setSize(250, 300);
		list.setUI(new FastListUI());

		// The estimated cell width feeds into the list's own preferred size
		// (used for scrolling/layout purposes), not the value returned by
		// getCellBounds() - that's derived from the list's own width instead.
		Dimension preferredSize = list.getUI().getPreferredSize(list);
		Assertions.assertEquals(250, preferredSize.width);
	}


	@Test
	void updateLayoutState_atOrAboveEstimationThreshold_withoutViewportParent_doesNotThrow() {
		JList<String> list = createList(250);
		list.setSize(300, 300);
		Assertions.assertDoesNotThrow(() -> list.setUI(new FastListUI()));
	}


}

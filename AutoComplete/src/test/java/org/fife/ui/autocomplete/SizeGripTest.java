/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import org.fife.ui.rsyntaxtextarea.OS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


@ExtendWith(SwingRunnerExtension.class)
class SizeGripTest {


	private static Image getOsxSizeGrip(SizeGrip grip) throws ReflectiveOperationException {
		Field field = SizeGrip.class.getDeclaredField("osxSizeGrip");
		field.setAccessible(true);
		return (Image) field.get(grip);
	}


	/**
	 * Creates a {@code SizeGrip} while {@link OS#get()} is mocked to return
	 * the given value.  This is necessary since {@code JPanel}'s constructor
	 * calls {@code updateUI()} itself, so simply mocking around a later,
	 * explicit call to {@code updateUI()} isn't enough to deterministically
	 * test either branch - the constructor's implicit call would already run
	 * against the real, unmocked OS.
	 */
	private static SizeGrip newSizeGrip(OS os) {
		try (MockedStatic<OS> mockedOs = Mockito.mockStatic(OS.class)) {
			mockedOs.when(OS::get).thenReturn(os);
			return new SizeGrip();
		}
	}


	private static void updateUiWithOs(SizeGrip grip, OS os) {
		try (MockedStatic<OS> mockedOs = Mockito.mockStatic(OS.class)) {
			mockedOs.when(OS::get).thenReturn(os);
			grip.updateUI();
		}
	}


	@Test
	void constructor_setsPreferredSize() {
		SizeGrip grip = new SizeGrip();
		Assertions.assertEquals(new Dimension(16, 16), grip.getPreferredSize());
	}


	@Test
	void applyComponentOrientation_ltr_setsNwResizeCursor() {
		SizeGrip grip = new SizeGrip();
		grip.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		Assertions.assertEquals(Cursor.NW_RESIZE_CURSOR, grip.getCursor().getType());
	}


	@Test
	void applyComponentOrientation_rtl_setsNeResizeCursor() {
		SizeGrip grip = new SizeGrip();
		grip.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		Assertions.assertEquals(Cursor.NE_RESIZE_CURSOR, grip.getCursor().getType());
	}


	@Test
	void possiblyFixCursor_onlyChangesCursorWhenNeeded() {
		SizeGrip grip = new SizeGrip();
		grip.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
		grip.possiblyFixCursor(true);
		Assertions.assertEquals(Cursor.NW_RESIZE_CURSOR, grip.getCursor().getType());
		grip.possiblyFixCursor(false);
		Assertions.assertEquals(Cursor.NE_RESIZE_CURSOR, grip.getCursor().getType());
	}


	@Test
	void updateUI_doesNotThrow() {
		SizeGrip grip = new SizeGrip();
		Assertions.assertDoesNotThrow(grip::updateUI);
	}


	@Test
	void updateUI_macOs_loadsOsxSizeGripImage() throws ReflectiveOperationException {
		// Start on a non-mac OS so the field is deterministically null before
		// the assertion, regardless of the OS actually running this test.
		SizeGrip grip = newSizeGrip(OS.LINUX);
		Assertions.assertNull(getOsxSizeGrip(grip));

		updateUiWithOs(grip, OS.MAC_OS_X);
		Assertions.assertNotNull(getOsxSizeGrip(grip));
	}


	@Test
	void updateUI_nonMacOs_leavesOsxSizeGripImageNull() throws ReflectiveOperationException {
		SizeGrip grip = newSizeGrip(OS.LINUX);
		Assertions.assertNull(getOsxSizeGrip(grip));

		updateUiWithOs(grip, OS.WINDOWS);
		Assertions.assertNull(getOsxSizeGrip(grip));
	}


	@Test
	void updateUI_switchingFromMacToNonMac_clearsOsxSizeGripImage()
			throws ReflectiveOperationException {
		SizeGrip grip = newSizeGrip(OS.MAC_OS_X);
		Assertions.assertNotNull(getOsxSizeGrip(grip));

		updateUiWithOs(grip, OS.WINDOWS);
		Assertions.assertNull(getOsxSizeGrip(grip));
	}


	@Test
	void paintComponent_nonMacOs_ltr_doesNotThrow() throws ReflectiveOperationException {
		SizeGrip grip = newSizeGrip(OS.LINUX);
		Assertions.assertNull(getOsxSizeGrip(grip)); // sanity check: fillRect path
		grip.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		grip.setSize(16, 16);
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		try {
			Assertions.assertDoesNotThrow(() -> grip.paint(g));
		} finally {
			g.dispose();
		}
	}


	@Test
	void paintComponent_nonMacOs_rtl_doesNotThrow() throws ReflectiveOperationException {
		SizeGrip grip = newSizeGrip(OS.LINUX);
		Assertions.assertNull(getOsxSizeGrip(grip)); // sanity check: fillRect path
		grip.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		grip.setSize(16, 16);
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		try {
			Assertions.assertDoesNotThrow(() -> grip.paint(g));
		} finally {
			g.dispose();
		}
	}


	@Test
	void paintComponent_macOs_doesNotThrow() throws ReflectiveOperationException {
		SizeGrip grip = newSizeGrip(OS.MAC_OS_X);
		Assertions.assertNotNull(getOsxSizeGrip(grip)); // sanity check: drawImage path
		grip.setSize(16, 16);
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		try {
			Assertions.assertDoesNotThrow(() -> grip.paint(g));
		} finally {
			g.dispose();
		}
	}


}

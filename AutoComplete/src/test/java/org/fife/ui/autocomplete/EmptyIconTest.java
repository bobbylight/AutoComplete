/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.io.Serializable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class EmptyIconTest {


	@Test
	void constructor_setsSize() {
		EmptyIcon icon = new EmptyIcon(16);
		Assertions.assertEquals(16, icon.getIconWidth());
		Assertions.assertEquals(16, icon.getIconHeight());
	}


	@Test
	void setSize_updatesWidthAndHeight() {
		EmptyIcon icon = new EmptyIcon(16);
		icon.setSize(32);
		Assertions.assertEquals(32, icon.getIconWidth());
		Assertions.assertEquals(32, icon.getIconHeight());
	}


	@Test
	void paintIcon_doesNothingAndDoesNotThrow() {
		EmptyIcon icon = new EmptyIcon(16);
		Assertions.assertDoesNotThrow(() -> icon.paintIcon(null, null, 0, 0));
	}


	@Test
	void isSerializable() {
		Assertions.assertInstanceOf(Serializable.class, new EmptyIcon(16));
	}


}

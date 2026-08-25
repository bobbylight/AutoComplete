/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class AutoCompletionStyleContextTest {


	@AfterEach
	void tearDown() {
		UIManager.put(AutoCompletionStyleContext.PROPERTY_PARAMETER_OUTLINE_COLOR, null);
		UIManager.put(AutoCompletionStyleContext.PROPERTY_PARAMETER_COPY_COLOR, null);
		UIManager.put(AutoCompletionStyleContext.PROPERTY_PARAMETERIZED_COMPLETION_CURSOR_POSITION_COLOR, null);
	}


	@Test
	void constructor_noUiManagerValues_usesHardcodedDefaults() {
		AutoCompletionStyleContext context = new AutoCompletionStyleContext();
		Assertions.assertEquals(Color.GRAY, context.getParameterOutlineColor());
		Assertions.assertEquals(new Color(0xb4d7ff), context.getParameterCopyColor());
		Assertions.assertEquals(new Color(0x00b400), context.getParameterizedCompletionCursorPositionColor());
	}


	@Test
	void constructor_noUiManagerValues_cachesDefaultsInUiManagerAsColorUIResource() {
		new AutoCompletionStyleContext();

		Color outline = UIManager.getColor(AutoCompletionStyleContext.PROPERTY_PARAMETER_OUTLINE_COLOR);
		Color copy = UIManager.getColor(AutoCompletionStyleContext.PROPERTY_PARAMETER_COPY_COLOR);
		Color cursor = UIManager.getColor(
			AutoCompletionStyleContext.PROPERTY_PARAMETERIZED_COMPLETION_CURSOR_POSITION_COLOR);

		Assertions.assertInstanceOf(ColorUIResource.class, outline);
		Assertions.assertInstanceOf(ColorUIResource.class, copy);
		Assertions.assertInstanceOf(ColorUIResource.class, cursor);
		Assertions.assertEquals(Color.GRAY, outline);
		Assertions.assertEquals(new Color(0xb4d7ff), copy);
		Assertions.assertEquals(new Color(0x00b400), cursor);
	}


	@Test
	void constructor_uiManagerValuesPresent_usesUiManagerValuesInsteadOfDefaults() {
		Color outline = new Color(1, 2, 3);
		Color copy = new Color(4, 5, 6);
		Color cursor = new Color(7, 8, 9);
		UIManager.put(AutoCompletionStyleContext.PROPERTY_PARAMETER_OUTLINE_COLOR, outline);
		UIManager.put(AutoCompletionStyleContext.PROPERTY_PARAMETER_COPY_COLOR, copy);
		UIManager.put(AutoCompletionStyleContext.PROPERTY_PARAMETERIZED_COMPLETION_CURSOR_POSITION_COLOR, cursor);

		AutoCompletionStyleContext context = new AutoCompletionStyleContext();

		Assertions.assertEquals(outline, context.getParameterOutlineColor());
		Assertions.assertEquals(copy, context.getParameterCopyColor());
		Assertions.assertEquals(cursor, context.getParameterizedCompletionCursorPositionColor());
	}


	@Test
	void constructor_uiManagerValuePresent_doesNotOverwriteExistingUiManagerValue() {
		Color outline = new Color(1, 2, 3);
		UIManager.put(AutoCompletionStyleContext.PROPERTY_PARAMETER_OUTLINE_COLOR, outline);

		new AutoCompletionStyleContext();

		Assertions.assertSame(outline,
			UIManager.getColor(AutoCompletionStyleContext.PROPERTY_PARAMETER_OUTLINE_COLOR));
	}


	@Test
	void getSetParameterCopyColor_roundTrips() {
		AutoCompletionStyleContext context = new AutoCompletionStyleContext();
		Color color = new Color(10, 20, 30);
		context.setParameterCopyColor(color);
		Assertions.assertEquals(color, context.getParameterCopyColor());
	}


	@Test
	void getSetParameterOutlineColor_roundTrips() {
		AutoCompletionStyleContext context = new AutoCompletionStyleContext();
		Color color = new Color(11, 21, 31);
		context.setParameterOutlineColor(color);
		Assertions.assertEquals(color, context.getParameterOutlineColor());
	}


	@Test
	void getSetParameterizedCompletionCursorPositionColor_roundTrips() {
		AutoCompletionStyleContext context = new AutoCompletionStyleContext();
		Color color = new Color(12, 22, 32);
		context.setParameterizedCompletionCursorPositionColor(color);
		Assertions.assertEquals(color, context.getParameterizedCompletionCursorPositionColor());
	}


}

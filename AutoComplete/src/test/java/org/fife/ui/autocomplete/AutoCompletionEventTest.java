/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class AutoCompletionEventTest {


	@Test
	void constructor_popupShown_setsSourceAndType() {
		AutoCompletion source = new AutoCompletion(new DefaultCompletionProvider());
		AutoCompletionEvent event =
			new AutoCompletionEvent(source, AutoCompletionEvent.Type.POPUP_SHOWN);
		Assertions.assertSame(source, event.getSource());
		Assertions.assertEquals(AutoCompletionEvent.Type.POPUP_SHOWN, event.getEventType());
	}


	@Test
	void constructor_popupHidden_setsSourceAndType() {
		AutoCompletion source = new AutoCompletion(new DefaultCompletionProvider());
		AutoCompletionEvent event =
			new AutoCompletionEvent(source, AutoCompletionEvent.Type.POPUP_HIDDEN);
		Assertions.assertSame(source, event.getSource());
		Assertions.assertEquals(AutoCompletionEvent.Type.POPUP_HIDDEN, event.getEventType());
	}


	@Test
	void getAutoCompletion_returnsSourceCastAsAutoCompletion() {
		AutoCompletion source = new AutoCompletion(new DefaultCompletionProvider());
		AutoCompletionEvent event =
			new AutoCompletionEvent(source, AutoCompletionEvent.Type.POPUP_SHOWN);
		Assertions.assertSame(source, event.getAutoCompletion());
	}


	@Test
	void constructor_nullSource_throwsIllegalArgumentException() {
		Assertions.assertThrows(IllegalArgumentException.class,
			() -> new AutoCompletionEvent(null, AutoCompletionEvent.Type.POPUP_SHOWN));
	}


}

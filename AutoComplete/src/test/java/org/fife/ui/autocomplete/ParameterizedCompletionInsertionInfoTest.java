/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import javax.swing.text.Position;

import org.fife.ui.autocomplete.ParameterizedCompletionInsertionInfo.ReplacementCopy;
import org.fife.ui.rtextarea.DocumentRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ParameterizedCompletionInsertionInfoTest {


	private static Position fixedPosition(int offset) {
		return () -> offset;
	}


	@Test
	void constructor_hasNoSelectionByDefault() {
		ParameterizedCompletionInsertionInfo info = new ParameterizedCompletionInsertionInfo();
		Assertions.assertFalse(info.hasSelection());
		Assertions.assertEquals(0, info.getSelectionStart());
		Assertions.assertEquals(0, info.getSelectionEnd());
	}


	@Test
	void constructor_hasNoReplacementsOrCopiesByDefault() {
		ParameterizedCompletionInsertionInfo info = new ParameterizedCompletionInsertionInfo();
		Assertions.assertEquals(0, info.getReplacementCount());
		Assertions.assertEquals(0, info.getReplacementCopyCount());
	}


	@Test
	void getSetTextToInsert_roundTrips() {
		ParameterizedCompletionInsertionInfo info = new ParameterizedCompletionInsertionInfo();
		Assertions.assertNull(info.getTextToInsert());
		info.setTextToInsert("foo()");
		Assertions.assertEquals("foo()", info.getTextToInsert());
	}


	@Test
	void setInitialSelection_setsStartAndEndAndHasSelection() {
		ParameterizedCompletionInsertionInfo info = new ParameterizedCompletionInsertionInfo();
		info.setInitialSelection(3, 7);
		Assertions.assertEquals(3, info.getSelectionStart());
		Assertions.assertEquals(7, info.getSelectionEnd());
		Assertions.assertTrue(info.hasSelection());
	}


	@Test
	void setInitialSelection_equalStartAndEnd_hasNoSelection() {
		ParameterizedCompletionInsertionInfo info = new ParameterizedCompletionInsertionInfo();
		info.setInitialSelection(5, 5);
		Assertions.assertFalse(info.hasSelection());
	}


	@Test
	void setCaretRange_getMinOffsetAndMaxOffset_roundTrip() {
		ParameterizedCompletionInsertionInfo info = new ParameterizedCompletionInsertionInfo();
		Position maxOffs = fixedPosition(10);
		info.setCaretRange(2, maxOffs);
		Assertions.assertEquals(2, info.getMinOffset());
		Assertions.assertSame(maxOffs, info.getMaxOffset());
	}


	@Test
	void getDefaultEndOffs_noExplicitValueSet_fallsBackToMaxOffset() {
		ParameterizedCompletionInsertionInfo info = new ParameterizedCompletionInsertionInfo();
		info.setCaretRange(0, fixedPosition(12));
		Assertions.assertEquals(12, info.getDefaultEndOffs());
	}


	@Test
	void setDefaultEndOffs_overridesMaxOffsetFallback() {
		ParameterizedCompletionInsertionInfo info = new ParameterizedCompletionInsertionInfo();
		info.setCaretRange(0, fixedPosition(12));
		info.setDefaultEndOffs(20);
		Assertions.assertEquals(20, info.getDefaultEndOffs());
	}


	@Test
	void addReplacementLocation_addsRetrievableRange() {
		ParameterizedCompletionInsertionInfo info = new ParameterizedCompletionInsertionInfo();
		info.addReplacementLocation(1, 4);
		info.addReplacementLocation(6, 9);

		Assertions.assertEquals(2, info.getReplacementCount());

		DocumentRange first = info.getReplacementLocation(0);
		Assertions.assertEquals(1, first.getStartOffset());
		Assertions.assertEquals(4, first.getEndOffset());

		DocumentRange second = info.getReplacementLocation(1);
		Assertions.assertEquals(6, second.getStartOffset());
		Assertions.assertEquals(9, second.getEndOffset());
	}


	@Test
	void addReplacementCopy_addsRetrievableCopy() {
		ParameterizedCompletionInsertionInfo info = new ParameterizedCompletionInsertionInfo();
		info.addReplacementCopy("a", 1, 2);
		info.addReplacementCopy("b", 3, 4);

		Assertions.assertEquals(2, info.getReplacementCopyCount());

		ReplacementCopy first = info.getReplacementCopy(0);
		Assertions.assertEquals("a", first.getId());
		Assertions.assertEquals(1, first.getStart());
		Assertions.assertEquals(2, first.getEnd());

		ReplacementCopy second = info.getReplacementCopy(1);
		Assertions.assertEquals("b", second.getId());
		Assertions.assertEquals(3, second.getStart());
		Assertions.assertEquals(4, second.getEnd());
	}


}

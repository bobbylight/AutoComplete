/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 * Unit tests for {@link ParameterizedCompletion}.  The interface itself has
 * no behavior of its own (its methods are exercised via implementations such
 * as {@code FunctionCompletion} and {@code TemplateCompletion}, which have
 * their own test classes), so these tests focus on the nested
 * {@link Parameter} class.
 */
@ExtendWith(SwingRunnerExtension.class)
class ParameterizedCompletionTest {


	@Test
	void constructor_twoArg_defaultsIsEndParamToFalse() {
		Parameter param = new Parameter("int", "a");
		Assertions.assertEquals("int", param.getType());
		Assertions.assertEquals("a", param.getName());
		Assertions.assertFalse(param.isEndParam());
	}


	@Test
	void constructor_threeArg_setsIsEndParam() {
		Parameter param = new Parameter("int", "a", true);
		Assertions.assertTrue(param.isEndParam());
	}


	@Test
	void constructor_nullType_getTypeReturnsNull() {
		Parameter param = new Parameter(null, "a");
		Assertions.assertNull(param.getType());
		Assertions.assertNull(param.getTypeObject());
	}


	@Test
	void getType_nonStringTypeObject_usesToString() {
		Object type = new Object() {
			@Override
			public String toString() {
				return "CustomType";
			}
		};
		Parameter param = new Parameter(type, "a");
		Assertions.assertEquals("CustomType", param.getType());
		Assertions.assertSame(type, param.getTypeObject());
	}


	@Test
	void getSetDescription_roundTrips() {
		Parameter param = new Parameter("int", "a");
		Assertions.assertNull(param.getDescription());
		param.setDescription("the a param");
		Assertions.assertEquals("the a param", param.getDescription());
	}


	@Test
	void toString_typeAndName_separatedBySpace() {
		Parameter param = new Parameter("int", "a");
		Assertions.assertEquals("int a", param.toString());
	}


	@Test
	void toString_typeOnly_noTrailingSpace() {
		Parameter param = new Parameter("int", null);
		Assertions.assertEquals("int", param.toString());
	}


	@Test
	void toString_nameOnly_noLeadingSpace() {
		Parameter param = new Parameter(null, "a");
		Assertions.assertEquals("a", param.toString());
	}


	@Test
	void toString_neitherTypeNorName_empty() {
		Parameter param = new Parameter(null, null);
		Assertions.assertEquals("", param.toString());
	}


}

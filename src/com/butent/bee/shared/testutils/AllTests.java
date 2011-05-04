package com.butent.bee.shared.testutils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
/**
 * Contains the Test Suite
 */
@RunWith(value = org.junit.runners.Suite.class)
@SuiteClasses(value = { TestBeeUtilsisEmpty.class,
		TestBeeUtilscontaintAny.class, TestBeeUtilsFilterType.class,
		TestBeeUtilsTransform.class, TestBeeUtilsIsEmpty2Par.class,
		TestArrayUtils.class, TestLogUtils.class, TestPropertyUtils.class,
		TestValueUtils.class, TestTimeUtils.class, TestRowComparator.class,
		TestCodec.class, TestIsExpression.class, TestIsCondition.class,
		TestSqlCreate.class, TestSqlUtilsIsQuery.class, TestSqlInsert.class,
		TestSqlUpdate.class, TestSqlDelete.class, TestSqlSelect.class,
		TestHasFrom.class, TestWildcardsPattern.class, TestWildcards.class,
		TestDateTime.class, TestJustDate.class, TestPair.class, TestBeeConst.class,
		TestArraySequence.class, TestBeeResource.class,TestIntValue.class,
		TestLongValue.class, TestListSequence.class, TestValueType.class,
		TestService.class, TestStage.class, TestStringArray.class,
		TestVariable.class, TestBooleanValue.class, TestDateTimeValue.class,
		TestDateValue.class, TestNumberValue.class, TestTextValue.class,
		TestTimeOfDayValue.class})

public class AllTests {
}

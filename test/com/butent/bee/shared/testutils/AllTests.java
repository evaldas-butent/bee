package com.butent.bee.shared.testutils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Contains the Test Suite.
 */
@RunWith(value = org.junit.runners.Suite.class)
@SuiteClasses(value = {TestBeeUtilscontaintAny.class,
    TestBeeUtilsTransform.class, TestArrayUtils.class, TestPropertyUtils.class,
    TestTimeUtils.class,
    TestCodec.class, TestIsExpression.class, TestIsCondition.class,
    TestSqlCreate.class, TestSqlUtilsIsQuery.class, TestSqlInsert.class,
    TestSqlUpdate.class, TestSqlDelete.class, TestSqlSelect.class,
    TestHasFrom.class, TestWildcardsPattern.class, TestWildcards.class,
    TestDateTime.class, TestJustDate.class, TestPair.class, TestBeeConst.class,
    TestResource.class, TestIntValue.class,
    TestLongValue.class, TestListSequence.class, TestValueType.class,
    TestService.class, TestStringArray.class,
    TestBooleanValue.class, TestDateTimeValue.class,
    TestDateValue.class, TestNumberValue.class, TestTextValue.class,
    TestTimeOfDayValue.class})
public class AllTests {
}

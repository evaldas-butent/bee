package com.butent.bee.shared.testutils;

import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.Grego;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.LogUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * Tests {@link com.butent.bee.shared.time.TimeUtils}.
 */
public class TestTimeUtils {

  public static final long MAX_RANDOM_REQUEST = 1000L;

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testAdd() {

    DateTime date = new DateTime();
    date.setTime(1298362388227L);

    TimeUtils.add(date, 10, 1);

    assertEquals("11:13:08.227", LogUtils.dateToLog(date.getTime()));
    TimeUtils.add(date, 10, 0);
    assertEquals("11:13:08.227", LogUtils.dateToLog(date.getTime()));
    TimeUtils.add(date, 10, 24);
    assertEquals("11:13:08.227", LogUtils.dateToLog(date.getTime()));
  }

  @Test
  public final void testDateDiff() {
    DateTime start = new DateTime(2011, 03, 07, 11, 50, 10, 00);
    DateTime end = new DateTime(2012, 03, 8, 12, 40, 20, 00);

    assertEquals(367, TimeUtils.dateDiff(start, end));
  }

  @Test
  public final void testDayOfWeek() {
    assertEquals(2, Grego.dayOfWeek(11));
    assertEquals(1, Grego.dayOfWeek(17));
    assertEquals(2, Grego.dayOfWeek(18));
    assertEquals(3, Grego.dayOfWeek(19));
    assertEquals(4, Grego.dayOfWeek(20));
  }

  @Test
  public final void testFieldDifference() {

    DateTime start = new DateTime();
    start.setTime(1298362388227L);
    DateTime end = new DateTime();
    end.setTime(1298362388327L);

    assertEquals(100,
        TimeUtils.fieldDifference(start, end, TimeUtils.MILLISECOND));
    assertEquals(-100,
        TimeUtils.fieldDifference(end, start, TimeUtils.MILLISECOND));
    assertEquals(0,
        TimeUtils.fieldDifference(start, start, TimeUtils.MILLISECOND));

    DateTime start1 = new DateTime(2010, 03, 7, 7, 10, 10);

    DateTime start2 = new DateTime(2011, 3, 7, 17, 30, 20);
    DateTime start3 = new DateTime(2011, 2, 7, 10, 30, 10);

    assertEquals(365, TimeUtils.fieldDifference(start1, start2,
        TimeUtils.DAY_OF_YEAR));
    assertEquals(12,
        TimeUtils.fieldDifference(start1, start2, TimeUtils.MONTH));
    assertEquals(1,
        TimeUtils.fieldDifference(start1, start2, TimeUtils.YEAR));
    assertEquals(52, TimeUtils.fieldDifference(start1, start2,
        TimeUtils.DAY_OF_WEEK_IN_MONTH));

    assertEquals(352,
        TimeUtils.fieldDifference(start1, start, TimeUtils.DAY_OF_YEAR));
    assertEquals(11,
        TimeUtils.fieldDifference(start1, start, TimeUtils.MONTH));
    assertEquals(0,
        TimeUtils.fieldDifference(start1, start, TimeUtils.YEAR));
    assertEquals(50, TimeUtils.fieldDifference(start1, start,
        TimeUtils.DAY_OF_WEEK_IN_MONTH));

    assertEquals(14,
        TimeUtils.fieldDifference(start3, start, TimeUtils.DAY_OF_YEAR));
    assertEquals(0,
        TimeUtils.fieldDifference(start3, start, TimeUtils.MONTH));
    assertEquals(0,
        TimeUtils.fieldDifference(start3, start, TimeUtils.YEAR));
    assertEquals(2, TimeUtils.fieldDifference(start3, start,
        TimeUtils.DAY_OF_WEEK_IN_MONTH));

    DateTime start4 = new DateTime(2011, 03, 01);
    DateTime end1 = new DateTime(2012, 03, 01);

    assertEquals(366,
        TimeUtils.fieldDifference(start4, end1, TimeUtils.DAY_OF_YEAR));
    assertEquals(12,
        TimeUtils.fieldDifference(start4, end1, TimeUtils.MONTH));
    assertEquals(1, TimeUtils.fieldDifference(start4, end1, TimeUtils.YEAR));
    assertEquals(2, TimeUtils.fieldDifference(start3, start,
        TimeUtils.DAY_OF_WEEK_IN_MONTH));

    DateTime start5 = new DateTime(2011, 2, 7, 11, 30, 20);
    DateTime start6 = new DateTime(2011, 2, 8, 12, 30, 20);

    assertEquals(0,
        TimeUtils.fieldDifference(start3, start5, TimeUtils.AM_PM));
    assertEquals(2,
        TimeUtils.fieldDifference(start3, start6, TimeUtils.AM_PM));
    assertEquals(60,
        TimeUtils.fieldDifference(start3, start5, TimeUtils.MINUTE));
    assertEquals(3610,
        TimeUtils.fieldDifference(start3, start5, TimeUtils.SECOND));
    assertEquals(1, TimeUtils.fieldDifference(start5, start6,
        TimeUtils.DAY_OF_WEEK));
  }

  @SuppressWarnings({"rawtypes", "unused"})
  @Test
  public final void testFieldName() {

    int index = -1;
    Method m[] = null;
    try {
      Class c = TimeUtils.class;
      m = c.getDeclaredMethods();
      for (int i = 0; i < m.length; i++) {
        System.out.println(m[i].toString());
        if (m[i]
            .toString()
            .equals(
                "private static java.lang.String com.butent.bee.shared.utils.TimeUtils.fieldName(int)")) {
          index = i;
        }
      }
    } catch (Throwable e) {
      fail("Method not found");
    }

    try {
      Class cls = Class.forName("com.butent.bee.shared.utils.TimeUtils");
      Class partypes[] = new Class[1];
      partypes[0] = String.class;

      Method meth = m[index];// cls.getMethod("private static java.lang.String transformString",
      // partypes);
      System.out.println(meth.toString());
      meth.setAccessible(true);

      Integer arglist[] = new Integer[1];
      arglist[0] = -5;

      assertEquals("Field -5", meth.invoke(null, (Object[]) arglist));

      arglist[0] = 0;
      assertEquals("ERA", meth.invoke(null, (Object[]) arglist));

      arglist[0] = 100;
      assertEquals("Field 100", meth.invoke(null, (Object[]) arglist));

      arglist[0] = 5;
      assertEquals("DAY_OF_MONTH", meth.invoke(null, (Object[]) arglist));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public final void testFieldsToDay() {
    assertEquals(15075, Grego.fieldsToDay(2011, 04, 11));
  }

  @Test
  public final void testIsDateOrDateTime() {
    assertTrue(TimeUtils.isDateOrDateTime(new DateTime(2011)));
    assertTrue(TimeUtils.isDateOrDateTime(new JustDate()));
    assertTrue(TimeUtils.isDateOrDateTime(new Date()));
    assertFalse(TimeUtils.isDateOrDateTime(null));
    assertFalse(TimeUtils.isDateOrDateTime("2011-12-20"));
    assertTrue(TimeUtils
        .isDateOrDateTime(new java.sql.Date(1298362388227L)));
    assertFalse(TimeUtils.isDateOrDateTime(1298362388227L));
  }

  @Test
  public final void testMillisToString() {
    assertEquals("500", TimeUtils.millisToString(500));
    assertEquals("010", TimeUtils.millisToString(10));
    assertEquals("5000", TimeUtils.millisToString(5000));
  }

  @Test
  public final void testParseFields() {
    assertArrayEquals(null, TimeUtils.parseFields(""));
    assertArrayEquals(null, TimeUtils.parseFields(null));

    int[] mas = {1, 2, 3, 4, 5, 0, 0};
    assertArrayEquals(mas, TimeUtils.parseFields("ab1cd2g3fd4ggh5"));

    int[] mas2 = {1, 2, 3, 4, 5, 8, 9};
    assertArrayEquals(mas2, TimeUtils.parseFields("a1a2a3a4a5a8a9a5a6a4a8"));
  }

  @Test
  public final void testRandomDate() {
    JustDate min = new JustDate(2011, 03, 07);
    JustDate max = new JustDate(2012, 03, 07);

    for (int i = 0; i < MAX_RANDOM_REQUEST; i++) {

      JustDate dt = TimeUtils.randomDate(min, max);

      assertEquals(true, (dt.compareTo(min) >= 0)
          && (dt.compareTo(max) <= 0));
    }
  }

  @Test
  public final void testRandomDateTime() {
    DateTime min = new DateTime(2011, 03, 07, 12, 25, 10);
    DateTime max = new DateTime(2012, 03, 07, 14, 15, 17);

    for (int i = 0; i < MAX_RANDOM_REQUEST; i++) {

      DateTime dt = TimeUtils.randomDateTime(min, max);

      assertEquals(true, (dt.compareTo(min) >= 0)
          && (dt.compareTo(max) <= 0));
    }
  }

  @Test
  public final void testToDate() {
    JustDate first = new JustDate(2011, 02, 22);
    DateTime dt = new DateTime(2011, 02, 22);
    Date d = new Date(1298362388227L);

    assertEquals(first, TimeUtils.toDate(first));
    assertEquals(true, TimeUtils.toDate(dt).compareTo(first) == 0);
    assertEquals(true, TimeUtils.toDate(d).compareTo(first) == 0);

    try {
      assertEquals(null, TimeUtils.toDate(null));
      fail("BeeRuntimeException not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
      System.out.println("toDate" + e.getMessage());
    } catch (Exception e) {
      fail("Java runtime error. Need BeeRuntimeException !!!");
    }
  }

  @Test
  public final void testToDateTime() {
    DateTime first = new DateTime(2011, 03, 29, 8, 54, 6, 875);
    DateTime first2 = new DateTime(2011, 03, 29);
    JustDate second = new JustDate(2011, 03, 29);

    DateTime dt = new DateTime(2011, 03, 29, 8, 54, 6, 875);
    Date d = new Date(1301378046875L);

    assertEquals(first, TimeUtils.toDateTime(first));
    assertEquals(true, TimeUtils.toDateTime(dt).compareTo(first) == 0);
    assertEquals(true, TimeUtils.toDateTime(d).compareTo(first) == 0);
    assertEquals(true, TimeUtils.toDateTime(second).compareTo(first2) == 0);

    try {
      assertEquals(null, TimeUtils.toDateTime(null));
      fail("BeeRuntimeException not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
      System.out.println("toDate" + e.getMessage());
    } catch (Exception e) {
      fail("Java runtime error. Need BeeRuntimeException !!!");
    }
  }

  @Test
  public final void testToJava() {
    Date first = new Date(1298362388227L);
    Date first2 = new Date(1301346000000L);

    DateTime dt = new DateTime(2011, 02, 22, 10, 13, 8, 227);
    JustDate d = new JustDate(2011, 03, 29);

    assertEquals(first, TimeUtils.toJava(first));
    assertEquals(true, TimeUtils.toJava(dt).compareTo(first) == 0);
    assertEquals(true, TimeUtils.toJava(d).compareTo(first2) == 0);

    try {
      assertEquals(null, TimeUtils.toJava(null));
      fail("BeeRuntimeException not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
      System.out.println("toDate" + e.getMessage());
    } catch (Exception e) {
      fail("Java runtime error. Need BeeRuntimeException !!!");
    }
  }

  @Test
  public final void testYearToString() {
    assertEquals("1995", TimeUtils.yearToString(1995));
    assertEquals("0", TimeUtils.yearToString(0));

    try {
      assertEquals("-1995", TimeUtils.yearToString(-1995));

    } catch (BeeRuntimeException e) {
      assertTrue(true);
      System.out.println("yearstostring" + e.getMessage());
    } catch (Exception e) {
      fail("Java runtime error. Need BeeRuntimeException !!!"
          + e.getMessage());
    }
  }
}

package com.butent.bee.shared.testutils;

import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link com.butent.bee.shared.time.DateTime}.
 */
public class TestDateTime {

  public DateTime varDate;
  public DateTime varDate1;

  @Before
  public void setUp() throws Exception {
    varDate = new DateTime(1298362388227L);
    varDate1 = new DateTime(2011, 2, 22, 8, 13, 8, 227);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testCompareTo() {
    DateTime dt = new DateTime(1298362388227L);

    assertEquals(0, varDate.compareTo(dt));
    dt = new DateTime(2011, 2, 22, 10, 13, 8, 446);
    assertEquals(-1, varDate.compareTo(dt));

    dt = new DateTime(2011, 2, 22);
    assertEquals(1, varDate.compareTo(dt));
  }

  @Test
  public final void testDateTime() {
    varDate = new DateTime();
    assertEquals(System.currentTimeMillis(), varDate.getTime(), 1000);
  }

  @Test
  public final void testDateTimeDate() {
    DateTime dt = new DateTime(new java.util.Date());
    assertEquals(System.currentTimeMillis(), dt.getTime(), 1000);

    try {
      dt = new DateTime((java.util.Date) null);
      assertEquals(0, dt.getTime());
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Java lang exception need BeeRuntimeException "
          + e.getMessage());
    }
  }

  @Test
  public final void testDateTimeIntIntIntIntIntInt() {
    varDate1 = new DateTime(2011, 2, 22, 10, 13, 8);
    assertEquals(1298362388000L, varDate1.getTime());
  }

  @Test
  public final void testDateTimeJustDate() {
    JustDate jd = new JustDate(2011, 02, 22);
    varDate = new DateTime(jd);

    assertEquals(2011, varDate.getYear());
    assertEquals(2, varDate.getMonth());
    assertEquals(22, varDate.getDom());

    try {
      varDate = new DateTime((JustDate) null);
      assertEquals(0, varDate.getTime());
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Java lang error. Need BeeRuntimeException " + e.getMessage());
    }
  }

  @Test
  public final void testDeserialize() {
    String st = "1298362388227";
    varDate.deserialize(st);

    assertEquals(1298362388227L, varDate.getTime());

    st = "1298362388446";
    varDate.deserialize(st);

    assertEquals(1298362388446L, varDate.getTime());

    st = "1298332800000";
    varDate.deserialize(st);

    assertEquals(1298332800000L, varDate.getTime());
  }

  @Test
  public final void testEqualsObject() {

    assertEquals(false, varDate1.equals("2011-02-22 15:13:08,277"));

    DateTime dt = new DateTime(1298362388227L);
    varDate1 = new DateTime(1298362388227L);
    assertEquals(true, varDate1.equals(dt));

    dt = new DateTime(2011, 2, 22, 8, 13, 8);
    assertEquals(false, varDate1.equals(dt));

    dt = null;
    assertEquals(false, varDate1.equals(dt));

    assertEquals(false, varDate1.equals(null));
  }

  @Test
  public final void testGetDom() {
    assertEquals(22, varDate.getDom());

    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(22, varDate.getDom());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(22, varDate.getDom());

    varDate = new DateTime(2011, 2, 29, 0, 0, 0, 0);
    assertEquals(1, varDate.getDom());
    assertEquals(3, varDate.getMonth());
  }

  @Test
  public final void testGetDow() {
    assertEquals(3, varDate.getDow());

    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(3, varDate.getDow());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(3, varDate.getDow());

    varDate = new DateTime(2011, 03, 30, 2, 1, 2);
    assertEquals(4, varDate.getDow());

    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals(1, varDate.getDow());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45);
    assertEquals(7, varDate.getDow());
  }

  @Test
  public final void testGetDoy() {
    assertEquals(3, varDate.getDow());

    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(53, varDate.getDoy());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(53, varDate.getDoy());

    varDate = new DateTime(2011, 03, 30, 2, 1, 2);
    assertEquals(89, varDate.getDoy());

    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals(65, varDate.getDoy());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45);
    assertEquals(78, varDate.getDoy());

    varDate = new DateTime(2011, 01, 01, 5, 1, 45);
    assertEquals(1, varDate.getDoy());

    varDate = new DateTime(2011, 12, 31, 5, 1, 45);
    assertEquals(365, varDate.getDoy());

    varDate = new DateTime(2012, 12, 31, 5, 1, 45);
    assertEquals(366, varDate.getDoy());
  }

  @Test
  public final void testGetHour() {
    assertEquals(10, varDate.getHour());

    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(8, varDate.getHour());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(0, varDate.getHour());

    varDate = new DateTime(2011, 2, 22, 24, 0, 0, 0);
    assertEquals(0, varDate.getHour());
    assertEquals(23, varDate.getDom());

    varDate = new DateTime(2011, 03, 27, 4, 1, 2);
    assertEquals(4, varDate.getHour());
  }

  @Test
  public final void testGetMillis() {
    assertEquals(227, varDate.getMillis());

    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(446, varDate.getMillis());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(0, varDate.getMillis());
  }

  @Test
  public final void testGetMinute() {
    assertEquals(13, varDate.getMinute());

    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(13, varDate.getMinute());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(0, varDate.getMinute());
  }

  @SuppressWarnings("unused")
  @Test
  public final void testGetMonth() {
    java.util.Date dt = new java.util.Date(1298362388227L);
    assertEquals(2, varDate.getMonth());

    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    dt = new java.util.Date(1298362388446L);
    assertEquals(2, varDate.getMonth());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    dt = new java.util.Date(1298332800000L);
    assertEquals(2, varDate.getMonth());
  }

  @Test
  public final void testGetSecond() {
    assertEquals(8, varDate.getSecond());

    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(8, varDate.getSecond());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(0, varDate.getSecond());
  }

  @Test
  public final void testGetTime() {
    java.util.Date dt = new java.util.Date(1298362388227L);
    assertEquals(dt.getTime(), varDate.getTime());

    varDate = new DateTime(2011, 2, 22, 10, 13, 8, 446);
    dt = new java.util.Date(1298362388446L);
    assertEquals(dt.getTime(), varDate.getTime());

    varDate = new DateTime(2011, 2, 22, 2, 0, 0, 0);
    dt = new java.util.Date(1298332800000L);
    assertEquals(dt.getTime(), varDate.getTime());
  }

  @Test
  public final void testGetTimezoneOffset() {
    assertEquals(-120, varDate.getTimezoneOffset());
    assertEquals(-120, varDate1.getTimezoneOffset());
    varDate1 = new DateTime(2011, 3, 26);
    assertEquals(-120, varDate1.getTimezoneOffset());
    varDate1 = new DateTime(2011, 3, 27);
    assertEquals(-120, varDate1.getTimezoneOffset());
  }

  @Test
  public final void testGetUtcDom() {

    assertEquals(22, varDate.getUtcDom());
    varDate = new DateTime(2011, 2, 22, 10, 13, 8, 446);

    assertEquals(22, varDate.getUtcDom());

    varDate = new DateTime(2011, 2, 22, 2, 0, 0, 0);
    assertEquals(22, varDate.getUtcDom());
    varDate1 = new DateTime(2011, 3, 26);
    assertEquals(25, varDate1.getUtcDom());
    varDate1 = new DateTime(2011, 3, 27);
    assertEquals(26, varDate1.getUtcDom());
  }

  @Test
  public final void testGetUtcDow() {
    assertEquals(3, varDate.getUtcDow());
    varDate = new DateTime(2011, 2, 22, 10, 13, 8, 446);
    assertEquals(3, varDate.getUtcDow());

    varDate = new DateTime(2011, 2, 22, 2, 0, 0, 0);
    assertEquals(3, varDate.getUtcDow());
    varDate1 = new DateTime(2011, 3, 26);
    assertEquals(6, varDate1.getUtcDow());
    varDate1 = new DateTime(2011, 3, 27);
    assertEquals(7, varDate1.getUtcDow());

    varDate1 = new DateTime(2011, 3, 27, 5, 12, 15);
    assertEquals(1, varDate1.getUtcDow());
  }

  @Test
  public final void testGetUtcDoy() {
    assertEquals(53, varDate.getUtcDoy());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(53, varDate.getUtcDoy());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(52, varDate.getUtcDoy());

    varDate = new DateTime(2011, 03, 30, 2, 1, 2);
    assertEquals(23, varDate.getUtcHour());
    assertEquals(88, varDate.getUtcDoy());

    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals(65, varDate.getUtcDoy());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45);
    assertEquals(78, varDate.getUtcDoy());
  }

  @Test
  public final void testGetUtcHour() {
    assertEquals(8, varDate.getUtcHour());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(6, varDate.getUtcHour());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals(3, varDate.getUtcHour());

    varDate = new DateTime(2011, 3, 19, 5, 1, 45);
    assertEquals(3, varDate.getUtcHour());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(22, varDate.getUtcHour());

    varDate = new DateTime(2011, 3, 27, 4, 1, 45);
    assertEquals(1, varDate.getUtcHour());

    varDate = new DateTime(2011, 3, 30, 2, 1, 2);
    assertEquals(23, varDate.getUtcHour());
  }

  @Test
  public final void testGetUtcMillis() {
    assertEquals(227, varDate.getUtcMillis());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(446, varDate.getUtcMillis());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals(0, varDate.getUtcMillis());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45);
    assertEquals(0, varDate.getUtcMillis());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(0, varDate.getUtcMillis());
    varDate = new DateTime(2011, 03, 27, 4, 1, 45);
    assertEquals(0, varDate.getUtcMillis());
    assertEquals(0, varDate.getUtcMillis());

    varDate = new DateTime(2011, 03, 30, 2, 1, 2);
    assertEquals(0, varDate.getUtcMillis());
    assertEquals(0, varDate.getUtcMillis());
  }

  @Test
  public final void testGetUtcMinute() {
    assertEquals(13, varDate.getUtcMinute());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(13, varDate.getUtcMinute());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals(1, varDate.getUtcMinute());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45);
    assertEquals(1, varDate.getUtcMinute());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(0, varDate.getUtcMinute());
    varDate = new DateTime(2011, 03, 27, 4, 1, 45);
    assertEquals(1, varDate.getUtcMinute());
    assertEquals(1, varDate.getUtcMinute());

    varDate = new DateTime(2011, 03, 30, 2, 1, 2);
    assertEquals(1, varDate.getUtcMinute());
    assertEquals(1, varDate.getUtcMinute());
  }

  @Test
  public final void testGetUtcMonth() {
    assertEquals(2, varDate.getUtcMonth());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(2, varDate.getUtcMonth());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals(3, varDate.getUtcMonth());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45);
    assertEquals(3, varDate.getUtcMonth());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(2, varDate.getUtcMonth());
    varDate = new DateTime(2011, 03, 27, 4, 1, 45);
    assertEquals(3, varDate.getUtcMonth());
    assertEquals(3, varDate.getUtcMonth());

    varDate = new DateTime(2011, 03, 30, 2, 1, 2);
    assertEquals(3, varDate.getUtcMonth());
    assertEquals(3, varDate.getUtcMonth());

    varDate = new DateTime(2011, 01, 30, 2, 1, 2);
    assertEquals(1, varDate.getUtcMonth());
    assertEquals(1, varDate.getUtcMonth());

    varDate = new DateTime(2011, 12, 30, 2, 1, 2);
    assertEquals(12, varDate.getUtcMonth());
    assertEquals(12, varDate.getUtcMonth());
  }

  @Test
  public final void testGetUtcSecond() {
    assertEquals(8, varDate.getUtcSecond());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(8, varDate.getUtcSecond());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals(45, varDate.getUtcSecond());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45);
    assertEquals(45, varDate.getUtcSecond());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    assertEquals(0, varDate.getUtcSecond());
    varDate = new DateTime(2011, 03, 27, 4, 1, 45);
    assertEquals(45, varDate.getUtcSecond());

    varDate = new DateTime(2011, 03, 30, 2, 1, 2);
    assertEquals(2, varDate.getUtcSecond());

    varDate = new DateTime(2011, 01, 30, 2, 1, 2);
    assertEquals(2, varDate.getUtcSecond());

    varDate = new DateTime(2011, 12, 30, 2, 1, 2);
    assertEquals(2, varDate.getUtcSecond());
  }

  @Test
  public final void testGetUtcYear() {
    assertEquals(2011, varDate.getUtcYear());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals(2011, varDate.getUtcYear());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals(2011, varDate.getUtcYear());
    varDate = new DateTime(2011, 1, 1, 1, 1, 45);
    assertEquals(2010, varDate.getUtcYear());
  }

  @SuppressWarnings("unused")
  @Test
  public final void testGetYear() {
    java.util.Date dt = new java.util.Date(1298362388227L);
    assertEquals(2011, varDate.getYear());

    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    dt = new java.util.Date(1298362388446L);
    assertEquals(2011, varDate.getYear());

    varDate = new DateTime(2011, 2, 22, 0, 0, 0, 0);
    dt = new java.util.Date(1298332800000L);
    assertEquals(2011, varDate.getYear());
  }

  @Test
  public final void testHashCode() {
    java.util.Date dt = new java.util.Date(1298362388227L);
    assertEquals(dt.hashCode(), varDate.hashCode());

    varDate = new DateTime(2011, 2, 22, 10, 13, 8, 446);
    dt = new java.util.Date(1298362388446L);
    assertEquals(dt.hashCode(), varDate.hashCode());

    varDate = new DateTime(2011, 2, 22, 2, 0, 0, 0);
    dt = new java.util.Date(1298332800000L);
    assertEquals(dt.hashCode(), varDate.hashCode());
  }

  @Test
  public final void testParse() {
    String s1 = "2011-02-22";
    DateTime d1 = DateTime.parse(s1);
    varDate1 = new DateTime(2011, 02, 22);

    assertEquals(d1.getTime(), varDate1.getTime());

    s1 = "21:02";
    d1 = DateTime.parse(s1);
    varDate1 = new DateTime(21, 2, 0, 0, 0, 0, 0);
    assertEquals(d1.getHour(), varDate1.getHour());
    assertEquals(d1.getMinute(), varDate1.getMinute());
    assertEquals(d1.getYear(), varDate1.getYear());
    assertEquals(d1.getTime(), varDate1.getTime());

    s1 = "21:02:52";
    d1 = DateTime.parse(s1);
    varDate1 = new DateTime(21, 2, 52, 0, 0, 0, 0);
    assertEquals(d1.getTime(), varDate1.getTime());

    s1 = "2011-02-22 10:15:10,5";
    d1 = DateTime.parse(s1);
    varDate1 = new DateTime(2011, 2, 22, 10, 15, 10, 5);
    assertEquals(d1.getTime(), varDate1.getTime());

    s1 = "11/02/22 10:15:10,5";
    d1 = DateTime.parse(s1);
    varDate1 = new DateTime(11, 2, 22, 10, 15, 10, 5);
    assertEquals(d1.getTime(), varDate1.getTime());

    s1 = "11-2";
    d1 = DateTime.parse(s1);
    varDate1 = new DateTime(11, 2, 0);
    assertEquals(varDate1.toString(), d1.toString());

    DateTime dn = DateTime.parse("2011-");
    assertEquals("2011.01.01 00:00:00", dn.toString());
  }

  @Test
  public final void testSerialize() {
    varDate = new DateTime(2011, 2, 22, 2, 0, 0, 0);
    String a = varDate.serialize();
    assertEquals("1298332800000", a);

    varDate = new DateTime(2011, 2, 22, 10, 13, 8, 227);
    a = varDate.serialize();
    assertEquals("1298362388227", a);
  }

  @Test
  public final void testSetTime() {
    varDate = new DateTime();
    varDate.setTime(1298362388227L);
    assertEquals(1298362388227L, varDate.getTime());
    assertEquals(2011, varDate.getYear());
    assertEquals(2, varDate.getMonth());
    assertEquals(22, varDate.getDom());
    assertEquals(8, varDate.getUtcHour());
    assertEquals(10, varDate.getHour());
    assertEquals(13, varDate.getMinute());
    assertEquals(8, varDate.getSecond());
    assertEquals(227, varDate.getMillis());
  }

  @Test
  public final void testToDateString() {
    assertEquals("2011.02.22", varDate.toDateString());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals("2011.02.22", varDate.toDateString());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals("2011.03.06", varDate.toDateString());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45);
    assertEquals("2011.03.19", varDate.toDateString());

    varDate = new DateTime(2011, 03, 27, 4, 1, 45);
    assertEquals("2011.03.27", varDate.toDateString());

    varDate = new DateTime(2011, 01, 01, 0, 1, 2);
    assertEquals("2011.01.01", varDate.toDateString());
  }

  @Test
  public final void testToString() {
    assertEquals("2011.02.22 10:13:08.227", varDate.toString());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals("2011.02.22 08:13:08.446", varDate.toString());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45, 1);
    assertEquals("2011.03.06 05:01:45.001", varDate.toString());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45, 10);
    assertEquals("2011.03.19 05:01:45.010", varDate.toString());

    varDate = new DateTime(2011, 03, 27, 4, 1, 45);
    assertEquals("2011.03.27 04:01:45", varDate.toString());

    varDate = new DateTime(2011, 01, 01, 0, 1, 2);
    assertEquals("2011.01.01 00:01:02", varDate.toString());
  }

  @Test
  public final void testToTimeString() {
    assertEquals("10:13:08.227", varDate.toTimeString());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals("08:13:08.446", varDate.toTimeString());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45, 1);
    assertEquals("05:01:45.001", varDate.toTimeString());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45, 10);
    assertEquals("05:01:45.010", varDate.toTimeString());

    varDate = new DateTime(2011, 03, 27, 4, 1, 45);
    assertEquals("04:01:45", varDate.toTimeString());

    varDate = new DateTime(2011, 01, 01, 0, 1, 2);
    assertEquals("00:01:02", varDate.toTimeString());
  }

  @Test
  public final void testToUtcDateString() {
    assertEquals("2011.02.22", varDate.toUtcDateString());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals("2011.02.22", varDate.toUtcDateString());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45);
    assertEquals("2011.03.06", varDate.toUtcDateString());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45);
    assertEquals("2011.03.19", varDate.toUtcDateString());

    varDate = new DateTime(2011, 03, 27, 4, 1, 45);
    assertEquals("2011.03.27", varDate.toUtcDateString());

    varDate = new DateTime(2011, 01, 01, 0, 1, 2);
    assertEquals("2010.12.31", varDate.toUtcDateString());
  }

  @Test
  public final void testToUtcString() {
    assertEquals("2011.02.22 08:13:08.227", varDate.toUtcString());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals("2011.02.22 06:13:08.446", varDate.toUtcString());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45, 1);
    assertEquals("2011.03.06 03:01:45.001", varDate.toUtcString());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45, 10);
    assertEquals("2011.03.19 03:01:45.010", varDate.toUtcString());

    varDate = new DateTime(2011, 01, 01, 0, 1, 2);
    assertEquals("2010.12.31 22:01:02", varDate.toUtcString());

    varDate = new DateTime(2011, 03, 27, 4, 1, 45);
    assertEquals("2011.03.27 01:01:45", varDate.toUtcString());
  }

  @Test
  public final void testToUtcTimeString() {
    assertEquals("08:13:08.227", varDate.toUtcTimeString());
    varDate = new DateTime(2011, 2, 22, 8, 13, 8, 446);
    assertEquals("06:13:08.446", varDate.toUtcTimeString());
    varDate = new DateTime(2011, 03, 6, 5, 1, 45, 1);
    assertEquals("03:01:45.001", varDate.toUtcTimeString());

    varDate = new DateTime(2011, 03, 19, 5, 1, 45, 10);
    assertEquals("03:01:45.010", varDate.toUtcTimeString());

    varDate = new DateTime(2011, 01, 01, 0, 1, 2);
    assertEquals("22:01:02", varDate.toUtcTimeString());

    varDate = new DateTime(2011, 03, 27, 4, 1, 45);
    assertEquals("01:01:45", varDate.toUtcTimeString());
  }
}

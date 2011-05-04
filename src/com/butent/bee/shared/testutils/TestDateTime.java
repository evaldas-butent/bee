package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.exceptions.BeeRuntimeException;

/**
 * Tests {@link com.butent.bee.shared.DateTime}.
 */
public class TestDateTime {

	public DateTime Date;
	public DateTime Date1;

	@Before
	public void setUp() throws Exception {
		Date = new DateTime(1298362388227L);
		Date1 = new DateTime(2011, 2, 22, 8, 13, 8, 227);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testHashCode() {
		java.util.Date dt = new java.util.Date(1298362388227L);
		assertEquals(dt.hashCode(), Date.hashCode());

		Date = new DateTime(2011, 2, 22, 10, 13, 8, 446);
		dt = new java.util.Date(1298362388446L);
		assertEquals(dt.hashCode(), Date.hashCode());

		Date = new DateTime(2011, 2, 22, 2, 0, 0, 0);
		dt = new java.util.Date(1298332800000L);
		assertEquals(dt.hashCode(), Date.hashCode());
	}

	@SuppressWarnings("unused")
	@Test
	public final void testParse() {
		try {
			DateTime d1 = DateTime.parse("");
			fail("Exceptions not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Java lang exception, need BeeRuntime exception: "
					+ e.getMessage());
		}

		try {
			DateTime d1 = DateTime.parse(null);
			fail("Exceptions not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Java lang exception, need BeeRuntime exception: "
					+ e.getMessage());
		}

		try {
			DateTime d1 = DateTime.parse("0:0:0 0:0:0:,0");
			fail("Exceptions not works" + d1.toDateString() + d1.toTimeString());
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Java lang exception, need BeeRuntime exception: "
					+ e.getMessage());
		}

		String s1 = "1298362388227";
		DateTime d1 = DateTime.parse(s1);
		Date1 = new DateTime(2011, 2, 22, 10, 13, 8, 227);
		assertEquals(d1.getTime(), Date1.getTime());

		s1 = "1298362388446";
		d1 = DateTime.parse(s1);
		Date1 = new DateTime(2011, 2, 22, 10, 13, 8, 446);
		assertEquals(d1.getTime(), Date1.getTime());

		s1 = "1298332800000";
		d1 = DateTime.parse(s1);
		Date1 = new DateTime(2011, 2, 22, 2, 0, 0, 0);
		assertEquals(d1.getTime(), Date1.getTime());

		s1 = "2011-02-22";
		d1 = DateTime.parse(s1);
		Date1 = new DateTime(2011, 02, 22);

		assertEquals(d1.getTime(), Date1.getTime());

		s1 = "21:02";
		d1 = DateTime.parse(s1);
		Date1 = new DateTime(21, 2, 0, 0, 0, 0, 0);
		assertEquals(d1.getHour(), Date1.getHour());
		assertEquals(d1.getMinute(), Date1.getMinute());
		assertEquals(d1.getYear(), Date1.getYear());
		assertEquals(d1.getTime(), Date1.getTime());

		s1 = "21:02:52";
		d1 = DateTime.parse(s1);
		Date1 = new DateTime(21, 2, 52, 0, 0, 0, 0);
		assertEquals(d1.getTime(), Date1.getTime());

		s1 = "2011-02-22 10:15:10,5";
		d1 = DateTime.parse(s1);
		Date1 = new DateTime(2011, 2, 22, 10, 15, 10, 5);
		assertEquals(d1.getTime(), Date1.getTime());

		s1 = "11/02/22 10:15:10,5";
		d1 = DateTime.parse(s1);
		Date1 = new DateTime(11, 2, 22, 10, 15, 10, 5);
		assertEquals(d1.getTime(), Date1.getTime());

		s1 = "11-2";
		d1 = DateTime.parse(s1);
		Date1 = new DateTime(11, 2, 0);
		assertEquals(Date1.toString(), d1.toString());
		
		DateTime dn = DateTime.parse("2011-");
		assertEquals("2011.01.01 00:00:00", dn.toString());
	}

	@Test
	public final void testDateTime() {
		Date = new DateTime();
		assertEquals(System.currentTimeMillis(), Date.getTime(), 1000);
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
		Date1 = new DateTime(2011, 2, 22, 10, 13, 8);
		assertEquals(1298362388000L, Date1.getTime());
	}

	
	@Test
	public final void testDateTimeJustDate() {
		JustDate jd = new JustDate(2011, 02, 22);
		Date = new DateTime(jd);

		assertEquals(2011, Date.getYear());
		assertEquals(2, Date.getMonth());
		assertEquals(22, Date.getDom());

		try {
			Date = new DateTime((JustDate) null);
			assertEquals(0, Date.getTime());
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Java lang error. Need BeeRuntimeException " + e.getMessage());
		}
	}

	@Test
	public final void testCompareTo() {
		DateTime dt = new DateTime(1298362388227L);

		assertEquals(0, Date.compareTo(dt));
		dt = new DateTime(2011, 2, 22, 10, 13, 8, 446);
		assertEquals(-1, Date.compareTo(dt));

		dt = new DateTime(2011, 2, 22);
		assertEquals(1, Date.compareTo(dt));
	}

	@Test
	public final void testDeserialize() {
		String st = "1298362388227";
		Date.deserialize(st);

		assertEquals(1298362388227L, Date.getTime());

		st = "1298362388446";
		Date.deserialize(st);

		assertEquals(1298362388446L, Date.getTime());

		st = "1298332800000";
		Date.deserialize(st);

		assertEquals(1298332800000L, Date.getTime());
	}

	@Test
	public final void testEqualsObject() {

		assertEquals(false, Date1.equals("2011-02-22 15:13:08,277"));

		DateTime dt = new DateTime(1298362388227L);
		Date1 = new DateTime(1298362388227L);
		assertEquals(true, Date1.equals(dt));

		dt = new DateTime(2011, 2, 22, 8, 13, 8);
		assertEquals(false, Date1.equals(dt));

		dt = null;
		assertEquals(false, Date1.equals(dt));

		assertEquals(false, Date1.equals(null));
	}

	@Test
	public final void testGetDom() {
		assertEquals(22, Date.getDom());

		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(22, Date.getDom());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(22, Date.getDom());

		Date = new DateTime(2011, 2, 29, 0, 0, 0, 0);
		assertEquals(1, Date.getDom());
		assertEquals(3, Date.getMonth());
	}

	@Test
	public final void testGetDow() {
		assertEquals(3, Date.getDow());

		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(3, Date.getDow());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(3, Date.getDow());

		Date = new DateTime(2011, 03, 30, 2, 1, 2);
		assertEquals(4, Date.getDow());

		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals(1, Date.getDow());

		Date = new DateTime(2011, 03, 19, 5, 1, 45);
		assertEquals(7, Date.getDow());
	}

	@Test
	public final void testGetDoy() {
		assertEquals(3, Date.getDow());

		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(53, Date.getDoy());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(53, Date.getDoy());

		Date = new DateTime(2011, 03, 30, 2, 1, 2);
		assertEquals(89, Date.getDoy());

		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals(65, Date.getDoy());

		Date = new DateTime(2011, 03, 19, 5, 1, 45);
		assertEquals(78, Date.getDoy());

		Date = new DateTime(2011, 01, 01, 5, 1, 45);
		assertEquals(1, Date.getDoy());

		Date = new DateTime(2011, 12, 31, 5, 1, 45);
		assertEquals(365, Date.getDoy());

		Date = new DateTime(2012, 12, 31, 5, 1, 45);
		assertEquals(366, Date.getDoy());
	}

	@Test
	public final void testGetHour() {
		assertEquals(10, Date.getHour());

		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(8, Date.getHour());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
        assertEquals(0, Date.getHour());
		
		Date = new DateTime(2011, 2, 22, 24, 0, 0, 0);
		assertEquals(0, Date.getHour());
		assertEquals(23, Date.getDom());

		Date = new DateTime(2011, 03, 27, 4, 1, 2);
		assertEquals(4, Date.getHour());
	}

	@Test
	public final void testGetMillis() {
		assertEquals(227, Date.getMillis());

		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(446, Date.getMillis());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(0, Date.getMillis());
	}

	@Test
	public final void testGetMinute() {
		assertEquals(13, Date.getMinute());

		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(13, Date.getMinute());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(0, Date.getMinute());
	}

	@SuppressWarnings("unused")
	@Test
	public final void testGetMonth() {
		java.util.Date dt = new java.util.Date(1298362388227L);
		assertEquals(2, Date.getMonth());

		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		dt = new java.util.Date(1298362388446L);
		assertEquals(2, Date.getMonth());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		dt = new java.util.Date(1298332800000L);
		assertEquals(2, Date.getMonth());
	}

	@Test
	public final void testGetSecond() {
		assertEquals(8, Date.getSecond());

		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(8, Date.getSecond());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(0, Date.getSecond());
	}

	@Test
	public final void testGetTime() {
		java.util.Date dt = new java.util.Date(1298362388227L);
		assertEquals(dt.getTime(), Date.getTime());

		Date = new DateTime(2011, 2, 22, 10, 13, 8, 446);
		dt = new java.util.Date(1298362388446L);
		assertEquals(dt.getTime(), Date.getTime());

		Date = new DateTime(2011, 2, 22, 2, 0, 0, 0);
		dt = new java.util.Date(1298332800000L);
		assertEquals(dt.getTime(), Date.getTime());
	}

	@Test
	public final void testGetTimezoneOffset() {
		assertEquals(-120, Date.getTimezoneOffset());
		assertEquals(-120, Date1.getTimezoneOffset());
		Date1 = new DateTime(2011, 3, 26);
		assertEquals(-120, Date1.getTimezoneOffset());
		Date1 = new DateTime(2011, 3, 27);
		assertEquals(-120, Date1.getTimezoneOffset());
	}

	@Test
	public final void testGetUtcDom() {

		assertEquals(22, Date.getUtcDom());
		Date = new DateTime(2011, 2, 22, 10, 13, 8, 446);

		assertEquals(22, Date.getUtcDom());

		Date = new DateTime(2011, 2, 22, 2, 0, 0, 0);
		assertEquals(22, Date.getUtcDom());
		Date1 = new DateTime(2011, 3, 26);
		assertEquals(25, Date1.getUtcDom());
		Date1 = new DateTime(2011, 3, 27);
		assertEquals(26, Date1.getUtcDom());
	}

	@Test
	public final void testGetUtcDow() {
		assertEquals(3, Date.getUtcDow());
		Date = new DateTime(2011, 2, 22, 10, 13, 8, 446);
		assertEquals(3, Date.getUtcDow());

		Date = new DateTime(2011, 2, 22, 2, 0, 0, 0);
		assertEquals(3, Date.getUtcDow());
		Date1 = new DateTime(2011, 3, 26);
		assertEquals(6, Date1.getUtcDow());
		Date1 = new DateTime(2011, 3, 27);
		assertEquals(7, Date1.getUtcDow());

		Date1 = new DateTime(2011, 3, 27, 5, 12, 15);
		assertEquals(1, Date1.getUtcDow());
	}

	@Test
	public final void testGetUtcDoy() {
		assertEquals(53, Date.getUtcDoy());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(53, Date.getUtcDoy());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(52, Date.getUtcDoy());

		Date = new DateTime(2011, 03, 30, 2, 1, 2); 
		assertEquals(23, Date.getUtcHour());
		assertEquals(88, Date.getUtcDoy());

		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals(65, Date.getUtcDoy());

		Date = new DateTime(2011, 03, 19, 5, 1, 45);
		assertEquals(78, Date.getUtcDoy());
	}

	@Test
	public final void testGetUtcHour() {
		assertEquals(8, Date.getUtcHour());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(6, Date.getUtcHour());
		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals(3, Date.getUtcHour());

		Date = new DateTime(2011, 3, 19, 5, 1, 45);
		assertEquals(3, Date.getUtcHour());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(22, Date.getUtcHour());

		Date = new DateTime(2011, 3, 27, 4, 1, 45);
		assertEquals(1, Date.getUtcHour());

		Date = new DateTime(2011, 3, 30, 2, 1, 2);
		assertEquals(23, Date.getUtcHour());
	}

	@Test
	public final void testGetUtcMillis() {
		assertEquals(227, Date.getUtcMillis());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(446, Date.getUtcMillis());
		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals(0, Date.getUtcMillis());

		Date = new DateTime(2011, 03, 19, 5, 1, 45);
		assertEquals(0, Date.getUtcMillis());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(0, Date.getUtcMillis());
		Date = new DateTime(2011, 03, 27, 4, 1, 45);
		assertEquals(0, Date.getUtcMillis());
		assertEquals(0, Date.getUtcMillis());

		Date = new DateTime(2011, 03, 30, 2, 1, 2);
		assertEquals(0, Date.getUtcMillis());
		assertEquals(0, Date.getUtcMillis());
	}

	@Test
	public final void testGetUtcMinute() {
		assertEquals(13, Date.getUtcMinute());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(13, Date.getUtcMinute());
		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals(1, Date.getUtcMinute());

		Date = new DateTime(2011, 03, 19, 5, 1, 45);
		assertEquals(1, Date.getUtcMinute());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(0, Date.getUtcMinute());
		Date = new DateTime(2011, 03, 27, 4, 1, 45);	
		assertEquals(1, Date.getUtcMinute());
		assertEquals(1, Date.getUtcMinute());

		Date = new DateTime(2011, 03, 30, 2, 1, 2);
		assertEquals(1, Date.getUtcMinute());
		assertEquals(1, Date.getUtcMinute());
	}

	@Test
	public final void testGetUtcMonth() {
		assertEquals(2, Date.getUtcMonth());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(2, Date.getUtcMonth());
		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals(3, Date.getUtcMonth());

		Date = new DateTime(2011, 03, 19, 5, 1, 45);
		assertEquals(3, Date.getUtcMonth());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(2, Date.getUtcMonth());
		Date = new DateTime(2011, 03, 27, 4, 1, 45);
		assertEquals(3, Date.getUtcMonth());
		assertEquals(3, Date.getUtcMonth());

		Date = new DateTime(2011, 03, 30, 2, 1, 2);
		assertEquals(3, Date.getUtcMonth());
		assertEquals(3, Date.getUtcMonth());

		Date = new DateTime(2011, 01, 30, 2, 1, 2);
		assertEquals(1, Date.getUtcMonth());
		assertEquals(1, Date.getUtcMonth());

		Date = new DateTime(2011, 12, 30, 2, 1, 2);
		assertEquals(12, Date.getUtcMonth());
		assertEquals(12, Date.getUtcMonth());
	}

	@Test
	public final void testGetUtcSecond() {
		assertEquals(8, Date.getUtcSecond());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(8, Date.getUtcSecond());
		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals(45, Date.getUtcSecond());

		Date = new DateTime(2011, 03, 19, 5, 1, 45);
		assertEquals(45, Date.getUtcSecond());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		assertEquals(0, Date.getUtcSecond());
		Date = new DateTime(2011, 03, 27, 4, 1, 45);
		assertEquals(45, Date.getUtcSecond());

		Date = new DateTime(2011, 03, 30, 2, 1, 2);
		assertEquals(2, Date.getUtcSecond());

		Date = new DateTime(2011, 01, 30, 2, 1, 2);
		assertEquals(2, Date.getUtcSecond());

		Date = new DateTime(2011, 12, 30, 2, 1, 2);
		assertEquals(2, Date.getUtcSecond());

	}

	@Test
	public final void testGetUtcYear() {
		assertEquals(2011, Date.getUtcYear());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals(2011, Date.getUtcYear());
		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals(2011, Date.getUtcYear());
		Date = new DateTime(2011, 1, 1, 1, 1, 45);
		assertEquals(2010, Date.getUtcYear());
	}

	@SuppressWarnings("unused")
	@Test
	public final void testGetYear() {
		java.util.Date dt = new java.util.Date(1298362388227L);
		assertEquals(2011, Date.getYear());

		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		dt = new java.util.Date(1298362388446L);
		assertEquals(2011, Date.getYear());

		Date = new DateTime(2011, 2, 22, 0, 0, 0, 0);
		dt = new java.util.Date(1298332800000L);
		assertEquals(2011, Date.getYear());
	}

	@Test
	public final void testSerialize() {
		Date = new DateTime(2011, 2, 22, 2, 0, 0, 0);
		String a = Date.serialize();
		assertEquals("1298332800000", a);

		Date = new DateTime(2011, 2, 22, 10, 13, 8, 227);
		a = Date.serialize();
		assertEquals("1298362388227", a);
	}

	@Test
	public final void testSetTime() {
		Date = new DateTime();
		Date.setTime(1298362388227L);
		assertEquals(1298362388227L, Date.getTime());
		assertEquals(2011, Date.getYear());
		assertEquals(2, Date.getMonth());
		assertEquals(22, Date.getDom());
		assertEquals(8, Date.getUtcHour());
		assertEquals(10, Date.getHour());
		assertEquals(13, Date.getMinute());
		assertEquals(8, Date.getSecond());
		assertEquals(227, Date.getMillis());
	}

	@Test
	public final void testToDateString() {
		assertEquals("2011.02.22", Date.toDateString());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals("2011.02.22", Date.toDateString());
		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals("2011.03.06", Date.toDateString());

		Date = new DateTime(2011, 03, 19, 5, 1, 45);
		assertEquals("2011.03.19", Date.toDateString());

		Date = new DateTime(2011, 03, 27, 4, 1, 45);
		assertEquals("2011.03.27", Date.toDateString());

		Date = new DateTime(2011, 01, 01, 0, 1, 2);
		assertEquals("2011.01.01", Date.toDateString());
	}

	@Test
	public final void testToString() {
		assertEquals("2011.02.22 10:13:08.227", Date.toString());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals("2011.02.22 08:13:08.446", Date.toString());
		Date = new DateTime(2011, 03, 6, 5, 1, 45, 1);
		assertEquals("2011.03.06 05:01:45.001", Date.toString());

		Date = new DateTime(2011, 03, 19, 5, 1, 45, 10);
		assertEquals("2011.03.19 05:01:45.010", Date.toString());

		Date = new DateTime(2011, 03, 27, 4, 1, 45);
		assertEquals("2011.03.27 04:01:45", Date.toString());

		Date = new DateTime(2011, 01, 01, 0, 1, 2);
		assertEquals("2011.01.01 00:01:02", Date.toString());
	}

	@Test
	public final void testToTimeString() {
		assertEquals("10:13:08.227", Date.toTimeString());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals("08:13:08.446", Date.toTimeString());
		Date = new DateTime(2011, 03, 6, 5, 1, 45, 1);
		assertEquals("05:01:45.001", Date.toTimeString());

		Date = new DateTime(2011, 03, 19, 5, 1, 45, 10);
		assertEquals("05:01:45.010", Date.toTimeString());

		Date = new DateTime(2011, 03, 27, 4, 1, 45);
		assertEquals("04:01:45", Date.toTimeString());

		Date = new DateTime(2011, 01, 01, 0, 1, 2);
		assertEquals("00:01:02", Date.toTimeString());
	}

	@Test
	public final void testToUtcDateString() {
		assertEquals("2011.02.22", Date.toUtcDateString());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals("2011.02.22", Date.toUtcDateString());
		Date = new DateTime(2011, 03, 6, 5, 1, 45);
		assertEquals("2011.03.06", Date.toUtcDateString());

		Date = new DateTime(2011, 03, 19, 5, 1, 45);
		assertEquals("2011.03.19", Date.toUtcDateString());

		Date = new DateTime(2011, 03, 27, 4, 1, 45);	
		assertEquals("2011.03.27", Date.toUtcDateString());

		Date = new DateTime(2011, 01, 01, 0, 1, 2);
		assertEquals("2010.12.31", Date.toUtcDateString());
	}

	@Test
	public final void testToUtcString() {
		assertEquals("2011.02.22 08:13:08.227", Date.toUtcString());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals("2011.02.22 06:13:08.446", Date.toUtcString());
		Date = new DateTime(2011, 03, 6, 5, 1, 45, 1);
		assertEquals("2011.03.06 03:01:45.001", Date.toUtcString());

		Date = new DateTime(2011, 03, 19, 5, 1, 45, 10);
		assertEquals("2011.03.19 03:01:45.010", Date.toUtcString());

		Date = new DateTime(2011, 01, 01, 0, 1, 2);
		assertEquals("2010.12.31 22:01:02", Date.toUtcString());

		Date = new DateTime(2011, 03, 27, 4, 1, 45);
		assertEquals("2011.03.27 01:01:45", Date.toUtcString());

	}

	@Test
	public final void testToUtcTimeString() {
		assertEquals("08:13:08.227", Date.toUtcTimeString());
		Date = new DateTime(2011, 2, 22, 8, 13, 8, 446);
		assertEquals("06:13:08.446", Date.toUtcTimeString());
		Date = new DateTime(2011, 03, 6, 5, 1, 45, 1);
		assertEquals("03:01:45.001", Date.toUtcTimeString());

		Date = new DateTime(2011, 03, 19, 5, 1, 45, 10);
		assertEquals("03:01:45.010", Date.toUtcTimeString());

		Date = new DateTime(2011, 01, 01, 0, 1, 2);
		assertEquals("22:01:02", Date.toUtcTimeString());

		Date = new DateTime(2011, 03, 27, 4, 1, 45);
		assertEquals("01:01:45", Date.toUtcTimeString());
	}

}

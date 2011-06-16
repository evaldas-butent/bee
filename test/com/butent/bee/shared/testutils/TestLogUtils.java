package com.butent.bee.shared.testutils;

import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.utils.LogUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * Tests {@link com.butent.bee.shared.utils.LogUtils}.
 */
public class TestLogUtils {

  private static DateTime BDDate1;
  private static DateTime BDDate2;
  private static DateTime BDDate3;
  private static Throwable TTrow1;
  @SuppressWarnings("unused")
  private static Logger Logger1;

  @Test
  public final void infoUtc() {
    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Formatter formatter = new SimpleFormatter();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Handler handler = new StreamHandler(out, formatter);
    logger.addHandler(handler);
    LogUtils.infoUtc(logger, "laikas");

    handler.flush();
    String msg = out.toString();
    assertNotNull(msg);

    System.out.println(msg);
    assertTrue(msg.contains("INFO:"));
    assertTrue(msg.contains("laikas"));
  }

  @Before
  public void setUp() throws Exception {

    BDDate1 = new DateTime();
    BDDate2 = new DateTime();
    BDDate3 = new DateTime();

    long t1 = 1298362388227L;
    BDDate1.setTime(t1);
    BDDate2.setTime(0);
    BDDate3.setTime(Long.MIN_VALUE);

    TTrow1 = new Throwable("This is a simple error");
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testDateToLogBeeDate() {
    assertEquals("10:13:08.227", LogUtils.dateToLog(BDDate1));
    assertEquals("03:00:00", LogUtils.dateToLog(BDDate2));
    assertEquals("18:47:04.192", LogUtils.dateToLog(BDDate3));
  }

  @Test
  public final void testDateToLogLong() {
    assertEquals("10:13:08.227", LogUtils.dateToLog(1298362388227L));
    assertEquals("03:00:00", LogUtils.dateToLog(0));
    assertEquals("18:47:04.192", LogUtils.dateToLog(Long.MIN_VALUE));
  }

  @Test
  public final void testErrorLoggerThrowableObjectArray() {
    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Formatter formatter = new SimpleFormatter();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Handler handler = new StreamHandler(out, formatter);
    logger.addHandler(handler);

    LogUtils.error(logger, TTrow1, "#", "komentaras apie klaida", "$", "antras kom");
    handler.flush();
    String msg = out.toString();
    assertNotNull(msg);

    assertTrue(msg.toLowerCase().contains("49"));
    assertTrue(msg.toLowerCase().contains("# komentaras apie klaida $ antras kom"));
    assertTrue(msg.toLowerCase().contains("this is a simple"));
  }

  @Test
  public final void testInfoLoggerObjectArray() {
    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Formatter formatter = new SimpleFormatter();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Handler handler = new StreamHandler(out, formatter);
    logger.addHandler(handler);

    LogUtils.info(logger, "this", "is", "a", "string");

    handler.flush();
    String msg = out.toString();
    assertNotNull(msg);

    assertTrue(msg.toLowerCase().contains("info: this is a string"));
    assertTrue(msg.toLowerCase().contains("info"));
  }

  @Test
  public final void testInfoLoggerString() {
    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Formatter formatter = new SimpleFormatter();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Handler handler = new StreamHandler(out, formatter);
    logger.addHandler(handler);

    LogUtils.info(logger, "One string");

    handler.flush();
    String msg = out.toString();
    assertNotNull(msg);

    assertTrue(msg.toLowerCase().contains("info: one string"));
  }

  @Test
  public final void testInfoNow() {

    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Formatter formatter = new SimpleFormatter();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Handler handler = new StreamHandler(out, formatter);
    logger.addHandler(handler);

    LogUtils.infoNow(logger, "One string", "became two");

    handler.flush();
    String msg = out.toString();
    assertNotNull(msg);

    assertTrue(msg.contains("One string became two"));
    assertTrue(msg.contains(LogUtils.now().substring(0, 4)));
  }

  @Test
  public final void testIsOff() {
    assertEquals(false, LogUtils.isOff(null));
    assertEquals(false, LogUtils.isOff(Level.FINE));
    assertEquals(true, LogUtils.isOff(Level.OFF));
  }

  @Test
  public final void testLogLoggerLevelObjectArray() {
    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Formatter formatter = new SimpleFormatter();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Handler handler = new StreamHandler(out, formatter);
    logger.addHandler(handler);

    LogUtils.log(logger, Level.SEVERE, "Testing", "log");

    handler.flush();
    String msg = out.toString();
    assertNotNull(msg);

    System.out.println(msg);
    assertTrue(msg.contains("SEVERE: Testing log"));
  }

  @Test
  public final void testSetDefaultLogger() {
    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Field field = null;
    Object rez = null;

    LogUtils.setDefaultLogger(logger);
    try {
      field = LogUtils.class.getDeclaredField("defaultLogger");
      field.setAccessible(true);
      try {
        rez = field.get(LogUtils.class);
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
    assertEquals(logger.toString(), rez.toString());
  }

  @Test
  public final void testSevereLoggerObjectArray() {
    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Formatter formatter = new SimpleFormatter();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Handler handler = new StreamHandler(out, formatter);
    logger.addHandler(handler);

    LogUtils.severe(logger, "Testing", "severe");

    handler.flush();
    String msg = out.toString();
    assertNotNull(msg);

    System.out.println(msg);
    assertTrue(msg.contains("SEVERE: Testing severe"));
  }

  @Test
  public final void testSevereLoggerThrowableObjectArray() {
    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Formatter formatter = new SimpleFormatter();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Handler handler = new StreamHandler(out, formatter);
    logger.addHandler(handler);

    Throwable err = new Throwable("STOP, this is a critical error");

    LogUtils.severe(logger, null, "Testing", "severe");

    handler.flush();
    String msg = out.toString();
    assertNotNull(msg);

    System.out.println(msg);
    assertTrue(msg.contains("SEVERE: Testing severe"));

    LogUtils.severe(logger, err);

    handler.flush();
    msg = out.toString();
    assertNotNull(msg);

    System.out.println(msg);

    assertTrue(msg.contains("SEVERE: java.lang.Throwable: STOP, this is a critical error"));
  }

  @Test
  public final void testStack() {
    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Formatter formatter = new SimpleFormatter();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Handler handler = new StreamHandler(out, formatter);
    logger.addHandler(handler);

    LogUtils.stack(logger, TTrow1);
    handler.flush();
    String msg = out.toString();
    assertNotNull(msg);
    assertTrue(msg.toLowerCase().contains("49"));
  }

  @Test
  public final void testTransformLevel() {

    assertEquals("FINEST", LogUtils.transformLevel(Level.FINEST));
    assertEquals("SEVERE", LogUtils.transformLevel(Level.SEVERE));
    assertEquals("", LogUtils.transformLevel(null));
  }

  @Test
  public final void testUtc() {

    long dabar = System.currentTimeMillis();
    DateTime data = new DateTime(dabar);

    System.out.println(Integer.toString(data.getHour()));

    assertEquals(true, LogUtils.utc().contains(Integer.toString(data.getHour() - 3)));
    assertEquals(true, LogUtils.utc().contains(Integer.toString(data.getMinute())));
  }

  @Test
  public final void testWarningLogger() {
    Logger logger = Logger.getLogger(TestLogUtils.class.getName());

    Formatter formatter = new SimpleFormatter();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Handler handler = new StreamHandler(out, formatter);
    logger.addHandler(handler);

    Throwable err = new Throwable("STOP, this is a critical error");

    LogUtils.warning(logger, "Testing", "severe");

    handler.flush();
    String msg = out.toString();
    assertNotNull(msg);

    System.out.println(msg);
    assertTrue(msg.contains("WARNING: Testing severe"));

    LogUtils.warning(logger, "Testing severe");
    handler.flush();
    msg = out.toString();
    assertTrue(msg.contains("WARNING: Testing severe"));

    LogUtils.warning(logger, null, "Testing severe");
    handler.flush();
    msg = out.toString();
    assertTrue(msg.contains("WARNING: Testing severe"));

    System.out.println(msg);
    LogUtils.warning(logger, err, "Testing severe");
    handler.flush();
    msg = out.toString();
    assertTrue(msg.contains("WARNING: java.lang.Throwable: STOP, this is a critical error"));
  }

}

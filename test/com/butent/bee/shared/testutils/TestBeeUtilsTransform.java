package com.butent.bee.shared.testutils;

import com.google.gwt.dev.util.collect.HashMap;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Tests {@link com.butent.bee.shared.utils.BeeUtils}.
 */
@SuppressWarnings("static-access")
public class TestBeeUtilsTransform extends TestCase implements ILogger {

  private BeeUtils beeUtils;
  private final boolean allowLogging = false;
  private TransObject obj;

  @Override
  public void log(String msg) {
    if (this.allowLogging) {
      System.out.print(msg);
    }
  }

  @Before
  public void setUp() throws Exception {
    log("Starting test TestBeeUtilsTransform \n \r");
    obj = new TransObject();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testAllEmpty() {
    assertEquals(false, beeUtils.allEmpty(5, 4, "3", 2, null, 10.0, 11));
    assertEquals(false, beeUtils.allEmpty(5, 4, "3", 2, "null", 10.0, 11));
    assertEquals(true, beeUtils.allEmpty("", null));
  }

  @Test
  public void testAllNotEmpty() {
    assertEquals(false, beeUtils.allNotEmpty(5, 4, "3", 2, null, 10.0, 11));
    assertEquals(!false, beeUtils.allNotEmpty(5, 4, "3", 2, "null", 10.0, 11));
    assertEquals(!true, beeUtils.allNotEmpty("", null));
  }

  @Test
  public void testAppend() {
    StringBuilder a = new StringBuilder();
    java.lang.Object b[] = {'a', 'b', 'c'};
    assertSame(a, beeUtils.append(a, (Object[]) null, ';'));
    assertEquals("a;b;c", beeUtils.append(a, b, ';').toString());
  }

  @Test
  public void testAppendIter() {
    StringBuilder a = new StringBuilder();
    StringBuilder c = new StringBuilder();
    List<Object> testc1 = new ArrayList<Object>();
    List<Object> testc2 = new ArrayList<Object>();
    testc1.add("Text");
    testc1.add(5);

    assertEquals("Text;5", beeUtils.append(a, testc1, ';').toString());
    assertEquals("", beeUtils.append(c, testc2, ';').toString());
  }

  @Test
  public void testBetweenExclusive() {
    assertEquals(true, beeUtils.betweenExclusive(5, 5, 10));
    assertEquals(false, beeUtils.betweenExclusive(1, 5, 10));
    assertEquals(false, beeUtils.betweenExclusive(10, 5, 10));
    assertEquals(false, beeUtils.betweenExclusive(100, 5, 10));
  }

  @Test
  public void testBetweenInclusive() {
    assertEquals(true, beeUtils.betweenInclusive(5, 5, 10));
    assertEquals(false, beeUtils.betweenInclusive(1, 5, 10));
    assertEquals(true, beeUtils.betweenInclusive(10, 5, 10));
    assertEquals(false, beeUtils.betweenInclusive(100, 5, 10));
  }

  @Test
  public void testBracket() {
    assertEquals("[Win]", beeUtils.bracket("Win"));
    assertEquals("", beeUtils.bracket(""));
    assertEquals("", beeUtils.bracket(null));
    assertEquals("[Win Win]", beeUtils.bracket("Win Win"));
  }

  @Test
  public void testClamp() {
    assertEquals(10, beeUtils.clamp(5, 10, 15));
    assertEquals(15, beeUtils.clamp(20, 1, 15));
    assertEquals(5, beeUtils.clamp(5, 1, 15));
    assertEquals(10.0, beeUtils.clamp(5.0, 10.0, 15.0));
    assertEquals(15.0, beeUtils.clamp(20.0, 1.0, 15.0));
    assertEquals(5.0, beeUtils.clamp(5.0, 1.00, 15.0));
    assertEquals(10.0, beeUtils.clamp(5, 10.0, 15.0));
    assertEquals(1.0, beeUtils.clamp(Double.MAX_VALUE * 5, 1.0, 15.0));
    assertEquals(15.0, beeUtils.clamp(Double.MAX_VALUE * 5, Double.MAX_VALUE * 5, 15.0));
    assertEquals(Double.POSITIVE_INFINITY, beeUtils.clamp(Double.MAX_VALUE * 5,
        Double.MAX_VALUE * 5, Double.MAX_VALUE * 5));
    assertEquals(5.0, beeUtils.clamp(5.0, 1, 15));
    assertEquals(5.0, beeUtils.clamp(5.0, Double.MAX_VALUE * 5, Double.MAX_VALUE * 5));
    assertEquals(5.0, beeUtils.clamp(5.0, 2.0, Double.MAX_VALUE * 5));
    assertEquals(5.0, beeUtils.clamp(5.0, Double.MAX_VALUE * 5, 10.0));
  }

  @Test
  public void testClip() {
    assertEquals("This i...[6/18]", beeUtils.clip("This is a sentence", 6));
    assertEquals("", beeUtils.clip("", 6));
    assertEquals("Win", beeUtils.clip("Win", 6));
    assertEquals("Win", beeUtils.clip("Win          ", 6));
  }

  @Test
  public void testCompare() {
    List<Object> testc1 = new ArrayList<Object>();
    List<Object> testc2 = new ArrayList<Object>();
    testc1.add("Text");
    testc1.add(5);

    testc2.add(5);
    testc2.add(2);
    testc2.add(10);

    assertEquals(BeeConst.COMPARE_EQUAL, beeUtils.compare("", ""));
    assertEquals(BeeConst.COMPARE_EQUAL, beeUtils.compare("Compare", "Compare"));
    assertEquals(true, beeUtils.compare("Aompare", "Compare") < 0);
    assertEquals(true, beeUtils.compare("Compare", "Aompare") > 0);
    assertEquals(true, beeUtils.compare(null, "Aompare") < 0);
    assertEquals(true, beeUtils.compare("Compare", null) > 0);
    assertEquals(BeeConst.COMPARE_EQUAL, beeUtils.compare((Object) null, (Object) null));
    assertEquals(true, beeUtils.compare(testc1, testc2) > 0);
    assertEquals(true, beeUtils.compare(7, 6) > 0);
    assertEquals(BeeConst.COMPARE_EQUAL, beeUtils.compare(6, 6));
    assertEquals(true, beeUtils.compare(5, 6) < 0);

    assertEquals(true, beeUtils.compare('a', 'c') < 0);
    assertEquals(BeeConst.COMPARE_EQUAL, beeUtils.compare('c', 'c'));
    assertEquals(false, beeUtils.compare('c', 'a') < 0);

    assertEquals(true, beeUtils.compare(true, false) > 0);
    assertEquals(true, beeUtils.compare(false, true) < 0);
    assertEquals(BeeConst.COMPARE_EQUAL, beeUtils.compare(true, true));
    assertEquals(BeeConst.COMPARE_EQUAL, beeUtils.compare(false, false));

    assertEquals(BeeConst.COMPARE_EQUAL, beeUtils.compare(5.0, 5.0));
    assertEquals(true, beeUtils.compare(-2.0, 5.69) < 0);
    assertEquals(true, beeUtils.compare(5.11, 3.0) > 0);
    assertEquals(true, beeUtils.compare((Object) null, 5) < 0);
    assertEquals(true, beeUtils.compare(5, (Object) null) > 0);
    assertEquals(BeeConst.COMPARE_EQUAL, beeUtils.compare((Object) 5, (Object) 5));
  }

  @Test
  public final void testCompareNormalized() {
    assertEquals(0, BeeUtils.compareNormalized(null, null));
    assertEquals(0, BeeUtils.compareNormalized("", null));
    assertEquals(0, BeeUtils.compareNormalized(null, ""));
    assertEquals(0, BeeUtils.compareNormalized("", ""));
    assertEquals(0, BeeUtils.compareNormalized("Normalized", "normaLIZED"));
    assertEquals(-5, BeeUtils.compareNormalized("Normalized", "SnormaLIZED"));
  }

  @Test
  public void testConcat() {
    assertEquals("", BeeUtils.concat());
    assertEquals("is:a:test", BeeUtils.concat(":", "is", "a", "test"));
  }

  @Test
  public void testContext() {
    assertEquals(false, beeUtils.context((CharSequence) null, ""));
    assertEquals(false, beeUtils.context("", (CharSequence) null));
    assertEquals(false, beeUtils.context((CharSequence) null, (CharSequence) null));
    assertEquals(false, beeUtils.context("", ""));
    assertEquals(true, beeUtils.context("is", "THIS IS A STRING"));
    assertEquals(false, beeUtils.context("isa", "THIS IS A STRING"));
  }

  @Test
  public void testContextCollection() {
    List<CharSequence> testc1 = new ArrayList<CharSequence>();
    List<CharSequence> testc2 = new ArrayList<CharSequence>();
    testc1.add("A");
    testc1.add("simple text");
    testc1.add("TEXT2");

    assertEquals(true, beeUtils.context("text", testc1));
    assertEquals(true, beeUtils.context("text2", testc1));
    assertEquals(false, beeUtils.context("texx", testc1));
    assertEquals(false, beeUtils.context("", testc1));
    assertEquals(false, beeUtils.context("text", testc2));
  }

  @Test
  public void testContextCollectionString() {
    List<CharSequence> testc1 = new ArrayList<CharSequence>();
    List<CharSequence> testc2 = new ArrayList<CharSequence>();
    testc1.add("A");
    testc1.add("text");
    testc1.add("TEXT2");

    assertEquals(true, beeUtils.context(testc1, "text"));
    assertEquals(true, beeUtils.context(testc1, "text2"));
    assertEquals(false, beeUtils.context(testc1, "texx"));
    assertEquals(false, beeUtils.context(testc1, ""));
    assertEquals(false, beeUtils.context(testc2, "text"));
  }

  @Test
  public final void testCount() {
    assertEquals(0, BeeUtils.count(null, 'a'));
    assertEquals(0, BeeUtils.count("", 'a'));
    assertEquals(5, BeeUtils.count("abracadabra", 'a'));
    assertEquals(0, BeeUtils.count("AbrA cAdAbrA", 'a'));
  }

  @Test
  public void testDelete() {
    assertEquals("", beeUtils.delete(null, 0, 5));
    assertEquals("", beeUtils.delete("", 0, 5));
    assertEquals("", beeUtils.delete("This is a string", -5, 88));
    assertEquals(" a string", beeUtils.delete("This is a string", -5, 7));
    assertEquals("This is a string", beeUtils.delete("This is a string", 30, 5));
    assertEquals("", beeUtils.delete("This is a string", 0, 50));
    assertEquals("Thi", beeUtils.delete("This is a string", 3, 50));
    assertEquals("Thiis a string", beeUtils.delete("This is a string", 3, 5));

    assertEquals("This is a string", beeUtils.delete("This is a string", -5, -10));
    assertEquals("This is a string", beeUtils.delete("This is a string", -10, -5));
    assertEquals("s is a string", beeUtils.delete("This is a string", -5, 3));
    assertEquals("This is a string", beeUtils.delete("This is a string", 80, 40));
  }

  @Test
  public void testElapsedSeconds() {
    long time = System.currentTimeMillis();
    assertEquals("[0.000]", beeUtils.elapsedSeconds(time));
  }

  @Test
  public void testEquals() {
    assertEquals(true, beeUtils.equals(5, 5));
    assertEquals(false, beeUtils.equals(5, 7));
    assertEquals(true, beeUtils.equals(5.0, 5.0));
    assertEquals(false, beeUtils.equals(5.0, 7.0));
    assertEquals(true, beeUtils.equals("win", "win"));
    assertEquals(true, beeUtils.equals(null, null));
    assertEquals(false, beeUtils.equals(null, "55"));
    assertEquals(false, beeUtils.equals(5, null));
  }

  @Test
  public void testEqualsTrim() {
    assertEquals(true, beeUtils.equalsTrim("", ""));
    assertEquals(true, beeUtils.equalsTrim("   ", "   "));
    assertEquals(true, beeUtils.equalsTrim("   This is a sentence", "This is a sentence     "));
    assertEquals(true, beeUtils.equalsTrim("   This is a sentence \n\t", "This is a sentence     "));
    assertEquals(true, beeUtils.equalsTrim(null, null));
    assertEquals(false, beeUtils.equalsTrim("5", null));
    assertEquals(false, beeUtils.equalsTrim(null, "5"));
  }

  @Test
  public void testExp10() {
    assertEquals(100, beeUtils.exp10(2));
    assertEquals(1000, beeUtils.exp10(3));
  }

  @Test
  public void testFitStart() {
    assertEquals(0, beeUtils.fitStart(0, 5, 10));
    assertEquals(-5, beeUtils.fitStart(0, 15, 10));
    assertEquals(4, beeUtils.fitStart(5, 6, 10));
  }

  @Test
  public void testFitStartMax() {
    assertEquals(2, beeUtils.fitStart(0, 5, 10, 2));
    assertEquals(2, beeUtils.fitStart(0, 15, 10, 2));
    assertEquals(4, beeUtils.fitStart(5, 6, 10, 2));
  }

  @Test
  public void testFromHex() {
    assertEquals("$", new String(beeUtils.fromHex("24")));
    assertEquals("]", new String(beeUtils.fromHex("005D")));
    assertEquals("]]", new String(beeUtils.fromHex("005D005D")));
    assertEquals("]", new String(beeUtils.fromHex("005d")));
    assertEquals("!P", new String(beeUtils.fromHex("210050")));
    assertEquals("]]", new String(beeUtils.fromHex("005d005d")));
    assertEquals(null, beeUtils.fromHex("242Z"));
    assertEquals(null, beeUtils.fromHex(null));
  }

  @Test
  public final void testGetClassName() {
    assertEquals("TestBeeUtilsTransform", NameUtils.getClassName(this.getClass()));
    assertEquals("BeeUtils", NameUtils.getClassName(BeeUtils.class));
  }

  @Test
  public void testGetContext() {
    List<CharSequence> testc1 = new ArrayList<CharSequence>();
    List<CharSequence> testc2 = new ArrayList<CharSequence>();
    testc1.add("A");
    testc1.add("simple text");
    testc1.add("TEXT2");

    List<CharSequence> rez1 = new ArrayList<CharSequence>();
    rez1.add("simple text");
    rez1.add("TEXT2");

    List<CharSequence> rez2 = new ArrayList<CharSequence>();
    rez2.add("TEXT2");

    List<CharSequence> rez3 = new ArrayList<CharSequence>();

    assertEquals(rez1, beeUtils.getContext("text", testc1));
    assertEquals(rez2, beeUtils.getContext("text2", testc1));
    assertEquals(rez3, beeUtils.getContext("texx", testc1));
    assertEquals(rez3, beeUtils.getContext("", testc1));
    assertEquals(testc2, beeUtils.getContext("text", testc2));
  }

  @Test
  public void testGetKey() {
    Map<String, Integer> testValue6 = new HashMap<String, Integer>();
    testValue6.put("1", 10);
    testValue6.put("2", 25);
    testValue6.put("4", 20);
    testValue6.put("3", -10);
    assertEquals("2", beeUtils.getKey(testValue6, 25));
    assertEquals(null, beeUtils.getKey(testValue6, 5));
  }

  @Test
  public void testGetPrefix() {
    assertEquals("for example", beeUtils.getPrefix("for example, this test...", ','));
    assertEquals("", beeUtils.getPrefix(", bad example", ','));
    assertEquals("", beeUtils.getPrefix("There is not a seperator", ','));
    assertEquals("", beeUtils.getPrefix("for example, this test...", '\0'));
    assertEquals("for example, t", beeUtils.getPrefix("for example, t\0his test...", '\0'));
    assertEquals("", beeUtils.getPrefix(null, '\0'));

    assertEquals("for example", beeUtils.getPrefix("for example, this test...", ","));
    assertEquals("", beeUtils.getPrefix(", bad example", ","));
    assertEquals("", beeUtils.getPrefix("There is not a seperator", ","));
    assertEquals("", beeUtils.getPrefix("for example, this test...", "\0"));
    assertEquals("for example, t", beeUtils.getPrefix("for example, t\0his test...", "\0"));
    assertEquals("for example,", beeUtils.getPrefix("for example, this test...", "this"));
    assertEquals("", beeUtils.getPrefix("for example, this test...", null));
  }

  @Test
  public void testGetQuietly() {
    List<CharSequence> testc1 = new ArrayList<CharSequence>();
    List<CharSequence> testc2 = new ArrayList<CharSequence>();
    List<CharSequence> rez1 = new ArrayList<CharSequence>();
    testc1.add("A");
    testc1.add("simple text");
    testc1.add("TEXT2");

    rez1.add("simple text");

    assertEquals("simple text", beeUtils.getQuietly(testc1, 1));
    assertEquals(null, beeUtils.getQuietly(testc1, -1));
    assertEquals(null, beeUtils.getQuietly(testc2, 5));
  }

  @Test
  public void testGetSuffix() {
    assertEquals("this test...", beeUtils.getSuffix("for example, this test...", ','));
    assertEquals("", beeUtils.getSuffix(" bad example,", ','));
    assertEquals("", beeUtils.getSuffix("There is not a seperator", ','));
    assertEquals("", beeUtils.getSuffix("for example, this test...", '\0'));
    assertEquals("his test...", beeUtils.getSuffix("for example, t\0his test...", '\0'));
    assertEquals("", beeUtils.getSuffix(null, '\0'));

    assertEquals("test...", beeUtils.getSuffix("for example, this ,test...", ","));
    assertEquals("", beeUtils.getSuffix(", bad example,", ","));
    assertEquals("", beeUtils.getSuffix("There is not a seperator", ","));
    assertEquals("", beeUtils.getSuffix("for example, this test...", "\0"));
    assertEquals("his test...", beeUtils.getSuffix("for example, t\0his test...", "\0"));
    assertEquals("test...", beeUtils.getSuffix("for example, this test...", "this"));
    assertEquals("", beeUtils.getSuffix("for example, this test...", null));
  }

  @Test
  public void testIfString() {
    assertEquals("test", beeUtils.ifString("test", "not"));
    assertEquals("not", beeUtils.ifString(10, "not"));
    assertEquals("not", beeUtils.ifString(null, "not"));
    assertEquals("test", beeUtils.ifString("test", null));
    assertEquals(null, beeUtils.ifString(10, null));
    assertEquals("not", beeUtils.ifString("", "not"));
    assertEquals(null, beeUtils.ifString("", null));
    assertEquals("", beeUtils.ifString("", ""));
  }

  @Test
  public void testIiff() {
    assertEquals("Teisinga", beeUtils.iif((1 < 2), "Teisinga", "Blogai"));
    assertEquals("Blogai", beeUtils.iif((1 > 2), "Teisinga", "Blogai"));
    assertEquals("Teisinga", beeUtils.iif((1 > 2), "Teisinga", (1 < 2), "Teisinga", "Blogai"));
    assertEquals("Teisinga", beeUtils.iif((1 < 2), "Teisinga", (1 < 2), "Teisinga", "Blogai"));
    assertEquals("Teisinga", beeUtils.iif((1 > 2), "Teisinga", (1 < 2), "Teisinga", "Blogai"));
    assertEquals("Blogai", beeUtils.iif((1 > 2), "Teisinga", (1 > 2), "Teisinga", "Blogai"));
    assertEquals("Blogai", beeUtils.iif((1 > 2), "Teisinga", (1 > 2), "Teisinga", "Blogai"));
  }

  @Test
  public void testIncrement() {
    assertEquals("-1", beeUtils.increment("-2"));
    assertEquals("1", beeUtils.increment(false));
    assertEquals("1", beeUtils.increment(true));
    assertEquals("5", beeUtils.increment(4));
    assertEquals("5", beeUtils.increment(4.5));
    assertEquals("1", beeUtils.increment('a'));
    assertEquals("1", beeUtils.increment(null));
    assertEquals("1", beeUtils.increment(""));
  }

  @Test
  public void testInList() {
    assertEquals(true, beeUtils.inList("text", "this", "is", "a", "text"));
    assertEquals(false, beeUtils.inList("texts", "this", "is", "a", "text"));
  }

  @Test
  public void testInListIgnoreCase() {
    assertEquals(true, beeUtils.inListIgnoreCase("text", "this", "is", "a", "tExt"));
    assertEquals(false, beeUtils.inListIgnoreCase("texts", "this", "is", "a", "text"));
    assertEquals(true, beeUtils.inListIgnoreCase("TeXT", "this", "is", "a", "text"));
    assertEquals(false, beeUtils.inListIgnoreCase("TEXTS", "this", "is", "a", "text"));
  }

  @Test
  public void testInListSame() {
    assertEquals(true, beeUtils.inListSame("text", "this", "is", "a", "tExt"));
    assertEquals(false, beeUtils.inListSame("texts", "this", "is", "a", "text"));
    assertEquals(true, beeUtils.inListSame("TeXT", "this", "is", "a", "text"));
    assertEquals(false, beeUtils.inListSame("TEXTS", "this", "is", "a", "text"));

    assertEquals(true, beeUtils.inListSame("text       ", "this", "is", "a", "tExt"));
    assertEquals(false, beeUtils.inListSame("texts", "this", "is", "a", "      text"));
    assertEquals(true, beeUtils.inListSame("  \n \0 teXT   ", "this", "is", "a", "text    "));
    assertEquals(false, beeUtils.inListSame("TS    ", "this", "   is", "a", "text"));
    assertEquals(true, beeUtils.inListSame("is   ", "this", null, "   is", "text"));
  }

  @Test
  public void testInsert() {
    assertEquals("This is a test", beeUtils.insert("Ths is a test", 2, 'i'));
    assertEquals("iThs is a test", beeUtils.insert("Ths is a test", 0, 'i'));
    assertEquals("Ths is a testi", beeUtils.insert("Ths is a test", 13, 'i'));
  }

  @Test
  public void testInsertCS() {
    assertEquals("This is a test", beeUtils.insert("T is a test", 1, "his"));
    assertEquals("This is a test", beeUtils.insert(" is a test", 0, "This"));
    assertEquals("This is a test and it works", beeUtils.insert("This is a test", 14,
        " and it works"));
  }

  @Test
  public void testInstanceOfFloatingPoint() {
    float a = (float) 10.0;
    double b = -0.0;

    assertEquals(true, beeUtils.instanceOfFloatingPoint(a));
    assertEquals(true, beeUtils.instanceOfFloatingPoint(b));
    assertEquals(false, beeUtils.instanceOfFloatingPoint(5));
    assertEquals(false, beeUtils.instanceOfFloatingPoint(0));
    assertEquals(true, beeUtils.instanceOfFloatingPoint(0.0));
    assertEquals(true, beeUtils.instanceOfFloatingPoint(0.5E4));
    assertEquals(false, beeUtils.instanceOfFloatingPoint(null));
  }

  @Test
  public void testInstanceOfIntegerType() {
    short b = 2;
    String c = "string";
    Integer a1 = new Integer(1);
    Long d = new Long(22154);

    assertEquals(false, BeeUtils.instanceOfIntegerType(null));
    assertEquals(true, BeeUtils.instanceOfIntegerType(a1));
    assertEquals(true, BeeUtils.instanceOfIntegerType(b));
    assertEquals(false, BeeUtils.instanceOfIntegerType(c));
    assertEquals(true, BeeUtils.instanceOfIntegerType(d));

    int g = 5;
    long e = 54;
    long f = Long.MAX_VALUE;

    assertEquals(true, beeUtils.instanceOfIntegerType(g));
    assertEquals(true, beeUtils.instanceOfIntegerType(e));
    assertEquals(true, beeUtils.instanceOfIntegerType(5));
    assertEquals(true, beeUtils.instanceOfIntegerType(0));
    assertEquals(false, beeUtils.instanceOfIntegerType(0.0));
    assertEquals(false, beeUtils.instanceOfIntegerType(0.5E4));
    assertEquals(false, beeUtils.instanceOfIntegerType(null));
    assertEquals(true, beeUtils.instanceOfIntegerType(f));
  }

  @Test
  public void testInstanceOfStringType() {
    String a = "a";
    StringBuilder builder = new StringBuilder();
    StringBuffer buffer = new StringBuffer();

    assertEquals(false, BeeUtils.instanceOfStringType(null));
    assertEquals(true, BeeUtils.instanceOfStringType(a));
    assertEquals(true, BeeUtils.instanceOfStringType(builder));
    assertEquals(true, BeeUtils.instanceOfStringType(buffer));
  }

  @Test
  public void testIsBoolean() {
    assertEquals(false, beeUtils.isBoolean(-1));
    assertEquals(true, beeUtils.isBoolean(0));
    assertEquals(true, beeUtils.isBoolean(1));
    assertEquals(false, beeUtils.isBoolean(null));
    assertEquals(false, beeUtils.isBoolean("pick"));
    assertEquals(false, beeUtils.isBoolean(""));
    assertEquals(true, beeUtils.isBoolean("true"));
    assertEquals(true, beeUtils.isBoolean("false"));
    assertEquals(true, beeUtils.isBoolean("yEs"));
    assertEquals(true, beeUtils.isBoolean("no"));
  }

  @Test
  public void testIsDigit() {
    assertEquals(true, beeUtils.isDigit('8'));
    assertEquals(false, beeUtils.isDigit('c'));
    assertEquals(false, beeUtils.isDigit('-'));
    assertEquals(false, beeUtils.isDigit(null));
    assertEquals(true, beeUtils.isDigit("123"));
    assertEquals(false, beeUtils.isDigit("12ab"));
    assertEquals(false, beeUtils.isDigit("ab12"));
    assertEquals(false, beeUtils.isDigit(""));
  }

  @Test
  public void testIsDouble() {
    assertEquals(true, beeUtils.isDouble((double) 1));
    assertEquals(true, beeUtils.isDouble((double) 'c'));
    assertEquals(true, beeUtils.isDouble((double) '5'));
    assertEquals(true, beeUtils.isDouble(-5.0));
    assertEquals(false, beeUtils.isDouble(""));
    assertEquals(false, beeUtils.isDouble((Double) null));
    assertEquals(false, beeUtils.isDouble("55e"));
    assertEquals(false, beeUtils.isDouble("e55"));
    assertEquals(true, beeUtils.isDouble("55"));
    assertEquals(false, beeUtils.isDouble(Double.MAX_VALUE * 5));
  }

  @Test
  public void testIsIdentifier() {
    assertEquals(false, NameUtils.isIdentifier(null));
    assertEquals(false, NameUtils.isIdentifier(""));
    assertEquals(true, NameUtils.isIdentifier("aaa5"));
    assertEquals(false, NameUtils.isIdentifier("5a"));
    assertEquals(false, NameUtils.isIdentifier("5a_a"));
    assertEquals(false, NameUtils.isIdentifier("\t\n\r"));
    assertEquals(false, NameUtils.isIdentifier("&&&^%$$&**()"));
  }

  @Test
  public void testIsIndex() {
    int a[] = {1, 2, 3, 4, 5, 6};
    assertEquals(false, beeUtils.isIndex(null, 10));
    assertEquals(false, beeUtils.isIndex("", 10));
    assertEquals(true, beeUtils.isIndex(a, 5));
    assertEquals(false, beeUtils.isIndex(a, 10));
  }

  @Test
  public void testIsInt() {
    assertEquals(false, beeUtils.isInt(-5555555555555.5));
    assertEquals(true, beeUtils.isInt(-5555.5555555555));
    assertEquals(false, beeUtils.isInt(55555555555555.555));
    assertEquals(false, beeUtils.isInt(null));
    assertEquals(false, beeUtils.isInt(""));
    assertEquals(false, beeUtils.isInt("asd"));
    assertEquals(true, beeUtils.isInt("44444"));
    assertEquals(false, beeUtils.isInt("3.141123546"));
  }

  @Test
  public void testIsLong() {
    assertEquals(false, beeUtils.isLong(-555555555555555555555555555555555555555.5));
    assertEquals(true, beeUtils.isLong(-5555.5555555555));
    assertEquals(false, beeUtils.isLong(555555555555555555555555555555555555555555.555));
  }

  @Test
  public void testIsOrdinal() {
    assertEquals(true, beeUtils.isOrdinal(BeeType.class, 5));
    assertEquals(false, beeUtils.isOrdinal(BeeType.class, 500));
    assertEquals(false, beeUtils.isOrdinal(BeeUtils.class, 500));
  }

  @Test
  public void testIsPositive() {
    assertEquals(false, beeUtils.isPositiveInt(""));
    assertEquals(false, beeUtils.isPositiveInt("5"));
    assertEquals(false, beeUtils.isPositiveInt("-5"));
    assertEquals(false, beeUtils.isPositive(-5));
    assertEquals(true, beeUtils.isPositive(5));
    assertEquals(true, beeUtils.isPositive(5.0));
    assertEquals(false, beeUtils.isPositive(0.0));
  }

  @Test
  public void testIsTrue() {
    assertEquals(false, beeUtils.isTrue(null));
    assertEquals(true, beeUtils.isTrue(true));
    assertEquals(false, beeUtils.isTrue(false));
  }

  @Test
  public void testIsZeroDouble() {
    double a = -0.00001;

    assertEquals(false, BeeUtils.isZero(a));
    a = 0.0;
    assertEquals(true, BeeUtils.isZero(a));
  }

  @Test
  public void testLeft() {
    assertEquals("This is a", beeUtils.left("This is a string", 9));
    assertEquals("", beeUtils.left("This is a string", -9));
    assertEquals(null, beeUtils.left(null, 9));
    assertEquals("This is a", beeUtils.left("This is a", 55));
    assertEquals("", beeUtils.left("This is a string", 0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLength() {
    char mas[] = {'h', 'e', 'l', 'l', 'o'};
    int intmas[] = {0, 1, 2, 3};
    String mas3 = "      \n\r";
    CharSequence mas1 = java.nio.CharBuffer.wrap(mas);
    CharSequence mas2 = "hello2";
    CharSequence mas4 = java.nio.CharBuffer.wrap(mas3);
    @SuppressWarnings("rawtypes")
    Vector b = new Vector();
    b.add('a');
    b.add('c');

    TransObject tObj = new TransObject();

    Map<String, Integer> testValue6 = new HashMap<String, Integer>();

    assertEquals(5, beeUtils.length(mas1));
    assertEquals(6, beeUtils.length(mas2));
    assertEquals(8, beeUtils.length(mas4));
    assertEquals(1, beeUtils.length('a'));
    assertEquals(0, beeUtils.length(null));
    assertEquals(5, beeUtils.length(mas));
    assertEquals(6, beeUtils.length("getout"));
    assertEquals(0, beeUtils.length(testValue6));
    assertEquals(4, beeUtils.length(intmas));
    assertEquals(0, beeUtils.length(1));
    assertEquals(2, beeUtils.length(b));
    assertEquals(5, beeUtils.length(tObj));
  }

  @Test
  public final void testNormalize() {
    assertEquals("", BeeUtils.normalize(null));
    assertEquals("abracadabra", BeeUtils.normalize("abracadabra"));
    assertEquals("abra cadabra", BeeUtils.normalize("AbrA cAdAbrA"));
  }

  @Test
  public void testNormSep() {
    char mas[] = {'h', 'e', 'l', 'l', 'o'};
    CharSequence mas1 = java.nio.CharBuffer.wrap(mas);
    CharSequence mas2 = "hello2";
    assertEquals(BeeConst.DEFAULT_LIST_SEPARATOR, BeeUtils.normSep(""));
    assertEquals("l", BeeUtils.normSep("l"));
    assertEquals("lie", BeeUtils.normSep("lie"));
    assertEquals(BeeConst.STRING_SPACE, BeeUtils.normSep(1));
    assertEquals(BeeConst.STRING_EMPTY, BeeUtils.normSep(-1));
    assertEquals("     ", BeeUtils.normSep(5));
    assertEquals("k", BeeUtils.normSep('k'));
    assertEquals(BeeConst.DEFAULT_LIST_SEPARATOR, BeeUtils.normSep(mas));
    assertEquals(BeeConst.DEFAULT_LIST_SEPARATOR, BeeUtils.normSep(null));
    assertEquals("hello", BeeUtils.normSep(mas1));
    assertEquals("hello2", BeeUtils.normSep(mas2));
  }

  @Test
  public void testNormSep2Par() {
    assertEquals("     ", beeUtils.normSep(5, ";"));
    assertEquals("win", beeUtils.normSep("win", ";"));
    assertEquals("c", beeUtils.normSep('c', ";"));
    assertEquals(";", beeUtils.normSep(null, ";"));
  }

  @Test
  public void testNvl() {
    assertEquals("string", beeUtils.nvl(null, "string"));
    assertEquals(null, beeUtils.nvl(null, null));
    assertEquals("stringas", beeUtils.nvl("stringas", null));
  }

  @Test
  public void testPadLeft() {
    assertEquals("     This is a string", beeUtils.padLeft("This is a string", 21, ' '));
    assertEquals("     ", beeUtils.padLeft("", 5, ' '));
    assertEquals(null, beeUtils.padLeft(null, 5, ' '));
    assertEquals("", beeUtils.padLeft("This is a string", 0, ' '));
    assertEquals("This is a string", beeUtils.padLeft("This is a string", 5, ' '));
  }

  @Test
  public void testParenthesize() {
    assertEquals("(This is a string)", beeUtils.parenthesize("This is a string"));
    assertEquals("(5)", beeUtils.parenthesize(5));
    assertEquals("", beeUtils.parenthesize((Object) null));
  }

  @Test
  public void testProgress() {
    assertEquals("5/10", beeUtils.progress(5, 10));
    assertEquals("-5/10", beeUtils.progress(-5, 10));
  }

  @Test
  public void testProper() {
    assertEquals("", beeUtils.proper(null, ";"));
    assertEquals("", beeUtils.proper("  ", ";"));
    assertEquals("S", beeUtils.proper(" s ", null));
    assertEquals("Ssssss", beeUtils.proper(" ssssss ", null));
    assertEquals("Ssssss Ssss Aaaa Bbbb", beeUtils.proper(" ssssss.ssss.aaaa.bbbb ", "."));
  }

  @Test
  public void testRandomInt() {
    for (int i = 0; i < 20; i++) {
      assertEquals(true, beeUtils.randomInt(5, 10) <= 5 + 5);
      assertEquals(true, beeUtils.randomInt(5, 10) >= 5);
    }
  }

  @Test
  public void testRandomString() {
    for (int i = 0; i < 20; i++) {
      assertEquals(true, beeUtils.randomString(5, 10, 'a', 'c').compareTo("cccccccccc") <= 1);
      assertEquals(true, beeUtils.randomString(5, 10, 'a', 'c').compareTo("aaaaa") >= -1);
      assertEquals(true, beeUtils.randomString(5, 5, 'a', 'c').length() == 5);
      assertEquals("aaaaa", beeUtils.randomString(5, 5, 'a', 'a'));
    }
  }

  @Test
  public void testRandomStringIntChar() {
    for (int i = 0; i < 20; i++) {
      assertEquals(true, beeUtils.randomString(5, "ab").compareTo("bbbbb") <= 1);
      assertEquals(true, beeUtils.randomString(5, "ab").compareTo("aaaa") >= -1);
      assertEquals(true, beeUtils.randomString(5, "a").compareTo("aaaaa") == 0);
    }
  }

  @Test
  public final void testRemoveTrailingZeros() {
    assertEquals(null, BeeUtils.removeTrailingZeros(null));
    assertEquals("AA", BeeUtils.removeTrailingZeros("AA"));
    assertEquals("00.000.AA", BeeUtils.removeTrailingZeros("00.000.AA"));
    assertEquals(".00.000", BeeUtils.removeTrailingZeros(".00.000"));
    assertEquals("AA.AAA.000", BeeUtils.removeTrailingZeros("AA.AAA.000"));
    assertEquals("00", BeeUtils.removeTrailingZeros("00.00"));
    assertEquals("00", BeeUtils.removeTrailingZeros("00.0000000000000"));
    assertEquals("XX", BeeUtils.removeTrailingZeros("XX.0000000000000"));
  }

  @Test
  public void testRemoveValue() {
    Map<String, Integer> a = new HashMap<String, Integer>();
    Map<String, Integer> b = new HashMap<String, Integer>();
    Map<String, Integer> c = new HashMap<String, Integer>();
    a.put("1", 55);
    a.put("2", 99);
    a.put("3", 11);
    b.put("1", 55);
    b.put("3", 11);
    b.put("5", 55);
    b.put("2", 99);
    b.put("4", 11);

    assertEquals(1, beeUtils.removeValue(a, 99));
    assertEquals(2, beeUtils.removeValue(b, 55));
    assertEquals(0, beeUtils.removeValue(c, 55));
    assertEquals(0, beeUtils.removeValue(null, null));
  }

  @Test
  public void testReplace() {
    assertEquals("text", beeUtils.replace("test", 2, 3, 'x'));
    assertEquals("texst", beeUtils.replace("test", 2, 3, "xs"));
    assertEquals("text", beeUtils.replace("test", "s", "x"));
    assertEquals("test", beeUtils.replace("test", "a", "x"));

    assertEquals("text text", beeUtils.replace("test test", "s", "x", 2));
    assertEquals("text test", beeUtils.replace("test test", "s", "x", 1));

    assertEquals(null, beeUtils.replace(null, "a", "x", 2));
    assertEquals("test", beeUtils.replace("test", null, "x", 2));
    assertEquals("test", beeUtils.replace("test", "s", null, 2));
    assertEquals("test", beeUtils.replace("test", "s", "x", 0));
    assertEquals(null, beeUtils.replace(null, null, null, 0));
  }

  @Test
  public void testReplicate() {
    assertEquals("t", BeeUtils.replicate('t', 1));
    assertEquals("ttttt", BeeUtils.replicate('t', 5));
  }

  @Test
  public void testRound() {
    assertEquals(3.14, beeUtils.round(3.1412, 2));
    assertEquals(0.0, beeUtils.round(Double.POSITIVE_INFINITY, 2));
    assertEquals(3.1, beeUtils.round(3.1412, 1));
    assertEquals(3.5, beeUtils.round(3.499, 1));
    assertEquals(3.0, beeUtils.round(3, 5));
    assertEquals(9.223372036854776E18, beeUtils.round(Long.MAX_VALUE, 5));
    assertEquals(-9.223372036854776E18, beeUtils.round(Long.MIN_VALUE, 5));
    assertEquals(0.0, beeUtils.round(Long.valueOf(0), 5));
    assertEquals(0.0, beeUtils.round(0.0, 5));
  }

  @Test
  public void testSame() {
    assertEquals(true, beeUtils.same("pick", " PicK "));
    assertEquals(false, beeUtils.same(null, " PicK "));
    assertEquals(false, beeUtils.same("pick", null));
    assertEquals(true, beeUtils.same("", null));
    assertEquals(true, beeUtils.same(null, null));
  }

  @Test
  public void testSpace() {
    assertEquals(BeeConst.STRING_SPACE, BeeUtils.space(1));
    assertEquals(BeeConst.STRING_EMPTY, BeeUtils.space(-1));
    assertEquals("     ", BeeUtils.space(5));
  }

  @Test
  public void testSplit() {
    String a[] = {"string epic", "epic", "string", ";"};

    assertEquals(a[0], beeUtils.split("string epic", ";;;;;")[0]);
    assertEquals(a[2], beeUtils.split("string;epic", ";")[0]);
    assertEquals(a[1], beeUtils.split("string;epic", ";")[1]);
    assertEquals(a[2], beeUtils.split("string", ";")[0]);
    assertEquals(null, beeUtils.split(null, ";;;;;"));
    assertEquals(BeeConst.EMPTY_STRING_ARRAY, beeUtils.split("", ";;;;;"));
  }

  @Test
  public void testStartsSame() {
    assertEquals(true, beeUtils.startsSame("string", "string and another one"));
    assertEquals(true, beeUtils.startsSame("str", "string and"));
    assertEquals(false, beeUtils.startsSame(null, "string and"));
    assertEquals(false, beeUtils.startsSame("string", null));
    assertEquals(false, beeUtils.startsSame("", ""));
  }

  @Test
  public void testToBoolean() {
    assertEquals(true, beeUtils.toBoolean(1));
    assertEquals(false, beeUtils.toBoolean(0));
    assertEquals(false, beeUtils.toBoolean(-5));
    assertEquals(false, beeUtils.toBoolean("false"));
    assertEquals(false, beeUtils.toBoolean("no"));
    assertEquals(true, beeUtils.toBoolean("yes"));
    assertEquals(true, beeUtils.toBoolean("true"));
    assertEquals(false, beeUtils.toBoolean("hjjj"));
    assertEquals(false, beeUtils.toBoolean(null));
  }

  @Test
  public final void testToBooleanOrNull() {
    assertEquals(null, BeeUtils.toBooleanOrNull(null));
    assertEquals(Boolean.FALSE, BeeUtils.toBooleanOrNull("false"));
    assertEquals(Boolean.FALSE, BeeUtils.toBooleanOrNull("asdasd"));
    assertEquals(Boolean.TRUE, BeeUtils.toBooleanOrNull("1"));
  }

  @Test
  public void testToDouble() {
    assertEquals(0.0, beeUtils.toDouble("0.0"));
    assertEquals(-0.569, beeUtils.toDouble("     -0.569  \r"));
    assertEquals(3.145, beeUtils.toDouble("3.145"));
    assertEquals(0.0, beeUtils.toDouble("     -0.5/0 infinity69  \r"));
    assertEquals(0.0, beeUtils.toDouble("     "));
    assertEquals(0.0, beeUtils.toDouble(null));
  }

  @Test
  public final void testToDoubleOrNull() {
    assertEquals(null, BeeUtils.toDoubleOrNull(null));
    assertEquals(0.0, BeeUtils.toDoubleOrNull("asd"));
    assertEquals(15.0, BeeUtils.toDoubleOrNull("15.0"));
  }

  @Test
  public void testToFloat() {
    assertEquals((float) 0.0, beeUtils.toFloat("0.0"));
    assertEquals((float) -0.569, beeUtils.toFloat("     -0.569  \r"));
    assertEquals((float) 3.145, beeUtils.toFloat("3.145"));
    assertEquals((float) 0.0, beeUtils.toFloat("     -0.5/0 infinity69  \r"));
    assertEquals((float) 0.0, beeUtils.toFloat("     "));
    assertEquals((float) 0.0, beeUtils.toFloat(null));
  }

  @Test
  public void testToInt() {
    assertEquals(1, beeUtils.toInt(true));
    assertEquals(0, beeUtils.toInt(false));
  }

  @Test
  public final void testToIntOrNull() {
    assertEquals(null, BeeUtils.toIntOrNull(null));
    assertEquals(Integer.valueOf(0), BeeUtils.toIntOrNull("asd"));
    assertEquals(Integer.valueOf(15), BeeUtils.toIntOrNull("15.0"));
    assertEquals(null, BeeUtils.toIntOrNull(""));
  }

  @Test
  public void testToLeadingZeroes() {
    assertEquals("0010", beeUtils.toLeadingZeroes(10, 4));
    assertEquals("10", beeUtils.toLeadingZeroes(10, 2));
    assertEquals("-10", beeUtils.toLeadingZeroes(-10, 15));
  }

  @Test
  public void testToLong() {
    assertEquals(0, beeUtils.toLong("0.5"));
    assertEquals(0, beeUtils.toLong("0"));
    assertEquals(-1, beeUtils.toLong("     -1  \r"));
    assertEquals(3, beeUtils.toLong("3"));
    assertEquals(0, beeUtils.toLong("     -0.5/0 infinity69  \r"));
    assertEquals(0, beeUtils.toLong("     "));
  }

  @Test
  public final void testToLongOrNull() {
    assertEquals(null, BeeUtils.toLongOrNull(null));
    assertEquals(Long.valueOf(0), BeeUtils.toLongOrNull("asd"));
    assertEquals(Long.valueOf(15), BeeUtils.toLongOrNull("15.0"));
    assertEquals(null, BeeUtils.toLongOrNull(""));
  }

  @Test
  public final void testToNonNegativeIntDouble() {
    assertEquals(0, BeeUtils.toNonNegativeInt((Double) null));
    assertEquals(5, BeeUtils.toNonNegativeInt(Double.valueOf("5.0")));
    assertEquals(0, BeeUtils.toNonNegativeInt(Double.valueOf("-15.0")));
    assertEquals(0, BeeUtils.toNonNegativeInt(Double.valueOf("-99.0")));
  }

  @Test
  public final void testToNonNegativeIntInteger() {
    assertEquals(0, BeeUtils.toNonNegativeInt((Integer) null));
    assertEquals(5, BeeUtils.toNonNegativeInt(Integer.valueOf("5")));
    assertEquals(0, BeeUtils.toNonNegativeInt(Integer.valueOf("-15")));
    assertEquals(0, BeeUtils.toNonNegativeInt(Integer.valueOf("-99")));
  }

  @Test
  public void testToString() {
    assertEquals("true", beeUtils.toString(true));
    assertEquals("false", beeUtils.toString(false));
    assertEquals("1.2", beeUtils.toString(1.2));
    assertEquals("5", beeUtils.toString(5));
    assertEquals("Infinity", beeUtils.toString(Double.POSITIVE_INFINITY));
    assertEquals("9", beeUtils.toString((long) 9));
  }

  @Test
  public void testTransformClass() {
    int a = 0;
    assertEquals(BeeConst.NULL, NameUtils.transformClass(null));
    assertEquals(this.getClass().getName(), NameUtils.transformClass(this));
    assertEquals("java.lang.Integer", NameUtils.transformClass(a));
  }

  @Test
  public void testTransformCollection() {
    List<BeeType> testc1 = new ArrayList<BeeType>();
    List<BeeType> testc2 = new ArrayList<BeeType>();
    List<Object> testc3 = new ArrayList<Object>();
    List<Object> testc4 = new ArrayList<Object>();
    List<Object> testc5 = new ArrayList<Object>();

    testc1.add((BeeType.BOOLEAN));
    testc1.add(BeeType.INT);
    testc1.add(BeeType.CHAR);

    testc2.add(BeeType.STRING);
    testc2.add(BeeType.DOUBLE);

    testc3.add(testc1);
    testc3.add(testc2);
    testc3.add("This");
    testc3.add("is");
    testc3.add("a");
    testc3.add("test");

    testc4.add(testc3);
    testc4.add("end line");

    assertEquals("BLOB_BOOLEAN_INT_CHAR_STRING_DOUBLE_This_is_a_test_end line", BeeUtils
        .transformCollection(testc4, "_"));
    assertEquals("BLOB, BOOLEAN, INT, CHAR, STRING, DOUBLE, This, is, a, test, end line", BeeUtils
        .transformCollection(testc4));
    assertEquals("BLOB, BOOLEAN, INT, CHAR:STRING, DOUBLE:This:is:a:test_end line", BeeUtils
        .transformCollection(testc4, "_", ":", null));
    assertEquals("", BeeUtils.transformCollection(testc5, "_", ":", null));
    assertEquals("", BeeUtils.transformCollection(testc5));
  }

  @Test
  public void testTransformEnumeration() {
    Vector<BeeType> testc1 = new Vector<BeeType>();
    Vector<BeeType> testc2 = new Vector<BeeType>();
    Vector<Object> testc3 = new Vector<Object>();
    Vector<Object> testc4 = new Vector<Object>();
    Vector<Object> testc5 = new Vector<Object>();

    testc1.add((BeeType.BOOLEAN));
    testc1.add(BeeType.INT);
    testc1.add(BeeType.CHAR);

    testc2.add(BeeType.STRING);
    testc2.add(BeeType.DOUBLE);

    testc3.add(testc1);
    testc3.add(testc2);
    testc3.add("This");
    testc3.add("is");
    testc3.add("a");
    testc3.add("test");

    testc4.add(testc3);
    testc4.add("end line");

    assertEquals("BLOB_BOOLEAN_INT_CHAR_STRING_DOUBLE_This_is_a_test_end line", BeeUtils
        .transformEnumeration(testc4.elements(), "_"));
    assertEquals("BLOB, BOOLEAN, INT, CHAR, STRING, DOUBLE, This, is, a, test, end line", BeeUtils
        .transformEnumeration(testc4.elements()));
    assertEquals("BLOB, BOOLEAN, INT, CHAR:STRING, DOUBLE:This:is:a:test_end line", BeeUtils
        .transformEnumeration(testc4.elements(), "_", ":", null));
    assertEquals("", BeeUtils.transformEnumeration(testc5.elements(), "_", ":", null));
    assertEquals("", BeeUtils.transformEnumeration(testc5.elements()));
  }

  @Test
  public void testTransformMap() {
    Map<Object, String> testc1 = new TreeMap<Object, String>();
    Map<Object, String> testc2 = new TreeMap<Object, String>();
    Map<Object, Object> testc3 = new TreeMap<Object, Object>();
    Map<Object, Object> testc4 = new TreeMap<Object, Object>();
    Map<Object, String> testc5 = new TreeMap<Object, String>();

    testc1.put(new Integer(1), "BLOB");
    testc1.put(new Integer(2), "BOOLEAN");
    testc1.put(new Integer(3), "INT");
    testc1.put(new Integer(4), "CHAR");

    testc2.put(new Integer(1), "STRING");
    testc2.put(new Integer(2), "DOUBLE");

    testc3.put(new Integer(1), testc1);
    testc3.put(new Integer(2), testc2);
    testc3.put(new Integer(3), "This");
    testc3.put(new Integer(4), "is");
    testc3.put(new Integer(5), "a");
    testc3.put(new Integer(6), "test");

    testc4.put(new Integer(1), testc3);
    testc4.put(new Integer(2), "end line");

    assertEquals(
        "1=1=1=BLOB_2=BOOLEAN_3=INT_4=CHAR_2=1=STRING_2=DOUBLE_3=This_4=is_5=a_6=test_2=end line",
        BeeUtils.transformMap(testc4, "_"));
    assertEquals(
        "1=1=1=BLOB, 2=BOOLEAN, 3=INT, 4=CHAR, 2=1=STRING, 2=DOUBLE, 3=This, 4=is, 5=a, 6=test, 2=end line",
        BeeUtils.transformMap(testc4));
    assertEquals(
        "1=1=1=BLOB, 2=BOOLEAN, 3=INT, 4=CHAR:2=1=STRING, 2=DOUBLE:3=This:4=is:5=a:6=test_2=end line",
        BeeUtils.transformMap(testc4, "_", ":", null));
    assertEquals("", BeeUtils.transformMap(testc5, "_", ":", null));
    assertEquals("", BeeUtils.transformMap(testc5));
    assertEquals("", BeeUtils.transformMap(null, null, null, null));
  }

  @Test
  public void testTransformNoTrim() {
    assertEquals("  test  ", beeUtils.transformNoTrim("  test  "));
    assertEquals("", beeUtils.transformNoTrim(""));
    assertEquals("", beeUtils.transformNoTrim(null));
    assertEquals("1.2", beeUtils.transformNoTrim(1.2));
  }

  @Test
  public void testTransformObject() {
    log("testTransformObject assert1 ");
    assertEquals("0", BeeUtils.transform(0));
    assertEquals("", BeeUtils.transform(null));

    log(" \t\t[done] \n \r testTransformObject assert2");
    String exp1 = Double.toString(TransObject.DOUBLE_DEFAULT_VALUE);

    log("\t\t [done] \n \r testTransformObject assert3");
    assertEquals(exp1, BeeUtils.transform(obj));
    obj.setDigit(4.8);
    assertEquals("4.8", BeeUtils.transform(obj));

    log("\t\t [done] \n \r testTransformObject assert4");
    assertEquals("helloWorld", BeeUtils.transform("          helloWorld        "));

    log("\t\t [done] \n \r testTransformObject assert5");
    assertEquals("helloWorld", BeeUtils.transform("          helloWorld    \n \r \t    "));
    log("\t\t [done]");

    int a[] = {1, 2, 3};
    assertEquals("1c2c3", BeeUtils.transformGeneric((Object) a, 'c'));
    assertEquals("class com.butent.bee.shared.BeeType",
        BeeUtils.transformGeneric(BeeType.class, ";"));
  }

  @Test
  public void testTransformOptions() {
    try {
      beeUtils.transformOptions((Object) null);
      fail("Exceptions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException: " + e.getMessage());
    }

    try {
      beeUtils.transformOptions("test");
      fail("Exceptions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException: " + e.getMessage());
    }

    assertEquals("jk=;", beeUtils.transformOptions("jk", ";", "kl"));
    assertEquals("jk=;", beeUtils.transformOptions("jk", ";", 3.12, ":", 5));
    assertEquals("jk=10;3.12=5", beeUtils.transformOptions("jk", "10", "3.12", 5));
    assertEquals("", beeUtils.transformOptions(null, null, null, null));
  }

  @Test
  public final void testTrim() {
    assertEquals("", BeeUtils.trim(null));
    assertEquals("", BeeUtils.trim(""));
    assertEquals("12345", BeeUtils.trim("12345"));
    assertEquals("12345", BeeUtils.trim(" 12345   "));
  }

  @Test
  public final void testUnboxBoolInt() {
    assertEquals(false, BeeUtils.unbox((Boolean) null));
    assertEquals(true, BeeUtils.unbox(Boolean.TRUE));
    assertEquals(false, BeeUtils.unbox(Boolean.FALSE));

    assertEquals(0, BeeUtils.unbox((Integer) null));
    assertEquals(5, BeeUtils.unbox(Integer.valueOf(5)));
    assertEquals(-55, BeeUtils.unbox(Integer.valueOf("-55")));
  }

  @Test
  public void testVal() {
    assertEquals(0, beeUtils.val("abcd 10 efg"));
    assertEquals(10, beeUtils.val("10"));
    assertEquals(10, beeUtils.val("10.5"));
    assertEquals(-10, beeUtils.val("   -10.5"));
    assertEquals(0, beeUtils.val("-inf"));
    assertEquals(10, beeUtils.val("     10   "));
    assertEquals(0, beeUtils.val("abcd  rft 10 4 6 kl"));
    assertEquals(0, beeUtils.val(""));
    assertEquals(0, beeUtils.val("         "));
    assertEquals(0, beeUtils.val(null));
  }

  @Test
  public void testZero() {
    assertEquals(null, beeUtils.zero(null));
    assertEquals(0, beeUtils.zero((Object) 15));
    assertEquals((long) 0, beeUtils.zero((Object) 5L));
    assertEquals((short) 0, beeUtils.zero((Object) (short) 3));
    assertEquals((byte) 0, beeUtils.zero((Object) (byte) 5));
    assertEquals(BigInteger.ZERO, beeUtils.zero(BigInteger.valueOf(7)));
    assertEquals(BigDecimal.ZERO, beeUtils.zero(BigDecimal.valueOf(999)));
    assertEquals((float) 0.0, beeUtils.zero((Object) (float) 5.5));
    assertEquals(0.0, beeUtils.zero((Object) 0.5E-3));
    assertEquals(0, beeUtils.zero("tekstas"));
  }
}

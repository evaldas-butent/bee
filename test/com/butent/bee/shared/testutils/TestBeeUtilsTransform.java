package com.butent.bee.shared.testutils;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

  @Override
  @Before
  public void setUp() throws Exception {
    log("Starting test TestBeeUtilsTransform \n \r");
    obj = new TransObject();
  }

  @Override
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testAllEmpty() {
    assertEquals(false, beeUtils.allEmpty("a", null));
    assertEquals(true, beeUtils.allEmpty(" ", null));
  }

  @Test
  public void testAllNotEmpty() {
    assertEquals(false, beeUtils.allNotEmpty("x", null));
    assertEquals(true, beeUtils.allNotEmpty("a", "b"));
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
  public void testFilterContext() {
    List<String> testc1 = new ArrayList<String>();
    List<String> testc2 = new ArrayList<String>();
    testc1.add("A");
    testc1.add("simple text");
    testc1.add("TEXT2");

    List<String> rez1 = new ArrayList<String>();
    rez1.add("simple text");
    rez1.add("TEXT2");

    List<String> rez2 = new ArrayList<String>();
    rez2.add("TEXT2");

    List<String> rez3 = new ArrayList<String>();

    assertEquals(rez1, beeUtils.filterContext(testc1, "text"));
    assertEquals(rez2, beeUtils.filterContext(testc1, "text2"));
    assertEquals(rez3, beeUtils.filterContext(testc1, "texx"));
    assertEquals(rez3, beeUtils.filterContext(testc1, ""));
    assertEquals(testc2, beeUtils.filterContext(testc2, "text"));
  }

  @Test
  public void testFitStart() {
    assertEquals(0, beeUtils.fitStart(0, 5, 10));
    assertEquals(-5, beeUtils.fitStart(0, 15, 10));
    assertEquals(4, beeUtils.fitStart(5, 6, 10));
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
    assertEquals("test", beeUtils.notEmpty("test", "not"));
    assertEquals("not", beeUtils.notEmpty(null, "not"));
    assertEquals("test", beeUtils.notEmpty("test", null));
    assertEquals("not", beeUtils.notEmpty("", "not"));
    assertEquals(null, beeUtils.notEmpty("", null));
    assertEquals("", beeUtils.notEmpty("", ""));
  }

  @Test
  public void testInList() {
    assertEquals(true, beeUtils.inList("text", "this", "is", "a", "text"));
    assertEquals(false, beeUtils.inList("texts", "this", "is", "a", "text"));
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
  public void testIsBoolean() {
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
  public void testIsInt() {
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
  }

  @Test
  public void testIsPositive() {
    assertEquals(false, beeUtils.isPositiveInt(""));
    assertEquals(true, beeUtils.isPositiveInt("5"));
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
  public void testJoin() {
    assertEquals("", BeeUtils.join(""));
    assertEquals("is:a:test", BeeUtils.join(":", "is", "a", "test"));
  }

  @Test
  public void testLeft() {
    assertEquals("This is a", beeUtils.left("This is a string", 9));
    assertEquals("", beeUtils.left("This is a string", -9));
    assertEquals(null, beeUtils.left(null, 9));
    assertEquals("This is a", beeUtils.left("This is a", 55));
    assertEquals("", beeUtils.left("This is a string", 0));
  }

  @Test
  public void testLength() {
    assertEquals(0, beeUtils.length(null));
    assertEquals(6, beeUtils.length("getout"));
  }

  @Test
  public final void testNormalize() {
    assertEquals("", BeeUtils.normalize(null));
    assertEquals("abracadabra", BeeUtils.normalize("abracadabra"));
    assertEquals("abra cadabra", BeeUtils.normalize("AbrA cAdAbrA"));
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
    assertEquals("", beeUtils.proper(null, ';'));
    assertEquals("", beeUtils.proper("  ", ';'));
    assertEquals("S", beeUtils.proper(" s ", null));
    assertEquals("Ssssss", beeUtils.proper(" ssssss ", null));
    assertEquals("Ssssss Ssss Aaaa Bbbb", beeUtils.proper(" ssssss.ssss.aaaa.bbbb ", '.'));
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

    assertEquals(a[0], beeUtils.split("string epic", ';')[0]);
    assertEquals(a[2], beeUtils.split("string;epic", ';')[0]);
    assertEquals(a[1], beeUtils.split("string;epic", ';')[1]);
    assertEquals(a[2], beeUtils.split("string", ';')[0]);
    assertEquals(null, beeUtils.split(null, ';'));
    assertEquals(BeeConst.EMPTY_STRING_ARRAY, beeUtils.split("", ';'));
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
    assertEquals("1c2c3", BeeUtils.transform(a, "c"));
    assertEquals("class com.butent.bee.shared.BeeType",
        BeeUtils.transform(BeeType.class, ";"));
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
}

package com.butent.bee.shared.testutils;

import com.google.gwt.dev.util.collect.HashMap;

import com.butent.bee.shared.utils.BeeUtils;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

/**
 * Tests {@link com.butent.bee.shared.utils.BeeUtils#isEmpty(Object)}.
 */
@SuppressWarnings("rawtypes")
@RunWith(value = Parameterized.class)
public class TestBeeUtilsisEmpty extends TestCase {

  private static Enumeration en;
  private static char mas[] = {'h', 'e', 'l', 'l', 'o'};
  private static CharSequence mas1 = java.nio.CharBuffer.wrap(mas);
  private static CharSequence mas2 = "hello2";
  private static String mas3 = "      \n\r";

  private static CharSequence mas4 = java.nio.CharBuffer.wrap(mas3);
  private static CharSequence testValue1 = "asdasd";
  private static ArrayList testValue10 = new ArrayList();
  private static int[] testValue11 = new int[5];
  private static Error testValue2 = new Error();
  private static String testValue3 = new String();
  private static ArrayList testValue4 = new ArrayList();
  private static ArrayList testValue5 = new ArrayList();
  private static Map<String, Integer> testValue6 = new HashMap<String, Integer>();
  private static Map<String, Integer> testValue7 = new HashMap<String, Integer>();
  private static Vector testValue8 = new Vector();
  private static Vector testValue9 = new Vector();

  @Parameters
  public static Collection<Object[]> getTestParameters() {
    return Arrays.asList(new Object[][]
    {
        {true, 0},
        {false, 1},
        {true, 0.0},
        {false, 0.001},
        {true, ""},
        {false, "     j     "},
        {false, "     1     "},
        {false, "     1   0  "},
        {true, "       "},
        {true, (-0.0)},
        {false, "@aceis"},
        {true, "\n \r \t"},
        {false, testValue1},
        {false, testValue2},
        {true, testValue3},
        {false, true},
        {true, false},
        {true, testValue4},
        {false, testValue5},
        {true, testValue6},
        {false, testValue7},
        {true, testValue8},
        {false, testValue9},
        {true, testValue10},
        {false, testValue11},
        {false, mas1},
        {false, mas2},
        {true, null},
        {true, mas4},
        {true, en}
    });
  }

  private Object expected;

  private Object value;

  public TestBeeUtilsisEmpty(Object expected, Object value) {
    this.expected = expected;
    this.value = value;
  }

  @Override
  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    testValue5.add('c');
    testValue7.put("String", 5);
    testValue9.add("String");
    testValue9.add(testValue11);
    en = testValue9.elements();
  }

  @Override
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testIsEmptyObject() {
    assertEquals(expected, BeeUtils.isEmpty(value));
  }
}

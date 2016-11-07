package com.butent.bee.shared.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;

public class TestBeeUtils {

  @Test
  public void testAddNotEmpty() {
    List<String> list1 = new ArrayList<>();
    assertTrue(BeeUtils.addNotEmpty(list1, "test"));
    assertFalse(BeeUtils.addNotEmpty(list1, ""));
    assertFalse(BeeUtils.addNotEmpty(list1, null));
  }

  @Test
  public void testAddNotNull() {
    List<String> list1 = new ArrayList<>();
    assertTrue(BeeUtils.addNotNull(list1, "test"));
    assertTrue(BeeUtils.addNotNull(list1, ""));
    assertFalse(BeeUtils.addNotNull(list1, null));
  }

  @Test
  public void testAddQuietly() {
    List<String> list1 = new ArrayList<>();
    String test1 = "test1";
    String test2 = "test2";
    String test3 = "test3";
    int index = 1;
    list1.add(test1);
    list1.add(test2);
    int size1 = list1.size();
    BeeUtils.addQuietly(list1, index, test3);
    assertEquals(size1 + 1, list1.size());
    assertTrue(list1.contains(test3));
  }

  @Test
  public void testCheck() {
    Predicate<String> predicate1 = null;
    String test1 = "test1";
    String test2 = "t2";
    assertEquals(true, BeeUtils.check(predicate1, test1));
    Predicate<String> predicate2 = (s) -> s.length() > 3;
    assertEquals(true, BeeUtils.check(predicate2, test1));
    assertEquals(false, BeeUtils.check(predicate2, test2));
  }

  @Test
  public void testCompareNullsFirst() {
    assertEquals(-1, BeeUtils.compareNullsFirst(null, -10));
    assertEquals(0, BeeUtils.compareNullsFirst(null, null));
    assertEquals(1, BeeUtils.compareNullsFirst(-100, null));
    assertEquals(1, BeeUtils.compareNullsFirst(4, 3));
  }

  @Test
  public void testCompareNullsLast() {
    assertEquals(1, BeeUtils.compareNullsLast(null, -10));
    assertEquals(0, BeeUtils.compareNullsLast(null, null));
    assertEquals(-1, BeeUtils.compareNullsLast(-100, null));
    assertEquals(1, BeeUtils.compareNullsLast(4, 3));
  }

  @Test
  public void testCeil() {
    assertEquals(0, BeeUtils.ceil(0.0));
    assertEquals(-1, BeeUtils.ceil(-1.1));
    assertEquals(-1, BeeUtils.ceil(-1.9));
    assertEquals(3, BeeUtils.ceil(2.9));
    assertEquals(3, BeeUtils.ceil(2.1));
  }

  @Test
  public void testIsBetween() {
    assertTrue(BeeUtils.isBetween(30.0, 30.0, true, 40.0, true));
    assertTrue(BeeUtils.isBetween(40.0, 30.0, true, 40.0, true));
    assertFalse(BeeUtils.isBetween(30.0, 30.1, true, 40.0, true));
    assertFalse(BeeUtils.isBetween(40.0, 30.0, true, 39.9, true));
    assertTrue(BeeUtils.isBetween(0.0, -30.0, true, 40.0, true));
    assertFalse(BeeUtils.isBetween(null, 30.0, true, 40.0, true));
    assertTrue(BeeUtils.isBetween(1.0, null, true, 40.0, true));
    assertTrue(BeeUtils.isBetween(30.1, 30.0, true, null, true));
    assertFalse(BeeUtils.isBetween(Double.NaN, 30.0, true, 40.0, true));
    assertTrue(BeeUtils.isBetween(0.0, Double.NaN, true, 40.0, true));
    assertTrue(BeeUtils.isBetween(10.0, 0.0, true, Double.NaN, true));
    assertTrue(BeeUtils.isBetween(10.0, Double.NEGATIVE_INFINITY, true, Double.POSITIVE_INFINITY,
        true));
    assertTrue(BeeUtils.isBetween(10.0, Double.NEGATIVE_INFINITY, true, Double.NaN, true));
  }

  @Test
  public void testIsDouble() {
    assertTrue(BeeUtils.isDouble("0.0", Double.NEGATIVE_INFINITY, false, 30.0, true));
    assertFalse(BeeUtils.isDouble("", Double.NEGATIVE_INFINITY, false, 30.0, true));
    String test1 = null;
    assertFalse(BeeUtils.isDouble(test1, Double.NEGATIVE_INFINITY, false, 30.0, true));
    assertFalse(BeeUtils.isDouble("test", Double.NEGATIVE_INFINITY, false, 30.0, true));
  }

  @Test
  public void testIsEmptyString() {
    String test = "test";
    String test2 = null;
    String test3 = "";
    assertFalse(BeeUtils.isEmpty(test));
    assertTrue(BeeUtils.isEmpty(test2));
    assertTrue(BeeUtils.isEmpty(test3));
  }

  @Test
  public void testIsEmptyMap() {
    Map<String, Integer> map1 = new HashMap<>();
    assertTrue(BeeUtils.isEmpty(map1));
    Map<String, Integer> map2 = new HashMap<>();
    map2.put("test", 1);
    assertFalse(BeeUtils.isEmpty(map2));
    Map<String, Integer> map3 = null;
    assertTrue(BeeUtils.isEmpty(map3));
  }

  @Test
  public void testIsEmptyCollection() {
    List<String> list1 = new ArrayList<>();
    list1.add("test");
    assertFalse(BeeUtils.isEmpty(list1));
    List<String> list2 = new ArrayList<>();
    assertTrue(BeeUtils.isEmpty(list2));
    Queue<Integer> queue1 = new LinkedList<>();
    assertTrue(BeeUtils.isEmpty(queue1));
    Queue<Integer> queue2 = new LinkedList<>();
    queue2.offer(5);
    assertFalse(BeeUtils.isEmpty(queue2));
  }

  @Test
  public void testDoubleToString() {
    assertEquals("NaN", BeeUtils.toString(Double.NaN));
    assertEquals("Infinity", BeeUtils.toString(Double.POSITIVE_INFINITY));
    assertEquals("-Infinity", BeeUtils.toString(Double.NEGATIVE_INFINITY));

    assertEquals("0", BeeUtils.toString(0.0));
    assertEquals("0", BeeUtils.toString(-0.0));

    assertEquals("1.2E15", BeeUtils.toString(1.2e15));
    assertEquals("-1.1E15", BeeUtils.toString(-1.1e15));

    assertEquals("1000000000000000", BeeUtils.toString(1e15));
    assertEquals("-1000000000000000", BeeUtils.toString(-1e15));

    assertEquals("0", BeeUtils.toString(0.9e-15));
    assertEquals("0", BeeUtils.toString(-0.9e-15));

    assertEquals("0.000000000000001", BeeUtils.toString(1e-15));
    assertEquals("-0.000000000000001", BeeUtils.toString(-1e-15));

    assertEquals("123456789012345", BeeUtils.toString(123456789012345.0));
    assertEquals("-123456789012345", BeeUtils.toString(-123456789012345.0));

    assertEquals("0.3", BeeUtils.toString(0.1 + 0.2));
    assertEquals("3.14159265358979", BeeUtils.toString(Math.PI));
  }
}

package com.butent.bee.shared.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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

}

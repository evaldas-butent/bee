package com.butent.bee.shared.utils;

import com.google.common.collect.Range;

import com.butent.bee.shared.Assert;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestIntRangeSet {

  @Test
  public void testAdd() {
    IntRangeSet rs = new IntRangeSet();

    rs.add(Range.open(1, 4));
    rs.add(Range.openClosed(5, 8));
    rs.add(Range.closedOpen(10, 13));
    rs.add(Range.closed(15, 16));
    check(rs, 2, 3, 6, 7, 8, 10, 11, 12, 15, 16);

    rs.clear();
    rs.add(Range.closed(5, 7));
    rs.add(Range.closed(10, 12));
    rs.add(Range.closed(4, 6));
    rs.add(Range.closed(9, 13));
    assertEquals(rs.size(), 2);
    check(rs, 4, 5, 6, 7, 9, 10, 11, 12, 13);

    rs.add(Range.closed(8, 8));
    assertEquals(rs.size(), 1);

    rs.clear();
    rs.add(null);
    rs.add(Range.open(1, 2));
    assertTrue(rs.isEmpty());
  }

  @Test
  public void testAddClosed() {
    IntRangeSet rs = new IntRangeSet();

    rs.addClosed(-20, -5);
    rs.addClosed(5, 20);
    assertEquals(rs.size(), 2);
    rs.addClosed(-4, 4);
    assertEquals(rs.size(), 1);
    assertEquals(Range.closed(-20, 20), rs.asList().get(0));
  }

  @Test
  public void testAddClosedOpen() {
    IntRangeSet rs = new IntRangeSet();

    rs.addClosedOpen(5, 10);
    rs.addClosedOpen(15, 20);
    assertEquals(rs.size(), 2);
    rs.addClosedOpen(10, 15);
    assertEquals(rs.size(), 1);
    assertEquals(Range.closed(5, 19), rs.asList().get(0));
  }

  @Test
  public void testAsList() {
    IntRangeSet rs = new IntRangeSet();

    rs.addClosed(10, 12);
    rs.addClosed(1, 3);
    assertEquals(Range.closed(1, 3), rs.asList().get(0));
    assertEquals(Range.closed(10, 12), rs.asList().get(1));

    rs.addClosed(1, 12);
    assertEquals(rs.size(), 1);
    assertEquals(Range.closed(1, 12), rs.asList().get(0));
  }

  @Test
  public void testComplement() {
    IntRangeSet rs = new IntRangeSet();

    rs.addClosed(5, 8);
    rs.addClosed(1, 3);
    rs.addClosed(10, 12);

    check(rs.complement(0, 10), 0, 4, 9);
    check(rs.complement(0, 9), 0, 4, 9);
    check(rs.complement(0, 14), 0, 4, 9, 13, 14);
    check(rs.complement(1, 8), 4);
    Assert.isTrue(rs.complement(5, 8).isEmpty());
  }

  @Test
  public void testRemove() {
    IntRangeSet rs = new IntRangeSet();

    rs.addClosed(0, 3);
    rs.addClosed(5, 9);
    rs.remove(Range.open(6, 8));
    assertEquals(rs.size(), 3);
    check(rs, 0, 1, 2, 3, 5, 6, 8, 9);

    rs.remove(Range.openClosed(2, 5));
    rs.remove(Range.closed(10, 10));
    rs.remove(Range.closed(-10, -5));
    assertEquals(rs.size(), 3);
    check(rs, 0, 1, 2, 6, 8, 9);

    rs.remove(Range.closed(2, 9));
    assertEquals(rs.size(), 1);
    check(rs, 0, 1);

    rs.remove(Range.closed(0, 1));
    assertTrue(rs.isEmpty());
  }

  @Test
  public void testRemoveClosed() {
    IntRangeSet rs = new IntRangeSet();

    rs.addClosed(0, 3);
    rs.addClosed(5, 9);
    rs.addClosed(12, 15);
    rs.removeClosed(2, 14);
    assertEquals(rs.size(), 2);
    check(rs, 0, 1, 15);
  }

  @Test
  public void testRemoveClosedOpen() {
    IntRangeSet rs = new IntRangeSet();

    rs.addClosed(0, 3);
    rs.addClosed(5, 9);
    rs.addClosed(12, 15);
    rs.removeClosedOpen(2, 14);
    assertEquals(rs.size(), 2);
    check(rs, 0, 1, 14, 15);
  }

  private static void check(IntRangeSet rangeSet, int... values) {
    assertArrayEquals(values, getValues(rangeSet));
  }

  private static int[] getValues(IntRangeSet rangeSet) {
    List<Integer> values = new ArrayList<>();

    for (Range<Integer> r : rangeSet.asList()) {
      for (int i = r.lowerEndpoint(); i <= r.upperEndpoint(); i++) {
        values.add(i);
      }
    }

    int[] arr = new int[values.size()];
    for (int i = 0; i < values.size(); i++) {
      arr[i] = values.get(i);
    }

    return arr;
  }
}

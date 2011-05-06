package com.butent.bee.shared.testutils;

import com.butent.bee.shared.ListSequence;
import com.butent.bee.shared.exceptions.BeeRuntimeException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Tests {@link com.butent.bee.shared.ListSequence}.
 */
public class TestListSequence {

  private ListSequence<String> ls;
  private ListSequence<Object> ls1;

  @Before
  public void setUp() throws Exception {
    ArrayList<String> ar = new ArrayList<String>();
    ar.add("this");
    ar.add("the");
    ar.add("junit");
    ar.add("test");

    ls = new ListSequence<String>(ar);
    ls1 = new ListSequence<Object>(5);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testClear() {
    ls.clear();
    ls1.clear();

    assertEquals(0, ls.getLength());
    assertEquals(0, ls1.getLength());
  }

  @Test
  public final void testGet() {
    assertEquals("junit", ls.get(2));
    assertEquals(null, ls1.get(2));

    assertEquals("this", ls.get(0));
    assertEquals(null, ls1.get(0));

    assertEquals("test", ls.get(3));
    assertEquals(null, ls1.get(4));

    try {
      ls.get(-1);
      fail("Exceptions not work: " + ls.get(-1));
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need  BeeRuntimeException: " + e.getMessage());
    }

    try {
      ls.get(5);
      fail("Exceptions not work: " + ls.get(-1));
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need  BeeRuntimeException: " + e.getMessage());
    }

    try {
      ls1.get(-1);
      fail("Exceptions not work: " + ls.get(-1));
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need  BeeRuntimeException: " + e.getMessage());
    }

    try {
      ls1.get(5);
      fail("Exceptions not work: " + ls.get(-1));
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need  BeeRuntimeException: " + e.getMessage());
    }
  }

  @Test
  public final void testGetArray() {
    String[] ex1 = {"this", "the", "junit", "test"};
    Object[] ex2 = {null, null, null, null, null};
    String[] pr2 = {};
    Object[] pr3 = {};
    Object[] re1 = ls.getArray(pr2).getA();
    Object[] re2 = ls1.getArray(pr3).getA();

    String[] pr1 = {"one", "two", "three", "four"};

    assertArrayEquals(ex1, re1);
    assertArrayEquals(ex2, re2);
    assertArrayEquals(ex2, ls1.getArray(pr1).getA());
  }

  @Test
  public final void testGetList() {
    ArrayList<String> ex1 = new ArrayList<String>();
    ArrayList<Object> ex2 = new ArrayList<Object>();

    ex1.add("this");
    ex1.add("the");
    ex1.add("junit");
    ex1.add("test");

    ex2.add(null);
    ex2.add(null);
    ex2.add(null);
    ex2.add(null);
    ex2.add(null);

    assertArrayEquals(ex1.toArray(), ls.getList().toArray());
    assertArrayEquals(ex2.toArray(), ls1.getList().toArray());
  }

  @Test
  public final void testInsert() {
    try {
      ls.insert(5, ".");
      fail("Exceptions not woks " + ls.getLength());
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException: " + e.getMessage());
    }

    try {
      ls.insert(-1, ".");
      fail("Exceptions not woks");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException: " + e.getMessage());
    }

    try {
      ls1.insert(-1, 0);
      fail("Exceptions not woks");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException: " + e.getMessage());
    }

    try {
      ls1.insert(6, "5");
      fail("Exceptions not woks ");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException: " + e.getMessage());
    }

    String[] exp1a = {"this", "is", "the", "junit", "test"};
    String[] exp1b = {".", "this", "is", "the", "junit", "test"};
    String[] exp1c = {".", "this", "is", "the", "junit", "test", "!"};
    String[] pr = {};
    Object[] pr1 = {};
    Object[] exp2a = {null, 10, null, null, null, null};
    Object[] exp2b = {"the", null, 10, null, null, null, null};
    Object[] exp2c = {"the", null, 10, null, null, null, null, 1L};

    ls.insert(1, "is");
    assertArrayEquals(exp1a, ls.getArray(pr).getA());

    ls.insert(0, ".");
    assertArrayEquals(exp1b, ls.getArray(pr).getA());

    ls.insert(6, "!");
    assertArrayEquals(exp1c, ls.getArray(pr).getA());

    ls1.insert(1, 10);
    assertArrayEquals(exp2a, ls1.getArray(pr1).getA());

    ls1.insert(0, "the");
    assertArrayEquals(exp2b, ls1.getArray(pr1).getA());

    ls1.insert(7, 1L);
    assertArrayEquals(exp2c, ls1.getArray(pr1).getA());
  }

  @Test
  public final void testLength() {
    assertEquals(4, ls.getLength());
    assertEquals(5, ls1.getLength());
  }

  @SuppressWarnings("unused")
  @Test
  public final void testListSequence() {
    try {
      ListSequence<Object> a = new ListSequence<Object>(null);
      fail("Exceptions not work");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException" + e.getMessage());
    }

    try {
      ListSequence<Object> a = new ListSequence<Object>(-1);
      fail("Exceptions not work");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException" + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @Test
  public final void testRemove() {

    String[] exp1a = {"this", "junit", "test"};
    String[] exp1b = {"junit", "test"};
    String[] exp1c = {"junit"};

    String[] exp2a = {null, null, null, null, null};
    String[] exp2b = {"junit", "test"};
    String[] exp2c = {"junit"};
    String[] pr = {};

    ls.remove(1);
    assertArrayEquals(exp1a, ls.getArray(pr).getA());

    ls.remove(0);
    assertArrayEquals(exp1b, ls.getArray(pr).getA());

    ls.remove(1);
    assertArrayEquals(exp1c, ls.getArray(pr).getA());

    try {
      ls.remove(-1);
      fail("Exceptions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need bee runtime exception" + e.getMessage());
    }

    try {
      ls.remove(10);
      fail("Exceptions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need bee runtime exception" + e.getMessage());
    }

    try {
      ls1.remove(-1);
      fail("Exceptions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need bee runtime exception" + e.getMessage());
    }

    try {
      ls1.remove(10);
      fail("Exceptions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need bee runtime exception" + e.getMessage());
    }
  }

  @Test
  public final void testSet() {

    String[] exp1a = {"this", "the", "junit", "test"};
    String[] exp1b = {"there", "the", "junit", "test"};
    String[] exp1c = {"there", "the", "junit", "mod"};
    String[] pr = {};
    Object[] pr1 = {};

    Object[] exp2a = {null, null, 5.3, null, null};
    Object[] exp2b = {"a", null, 5.3, null, null};
    Object[] exp2c = {"a", null, 5.3, null, ""};

    ls.set(1, "the");
    assertArrayEquals(exp1a, ls.getArray(pr).getA());

    ls.set(0, "there");
    assertArrayEquals(exp1b, ls.getArray(pr).getA());

    ls.set(3, "mod");
    assertArrayEquals(exp1c, ls.getArray(pr).getA());

    ls1.set(2, 5.3);
    assertArrayEquals(exp2a, ls1.getArray(pr1).getA());

    ls1.set(0, "a");
    assertArrayEquals(exp2b, ls1.getArray(pr1).getA());

    ls1.set(4, "");
    assertArrayEquals(exp2c, ls1.getArray(pr1).getA());

    try {
      ls.set(-1, ".");
      fail("Exceptiions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException: " + e.getMessage());
    }

    try {
      ls.set(10, ".");
      fail("Exceptiions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException: " + e.getMessage());
    }

    try {
      ls1.set(-1, ".");
      fail("Exceptiions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeExceptio: " + e.getMessage());
    }

    try {
      ls1.set(10, ".");
      fail("Exceptiions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException: " + e.getMessage());
    }
  }

  @Test
  public final void testSetValuesListOfT() {
    ArrayList<String> exp1 = new ArrayList<String>();
    ArrayList<Object> exp2 = new ArrayList<Object>();
    String[] pr = {};
    Object[] pr1 = {};
    exp1.add("one");
    exp1.add("two");
    exp1.add("tree");

    exp2.add(1);
    exp2.add(3.14);
    exp2.add("");

    ls.setValues(exp1);
    ls1.setValues(exp2);

    assertArrayEquals(exp1.toArray(), ls.getArray(pr).getA());
    assertArrayEquals(exp2.toArray(), ls1.getArray(pr1).getA());
  }

  @Test
  public final void testSetValuesTArray() {
    String[] exp1 = {"abc", "cde", "qwe"};
    Object[] exp2 = {"", null, 1, 2, 3.14, 2L};
    String[] pr = {};
    Object[] pr1 = {};

    ls.setValues(exp1);
    ls1.setValues(exp2);

    assertArrayEquals(exp1, ls.getArray(pr).getA());
    assertArrayEquals(exp2, ls1.getArray(pr1).getA());

    exp1[0] = "123";
    assertArrayEquals(exp1, ls.getArray(pr).getA());
  }
}

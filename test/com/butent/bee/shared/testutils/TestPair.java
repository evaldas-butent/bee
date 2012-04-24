package com.butent.bee.shared.testutils;

import com.butent.bee.shared.Pair;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link com.butent.bee.shared.Pair}.
 */
public class TestPair {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  public final void testGetA() {
    Pair p = Pair.create("a", 1.2);
    assertEquals("a", p.getA());

    p = Pair.create(null, 5);
    assertEquals(null, p.getA());

    p = Pair.create('\0', 7);
    assertEquals('\0', p.getA());

    Pair<String, Double> g = Pair.create("a", 1.2);
    assertEquals("a", g.getA());

    g = Pair.create(null, (double) 5);
    assertEquals(null, g.getA());

    g = Pair.create(String.valueOf('\0'), (double) 7);
    assertEquals("\0", g.getA());
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  public final void testGetB() {
    Pair p = Pair.create(1.2, "a");
    assertEquals("a", p.getB());

    p = Pair.create(5, null);
    assertEquals(null, p.getB());

    p = Pair.create(7, '\0');
    assertEquals('\0', p.getB());
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  public final void testToString() {
    Pair p = Pair.create(1.2, "a");
    assertEquals("1.2, a", p.toString());

    p = Pair.create(5, null);
    assertEquals("5", p.toString());

    p = Pair.create(7, '\0');
    assertEquals("7, \0", p.toString());
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  public final void testTransform() {
    Pair p = Pair.create("a", 1.2);
    assertEquals("a, 1.2", p.transform());

    p = Pair.create(null, 5);
    assertEquals("5", p.transform());

    p = Pair.create('\0', 7);
    assertEquals("\0, 7", p.transform());
  }

}

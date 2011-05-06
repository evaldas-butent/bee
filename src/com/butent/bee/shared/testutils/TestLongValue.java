package com.butent.bee.shared.testutils;

import com.butent.bee.shared.LongValue;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link com.butent.bee.shared.LongValue}.
 */
public class TestLongValue {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testGetInt() {
    LongValue lv = new LongValue(1298362388446L);
    assertEquals(1282265054, lv.getInt());

    lv.setValue(10L);
    assertEquals(10, lv.getInt());

    lv.setValue(-999);
    assertEquals(-999, lv.getInt());
  }

  @Test
  public final void testLongValue() {
    LongValue lv = new LongValue(5L);

    assertEquals(5, lv.getLong());
    lv.setValue(1298362388446L);

    assertEquals(1298362388446L, lv.getLong());
  }

}

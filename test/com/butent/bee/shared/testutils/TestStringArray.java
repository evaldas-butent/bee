package com.butent.bee.shared.testutils;

import com.butent.bee.shared.StringArray;
import com.butent.bee.shared.exceptions.BeeRuntimeException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link com.butent.bee.shared.StringArray}.
 */
public class TestStringArray {

  private StringArray sa;

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testStringArrayStringArray() {
    try {
      sa = new StringArray(null);
      fail("Exceptions not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Java exception, needBeeRuntimeException " + e.getMessage());
    }

    String a[] = {"one", "two", "three"};

    sa = new StringArray(a);

    assertArrayEquals(a, sa.getArray(null).getA());
  }
}

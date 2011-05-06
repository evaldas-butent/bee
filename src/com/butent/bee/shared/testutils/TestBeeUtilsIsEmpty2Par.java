package com.butent.bee.shared.testutils;

import com.google.common.collect.Sets;

import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.utils.BeeUtils;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * Tests {@link com.butent.bee.shared.utils.BeeUtils#isEmpty(Object, Set)}.
 */
@RunWith(value = Parameterized.class)
public class TestBeeUtilsIsEmpty2Par extends TestCase {
  private static Set<BeeType> a = Sets.newHashSet();
  private static Set<BeeType> a1 = Sets.newHashSet();
  private static Set<BeeType> a2 = Sets.newHashSet();

  @Parameters
  public static Collection<Object[]> getTestParameters() {
    return Arrays.asList(new Object[][] {
        {false, true, a1},
        {false, 10, a},
        {false, 10.0, a},
        {false, (float) 10e-10, a}
    });
  }

  private BeeUtils beeUtils;
  private boolean expected;
  private Object value1;

  private Set<BeeType> value2;

  public TestBeeUtilsIsEmpty2Par(boolean expected, Object value1,
      Set<BeeType> value2) {
    super();
    this.expected = expected;
    this.value1 = value1;
    this.value2 = value2;
  }

  @SuppressWarnings("static-access")
  @Before
  public void setUp() throws Exception {
    BeeType b = null;
    a.add(b.INT);
    a1.add(b.NUMBER);
    a2.add(b.UNKNOWN);
    a2.add(b.BOOLEAN);
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("static-access")
  @Test
  public void testIsEmpty() {
    assertEquals(expected, beeUtils.isEmpty(value1, value2));
  }
}

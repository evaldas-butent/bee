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
 * Tests {@link com.butent.bee.shared.utils.BeeUtils#containsAny(Collection, Collection)}.
 */
@RunWith(value = Parameterized.class)
public class TestBeeUtilscontaintAny extends TestCase {

  private static Set<BeeType> testc1 = Sets.newHashSet();
  private static Set<BeeType> testc2 = Sets.newHashSet();

  private static Set<BeeType> testc3 = Sets.newHashSet();
  private static Set<BeeType> testc4 = Sets.newHashSet();

  @Parameters
  public static Collection<Object[]> getTestParameters() {
    return Arrays.asList(new Object[][]
    {
        {false, testc1, testc2},
        {false, testc1, testc3},
        {false, testc3, testc1},
        {true, testc1, testc4}
    });
  }

  boolean expected;

  Set<BeeType> value1, value2;

  public TestBeeUtilscontaintAny(boolean expected, Set<BeeType> value1, Set<BeeType> value2) {
    this.expected = expected;
    this.value1 = value1;
    this.value2 = value2;
  }

  @Override
  @SuppressWarnings("static-access")
  @Before
  public void setUp() throws Exception {
    BeeType a = null;
    BeeType b = null;
    testc1.add(a.BOOLEAN);
    testc1.add(a.DOUBLE);
    testc1.add(a.NUMBER);

    testc2.add(b.BYTE);
    testc2.add(b.FLOAT);
    testc2.add(b.INT);

    testc3.add(a.CHAR);
    
    testc4.add(a.DOUBLE);
  }

  @Override
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testContainsAny() {
    assertEquals(expected, BeeUtils.containsAny(value1, value2));
  }
}

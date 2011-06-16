package com.butent.bee.shared.testutils;

import com.butent.bee.shared.Stage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link com.butent.bee.shared.Stage}.
 */
public class TestStage {

  private Stage stage1;
  private Stage stage2;

  @Before
  public void setUp() throws Exception {
    stage1 = new Stage("ServiceA", "Stage1of1");
    stage2 = new Stage();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testGetService() {
    assertEquals("ServiceA", stage1.getService());
    assertNull(stage2.getService());
  }

  @Test
  public final void testGetStage() {
    assertEquals("Stage1of1", stage1.getStage());
    assertNull(stage2.getStage());
  }

  @Test
  public final void testSetService() {
    stage1.setService("svc");
    assertEquals("svc", stage1.getService());

    stage2.setService("");
    assertEquals("", stage2.getService());

    stage1.setService(null);
    assertNull(stage1.getService());
  }

  @Test
  public final void testSetStage() {
    stage1.setStage("aaa");
    assertEquals("aaa", stage1.getStage());

    stage1.setStage(null);
    assertNull(stage1.getStage());

    stage2.setStage("ddd");
    assertEquals("ddd", stage2.getStage());
  }
}

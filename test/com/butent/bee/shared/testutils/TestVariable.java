package com.butent.bee.shared.testutils;

import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Variable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Tests {@link com.butent.bee.shared.Variable}.
 */
public class TestVariable {

  Variable var;

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testGetBoolean() {
    var = new Variable(BeeType.STRING);
    var.setValue("10");

    assertFalse(var.getBoolean());
    var.setValue(true);
    assertTrue(var.getBoolean());

    var.setValue(0.9);

    assertFalse(var.getBoolean());

    var.setValue("firm");
    assertFalse(var.getBoolean());
  }

  @Test
  public final void testGetDouble() {
    var = new Variable(BeeType.STRING);

    var.setValue("xyz");
    assertEquals(0.0, var.getDouble(), 0.0);

    var.setValue(true);

    assertEquals(0.0, var.getDouble(), 0.0);

    var.setValue(false);
    assertEquals(0.0, var.getDouble(), 0.0);

    var.setValue(1.0);
    assertEquals(1.0, var.getDouble(), 0.0);

    var.setValue(99);
    assertEquals(99.0, var.getDouble(), 0.0);

    var.setValue("-128.99");
    assertEquals(-128.99, var.getDouble(), 0.0);
  }

  @Test
  public final void testGetInt() {
    var = new Variable(BeeType.INT);

    var.setValue(true);

    assertEquals(0, var.getInt());

    var.setValue(false);
    assertEquals(0, var.getInt());

    var.setValue(1.0);
    assertEquals(1, var.getInt());

    var.setValue(99);
    assertEquals(99, var.getInt());

    var.setValue(-1.0);
    assertEquals(-1, var.getInt());

    var.setValue(254.0);
    assertEquals(254, var.getInt());

    var.setValue(458.1);
    assertEquals(458, var.getInt());

    var.setValue("-128.99");
    assertEquals(-128, var.getInt());
  }

  @Test
  public final void testGetItems() {
    var = new Variable(BeeType.STRING);

    assertNull(var.getItems());

    String expArr[] = {"any", "many", "delta"};
    ArrayList<String> expLst = new ArrayList<String>();
    expLst.add("any");
    expLst.add("many");
    expLst.add("delta");

    var.setItems(expLst);

    assertArrayEquals(expArr, var.getItems().toArray());
  }

  @Test
  public final void testGetLong() {
    var = new Variable(BeeType.LONG);

    var.setValue(true);

    assertEquals(0, var.getLong());

    var.setValue(false);
    assertEquals(0, var.getLong());

    var.setValue(1.0);
    assertEquals(1, var.getLong());

    var.setValue(99);
    assertEquals(99, var.getLong());

    var.setValue("-128.99");
    assertEquals(-128, var.getLong());
  }

  @Test
  public final void testGetString() {
    var = new Variable(BeeType.STRING);

    var.setValue(true);

    assertEquals("true", var.getString());

    var.setValue(false);
    assertEquals("false", var.getString());

    var.setValue(1.0);
    try {
      assertEquals("1.0", var.getString());
    } catch (AssertionError e) {
      System.out
          .println("[INFO] TeestVariable.testGetString(): Alternative test 1");
      assertEquals("1", var.getString());
    }

    var.setValue(99);
    assertEquals("99", var.getString());

    var.setValue("-128.99");
    assertEquals("-128.99", var.getString());
  }

  @Test
  public final void testGetWidth() {
    var = new Variable(BeeType.STRING);

    var.setWidth(null);
    assertNull(var.getWidth());
  }

  @Test
  public final void testSetWidget() {
    var = new Variable(BeeType.STRING);

    assertNull(var.getWidget());

    var.setWidget(BeeWidget.UPLOAD);
    assertEquals(BeeWidget.UPLOAD, var.getWidget());
  }

  @Test
  public final void testVariable() {
    var = new Variable(BeeType.STRING);
    assertEquals(BeeType.STRING, var.getType());
    assertNull(var.getValue());
    assertNull(var.getCaption());
    assertNull(var.getWidget());
    assertNull(var.getItems());
  }

  @Test
  public final void testVariableBeeType() {
    var = new Variable(BeeType.DATE);
    assertEquals(BeeType.DATE, var.getType());
    assertNull(var.getValue());
    assertNull(var.getCaption());
    assertNull(var.getWidget());
    assertNull(var.getItems());
  }

  @Test
  public final void testVariableBeeTypeString() {
    var = new Variable(BeeType.CHAR, "d");
    assertEquals(BeeType.CHAR, var.getType());
    assertEquals(null, var.getCaption());
    assertEquals("d", var.getValue());
    assertNull(var.getWidget());
    assertNull(var.getItems());
  }

  @Test
  public final void testVariableStringBeeType() {
    var = new Variable("value 5", BeeType.BYTE);

    assertEquals("value 5", var.getCaption());
    assertEquals(BeeType.BYTE, var.getType());
    assertNull(var.getValue());
    assertNull(var.getWidget());
    assertNull(var.getItems());
  }

  @Test
  public final void testVariableStringBeeTypeString() {
    var = new Variable(null, BeeType.BOOLEAN, "true");

    assertEquals(null, var.getCaption());
    assertEquals(BeeType.BOOLEAN, var.getType());
    assertEquals("true", var.getValue());
    assertEquals(true, var.getBoolean());

    var = new Variable("Var2", null, "lblobas");

    assertEquals("Var2", var.getCaption());
    assertEquals(null, var.getType());
    assertEquals("lblobas", var.getValue());

    var = new Variable("Var3", BeeType.ENUM, null);

    assertEquals("Var3", var.getCaption());
    assertEquals(BeeType.ENUM, var.getType());
    assertEquals(null, var.getValue());
    assertNull(var.getWidget());
    assertNull(var.getItems());
  }

  @Test
  public final void testVariableStringBeeTypeStringBeeWidgetStringArray() {
    var = new Variable("Var10", BeeType.INT, "100", BeeWidget.BUTTON_GROUP,
        "one");
    assertEquals("Var10", var.getCaption());
    assertEquals(BeeType.INT, var.getType());
    assertEquals("100", var.getValue());
    assertEquals(100, var.getInt());
    assertEquals(BeeWidget.BUTTON_GROUP, var.getWidget());
    String[] a = {"one"};
    assertArrayEquals(a, var.getItems().toArray());

    var = new Variable("Var11", BeeType.LONG, "54", BeeWidget.CHECK, "one",
        "time");
    assertEquals("Var11", var.getCaption());
    assertEquals(BeeType.LONG, var.getType());
    assertEquals("54", var.getValue());
    assertEquals(54, var.getLong());
    assertEquals(BeeWidget.CHECK, var.getWidget());
    String[] b = {"one", "time"};
    assertArrayEquals(b, var.getItems().toArray());

    var = new Variable("Var9", BeeType.FLOAT, "1.255", BeeWidget.BUTTON,
        (String[]) null);

    assertEquals("Var9", var.getCaption());
    assertEquals(BeeType.FLOAT, var.getType());
    assertEquals("1.255", var.getValue());
    assertEquals(1.255, var.getDouble(), 0.0);
    assertEquals(BeeWidget.BUTTON, var.getWidget());
    assertNull(var.getItems());
  }

}

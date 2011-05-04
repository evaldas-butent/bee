package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Variable;

/**
 * Tests {@link com.butent.bee.shared.Variable}
 */
public class TestVariable {

	Variable Var;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testVariable() {
		Var = new Variable();
		assertEquals(BeeType.UNKNOWN, Var.getType());
		assertNull(Var.getValue());
		assertNull(Var.getCaption());
		assertNull(Var.getWidget());
		assertNull(Var.getItems());
	}

	@Test
	public final void testVariableBeeType() {
		Var = new Variable(BeeType.DATE);
		assertEquals(BeeType.DATE, Var.getType());
		assertNull(Var.getValue());
		assertNull(Var.getCaption());
		assertNull(Var.getWidget());
		assertNull(Var.getItems());
	}

	@Test
	public final void testVariableBeeTypeString() {
		Var = new Variable(BeeType.CHAR, "d");
		assertEquals(BeeType.CHAR, Var.getType());
		assertEquals(null, Var.getCaption());
		assertEquals("d", Var.getValue());
		assertNull(Var.getWidget());
		assertNull(Var.getItems());
	}

	@Test
	public final void testVariableStringBeeType() {
		Var = new Variable("value 5", BeeType.BYTE);

		assertEquals("value 5", Var.getCaption());
		assertEquals(BeeType.BYTE, Var.getType());
		assertNull(Var.getValue());
		assertNull(Var.getWidget());
		assertNull(Var.getItems());
	}

	@Test
	public final void testVariableStringBeeTypeString() {
		Var = new Variable("Var1", BeeType.BLOB, "blobas");

		assertEquals("Var1", Var.getCaption());
		assertEquals(BeeType.BLOB, Var.getType());
		assertEquals("blobas", Var.getValue());

		Var = new Variable(null, BeeType.BOOLEAN, "true");

		assertEquals(null, Var.getCaption());
		assertEquals(BeeType.BOOLEAN, Var.getType());
		assertEquals("true", Var.getValue());
		assertEquals(true, Var.getBoolean());

		Var = new Variable("Var2", null, "lblobas");

		assertEquals("Var2", Var.getCaption());
		assertEquals(null, Var.getType());
		assertEquals("lblobas", Var.getValue());

		Var = new Variable("Var3", BeeType.ENUM, null);

		assertEquals("Var3", Var.getCaption());
		assertEquals(BeeType.ENUM, Var.getType());
		assertEquals(null, Var.getValue());
		assertNull(Var.getWidget());
		assertNull(Var.getItems());
	}

	@Test
	public final void testVariableStringBeeTypeStringBeeWidget() {
		Var = new Variable("Var8", BeeType.FILE, "/@vmlinuz", BeeWidget.AREA);

		assertEquals("Var8", Var.getCaption());
		assertEquals(BeeType.FILE, Var.getType());
		assertEquals("/@vmlinuz", Var.getValue());
		assertEquals(BeeWidget.AREA, Var.getWidget());
		assertNull(Var.getItems());
	}

	@Test
	public final void testVariableStringBeeTypeStringBeeWidgetStringArray() {

		Var = new Variable("Var10", BeeType.INT, "100", BeeWidget.BUTTON_GROUP,
				"one");
		assertEquals("Var10", Var.getCaption());
		assertEquals(BeeType.INT, Var.getType());
		assertEquals("100", Var.getValue());
		assertEquals(100, Var.getInt());
		assertEquals(BeeWidget.BUTTON_GROUP, Var.getWidget());
		String[] a = { "one" };
		assertArrayEquals(a, Var.getItems().toArray());

		Var = new Variable("Var11", BeeType.LONG, "54", BeeWidget.CHECK, "one",
				"time");
		assertEquals("Var11", Var.getCaption());
		assertEquals(BeeType.LONG, Var.getType());
		assertEquals("54", Var.getValue());
		assertEquals(54, Var.getLong());
		assertEquals(BeeWidget.CHECK, Var.getWidget());
		String[] b = { "one", "time" };
		assertArrayEquals(b, Var.getItems().toArray());

		Var = new Variable("Var9", BeeType.FLOAT, "1.255", BeeWidget.BUTTON,
				(String[]) null);

		assertEquals("Var9", Var.getCaption());
		assertEquals(BeeType.FLOAT, Var.getType());
		assertEquals("1.255", Var.getValue());
		assertEquals(1.255, Var.getDouble(), 0.0);
		assertEquals(BeeWidget.BUTTON, Var.getWidget());
		assertNull(Var.getItems());
	}

	@Test
	public final void testGetBoolean() {
		Var = new Variable();
		Var.setValue("10");

		assertTrue(Var.getBoolean());
		Var.setValue(true);
		assertTrue(Var.getBoolean());

		Var.setValue(0.9);

		assertFalse(Var.getBoolean());

		Var.setValue("firm");
		assertFalse(Var.getBoolean());
	}

	@Test
	public final void testGetDouble() {
		Var = new Variable();

		Var.setValue("xyz");
		assertEquals(0.0, Var.getDouble(), 0.0);

		Var.setValue(true);

		assertEquals(0.0, Var.getDouble(), 0.0);

		Var.setValue(false);
		assertEquals(0.0, Var.getDouble(), 0.0);

		Var.setValue(1.0);
		assertEquals(1.0, Var.getDouble(), 0.0);

		Var.setValue(99);
		assertEquals(99.0, Var.getDouble(), 0.0);

		Var.setValue("-128.99");
		assertEquals(-128.99, Var.getDouble(), 0.0);

	}

	@Test
	public final void testGetInt() {
		Var = new Variable();

		Var.setValue(true);

		assertEquals(0, Var.getInt());

		Var.setValue(false);
		assertEquals(0, Var.getInt());

		Var.setValue(1.0);
		assertEquals(1, Var.getInt());

		Var.setValue(99);
		assertEquals(99, Var.getInt());

		Var.setValue(-1.0);
		assertEquals(-1, Var.getInt());

		Var.setValue(254.0);
		assertEquals(254, Var.getInt());

		Var.setValue(458.1);
		assertEquals(458, Var.getInt());

		Var.setValue("-128.99");
		assertEquals(-128, Var.getInt());
	}

	@Test
	public final void testGetItems() {
		Var = new Variable();

		assertNull(Var.getItems());

		String expArr[] = { "any", "many", "delta" };
		ArrayList<String> expLst = new ArrayList<String>();
		expLst.add("any");
		expLst.add("many");
		expLst.add("delta");

		Var.setItems(expLst);

		assertArrayEquals(expArr, Var.getItems().toArray());
	}

	@Test
	public final void testGetLong() {
		Var = new Variable();

		Var.setValue(true);

		assertEquals(0, Var.getLong());

		Var.setValue(false);
		assertEquals(0, Var.getLong());

		Var.setValue(1.0);
		assertEquals(1, Var.getLong());

		Var.setValue(99);
		assertEquals(99, Var.getLong());

		Var.setValue("-128.99");
		assertEquals(-128, Var.getLong());
	}

	@Test
	public final void testGetString() {
		Var = new Variable();

		Var.setValue(true);

		assertEquals("true", Var.getString());

		Var.setValue(false);
		assertEquals("false", Var.getString());

		Var.setValue(1.0);
		try {
			assertEquals("1.0", Var.getString());
		} catch (AssertionError e) {
			System.out
					.println("[INFO] TeestVariable.testGetString(): Alternative test 1");
			assertEquals("1", Var.getString());
		}

		Var.setValue(99);
		assertEquals("99", Var.getString());

		Var.setValue("-128.99");
		assertEquals("-128.99", Var.getString());
	}

	@Test
	public final void testGetWidth() {
		Var = new Variable();

		Var.setWidth(null);
		assertNull(Var.getWidth());
	}

	@Test
	public final void testSetType() {
		Var = new Variable();

		assertEquals(BeeType.UNKNOWN, Var.getType());

		Var.setType(BeeType.TEXT);

		assertEquals(BeeType.TEXT, Var.getType());
	}

	@Test
	public final void testSetWidget() {
		Var = new Variable();

		assertNull(Var.getWidget());

		Var.setWidget(BeeWidget.UPLOAD);
		assertEquals(BeeWidget.UPLOAD, Var.getWidget());

	}

	@Test
	public final void testToString() {
		Var = new Variable("TestUnit", BeeType.BLOB, "1.0");

		assertEquals("caption=TestUnit;type=BLOB;value=1.0", Var.toString());
	}

	@Test
	public final void testTransform() {
		Var = new Variable("TestUnit", BeeType.BLOB, "1.0");
		assertEquals("caption=TestUnit;type=BLOB;value=1.0", Var.transform());
	}

}

package com.butent.bee.shared.testutils;

import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.exceptions.BeeRuntimeException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link com.butent.bee.server.sql.SqlInsert}.
 */
public class TestSqlInsert {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testAddConstant() {
    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlBuilder builder = SqlBuilderFactory.getBuilder();

    SqlInsert insert = new SqlInsert("TableName");

    insert.addConstant("username", "Petras");
    insert.addConstant("pass", "slaptasPass");

    assertEquals(
        "INSERT INTO TableName (username, pass) VALUES ('Petras', 'slaptasPass')",
        insert.getSqlString(builder, false));
  }

  @Test
  public final void testAddFields() {
    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlBuilder builder = SqlBuilderFactory.getBuilder();

    SqlInsert insert = new SqlInsert("TableName");

    insert.addFields("field1", "field2", "field3");
    insert.addConstant("field4", "slaptasPass");

    assertEquals(
        "INSERT INTO TableName (field1, field2, field3, field4) VALUES ('slaptasPass')",
        insert.getSqlString(builder, false));
  }

  @Test
  public final void testGetFieldCount() {
    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlInsert insert = new SqlInsert("TableName");

    insert.addFields("field1", "field2", "field3");
    assertEquals(3, insert.getFieldCount());

    try {
      insert.addFields("field4", null);
      assertEquals(4, insert.getFieldCount());
      fail("BeeRuntimeException not works");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
      System.out
          .println("public final void getField(): " + e.getMessage());
    } catch (Exception e) {
      fail("Java runtime error. Need BeeRuntimeException !!!");
    }
  }

  @Test
  public final void testGetSources() {

    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlInsert insert = new SqlInsert("TableName");

    SqlSelect select = new SqlSelect();
    select.addFields("Table1", "field1", "field2");

    select.addFrom("Table1");
    select.addFrom("Table2");
    insert.setDataSource(select);

    Object[] rez = insert.getSources().toArray();
    Object[] expected = {"Table2", "TableName", "Table1"};

    assertArrayEquals(expected, rez);
  }

  @Test
  public final void testGetSqlParams() {
    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlInsert insert2 = new SqlInsert("Target");

    insert2.addConstant("field", 5);
    insert2.addConstant("field2", 10);

    Object[] expectedStr = {5, 10};

    for (int i = 0; i < insert2.getSqlParams().size(); i++) {
      assertEquals(expectedStr[i], insert2.getSqlParams().toArray()[i]);
    }

    insert2.reset();

    SqlSelect select2 = new SqlSelect();
    select2.addFields("Table1", "field1", "field2");
    select2.addFrom("Table1");
    select2.addSum(SqlUtils.constant("constant"), "consant_alias");
    select2.addGroup("table1", "field2");

    insert2.addFields("field1");
    insert2.setDataSource(select2);

    Object[] expectedStr2 = {"constant", 10};

    for (int i = 0; i < insert2.getSqlParams().size(); i++) {
      assertEquals(expectedStr2[i], insert2.getSqlParams().toArray()[i]);
    }
  }

  @Test
  public final void testIsEmpty() {
    SqlInsert insert = new SqlInsert("Table1");
    assertTrue(insert.isEmpty());
    insert.addExpression("Field1", SqlUtils.expression("100"));
    assertFalse(insert.isEmpty());
  }

  @Test
  public final void testReset() {

    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlInsert insert = new SqlInsert("TableName");

    SqlSelect select = new SqlSelect();
    select.addFields("Table1", "field1", "field2");
    select.addFrom("Table1");
    IsCondition clause = SqlUtils.equal("Table1", "password", "12345");
    select.setWhere(clause);

    insert.addFields("field1", "field2", "field3");
    insert.addConstant("field4", "slaptasPass");

    insert.reset();

    assertEquals(0, insert.getFieldCount());

    insert.setDataSource(select);
    insert.reset();
    assertEquals(null, insert.getDataSource());
  }
}

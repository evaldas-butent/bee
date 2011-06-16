package com.butent.bee.shared.testutils;

import com.butent.bee.server.sql.BeeConstants.DataType;
import com.butent.bee.server.sql.BeeConstants.Keyword;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.exceptions.BeeRuntimeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link com.butent.bee.server.sql.SqlCreate}.
 */
public class TestSqlCreate {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public final void testIsEmpty() {
    try {
      SqlCreate create = new SqlCreate("\n \r \t");
      fail("Exceptions not work");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeEception : " + e.getMessage());
    }

    SqlCreate create = new SqlCreate("table");
    assertTrue(create.isEmpty());
    assertFalse(create.hasField("table"));

    create.addField("Field1", DataType.DOUBLE, 5, 100);

    assertFalse(create.isEmpty());
    assertFalse(create.hasField("table"));
    assertTrue(create.hasField("Field1"));
  }

  @Test
  public final void testSqlCreate() {

    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlBuilder builder = SqlBuilderFactory.getBuilder();

    SqlCreate create = new SqlCreate("Target", true);

    create.addBoolean("arIvykdyta", Keyword.SET_NULL);
    create.getSqlString(builder, false);
    assertEquals("CREATE TEMPORARY TABLE Target (arIvykdyta BIT SET NULL)", create.getSqlString(
        builder, false));

    SqlBuilderFactory.setDefaultEngine(BeeConst.MSSQL);
    builder = SqlBuilderFactory.getBuilder();

    create = new SqlCreate("Target", true);

    create.addBoolean("arIvykdyta", Keyword.SET_NULL);
    create.addField("field2", DataType.INTEGER, 5, 6);
    create.getSqlString(builder, false);
    assertEquals("CREATE TABLE [Target] ([arIvykdyta] BIT SET NULL, [field2] INTEGER)", create
        .getSqlString(builder, false));

    SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
    builder = SqlBuilderFactory.getBuilder();

    create = new SqlCreate("Target", true);

    create.addBoolean("arIvykdyta", Keyword.SET_NULL);
    create.addField("field2", DataType.INTEGER, 5, 6);
    create.getSqlString(builder, false);
    assertEquals(
        "CREATE TEMPORARY TABLE `Target` (`arIvykdyta` BIT SET NULL, `field2` INTEGER) ENGINE=InnoDB",
        create.getSqlString(builder, false));

    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlBuilder builder2 = SqlBuilderFactory.getBuilder();

    SqlCreate create2 = new SqlCreate("Target", true);

    SqlSelect select2 = new SqlSelect();
    select2.addFields("Table1", "field1", "field2");
    select2.addFrom("Table1");

    create2.setDataSource(select2);
    assertEquals(
        "CREATE TEMPORARY TABLE Target AS SELECT Table1.field1, Table1.field2 FROM Table1", create2
            .getSqlString(builder2, false));

    SqlBuilder builder3 = SqlBuilderFactory.getBuilder();

    SqlCreate create3 = new SqlCreate("Target", true);

    SqlSelect select3 = new SqlSelect();
    select3.addFields("Table1", "field1", "field2");
    select3.addFrom("Table1");

    create3.addBoolean("boolean field", Keyword.SET_NULL);
    create3.addChar("char", 25, Keyword.NOT_NULL);
    create3.addDate("data");
    create3.addDateTime("datetime");
    create3.addDouble("double value");
    create3.addInteger("int field", Keyword.NOT_NULL);
    create3.addLong("long field");
    create3.addDecimal("numeric field", 10, 10);
    create3.addString("string field", 7);

    assertEquals(
        "CREATE TEMPORARY TABLE Target (boolean field BIT SET NULL, char CHAR(25) NOT NULL, data INTEGER, datetime BIGINT, double value DOUBLE, int field INTEGER NOT NULL, long field BIGINT, numeric field NUMERIC(10, 10), string field VARCHAR(7))",
        create3.getSqlString(builder3, false));

    SqlBuilderFactory.setDefaultEngine(BeeConst.PGSQL);
    builder = SqlBuilderFactory.getBuilder();

    create = new SqlCreate("Target", true);

    create.addBoolean("arIvykdyta", Keyword.SET_NULL);
    create.addDouble("kaina");
    create.addDate("data");
    create.getSqlString(builder, false);
    assertEquals(
        "CREATE TEMPORARY TABLE \"Target\" (\"arIvykdyta\" NUMERIC(1) SET NULL, \"kaina\" DOUBLE PRECISION, \"data\" INTEGER)",
        create.getSqlString(builder, false));

    SqlBuilderFactory.setDefaultEngine(BeeConst.ORACLE);
    builder = SqlBuilderFactory.getBuilder();

    create = new SqlCreate("Target", true);

    create.addBoolean("arIvykdyta", Keyword.SET_NULL);
    create.addDouble("kaina");
    create.addDate("data");
    create.addInteger("kiek");
    create.addLong("millis");
    create.addDateTime("dateTime");
    create.addString("pav", 10);
    create.addChar("char field", 2);

    create.getSqlString(builder, false);
    assertEquals(
        "CREATE TABLE \"Target\" (\"arIvykdyta\" NUMERIC(1) SET NULL, \"kaina\" BINARY_DOUBLE, \"data\" NUMERIC(10), \"kiek\" NUMERIC(10), \"millis\" NUMERIC(19), \"dateTime\" NUMERIC(19), \"pav\" NVARCHAR2(10), \"char field\" CHAR(2))",
        create.getSqlString(builder, false));
  }

  @Test
  public final void testSqlCreateDataSource() {
    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlBuilder builder4 = SqlBuilderFactory.getBuilder();

    SqlCreate create4 = new SqlCreate("Target");
    SqlCreate create5 = new SqlCreate("Target2");
    create5.addBoolean("bool2");

    SqlSelect select4 = new SqlSelect();
    select4.addFields("Table1", "field1", "field2");
    select4.addFields("Table1", "field1");
    select4.addFrom("Table1");

    create4.addBoolean("boolean field", Keyword.SET_NULL);

    create4.addDate("data");
    create4.addDateTime("datetime");
    create4.addDouble("double value");
    create4.addLong("long field");
    create4.addDecimal("numeric field", 10, 10);
    create4.addString("string field", 7);

    assertEquals(
        "CREATE TEMPORARY TABLE Target (boolean field BIT SET NULL, data INTEGER, datetime BIGINT, double value DOUBLE, long field BIGINT, numeric field NUMERIC(10, 10), string field VARCHAR(7))",
        create4.getSqlString(builder4, false));

    IsExpression expression = create5.getField("bool2").getName();
    assertEquals("bool2", expression.getSqlString(builder4, false));
  }

  @Test
  public final void testSqlCreateGetSources() {
    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlCreate create2 = new SqlCreate("Target", true);

    SqlSelect select2 = new SqlSelect();
    select2.addFields("Table1", "field1", "field2");
    select2.addFrom("Table1");

    create2.setDataSource(select2);
    String[] expectedStr = {"Table1"};
    for (int i = 0; i < create2.getSources().size(); i++) {
      assertEquals(expectedStr[i], create2.getSources().toArray()[i]);
    }
  }

  @Test
  public final void testSqlCreateGetSqlParams() {
    SqlBuilderFactory.setDefaultEngine("Generic");
    SqlCreate create2 = new SqlCreate("Target", true);

    SqlSelect select2 = new SqlSelect();
    select2.addFields("Table1", "field1", "field2");
    select2.addFrom("Table1");
    select2.addSum(SqlUtils.constant("constant"), "consant_alias");
    select2.addGroup("table1", "field2");

    create2.setDataSource(select2);

    String[] expectedStr = {"constant"};

    for (int i = 0; i < create2.getSqlParams().size(); i++) {
      assertEquals(expectedStr[i], create2.getSqlParams().toArray()[i]);
    }
  }
}

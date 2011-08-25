package com.butent.bee.shared.testutils;

import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst.SqlEngine;
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
 * Tests {@link com.butent.bee.server.sql.SqlDelete}.
 */
public class TestSqlDelete {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testGetSources() {
    SqlBuilderFactory.setDefaultBuilder(SqlEngine.GENERIC);

    SqlDelete delete = new SqlDelete("Target_table", "target_alias");
    delete.addFrom("From_source1");
    delete.addFrom("From_source2");
    delete.setWhere(SqlUtils.sqlFalse());

    Object[] arr = delete.getSources().toArray();
    Object[] rez = {"From_source1", "Target_table", "From_source2"};

    assertArrayEquals(rez, arr);
  }

  @Test
  public final void testGetSqlString() {
    SqlBuilderFactory.setDefaultBuilder(SqlEngine.GENERIC);
    SqlBuilder builder = SqlBuilderFactory.getBuilder();

    SqlDelete delete = new SqlDelete("Target_table");
    delete.addFrom("From_source1");
    delete.addFrom("From_source2");

    delete.setWhere(SqlUtils.equal(SqlUtils.name("username"), "root"));

    assertEquals(
        "DELETE FROM Target_table FROM From_source1, From_source2 WHERE username = 'root'",
        delete.getSqlString(builder));
  }

  @Test
  public final void testGetSqlStringAlias() {
    SqlBuilderFactory.setDefaultBuilder(SqlEngine.GENERIC);
    SqlBuilder builder = SqlBuilderFactory.getBuilder();

    SqlDelete delete = new SqlDelete("Target_table", "target_alias");
    delete.addFrom("From_source1");
    delete.addFrom("From_source2");

    delete.setWhere(SqlUtils.equal(SqlUtils.name("username"), "root"));

    assertEquals(
        "DELETE FROM Target_table target_alias FROM From_source1, From_source2 WHERE username = 'root'",
        delete.getSqlString(builder));
  }

  @SuppressWarnings("unused")
  @Test
  public final void testIsEmpty() {
    try {
      SqlDelete del = new SqlDelete("\n \t \r");
      fail("Exceptions not work");
    } catch (BeeRuntimeException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail("Need BeeRuntimeException: " + e.getMessage());
    }
    SqlDelete del = new SqlDelete("Table1");
    assertTrue(del.isEmpty());

    del.setWhere(SqlUtils.isNull("Table1", "Filed1"));
    assertFalse(del.isEmpty());
  }

}

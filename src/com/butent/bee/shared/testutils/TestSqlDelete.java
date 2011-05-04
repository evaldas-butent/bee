package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlUtils;

public class TestSqlDelete {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGetSqlString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlDelete delete = new SqlDelete("Target_table");
		delete.addFrom("From_source1");
		delete.addFrom("From_source2");

		delete.setWhere(SqlUtils.equal(SqlUtils.name("username"), "root"));

		assertEquals(
				"DELETE FROM Target_table FROM From_source1, From_source2 WHERE username='root'",
				delete.getSqlString(builder, false));
	}

	@Test
	public final void testGetSqlStringAlias() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlDelete delete = new SqlDelete("Target_table", "target_alias");
		delete.addFrom("From_source1");
		delete.addFrom("From_source2");

		delete.setWhere(SqlUtils.equal(SqlUtils.name("username"), "root"));

		assertEquals(
				"DELETE FROM Target_table target_alias FROM From_source1, From_source2 WHERE username='root'",
				delete.getSqlString(builder, false));
	}

	@Test
	public final void testGetSources() {
		SqlBuilderFactory.setDefaultEngine("Generic");

		SqlDelete delete = new SqlDelete("Target_table", "target_alias");
		delete.addFrom("From_source1");
		delete.addFrom("From_source2");
		delete.setWhere(SqlUtils.sqlFalse());

		Object[] arr = delete.getSources().toArray();
		Object[] rez = { "From_source1", "Target_table", "From_source2" };

		assertArrayEquals(rez, arr);
	}

	@Test
	public final void testGetSqlParams() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlDelete delete = new SqlDelete("Target_table", "target_alias");
		delete.addFrom("From_source1");
		delete.addFrom("From_source2");

		try {
			delete.setWhere(SqlUtils.equal("Target_table", "field", 'c'));

			Object[] a = delete.getSqlParams().toArray();
			Object[] rez = { 'c' };

			assertArrayEquals(rez, a);

			assertEquals(
					"DELETE FROM Target_table target_alias FROM From_source1, From_source2 WHERE Target_table.field=c",
					delete.getSqlString(builder, false));
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		}

		delete.setWhere(SqlUtils.equal("Target_table", "field", "c"));

		Object[] a1 = delete.getSqlParams().toArray();
		Object[] rez1 = { "c" };

		assertArrayEquals(rez1, a1);

		assertEquals(
				"DELETE FROM Target_table target_alias FROM From_source1, From_source2 WHERE Target_table.field='c'",
				delete.getSqlString(builder, false));

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
			fail("Need BeeRuntimeException :" + e.getMessage());
		}
		SqlDelete del = new SqlDelete("Table1");
		assertTrue(del.isEmpty());

		del.setWhere(SqlUtils.isNull("Table1", "Filed1"));
		assertFalse(del.isEmpty());
	}

}

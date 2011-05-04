package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;

/**
 * Tests {@link com.butent.bee.server.sql.SqlSelect}
 */
public class TestSqlSelect {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testAddAllFields() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("from_table");
		select.addAllFields("Source_table");

		assertEquals("SELECT Source_table.* FROM from_table",
				select.getSqlString(builder, false));
		SqlBuilderFactory.setDefaultEngine(BeeConst.MSSQL);
		builder = SqlBuilderFactory.getBuilder();
		assertEquals("SELECT [Source_table].* FROM [from_table]",
				select.getSqlString(builder, false));
		select.setLimit(30);
		assertEquals("SELECT TOP 30 [Source_table].* FROM [from_table]",
				select.getSqlString(builder, false));
		select.setOffset(10);
		select.addOrder("Table1", "field2");

		assertEquals(true,
				select.getSqlString(builder, false).contains("SELECT ["));
		assertEquals(
				true,
				select.getSqlString(builder, false)
						.contains(
								"].* FROM (SELECT TOP 40 ROW_NUMBER() OVER (ORDER BY [Table1].[field2]) AS ["));
		assertEquals(
				true,
				select.getSqlString(builder, false).contains(
						"], [Source_table].* FROM [from_table]) ["));
		assertEquals(true,
				select.getSqlString(builder, false).contains("] WHERE ["));

		SqlSelect select1 = new SqlSelect();
		select1.addFrom("Table1");
		select1.addAllFields("Table12");

		select = new SqlSelect();
		select.addFrom("from_table");
		select.addAllFields("Source_table");
		select.setLimit(9);
		select.addUnion(select1);
		select.addOrder("Table22", "field21");

		assertEquals(true,
				select.getSqlString(builder, false).contains("SELECT TOP 9 ["));
		assertEquals(
				true,
				select.getSqlString(builder, false)
						.contains(
								"].* FROM (SELECT [Source_table].* FROM [from_table] UNION ALL (SELECT [Table12].* FROM [Table1]) ORDER BY [field21]) ["));
	}

	@Test
	public final void testAddAvgExprString() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("from_table");
		select.addAvg(SqlUtils.name("salary"), "atlygio_vidurkis");
		assertEquals("SELECT AVG(salary) AS atlygio_vidurkis FROM from_table",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddAvgStringString() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("from_table");
		select.addAvg("from_table", "salary");
		assertEquals("SELECT AVG(from_table.salary) AS salary FROM from_table",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddAvgStringStringString() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("from_table");
		select.addAvg("from_table", "salary", "atlygio_vidurkis");
		assertEquals(
				"SELECT AVG(from_table.salary) AS atlygio_vidurkis FROM from_table",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddConstant() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("from_table");
		select.addConstant("name", "const_alias");
		assertEquals("SELECT 'name' AS const_alias FROM from_table",
				select.getSqlString(builder, false));

		select.addConstant(true, "const_alias2");
		assertEquals(
				"SELECT 'name' AS const_alias, 1 AS const_alias2 FROM from_table",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddCountExprString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("from_table");

		select.addCount(SqlUtils.name("name"), "name_alias");

		assertEquals("SELECT COUNT(name) AS name_alias FROM from_table",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddCountString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("from_table");
		select.addCount("count_alias");

		assertEquals("SELECT COUNT(*) AS count_alias FROM from_table",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddExprStringString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("from_table");
		select.addExpr("kazkas", "expr_alias");

		assertEquals("SELECT kazkas AS expr_alias FROM from_table",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddMaxExprString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addMax(SqlUtils.expression("salary"), "highest_salary");

		assertEquals("SELECT MAX(salary) AS highest_salary FROM employees",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddMaxStringString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addMax("employees", "salary");

		assertEquals("SELECT MAX(employees.salary) AS salary FROM employees",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddMaxStringStringString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addMax("employees", "salary", "highest_salary");

		assertEquals(
				"SELECT MAX(employees.salary) AS highest_salary FROM employees",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddMinExprString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addMin(SqlUtils.expression("salary"), "lowest_salary");

		assertEquals("SELECT MIN(salary) AS lowest_salary FROM employees",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddMinStringString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addMin("employees", "salary");

		assertEquals("SELECT MIN(employees.salary) AS salary FROM employees",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddMinStringStringString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addMin("employees", "salary", "lowest_salary");

		assertEquals(
				"SELECT MIN(employees.salary) AS lowest_salary FROM employees",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddOrder() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addFields("employees", "name");
		select.addOrder("employees", "salary", "hours");

		assertEquals(
				"SELECT employees.name FROM employees ORDER BY employees.salary, employees.hours",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddOrderDesc() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addFields("employees", "name");
		select.addOrderDesc("employees", "salary", "hours");

		assertEquals(
				"SELECT employees.name FROM employees ORDER BY employees.salary DESC, employees.hours DESC",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testSumMinExprString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addSum(SqlUtils.expression("salary"), "all_salaries");

		assertEquals("SELECT SUM(salary) AS all_salaries FROM employees",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testSumMinStringString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addSum("employees", "salary");

		assertEquals("SELECT SUM(employees.salary) AS salary FROM employees",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testSumMinStringStringString() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("employees");
		select.addSum("employees", "salary", "all_salaries");
		select.setWhere(SqlUtils.notNull("employees", "salary"));

		assertEquals(
				"SELECT SUM(employees.salary) AS all_salaries FROM employees WHERE employees.salary IS NOT NULL",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testAddUnion() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addField("Table", "name", "vardai");
		select.addFrom("employees_Butent_Kaunas");

		SqlSelect select2 = new SqlSelect();
		select2.addField("Table2", "name2", "vardai2");
		select2.addFrom("employees_Butent_Vilnius");

		select.addUnion(select2);

		assertEquals(
				"SELECT Table.name AS vardai FROM employees_Butent_Kaunas UNION ALL (SELECT Table2.name2 AS vardai2 FROM employees_Butent_Vilnius)",
				select.getSqlString(builder, false));
	}

	@Test
	public final void testReset() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlSelect select = new SqlSelect();
		select.addField("Table", "name", "vardai");
		select.addFrom("employees_Butent_Kaunas");

		SqlSelect select2 = new SqlSelect();
		select2.addField("Table2", "name2", "vardai2");
		select2.addFrom("employees_Butent_Vilnius");

		select.addUnion(select2);
		select.addOrder("Table", "name");
		select.addGroup("Table", "name");
		select.setHaving(SqlUtils.contains(SqlUtils.expression("name"), "Petr"));
		select.setDistinctMode(false);

		select.reset();

		assertEquals(0, select.getFields().size());
		assertEquals(0, select.getGroupBy().size());
		assertEquals(null, select.getHaving());
		assertEquals(0, select.getLimit());
		assertEquals(0, select.getOrderBy().size());
		assertEquals(0, select.getOffset());

	}

	@Test
	public final void testGetSources() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlSelect select = new SqlSelect();
		select.addField("Table", "name", "vardai");
		select.addFrom("employees_Butent_Kaunas");

		SqlSelect select2 = new SqlSelect();
		select2.addField("Table2", "name2", "vardai2");
		select2.addFrom("employees_Butent_Vilnius");
		select2.setWhere(SqlUtils.sqlTrue());

		select.addUnion(select2);
		select.addOrder("Table", "name");
		select.addGroup("Table", "name");
		select.setHaving(SqlUtils.contains(SqlUtils.expression("name"), "Petr"));
		select.setDistinctMode(false);

		Object[] a = select.getSources().toArray();
		Object[] rez = { "employees_Butent_Vilnius", "employees_Butent_Kaunas" };

		assertArrayEquals(rez, a);

		SqlSelect select3 = new SqlSelect();
		select3.addFrom("Table2");
		select3.addFields("Table2", "field21");
		select3.setWhere(SqlUtils.equal(SqlUtils.name("field31"), "val1"));
		Map<Integer, Object> ss = select3.getQueryParams();
		assertEquals("val1", ss.get(1));
	}

	@Test
	public final void testGetSqlParams() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlSelect select = new SqlSelect();
		select.addField("Table", "name", "vardai");
		select.addFrom("employees_Butent_Kaunas");

		SqlSelect select2 = new SqlSelect();
		select2.addField("Table2", "name2", "vardai2");
		select2.addFrom("employees_Butent_Vilnius");
		select2.setWhere(SqlUtils.sqlTrue()); // getSqlParams returns 1

		select.addUnion(select2);
		select.addOrder("Table", "name");
		select.addGroup("Table", "name");
		select.setHaving(SqlUtils.contains(SqlUtils.expression("name"), "Petr")); // getSqlParams
																					// returns
																					// %Petr%
		select.setDistinctMode(false);
		select2.setWhere(SqlUtils.sqlTrue());

		Object[] a = select.getSqlParams().toArray();
		Object[] rez = { "%Petr%", 1.0 };

		assertArrayEquals(rez, a);
	}

	@Test
	public final void testCopyOf() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlSelect select = new SqlSelect();

		select.addFields("Table1", "field1", "field2");
		select.addFrom("Table1");

		SqlSelect cpselect = select.copyOf();

		assertEquals(select.getQuery(), cpselect.getQuery());
		String excpected = select.getQuery();

		select.addFrom("Table2");
		assertEquals(excpected, cpselect.getQuery());

		cpselect = select.copyOf();
		assertEquals(select.getQuery(), cpselect.getQuery());

		excpected = select.getQuery();
		select.addGroup("Table2", "field1", "field2");
		assertEquals(excpected, cpselect.getQuery());
		cpselect = select.copyOf();
		assertEquals(select.getQuery(), cpselect.getQuery());

		excpected = select.getQuery();
		select.addOrder("Table1", "field3", "field4");
		assertEquals(excpected, cpselect.getQuery());
		cpselect = select.copyOf();
		assertEquals(select.getQuery(), cpselect.getQuery());

		excpected = select.getQuery();
		select.addUnion(cpselect);
		assertEquals(excpected, cpselect.getQuery());
		cpselect = select.copyOf();
		assertEquals(select.getQuery(), cpselect.getQuery());
	}

	@Test
	public final void testIsEmpty() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlSelect select = new SqlSelect();
		assertTrue(select.isEmpty());

		select.addFields("Table1", "field1");
		assertTrue(select.isEmpty());

		select.addFrom("Table1");
		assertFalse(select.isEmpty());
	}

	@Test
	public final void testSqlLimitOffset() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		SqlSelect select = new SqlSelect();
		select.addFrom("table1");
		select.addFields("table1", "field1");

		select.setOffset(10);
		assertEquals(
				"SELECT `table1`.`field1` FROM `table1` LIMIT 1000000000 OFFSET 10",
				select.getSqlString(builder, false));

		select.setLimit(10);
		assertEquals(
				"SELECT `table1`.`field1` FROM `table1` LIMIT 10 OFFSET 10",
				select.getSqlString(builder, false));

		SqlBuilderFactory.setDefaultEngine(BeeConst.PGSQL);
		builder = SqlBuilderFactory.getBuilder();

		SqlSelect select2 = new SqlSelect();
		select2.addFrom("table1");
		select2.addFields("table1", "field1");

		select2.setOffset(10);
		assertEquals("SELECT \"table1\".\"field1\" FROM \"table1\" OFFSET 10",
				select2.getSqlString(builder, false));

		select2.setLimit(10);
		assertEquals(
				"SELECT \"table1\".\"field1\" FROM \"table1\" LIMIT 10 OFFSET 10",
				select2.getSqlString(builder, false));

		SqlBuilderFactory.setDefaultEngine(BeeConst.ORACLE);
		builder = SqlBuilderFactory.getBuilder();

		SqlSelect select3 = new SqlSelect();
		select3.addFrom("table1");
		select3.addFields("table1", "field1");

		select3.setOffset(10);
		assertEquals(true,
				select3.getSqlString(builder, false).contains("SELECT "));
		assertEquals(
				true,
				select3.getSqlString(builder, false).contains(
						".* FROM (SELECT ROWNUM AS "));
		assertEquals(true, select3.getSqlString(builder, false).contains(", "));
		assertEquals(
				true,
				select3.getSqlString(builder, false)
						.contains(
								".* FROM (SELECT \"table1\".\"field1\" FROM \"table1\") "));
		assertEquals(true,
				select3.getSqlString(builder, false).contains(" > 10"));
		
		select3.setLimit(10);
		assertEquals(true,
				select3.getSqlString(builder, false).contains("SELECT "));
		assertEquals(
				true,
				select3.getSqlString(builder, false).contains(
						".* FROM (SELECT /*+ FIRST_ROWS(20) */ ROWNUM AS "));
		assertEquals(true, select3.getSqlString(builder, false).contains(", "));
		assertEquals(
				true,
				select3.getSqlString(builder, false)
						.contains(
								".* FROM (SELECT \"table1\".\"field1\" FROM \"table1\") "));
		assertEquals(
				true,
				select3.getSqlString(builder, false).contains(
						" WHERE ROWNUM <= 20) "));
		assertEquals(true,
				select3.getSqlString(builder, false).contains(" WHERE "));
		assertEquals(true,
				select3.getSqlString(builder, false).contains(" > 10"));

	}
	
	@Test
	public final void testAddEmptyBoolean() {
		SqlSelect sql;
		SqlBuilderFactory.setDefaultEngine(BeeConst.UNKNOWN);

		sql = new SqlSelect();
		sql.addFields("Table1", "field11");
		sql.addFrom("Table1");

		sql.addEmptyBoolean("bool1");

		assertEquals(
				"SELECT Table1.field11, CAST(0 AS BIT) AS bool1 FROM Table1",
				sql.getQuery());

		sql = new SqlSelect();
		sql.addFields("Table1", "field11");
		sql.addFrom("Table1");

		sql.addEmptyBoolean("");

		assertEquals("SELECT Table1.field11, CAST(0 AS BIT) FROM Table1",
				sql.getQuery());

		sql = new SqlSelect();
		sql.addFields("Table1", "field11");
		sql.addFrom("Table1");

		sql.addEmptyBoolean(null);

		assertEquals("SELECT Table1.field11, CAST(0 AS BIT) FROM Table1",
				sql.getQuery());
	}
}

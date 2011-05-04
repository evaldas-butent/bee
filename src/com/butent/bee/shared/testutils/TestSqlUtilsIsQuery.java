package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.sql.BeeConstants.Keyword;

/**
 * Tests {@link com.butent.bee.server.sql.SqlUtils}.
 */
public class TestSqlUtilsIsQuery {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testCreateForeignKey() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		assertEquals(
				"ALTER TABLE Table1 ADD CONSTRAINT name FOREIGN KEY (field1) REFERENCES refTable (refField1) ON DELETE CASCADE",
				SqlUtils.createForeignKey("Table1", "name", "field1",
						"refTable", "refField1", Keyword.CASCADE).getQuery());
		assertEquals(
				"ALTER TABLE Table1 ADD CONSTRAINT name FOREIGN KEY (field1) REFERENCES refTable (refField1)",
				SqlUtils.createForeignKey("Table1", "name", "field1",
						"refTable", "refField1", null).getQuery());
	}

	@Test
	public final void testCreateIndex() {

		SqlBuilderFactory.setDefaultEngine("Generic");

		assertEquals("CREATE INDEX indexName ON Table1 (field1, field2)",
				SqlUtils.createIndex("Table1", "indexName", "field1", "field2")
						.getQuery());
		assertEquals("CREATE INDEX indexName ON Table1 (field1)", SqlUtils
				.createIndex("Table1", "indexName", "field1").getQuery());
		assertEquals("CREATE INDEX indexName ON Table1 (indexName)", SqlUtils
				.createIndex("Table1", "indexName").getQuery());
		assertEquals("CREATE UNIQUE INDEX indexName ON Table1 (indexName)",
				SqlUtils.createUniqueIndex("Table1", "indexName").getQuery());

		try {
			assertEquals("CREATE INDEX indexName ON Table1 (indexName)",
					SqlUtils.createIndex("Table1", null).getQuery());
			assertEquals("CREATE INDEX indexName ON Table1 (indexName)",
					SqlUtils.createIndex(null, "indexName").getQuery());
			fail("BeeRuntimeException not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testCreateIndex():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testCreatePrimaryKey() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		assertEquals(
				"ALTER TABLE Table1 ADD CONSTRAINT constraint_name PRIMARY KEY (User_ID, Username)",
				SqlUtils.createPrimaryKey("Table1", "constraint_name",
						"User_ID", "Username").getQuery());
		assertEquals(
				"ALTER TABLE Table1 ADD CONSTRAINT constraint_name PRIMARY KEY (constraint_name)",
				SqlUtils.createPrimaryKey("Table1", "constraint_name")
						.getQuery());

		try {
			assertEquals(
					"ALTER TABLE Table1 ADD CONSTRAINT constraint_name PRIMARY KEY (User_ID, Username)",
					SqlUtils.createPrimaryKey("Table1", null).getQuery());
			assertEquals(
					"ALTER TABLE Table1 ADD CONSTRAINT constraint_name PRIMARY KEY (User_ID, Username)",
					SqlUtils.createPrimaryKey(null, null).getQuery());
			fail("BeeRuntimeException not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testCreateIndex():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testDBForeignKeys() {

		SqlBuilderFactory.setDefaultEngine("Generic");

		assertEquals(
				"SELECT c.constraint_name AS fkName, t.table_name AS fkTable, r.table_name AS fkRefTable FROM information_schema.referential_constraints c INNER JOIN information_schema.table_constraints t ON c.constraint_name=t.constraint_name INNER JOIN information_schema.table_constraints r ON c.unique_constraint_name=r.constraint_name WHERE c.constraint_catalog='MyDB' AND t.table_catalog='MyDB' AND c.constraint_schema='MyDbSchema' AND t.table_schema='MyDbSchema' AND t.table_name='Table1' AND r.table_name='RefTable1'",
				SqlUtils.dbForeignKeys("MyDB", "MyDbSchema", "Table1",
						"RefTable1").getQuery());
		assertEquals(
				"SELECT c.constraint_name AS fkName, t.table_name AS fkTable, r.table_name AS fkRefTable FROM information_schema.referential_constraints c INNER JOIN information_schema.table_constraints t ON c.constraint_name=t.constraint_name INNER JOIN information_schema.table_constraints r ON c.unique_constraint_name=r.constraint_name WHERE c.constraint_catalog='MyDB' AND t.table_catalog='MyDB' AND c.constraint_schema='MyDbSchema' AND t.table_schema='MyDbSchema' AND t.table_name='Table1'",
				SqlUtils.dbForeignKeys("MyDB", "MyDbSchema", "Table1", null)
						.getQuery());
		assertEquals(
				"SELECT c.constraint_name AS fkName, t.table_name AS fkTable, r.table_name AS fkRefTable FROM information_schema.referential_constraints c INNER JOIN information_schema.table_constraints t ON c.constraint_name=t.constraint_name INNER JOIN information_schema.table_constraints r ON c.unique_constraint_name=r.constraint_name WHERE c.constraint_catalog='MyDB' AND t.table_catalog='MyDB' AND c.constraint_schema='MyDbSchema' AND t.table_schema='MyDbSchema'",
				SqlUtils.dbForeignKeys("MyDB", "MyDbSchema", null, null)
						.getQuery());
		assertEquals(
				"SELECT c.constraint_name AS fkName, t.table_name AS fkTable, r.table_name AS fkRefTable FROM information_schema.referential_constraints c INNER JOIN information_schema.table_constraints t ON c.constraint_name=t.constraint_name INNER JOIN information_schema.table_constraints r ON c.unique_constraint_name=r.constraint_name WHERE c.constraint_catalog='MyDB' AND t.table_catalog='MyDB'",
				SqlUtils.dbForeignKeys("MyDB", null, null, null).getQuery());
		assertEquals(
				"SELECT c.constraint_name AS fkName, t.table_name AS fkTable, r.table_name AS fkRefTable FROM information_schema.referential_constraints c INNER JOIN information_schema.table_constraints t ON c.constraint_name=t.constraint_name INNER JOIN information_schema.table_constraints r ON c.unique_constraint_name=r.constraint_name",
				SqlUtils.dbForeignKeys(null, null, null, null).getQuery());

		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		assertEquals(
				"SELECT `c`.`constraint_name` AS `fkName`, `t`.`table_name` AS `fkTable`, `c`.`referenced_table_name` AS `fkRefTable` FROM `information_schema`.`referential_constraints` `c` INNER JOIN `information_schema`.`table_constraints` `t` ON `c`.`constraint_name`=`t`.`constraint_name` WHERE `c`.`constraint_catalog`='MyDB' AND `t`.`table_catalog`='MyDB' AND `c`.`constraint_schema`='MyDbSchema' AND `t`.`table_schema`='MyDbSchema' AND `t`.`table_name`='Table1' AND `c`.`referenced_table_name`='RefTable1'",
				SqlUtils.dbForeignKeys("MyDB", "MyDbSchema", "Table1",
						"RefTable1").getQuery());
		assertEquals(
				"SELECT `c`.`constraint_name` AS `fkName`, `t`.`table_name` AS `fkTable`, `c`.`referenced_table_name` AS `fkRefTable` FROM `information_schema`.`referential_constraints` `c` INNER JOIN `information_schema`.`table_constraints` `t` ON `c`.`constraint_name`=`t`.`constraint_name` WHERE `c`.`constraint_catalog`='MyDB' AND `t`.`table_catalog`='MyDB' AND `c`.`constraint_schema`='MyDbSchema' AND `t`.`table_schema`='MyDbSchema' AND `t`.`table_name`='Table1'",
				SqlUtils.dbForeignKeys("MyDB", "MyDbSchema", "Table1", null)
						.getQuery());
		assertEquals(
				"SELECT `c`.`constraint_name` AS `fkName`, `t`.`table_name` AS `fkTable`, `c`.`referenced_table_name` AS `fkRefTable` FROM `information_schema`.`referential_constraints` `c` INNER JOIN `information_schema`.`table_constraints` `t` ON `c`.`constraint_name`=`t`.`constraint_name` WHERE `c`.`constraint_catalog`='MyDB' AND `t`.`table_catalog`='MyDB' AND `c`.`constraint_schema`='MyDbSchema' AND `t`.`table_schema`='MyDbSchema'",
				SqlUtils.dbForeignKeys("MyDB", "MyDbSchema", null, null)
						.getQuery());
		assertEquals(
				"SELECT `c`.`constraint_name` AS `fkName`, `t`.`table_name` AS `fkTable`, `c`.`referenced_table_name` AS `fkRefTable` FROM `information_schema`.`referential_constraints` `c` INNER JOIN `information_schema`.`table_constraints` `t` ON `c`.`constraint_name`=`t`.`constraint_name` WHERE `c`.`constraint_catalog`='MyDB' AND `t`.`table_catalog`='MyDB'",
				SqlUtils.dbForeignKeys("MyDB", null, null, null).getQuery());
		assertEquals(
				"SELECT `c`.`constraint_name` AS `fkName`, `t`.`table_name` AS `fkTable`, `c`.`referenced_table_name` AS `fkRefTable` FROM `information_schema`.`referential_constraints` `c` INNER JOIN `information_schema`.`table_constraints` `t` ON `c`.`constraint_name`=`t`.`constraint_name`",
				SqlUtils.dbForeignKeys(null, null, null, null).getQuery());

		SqlBuilderFactory.setDefaultEngine(BeeConst.ORACLE);

		assertEquals(
				"SELECT \"c\".\"CONSTRAINT_NAME\" AS \"fkName\", \"c\".\"TABLE_NAME\" AS \"fkTable\", \"r\".\"TABLE_NAME\" AS \"fkRefTable\" FROM \"ALL_CONSTRAINTS\" \"c\" INNER JOIN \"ALL_CONSTRAINTS\" \"r\" ON \"c\".\"R_CONSTRAINT_NAME\"=\"r\".\"CONSTRAINT_NAME\" WHERE \"c\".\"CONSTRAINT_TYPE\"='R' AND \"c\".\"OWNER\"='MyDbSchema' AND \"r\".\"OWNER\"='MyDbSchema' AND \"c\".\"TABLE_NAME\"='Table1' AND \"r\".\"TABLE_NAME\"='RefTable1'",
				SqlUtils.dbForeignKeys("MyDB", "MyDbSchema", "Table1",
						"RefTable1").getQuery());
		assertEquals(
				"SELECT \"c\".\"CONSTRAINT_NAME\" AS \"fkName\", \"c\".\"TABLE_NAME\" AS \"fkTable\", \"r\".\"TABLE_NAME\" AS \"fkRefTable\" FROM \"ALL_CONSTRAINTS\" \"c\" INNER JOIN \"ALL_CONSTRAINTS\" \"r\" ON \"c\".\"R_CONSTRAINT_NAME\"=\"r\".\"CONSTRAINT_NAME\" WHERE \"c\".\"CONSTRAINT_TYPE\"='R' AND \"c\".\"OWNER\"='MyDbSchema' AND \"r\".\"OWNER\"='MyDbSchema' AND \"c\".\"TABLE_NAME\"='Table1'",
				SqlUtils.dbForeignKeys("MyDB", "MyDbSchema", "Table1", null)
						.getQuery());
		assertEquals(
				"SELECT \"c\".\"CONSTRAINT_NAME\" AS \"fkName\", \"c\".\"TABLE_NAME\" AS \"fkTable\", \"r\".\"TABLE_NAME\" AS \"fkRefTable\" FROM \"ALL_CONSTRAINTS\" \"c\" INNER JOIN \"ALL_CONSTRAINTS\" \"r\" ON \"c\".\"R_CONSTRAINT_NAME\"=\"r\".\"CONSTRAINT_NAME\" WHERE \"c\".\"CONSTRAINT_TYPE\"='R' AND \"c\".\"OWNER\"='MyDbSchema' AND \"r\".\"OWNER\"='MyDbSchema'",
				SqlUtils.dbForeignKeys("MyDB", "MyDbSchema", null, null)
						.getQuery());
		assertEquals(
				"SELECT \"c\".\"CONSTRAINT_NAME\" AS \"fkName\", \"c\".\"TABLE_NAME\" AS \"fkTable\", \"r\".\"TABLE_NAME\" AS \"fkRefTable\" FROM \"ALL_CONSTRAINTS\" \"c\" INNER JOIN \"ALL_CONSTRAINTS\" \"r\" ON \"c\".\"R_CONSTRAINT_NAME\"=\"r\".\"CONSTRAINT_NAME\" WHERE \"c\".\"CONSTRAINT_TYPE\"='R'",
				SqlUtils.dbForeignKeys("MyDB", null, null, null).getQuery());
		assertEquals(
				"SELECT \"c\".\"CONSTRAINT_NAME\" AS \"fkName\", \"c\".\"TABLE_NAME\" AS \"fkTable\", \"r\".\"TABLE_NAME\" AS \"fkRefTable\" FROM \"ALL_CONSTRAINTS\" \"c\" INNER JOIN \"ALL_CONSTRAINTS\" \"r\" ON \"c\".\"R_CONSTRAINT_NAME\"=\"r\".\"CONSTRAINT_NAME\" WHERE \"c\".\"CONSTRAINT_TYPE\"='R'",
				SqlUtils.dbForeignKeys(null, null, null, null).getQuery());
	}

	@Test
	public final void testDbName() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		assertEquals("", SqlUtils.dbName().getQuery());
		SqlBuilderFactory.setDefaultEngine(BeeConst.PGSQL);
		assertEquals("SELECT current_database() as \"dbName\"", SqlUtils
				.dbName().getQuery());
		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		assertEquals("", SqlUtils.dbName().getQuery());
		SqlBuilderFactory.setDefaultEngine(BeeConst.MSSQL);
		assertEquals("SELECT db_name() AS [dbName]", SqlUtils.dbName()
				.getQuery());
	}

	@Test
	public final void testDbSchema() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		assertEquals("", SqlUtils.dbSchema().getQuery());
		SqlBuilderFactory.setDefaultEngine(BeeConst.PGSQL);
		assertEquals("SELECT current_schema() as \"dbSchema\"", SqlUtils
				.dbSchema().getQuery());
		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		assertEquals("SELECT schema() AS `dbSchema`", SqlUtils.dbSchema()
				.getQuery());
		SqlBuilderFactory.setDefaultEngine(BeeConst.MSSQL);
		assertEquals("SELECT schema_name() AS [dbSchema]", SqlUtils.dbSchema()
				.getQuery());

		SqlBuilderFactory.setDefaultEngine(BeeConst.ORACLE);
		assertEquals(
				"SELECT sys_context('USERENV', 'CURRENT_SCHEMA') AS \"dbSchema\" FROM dual",
				SqlUtils.dbSchema().getQuery());
	}

	@Test
	public final void testDbTables() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		assertEquals(
				"SELECT t.table_name FROM information_schema.tables t WHERE t.table_catalog='MyDB' AND t.table_schema='MyDBSchema' AND t.table_name='table1'",
				SqlUtils.dbTables("MyDB", "MyDBSchema", "table1").getQuery());
		assertEquals(
				"SELECT t.table_name FROM information_schema.tables t WHERE t.table_catalog='MyDB' AND t.table_schema='MyDBSchema'",
				SqlUtils.dbTables("MyDB", "MyDBSchema", null).getQuery());
		assertEquals(
				"SELECT t.table_name FROM information_schema.tables t WHERE t.table_catalog='MyDB'",
				SqlUtils.dbTables("MyDB", null, null).getQuery());
		assertEquals("SELECT t.table_name FROM information_schema.tables t",
				SqlUtils.dbTables(null, null, null).getQuery());
		SqlBuilderFactory.setDefaultEngine(BeeConst.PGSQL);
		assertEquals(
				"SELECT \"t\".\"table_name\" FROM \"information_schema\".\"tables\" \"t\" WHERE \"t\".\"table_catalog\"='MyDB' AND \"t\".\"table_schema\"='MyDBSchema' AND \"t\".\"table_name\"='table1'",
				SqlUtils.dbTables("MyDB", "MyDBSchema", "table1").getQuery());
		assertEquals(
				"SELECT \"t\".\"table_name\" FROM \"information_schema\".\"tables\" \"t\" WHERE \"t\".\"table_catalog\"='MyDB' AND \"t\".\"table_schema\"='MyDBSchema'",
				SqlUtils.dbTables("MyDB", "MyDBSchema", null).getQuery());
		assertEquals(
				"SELECT \"t\".\"table_name\" FROM \"information_schema\".\"tables\" \"t\" WHERE \"t\".\"table_catalog\"='MyDB'",
				SqlUtils.dbTables("MyDB", null, null).getQuery());
		assertEquals(
				"SELECT \"t\".\"table_name\" FROM \"information_schema\".\"tables\" \"t\"",
				SqlUtils.dbTables(null, null, null).getQuery());
		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		assertEquals("SHOW TABLES IN `MyDBSchema` LIKE 'table1'", SqlUtils
				.dbTables("MyDB", "MyDBSchema", "table1").getQuery());
		assertEquals("SHOW TABLES IN `MyDBSchema`",
				SqlUtils.dbTables("MyDB", "MyDBSchema", null).getQuery());
		assertEquals("SHOW TABLES", SqlUtils.dbTables("MyDB", null, null)
				.getQuery());
		assertEquals("SHOW TABLES", SqlUtils.dbTables(null, null, null)
				.getQuery());
		SqlBuilderFactory.setDefaultEngine(BeeConst.ORACLE);
		assertEquals(
				"SELECT \"t\".\"TABLE_NAME\" FROM \"ALL_TABLES\" \"t\" WHERE \"t\".\"OWNER\"='MyDBSchema' AND \"t\".\"TABLE_NAME\"='table1'",
				SqlUtils.dbTables("MyDB", "MyDBSchema", "table1").getQuery());
		assertEquals(
				"SELECT \"t\".\"TABLE_NAME\" FROM \"ALL_TABLES\" \"t\" WHERE \"t\".\"OWNER\"='MyDBSchema'",
				SqlUtils.dbTables("MyDB", "MyDBSchema", null).getQuery());
		assertEquals("SELECT \"t\".\"TABLE_NAME\" FROM \"ALL_TABLES\" \"t\"",
				SqlUtils.dbTables("MyDB", null, null).getQuery());
		assertEquals("SELECT \"t\".\"TABLE_NAME\" FROM \"ALL_TABLES\" \"t\"",
				SqlUtils.dbTables(null, null, null).getQuery());
	}

	@Test
	public final void testDropForeignKey() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.UNKNOWN);
		assertEquals("ALTER TABLE Table1 DROP CONSTRAINT foreignkey_name",
				SqlUtils.dropForeignKey("Table1", "foreignkey_name").getQuery());

		try {
			assertEquals("ALTER TABLE Table1 DROP FOREIGN KEY", SqlUtils
					.dropForeignKey("Table1", null).getQuery());
			assertEquals("ALTER TABLE Table1 DROP FOREIGN KEY", SqlUtils
					.dropForeignKey(null, null).getQuery());
			fail("BeeRuntimeException not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testCreateIndex():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}

		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		SqlBuilder builder = SqlBuilderFactory.getBuilder();
		assertEquals("ALTER TABLE `Table1` DROP FOREIGN KEY `foreignkey_name`",
				SqlUtils.dropForeignKey("Table1", "foreignkey_name")
						.getSqlString(builder, false));

		try {
			assertEquals("ALTER TABLE Table1 DROP FOREIGN KEY foreignkey_name",
					SqlUtils.dropForeignKey("Table1", null).getQuery());
			assertEquals("ALTER TABLE Table1 DROP FOREIGN KEY foreignkey_name",
					SqlUtils.dropForeignKey(null, null).getQuery());
			fail("BeeRuntimeException not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testCreateIndex():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}
	}

	@Test
	public final void testDropTable() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		assertEquals("DROP TABLE `Table1`", SqlUtils.dropTable("Table1")
				.getQuery());
		try {
			assertEquals("DROP TABLE `Table1`", SqlUtils.dropTable(null)
					.getQuery());
			fail("BeeRuntimeException not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
			System.out.println("public final void testCreateIndex():"
					+ e.getMessage());
		} catch (Exception e) {
			fail("Java runtime error. Need BeeRuntimeException !!!");
		}

	}

	@Test
	public void testTemporaryName() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.MSSQL);
		SqlBuilder builder = SqlBuilderFactory.getBuilder();
		SqlSelect select = new SqlSelect();
		select.setWhere(SqlUtils.contains(
				SqlUtils.constant(SqlUtils.temporaryName("temp value")), "5"));
		select.addFrom("Source_table");
		select.addFields("Source_table", "name");
		assertEquals(
				"SELECT [Source_table].[name] FROM [Source_table] WHERE '#temp value' LIKE '%5%'",
				select.getSqlString(builder, false));
	}
}

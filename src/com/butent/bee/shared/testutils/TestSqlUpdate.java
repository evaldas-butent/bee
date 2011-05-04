package com.butent.bee.shared.testutils;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;

/**
 * Tests {@link com.butent.bee.server.sql.SqlUpdate}
 */
public class TestSqlUpdate {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public final void testGetSqlString(){
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();
		
		SqlUpdate update = new SqlUpdate("Source1");
		update.addConstant("field1", "value1");
		update.addConstant("field2", "value2");
		assertEquals("UPDATE Source1 SET field1='value1', field2='value2'",update.getSqlString(builder, false));
		
		update.reset();
		IsExpression expr = SqlUtils.field("Sourceexpr", "fieldexpr");
		update.addExpression("field1", expr);
		
		assertEquals("UPDATE Source1 SET field1=Sourceexpr.fieldexpr",update.getSqlString(builder, false));

		SqlUpdate update2 = new SqlUpdate("Source1", "alias1");
		IsCondition where = SqlUtils.equal("Source1", "name", "John");
		update2.addConstant("name", "Petras");
		update2.setWhere(where);
		
		assertEquals("UPDATE Source1 alias1 SET name='Petras' WHERE Source1.name='John'",update2.getSqlString(builder, false));

		SqlUpdate update3 = new SqlUpdate("Source1", "alias1");
		IsCondition where2 = SqlUtils.equal("Source2", "name", "John");
		update3.addConstant("name", "Petras");
		update3.setWhere(where2);
		update3.addFrom("Source5");
		update3.addFrom("Source4");
		update3.addFrom("Source3");
		SqlSelect select = new SqlSelect();
		select.addFields("Table1", "field1", "field2");
		select.addFrom("InFrom");
		select.setWhere(SqlUtils.contains(SqlUtils.expression(SqlUtils.name("field2")), "naikinamas_irasas"));
		update3.addFrom(select, "selAlias");

		assertEquals("UPDATE Source1 alias1 SET name=? FROM Source5, Source4, Source3, (SELECT Table1.field1, Table1.field2 FROM InFrom WHERE field2 LIKE ?) selAlias WHERE Source2.name=?",update3.getSqlString(builder, true));
		
		Object[] a = update3.getSqlParams().toArray();
		Object[] expected = {"Petras", "%naikinamas_irasas%", "John"};
		
		assertArrayEquals(expected, a);	
	}
	
	
	
	@Test
	public final void testGetSources(){
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();
		SqlUpdate update2 = new SqlUpdate("Source1", "alias1");
		IsCondition where = SqlUtils.equal("Source2", "name", "John");
		update2.addConstant("name", "Petras");
		update2.setWhere(where);
		update2.addFrom("Source5");
		update2.addFrom("Source4");
		update2.addFrom("Source3");
			
		Object[] arr = update2.getSources().toArray();
		Object[] expected = { "Source5","Source4","Source3","Source1"};
		
		assertArrayEquals(expected, arr);
		
		assertEquals("UPDATE Source1 alias1 SET name=? FROM Source5, Source4, Source3 WHERE Source2.name=?",update2.getSqlString(builder, true));
	}
	
	@Test
	public final void testGetSqlParams(){
		SqlUpdate update2 = new SqlUpdate("Source1", "alias1");
		IsCondition where = SqlUtils.equal("Source2", "name", "John");
		update2.addConstant("name", "Petras");
		update2.setWhere(where);
		update2.addFrom("Source5");
		
		Object[] arr = update2.getSqlParams().toArray();
		Object[] expected = { "Petras", "John"};
		
		assertArrayEquals(expected, arr);
	}
	
	@Test
	public final void testIsEmpty()
	{
		SqlUpdate update = new SqlUpdate("target", "trg");
		assertTrue(update.isEmpty()); // nes neturi BeeUtils nepalaiko isFrom, bet tikrina ar jis nera NULL ;
		
		update.addExpression("Field1", SqlUtils.constant("hello"));
		
		assertFalse(update.isEmpty());
		
		update = new SqlUpdate ("Table1");
		assertTrue (update.isEmpty());	
	}
}

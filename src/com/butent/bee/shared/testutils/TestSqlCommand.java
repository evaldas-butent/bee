package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.server.sql.*;

public class TestSqlCommand {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testSqlCommand() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();
		
		IsExpression e = SqlUtils.bitAnd(SqlUtils.bitAnd("Table1", "field1", "value"), "value2");
		assertEquals ("((Table1.field1&value)&value2)" , e.getSqlString(builder, false));
	}
}

package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.TimeOfDayValue;
import com.butent.bee.shared.data.value.ValueType;

public class TestValueType {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGetByTypeCode(){
	     assertEquals(ValueType.BOOLEAN, ValueType.getByTypeCode("boolean"));
	     assertEquals(ValueType.DATETIME, ValueType.getByTypeCode("Datetime"));	
	     assertEquals(ValueType.DATE, ValueType.getByTypeCode(" date "));	
	     assertEquals(ValueType.NUMBER, ValueType.getByTypeCode("Number"));
	     assertEquals(null, ValueType.getByTypeCode("TEXT"));
	     assertEquals(ValueType.TEXT, ValueType.getByTypeCode("STRING"));
	     assertEquals(ValueType.TIMEOFDAY, ValueType.getByTypeCode("timeofday"));	
	     assertEquals(null, ValueType.getByTypeCode(null));	
	}
	
	@Test
	public final void testIsNumber(){
		assertEquals(false, ValueType.isNumber(ValueType.getByTypeCode("boolean")));
		assertEquals(false, ValueType.isNumber(null));
		assertEquals(true, ValueType.isNumber(ValueType.getByTypeCode("number")));
	}
	
	@Test
	public final void testCreateValue(){ 
		assertEquals(BooleanValue.getNullValue(), ValueType.valueOf("BOOLEAN").createValue(null));
		assertEquals(new TextValue("String inside"), ValueType.valueOf("TEXT").createValue("String inside"));
		assertEquals(new NumberValue(5.0), ValueType.valueOf("NUMBER").createValue(5.0));
		assertEquals(BooleanValue.TRUE, ValueType.valueOf("BOOLEAN").createValue(true));
		assertEquals(BooleanValue.FALSE, ValueType.valueOf("BOOLEAN").createValue(false));
		assertEquals(new DateValue(2011,02,22), ValueType.valueOf("DATE").createValue(new DateTime(1298362388227L)));
		assertEquals(new DateTimeValue(2011,02,22,10,13,8), ValueType.valueOf("DATETIME").createValue(new DateTime(2011,02,22,10,13,8)));
		assertEquals(new DateTimeValue(2011,02,22,10,13,8), ValueType.valueOf("DATETIME").createValue(new DateTime(2011,02,22,10,13,8)));
		assertEquals(new TimeOfDayValue(10,13,8,227), ValueType.valueOf("TIMEOFDAY").createValue(new DateTime(1298362388227L)));
	}
	
	@Test
	public final void testGetTypeCode(){
		assertEquals("boolean",	ValueType.valueOf("BOOLEAN").getTypeCode());
		assertEquals("number",	ValueType.valueOf("NUMBER").getTypeCode());
		assertEquals("string",	ValueType.valueOf("TEXT").getTypeCode());
	}
}

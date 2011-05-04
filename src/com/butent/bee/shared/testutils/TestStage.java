package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.Stage;

/**
 * Tests {@link com.butent.bee.shared.Stage}
 */
public class TestStage {

	private Stage Stage1 ;
	private Stage Stage2;

	@Before
	public void setUp() throws Exception {
		Stage1 = new Stage("ServiceA", "Stage1of1");
		Stage2 = new Stage ();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGetService() {
		assertEquals ("ServiceA", Stage1.getService());
		assertNull (Stage2.getService());
	}

	@Test
	public final void testGetStage() {
		assertEquals ("Stage1of1", Stage1.getStage());
		assertNull(Stage2.getStage());
	}

	
	@Test
	public final void testSetService() {
		Stage1.setService("svc");
		assertEquals ("svc", Stage1.getService());
		
		Stage2.setService("");
		assertEquals ("", Stage2.getService());
		
		Stage1.setService(null);
		assertNull (Stage1.getService());
	}

	@Test
	public final void testSetStage() {
		Stage1.setStage("aaa");
		assertEquals ("aaa", Stage1.getStage());
		
		Stage1.setStage(null);
		assertNull(Stage1.getStage());
		
		Stage2.setStage("ddd");
		assertEquals("ddd", Stage2.getStage());	  
	}
}

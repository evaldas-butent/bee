package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.exceptions.BeeRuntimeException;

public class TestService {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testIsCompositeService() {
		try {
			Service.isCompositeService("");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isCompositeService("\t \r \r");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isCompositeService(null);
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		assertTrue(Service.isCompositeService("comp_"));
		assertTrue(Service.isCompositeService("comp_complex"));
		assertTrue(Service.isCompositeService("comp_win"));

		assertFalse(Service.isCompositeService("\t\t\t\r comp_"));
		assertFalse(Service.isCompositeService("co"));
		assertFalse(Service.isCompositeService("company"));
		assertFalse(Service.isCompositeService("acomp_"));
		assertFalse(Service.isCompositeService("a comp_"));

	}

	@Test
	public final void testIsDataService() {
		try {
			Service.isDataService("");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isDataService("\t \r \r");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isDataService(null);
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		assertTrue(Service.isDataService("rpc_data_"));
		assertTrue(Service.isDataService("rpc_data_complex"));
		assertTrue(Service.isDataService("rpc_data_win"));

		assertFalse(Service.isDataService("\t\t\t\r rpc_data_"));
		assertFalse(Service.isDataService("rpc_"));
		assertFalse(Service.isDataService("rpc_dataada"));
		assertFalse(Service.isDataService("arpc_data_"));
		assertFalse(Service.isDataService("a rpc_data_"));
	}

	@Test
	public final void testIsDbMetaService() {
		try {
			Service.isDbMetaService("");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isDbMetaService("\t \r \r");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isDbMetaService(null);
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		assertTrue(Service.isDbMetaService("rpc_db_meta_"));
		assertTrue(Service.isDbMetaService("rpc_db_meta__complex"));
		assertTrue(Service.isDbMetaService("rpc_db_meta_win"));

		assertFalse(Service.isDbMetaService("\t\t\t\r rpc_db_meta_"));
		assertFalse(Service.isDbMetaService("rpc_"));
		assertFalse(Service.isDbMetaService("rpc_db_metaetabeta"));
		assertFalse(Service.isDbMetaService("arpc_db_meta_"));
		assertFalse(Service.isDbMetaService("a rpc_db_meta_"));
	}

	@Test
	public final void testIsDbService() {
		try {
			Service.isDbService("");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isDbService("\t \r \r");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isDbService(null);
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		assertTrue(Service.isDbService("rpc_db_"));
		assertTrue(Service.isDbService("rpc_db__complex"));
		assertTrue(Service.isDbService("rpc_db_win"));

		assertFalse(Service.isDbService("\t\t\t\r rpc_db_"));
		assertFalse(Service.isDbService("rpc_"));
		assertFalse(Service.isDbService("rpc_dbmetaetabeta"));
		assertFalse(Service.isDbService("arpc_db_"));
		assertFalse(Service.isDbService("a rpc_db_"));
	}

	@Test
	public final void testIsInvocation() {

		assertFalse(Service.isInvocation(""));

		assertFalse(Service.isInvocation("\t \r \r"));

		assertFalse(Service.isInvocation(null));

		assertTrue(Service.isInvocation("rpc_invoke"));
		assertTrue(Service.isInvocation("rpc_iNvoke"));
		assertTrue(Service.isInvocation("rpc_invoke           "));
		assertTrue(Service.isInvocation("             rpc_invoke"));
		assertTrue(Service.isInvocation("\0\0\0 rpc_invoke"));
		assertTrue(Service.isInvocation("rpc_INVoke "));

		assertFalse(Service.isInvocation("\t\t\t\r rpc_invokre"));
		assertFalse(Service.isInvocation("rpc_"));
		assertFalse(Service.isInvocation("r\0pc_invokess"));
		assertFalse(Service.isInvocation("arpc_inv\0oke"));
		assertFalse(Service.isInvocation("a rpc_inveoke"));
	}

	@Test
	public final void testIsRpcService() {
		try {
			Service.isRpcService("");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isRpcService("\t \r \r");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isRpcService(null);
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		assertTrue(Service.isRpcService("rpc_db_"));
		assertTrue(Service.isRpcService("rpc_db__complex"));
		assertTrue(Service.isRpcService("rpc_db_win"));
		assertTrue(Service.isRpcService("rpc_"));

		assertFalse(Service.isRpcService("\t\t\t\r rpc_db_"));
		assertFalse(Service.isRpcService("rpc"));
		assertFalse(Service.isRpcService("rpcdbmetaetabeta"));
		assertFalse(Service.isRpcService("arpc_db_"));
		assertFalse(Service.isRpcService("a rpc_"));
	}

	@Test
	public final void testIsSysService() {
		try {
			Service.isSysService("");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isSysService("\t \r \r");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isSysService(null);
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		assertTrue(Service.isSysService("rpc_sys_"));
		assertTrue(Service.isSysService("rpc_sys__complex"));
		assertTrue(Service.isSysService("rpc_sys_win"));
		assertTrue(Service.isSysService("rpc_sys_"));

		assertFalse(Service.isSysService("\t\t\t\r rpc_sys_"));
		assertFalse(Service.isSysService("rpc"));
		assertFalse(Service.isSysService("rpc_sysdbmetaetabeta"));
		assertFalse(Service.isSysService("arpc_sys_"));
		assertFalse(Service.isSysService("a rpc_sys_"));
	}

	@Test
	public final void testIsUiService() {
		try {
			Service.isUiService("");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isUiService("\t \r \r");
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			Service.isUiService(null);
			fail("exception not works");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		assertTrue(Service.isUiService("ui_"));
		assertTrue(Service.isUiService("ui__complex"));
		assertTrue(Service.isUiService("ui_win"));
		assertTrue(Service.isUiService("ui_"));

		assertFalse(Service.isUiService("\t\t\t\r ui_"));
		assertFalse(Service.isUiService("ui"));
		assertFalse(Service.isUiService("uisysdbmetaetabeta"));
		assertFalse(Service.isUiService("aui_"));
		assertFalse(Service.isUiService("a ui_"));
	}

}

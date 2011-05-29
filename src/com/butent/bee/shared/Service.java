package com.butent.bee.shared;

import com.butent.bee.shared.utils.BeeUtils;

/**
 * Contains service tag descriptions and type identification methods.
 */

public class Service {

  public static final String RPC_SERVICE_PREFIX = "rpc_";
  public static final String UI_SERVICE_PREFIX = "ui_";
  public static final String COMPOSITE_SERVICE_PREFIX = "comp_";

  public static final String DB_SERVICE_PREFIX = RPC_SERVICE_PREFIX + "db_";
  public static final String DB_JDBC = DB_SERVICE_PREFIX + "jdbc";

  public static final String DB_META_SERVICE_PREFIX = DB_SERVICE_PREFIX + "meta_";
  public static final String DB_PING = DB_META_SERVICE_PREFIX + "ping";
  public static final String DB_INFO = DB_META_SERVICE_PREFIX + "info";
  public static final String DB_TABLES = DB_META_SERVICE_PREFIX + "tables";
  public static final String DB_KEYS = DB_META_SERVICE_PREFIX + "keys";
  public static final String DB_PRIMARY = DB_META_SERVICE_PREFIX + "primary";

  public static final String SYS_SERVICE_PREFIX = RPC_SERVICE_PREFIX + "sys_";
  public static final String GET_CLASS_INFO = SYS_SERVICE_PREFIX + "class_info";
  public static final String GET_XML_INFO = SYS_SERVICE_PREFIX + "xml_info";
  public static final String GET_RESOURCE = SYS_SERVICE_PREFIX + "get_resource";
  public static final String SAVE_RESOURCE = SYS_SERVICE_PREFIX + "save_resource";
  public static final String GET_DIGEST = SYS_SERVICE_PREFIX + "get_digest";

  public static final String INVOKE = RPC_SERVICE_PREFIX + "invoke";

  public static final String GET_LOGIN = COMPOSITE_SERVICE_PREFIX + "get_login";
  public static final String LOGIN = RPC_SERVICE_PREFIX + "login";
  public static final String LOGOUT = RPC_SERVICE_PREFIX + "logout";

  public static final String LOAD_MENU = RPC_SERVICE_PREFIX + "load_menu";

  public static final String WHERE_AM_I = RPC_SERVICE_PREFIX + "where_am_i";

  public static final String CLOSE_DIALOG = UI_SERVICE_PREFIX + "close_dialog";
  public static final String CONFIRM_DIALOG = UI_SERVICE_PREFIX + "confirm_dialog";
  public static final String CANCEL_DIALOG = UI_SERVICE_PREFIX + "cancel_dialog";
  public static final String REFRESH_MENU = UI_SERVICE_PREFIX + "refresh_menu";

  public static final String GET_CLASS = COMPOSITE_SERVICE_PREFIX + "get_class";
  public static final String GET_XML = COMPOSITE_SERVICE_PREFIX + "get_xml";
  public static final String GET_DATA = COMPOSITE_SERVICE_PREFIX + "get_data";

  public static final String DATA_SERVICE_PREFIX = RPC_SERVICE_PREFIX + "data_";
  public static final String GET_FORM = DATA_SERVICE_PREFIX + "form";
  public static final String GET_FORM_LIST = DATA_SERVICE_PREFIX + "form_list";
  public static final String GET_MENU = DATA_SERVICE_PREFIX + "menu";
  public static final String GET_GRID = DATA_SERVICE_PREFIX + "grid";
  public static final String REBUILD = DATA_SERVICE_PREFIX + "rebuild";
  public static final String DO_SQL = DATA_SERVICE_PREFIX + "do_sql";
  public static final String GET_TABLE_LIST = DATA_SERVICE_PREFIX + "table_list";
  public static final String QUERY = DATA_SERVICE_PREFIX + "query";
  public static final String GET_STATES = DATA_SERVICE_PREFIX + "states";
  public static final String GET_STATE_TABLE = DATA_SERVICE_PREFIX + "state_table";
  public static final String COMMIT = DATA_SERVICE_PREFIX + "commit";
  public static final String GET_VIEW_LIST = DATA_SERVICE_PREFIX + "view_list";
  public static final String GENERATE = DATA_SERVICE_PREFIX + "generate";
  public static final String COUNT_ROWS = DATA_SERVICE_PREFIX + "row_count";
  public static final String DELETE_ROWS = DATA_SERVICE_PREFIX + "delete_rows";
  public static final String UPDATE_CELL = DATA_SERVICE_PREFIX + "update_cell";
  public static final String UPDATE_ROW = DATA_SERVICE_PREFIX + "update_row";

  public static final String RPC_VAR_PREFIX = "bee_";
  public static final String RPC_VAR_SYS_PREFIX = RPC_VAR_PREFIX + "sys_";

  public static final String RPC_VAR_SVC = RPC_VAR_SYS_PREFIX + "svc";
  public static final String RPC_VAR_QID = RPC_VAR_SYS_PREFIX + "qid";
  public static final String RPC_VAR_SID = RPC_VAR_SYS_PREFIX + "sid";
  public static final String RPC_VAR_DSN = RPC_VAR_SYS_PREFIX + "dsn";
  public static final String RPC_VAR_SEP = RPC_VAR_SYS_PREFIX + "sep";
  public static final String RPC_VAR_OPT = RPC_VAR_SYS_PREFIX + "opt";
  public static final String RPC_VAR_LOC = RPC_VAR_SYS_PREFIX + "loc";

  public static final String RPC_VAR_CNT = RPC_VAR_SYS_PREFIX + "cnt";
  public static final String RPC_VAR_COLS = RPC_VAR_SYS_PREFIX + "c_c";
  public static final String RPC_VAR_ROWS = RPC_VAR_SYS_PREFIX + "r_c";
  public static final String RPC_VAR_CTP = RPC_VAR_SYS_PREFIX + "ctp";
  public static final String RPC_VAR_URI = RPC_VAR_SYS_PREFIX + "uri";
  public static final String RPC_VAR_MD5 = RPC_VAR_SYS_PREFIX + "md5";

  public static final String RPC_VAR_MSG_CNT = RPC_VAR_SYS_PREFIX + "m_c";
  public static final String RPC_VAR_MSG = RPC_VAR_SYS_PREFIX + "msg";
  public static final String RPC_VAR_PRM_CNT = RPC_VAR_SYS_PREFIX + "p_c";
  public static final String RPC_VAR_PRM = RPC_VAR_SYS_PREFIX + "prm";
  public static final String RPC_VAR_PART_CNT = RPC_VAR_SYS_PREFIX + "x_c";
  public static final String RPC_VAR_PART = RPC_VAR_SYS_PREFIX + "part";

  public static final String RPC_VAR_METH = RPC_VAR_SYS_PREFIX + "meth";

  public static final String VAR_LOGIN = RPC_VAR_PREFIX + "login";
  public static final String VAR_PASSWORD = RPC_VAR_PREFIX + "password";
  public static final String VAR_USER_SIGN = RPC_VAR_PREFIX + "user_sign";

  public static final String VAR_CLASS_NAME = RPC_VAR_PREFIX + "class_name";
  public static final String VAR_PACKAGE_LIST = RPC_VAR_PREFIX + "package_list";
  public static final String VAR_FILE_NAME = RPC_VAR_PREFIX + "file_name";

  public static final String VAR_XML_SOURCE = RPC_VAR_PREFIX + "xml_source";
  public static final String VAR_XML_TRANSFORM = RPC_VAR_PREFIX + "xml_transform";
  public static final String VAR_XML_TARGET = RPC_VAR_PREFIX + "xml_target";
  public static final String VAR_XML_RETURN = RPC_VAR_PREFIX + "xml_return";

  public static final String VAR_JDBC_QUERY = RPC_VAR_PREFIX + "jdbc_query";
  public static final String VAR_CONNECTION_AUTO_COMMIT = RPC_VAR_PREFIX + "conn_auto_commit";
  public static final String VAR_CONNECTION_READ_ONLY = RPC_VAR_PREFIX + "conn_read_only";
  public static final String VAR_CONNECTION_HOLDABILITY = RPC_VAR_PREFIX + "conn_holdability";
  public static final String VAR_CONNECTION_TRANSACTION_ISOLATION = RPC_VAR_PREFIX + "conn_ti";
  public static final String VAR_STATEMENT_CURSOR_NAME = RPC_VAR_PREFIX + "stmt_cursor";
  public static final String VAR_STATEMENT_ESCAPE_PROCESSING = RPC_VAR_PREFIX + "stmt_escape";
  public static final String VAR_STATEMENT_FETCH_DIRECTION = RPC_VAR_PREFIX + "stmt_fetch_dir";
  public static final String VAR_STATEMENT_FETCH_SIZE = RPC_VAR_PREFIX + "stmt_fetch_size";
  public static final String VAR_STATEMENT_MAX_FIELD_SIZE = RPC_VAR_PREFIX + "stmt_field_size";
  public static final String VAR_STATEMENT_MAX_ROWS = RPC_VAR_PREFIX + "stmt_max_rows";
  public static final String VAR_STATEMENT_POOLABLE = RPC_VAR_PREFIX + "stmt_poolable";
  public static final String VAR_STATEMENT_QUERY_TIMEOUT = RPC_VAR_PREFIX + "stmt_timeout";
  public static final String VAR_STATEMENT_RS_TYPE = RPC_VAR_PREFIX + "stmt_rs_type";
  public static final String VAR_STATEMENT_RS_CONCURRENCY = RPC_VAR_PREFIX + "stmt_rs_concurrency";
  public static final String VAR_STATEMENT_RS_HOLDABILITY = RPC_VAR_PREFIX + "stmt_rs_holdability";
  public static final String VAR_RESULT_SET_FETCH_DIRECTION = RPC_VAR_PREFIX + "rs_fetch_dir";
  public static final String VAR_RESULT_SET_FETCH_SIZE = RPC_VAR_PREFIX + "rs_fetch_size";
  public static final String VAR_JDBC_RETURN = RPC_VAR_PREFIX + "jdbc_return";

  public static final String VAR_VIEW_NAME = RPC_VAR_PREFIX + "view_name";
  public static final String VAR_VIEW_WHERE = RPC_VAR_PREFIX + "view_where";
  public static final String VAR_VIEW_ORDER = RPC_VAR_PREFIX + "view_order";
  public static final String VAR_VIEW_OFFSET = RPC_VAR_PREFIX + "view_offset";
  public static final String VAR_VIEW_LIMIT = RPC_VAR_PREFIX + "view_limit";
  public static final String VAR_VIEW_STATES = RPC_VAR_PREFIX + "view_states";
  public static final String VAR_VIEW_ROWS = RPC_VAR_PREFIX + "view_rows";
  public static final String VAR_VIEW_ROW_ID = RPC_VAR_PREFIX + "view_row_id";
  public static final String VAR_VIEW_VERSION = RPC_VAR_PREFIX + "view_version";
  public static final String VAR_VIEW_COLUMN = RPC_VAR_PREFIX + "view_column";
  public static final String VAR_VIEW_TYPE = RPC_VAR_PREFIX + "view_type";
  public static final String VAR_VIEW_OLD_VALUE = RPC_VAR_PREFIX + "view_old";
  public static final String VAR_VIEW_NEW_VALUE = RPC_VAR_PREFIX + "view_new";

  public static final String XML_TAG_DATA = RPC_VAR_PREFIX + "data";

  /**
   * Returns true if {@code svc} value starts with {@link #COMPOSITE_SERVICE_PREFIX}.
   * 
   * @param svc name of service
   * @return true if name of service starts with {@link #COMPOSITE_SERVICE_PREFIX}
   */
  public static boolean isCompositeService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(COMPOSITE_SERVICE_PREFIX);
  }

  /**
   * Returns true if {@code svc} value starts with {@link #DATA_SERVICE_PREFIX}.
   * 
   * @param svc name of service
   * @return true if name of service starts with {@link #DATA_SERVICE_PREFIX}
   */
  public static boolean isDataService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(DATA_SERVICE_PREFIX);
  }

  /**
   * Returns true if {@code svc} value starts with {@link #DB_META_SERVICE_PREFIX}.
   * 
   * @param svc name of service
   * @return true if name of service starts with {@link #DB_META_SERVICE_PREFIX}
   */
  public static boolean isDbMetaService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(DB_META_SERVICE_PREFIX);
  }

  /**
   * Returns true if {@code svc} value starts with {@link #DB_SERVICE_PREFIX}.
   * 
   * @param svc name of service;
   * @return true if name of service starts with {@link #DB_SERVICE_PREFIX}
   */
  public static boolean isDbService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(DB_SERVICE_PREFIX);
  }

  /**
   * Returns true if {@code svc} value has {@link #INVOKE}.
   * 
   * @param svc name of service;
   * @return true if name of service starts {@link #INVOKE}
   */
  public static boolean isInvocation(String svc) {
    return BeeUtils.same(svc, INVOKE);
  }

  /**
   * Returns true if {@code svc} value starts with {@link #RPC_SERVICE_PREFIX}.
   * 
   * @param svc name of service
   * @return true if name of servce starts with {@link #RPC_SERVICE_PREFIX}
   */
  public static boolean isRpcService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(RPC_SERVICE_PREFIX);
  }

  /**
   * Returns true if {@code svc} value starts with {@link #SYS_SERVICE_PREFIX}.
   * 
   * @param svc name of service
   * @return true if name of service starts with {@link #SYS_SERVICE_PREFIX};
   */
  public static boolean isSysService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(SYS_SERVICE_PREFIX);
  }

  /**
   * Returns true if {@code svc} value stars with {@link #UI_SERVICE_PREFIX}.
   * 
   * @param svc name of service
   * @return true if name of service value starts with {@link #UI_SERVICE_PREFIX}
   */
  public static boolean isUiService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(UI_SERVICE_PREFIX);
  }

  private Service() {
  }
}

package com.butent.bee.egg.shared;

import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeService {

  public static final String RPC_SERVICE_PREFIX = "rpc_";
  public static final String UI_SERVICE_PREFIX = "ui_";
  public static final String COMPOSITE_SERVICE_PREFIX = "comp_";

  public static final String DB_SERVICE_PREFIX = RPC_SERVICE_PREFIX + "db_";
  public static final String SERVICE_DB_JDBC = DB_SERVICE_PREFIX + "jdbc";

  public static final String DB_META_SERVICE_PREFIX = DB_SERVICE_PREFIX + "meta_";
  public static final String SERVICE_DB_PING = DB_META_SERVICE_PREFIX + "ping";
  public static final String SERVICE_DB_INFO = DB_META_SERVICE_PREFIX + "info";
  public static final String SERVICE_DB_TABLES = DB_META_SERVICE_PREFIX + "tables";
  public static final String SERVICE_DB_KEYS = DB_META_SERVICE_PREFIX + "keys";
  public static final String SERVICE_DB_PRIMARY = DB_META_SERVICE_PREFIX + "primary";

  public static final String SYS_SERVICE_PREFIX = RPC_SERVICE_PREFIX + "sys_";
  public static final String SERVICE_CLASS_INFO = SYS_SERVICE_PREFIX + "class_info";
  public static final String SERVICE_XML_INFO = SYS_SERVICE_PREFIX + "xml_info";
  public static final String SERVICE_GET_RESOURCE = SYS_SERVICE_PREFIX + "get_resource";
  public static final String SERVICE_SAVE_RESOURCE = SYS_SERVICE_PREFIX + "save_resource";
  public static final String SERVICE_GET_DIGEST = SYS_SERVICE_PREFIX + "get_digest";

  public static final String SERVICE_INVOKE = RPC_SERVICE_PREFIX + "invoke";

  public static final String SERVICE_LOGIN = RPC_SERVICE_PREFIX + "login";
  public static final String SERVICE_LOGOUT = RPC_SERVICE_PREFIX + "logout";

  public static final String SERVICE_GET_MENU = RPC_SERVICE_PREFIX + "get_menu";

  public static final String SERVICE_WHERE_AM_I = RPC_SERVICE_PREFIX + "where_am_i";

  public static final String SERVICE_CLOSE_DIALOG = UI_SERVICE_PREFIX + "close_dialog";
  public static final String SERVICE_CONFIRM_DIALOG = UI_SERVICE_PREFIX + "confirm_dialog";
  public static final String SERVICE_CANCEL_DIALOG = UI_SERVICE_PREFIX + "cancel_dialog";
  public static final String SERVICE_REFRESH_MENU = UI_SERVICE_PREFIX + "refresh_menu";

  public static final String SERVICE_GET_CLASS = COMPOSITE_SERVICE_PREFIX + "get_class";
  public static final String SERVICE_GET_XML = COMPOSITE_SERVICE_PREFIX + "get_xml";
  public static final String SERVICE_GET_DATA = COMPOSITE_SERVICE_PREFIX + "get_data";

  public static final String RPC_FIELD_PREFIX = "bee_";
  public static final String RPC_FIELD_SYS_PREFIX = RPC_FIELD_PREFIX + "sys_";

  public static final String RPC_FIELD_SVC = RPC_FIELD_SYS_PREFIX + "svc";
  public static final String RPC_FIELD_QID = RPC_FIELD_SYS_PREFIX + "qid";
  public static final String RPC_FIELD_SID = RPC_FIELD_SYS_PREFIX + "sid";
  public static final String RPC_FIELD_DSN = RPC_FIELD_SYS_PREFIX + "dsn";
  public static final String RPC_FIELD_SEP = RPC_FIELD_SYS_PREFIX + "sep";
  public static final String RPC_FIELD_OPT = RPC_FIELD_SYS_PREFIX + "opt";

  public static final String RPC_FIELD_CNT = RPC_FIELD_SYS_PREFIX + "cnt";
  public static final String RPC_FIELD_COLS = RPC_FIELD_SYS_PREFIX + "c_c";
  public static final String RPC_FIELD_ROWS = RPC_FIELD_SYS_PREFIX + "r_c";
  public static final String RPC_FIELD_CTP = RPC_FIELD_SYS_PREFIX + "ctp";
  public static final String RPC_FIELD_URI = RPC_FIELD_SYS_PREFIX + "uri";
  public static final String RPC_FIELD_MD5 = RPC_FIELD_SYS_PREFIX + "md5";

  public static final String RPC_FIELD_MSG_CNT = RPC_FIELD_SYS_PREFIX + "m_c";
  public static final String RPC_FIELD_MSG = RPC_FIELD_SYS_PREFIX + "msg";
  public static final String RPC_FIELD_PRM_CNT = RPC_FIELD_SYS_PREFIX + "p_c";
  public static final String RPC_FIELD_PRM = RPC_FIELD_SYS_PREFIX + "prm";
  public static final String RPC_FIELD_PART_CNT = RPC_FIELD_SYS_PREFIX + "x_c";
  public static final String RPC_FIELD_PART = RPC_FIELD_SYS_PREFIX + "part";

  public static final String RPC_FIELD_METH = RPC_FIELD_SYS_PREFIX + "meth";

  public static final String FIELD_CLASS_NAME = RPC_FIELD_PREFIX + "class_name";
  public static final String FIELD_PACKAGE_LIST = RPC_FIELD_PREFIX
      + "package_list";
  public static final String FIELD_FILE_NAME = RPC_FIELD_PREFIX + "file_name";

  public static final String FIELD_XML_SOURCE = RPC_FIELD_PREFIX + "xml_source";
  public static final String FIELD_XML_TRANSFORM = RPC_FIELD_PREFIX
      + "xml_transform";
  public static final String FIELD_XML_TARGET = RPC_FIELD_PREFIX + "xml_target";
  public static final String FIELD_XML_RETURN = RPC_FIELD_PREFIX + "xml_return";

  public static final String FIELD_JDBC_QUERY = RPC_FIELD_PREFIX + "jdbc_query";
  public static final String FIELD_CONNECTION_AUTO_COMMIT = RPC_FIELD_PREFIX
      + "conn_auto_commit";
  public static final String FIELD_CONNECTION_READ_ONLY = RPC_FIELD_PREFIX
      + "conn_read_only";
  public static final String FIELD_CONNECTION_HOLDABILITY = RPC_FIELD_PREFIX
      + "conn_holdability";
  public static final String FIELD_CONNECTION_TRANSACTION_ISOLATION = RPC_FIELD_PREFIX
      + "conn_ti";
  public static final String FIELD_STATEMENT_CURSOR_NAME = RPC_FIELD_PREFIX
      + "stmt_cursor";
  public static final String FIELD_STATEMENT_ESCAPE_PROCESSING = RPC_FIELD_PREFIX
      + "stmt_escape";
  public static final String FIELD_STATEMENT_FETCH_DIRECTION = RPC_FIELD_PREFIX
      + "stmt_fetch_dir";
  public static final String FIELD_STATEMENT_FETCH_SIZE = RPC_FIELD_PREFIX
      + "stmt_fetch_size";
  public static final String FIELD_STATEMENT_MAX_FIELD_SIZE = RPC_FIELD_PREFIX
      + "stmt_field_size";
  public static final String FIELD_STATEMENT_MAX_ROWS = RPC_FIELD_PREFIX
      + "stmt_max_rows";
  public static final String FIELD_STATEMENT_POOLABLE = RPC_FIELD_PREFIX
      + "stmt_poolable";
  public static final String FIELD_STATEMENT_QUERY_TIMEOUT = RPC_FIELD_PREFIX
      + "stmt_timeout";
  public static final String FIELD_STATEMENT_RS_TYPE = RPC_FIELD_PREFIX
      + "stmt_rs_type";
  public static final String FIELD_STATEMENT_RS_CONCURRENCY = RPC_FIELD_PREFIX
      + "stmt_rs_concurrency";
  public static final String FIELD_STATEMENT_RS_HOLDABILITY = RPC_FIELD_PREFIX
      + "stmt_rs_holdability";
  public static final String FIELD_RESULT_SET_FETCH_DIRECTION = RPC_FIELD_PREFIX
      + "rs_fetch_dir";
  public static final String FIELD_RESULT_SET_FETCH_SIZE = RPC_FIELD_PREFIX
      + "rs_fetch_size";
  public static final String FIELD_JDBC_RETURN = RPC_FIELD_PREFIX
      + "jdbc_return";

  public static final String XML_TAG_DATA = RPC_FIELD_PREFIX + "data";

  public static boolean isCompositeService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(COMPOSITE_SERVICE_PREFIX);
  }

  public static boolean isDbMetaService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(DB_META_SERVICE_PREFIX);
  }

  public static boolean isDbService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(DB_SERVICE_PREFIX);
  }

  public static boolean isInvocation(String svc) {
    return BeeUtils.same(svc, SERVICE_INVOKE);
  }
  
  public static boolean isRpcService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(RPC_SERVICE_PREFIX);
  }

  public static boolean isSysService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(SYS_SERVICE_PREFIX);
  }

  public static boolean isUiService(String svc) {
    Assert.notEmpty(svc);
    return svc.startsWith(UI_SERVICE_PREFIX);
  }

}

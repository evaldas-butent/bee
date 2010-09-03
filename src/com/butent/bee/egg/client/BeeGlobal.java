package com.butent.bee.egg.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.butent.bee.egg.client.dialog.BeeInputBox;
import com.butent.bee.egg.client.dialog.BeeMessageBox;
import com.butent.bee.egg.client.grid.BeeGrid;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeField;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.BeeType;
import com.butent.bee.egg.shared.BeeWidget;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

public class BeeGlobal implements BeeModule {
  public static final String FIELD_DEBUG = "debug";

  private static final BeeMessageBox msgBox = new BeeMessageBox();
  private static final BeeInputBox inpBox = new BeeInputBox();
  private static final BeeGrid grids = new BeeGrid();

  private static final Map<String, BeeField> fields = new HashMap<String, BeeField>();

  public String getName() {
    return getClass().getName();
  }

  public int getPriority(int p) {
    switch (p) {
    case PRIORITY_INIT:
      return 20;
    case PRIORITY_START:
      return DO_NOT_CALL;
    case PRIORITY_END:
      return DO_NOT_CALL;
    default:
      return DO_NOT_CALL;
    }
  }

  public void init() {
    initFields();
  }

  public void start() {
  }

  public void end() {
  }

  public static void showDialog(Object... obj) {
    msgBox.showInfo(obj);
  }

  public static void showDialog(String cap, String msg, Throwable err) {
    if (err == null) {
      msgBox.showInfo(cap, msg);
    } else {
      msgBox.showError(cap, msg, err);
    }
  }

  public static void showError(Object... obj) {
    msgBox.showError(obj);
  }

  public static boolean closeDialog(GwtEvent<?> event) {
    if (event == null)
      return false;
    else
      return msgBox.close(event.getSource());
  }

  public static Widget createSimpleGrid(String[] cols, String[][] data) {
    return grids.simpleGrid(cols, data);
  }

  public static Widget createGrid(int c, JsArrayString data) {
    return grids.createGrid(c, data);
  }

  public static void createField(String name, String caption, int type,
      String value) {
    Assert.notEmpty(name);
    Assert.isTrue(BeeType.isValid(type));

    fields.put(name, new BeeField(caption, type, value));
  }

  public static void createField(String name, String caption, int type,
      String value, BeeWidget widget, String... items) {
    Assert.notEmpty(name);
    Assert.isTrue(BeeType.isValid(type));

    fields.put(name, new BeeField(caption, type, value, widget, items));
  }

  public static BeeField getField(String name) {
    Assert.contains(fields, name);

    return fields.get(name);
  }

  public static String getFieldCaption(String name) {
    return getField(name).getCaption();
  }

  public static BeeWidget getFieldWidget(String name) {
    return getField(name).getWidget();
  }

  public static List<String> getFieldItems(String name) {
    return getField(name).getItems();
  }

  public static int getFieldType(String name) {
    return getField(name).getType();
  }

  public static String getFieldWidth(String name) {
    return getField(name).getWidth();
  }

  public static String getFieldValue(String name) {
    return getField(name).getValue();
  }

  public static void setField(String name, BeeField fld) {
    Assert.notEmpty(name);
    Assert.notNull(fld);

    fields.put(name, fld);
  }

  public static void setFieldValue(String name, String value) {
    getField(name).setValue(value);
  }

  public static void setFieldValue(String name, Integer value) {
    getField(name).setValue(BeeUtils.transform(value));
  }

  public static void setFieldWidth(String name, String width) {
    getField(name).setWidth(width);
  }

  public static void inputFields(BeeStage bst, String cap, String... flds) {
    inpBox.inputFields(bst, cap, flds);
  }

  public static boolean getFieldBoolean(String name) {
    return BeeUtils.toBoolean(getField(name).getValue());
  }

  public static int getFieldInt(String name) {
    return BeeUtils.toInt(getField(name).getValue());
  }

  public static boolean isDebug() {
    return getFieldBoolean(FIELD_DEBUG);
  }

  public static boolean isField(String name) {
    return fields.containsKey(name);
  }

  public static void showFields() {
    inputFields(null, "Fields", fields.keySet().toArray(new String[0]));
  }

  private void initFields() {
    createField(BeeService.FIELD_CLASS_NAME, "Class name", BeeType.TYPE_STRING,
        BeeConst.STRING_EMPTY);
    createField(BeeService.FIELD_PACKAGE_LIST, "Default Packages",
        BeeType.TYPE_STRING, BeeConst.STRING_EMPTY);
    createField(BeeService.FIELD_XML_FILE, "XML full path",
        BeeType.TYPE_STRING, BeeConst.STRING_EMPTY);

    createField(BeeService.FIELD_FILE_NAME, null, BeeType.TYPE_FILE,
        BeeConst.STRING_EMPTY);

    createField(BeeService.FIELD_JDBC_QUERY, "Jdbc Query", BeeType.TYPE_STRING,
        BeeConst.STRING_EMPTY);
    setFieldWidth(BeeService.FIELD_JDBC_QUERY, "500px");

    createField(BeeService.FIELD_CONNECTION_AUTO_COMMIT,
        "Connection auto commit", BeeType.TYPE_STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.STRING_FALSE,
        BeeConst.STRING_TRUE);
    createField(BeeService.FIELD_CONNECTION_READ_ONLY, "Connection read only",
        BeeType.TYPE_STRING, BeeConst.DEFAULT, BeeWidget.RADIO,
        BeeConst.DEFAULT, BeeConst.STRING_FALSE, BeeConst.STRING_TRUE);
    createField(BeeService.FIELD_CONNECTION_HOLDABILITY,
        "Connection holdability", BeeType.TYPE_STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.HOLD_CURSORS_OVER_COMMIT,
        BeeConst.CLOSE_CURSORS_AT_COMMIT);
    createField(BeeService.FIELD_CONNECTION_TRANSACTION_ISOLATION,
        "Transaction isolation", BeeType.TYPE_STRING, BeeConst.DEFAULT,
        BeeWidget.LIST, BeeConst.DEFAULT, BeeConst.TRANSACTION_NONE,
        BeeConst.TRANSACTION_READ_COMMITTED,
        BeeConst.TRANSACTION_READ_UNCOMMITTED,
        BeeConst.TRANSACTION_REPEATABLE_READ, BeeConst.TRANSACTION_SERIALIZABLE);

    createField(BeeService.FIELD_STATEMENT_CURSOR_NAME, "Cursor name",
        BeeType.TYPE_STRING, BeeConst.STRING_EMPTY);
    createField(BeeService.FIELD_STATEMENT_ESCAPE_PROCESSING,
        "Escape Processing", BeeType.TYPE_STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.STRING_FALSE,
        BeeConst.STRING_TRUE);
    createField(BeeService.FIELD_STATEMENT_FETCH_DIRECTION,
        "Statement fetch direction", BeeType.TYPE_STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.FETCH_FORWARD,
        BeeConst.FETCH_REVERSE, BeeConst.FETCH_UNKNOWN);
    createField(BeeService.FIELD_STATEMENT_FETCH_SIZE, "Statement fetch size",
        BeeType.TYPE_STRING, BeeConst.STRING_EMPTY);
    createField(BeeService.FIELD_STATEMENT_MAX_FIELD_SIZE,
        "Statement max field size", BeeType.TYPE_STRING, BeeConst.STRING_EMPTY);
    createField(BeeService.FIELD_STATEMENT_MAX_ROWS, "Statement max rows",
        BeeType.TYPE_STRING, BeeConst.STRING_EMPTY);
    createField(BeeService.FIELD_STATEMENT_POOLABLE, "Poolable",
        BeeType.TYPE_STRING, BeeConst.DEFAULT, BeeWidget.RADIO,
        BeeConst.DEFAULT, BeeConst.STRING_FALSE, BeeConst.STRING_TRUE);
    createField(BeeService.FIELD_STATEMENT_QUERY_TIMEOUT, "Query timeout",
        BeeType.TYPE_STRING, BeeConst.STRING_EMPTY);

    createField(BeeService.FIELD_STATEMENT_RS_TYPE, "Statement rs type",
        BeeType.TYPE_STRING, BeeConst.DEFAULT, BeeWidget.RADIO,
        BeeConst.DEFAULT, BeeConst.TYPE_FORWARD_ONLY,
        BeeConst.TYPE_SCROLL_INSENSITIVE, BeeConst.TYPE_SCROLL_SENSITIVE);
    createField(BeeService.FIELD_STATEMENT_RS_CONCURRENCY,
        "Statement rs concurrency", BeeType.TYPE_STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.CONCUR_READ_ONLY,
        BeeConst.CONCUR_UPDATABLE);
    createField(BeeService.FIELD_STATEMENT_RS_HOLDABILITY,
        "Statement rs holdability", BeeType.TYPE_STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.HOLD_CURSORS_OVER_COMMIT,
        BeeConst.CLOSE_CURSORS_AT_COMMIT);

    createField(BeeService.FIELD_RESULT_SET_FETCH_DIRECTION,
        "Rs fetch direction", BeeType.TYPE_STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.FETCH_FORWARD,
        BeeConst.FETCH_REVERSE, BeeConst.FETCH_UNKNOWN);
    createField(BeeService.FIELD_RESULT_SET_FETCH_SIZE, "Rs fetch size",
        BeeType.TYPE_STRING, BeeConst.STRING_EMPTY);

    createField(BeeService.FIELD_JDBC_RETURN, "Jdbc return",
        BeeType.TYPE_STRING, BeeConst.JDBC_RESULT_SET, BeeWidget.RADIO,
        BeeConst.JDBC_RESULT_SET, BeeConst.JDBC_META_DATA,
        BeeConst.JDBC_ROW_COUNT, BeeConst.JDBC_COLUMNS);

    createField(FIELD_DEBUG, "Debug", BeeType.TYPE_BOOLEAN,
        BeeUtils.toString(false));
  }

}

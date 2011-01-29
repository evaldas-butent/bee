package com.butent.bee.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsDate;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.CacheUtils;
import com.butent.bee.client.dialog.InputBox;
import com.butent.bee.client.dialog.MessageBox;
import com.butent.bee.client.grid.CellType;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.resources.Images;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.BeeStage;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Global implements Module {
  public static final String VAR_DEBUG = "debug";

  private static final MessageBox msgBox = new MessageBox();
  private static final InputBox inpBox = new InputBox();
  private static final GridFactory grids = new GridFactory();
  private static final CacheUtils cache = new CacheUtils();

  private static final Map<String, Variable> vars = new HashMap<String, Variable>();

  private static int tzo = -JsDate.create().getTimezoneOffset() * TimeUtils.MILLIS_PER_MINUTE;

  private static Images images = GWT.create(Images.class);

  public static void alert(Object... obj) {
    msgBox.alert(obj);
  }

  public static Widget cellGrid(Object data, CellType cellType, String... columns) {
    return grids.cellGrid(data, cellType, (Object[]) columns);
  }

  public static boolean closeDialog(GwtEvent<?> event) {
    if (event == null) {
      return false;
    } else {
      return msgBox.close(event.getSource());
    }
  }

  public static boolean confirm(Object... obj) {
    return msgBox.confirm(obj);
  }

  public static void createVar(String name, String caption) {
    createVar(name, caption, BeeType.STRING, BeeConst.STRING_EMPTY);
  }

  public static void createVar(String name, String caption, BeeType type, String value) {
    Assert.notEmpty(name);
    Assert.notNull(type);

    vars.put(name, new Variable(caption, type, value));
  }

  public static void createVar(String name, String caption, BeeType type, String value,
      BeeWidget widget, String... items) {
    Assert.notEmpty(name);
    Assert.notNull(type);

    vars.put(name, new Variable(caption, type, value, widget, items));
  }

  public static CacheUtils getCache() {
    return cache;
  }

  public static Images getImages() {
    return images;
  }

  public static int getTzo() {
    return tzo;
  }

  public static Variable getVar(String name) {
    Assert.contains(vars, name);
    return vars.get(name);
  }

  public static boolean getVarBoolean(String name) {
    return getVar(name).getBoolean();
  }

  public static String getVarCaption(String name) {
    return getVar(name).getCaption();
  }

  public static int getVarInt(String name) {
    return getVar(name).getInt();
  }

  public static List<String> getVarItems(String name) {
    return getVar(name).getItems();
  }

  public static long getVarLong(String name) {
    return getVar(name).getLong();
  }

  public static String getVarName(Variable var) {
    Assert.notNull(var);
    return BeeUtils.getKey(vars, var);
  }

  public static BeeType getVarType(String name) {
    return getVar(name).getType();
  }

  public static String getVarValue(String name) {
    return getVar(name).getValue();
  }

  public static BeeWidget getVarWidget(String name) {
    return getVar(name).getWidget();
  }

  public static String getVarWidth(String name) {
    return getVar(name).getWidth();
  }

  public static void inform(Object... obj) {
    msgBox.showInfo(obj);
  }

  public static void inputVars(BeeStage bst, String cap, String... names) {
    List<Variable> lst = new ArrayList<Variable>();
    for (String name : names) {
      if (vars.containsKey(name)) {
        lst.add(vars.get(name));
      }
    }
    inputVars(bst, cap, lst.toArray(new Variable[0]));
  }

  public static void inputVars(BeeStage bst, String cap, Variable... variables) {
    inpBox.inputVars(bst, cap, variables);
  }

  public static boolean isDebug() {
    return getVarBoolean(VAR_DEBUG);
  }

  public static boolean isVar(String name) {
    return vars.containsKey(name);
  }

  public static void modalGrid(String cap, Object data, String... cols) {
    msgBox.showGrid(cap, data, cols);
  }

  public static void sayHuh(Object... obj) {
    msgBox.showInfo("Huh ?", obj);
  }

  public static Widget scrollGrid(int width, Object data, String... columns) {
    return grids.scrollGrid(width, data, (Object[]) columns);
  }

  public static Widget scrollGrid(Object data, String... columns) {
    return scrollGrid(-1, data, columns);
  }

  public static void setVar(String name, Variable var) {
    Assert.notEmpty(name);
    Assert.notNull(var);

    vars.put(name, var);
  }

  public static void setVarValue(String name, boolean value) {
    getVar(name).setValue(value);
  }

  public static void setVarValue(String name, int value) {
    getVar(name).setValue(value);
  }

  public static void setVarValue(String name, long value) {
    getVar(name).setValue(value);
  }

  public static void setVarValue(String name, String value) {
    getVar(name).setValue(value);
  }

  public static void setVarWidth(String name, String width) {
    getVar(name).setWidth(width);
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

  public static void showVars(String... context) {
    int n = context.length;
    Variable[] arr = null;

    if (n > 0) {
      Set<String> names = vars.keySet();
      Set<String> keys = new LinkedHashSet<String>();
      for (String z : context) {
        keys.addAll(BeeUtils.getContext(z, names));
      }
      if (keys.size() > 0) {
        arr = new Variable[keys.size()];
        int idx = 0;
        for (String key : keys) {
          arr[idx++] = vars.get(key);
        }
      }
    } else {
      arr = vars.values().toArray(new Variable[0]);
    }

    if (BeeUtils.isEmpty(arr)) {
      showError("no variables found", context);
    } else {
      inputVars(null, "Variables", arr);
    }
  }

  public static Widget simpleGrid(Object data, String... columns) {
    return grids.simpleGrid(data, (Object[]) columns);
  }

  public void end() {
  }

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
    initVars();
  }

  public void start() {
  }

  private void initVars() {
    createVar(BeeService.VAR_CLASS_NAME, "Class name");
    createVar(BeeService.VAR_PACKAGE_LIST, "Default Packages");

    createVar(BeeService.VAR_LOGIN, "Login");
    createVar(BeeService.VAR_PASSWORD, "Password");

    createVar(BeeService.VAR_XML_SOURCE, "source");
    createVar(BeeService.VAR_XML_TRANSFORM, "transform");
    createVar(BeeService.VAR_XML_TARGET, "target");
    createVar(BeeService.VAR_XML_RETURN, "return", BeeType.STRING,
        "all", BeeWidget.RADIO, "all", "xsl", "source", "xml", "prop");

    setVarWidth(BeeService.VAR_XML_SOURCE, "300px");
    setVarWidth(BeeService.VAR_XML_TRANSFORM, "300px");
    setVarWidth(BeeService.VAR_XML_TARGET, "300px");

    createVar(BeeService.VAR_FILE_NAME, null, BeeType.FILE, BeeConst.STRING_EMPTY);

    createVar(BeeService.VAR_JDBC_QUERY, "Jdbc Query");
    setVarWidth(BeeService.VAR_JDBC_QUERY, "500px");

    createVar(BeeService.VAR_CONNECTION_AUTO_COMMIT,
        "Connection auto commit", BeeType.STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.STRING_FALSE, BeeConst.STRING_TRUE);
    createVar(BeeService.VAR_CONNECTION_READ_ONLY, "Connection read only",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO,
        BeeConst.DEFAULT, BeeConst.STRING_FALSE, BeeConst.STRING_TRUE);
    createVar(BeeService.VAR_CONNECTION_HOLDABILITY, "Connection holdability",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT,
        BeeConst.HOLD_CURSORS_OVER_COMMIT, BeeConst.CLOSE_CURSORS_AT_COMMIT);
    createVar(BeeService.VAR_CONNECTION_TRANSACTION_ISOLATION,
        "Transaction isolation", BeeType.STRING, BeeConst.DEFAULT,
        BeeWidget.LIST, BeeConst.DEFAULT, BeeConst.TRANSACTION_NONE,
        BeeConst.TRANSACTION_READ_COMMITTED, BeeConst.TRANSACTION_READ_UNCOMMITTED,
        BeeConst.TRANSACTION_REPEATABLE_READ, BeeConst.TRANSACTION_SERIALIZABLE);

    createVar(BeeService.VAR_STATEMENT_CURSOR_NAME, "Cursor name");
    createVar(BeeService.VAR_STATEMENT_ESCAPE_PROCESSING,
        "Escape Processing", BeeType.STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.STRING_FALSE, BeeConst.STRING_TRUE);
    createVar(BeeService.VAR_STATEMENT_FETCH_DIRECTION, "Statement fetch direction",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT,
        BeeConst.FETCH_FORWARD, BeeConst.FETCH_REVERSE, BeeConst.FETCH_UNKNOWN);
    createVar(BeeService.VAR_STATEMENT_FETCH_SIZE, "Statement fetch size");
    createVar(BeeService.VAR_STATEMENT_MAX_FIELD_SIZE, "Statement max field size");
    createVar(BeeService.VAR_STATEMENT_MAX_ROWS, "Statement max rows");
    createVar(BeeService.VAR_STATEMENT_POOLABLE, "Poolable", BeeType.STRING,
        BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.STRING_FALSE,
        BeeConst.STRING_TRUE);
    createVar(BeeService.VAR_STATEMENT_QUERY_TIMEOUT, "Query timeout");

    createVar(BeeService.VAR_STATEMENT_RS_TYPE, "Statement rs type", BeeType.STRING,
        BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.TYPE_FORWARD_ONLY,
        BeeConst.TYPE_SCROLL_INSENSITIVE, BeeConst.TYPE_SCROLL_SENSITIVE);
    createVar(BeeService.VAR_STATEMENT_RS_CONCURRENCY, "Statement rs concurrency",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT,
        BeeConst.CONCUR_READ_ONLY, BeeConst.CONCUR_UPDATABLE);
    createVar(BeeService.VAR_STATEMENT_RS_HOLDABILITY, "Statement rs holdability",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT,
        BeeConst.HOLD_CURSORS_OVER_COMMIT, BeeConst.CLOSE_CURSORS_AT_COMMIT);

    createVar(BeeService.VAR_RESULT_SET_FETCH_DIRECTION, "Rs fetch direction",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT,
        BeeConst.FETCH_FORWARD, BeeConst.FETCH_REVERSE, BeeConst.FETCH_UNKNOWN);
    createVar(BeeService.VAR_RESULT_SET_FETCH_SIZE, "Rs fetch size");

    createVar(BeeService.VAR_JDBC_RETURN, "Jdbc return", BeeType.STRING,
        BeeConst.JDBC_RESULT_SET, BeeWidget.RADIO, BeeConst.JDBC_RESULT_SET,
        BeeConst.JDBC_META_DATA, BeeConst.JDBC_ROW_COUNT, BeeConst.JDBC_COLUMNS);

    createVar(VAR_DEBUG, "Debug", BeeType.BOOLEAN, BeeUtils.toString(false));

    for (int i = MenuConstants.ROOT_MENU_INDEX; i < MenuConstants.MAX_MENU_DEPTH; i++) {
      if (MenuConstants.isRootLevel(i)) {
        createVar(MenuConstants.varMenuLayout(i), "Root", BeeType.STRING,
            MenuConstants.DEFAULT_ROOT_LAYOUT, BeeWidget.LIST,
            MenuConstants.LAYOUT_MENU_HOR, MenuConstants.LAYOUT_MENU_VERT,
            MenuConstants.LAYOUT_STACK, MenuConstants.LAYOUT_TAB,
            MenuConstants.LAYOUT_TREE, MenuConstants.LAYOUT_CELL_TREE,
            MenuConstants.LAYOUT_CELL_BROWSER, MenuConstants.LAYOUT_LIST,
            MenuConstants.LAYOUT_ORDERED_LIST, MenuConstants.LAYOUT_UNORDERED_LIST,
            MenuConstants.LAYOUT_DEFINITION_LIST, MenuConstants.LAYOUT_RADIO_HOR,
            MenuConstants.LAYOUT_RADIO_VERT, MenuConstants.LAYOUT_BUTTONS_HOR,
            MenuConstants.LAYOUT_BUTTONS_VERT);
      } else {
        createVar(MenuConstants.varMenuLayout(i), "Items " + i,
            BeeType.STRING, MenuConstants.DEFAULT_ITEM_LAYOUT, BeeWidget.LIST,
            MenuConstants.LAYOUT_MENU_HOR, MenuConstants.LAYOUT_MENU_VERT,
            MenuConstants.LAYOUT_TREE, MenuConstants.LAYOUT_LIST,
            MenuConstants.LAYOUT_ORDERED_LIST, MenuConstants.LAYOUT_UNORDERED_LIST,
            MenuConstants.LAYOUT_DEFINITION_LIST, MenuConstants.LAYOUT_RADIO_HOR,
            MenuConstants.LAYOUT_RADIO_VERT, MenuConstants.LAYOUT_BUTTONS_HOR,
            MenuConstants.LAYOUT_BUTTONS_VERT);
      }

      createVar(MenuConstants.varMenuBarType(i), BeeConst.STRING_EMPTY,
          BeeType.BOOLEAN, BeeUtils.toString(false));
    }

    createVar(MenuConstants.VAR_ROOT_LIMIT, "Max  Roots", BeeType.INT,
        BeeUtils.transform(MenuConstants.DEFAULT_ROOT_LIMIT));
    createVar(MenuConstants.VAR_ITEM_LIMIT, "Max  Items", BeeType.INT,
        BeeUtils.transform(MenuConstants.DEFAULT_ITEM_LIMIT));
  }
}

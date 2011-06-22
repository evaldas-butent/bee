package com.butent.bee.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.Event;

import com.butent.bee.client.data.Explorer;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.MessageBoxes;
import com.butent.bee.client.grid.TextCellType;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.resources.Images;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.data.cache.CacheManager;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.LocalizableMessages;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * initializes and contains system parameters, which are used globally in the whole system.
 */

public class Global implements Module {
  public static final String VAR_DEBUG = "debug";

  public static LocalizableConstants constants = GWT.create(LocalizableConstants.class);
  public static LocalizableMessages messages = GWT.create(LocalizableMessages.class);

  private static final MessageBoxes msgBoxen = new MessageBoxes();
  private static final InputBoxes inpBoxen = new InputBoxes();
  private static final Explorer dataExplorer = new Explorer();

  private static final Map<String, Variable> vars = new HashMap<String, Variable>();
  
  private static final CacheManager cache = new CacheManager();

  private static Images images = GWT.create(Images.class);

  public static void alert(Object... obj) {
    msgBoxen.alert(obj);
  }

  public static Widget cellTable(Object data, TextCellType cellType, String... columnLabels) {
    return GridFactory.cellTable(data, cellType, columnLabels);
  }

  public static boolean closeDialog(Event<?> event) {
    if (event == null) {
      return false;
    } else {
      return msgBoxen.close(event.getSource());
    }
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

  public static CacheManager getCache() {
    return cache;
  }

  public static Explorer getDataExplorer() {
    return dataExplorer;
  }

  public static Images getImages() {
    return images;
  }

  public static MessageBoxes getMsgBoxen() {
    return msgBoxen;
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
    msgBoxen.showInfo(obj);
  }

  public static void inputVars(Stage bst, String cap, String... names) {
    Assert.notNull(names);
    List<Variable> lst = new ArrayList<Variable>();
    for (String name : names) {
      if (vars.containsKey(name)) {
        lst.add(vars.get(name));
      }
    }
    inputVars(bst, cap, lst.toArray(new Variable[0]));
  }

  public static void inputVars(Stage bst, String cap, Variable... variables) {
    inpBoxen.inputVars(bst, cap, variables);
  }

  public static boolean isDebug() {
    return getVarBoolean(VAR_DEBUG);
  }

  public static boolean isVar(String name) {
    return vars.containsKey(name);
  }

  public static void modalGrid(String cap, Object data, String... cols) {
    msgBoxen.showGrid(cap, data, cols);
  }

  public static boolean nativeConfirm(Object... obj) {
    return msgBoxen.nativeConfirm(obj);
  }

  public static void sayHuh(Object... obj) {
    msgBoxen.showInfo("Huh ?", obj);
  }

  public static Widget scrollGrid(int width, Object data, String... columnLabels) {
    return GridFactory.scrollGrid(width, data, columnLabels);
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
    msgBoxen.showInfo(obj);
  }

  public static void showDialog(String cap, String msg, Throwable err) {
    if (err == null) {
      msgBoxen.showInfo(cap, msg);
    } else {
      msgBoxen.showError(cap, msg, err);
    }
  }

  public static void showError(Object... obj) {
    msgBoxen.showError(obj);
  }

  public static void showVars(String... context) {
    int n = (context == null) ? 0 : context.length;
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

  public static void showWidget(Widget widget) {
    msgBoxen.showWidget(widget);
  }

  public static Widget simpleGrid(Object data, String... columnLabels) {
    return GridFactory.simpleGrid(data, columnLabels);
  }

  Global() {
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
    initCache();
    initDataExplorer();
    initVars();
  }

  public void start() {
  }
  
  private void initCache() {
    BeeKeeper.getBus().registerRowDeleteHandler(getCache());
    BeeKeeper.getBus().registerMultiDeleteHandler(getCache());

    BeeKeeper.getBus().registerCellUpdateHandler(getCache());
    BeeKeeper.getBus().registerRowUpdateHandler(getCache());

    BeeKeeper.getBus().registerRowInsertHandler(getCache());
  }

  private void initDataExplorer() {
    BeeKeeper.getBus().registerRowDeleteHandler(getDataExplorer());
    BeeKeeper.getBus().registerMultiDeleteHandler(getDataExplorer());

    BeeKeeper.getBus().registerRowInsertHandler(getDataExplorer());
  }
  
  private void initVars() {
    createVar(Service.VAR_CLASS_NAME, "Class name");
    createVar(Service.VAR_PACKAGE_LIST, "Default Packages");

    createVar(Service.VAR_LOGIN, "Login");
    createVar(Service.VAR_PASSWORD, "Password");

    createVar(Service.VAR_XML_SOURCE, "source");
    createVar(Service.VAR_XML_TRANSFORM, "transform");
    createVar(Service.VAR_XML_TARGET, "target");
    createVar(Service.VAR_XML_RETURN, "return", BeeType.STRING,
        "all", BeeWidget.RADIO, "all", "xsl", "source", "xml", "prop");

    setVarWidth(Service.VAR_XML_SOURCE, "300px");
    setVarWidth(Service.VAR_XML_TRANSFORM, "300px");
    setVarWidth(Service.VAR_XML_TARGET, "300px");

    createVar(Service.VAR_FILE_NAME, null, BeeType.FILE, BeeConst.STRING_EMPTY);

    createVar(Service.VAR_JDBC_QUERY, "Jdbc Query");
    setVarWidth(Service.VAR_JDBC_QUERY, "500px");

    createVar(Service.VAR_CONNECTION_AUTO_COMMIT,
        "Connection auto commit", BeeType.STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.STRING_FALSE, BeeConst.STRING_TRUE);
    createVar(Service.VAR_CONNECTION_READ_ONLY, "Connection read only",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO,
        BeeConst.DEFAULT, BeeConst.STRING_FALSE, BeeConst.STRING_TRUE);
    createVar(Service.VAR_CONNECTION_HOLDABILITY, "Connection holdability",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT,
        BeeConst.HOLD_CURSORS_OVER_COMMIT, BeeConst.CLOSE_CURSORS_AT_COMMIT);
    createVar(Service.VAR_CONNECTION_TRANSACTION_ISOLATION,
        "Transaction isolation", BeeType.STRING, BeeConst.DEFAULT,
        BeeWidget.LIST, BeeConst.DEFAULT, BeeConst.TRANSACTION_NONE,
        BeeConst.TRANSACTION_READ_COMMITTED, BeeConst.TRANSACTION_READ_UNCOMMITTED,
        BeeConst.TRANSACTION_REPEATABLE_READ, BeeConst.TRANSACTION_SERIALIZABLE);

    createVar(Service.VAR_STATEMENT_CURSOR_NAME, "Cursor name");
    createVar(Service.VAR_STATEMENT_ESCAPE_PROCESSING,
        "Escape Processing", BeeType.STRING, BeeConst.DEFAULT,
        BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.STRING_FALSE, BeeConst.STRING_TRUE);
    createVar(Service.VAR_STATEMENT_FETCH_DIRECTION, "Statement fetch direction",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT,
        BeeConst.FETCH_FORWARD, BeeConst.FETCH_REVERSE, BeeConst.FETCH_UNKNOWN);
    createVar(Service.VAR_STATEMENT_FETCH_SIZE, "Statement fetch size");
    createVar(Service.VAR_STATEMENT_MAX_FIELD_SIZE, "Statement max field size");
    createVar(Service.VAR_STATEMENT_MAX_ROWS, "Statement max rows");
    createVar(Service.VAR_STATEMENT_POOLABLE, "Poolable", BeeType.STRING,
        BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.STRING_FALSE,
        BeeConst.STRING_TRUE);
    createVar(Service.VAR_STATEMENT_QUERY_TIMEOUT, "Query timeout");

    createVar(Service.VAR_STATEMENT_RS_TYPE, "Statement rs type", BeeType.STRING,
        BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT, BeeConst.TYPE_FORWARD_ONLY,
        BeeConst.TYPE_SCROLL_INSENSITIVE, BeeConst.TYPE_SCROLL_SENSITIVE);
    createVar(Service.VAR_STATEMENT_RS_CONCURRENCY, "Statement rs concurrency",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT,
        BeeConst.CONCUR_READ_ONLY, BeeConst.CONCUR_UPDATABLE);
    createVar(Service.VAR_STATEMENT_RS_HOLDABILITY, "Statement rs holdability",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT,
        BeeConst.HOLD_CURSORS_OVER_COMMIT, BeeConst.CLOSE_CURSORS_AT_COMMIT);

    createVar(Service.VAR_RESULT_SET_FETCH_DIRECTION, "Rs fetch direction",
        BeeType.STRING, BeeConst.DEFAULT, BeeWidget.RADIO, BeeConst.DEFAULT,
        BeeConst.FETCH_FORWARD, BeeConst.FETCH_REVERSE, BeeConst.FETCH_UNKNOWN);
    createVar(Service.VAR_RESULT_SET_FETCH_SIZE, "Rs fetch size");

    createVar(Service.VAR_JDBC_RETURN, "Jdbc return", BeeType.STRING,
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

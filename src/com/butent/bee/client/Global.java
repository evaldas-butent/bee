package com.butent.bee.client;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.ClientDefaults;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dialog.MessageBoxes;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.images.Images;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.output.Reports;
import com.butent.bee.client.screen.Favorites;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.search.Filters;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.data.Defaults;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.cache.CacheManager;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.CssUnit;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * initializes and contains system parameters, which are used globally in the whole system.
 */

public class Global implements Module {

  private static final BeeLogger logger = LogUtils.getLogger(Global.class);

  private static final MessageBoxes msgBoxen = new MessageBoxes();
  private static final InputBoxes inpBoxen = new InputBoxes();

  private static final Map<String, Variable> vars = Maps.newHashMap();

  private static final CacheManager cache = new CacheManager();

  private static final Images.Resources images = Images.createResources();

  private static final Map<String, String> styleSheets = Maps.newHashMap();

  private static final Favorites favorites = new Favorites();

  private static final Defaults defaults = new ClientDefaults();

  private static final Search search = new Search();

  private static final Filters filters = new Filters();

  private static final Reports reports = new Reports();

  private static boolean debug = false;

  public static void addReport(String caption, Command command) {
    reports.addReport(caption, command);
  }

  public static void addStyleSheet(String name, String text) {
    if (BeeUtils.isEmpty(name)) {
      logger.warning("style sheet name not specified");
      return;
    }
    if (BeeUtils.isEmpty(text)) {
      logger.warning("style sheet text not specified");
      return;
    }

    String key = name.trim().toLowerCase();
    String value = text.trim();

    if (!value.equals(styleSheets.get(key))) {
      styleSheets.put(key, value);

      String css = CharMatcher.BREAKING_WHITESPACE.collapseFrom(value, BeeConst.CHAR_SPACE);
      StyleInjector.inject(css);
      Printer.onInjectStyleSheet(css);
    }
  }

  public static void addStyleSheets(Map<String, String> sheets) {
    if (sheets == null) {
      return;
    }
    for (Map.Entry<String, String> entry : sheets.entrySet()) {
      addStyleSheet(entry.getKey(), entry.getValue());
    }
  }

  public static void choice(String caption, String prompt, List<String> options,
      ChoiceCallback callback) {
    msgBoxen.choice(caption, prompt, options, callback, BeeConst.UNDEF, BeeConst.UNDEF, null, null);
  }

  public static boolean closeDialog(Widget source) {
    if (source == null) {
      return false;
    } else {
      return msgBoxen.close(source);
    }
  }

  public static void confirm(String message, ConfirmationCallback callback) {
    confirm(null, null, Lists.newArrayList(message), callback);
  }

  public static void confirm(String caption, Icon icon, List<String> messages,
      ConfirmationCallback callback) {
    msgBoxen.confirm(caption, icon, messages, callback, null, null, null);
  }

  public static void confirmDelete(String caption, Icon icon, List<String> messages,
      ConfirmationCallback callback) {
    msgBoxen.confirm(caption, icon, messages, callback, null,
        StyleUtils.FontSize.LARGE.getClassName(), StyleUtils.FontSize.MEDIUM.getClassName());
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

  public static void debug(String s) {
    logger.debug(s);
  }

  public static void decide(String caption, List<String> messages, DecisionCallback callback,
      int defaultValue) {
    msgBoxen.decide(caption, messages, callback, defaultValue, null, null, null);
  }

  public static CacheManager getCache() {
    return cache;
  }

  public static Defaults getDefaults() {
    return defaults;
  }

  public static Favorites getFavorites() {
    return favorites;
  }

  public static Filters getFilters() {
    return filters;
  }

  public static Images.Resources getImages() {
    return images;
  }

  public static InputBoxes getInpBoxen() {
    return inpBoxen;
  }

  public static MessageBoxes getMsgBoxen() {
    return msgBoxen;
  }

  public static Reports getReports() {
    return reports;
  }

  public static Search getSearch() {
    return search;
  }

  public static Widget getSearchWidget() {
    return search.ensureSearchWidget();
  }

  public static Map<String, String> getStylesheets() {
    return styleSheets;
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

  public static void inform(String... messages) {
    msgBoxen.showInfo(messages);
  }

  public static void inputString(String caption, String prompt, StringCallback callback) {
    inputString(caption, prompt, callback, null);
  }

  public static void inputString(String caption, String prompt, StringCallback callback,
      String defaultValue) {
    inputString(caption, prompt, callback, defaultValue, BeeConst.UNDEF);
  }

  public static void inputString(String caption, String prompt, StringCallback callback,
      String defaultValue, int maxLength) {
    inputString(caption, prompt, callback, defaultValue, maxLength, BeeConst.DOUBLE_UNDEF, null);
  }

  public static void inputString(String caption, String prompt, StringCallback callback,
      String defaultValue, int maxLength, double width, CssUnit widthUnit) {
    inputString(caption, prompt, callback, defaultValue, maxLength, width, widthUnit,
        BeeConst.UNDEF, Localized.constants.ok(), Localized.constants.cancel(), null);
  }

  public static void inputString(String caption, String prompt, StringCallback callback,
      String defaultValue, int maxLength, double width, CssUnit widthUnit, int timeout,
      String confirmHtml, String cancelHtml, WidgetInitializer initializer) {
    inpBoxen.inputString(caption, prompt, callback, defaultValue, maxLength, width, widthUnit,
        timeout, confirmHtml, cancelHtml, initializer);
  }

  public static void inputString(String caption, StringCallback callback) {
    inputString(caption, null, callback);
  }

  public static void inputVars(String caption, List<String> names, DialogCallback callback) {
    Assert.notNull(names);

    List<Variable> lst = Lists.newArrayList();
    for (String name : names) {
      if (vars.containsKey(name)) {
        lst.add(vars.get(name));
      }
    }

    inpBoxen.inputVars(caption, lst, callback);
  }

  public static void inputWidget(String caption, IsWidget input, InputCallback callback) {
    inputWidget(caption, input, callback, null, null, Action.NO_ACTIONS);
  }

  public static void inputWidget(String caption, IsWidget input, InputCallback callback,
      String dialogStyle) {
    inputWidget(caption, input, callback, dialogStyle, null, Action.NO_ACTIONS);
  }

  public static void inputWidget(String caption, IsWidget input, InputCallback callback,
      String dialogStyle, UIObject target) {
    inputWidget(caption, input, callback, dialogStyle, target, Action.NO_ACTIONS);
  }

  public static void inputWidget(String caption, IsWidget input, InputCallback callback,
      String dialogStyle, UIObject target, Set<Action> enabledActions) {
    inpBoxen.inputWidget(caption, input, callback, dialogStyle, target, enabledActions, null);
  }

  public static boolean isDebug() {
    return debug;
  }

  public static boolean isVar(String name) {
    return vars.containsKey(name);
  }

  public static void messageBox(String caption, Icon icon, List<String> messages,
      List<String> options, int defaultValue, ChoiceCallback callback) {
    msgBoxen.display(caption, icon, messages, options, defaultValue, callback, BeeConst.UNDEF,
        null, null, null, null);
  }

  public static boolean nativeConfirm(String... lines) {
    return msgBoxen.nativeConfirm(lines);
  }

  public static void sayHuh(String... messages) {
    int n = (messages == null) ? 0 : messages.length;
    String[] arr = new String[n + 1];
    arr[0] = "Huh ?";

    for (int i = 0; i < n; i++) {
      arr[i + 1] = messages[i];
    }
    inform(arr);
  }

  public static void setDebug(boolean debug) {
    Global.debug = debug;
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

  public static void showError(List<String> messages) {
    showError(null, messages, null, null);
  }

  public static void showError(String message) {
    List<String> messages = Lists.newArrayList();
    if (!BeeUtils.isEmpty(message)) {
      messages.add(message);
    }

    showError(messages);
  }

  public static void showError(String caption, List<String> messages) {
    showError(caption, messages, null, null);
  }

  public static void showError(String caption, List<String> messages, String dialogStyle) {
    showError(caption, messages, dialogStyle, null);
  }

  public static void showError(String caption, List<String> messages, String dialogStyle,
      String closeHtml) {
    msgBoxen.showError(caption, messages, dialogStyle, closeHtml);
  }

  public static void showGrid(IsTable<?, ?> table) {
    Assert.notNull(table, "showGrid: table is null");
    CellGrid grid = GridFactory.simpleGrid(table, BeeKeeper.getScreen().getActivePanelWidth());
    if (grid != null) {
      BeeKeeper.getScreen().updateActivePanel(grid);
    }
  }

  public static void showModalGrid(String caption, IsTable<?, ?> table) {
    msgBoxen.showTable(caption, table);
  }

  public static void showModalWidget(Widget widget) {
    msgBoxen.showWidget(widget);
  }

  public static void showVars(String... context) {
    int n = (context == null) ? 0 : context.length;
    List<String> names = Lists.newArrayList();

    if (n > 0) {
      Set<String> keys = new LinkedHashSet<String>();
      for (String z : context) {
        keys.addAll(BeeUtils.filterContext(vars.keySet(), z));
      }

      names.addAll(keys);
    } else {
      names.addAll(vars.keySet());
    }

    if (names.isEmpty()) {
      showError(Lists.newArrayList("no variables found", ArrayUtils.joinWords(context)));
    } else {
      inputVars("Variables", names, null);
    }
  }

  Global() {
  }

  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
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

  @Override
  public void init() {
    initCache();
    initVars();
    initImages();
    initFavorites();

    exportMethods();
  }

  @Override
  public void onExit() {
  }

  @Override
  public void start() {
  }

  private native void exportMethods() /*-{
    $wnd.Bee_updateForm = $entry(@com.butent.bee.client.ui.UiHelper::updateForm(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
    $wnd.Bee_getCaption = $entry(@com.butent.bee.shared.ui.Captions::getCaption(Ljava/lang/String;I));
    $wnd.Bee_debug = $entry(@com.butent.bee.client.Global::debug(Ljava/lang/String;));
    $wnd.Bee_updateActor = $entry(@com.butent.bee.client.decorator.TuningHelper::updateActor(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
  }-*/;

  private void initCache() {
    BeeKeeper.getBus().registerDataHandler(getCache(), true);
  }

  private void initFavorites() {
    BeeKeeper.getBus().registerRowDeleteHandler(getFavorites(), false);
    BeeKeeper.getBus().registerMultiDeleteHandler(getFavorites(), false);
  }

  private void initImages() {
    Images.init(getImages());
  }

  private void initVars() {
    createVar(Service.VAR_XML_SOURCE, "source");
    createVar(Service.VAR_XML_TRANSFORM, "transform");
    createVar(Service.VAR_XML_TARGET, "target");
    createVar(Service.VAR_XML_RETURN, "return", BeeType.STRING,
        "all", BeeWidget.RADIO, "all", "xsl", "source", "xml", "prop");

    setVarWidth(Service.VAR_XML_SOURCE, "300px");
    setVarWidth(Service.VAR_XML_TRANSFORM, "300px");
    setVarWidth(Service.VAR_XML_TARGET, "300px");

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
  }
}

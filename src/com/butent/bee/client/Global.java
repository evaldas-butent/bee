package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.data.ClientDefaults;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputWidgetCallback;
import com.butent.bee.client.dialog.MessageBoxes;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.images.Images;
import com.butent.bee.client.output.Reports;
import com.butent.bee.client.screen.Favorites;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.utils.Command;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.data.Defaults;
import com.butent.bee.shared.data.cache.CacheManager;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.LocalizableMessages;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * initializes and contains system parameters, which are used globally in the whole system.
 */

public class Global implements Module {

  public static final String VAR_DEBUG = "debug";

  public static final LocalizableConstants CONSTANTS = GWT.create(LocalizableConstants.class);
  public static final LocalizableMessages MESSAGES = GWT.create(LocalizableMessages.class);

  private static final MessageBoxes MSG_BOXEN = new MessageBoxes();
  private static final InputBoxes INP_BOXEN = new InputBoxes();

  private static final Map<String, Variable> VARS = Maps.newHashMap();

  private static final CacheManager CACHE = new CacheManager();

  private static final Images.Resources IMAGES = Images.createResources();

  private static final Map<String, String> STYLE_SHEETS = Maps.newHashMap();
  
  private static final Map<String, Class<? extends Enum<?>>> CAPTIONS = Maps.newHashMap();

  private static final Favorites FAVORITES = new Favorites();
  
  private static final Defaults DEFAULTS = new ClientDefaults();

  private static final Search SEARCH = new Search();

  private static final Reports REPORTS = new Reports();
  
  public static void addReport(String caption, Command command) {
    REPORTS.addReport(caption, command);
  }
  
  public static void addStyleSheet(String name, String text) {
    if (BeeUtils.isEmpty(name)) {
      BeeKeeper.getLog().warning("style sheet name not specified");
      return;
    }
    if (BeeUtils.isEmpty(text)) {
      BeeKeeper.getLog().warning("style sheet text not specified");
      return;
    }

    String key = name.trim().toLowerCase();
    String value = text.trim();

    if (!value.equals(STYLE_SHEETS.get(key))) {
      StyleInjector.inject(value);
      STYLE_SHEETS.put(key, value);
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

  public static void alert(Object... obj) {
    MSG_BOXEN.alert(obj);
  }

  public static void choice(String caption, List<String> options,
      DialogCallback<Integer> callback) {
    choice(caption, null, options, callback, BeeConst.UNDEF, BeeConst.UNDEF, null, null);
  }

  public static void choice(String caption, String prompt, List<String> options,
      DialogCallback<Integer> callback) {
    choice(caption, prompt, options, callback, BeeConst.UNDEF, BeeConst.UNDEF, null, null);
  }

  public static void choice(String caption, String prompt, List<String> options,
      DialogCallback<Integer> callback, int defaultValue) {
    choice(caption, prompt, options, callback, defaultValue, BeeConst.UNDEF, null, null);
  }
  
  public static void choice(String caption, String prompt, List<String> options,
      DialogCallback<Integer> callback, int defaultValue, int timeout) {
    choice(caption, prompt, options, callback, defaultValue, timeout, null, null);
  }

  public static void choice(String caption, String prompt, List<String> options,
      DialogCallback<Integer> callback, int defaultValue, int timeout, String cancelHtml) {
    choice(caption, prompt, options, callback, defaultValue, timeout, cancelHtml, null);
  }

  public static void choice(String caption, String prompt, List<String> options,
      DialogCallback<Integer> callback, int defaultValue, int timeout, String cancelHtml,
      WidgetInitializer initializer) {
    MSG_BOXEN.choice(caption, prompt, options, callback, defaultValue, timeout, cancelHtml,
        initializer);
  }

  public static void choice(String caption, String prompt, List<String> options,
      DialogCallback<Integer> callback, String cancelHtml) {
    choice(caption, prompt, options, callback, BeeConst.UNDEF, BeeConst.UNDEF, cancelHtml, null);
  }
  
  public static boolean closeDialog(Widget source) {
    if (source == null) {
      return false;
    } else {
      return MSG_BOXEN.close(source);
    }
  }

  public static void confirm(List<String> messages, Command command) {
    MSG_BOXEN.confirm(null, messages, command);
  }

  public static void confirm(String message, Command command) {
    MSG_BOXEN.confirm(message, command);
  }

  public static void confirm(String caption, List<String> messages, Command command) {
    MSG_BOXEN.confirm(caption, messages, command);
  }

  public static void confirm(String caption, String message, Command command) {
    MSG_BOXEN.confirm(caption, message, command);
  }

  public static void createVar(String name, String caption) {
    createVar(name, caption, BeeType.STRING, BeeConst.STRING_EMPTY);
  }

  public static void createVar(String name, String caption, BeeType type, String value) {
    Assert.notEmpty(name);
    Assert.notNull(type);

    VARS.put(name, new Variable(caption, type, value));
  }

  public static void createVar(String name, String caption, BeeType type, String value,
      BeeWidget widget, String... items) {
    Assert.notEmpty(name);
    Assert.notNull(type);

    VARS.put(name, new Variable(caption, type, value, widget, items));
  }
  
  public static void debug(String s) {
    BeeKeeper.getLog().debug(s);
  }

  public static CacheManager getCache() {
    return CACHE;
  }
  
  public static String getCaption(String key, int index) {
    if (BeeUtils.isEmpty(key)) {
      BeeKeeper.getLog().severe("Caption key not specified");
      return null;
    }
    
    List<String> list = getCaptions(key);
    if (!BeeUtils.isIndex(list, index)) {
      BeeKeeper.getLog().severe("cannot get caption: key", key, "index", index);
      return null;
    } else {
      return list.get(index);
    }
  }
  
  public static List<String> getCaptions(String key) {
    Assert.notEmpty(key);
    Class<? extends Enum<?>> clazz = CAPTIONS.get(BeeUtils.normalize(key));

    if (clazz == null) {
      BeeKeeper.getLog().severe("Captions not registered: " + key);
      return null;
    } else {
      return UiHelper.getCaptions(clazz);
    }
  }

  public static Defaults getDefaults() {
    return DEFAULTS;
  }

  public static Favorites getFavorites() {
    return FAVORITES;
  }

  public static Images.Resources getImages() {
    return IMAGES;
  }

  public static InputBoxes getInpBoxen() {
    return INP_BOXEN;
  }

  public static MessageBoxes getMsgBoxen() {
    return MSG_BOXEN;
  }
  
  public static Set<String> getRegisteredCaptionKeys() {
    return CAPTIONS.keySet();
  }
  
  public static Reports getReports() {
    return REPORTS;
  }

  public static Search getSearch() {
    return SEARCH;
  }

  public static Widget getSearchWidget() {
    return SEARCH.ensureWidget();
  }
  
  public static Map<String, String> getStylesheets() {
    return STYLE_SHEETS;
  }

  public static Variable getVar(String name) {
    Assert.contains(VARS, name);
    return VARS.get(name);
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
    return BeeUtils.getKey(VARS, var);
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
    MSG_BOXEN.showInfo(obj);
  }

  public static void inputString(String caption, DialogCallback<String> callback) {
    inputString(caption, null, callback);
  }

  public static void inputString(String caption, String prompt, DialogCallback<String> callback) {
    inputString(caption, prompt, callback, null);
  }

  public static void inputString(String caption, String prompt, DialogCallback<String> callback,
      String defaultValue) {
    inputString(caption, prompt, callback, defaultValue,
        BeeConst.UNDEF, BeeConst.DOUBLE_UNDEF, null);
  }

  public static void inputString(String caption, String prompt, DialogCallback<String> callback,
      String defaultValue, int maxLength, double width, Unit widthUnit) {
    inputString(caption, prompt, callback, defaultValue, maxLength, width, widthUnit,
        BeeConst.UNDEF, DialogConstants.OK, DialogConstants.CANCEL, null);
  }

  public static void inputString(String caption, String prompt, DialogCallback<String> callback,
      String defaultValue, int maxLength, double width, Unit widthUnit, int timeout,
      String confirmHtml, String cancelHtml, WidgetInitializer initializer) {
    INP_BOXEN.inputString(caption, prompt, callback, defaultValue, maxLength, width, widthUnit,
        timeout, confirmHtml, cancelHtml, initializer);
  }

  public static void inputVars(String caption, List<String> names, ConfirmationCallback callback) {
    Assert.notEmpty(names);

    List<Variable> lst = Lists.newArrayList();
    for (String name : names) {
      if (VARS.containsKey(name)) {
        lst.add(VARS.get(name));
      }
    }

    INP_BOXEN.inputVars(caption, lst, callback);
  }

  public static void inputWidget(String caption, Widget input, InputWidgetCallback callback) {
    inputWidget(caption, input, callback, false, null, false);
  }

  public static void inputWidget(String caption, Widget input, InputWidgetCallback callback,
      boolean enableGlass, String dialogStyle, boolean enablePrint) {
    inputWidget(caption, input, callback, enableGlass, dialogStyle, null, enablePrint);
  }

  public static void inputWidget(String caption, Widget input, InputWidgetCallback callback,
      boolean enableGlass, String dialogStyle, UIObject target, boolean enablePrint) {
    INP_BOXEN.inputWidget(caption, input, callback, enableGlass, dialogStyle, target, enablePrint,
        null, null, BeeConst.UNDEF, null);
  }

  public static void inputWidget(String caption, Widget input, InputWidgetCallback callback,
      boolean enableGlass, String dialogStyle, UIObject target, boolean enablePrint,
      String confirmHtml, String cancelHtml, int timeout, WidgetInitializer initializer) {
    INP_BOXEN.inputWidget(caption, input, callback, enableGlass, dialogStyle, target, enablePrint,
        confirmHtml, cancelHtml, timeout, initializer);
  }

  public static boolean isDebug() {
    return getVarBoolean(VAR_DEBUG);
  }

  public static boolean isVar(String name) {
    return VARS.containsKey(name);
  }

  public static void modalGrid(String cap, Object data, String... cols) {
    MSG_BOXEN.showGrid(cap, data, cols);
  }

  public static boolean nativeConfirm(Object... obj) {
    return MSG_BOXEN.nativeConfirm(obj);
  }

  public static <E extends Enum<?> & HasCaption> void registerCaptions(Class<E> clazz) {
    Assert.notNull(clazz);
    registerCaptions(NameUtils.getClassName(clazz), clazz);
  }
  
  public static <E extends Enum<?> & HasCaption> void registerCaptions(String key, Class<E> clazz) {
    Assert.notEmpty(key);
    Assert.notNull(clazz);
    CAPTIONS.put(BeeUtils.normalize(key), clazz);
  }
  
  public static void sayHuh(Object... obj) {
    MSG_BOXEN.showInfo("Huh ?", obj);
  }

  public static void setVar(String name, Variable var) {
    Assert.notEmpty(name);
    Assert.notNull(var);

    VARS.put(name, var);
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
    MSG_BOXEN.showInfo(obj);
  }

  public static void showDialog(String cap, String msg, Throwable err) {
    if (err == null) {
      MSG_BOXEN.showInfo(cap, msg);
    } else {
      MSG_BOXEN.showError(cap, msg, err);
    }
  }

  public static void showError(Object... obj) {
    MSG_BOXEN.showError(obj);
  }

  public static void showVars(String... context) {
    int n = (context == null) ? 0 : context.length;
    List<String> names = Lists.newArrayList(); 

    if (n > 0) {
      Set<String> keys = new LinkedHashSet<String>();
      for (String z : context) {
        keys.addAll(BeeUtils.filterContext(VARS.keySet(), z));
      }
      
      names.addAll(keys);
    } else {
      names.addAll(VARS.keySet());
    }

    if (names.isEmpty()) {
      showError("no variables found", context);
    } else {
      inputVars("Variables", names, null);
    }
  }

  public static void showWidget(Widget widget) {
    MSG_BOXEN.showWidget(widget);
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
    initVars();
    initImages();
    initFavorites();

    exportMethods();
  }

  public void start() {
  }

  private native void exportMethods() /*-{
    $wnd.Bee_updateForm = $entry(@com.butent.bee.client.ui.UiHelper::updateForm(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
    $wnd.Bee_getCaption = $entry(@com.butent.bee.client.Global::getCaption(Ljava/lang/String;I));
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

    createVar(VAR_DEBUG, "Debug", BeeType.BOOLEAN, BeeUtils.toString(false));
  }
}

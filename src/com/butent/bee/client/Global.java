package com.butent.bee.client;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.ClientDefaults;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DecisionCallback;
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
import com.butent.bee.client.style.Font;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.search.Filters;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.css.values.FontSize;
import com.butent.bee.shared.data.Defaults;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.cache.CacheManager;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

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

  private static final CacheManager cache = new CacheManager();

  private static final Images.Resources images = Images.createResources();

  private static final Map<String, String> styleSheets = Maps.newHashMap();

  private static final Favorites favorites = new Favorites();

  private static final Defaults defaults = new ClientDefaults();

  private static final Search search = new Search();

  private static final Filters filters = new Filters();

  private static final Reports reports = new Reports();

  private static boolean debug;

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
        Font.getClassName(FontSize.LARGE), Font.getClassName(FontSize.MEDIUM));
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

  public static void getParameter(String module, String prm, final Consumer<String> prmConsumer) {
    if (prmConsumer == null || BeeUtils.anyEmpty(module, prm)) {
      return;
    }
    ParameterList args = BeeKeeper.getRpc().createParameters(COMMONS_MODULE);
    args.addQueryItem(COMMONS_METHOD, SVC_GET_PARAMETER);
    args.addDataItem(VAR_PARAMETERS_MODULE, module);
    args.addDataItem(VAR_PARAMETERS, prm);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (!response.hasErrors()) {
          prmConsumer.accept(response.getResponseAsString());
        }
      }
    });
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

  public static Map<String, String> getStyleSheets() {
    return styleSheets;
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
        BeeConst.UNDEF, Localized.getConstants().ok(), Localized.getConstants().cancel(), null);
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

  public static void inputWidget(String caption, IsWidget input, InputCallback callback) {
    inputWidget(caption, input, callback, null, null, Action.NO_ACTIONS);
  }

  public static void inputWidget(String caption, IsWidget input, InputCallback callback,
      String dialogStyle) {
    inputWidget(caption, input, callback, dialogStyle, null, Action.NO_ACTIONS);
  }

  public static void inputWidget(String caption, IsWidget input, InputCallback callback,
      String dialogStyle, Element target) {
    inputWidget(caption, input, callback, dialogStyle, target, Action.NO_ACTIONS);
  }

  public static void inputWidget(String caption, IsWidget input, InputCallback callback,
      String dialogStyle, Element target, Set<Action> enabledActions) {
    inpBoxen.inputWidget(caption, input, callback, dialogStyle, target, enabledActions, null);
  }

  public static boolean isDebug() {
    return debug;
  }

  public static void messageBox(String caption, Icon icon, List<String> messages,
      List<String> options, int defaultValue, ChoiceCallback callback) {
    msgBoxen.display(caption, icon, messages, options, defaultValue, callback, BeeConst.UNDEF,
        null, null, null, null);
  }

  public static boolean nativeConfirm(String... lines) {
    return msgBoxen.nativeConfirm(lines);
  }

  public static void sayHuh(String... huhs) {
    String caption;
    List<String> messages;

    if (huhs == null) {
      caption = null;
      messages = Lists.newArrayList("Huh");
    } else {
      caption = "Huh";
      messages = Lists.newArrayList(huhs);
    }

    messageBox(caption, Icon.QUESTION, messages, Lists.newArrayList("kthxbai"), 0, null);
  }

  public static void setDebug(boolean debug) {
    Global.debug = debug;
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

  public static void showInfo(List<String> messages) {
    showInfo(null, messages, null, null);
  }

  public static void showInfo(String message) {
    List<String> messages = Lists.newArrayList();
    if (!BeeUtils.isEmpty(message)) {
      messages.add(message);
    }

    showInfo(messages);
  }

  public static void showInfo(String caption, List<String> messages) {
    showInfo(caption, messages, null, null);
  }

  public static void showInfo(String caption, List<String> messages, String dialogStyle) {
    showInfo(caption, messages, dialogStyle, null);
  }

  public static void showInfo(String caption, List<String> messages, String dialogStyle,
      String closeHtml) {
    msgBoxen.showInfo(caption, messages, dialogStyle, closeHtml);
  }

  public static void showModalGrid(String caption, IsTable<?, ?> table) {
    msgBoxen.showTable(caption, table);
  }

  public static void showModalWidget(Widget widget) {
    msgBoxen.showWidget(widget);
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

  // CHECKSTYLE:OFF
  private native void exportMethods() /*-{
    $wnd.Bee_updateForm = $entry(@com.butent.bee.client.ui.UiHelper::updateForm(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
    $wnd.Bee_getCaption = $entry(@com.butent.bee.shared.ui.Captions::getCaption(Ljava/lang/String;I));
    $wnd.Bee_debug = $entry(@com.butent.bee.client.Global::debug(Ljava/lang/String;));
    $wnd.Bee_updateActor = $entry(@com.butent.bee.client.decorator.TuningHelper::updateActor(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
    $wnd.Bee_maybeTranslate = $entry(@com.butent.bee.shared.i18n.Localized::maybeTranslate(Ljava/lang/String;));
  }-*/;

  // CHECKSTYLE:ON

  private static void initCache() {
    BeeKeeper.getBus().registerDataHandler(getCache(), true);
  }

  private static void initFavorites() {
    BeeKeeper.getBus().registerRowDeleteHandler(getFavorites(), false);
    BeeKeeper.getBus().registerMultiDeleteHandler(getFavorites(), false);
  }

  private static void initImages() {
    Images.init(getImages());
  }
}

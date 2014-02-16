package com.butent.bee.client;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
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
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.images.Images;
import com.butent.bee.client.modules.commons.CommonsKeeper;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.screen.Favorites;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.search.Filters;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.css.values.FontSize;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.data.Defaults;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.cache.CacheManager;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
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

  private static final Users users = new Users();
  private static final Rooms rooms = new Rooms();

  private static final NewsAggregator newsAggregator = new NewsAggregator();

  private static boolean debug;

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
    confirm(caption, icon, messages, Localized.getConstants().yes(), Localized.getConstants().no(),
        callback);
  }

  public static void confirm(String caption, Icon icon, List<String> messages,
      String optionYes, String optionNo, ConfirmationCallback callback) {
    confirm(caption, icon, messages, optionYes, optionNo, callback, null);
  }

  public static void confirm(String caption, Icon icon, List<String> messages,
      String optionYes, String optionNo, ConfirmationCallback callback, Element target) {
    msgBoxen.confirm(caption, icon, messages, optionYes, optionNo, callback, null, null, null,
        target);
  }

  public static void confirmDelete(String caption, Icon icon, List<String> messages,
      ConfirmationCallback callback) {
    confirmDelete(caption, icon, messages, callback, null);
  }

  public static void confirmDelete(String caption, Icon icon, List<String> messages,
      ConfirmationCallback callback, Element target) {
    msgBoxen.confirm(caption, icon, messages, Localized.getConstants().delete(),
        Localized.getConstants().cancel(), callback, null,
        StyleUtils.className(FontSize.LARGE), StyleUtils.className(FontSize.MEDIUM), target);
  }

  public static void debug(String s) {
    logger.debug(s);
  }

  public static void decide(String caption, List<String> messages, DecisionCallback callback,
      int defaultValue) {
    msgBoxen.decide(caption, messages, callback, defaultValue, null, null, null, null);
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

  public static NewsAggregator getNewsAggregator() {
    return newsAggregator;
  }

  public static void getParameter(String prm, final Consumer<String> prmConsumer) {
    Assert.notEmpty(prm);
    Assert.notNull(prmConsumer);

    ParameterList args = CommonsKeeper.createArgs(SVC_GET_PARAMETER);
    args.addDataItem(VAR_PARAMETER, prm);

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

  public static Rooms getRooms() {
    return rooms;
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

  public static Users getUsers() {
    return users;
  }

  public static void inputCollection(String caption, String valueCaption, final boolean unique,
      Collection<String> defaultCollection, final Consumer<Collection<String>> consumer,
      final Function<String, Editor> editorSupplier) {

    Assert.notNull(consumer);

    final HtmlTable table = new HtmlTable();
    final Consumer<String> rowCreator = new Consumer<String>() {
      @Override
      public void accept(String value) {
        Editor input = null;

        if (editorSupplier != null) {
          input = editorSupplier.apply(value);
        }
        if (input == null) {
          input = new InputText();

          if (!BeeUtils.isEmpty(value)) {
            input.setValue(value);
          }
        }
        int row = table.getRowCount();
        table.setWidget(row, 0, input.asWidget());

        final FaLabel delete = new FaLabel(FontAwesome.TRASH_O);
        delete.setTitle(Localized.getConstants().delete());
        delete.getElement().getStyle().setCursor(Cursor.POINTER);

        delete.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            for (int i = 0; i < table.getRowCount(); i++) {
              if (Objects.equal(delete, table.getWidget(i, 1))) {
                table.removeRow(i);
                break;
              }
            }
          }
        });
        table.setWidget(row, 1, delete);
      }
    };
    if (!BeeUtils.isEmpty(defaultCollection)) {
      for (String value : defaultCollection) {
        rowCreator.accept(value);
      }
    }
    FlowPanel widget = new FlowPanel();
    Label cap = new Label(valueCaption);
    StyleUtils.setTextAlign(cap.getElement(), TextAlign.CENTER);
    widget.add(cap);

    widget.add(table);

    FaLabel add = new FaLabel(FontAwesome.PLUS);
    add.setTitle(Localized.getConstants().actionAdd());
    add.getElement().getStyle().setCursor(Cursor.POINTER);
    StyleUtils.setTextAlign(add.getElement(), TextAlign.CENTER);

    add.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        rowCreator.accept(null);
        UiHelper.focus(table.getWidget(table.getRowCount() - 1, 0));
      }
    });
    widget.add(add);

    inputWidget(caption, widget, new InputCallback() {
      @Override
      public String getErrorMessage() {
        String error = super.getErrorMessage();

        if (BeeUtils.isEmpty(error)) {
          Set<String> values = Sets.newHashSet();

          for (int i = 0; i < table.getRowCount(); i++) {
            Editor input = (Editor) table.getWidget(i, 0);
            String value = input.getNormalizedValue();

            if (BeeUtils.isEmpty(value)) {
              error = Localized.getConstants().valueRequired();
            } else if (unique && values.contains(BeeUtils.normalize(value))) {
              error = Localized.getMessages().valueExists(value);
            } else {
              values.add(BeeUtils.normalize(value));
              continue;
            }
            UiHelper.focus(input.asWidget());
            break;
          }
        }
        return error;
      }

      @Override
      public void onSuccess() {
        Collection<String> result;

        if (unique) {
          result = Sets.newLinkedHashSet();
        } else {
          result = Lists.newArrayList();
        }
        for (int i = 0; i < table.getRowCount(); i++) {
          result.add(((Editor) table.getWidget(i, 0)).getNormalizedValue());
        }
        consumer.accept(result);
      }
    });
  }

  public static void inputMap(String caption, final String keyCaption, final String valueCaption,
      Map<String, String> map, final Consumer<Map<String, String>> consumer) {

    final HtmlTable table = new HtmlTable();
    final BiConsumer<String, String> rowCreator = new BiConsumer<String, String>() {
      @Override
      public void accept(String key, String value) {
        int row = table.getRowCount();
        InputText input = new InputText();
        table.setWidget(row, 0, input);

        if (!BeeUtils.isEmpty(key)) {
          input.setValue(key);
        }
        input = new InputText();
        table.setWidget(row, 1, input);

        if (!BeeUtils.isEmpty(value)) {
          input.setValue(value);
        }
        final FaLabel delete = new FaLabel(FontAwesome.TRASH_O);
        delete.setTitle(Localized.getConstants().delete());
        delete.getElement().getStyle().setCursor(Cursor.POINTER);

        delete.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            for (int i = 1; i < table.getRowCount(); i++) {
              if (Objects.equal(delete, table.getWidget(i, 2))) {
                table.removeRow(i);
                break;
              }
            }
          }
        });
        table.setWidget(row, 2, delete);
      }
    };
    Label cap = new Label(keyCaption);
    StyleUtils.setMinWidth(cap, 100);
    StyleUtils.setTextAlign(cap.getElement(), TextAlign.CENTER);
    table.setWidget(0, 0, cap);

    cap = new Label(valueCaption);
    StyleUtils.setMinWidth(cap, 100);
    StyleUtils.setTextAlign(cap.getElement(), TextAlign.CENTER);
    table.setWidget(0, 1, cap);

    for (String key : map.keySet()) {
      rowCreator.accept(key, map.get(key));
    }
    FlowPanel widget = new FlowPanel();
    widget.add(table);

    FaLabel add = new FaLabel(FontAwesome.PLUS);
    add.setTitle(Localized.getConstants().actionAdd());
    add.getElement().getStyle().setCursor(Cursor.POINTER);
    StyleUtils.setTextAlign(add.getElement(), TextAlign.CENTER);

    add.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        rowCreator.accept(null, null);
        UiHelper.focus(table.getWidget(table.getRowCount() - 1, 0));
      }
    });
    widget.add(add);

    inputWidget(caption, widget, new InputCallback() {
      @Override
      public String getErrorMessage() {
        String error = super.getErrorMessage();

        if (BeeUtils.isEmpty(error)) {
          Set<String> values = Sets.newHashSet();

          for (int i = 1; i < table.getRowCount(); i++) {
            InputText input = (InputText) table.getWidget(i, 0);

            if (BeeUtils.isEmpty(input.getValue())) {
              error = Localized.getConstants().valueRequired();
            } else if (values.contains(BeeUtils.normalize(input.getValue()))) {
              error = Localized.getMessages().valueExists(input.getValue());
            } else {
              values.add(BeeUtils.normalize(input.getValue()));
              continue;
            }
            UiHelper.focus(input);
            break;
          }
        }
        return error;
      }

      @Override
      public void onSuccess() {
        Map<String, String> result = Maps.newLinkedHashMap();

        for (int i = 1; i < table.getRowCount(); i++) {
          result.put(((InputText) table.getWidget(i, 0)).getValue(),
              ((InputText) table.getWidget(i, 1)).getValue());
        }
        consumer.accept(result);
      }
    });
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

  public static void messageBox(String caption, Icon icon, String message) {
    messageBox(caption, icon, Lists.newArrayList(message),
        Lists.newArrayList(Localized.getConstants().ok()), 0, null);
  }

  public static void messageBox(String caption, Icon icon, List<String> messages,
      List<String> options, int defaultValue, ChoiceCallback callback) {
    msgBoxen.display(caption, icon, messages, options, defaultValue, callback, BeeConst.UNDEF,
        null, null, null, null, null);
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

  public static void setParameter(String prm, String value) {
    Assert.notEmpty(prm);

    ParameterList args = CommonsKeeper.createArgs(SVC_SET_PARAMETER);
    args.addDataItem(VAR_PARAMETER, prm);

    if (!BeeUtils.isEmpty(value)) {
      args.addDataItem(VAR_PARAMETER_VALUE, value);
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());
      }
    });
  }

  public static void showError(List<String> messages) {
    showError(Localized.getConstants().error(), messages, null, null);
  }

  public static void showError(String message) {
    List<String> messages = Lists.newArrayList();
    if (!BeeUtils.isEmpty(message)) {
      messages.add(message);
    }

    showError(Localized.getConstants().error(), messages);
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

  public static void showGrid(String caption, IsTable<?, ?> table) {
    Assert.notNull(table, "showGrid: table is null");
    CellGrid grid = GridFactory.simpleGrid(caption, table,
        BeeKeeper.getScreen().getActivePanelWidth());
    if (grid != null) {
      BeeKeeper.getScreen().showWidget(grid, true);
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
    showModalWidget(null, widget, null);
  }

  public static void showModalWidget(String caption, Widget widget) {
    showModalWidget(caption, widget, null);
  }

  public static void showModalWidget(Widget widget, Element target) {
    showModalWidget(null, widget, target);
  }

  public static void showModalWidget(String caption, Widget widget, Element target) {
    msgBoxen.showWidget(caption, widget, target);
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
    initNewsAggregator();

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

  private static void initNewsAggregator() {
    BeeKeeper.getBus().registerDataHandler(getNewsAggregator(), true);
  }
}

package com.butent.bee.client.cli;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.Video;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.ajaxloader.AjaxKeyRepository;
import com.butent.bee.client.ajaxloader.AjaxLoader;
import com.butent.bee.client.ajaxloader.ClientLocation;
import com.butent.bee.client.canvas.CanvasDemo;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcList;
import com.butent.bee.client.composite.SliderBar;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.JsData;
import com.butent.bee.client.decorator.TuningFactory;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.ComputedStyles;
import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Features;
import com.butent.bee.client.dom.Font;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.dom.Stacking;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.language.DetectionCallback;
import com.butent.bee.client.language.DetectionResult;
import com.butent.bee.client.language.Language;
import com.butent.bee.client.language.Translation;
import com.butent.bee.client.language.TranslationCallback;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.logging.ClientLogManager;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.ui.CompositeService;
import com.butent.bee.client.ui.DsnService;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.WidgetInitializer;
import com.butent.bee.client.utils.Browser;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.visualization.showcase.Showcase;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InlineHtml;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.Meter;
import com.butent.bee.client.widget.Progress;
import com.butent.bee.client.widget.Svg;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Resource;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.ExtendedPropertiesData;
import com.butent.bee.shared.data.PropertiesData;
import com.butent.bee.shared.data.StringMatrix;
import com.butent.bee.shared.data.TableColumn;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;
import com.butent.bee.shared.utils.Wildcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import elemental.js.JsBrowser;
import elemental.js.css.JsCSSRuleList;
import elemental.js.css.JsCSSStyleSheet;
import elemental.js.stylesheets.JsStyleSheetList;

/**
 * Contains the engine for processing client side command line interface commands.
 */

public class CliWorker {

  private static final BeeLogger logger = LogUtils.getLogger(CliWorker.class);
  
  private static boolean cornified = false;

  public static void execute(String line) {
    if (BeeUtils.isEmpty(line)) {
      return;
    }

    String v = line.trim();
    String[] arr = BeeUtils.split(v, BeeConst.CHAR_SPACE);

    String z = arr[0].toLowerCase();
    String args = (arr.length > 1) ? v.substring(z.length()).trim() : BeeConst.STRING_EMPTY;

    if (z.equals("?")) {
      whereAmI();
    } else if (z.startsWith("ajaxk") || z.startsWith("apik") || z.startsWith("gook")) {
      doAjaxKeys(arr);
    } else if (z.equals("audio")) {
      playAudio(args);
    } else if (z.equals("browser") || z.startsWith("wind")) {
      showBrowser(arr);
    } else if (z.equals("cache")) {
      showExtData(Global.getCache().getExtendedInfo());
    } else if (z.startsWith("cap")) {
      showCaptions();
    } else if (z.equals("canvas")) {
      new CanvasDemo().start();
    } else if (BeeUtils.inList(z, "center", "east", "north", "south", "screen", "west")) {
      doScreen(arr);
    } else if (z.equals("charset")) {
      getCharsets();
    } else if (z.startsWith("cho")) {
      showChoice(arr);
    } else if (z.equals("class")) {
      getClassInfo(args);
    } else if (z.equals("clear")) {
      clear(args);
    } else if (z.startsWith("client")) {
      showClientLocation();
    } else if (z.startsWith("collect")) {
      doCollections(arr);
    } else if (z.startsWith("column")) {
      showExtData(Data.getColumnMapper().getExtendedInfo());
    } else if (z.startsWith("data")) {
      showDataInfo(args);
    } else if (z.startsWith("conf")) {
      BeeKeeper.getRpc().invoke("configInfo");
    } else if (z.startsWith("conn") || z.equals("http")) {
      BeeKeeper.getRpc().invoke("connectionInfo");
    } else if (z.equals("cornify")) {
      cornify(arr);
    } else if (z.startsWith("dbinf")) {
      BeeKeeper.getRpc().makeGetRequest(Service.DB_INFO);
    } else if (z.startsWith("decor")) {
      if (BeeUtils.isEmpty(args)) {
        showExtData(TuningFactory.getExtendedInfo());
      } else {
        TuningFactory.refresh();
      }
    } else if (z.startsWith("df")) {
      showDateFormat(args);
    } else if (z.startsWith("dim")) {
      showDimensions(args);
    } else if (z.equals("dsn")) {
      CompositeService.doService(new DsnService().name(), DsnService.SVC_GET_DSNS);
    } else if (z.startsWith("dt")) {
      showDate(z, args);
    } else if (BeeUtils.inList(z, "dir", "file", "get", "download", "src")) {
      getResource(arr);
    } else if (z.equals("eval")) {
      eval(v, arr);
    } else if (BeeUtils.inList(z, "f", "func")) {
      showFunctions(v, arr);
    } else if (z.equals("form") && arr.length == 2) {
      FormFactory.openForm(arr[1]);
    } else if (z.startsWith("forminf") || z.equals("ff")) {
      showPropData(FormFactory.getInfo());
    } else if (z.equals("fs")) {
      getFs();
    } else if (z.equals("gen")) {
      BeeKeeper.getRpc().sendText(Service.GENERATE, BeeUtils.joinWords(arr[1], arr[2],
          ArrayUtils.getQuietly(arr, 3), ArrayUtils.getQuietly(arr, 4)));
    } else if (z.equals("geo")) {
      showGeo();
    } else if (z.equals("grid") && arr.length == 2) {
      GridFactory.openGrid(arr[1]);
    } else if (z.startsWith("gridinf")) {
      GridFactory.showGridInfo(args);
    } else if (z.equals("gwt")) {
      showGwt();
    } else if (BeeUtils.inList(z, "h5", "html5", "supp", "support")) {
      showSupport();
    } else if (z.equals("id")) {
      showElement(v, arr);
    } else if (z.startsWith("inp") && z.contains("type")) {
      showInputTypes();
    } else if (z.startsWith("inp") && z.contains("box") || z.equals("prompt")) {
      showInputBox(arr);
    } else if (z.equals("jdbc")) {
      doJdbc();
    } else if (BeeUtils.inList(z, "keys", "pk")) {
      getKeys(arr);
    } else if (z.startsWith("like") && arr.length >= 3) {
      doLike(arr);
    } else if (z.equals("loaders")) {
      BeeKeeper.getRpc().invoke("loaderInfo");
    } else if (z.equals("locale")) {
      doLocale(arr);
    } else if (z.equals("log")) {
      doLog(arr);
    } else if (z.equals("menu")) {
      doMenu();
    } else if (z.equals("meter")) {
      showMeter(arr);
    } else if (z.equals("md5")) {
      digest(v);
    } else if (z.equals("nf") && arr.length >= 3) {
      logger.info(NumberFormat.getFormat(arr[1]).format(BeeUtils.toDouble(arr[2])));
    } else if (z.equals("notify") && arr.length >= 2) {
      showNotes(args);
    } else if (BeeUtils.inList(z, "p", "prop")) {
      showProperties(v, arr);
    } else if (z.startsWith("ping")) {
      BeeKeeper.getRpc().makeGetRequest(Service.DB_PING);
    } else if (z.startsWith("print")) {
      print(args);
    } else if (z.equals("progress")) {
      showProgress(arr);
    } else if (z.equals("rebuild")) {
      rebuildSomething(args);
    } else if (z.equals("rpc")) {
      showRpc();
    } else if (z.startsWith("selector") && arr.length >= 2) {
      querySelector(z, args);
    } else if (z.startsWith("serv") || z.startsWith("sys")) {
      BeeKeeper.getRpc().invoke("systemInfo");
    } else if (z.equals("settings")) {
      showPropData(Settings.getInfo());
    } else if (z.startsWith("sheets")) {
      Global.showGrid(new PropertiesData(Global.getStylesheets()));
    } else if (z.equals("size") && arr.length >= 2) {
      showSize(arr);
    } else if (z.equals("slider")) {
      showSlider(arr);
    } else if (z.equals("sql")) {
      doSql(args);
    } else if (z.equals("stack")) {
      showStack();
    } else if (z.equals("stacking") || z.startsWith("zind") || z.startsWith("z-ind")) {
      showPropData(Stacking.getInfo());
    } else if (z.startsWith("stor")) {
      storage(arr);
    } else if (z.equals("style")) {
      style(v, arr);
    } else if (z.equals("svg")) {
      showSvg(arr);
    } else if (z.equals("table")) {
      showTableInfo(args);
    } else if (z.equals("tables")) {
      BeeKeeper.getRpc().makeGetRequest(Service.DB_TABLES);
    } else if (z.equals("tiles")) {
      BeeKeeper.getScreen().showInfo();
    } else if (z.startsWith("tran") || z.startsWith("detec")) {
      translate(arr, z.startsWith("detec"));
    } else if (z.equals("uc") || "unicode".startsWith(z)) {
      unicode(arr);
    } else if (z.startsWith("unit")) {
      showUnits(arr);
    } else if (z.startsWith("user")) {
      showPropData(BeeKeeper.getUser().getInfo());
    } else if (z.equals("vars")) {
      showVars(arr);
    } else if (z.equals("video")) {
      playVideo(args);
    } else if (z.startsWith("view")) {
      showViewInfo(args);
    } else if (z.startsWith("viz")) {
      Showcase.open();
    } else if (z.equals("vm")) {
      BeeKeeper.getRpc().invoke("vmInfo");
    } else if (z.equals("widget") && arr.length >= 2) {
      showWidgetInfo(arr);
    } else if (z.startsWith("xml")) {
      showXmlInfo(arr);
    } else if (z.equals("mail")) {
      BeeKeeper.getRpc().sendText(Service.MAIL, args);

    } else {
      Global.inform("wtf", v);
    }
  }

  private static void clear(String args) {
    if (BeeUtils.isEmpty(args) || BeeUtils.startsSame(args, "log")) {
      ClientLogManager.clearPanel();

    } else if (BeeUtils.startsSame(args, "grids")) {
      GridFactory.clearDescriptionCache();
      debugWithSeparator("grid cache cleared");
    
    } else if (BeeUtils.startsSame(args, "forms")) {
      FormFactory.clearDescriptionCache();
      debugWithSeparator("form cache cleared");
    
    } else if (BeeUtils.startsSame(args, "cache")) {
      Global.getCache().clear();
      debugWithSeparator("cache cleared");
    
    } else if (BeeUtils.startsSame(args, "rpc")) {
      BeeKeeper.getRpc().getRpcList().clear();
      debugWithSeparator("rpc list cleared");
    }
  }

  private static void cornify(String[] arr) {
    if (!cornified) {
      DomUtils.injectExternalScript("http://www.cornify.com/js/cornify.js");
      cornified = true;
    }

    final int cnt = BeeUtils.clamp(BeeUtils.toInt(ArrayUtils.getQuietly(arr, 1)), 1, 50);

    int delay = BeeUtils.toInt(ArrayUtils.getQuietly(arr, 2));
    if (delay <= 0) {
      delay = 2000;
    }

    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
      private int counter = 0;

      @Override
      public boolean execute() {
        cornifyAdd();
        return (++counter < cnt);
      }
    }, delay);
  }

  private static native void cornifyAdd() /*-{
    try {
      $wnd.cornify_add();
    } catch (err) {
    }
  }-*/;
  
  private static void debugWithSeparator(String message) {
    logger.debug(message);
    logger.addSeparator();
  }

  private static void digest(String v) {
    String src = null;
    int p = v.indexOf(BeeConst.CHAR_SPACE);

    if (p > 0) {
      String z = v.substring(p + 1).trim();

      if (BeeUtils.isDigit(z)) {
        int x = BeeUtils.toInt(z);
        if (BeeUtils.betweenInclusive(x, 1, BeeUtils.exp10(6))) {
          src = BeeUtils.randomString(x, x, BeeConst.CHAR_SPACE, '\u0800');
        } else {
          src = z;
        }
      } else if (z.length() > 0) {
        src = z;
      }
    }

    if (src == null) {
      src = BeeUtils.randomString(10, 20, BeeConst.CHAR_SPACE, '\u0400');
    }

    if (src.length() > 100) {
      logger.info("Source length", src.length());
    } else {
      logger.info(Codec.escapeUnicode(src));
    }
    logger.info(BeeConst.CLIENT, Codec.md5(src));
    BeeKeeper.getRpc().makePostRequest(Service.GET_DIGEST, ContentType.HTML, src);
  }

  private static void doAjaxKeys(String[] arr) {
    if (ArrayUtils.length(arr) == 3) {
      String loc = arr[1];
      String key = arr[2];

      if (Global.nativeConfirm("add api key", loc, key)) {
        AjaxKeyRepository.putKey(loc, key);
      }
    }

    Map<String, String> keyMap = AjaxKeyRepository.getKeys();
    if (BeeUtils.isEmpty(keyMap)) {
      logger.warning("api key repository is empty");
      return;
    }

    List<Property> lst = new ArrayList<Property>();
    for (Map.Entry<String, String> entry : keyMap.entrySet()) {
      lst.add(new Property(entry.getKey(), entry.getValue()));
    }

    showPropData(lst, "Location", "Api Key");
  }

  private static void doCollections(String[] arr) {
    int size = 2000;
    int query = 20;

    for (int i = 1; i < arr.length; i++) {
      if (BeeUtils.isPositiveInt(arr[i])) {
        if (i == 1) {
          size = BeeUtils.toInt(arr[i]);
        } else {
          query = BeeUtils.toInt(arr[i]);
        }
      }
    }

    List<Integer> lia = Lists.newArrayList();
    List<Long> lla = Lists.newArrayList();
    List<Double> lda = Lists.newArrayList();
    List<String> lsa = Lists.newArrayList();

    List<Integer> lil = Lists.newLinkedList();
    List<Long> lll = Lists.newLinkedList();
    List<Double> ldl = Lists.newLinkedList();
    List<String> lsl = Lists.newLinkedList();

    Map<Integer, String> mih = Maps.newHashMap();
    Map<Long, String> mlh = Maps.newHashMap();
    Map<Double, String> mdh = Maps.newHashMap();
    Map<String, String> msh = Maps.newHashMap();

    Map<Integer, String> mil = Maps.newLinkedHashMap();
    Map<Long, String> mll = Maps.newLinkedHashMap();
    Map<Double, String> mdl = Maps.newLinkedHashMap();
    Map<String, String> msl = Maps.newLinkedHashMap();

    for (int i = 0; i < size; i++) {
      String s = Integer.toString(i);

      lia.add(i);
      lla.add((long) i);
      lda.add((double) i);
      lsa.add(s);

      lil.add(i);
      lll.add((long) i);
      ldl.add((double) i);
      lsl.add(s);

      mih.put(i, s);
      mlh.put((long) i, s);
      mdh.put((double) i, s);
      msh.put(s, s);

      mil.put(i, s);
      mll.put((long) i, s);
      mdl.put((double) i, s);
      msl.put(s, s);
    }

    logger.debug(size, query);

    double x = 0;
    long start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += lia.indexOf((int) (Math.random() * size));
    }
    logger.debug("lia", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += lla.indexOf((long) (Math.random() * size));
    }
    logger.debug("lla", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += lda.indexOf(Math.floor(Math.random() * size));
    }
    logger.debug("lda", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += lsa.indexOf(Integer.toString((int) (Math.random() * size)));
    }
    logger.debug("lsa", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += lil.indexOf((int) (Math.random() * size));
    }
    logger.debug("lil", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += lll.indexOf((long) (Math.random() * size));
    }
    logger.debug("lll", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += ldl.indexOf(Math.floor(Math.random() * size));
    }
    logger.debug("ldl", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += lsl.indexOf(Integer.toString((int) (Math.random() * size)));
    }
    logger.debug("lsl", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += (mih.get((int) (Math.random() * size)) == null) ? 0 : 1;
    }
    logger.debug("mih", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += (mlh.get((long) (Math.random() * size)) == null) ? 0 : 1;
    }
    logger.debug("mlh", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += (mdh.get(Math.floor(Math.random() * size)) == null) ? 0 : 1;
    }
    logger.debug("mdh", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += (msh.get(Integer.toString((int) (Math.random() * size))) == null) ? 0 : 1;
    }
    logger.debug("msh", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += (mil.get((int) (Math.random() * size)) == null) ? 0 : 1;
    }
    logger.debug("mil", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += (mll.get((long) (Math.random() * size)) == null) ? 0 : 1;
    }
    logger.debug("mll", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += (mdl.get(Math.floor(Math.random() * size)) == null) ? 0 : 1;
    }
    logger.debug("mdl", System.currentTimeMillis() - start, x);

    x = 0;
    start = System.currentTimeMillis();
    for (int i = 0; i < query; i++) {
      x += (msl.get(Integer.toString((int) (Math.random() * size))) == null) ? 0 : 1;
    }
    logger.debug("msl", System.currentTimeMillis() - start, x);
    logger.addSeparator();
  }

  private static void doJdbc() {
    final List<String> varNames = Lists.newArrayList(Service.VAR_JDBC_QUERY,
        Service.VAR_CONNECTION_AUTO_COMMIT,
        Service.VAR_CONNECTION_HOLDABILITY,
        Service.VAR_CONNECTION_READ_ONLY,
        Service.VAR_CONNECTION_TRANSACTION_ISOLATION,
        Service.VAR_STATEMENT_CURSOR_NAME,
        Service.VAR_STATEMENT_ESCAPE_PROCESSING,
        Service.VAR_STATEMENT_FETCH_DIRECTION,
        Service.VAR_STATEMENT_FETCH_SIZE,
        Service.VAR_STATEMENT_MAX_FIELD_SIZE,
        Service.VAR_STATEMENT_MAX_ROWS,
        Service.VAR_STATEMENT_POOLABLE,
        Service.VAR_STATEMENT_QUERY_TIMEOUT,
        Service.VAR_STATEMENT_RS_TYPE,
        Service.VAR_STATEMENT_RS_CONCURRENCY,
        Service.VAR_STATEMENT_RS_HOLDABILITY,
        Service.VAR_RESULT_SET_FETCH_DIRECTION,
        Service.VAR_RESULT_SET_FETCH_SIZE,
        Service.VAR_JDBC_RETURN);

    Global.inputVars("Jdbc Test", varNames, new ConfirmationCallback() {
      @Override
      public boolean onConfirm(Popup popup) {
        String sql = Global.getVarValue(Service.VAR_JDBC_QUERY);
        if (BeeUtils.isEmpty(sql)) {
          Global.showError("Query not specified");
          return false;
        } else {
          BeeKeeper.getRpc().makePostRequest(Service.DB_JDBC,
              XmlUtils.fromVars(Service.XML_TAG_DATA, varNames));
          return true;
        }
      }
    });
  }

  private static void doLike(String[] arr) {
    int len = ArrayUtils.length(arr);
    if (len < 3) {
      Global.sayHuh(ArrayUtils.join(BeeConst.STRING_SPACE, arr));
      return;
    }

    String mode = ArrayUtils.getQuietly(arr, 0);
    String input = ArrayUtils.join(BeeConst.STRING_SPACE, arr, 1, len - 1);
    String expr = ArrayUtils.getQuietly(arr, len - 1);

    boolean sens = mode.indexOf('+') > 0;
    boolean insens = mode.indexOf('-') > 0;
    String defCase = null;
    boolean match;

    if (BeeUtils.containsSame(mode, "s")) {
      if (sens || insens) {
        match = Wildcards.isSqlLike(input, expr, sens);
      } else {
        defCase = BeeUtils.joinWords("sql", BooleanValue.pack(Wildcards.isSqlCaseSensitive()));
        match = Wildcards.isSqlLike(input, expr);
      }
    } else if (BeeUtils.containsSame(mode, "f")) {
      if (sens || insens) {
        match = Wildcards.isFsLike(input, expr, sens);
      } else {
        defCase = BeeUtils.joinWords("fs", BooleanValue.pack(Wildcards.isFsCaseSensitive()));
        match = Wildcards.isFsLike(input, expr);
      }
    } else {
      if (sens || insens) {
        match = Wildcards.isLike(input, expr, sens);
      } else {
        defCase = BeeUtils.joinWords("def",
            BooleanValue.pack(Wildcards.isDefaultCaseSensitive()));
        match = Wildcards.isLike(input, expr);
      }
    }
    Global.inform(mode, NameUtils.addName("input", input), NameUtils.addName("pattern", expr),
        NameUtils.addName("case", sens ? "sensitive" : (insens ? "insensitive" : defCase)),
        NameUtils.addName("match", BeeUtils.toString(match)));
  }

  private static void doLocale(String[] arr) {
    String mode = ArrayUtils.getQuietly(arr, 1);
    if (BeeUtils.isEmpty(mode)) {
      showExtData(LocaleUtils.getInfo());
      return;
    }

    String lang = ArrayUtils.getQuietly(arr, 2);
    BeeKeeper.getRpc().invoke("localeInfo", ContentType.TEXT, BeeUtils.joinWords(mode, lang));
  }

  private static void doLog(String[] arr) {
    if (ArrayUtils.length(arr) > 1) {
      String z = arr[1];

      if (BeeUtils.inList(z, BeeConst.STRING_ZERO, BeeConst.STRING_MINUS)) {
        ClientLogManager.setPanelVisible(false);
      } else if (BeeUtils.isDigit(z)) {
        ClientLogManager.setPanelSize(BeeUtils.toInt(z));
      } else if (BeeUtils.startsSame(z, "clear")) {
        ClientLogManager.clearPanel();
      } else {
        ClientLogManager.setPanelVisible(true);
        logger.info((Object[]) arr);
        logger.addSeparator();
      }

      return;
    }

    for (LogLevel lvl : LogLevel.values()) {
      logger.log(lvl, lvl.name().toLowerCase());
    }
    logger.addSeparator();
  }

  private static void doMenu() {
    BeeKeeper.getMenu().showMenuInfo();
  }

  private static void doScreen(String[] arr) {
    Split screen = BeeKeeper.getScreen().getScreenPanel();
    Assert.notNull(screen);

    String p1 = ArrayUtils.getQuietly(arr, 0);
    String p2 = ArrayUtils.getQuietly(arr, 1);

    if (BeeUtils.same(p1, "screen")) {
      showExtData(screen.getExtendedInfo());
      return;
    }

    Direction dir = DomUtils.getDirection(p1);
    if (dir == null) {
      Global.sayHuh(p1, p2);
      return;
    }

    if (BeeUtils.isEmpty(p2)) {
      showExtData(screen.getDirectionInfo(dir));
      return;
    }

    double size = BeeUtils.toDouble(p2);
    if (Double.isNaN(size)) {
      Global.showError(p1, p2, "NaN");
      return;
    }

    screen.setDirectionSize(dir, size);
  }

  private static void doSql(String sql) {
    BeeKeeper.getRpc().sendText(Service.DO_SQL, sql,
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);

            if (response.hasResponse(BeeRowSet.class)) {
              BeeRowSet rs = BeeRowSet.restore((String) response.getResponse());

              if (rs.isEmpty()) {
                BeeKeeper.getScreen().updateActivePanel(new BeeLabel("RowSet is empty"));
              } else {
                Global.showGrid(rs);
              }
            }
          }
        });
  }

  private static void eval(String v, String[] arr) {
    String xpr = v.substring(arr[0].length()).trim();

    if (BeeUtils.isEmpty(xpr)) {
      Global.sayHuh(v);
    } else {
      Global.inform(xpr, JsUtils.evalToString(xpr));
    }
  }

  private static void getCharsets() {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_RESOURCE);
    params.addPositionalHeader("cs");

    BeeKeeper.getRpc().makeGetRequest(params);
  }

  private static void getClassInfo(String args) {
    Pair<String, String> params = Pair.split(args);

    String cls = (params == null) ? null : params.getA();
    String pck = (params == null) ? null : params.getB();

    if (BeeUtils.isEmpty(cls)) {
      Global.showError("Class name not specified");
    } else if (cls.length() < 2) {
      Global.showError("Class name", cls, "too short");
    } else {
      BeeKeeper.getRpc().makePostRequest(Service.GET_CLASS_INFO,
          XmlUtils.createString(Service.XML_TAG_DATA,
              Service.VAR_CLASS_NAME, cls, Service.VAR_PACKAGE_LIST, pck));
    }
  }

  private static void getFs() {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_RESOURCE);
    params.addPositionalHeader("fs");

    BeeKeeper.getRpc().makeGetRequest(params);
  }

  private static native void getGeo(Element element) /*-{
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(showPosition);
    } else {
      element.innerHTML = "no geolocation support";
    }

    function showPosition(position) {
      var lat = position.coords.latitude;
      var lng = position.coords.longitude;
      element.innerHTML = "Lat = " + lat + ", Lng = " + lng;
    }
  }-*/;

  private static void getKeys(String[] arr) {
    int parCnt = ArrayUtils.length(arr) - 1;
    if (parCnt <= 0) {
      Global.showError("getKeys", "table not specified");
      return;
    }

    ParameterList params = BeeKeeper.getRpc().createParameters(
        BeeUtils.same(arr[0], "pk") ? Service.DB_PRIMARY : Service.DB_KEYS);
    for (int i = 0; i < parCnt; i++) {
      params.addPositionalHeader(arr[i + 1]);
    }
    BeeKeeper.getRpc().makeGetRequest(params);
  }
  
  private static void getResource(String[] arr) {
    if (ArrayUtils.length(arr) < 2) {
      Global.sayHuh(ArrayUtils.join(BeeConst.STRING_SPACE, arr));
      return;
    }

    if (BeeUtils.same(arr[0], "download")) {
      String url = GWT.getModuleBaseURL() + "file/" 
          + Codec.encodeBase64(ArrayUtils.join(BeeConst.STRING_SPACE, arr, 1));
      Window.open(url, "", "");
      return;
    }

    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_RESOURCE);
    for (String v : arr) {
      params.addPositionalHeader(v);
    }

    BeeKeeper.getRpc().makeGetRequest(params);
  }

  private static void playAudio(final String src) {
    if (BeeUtils.isEmpty(src)) {
      logger.warning("source not specified");
      return;
    }

    final Audio widget = Audio.createIfSupported();
    if (widget == null) {
      logger.severe("audio not supported");
      return;
    }

    widget.getAudioElement().setSrc(src);
    widget.getAudioElement().setControls(true);

    widget.addDomHandler(new ErrorHandler() {
      @Override
      public void onError(ErrorEvent event) {
        BeeKeeper.getScreen().notifyWarning(src, EventUtils.transformMediaError(widget.getError()));
      }
    }, ErrorEvent.getType());

    BeeKeeper.getScreen().updateActivePanel(widget, ScrollBars.BOTH);
  }

  private static void playVideo(String args) {
    if (!Video.isSupported()) {
      logger.severe("video not supported");
      return;
    }

    final String src = BeeUtils.notEmpty(args,
        "http://people.opera.com/shwetankd/webm/sunflower.webm");

    final Video widget = Video.createIfSupported();
    Assert.notNull(widget);
    widget.getVideoElement().setSrc(src);
    widget.getVideoElement().setControls(true);

    widget.addDomHandler(new ErrorHandler() {
      @Override
      public void onError(ErrorEvent event) {
        BeeKeeper.getScreen().notifyWarning(src, EventUtils.transformMediaError(widget.getError()));
      }
    }, ErrorEvent.getType());

    BeeKeeper.getScreen().updateActivePanel(widget, ScrollBars.BOTH);
  }

  private static void print(String args) {
    Widget widget = null;
    Element element = null;

    if (BeeUtils.same(args, "log")) {
      widget = ClientLogManager.getLogPanel();
      if (widget == null) {
        Global.showError("log widget not available");
        return;
      }

    } else if (!BeeUtils.isEmpty(args)) {
      element = DomUtils.getElementQuietly(args);
      if (element == null) {
        Global.showError(args, "element not found");
        return;
      }

    } else {
      widget = BeeKeeper.getScreen().getActiveWidget();
      if (widget == null) {
        Global.showError("active widget not available");
        return;
      }
    }

    if (widget != null) {
      if (widget instanceof Printable) {
        Printer.print((Printable) widget);

      } else if (widget instanceof HasId) {
        final Element root = widget.getElement();
        final String id = root.getId();

        Printer.print(new Printable() {
          @Override
          public Element getPrintElement() {
            return root;
          }

          @Override
          public boolean onPrint(Element source, Element target) {
            if (id.equals(source.getId())) {
              int width = Math.max(source.getScrollWidth(), DomUtils.getClientWidth() / 2);
              if (width > target.getClientWidth()) {
                StyleUtils.setWidth(target, width);
              }

              int height = source.getScrollHeight();
              if (height > target.getClientHeight()) {
                StyleUtils.setHeight(target, height);
              }
            }
            return true;
          }
        });

      } else {
        Printer.print(widget.getElement(), null);
      }

    } else if (element != null) {
      Printer.print(element, null);
    }
  }

  private static void querySelector(String command, String selectors) {
    int p = command.indexOf('#');
    Element root = null;
    if (p > 0 && p < command.length() - 1) {
      String id = command.substring(p + 1);
      root = DOM.getElementById(id);
      if (root == null) {
        Global.showError(command, id, "element id not found");
        return;
      }
    }
    boolean all = command.indexOf('=') < 0;

    Element element = null;
    NodeList<Element> nodes = null;

    if (root == null) {
      if (all) {
        nodes = Selectors.getNodes(selectors);
      } else {
        element = Selectors.getElement(selectors);
      }
    } else {
      if (all) {
        nodes = Selectors.getNodes(root, selectors);
      } else {
        element = Selectors.getElement(root, selectors);
      }
    }

    if (element != null) {
      Global.inform(DomUtils.transformElement(element));
      return;
    }

    int cnt = (nodes == null) ? 0 : nodes.getLength();
    if (cnt <= 0) {
      Global.showError(command, selectors, "no elements found");
      return;
    }

    List<Property> info = PropertyUtils.createProperties(command, selectors, "Count", cnt);
    for (int i = 0; i < cnt; i++) {
      info.add(new Property(BeeUtils.progress(i + 1, cnt),
          DomUtils.transformElement(nodes.getItem(i))));
    }
    PropertiesData table = new PropertiesData(info);

    if (showModal(cnt)) {
      Global.showModalGrid("Selectors", table);
    } else {
      Global.showGrid(table);
    }
  }

  private static void rebuildSomething(String args) {
    BeeKeeper.getRpc().sendText(Service.REBUILD, args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasResponse(Resource.class)) {
          final BeeLayoutPanel p = new BeeLayoutPanel();

          final InputArea area = new InputArea(new Resource((String) response.getResponse()));
          p.add(area);
          p.setWidgetTopBottom(area, 0, Unit.EM, 2, Unit.EM);

          BeeButton button = new BeeButton("Save schema", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              if (area.isValueChanged()) {
                BeeKeeper.getRpc().sendText(Service.REBUILD, "schema " + area.getValue(),
                    new ResponseCallback() {
                      @Override
                      public void onResponse(ResponseObject resp) {
                        Assert.notNull(resp);

                        if (resp.hasResponse(Resource.class)) {
                          p.getWidget(1).removeFromParent();
                          InputArea res =
                              new InputArea(new Resource((String) resp.getResponse()));
                          p.add(res);
                          p.setWidgetLeftRight(res, 50, Unit.PCT, 0, Unit.EM);
                          p.setWidgetTopBottom(area, 0, Unit.EM, 0, Unit.EM);
                          p.setWidgetLeftRight(area, 0, Unit.EM, 50, Unit.PCT);
                        } else {
                          Global.showError("Wrong response received");
                        }
                      }
                    });
              } else {
                Global.inform("Value has not changed", area.getDigest());
              }
            }
          });
          p.add(button);
          p.setWidgetVerticalPosition(button, Layout.Alignment.END);
          p.setWidgetLeftWidth(button, 42, Unit.PCT, 16, Unit.PCT);

          Storage stor = Storage.getSessionStorageIfSupported();

          if (stor == null) {
            BeeKeeper.getScreen().updateActivePanel(p);
          } else {
            final String tmpKey = BeeUtils.randomString(5, 5, 'a', 'z');
            stor.setItem(tmpKey, area.getValue());

            Storage.addStorageEventHandler(new StorageEvent.Handler() {
              @Override
              public void onStorageChange(StorageEvent event) {
                if (BeeUtils.same(event.getKey(), tmpKey)) {
                  BeeKeeper.getRpc().sendText(Service.REBUILD, "schema " + event.getNewValue(),
                      new ResponseCallback() {
                        @Override
                        public void onResponse(ResponseObject resp) {
                          Assert.notNull(resp);

                          if (resp.hasResponse(Resource.class)) {
                            InputArea res =
                                new InputArea(new Resource((String) resp.getResponse()));
                            Global.inform(res.getValue());
                          } else {
                            Global.showError("Wrong response received");
                          }
                        }
                      });
                  Storage.removeStorageEventHandler(this);
                }
              }
            });
            String url = GWT.getHostPageBaseURL() + "SqlDesigner/index.html?keyword=" + tmpKey;
            String xml = "<Form><ResizePanel><Frame url=\"" + url + "\" /></ResizePanel></Form>";

            FormFactory.openForm(FormFactory.parseFormDescription(xml), null);
          }
        } else if (response.hasResponse()) {
          showPropData(PropertyUtils.restoreProperties((String) response.getResponse()));
        }
      }
    });
  }

  private static native void sampleCanvas(Element el) /*-{
    var ctx = el.getContext("2d");

    for ( var i = 0; i < 6; i++) {
      for ( var j = 0; j < 6; j++) {
        ctx.fillStyle = 'rgb(' + Math.floor(255 - 42.5 * i) + ', ' + Math.floor(255 - 42.5 * j) + ', 0)';
        ctx.fillRect(j * 25, i * 25, 25, 25);
      }
    }
  }-*/;

  private static void showBrowser(String[] arr) {
    boolean wnd = false;
    boolean loc = false;
    boolean nav = false;
    boolean scr = false;

    for (int i = 1; i < ArrayUtils.length(arr); i++) {
      switch (arr[i].toLowerCase().charAt(0)) {
        case 'w':
          wnd = true;
          break;
        case 'l':
          loc = true;
          break;
        case 'n':
          nav = true;
          break;
        case 's':
          scr = true;
          break;
      }
    }

    if (!wnd && !loc && !nav && !scr) {
      wnd = loc = nav = scr = true;
    }

    List<ExtendedProperty> info = new ArrayList<ExtendedProperty>();

    if (wnd) {
      PropertyUtils.appendChildrenToExtended(info, "Window", Browser.getWindowInfo());
    }
    if (loc) {
      PropertyUtils.appendChildrenToExtended(info, "Location", Browser.getLocationInfo());
    }
    if (nav) {
      PropertyUtils.appendChildrenToExtended(info, "Navigator", Browser.getNavigatorInfo());
    }
    if (scr) {
      PropertyUtils.appendChildrenToExtended(info, "Screen", Browser.getScreenInfo());
    }

    showExtData(info);
  }

  private static void showCaptions() {
    Set<String> keys = Global.getRegisteredCaptionKeys();
    if (BeeUtils.isEmpty(keys)) {
      logger.debug("no captions registered");
      return;
    }

    List<Property> props = PropertyUtils.createProperties("Caption Keys",
        BeeUtils.bracket(keys.size()));

    for (String key : keys) {
      for (String caption : Global.getCaptions(key)) {
        props.add(new Property(key, caption));
      }
    }

    showPropData(props);
  }

  private static void showChoice(String[] arr) {
    String caption = null;
    String prompt = null;
    List<String> options = Lists.newArrayList();
    int defaultValue = BeeConst.UNDEF;
    int timeout = BeeConst.UNDEF;
    String cancelHtml = null;

    final Holder<String> widgetName = new Holder<String>(null);
    final Holder<String> widgetStyle = Holder.of("background-color:green");

    String v;

    for (int i = 1; i < ArrayUtils.length(arr); i++) {
      if (arr[i].length() < 3 || arr[i].charAt(1) != '=') {
        options.add(arr[i]);
        continue;
      }

      char k = arr[i].toLowerCase().charAt(0);
      v = arr[i].substring(2).trim();
      if (BeeUtils.isEmpty(v)) {
        continue;
      }

      switch (k) {
        case 'c':
          caption = v;
          break;
        case 'p':
          prompt = v;
          break;
        case 'd':
          defaultValue = BeeUtils.toInt(v);
          break;
        case 't':
          timeout = BeeUtils.toInt(v);
          break;
        case 'n':
          cancelHtml = v;
          break;
        case 'i':
          widgetName.set(v);
          break;
        case 's':
          widgetStyle.set(v);
          break;
        default:
          options.add(arr[i]);
      }
    }

    if (options.isEmpty()) {
      for (int i = 0; i < BeeUtils.randomInt(1, 10); i++) {
        options.add(BeeUtils.randomString(3, 20, ' ', 'z'));
      }
    }

    Global.choice(caption, prompt, options, new ChoiceCallback() {
      @Override
      public void onCancel() {
        logger.info("cancel");
      }

      @Override
      public void onSuccess(int value) {
        logger.info("success", value);
      }

      @Override
      public void onTimeout() {
        logger.info("timeout");
      }

    }, defaultValue, timeout, cancelHtml, new WidgetInitializer() {
      @Override
      public Widget initialize(Widget widget, String name) {
        if (BeeUtils.containsSame(name, widgetName.get())) {
          StyleUtils.updateAppearance(widget, null, widgetStyle.get());
          logger.info(name, StyleUtils.getCssText(widget));
        }
        return widget;
      }
    });
  }

  private static void showClientLocation() {
    AjaxLoader.load(new Runnable() {
      @Override
      public void run() {
        ClientLocation location = AjaxLoader.getClientLocation();

        showPropData(PropertyUtils.createProperties("City", location.getCity(),
            "Country", location.getCountry(), "Country Code", location.getCountryCode(),
            "Latitude", location.getLatitude(), "Longitude", location.getLongitude(),
            "Region", location.getRegion()));
      }
    });
  }

  private static void showDataInfo(String viewName) {
    if (BeeUtils.isEmpty(viewName)) {
      List<DataInfo> list = Lists.newArrayList(Data.getDataInfoProvider().getViews());
      if (list.isEmpty()) {
        Global.showError("no data infos available");
        return;
      }

      Collections.sort(list);
      String[][] data = new String[list.size()][7];

      for (int i = 0; i < list.size(); i++) {
        DataInfo di = list.get(i);

        data[i][0] = di.getViewName();
        data[i][1] = di.getTableName();
        data[i][2] = di.getIdColumn();
        data[i][3] = di.getVersionColumn();

        data[i][4] = BeeUtils.toString(di.getColumnCount());
        data[i][5] = BeeUtils.toString(di.getViewColumns().size());
        data[i][6] = BeeUtils.toString(di.getRowCount());
      }
      showMatrix(data, "view", "table", "id", "version", "cc", "vc", "rc");

    } else if (BeeUtils.inListSame(viewName, "load", "refresh", "+", "x")) {
      Data.getDataInfoProvider().load();

    } else {
      DataInfo dataInfo = Data.getDataInfo(viewName);
      if (dataInfo != null) {
        showExtData(dataInfo.getExtendedInfo());
      }
    }
  }

  private static void showDate(String cmnd, String args) {
    DateTimeFormat dtf = null;
    String inp = null;

    char sep = ';';
    if (BeeUtils.contains(args, sep)) {
      dtf = Format.getDateTimeFormat(BeeUtils.getPrefix(args, sep));
      inp = BeeUtils.getSuffix(args, sep);
    } else if (BeeUtils.contains(cmnd, 'f') && !BeeUtils.isEmpty(args)) {
      dtf = Format.getDefaultDateTimeFormat();
      inp = args;
    }

    DateTime t = null;
    JustDate d = null;

    if (dtf != null) {
      try {
        if (BeeUtils.contains(cmnd, 's')) {
          t = dtf.parseStrict(inp);
        } else {
          t = dtf.parse(inp);
        }
      } catch (IllegalArgumentException ex) {
        Global.showError(args, dtf.getPattern(), inp, ex.toString());
      }

    } else if (!BeeUtils.isEmpty(args)) {
      t = DateTime.parse(args);
      if (t == null) {
        logger.severe("cannot parse", args);
      } else {
        d = JustDate.parse(args);
      }

    } else {
      t = new DateTime();
      d = new JustDate();
    }

    if (t == null) {
      return;
    }
    if (d == null) {
      d = new JustDate(t);
    }

    List<Property> lst = Lists.newArrayList();
    if (dtf != null) {
      lst.add(new Property("Format", dtf.getPattern()));
    }
    if (!BeeUtils.isEmpty(inp)) {
      lst.add(new Property("Input", inp));
    }

    PropertyUtils.addProperties(lst,
        "Day", d.getDays(),
        "Year", d.getYear(),
        "Month", d.getMonth(),
        "Dom", d.getDom(),
        "Dow", d.getDow(),
        "Doy", d.getDoy(),
        "String", d.toString(),
        "DateTime", TimeUtils.toDateTime(d).toString(),
        "Java Date", TimeUtils.toJava(d).toString(),
        "Time", t.getTime(),
        "Year", t.getYear(),
        "Month", t.getMonth(),
        "Dom", t.getDom(),
        "Dow", t.getDow(),
        "Doy", t.getDoy(),
        "Hour", t.getHour(),
        "Minute", t.getMinute(),
        "Second", t.getSecond(),
        "Millis", t.getMillis(),
        "Date String", t.toDateString(),
        "Time String", t.toTimeString(),
        "String", t.toString(),
        "Timezone Offset", t.getTimezoneOffset(),
        "Utc Year", t.getUtcYear(),
        "Utc Month", t.getUtcMonth(),
        "Utc Dom", t.getUtcDom(),
        "Utc Dow", t.getUtcDow(),
        "Utc Doy", t.getUtcDoy(),
        "Utc Hour", t.getUtcHour(),
        "Utc Minute", t.getUtcMinute(),
        "Utc Second", t.getUtcSecond(),
        "Utc Millis", t.getUtcMillis(),
        "Utc Date String", t.toUtcDateString(),
        "Utc Time String", t.toUtcTimeString(),
        "Utc String", t.toUtcString(),
        "JustDate", TimeUtils.toDate(t).toString(),
        "Java Date", TimeUtils.toJava(t).toString());

    showPropData(lst);
  }

  private static void showDateFormat(String args) {
    if (BeeUtils.isEmpty(args)) {
      int r = DateTimeFormat.PredefinedFormat.values().length;
      String[][] data = new String[r][3];

      DateTime d = new DateTime();
      int i = 0;
      for (DateTimeFormat.PredefinedFormat dtf : DateTimeFormat.PredefinedFormat.values()) {
        data[i][0] = dtf.toString();

        DateTimeFormat format = DateTimeFormat.getFormat(dtf);
        data[i][1] = format.getPattern();
        data[i][2] = format.format(d);
        i++;
      }

      showMatrix(data, "Format", "Pattern", "Value");

    } else {
      DateTimeFormat dtf = null;
      DateTime t = null;

      char sep = ';';
      if (BeeUtils.contains(args, sep)) {
        dtf = Format.getDateTimeFormat(BeeUtils.getPrefix(args, sep));
        t = DateTime.parse(BeeUtils.getSuffix(args, sep));
      } else {
        dtf = Format.getDateTimeFormat(args);
        t = new DateTime();
      }

      if (dtf == null || t == null) {
        logger.severe("cannot parse", args);
      } else {
        String result = dtf.format(t);
        logger.debug("format", dtf.getPattern());
        logger.debug("input", t);
        logger.debug("result", result);
        logger.addSeparator();
      }
    }
  }

  private static void showDimensions(String id) {
    final String caption;
    final List<Property> info;

    if (BeeUtils.isEmpty(id)) {
      caption = "Dimensions";
      info = PropertyUtils.createProperties(
          "Viewport width", DomUtils.getClientWidth(),
          "Viewport height", DomUtils.getClientHeight(),
          "TextBox client width", DomUtils.getTextBoxClientWidth(),
          "TextBox client height", DomUtils.getTextBoxClientHeight(),
          "TextBox offset width", DomUtils.getTextBoxOffsetWidth(),
          "TextBox offset height", DomUtils.getTextBoxOffsetHeight(),
          "CheckBox client width", DomUtils.getCheckBoxClientWidth(),
          "CheckBox client height", DomUtils.getCheckBoxClientHeight(),
          "CheckBox offset width", DomUtils.getCheckBoxOffsetWidth(),
          "CheckBox offset height", DomUtils.getCheckBoxOffsetHeight(),
          "Scrollbar width", DomUtils.getScrollBarWidth(),
          "Scrollbar height", DomUtils.getScrollBarHeight());

    } else {
      Element elem = Document.get().getElementById(id);
      if (elem == null) {
        Global.inform("element id", id, "not found");
        return;
      }
     
      caption = id;
      info = PropertyUtils.createProperties(
          "Client width", elem.getClientWidth(),
          "Client height", elem.getClientHeight(),
          "Offset width", elem.getOffsetWidth(),
          "Offset height", elem.getOffsetHeight(),
          "Scroll width", elem.getScrollWidth(),
          "Scroll height", elem.getScrollHeight(),
          "Outer width", DomUtils.getOuterWidth(elem),
          "Outer height", DomUtils.getOuterHeight(elem));
    }

    PropertiesData table = new PropertiesData(info);
    if (showModal(info.size())) {
      Global.showModalGrid(caption, table);
    } else {
      Global.showGrid(table);
    }
  }

  private static void showElement(String v, String[] arr) {
    if (ArrayUtils.length(arr) < 2) {
      Global.sayHuh(v);
      return;
    }

    JavaScriptObject obj = DOM.getElementById(arr[1]);
    if (obj == null) {
      Global.showError(arr[1], "element id not found");
      return;
    }

    String patt = ArrayUtils.getQuietly(arr, 2);
    JsArrayString prp = JsUtils.getProperties(obj, patt);

    if (JsUtils.isEmpty(prp)) {
      Global.showError(v, "properties not found");
      return;
    }

    JsData<?> table = new JsData<TableColumn>(prp, "property", "type", "value");
    table.sort(0);

    if (showModal(table.getNumberOfRows())) {
      Global.showModalGrid(v, table);
    } else {
      Global.showGrid(table);
    }
  }

  private static void showExtData(List<ExtendedProperty> data, String... columnLabels) {
    Global.showGrid(new ExtendedPropertiesData(data, columnLabels));
  }

  private static void showFunctions(String v, String[] arr) {
    if (ArrayUtils.length(arr) < 2) {
      Global.sayHuh(v);
      return;
    }

    JavaScriptObject obj;
    if (arr[1].startsWith("#")) {
      obj = DOM.getElementById(arr[1].substring(1));
    } else {
      obj = JsUtils.eval(arr[1]);
    }
    if (obj == null) {
      Global.showError(arr[1], "not a js object");
      return;
    }

    String patt = ArrayUtils.getQuietly(arr, 2);
    JsArrayString fnc = JsUtils.getFunctions(obj, patt);

    if (JsUtils.isEmpty(fnc)) {
      Global.showError(v, "functions not found");
      return;
    }
    if (fnc.length() <= 5) {
      Global.inform(v, fnc.join());
      return;
    }

    JsData<?> table = new JsData<TableColumn>(fnc, "function");
    table.sort(0);

    if (BeeUtils.same(arr[0], "f") && showModal(table.getNumberOfRows())) {
      Global.showModalGrid(v, table);
    } else {
      Global.showGrid(table);
    }
  }

  private static void showGeo() {
    BeeLabel widget = new BeeLabel("Looking for location...");
    getGeo(widget.getElement());
    BeeKeeper.getScreen().updateActivePanel(widget);
  }

  private static void showGwt() {
    List<Property> info = PropertyUtils.createProperties(
        "Host Page Base URL", GWT.getHostPageBaseURL(),
        "Module Base URL", GWT.getModuleBaseURL(),
        "Module Name", GWT.getModuleName(),
        "Permutation Strong Name", GWT.getPermutationStrongName(),
        "Uncaught Exception Handler", GWT.getUncaughtExceptionHandler(),
        "Unique Thread Id", GWT.getUniqueThreadId(),
        "Version", GWT.getVersion(),
        "Is Client", GWT.isClient(),
        "Is Prod Mode", GWT.isProdMode(),
        "Is Script", GWT.isScript());

    PropertiesData table = new PropertiesData(info);
    if (showModal(info.size())) {
      Global.showModalGrid("GWT", table);
    } else {
      Global.showGrid(table);
    }
  }

  private static void showInputBox(String[] arr) {
    String caption = null;
    String prompt = null;
    String defaultValue = null;
    int maxLength = BeeConst.UNDEF;
    double width = BeeConst.DOUBLE_UNDEF;
    Unit widthUnit = null;
    int timeout = BeeConst.UNDEF;
    String confirmHtml = DialogConstants.OK;
    String cancelHtml = DialogConstants.CANCEL;

    boolean required = true;

    final Holder<String> widgetName = new Holder<String>(null);
    final Holder<String> widgetStyle = Holder.of("background-color:green");

    String v;

    for (int i = 1; i < ArrayUtils.length(arr); i++) {
      if (arr[i].length() < 2) {
        continue;
      }
      char k = arr[i].toLowerCase().charAt(0);
      if (arr[i].charAt(1) == '=') {
        v = arr[i].substring(2).trim();
      } else {
        v = arr[i].substring(1).trim();
      }
      if (BeeUtils.isEmpty(v)) {
        continue;
      }

      switch (k) {
        case 'c':
          caption = v;
          break;
        case 'p':
          prompt = v;
          break;
        case 'd':
          defaultValue = v;
          break;
        case 'm':
          maxLength = BeeUtils.toInt(v);
          break;
        case 'w':
          width = BeeUtils.toDouble(v);
          break;
        case 'u':
          widthUnit = StyleUtils.parseUnit(v);
          break;
        case 't':
          timeout = BeeUtils.toInt(v);
          break;
        case 'y':
          confirmHtml = BeeConst.isFalse(v) ? null : v;
          break;
        case 'n':
          cancelHtml = BeeConst.isFalse(v) ? null : v;
          break;
        case 'r':
          required = BeeUtils.toBoolean(v);
          break;
        case 'i':
          widgetName.set(v);
          break;
        case 's':
          widgetStyle.set(v);
          break;
        default:
          logger.info("option not recognized", i, arr[i], k, v);
      }
    }

    Global.inputString(caption, prompt,
        new StringCallback(required) {
          @Override
          public void onCancel() {
            logger.info("cancel");
          }

          @Override
          public void onSuccess(String value) {
            logger.info("success", value);
          }

          @Override
          public void onTimeout(String value) {
            logger.info("timeout", value);
          }

          @Override
          public boolean validate(String value) {
            if (BeeUtils.isEmpty(value)) {
              return super.validate(value);
            } else {
              return value.indexOf('x') < 0;
            }
          }
        }, defaultValue, maxLength, width, widthUnit, timeout, confirmHtml, cancelHtml,
        new WidgetInitializer() {
          @Override
          public Widget initialize(Widget widget, String name) {
            if (BeeUtils.containsSame(name, widgetName.get())) {
              StyleUtils.updateAppearance(widget, null, widgetStyle.get());
              logger.info(name, StyleUtils.getCssText(widget));
            }
            return widget;
          }
        });
  }

  private static void showInputTypes() {
    FlexTable table = new FlexTable();
    table.setCellSpacing(3);

    String[] types = new String[] {
        "search", "tel", "url", "email", "datetime", "date", "month", "week", "time",
        "datetime-local", "number", "range", "color"};
    TextBox widget;

    int row = 0;
    for (String type : types) {
      table.setWidget(row, 0, new BeeLabel(type));

      if (Features.supportsInputType(type)) {
        widget = new TextBox();
        widget.getElement().setAttribute(DomUtils.ATTRIBUTE_TYPE, type);

        if (type.equals("search")) {
          if (Features.supportsAttributePlaceholder()) {
            widget.getElement().setAttribute("placeholder", "Search...");
            widget.getElement().setAttribute("results", "0");
          }
        } else if (type.equals("number")) {
          widget.getElement().setAttribute("min", "0");
          widget.getElement().setAttribute("max", "20");
          widget.getElement().setAttribute("step", "2");
          widget.getElement().setAttribute("value", "4");
        } else if (type.equals("range")) {
          widget.getElement().setAttribute("min", "0");
          widget.getElement().setAttribute("max", "50");
          widget.getElement().setAttribute("step", "5");
          widget.getElement().setAttribute("value", "30");
        }

        table.setWidget(row, 1, widget);
      } else {
        table.setWidget(row, 1, new BeeLabel("not supported"));
      }
      row++;
    }

    BeeKeeper.getScreen().updateActivePanel(table, ScrollBars.BOTH);
  }

  private static void showMatrix(String[][] data, String... columnLabels) {
    Global.showGrid(new StringMatrix<TableColumn>(data, columnLabels));
  }

  private static void showMeter(String[] arr) {
    if (!Features.supportsElementMeter()) {
      Global.showError("meter element not supported");
      return;
    }

    double min = 0;
    double max = 100;
    double value = 30;
    double low = 20;
    double high = 80;
    double optimum = 50;

    String pars = "<>vlho";
    int p = -1, j;
    String s, z;
    char c;

    for (int i = 1; i < arr.length; i++) {
      s = arr[i].toLowerCase();
      c = s.charAt(0);
      j = pars.indexOf(c);

      if (j >= 0) {
        z = s.substring(1);
        p = j;
      } else {
        z = s;
        p++;
      }

      switch (p) {
        case 0:
          min = BeeUtils.toDouble(z);
          break;
        case 1:
          max = BeeUtils.toDouble(z);
          break;
        case 2:
          value = BeeUtils.toDouble(z);
          break;
        case 3:
          low = BeeUtils.toDouble(z);
          break;
        case 4:
          high = BeeUtils.toDouble(z);
          break;
        case 5:
          optimum = BeeUtils.toDouble(z);
          break;
      }
    }

    if (max <= min) {
      max = min + 100;
    }
    value = BeeUtils.clamp(value, min, max);
    low = BeeUtils.clamp(low, min, max);
    high = BeeUtils.clamp(high, low, max);
    optimum = BeeUtils.clamp(optimum, min, max);

    FlexTable table = new FlexTable();
    table.setCellPadding(3);
    table.setCellSpacing(3);
    table.setBorderWidth(1);

    int r = 0;
    table.setHTML(r, 0, "min");
    table.setHTML(r, 1, BeeUtils.toString(min));
    r++;
    table.setHTML(r, 0, "max");
    table.setHTML(r, 1, BeeUtils.toString(max));
    r++;
    table.setHTML(r, 0, "low");
    table.setHTML(r, 1, BeeUtils.toString(low));
    r++;
    table.setHTML(r, 0, "high");
    table.setHTML(r, 1, BeeUtils.toString(high));
    r++;
    table.setHTML(r, 0, "optimum");
    table.setHTML(r, 1, BeeUtils.toString(optimum));

    r++;
    table.setHTML(r, 0, BeeUtils.toString(value));
    table.setWidget(r, 1, new Meter(min, max, value, low, high, optimum));

    for (double i = min; i <= max; i += (max - min) / 10) {
      r++;
      table.setHTML(r, 0, BeeUtils.toString(i));
      table.setWidget(r, 1, new Meter(min, max, i, low, high, optimum));
    }
    BeeKeeper.getScreen().updateActivePanel(table, ScrollBars.BOTH);
  }

  private static boolean showModal(int rowCount) {
    if (rowCount <= 1) {
      return true;
    }
    if (rowCount > 50) {
      return false;
    }

    int h = rowCount * 20 + 70;
    int vph = DomUtils.getClientHeight();
    int aph = BeeKeeper.getScreen().getActivePanelHeight();

    if (vph > 0 && h > vph / 2) {
      return false;
    }
    if (aph > 0 && aph < 80) {
      return true;
    }
    return rowCount <= 12;
  }

  private static void showNotes(String args) {
    if (BeeUtils.isEmpty(args)) {
      Global.sayHuh(args);
      return;
    }

    for (String msg : Splitter.on(';').omitEmptyStrings().trimResults().split(args)) {
      List<String> lst = Lists.newArrayList();
      for (String line : Splitter.on(',').trimResults().split(msg)) {
        lst.add(line);
      }
      int c = lst.size();
      if (c <= 0) {
        continue;
      }

      String lvl = lst.get(0);
      String[] arr = new String[0];
      if (c > 1 && BeeUtils.inListSame(lvl, "w", "e", "s")) {
        arr = lst.subList(1, c).toArray(arr);
        if (BeeUtils.same(lvl, "w")) {
          BeeKeeper.getScreen().notifyWarning(arr);
        } else {
          BeeKeeper.getScreen().notifySevere(arr);
        }

      } else {
        BeeKeeper.getScreen().notifyInfo(lst.toArray(arr));
      }
    }
  }
  
  private static void showProgress(String[] arr) {
    if (!Features.supportsElementProgress()) {
      Global.showError("progress element not supported");
      return;
    }

    final int steps;
    int millis = 250;

    double max = 100;
    double value = 0;

    String s = ArrayUtils.getQuietly(arr, 1);
    if (BeeUtils.isDigit(s)) {
      steps = BeeUtils.toInt(s);
    } else {
      steps = 100;
    }
    s = ArrayUtils.getQuietly(arr, 2);
    if (BeeUtils.isDigit(s)) {
      millis = BeeUtils.toInt(s);
    }
    s = ArrayUtils.getQuietly(arr, 3);
    if (BeeUtils.isDigit(s)) {
      max = BeeUtils.toDouble(s);
    }

    Absolute panel = new Absolute();
    panel.add(new BeeLabel("indeterminate"), 10, 8);
    panel.add(new Progress(), 120, 10);

    panel.add(new BeeLabel(NameUtils.addName("steps", steps)), 10, 36);
    panel.add(new BeeLabel(NameUtils.addName("millis", millis)), 10, 53);
    panel.add(new BeeLabel(NameUtils.addName("max", BeeUtils.toString(max))), 10, 70);

    final Progress prg = new Progress(max, value);
    panel.add(prg, 120, 40);
    final BeeLabel lbl = new BeeLabel();
    panel.add(lbl, 160, 70);

    Timer timer = new Timer() {
      @Override
      public void run() {
        double v = prg.getValue();
        double x = prg.getMax();

        v += x / steps;
        if (v > x) {
          v = 0;
        }

        prg.setValue(v);
        lbl.setText(BeeUtils.join(BeeUtils.space(3), BeeUtils.round(v, 1),
            BeeUtils.round(prg.getPosition(), 3)));
      }
    };
    timer.scheduleRepeating(millis);

    BeeKeeper.getScreen().updateActivePanel(panel, ScrollBars.BOTH);
  }

  private static void showPropData(List<Property> data, String... columnLabels) {
    Global.showGrid(new PropertiesData(data, columnLabels));
  }

  private static void showProperties(String v, String[] arr) {
    if (ArrayUtils.length(arr) < 2) {
      Global.sayHuh(v);
      return;
    }

    JavaScriptObject obj;
    if (arr[1].startsWith("#")) {
      obj = DOM.getElementById(arr[1].substring(1));
    } else {
      obj = JsUtils.eval(arr[1]);
    }
    if (obj == null) {
      Global.showError(arr[1], "not a js object");
      return;
    }

    String patt = ArrayUtils.getQuietly(arr, 2);
    JsArrayString prp = JsUtils.getProperties(obj, patt);

    if (JsUtils.isEmpty(prp)) {
      Global.showError(v, "properties not found");
      return;
    }

    JsData<?> table = new JsData<TableColumn>(prp, "property", "type", "value");
    table.sort(0);

    if (BeeUtils.same(arr[0], "p") && showModal(table.getNumberOfRows())) {
      Global.showModalGrid(v, table);
    } else {
      Global.showGrid(table);
    }
  }
  
  private static void showRpc() {
    if (BeeKeeper.getRpc().getRpcList().isEmpty()) {
      Global.inform("RpcList empty");
    } else {
      Global.showGrid(new StringMatrix<TableColumn>(
          BeeKeeper.getRpc().getRpcList().getDefaultInfo(), RpcList.DEFAULT_INFO_COLUMNS));
    }
  }

  private static void showSize(String[] arr) {
    int len = ArrayUtils.length(arr);
    String html = ArrayUtils.getQuietly(arr, 1);

    if (BeeUtils.isEmpty(html)) {
      Global.sayHuh();
      return;
    }

    int pos = 2;

    if (html.equals("[") && len > 3) {
      pos = len;
      StringBuilder sb = new StringBuilder(arr[2]);
      for (int i = 3; i < len; i++) {
        if (arr[i].equals("]")) {
          pos = i + 1;
          break;
        }
        sb.append(BeeConst.CHAR_SPACE).append(arr[i]);
      }
      html = sb.toString();
    } else {
      Element elem = Document.get().getElementById(html);
      if (elem != null) {
        html = elem.getInnerHTML();
      }
    }

    Font font = (len > pos) ? Font.parse(ArrayUtils.slice(arr, pos)) : null;
    Dimensions lineDim = Rulers.getLineDimensions(html, font);
    Dimensions areaDim = Rulers.getAreaDimensions(html, font);

    int lineW = -1;
    int lineH = -1;
    int areaW = -1;
    int areaH = -1;

    if (lineDim != null) {
      lineW = BeeUtils.toInt(lineDim.getWidthValue());
      lineH = BeeUtils.toInt(lineDim.getHeightValue());
    }
    if (areaDim != null) {
      areaW = BeeUtils.toInt(areaDim.getWidthValue());
      areaH = BeeUtils.toInt(areaDim.getHeightValue());
    }

    List<Property> info = PropertyUtils.createProperties("Line Width", lineW, "Line Height", lineH,
        "Area Width", areaW, "Area Height", areaH);
    if (font != null) {
      info.addAll(font.getInfo());
    }

    InlineHtml lineHtml = new InlineHtml();
    if (font != null) {
      font.applyTo(lineHtml);
    }
    lineHtml.setHTML(html);

    Html areaHtml = new Html();
    if (font != null) {
      font.applyTo(areaHtml);
    }
    areaHtml.setHTML(html);

    FlexTable table = new FlexTable();
    table.setCellPadding(3);
    table.setBorderWidth(1);

    for (int i = 0; i < info.size(); i++) {
      table.setHTML(i, 0, info.get(i).getName());
      table.setHTML(i, 1, info.get(i).getValue());
    }

    Absolute panel = new Absolute();
    panel.setPixelSize(Math.max(Math.max(lineH, areaH) + 40, 256),
        lineH + areaH + 20 + info.size() * 32);

    panel.add(lineHtml, 10, 5);
    panel.add(areaHtml, 10, lineH + 10);
    panel.add(table, 10, lineH + areaH + 20);

    Global.showModalWidget(panel);
  }

  private static void showSlider(String[] arr) {
    double value = 0;
    double min = 0;
    double max = 100;
    double step = 1;
    int labels = 5;
    int ticks = 10;

    String pars = "v<>slt";
    int p = -1, j;
    String s, z;
    char c;

    for (int i = 1; i < arr.length; i++) {
      s = arr[i].toLowerCase();
      c = s.charAt(0);
      j = pars.indexOf(c);

      if (j >= 0) {
        z = s.substring(1);
        p = j;
      } else {
        z = s;
        p++;
      }

      switch (p) {
        case 0:
          value = BeeUtils.toDouble(z);
          break;
        case 1:
          min = BeeUtils.toDouble(z);
          break;
        case 2:
          max = BeeUtils.toDouble(z);
          break;
        case 3:
          step = BeeUtils.toDouble(z);
          break;
        case 4:
          labels = BeeUtils.toInt(z);
          break;
        case 5:
          ticks = BeeUtils.toInt(z);
          break;
      }
    }

    if (max <= min) {
      max = min + 100;
    }
    if (step <= 0 || step > (max - min) / 2) {
      step = (max - min) / 10;
    }
    if (value < min) {
      value = min;
    }
    if (value > max) {
      value = max;
    }

    BeeKeeper.getScreen().updateActivePanel(new SliderBar(value, min, max, step, labels, ticks));
  }

  private static void showStack() {
    Throwable err = new Throwable();
    err.fillInStackTrace();
    logger.debug(err);
    logger.addSeparator();
  }

  private static void showSupport() {
    showPropData(Features.getInfo());
  }

  private static void showSvg(String[] arr) {
    if (!Features.supportsSvg()) {
      BeeKeeper.getScreen().notifySevere("svg not supported");
      return;
    }

    int width = BeeKeeper.getScreen().getActivePanelWidth();
    int height = BeeKeeper.getScreen().getActivePanelHeight();

    int type = BeeUtils.randomInt(0, 3);

    int rMin = Math.min(width, height) / 50;
    int rMax = rMin * 10;

    int cntMin = 10;
    int cntMax = 100;

    int colorStep = 16;

    double minOpacity = 0.5;
    double maxOpacity = 1;

    int len = ArrayUtils.length(arr);
    if (len > 1) {
      for (int i = 1; i < len; i++) {
        String s = BeeUtils.trim(arr[i]);
        if (BeeUtils.isEmpty(s) || s.length() < 2) {
          continue;
        }

        char pName = s.charAt(0);
        String pMin = BeeUtils.getPrefix(s.substring(1), BeeConst.CHAR_MINUS);
        String pMax = BeeUtils.getSuffix(s.substring(1), BeeConst.CHAR_MINUS);
        if (BeeUtils.isEmpty(pMin)) {
          pMin = s.substring(1);
        }

        if (BeeUtils.isDouble(pMin) || BeeUtils.isDouble(pMax)) {
          switch (pName) {
            case 't':
              if (BeeUtils.isDigit(pMin)) {
                type = BeeUtils.toInt(pMin);
              } else if (BeeUtils.isDigit(pMax)) {
                type = BeeUtils.toInt(pMax);
              }
              break;

            case 'r':
              if (BeeUtils.isDigit(pMin)) {
                rMin = BeeUtils.toInt(pMin);
              }
              if (BeeUtils.isDigit(pMax)) {
                rMax = BeeUtils.toInt(pMax);
              }
              if (rMax == rMin) {
                rMax = rMin + 1;
              } else if (rMax < rMin) {
                int z = rMax;
                rMax = rMin;
                rMin = z;
              }
              break;

            case 'c':
              if (BeeUtils.isDigit(pMin)) {
                cntMin = BeeUtils.toInt(pMin);
              }
              if (BeeUtils.isDigit(pMax)) {
                cntMax = BeeUtils.toInt(pMax);
              }
              if (cntMax == cntMin) {
                cntMax = cntMin + 1;
              } else if (cntMax < cntMin) {
                int z = cntMax;
                cntMax = cntMin;
                cntMin = z;
              }
              break;

            case 's':
              if (BeeUtils.isDigit(pMin)) {
                colorStep = BeeUtils.toInt(pMin);
              } else if (BeeUtils.isDigit(pMax)) {
                colorStep = BeeUtils.toInt(pMax);
              }
              break;

            case 'o':
              if (BeeUtils.isDouble(pMin)) {
                minOpacity = BeeUtils.toDouble(pMin);
              }
              if (BeeUtils.isDouble(pMax)) {
                maxOpacity = BeeUtils.toDouble(pMax);
              }
              if (maxOpacity < minOpacity) {
                double z = maxOpacity;
                maxOpacity = minOpacity;
                minOpacity = z;
              }
              break;
          }
        }
      }
    }

    Svg widget = new Svg();
    Element parent = widget.getElement();
    Element child;

    for (int i = 0; i < BeeUtils.randomInt(cntMin, cntMax); i++) {
      String x = BeeUtils.toString(BeeUtils.randomInt(0, width));
      String y = BeeUtils.toString(BeeUtils.randomInt(0, height));
      String rx = BeeUtils.toString(BeeUtils.randomInt(rMin, rMax));
      String ry = BeeUtils.toString(BeeUtils.randomInt(rMin, rMax));

      switch (type) {
        case 0:
          child = DomUtils.createSvg("rect");
          child.setAttribute("x", x);
          child.setAttribute("y", y);
          child.setAttribute("width", rx);
          child.setAttribute("height", ry);
          break;
        case 1:
          child = DomUtils.createSvg("circle");
          child.setAttribute("cx", x);
          child.setAttribute("cy", y);
          child.setAttribute("r", rx);
          break;
        default:
          child = DomUtils.createSvg("ellipse");
          child.setAttribute("cx", x);
          child.setAttribute("cy", y);
          child.setAttribute("rx", rx);
          child.setAttribute("ry", ry);
      }

      int r = Math.min(BeeUtils.randomInt(0, colorStep + 1) * 256 / colorStep, 255);
      int g = Math.min(BeeUtils.randomInt(0, colorStep + 1) * 256 / colorStep, 255);
      int b = Math.min(BeeUtils.randomInt(0, colorStep + 1) * 256 / colorStep, 255);

      child.setAttribute("fill", "rgb(" + r + "," + g + "," + b + ")");
      child.setAttribute("opacity", BeeUtils.toString((minOpacity == maxOpacity)
          ? minOpacity : BeeUtils.randomDouble(minOpacity, maxOpacity)));

      parent.appendChild(child);
    }

    BeeKeeper.getScreen().updateActivePanel(widget);
  }

  private static void showTableInfo(String args) {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_TABLE_INFO);
    if (!BeeUtils.isEmpty(args)) {
      params.addPositionalHeader(args.trim());
    }
    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        showExtData(PropertyUtils.restoreExtended((String) response.getResponse()));
      }
    });
  }

  private static void showUnits(String[] arr) {
    int len = ArrayUtils.length(arr);

    Double value = null;
    Unit unit = null;
    Font font = null;
    Integer containerSize = null;

    if (len > 1) {
      for (int i = 1; i < len; i++) {
        if (arr[i] == "f" && i < len - 1) {
          font = Font.parse(ArrayUtils.slice(arr, i + 1));
          break;
        }
        if (arr[i] == "c" && i < len - 1) {
          containerSize = BeeUtils.toInt(arr[i + 1]);
          i++;
          continue;
        }

        if (BeeUtils.isDouble(arr[i])) {
          value = BeeUtils.toDouble(arr[i]);
        } else {
          unit = StyleUtils.parseUnit(arr[i]);
        }
      }
    }

    List<Unit> units = Lists.newArrayList();
    if (value == null) {
      value = 1.0;
    }

    if (unit != null) {
      units.add(unit);
    } else {
      for (Unit u : Unit.values()) {
        units.add(u);
      }
    }

    List<Property> info = PropertyUtils.createProperties("Value", value);
    if (font != null) {
      info.addAll(font.getInfo());
    }
    if (containerSize != null) {
      info.add(new Property("Container Size", containerSize.toString()));
    }

    for (Unit u : units) {
      int px = Rulers.getIntPixels(value, u, font, BeeUtils.unbox(containerSize));
      info.add(new Property(u.getType(), BeeUtils.toString(px)));
    }
    
    PropertiesData table = new PropertiesData(info);
    if (showModal(info.size())) {
      Global.showModalGrid("Pixels", table);
    } else {
      Global.showGrid(table);
    }
  }

  private static void showVars(String[] arr) {
    int len = ArrayUtils.length(arr);
    if (len > 1) {
      String[] vars = new String[len - 1];
      for (int i = 0; i < len - 1; i++) {
        vars[i] = arr[i + 1];
      }
      Global.showVars(vars);
    } else {
      Global.showVars();
    }
  }

  private static void showViewInfo(String args) {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_VIEW_INFO);
    if (!BeeUtils.isEmpty(args)) {
      params.addPositionalHeader(args.trim());
    }
    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        showExtData(PropertyUtils.restoreExtended((String) response.getResponse()));
      }
    });
  }

  private static void showWidgetInfo(String[] arr) {
    String id = ArrayUtils.getQuietly(arr, 1);
    if (BeeUtils.isEmpty(id)) {
      Global.showError("widget id not specified");
      return;
    }

    Widget widget = DomUtils.getWidget(id);
    if (widget == null) {
      Global.showError(id, "widget not found");
      return;
    }

    String z = ArrayUtils.getQuietly(arr, 2);
    int depth = BeeUtils.isDigit(z) ? BeeUtils.toInt(z) : 0;

    List<ExtendedProperty> info = DomUtils.getInfo(widget, id, depth);
    showExtData(info);
  }

  private static void showXmlInfo(String[] arr) {
    if (arr.length >= 2) {
      String[] opt = ArrayUtils.copyOf(arr);
      final boolean detailed = !BeeUtils.same(opt[0], "xml");
      if (detailed) {
        opt[0] = "xml";
      }

      ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_RESOURCE);
      for (String v : opt) {
        params.addPositionalHeader(v);
      }

      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          showExtData(XmlUtils.getInfo((String) response.getResponse(), detailed));
        }
      });

    } else {
      final List<String> varNames = Lists.newArrayList(Service.VAR_XML_SOURCE,
          Service.VAR_XML_TRANSFORM, Service.VAR_XML_TARGET, Service.VAR_XML_RETURN);

      Global.inputVars("Xml Info", varNames, new ConfirmationCallback() {
        @Override
        public boolean onConfirm(Popup popup) {
          String src = Global.getVarValue(Service.VAR_XML_SOURCE);
          if (BeeUtils.isEmpty(src)) {
            Global.showError("Source not specified");
            return false;
          } else {
            BeeKeeper.getRpc().makePostRequest(Service.GET_XML_INFO,
                XmlUtils.fromVars(Service.XML_TAG_DATA, varNames));
            return true;
          }
        }
      });
    }
  }

  private static void storage(String[] arr) {
    int parCnt = ArrayUtils.length(arr) - 1;
    int len = BeeKeeper.getStorage().length();

    if (parCnt <= 1 && len <= 0) {
      Global.inform("Storage empty");
      return;
    }

    if (parCnt <= 0) {
      showPropData(BeeKeeper.getStorage().getAll());
      return;
    }

    String key = arr[1];

    if (parCnt == 1) {
      if (key.equals(BeeConst.STRING_MINUS)) {
        BeeKeeper.getStorage().clear();
        Global.inform(BeeUtils.joinWords(len, "items cleared"));
      } else {
        String z = BeeKeeper.getStorage().getItem(key);
        if (z == null) {
          Global.showError(Global.MESSAGES.keyNotFound(key));
        } else {
          Global.inform(key, z);
        }
      }
      return;
    }

    String value = ArrayUtils.join(BeeConst.STRING_SPACE, arr, 2);

    if (key.equals(BeeConst.STRING_MINUS)) {
      BeeKeeper.getStorage().removeItem(value);
      Global.inform(value, "removed");
    } else {
      BeeKeeper.getStorage().setItem(key, value);
      Global.inform("Storage", NameUtils.addName(key, value));
    }
  }

  private static void style(String v, String[] arr) {
    if (ArrayUtils.length(arr) < 2) {
      JsStyleSheetList sheets = JsBrowser.getDocument().getStyleSheets();
      int sheetCnt = (sheets == null) ? 0 : sheets.getLength();

      List<ExtendedProperty> lst = Lists.newArrayList();
      PropertyUtils.addExtended(lst, "sheets", "count", sheetCnt);

      for (int i = 0; i < sheetCnt; i++) {
        JsCSSStyleSheet sheet = (JsCSSStyleSheet) sheets.item(i);
        if (sheet == null) {
          continue;
        }

        JsCSSRuleList rules = sheet.getRules();

        int len = (rules == null) ? 0 : rules.length();
        PropertyUtils.addExtended(lst, "sheet " + BeeUtils.progress(i + 1, sheetCnt), "rules", len);

        for (int j = 0; j < len; j++) {
          PropertyUtils.addExtended(lst, "rule", BeeUtils.progress(j + 1, len),
              rules.item(j).getCssText());
        }
      }

      showExtData(lst);
      return;
    }

    if (!v.contains("{")) {
      Element elem = Document.get().getElementById(arr[1]);
      if (elem == null) {
        Global.inform("element id", arr[1], "not found");
        return;
      }

      List<Property> info = Lists.newArrayList();
      List<Property> lst;

      if (elem.getStyle() != null) {
        lst = StyleUtils.getStyleInfo(elem.getStyle());
        if (!BeeUtils.isEmpty(lst)) {
          info.add(new Property("element style", BeeUtils.bracket(lst.size())));
          info.addAll(lst);
        }
      }

      lst = new ComputedStyles(elem).getInfo();
      if (!BeeUtils.isEmpty(lst)) {
        info.add(new Property("computed style", BeeUtils.bracket(lst.size())));
        info.addAll(lst);
      }

      if (BeeUtils.isEmpty(info)) {
        Global.inform("element id", arr[1], "has no style");
      } else {
        showPropData(info);
      }
      return;
    }

    boolean start = false;
    boolean end = false;
    boolean immediate = false;

    StringBuilder sb = new StringBuilder();
    boolean tg = false;

    for (int i = 1; i < arr.length; i++) {
      if (!tg) {
        if (arr[i].contains("{")) {
          tg = true;
        } else {
          if (BeeUtils.same(arr[i], "start")) {
            start = true;
            continue;
          }
          if (BeeUtils.same(arr[i], "end")) {
            end = true;
            continue;
          }
          if (arr[i].equals("+")) {
            immediate = true;
            continue;
          }
        }
      }
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(arr[i]);
    }

    String st = sb.toString();
    if (!st.contains("{") || !st.contains("}")) {
      Global.showError("Nah pop no style, a strictly roots", v, st);
      return;
    }

    logger.info(st);
    logger.addSeparator();

    if (start) {
      StyleInjector.injectAtStart(st, immediate);
    } else if (end) {
      StyleInjector.injectAtEnd(st, immediate);
    } else {
      StyleInjector.inject(st, immediate);
    }
  }

  private static void translate(final String[] arr, boolean detect) {
    int len = ArrayUtils.length(arr);
    if (len < 2) {
      Global.sayHuh(arr);
      return;
    }

    if (detect) {
      final String detText = ArrayUtils.join(BeeConst.STRING_SPACE, arr, 1, len);
      Translation.detect(detText, new DetectionCallback() {
        @Override
        protected void onCallback(DetectionResult result) {
          Global.showModalGrid("Language Detection",
              new PropertiesData(PropertyUtils.createProperties("Text", detText,
                  "Language Code", result.getLanguage(),
                  "Language", Language.getByCode(result.getLanguage()),
                  "Confidence", result.getConfidence(),
                  "Reliable", result.isReliable())));
        }
      });
      return;
    }

    final String text;
    final String codeFrom;
    final String codeTo;

    Language dst = Language.getByCode(arr[len - 1]);

    if (dst == null) {
      text = ArrayUtils.join(BeeConst.STRING_SPACE, arr, 1, len);
      codeFrom = BeeConst.STRING_EMPTY;
      codeTo = LocaleUtils.getLanguageCode(LocaleInfo.getCurrentLocale());
    } else {
      codeTo = dst.getLangCode();
      if (len <= 2) {
        text = BeeConst.STRING_EMPTY;
        codeFrom = BeeConst.STRING_EMPTY;
      } else if (len <= 3) {
        text = arr[len - 2];
        codeFrom = BeeConst.STRING_EMPTY;
      } else {
        Language src = Language.getByCode(arr[len - 2]);
        if (src == null) {
          text = ArrayUtils.join(BeeConst.STRING_SPACE, arr, 1, len - 1);
          codeFrom = BeeConst.STRING_EMPTY;
        } else {
          text = ArrayUtils.join(BeeConst.STRING_SPACE, arr, 1, len - 2);
          codeFrom = src.getLangCode();
        }
      }
    }

    if (BeeUtils.isEmpty(text) || BeeUtils.same(text, BeeConst.STRING_ALL)) {
      logger.info("Translate", codeFrom, codeTo);
      List<Element> elements = Lists.newArrayList();

      NodeList<Element> nodes = Document.get().getElementsByTagName(DomUtils.TAG_BUTTON);
      for (int i = 0; i < nodes.getLength(); i++) {
        elements.add(nodes.getItem(i));
      }
      nodes = Document.get().getElementsByTagName(DomUtils.TAG_TH);
      for (int i = 0; i < nodes.getLength(); i++) {
        elements.add(nodes.getItem(i));
      }
      nodes = Document.get().getElementsByTagName(DomUtils.TAG_LABEL);
      for (int i = 0; i < nodes.getLength(); i++) {
        elements.add(nodes.getItem(i));
      }
      nodes = Document.get().getElementsByTagName(DomUtils.TAG_OPTION);
      for (int i = 0; i < nodes.getLength(); i++) {
        elements.add(nodes.getItem(i));
      }

      for (final Element elem : elements) {
        String elTxt = elem.getInnerText();
        if (!BeeUtils.hasLength(elTxt, 3)) {
          continue;
        }

        TranslationCallback elBack = new TranslationCallback() {
          @Override
          protected void onCallback(String translation) {
            elem.setInnerText(translation);
          }
        };

        if (BeeUtils.isEmpty(codeFrom)) {
          Translation.detectAndTranslate(elTxt, codeTo, elBack);
        } else {
          Translation.translate(elTxt, codeFrom, codeTo, elBack);
        }
      }
      return;
    }

    final String info = ArrayUtils.join(BeeConst.STRING_SPACE, arr, 1, len);

    TranslationCallback callback = new TranslationCallback() {
      @Override
      protected void onCallback(String translation) {
        Global.inform(info, translation);
      }
    };

    if (BeeUtils.isEmpty(codeFrom)) {
      Translation.detectAndTranslate(text, codeTo, callback);
    } else {
      Translation.translate(text, codeFrom, codeTo, callback);
    }
  }

  private static void unicode(String[] arr) {
    StringBuilder sb = new StringBuilder();
    int len = ArrayUtils.length(arr);

    if (len < 2 || len == 2 && BeeUtils.isDigit(ArrayUtils.getQuietly(arr, 1))) {
      int n = (len < 2) ? 10 : BeeUtils.toInt(arr[1]);
      for (int i = 0; i < n; i++) {
        sb.append((char) BeeUtils.randomInt(Character.MIN_VALUE, Character.MIN_SURROGATE));
      }

    } else {
      for (int i = 1; i < len; i++) {
        String s = arr[i];

        if (s.length() > 1 && BeeUtils.inListSame(s.substring(0, 1), "u", "x")
            && BeeUtils.isHexString(s.substring(1))) {
          sb.append(BeeUtils.fromHex(s.substring(1)));
        } else if (s.length() > 2 && BeeUtils.startsSame(s, "0x")
            && BeeUtils.isHexString(s.substring(2))) {
          sb.append(BeeUtils.fromHex(s.substring(2)));

        } else if (BeeUtils.isDigit(s)) {
          int n = BeeUtils.toInt(s);
          if (n > 0 && n < Character.MAX_VALUE && sb.length() > 0) {
            for (int j = 0; j < n; j++) {
              sb.append((char) (sb.charAt(sb.length() - 1) + 1));
            }
          } else {
            sb.append((char) n);
          }

        } else {
          sb.append(s);
        }
      }
    }

    String s = sb.toString();
    byte[] bytes = Codec.toBytes(s);

    int id = BeeKeeper.getRpc().invoke("stringInfo", ContentType.TEXT, s);
    
    Map<String, String> data = Maps.newHashMap();
    data.put("length", BeeUtils.toString(s.length()));
    data.put("data", s);
    
    data.put("adler32", Codec.adler32(bytes));
    data.put("crc16", Codec.crc16(bytes));
    data.put("crc32", Codec.crc32(bytes));
    data.put("crc32d", Codec.crc32Direct(bytes));

    BeeKeeper.getRpc().setUserData(id, data);
  }

  private static void whereAmI() {
    logger.info(BeeConst.whereAmI());
    BeeKeeper.getRpc().makeGetRequest(Service.WHERE_AM_I);
  }

  private CliWorker() {
  }
}

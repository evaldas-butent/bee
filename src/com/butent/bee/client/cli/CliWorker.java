package com.butent.bee.client.cli;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
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
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
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
import com.butent.bee.client.data.JsData;
import com.butent.bee.client.dom.ComputedStyles;
import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Features;
import com.butent.bee.client.dom.Font;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.GridFactory;
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
import com.butent.bee.client.layout.TilePanel;
import com.butent.bee.client.tree.BeeTree;
import com.butent.bee.client.ui.FormFactory;
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
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;
import com.butent.bee.shared.utils.TimeUtils;
import com.butent.bee.shared.utils.Wildcards;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Contains the engine for processing client side command line interface commands.
 */

public class CliWorker {

  private static boolean cornified = false;

  public static void clear(String args) {
    if (BeeUtils.isEmpty(args) || BeeUtils.startsSame(args, "log")) {
      BeeKeeper.getLog().clear();
    } else if (BeeUtils.startsSame(args, "grids")) {
      GridFactory.clearDescriptionCache();
      BeeKeeper.getLog().info("grid cache cleared");
    } else if (BeeUtils.startsSame(args, "cache")) {
      Global.getCache().removeAll();
      BeeKeeper.getLog().info("cache cleared");
    }
  }

  public static void cornify(String[] arr) {
    if (!cornified) {
      DomUtils.injectExternalScript("http://www.cornify.com/js/cornify.js");
      cornified = true;
    }

    final int cnt = BeeUtils.limit(BeeUtils.toInt(ArrayUtils.getQuietly(arr, 1)), 1, 50);

    int delay = BeeUtils.toInt(ArrayUtils.getQuietly(arr, 2));
    if (delay <= 0) {
      delay = 2000;
    }

    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
      private int counter = 0;

      public boolean execute() {
        cornifyAdd();
        return (++counter < cnt);
      }
    }, delay);
  }

  public static void digest(String v) {
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
      BeeKeeper.getLog().info("Source length", src.length());
    } else {
      BeeKeeper.getLog().info(Codec.escapeUnicode(src));
    }

    BeeKeeper.getLog().info("js", JsUtils.md5(src));
    BeeKeeper.getLog().info("js fast", JsUtils.md5fast(src));
    BeeKeeper.getLog().info(BeeConst.CLIENT, Codec.md5(src));

    BeeKeeper.getRpc().makePostRequest(Service.GET_DIGEST, ContentType.BINARY, src);
  }

  public static void doAjaxKeys(String[] arr) {
    if (BeeUtils.length(arr) == 3) {
      String loc = arr[1];
      String key = arr[2];

      if (Global.nativeConfirm("add api key", loc, key)) {
        AjaxKeyRepository.putKey(loc, key);
      }
    }

    Map<String, String> keyMap = AjaxKeyRepository.getKeys();
    if (BeeUtils.isEmpty(keyMap)) {
      BeeKeeper.getLog().warning("api key repository is empty");
      return;
    }

    List<Property> lst = new ArrayList<Property>();
    for (Map.Entry<String, String> entry : keyMap.entrySet()) {
      lst.add(new Property(entry.getKey(), entry.getValue()));
    }

    BeeKeeper.getScreen().showGrid(lst, "Location", "Api Key");
  }

  public static void doLike(String[] arr) {
    int len = ArrayUtils.length(arr);
    if (len < 3) {
      Global.sayHuh(ArrayUtils.join(arr, 1));
      return;
    }

    String mode = ArrayUtils.getQuietly(arr, 0);
    String input = ArrayUtils.join(arr, 1, 1, len - 1);
    String expr = ArrayUtils.getQuietly(arr, len - 1);

    boolean sens = mode.indexOf('+') > 0;
    boolean insens = mode.indexOf('-') > 0;
    String defCase = null;
    boolean match;

    if (BeeUtils.context("s", mode)) {
      if (sens || insens) {
        match = Wildcards.isSqlLike(input, expr, sens);
      } else {
        defCase = BeeUtils.concat(1, "sql", BooleanValue.pack(Wildcards.isSqlCaseSensitive()));
        match = Wildcards.isSqlLike(input, expr);
      }
    } else if (BeeUtils.context("f", mode)) {
      if (sens || insens) {
        match = Wildcards.isFsLike(input, expr, sens);
      } else {
        defCase = BeeUtils.concat(1, "fs", BooleanValue.pack(Wildcards.isFsCaseSensitive()));
        match = Wildcards.isFsLike(input, expr);
      }
    } else {
      if (sens || insens) {
        match = Wildcards.isLike(input, expr, sens);
      } else {
        defCase = BeeUtils.concat(1, "def",
            BooleanValue.pack(Wildcards.isDefaultCaseSensitive()));
        match = Wildcards.isLike(input, expr);
      }
    }
    Global.showDialog(mode, BeeUtils.addName("input", input), BeeUtils.addName("pattern", expr),
        BeeUtils.addName("case", BeeUtils.iif(sens, "sensitive", insens, "insensitive", defCase)),
        BeeUtils.addName("match", match));
  }

  public static void doLocale(String[] arr) {
    String mode = ArrayUtils.getQuietly(arr, 1);
    if (BeeUtils.isEmpty(mode)) {
      BeeKeeper.getScreen().showGrid(LocaleUtils.getInfo());
      return;
    }

    String lang = ArrayUtils.getQuietly(arr, 2);
    BeeKeeper.getRpc().invoke("localeInfo", ContentType.TEXT, BeeUtils.concat(1, mode, lang));
  }

  public static void doLog(String[] arr) {
    if (BeeUtils.length(arr) > 1) {
      String z = arr[1];

      if (BeeUtils.inList(z, BeeConst.STRING_ZERO, BeeConst.STRING_MINUS)) {
        BeeKeeper.getLog().hide();
      } else if (BeeUtils.isDigit(z)) {
        BeeKeeper.getLog().resize(BeeUtils.toInt(z));
      } else if (BeeUtils.startsSame(z, "clear")) {
        BeeKeeper.getLog().clear();
      } else {
        BeeKeeper.getLog().show();
        BeeKeeper.getLog().info((Object[]) arr);
        BeeKeeper.getLog().addSeparator();
      }

      return;
    }

    Level[] levels = new Level[] {Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG, Level.INFO,
        Level.WARNING, Level.SEVERE};
    for (Level lvl : levels) {
      BeeKeeper.getLog().log(lvl, lvl.getName().toLowerCase());
    }
    BeeKeeper.getLog().addSeparator();
  }

  public static void doMenu(String[] arr) {
    if (BeeUtils.length(arr) > 1) {
      ParameterList params = BeeKeeper.getRpc().createParameters(Service.LOAD_MENU);
      params.addPositionalHeader(arr[1]);
      BeeKeeper.getRpc().makeGetRequest(params);
    } else {
      BeeKeeper.getMenu().showMenu();
    }
  }

  public static void doScreen(String[] arr) {
    Split screen = BeeKeeper.getScreen().getScreenPanel();
    Assert.notNull(screen);

    String p1 = ArrayUtils.getQuietly(arr, 0);
    String p2 = ArrayUtils.getQuietly(arr, 1);

    if (BeeUtils.same(p1, "screen")) {
      BeeKeeper.getScreen().showGrid(screen.getInfo());
      return;
    }

    Direction dir = DomUtils.getDirection(p1);
    if (dir == null) {
      Global.sayHuh(p1, p2);
      return;
    }

    if (BeeUtils.isEmpty(p2)) {
      BeeKeeper.getScreen().showGrid(screen.getDirectionInfo(dir));
      return;
    }

    double size = BeeUtils.toDouble(p2);
    if (Double.isNaN(size)) {
      Global.showError(p1, p2, "NaN");
      return;
    }

    screen.setDirectionSize(dir, size);
  }

  public static void eval(String v, String[] arr) {
    String xpr = v.substring(arr[0].length()).trim();

    if (BeeUtils.isEmpty(xpr)) {
      Global.sayHuh(v);
    } else {
      Global.showDialog(xpr, JsUtils.evalToString(xpr));
    }
  }

  public static void execute(String line) {
    if (BeeUtils.isEmpty(line)) {
      return;
    }

    String v = line.trim();
    String[] arr = BeeUtils.split(v, BeeConst.STRING_SPACE);
    Assert.notEmpty(arr);

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
      BeeKeeper.getScreen().showGrid(Global.getCache().getInfo());
    } else if (z.equals("canvas")) {
      new CanvasDemo().start();
    } else if (BeeUtils.inList(z, "center", "east", "north", "south", "screen", "west")) {
      doScreen(arr);
    } else if (z.equals("charset")) {
      getCharsets();
    } else if (z.equals("clear")) {
      clear(args);
    } else if (z.startsWith("client")) {
      showClientLocation();
    } else if (z.startsWith("conf")) {
      BeeKeeper.getRpc().invoke("configInfo");
    } else if (z.startsWith("conn") || z.equals("http")) {
      BeeKeeper.getRpc().invoke("connectionInfo");
    } else if (z.equals("cornify")) {
      cornify(arr);
    } else if (z.equals("df")) {
      showDateFormat();
    } else if (z.startsWith("dim")) {
      showDimensions();
    } else if (z.equals("dnd")) {
      showDnd();
    } else if (z.equals("dt")) {
      showDate(arr);
    } else if (BeeUtils.inList(z, "dir", "file", "get", "download", "src")) {
      getResource(arr);
    } else if (z.equals("eval")) {
      eval(v, arr);
    } else if (BeeUtils.inList(z, "f", "func")) {
      showFunctions(v, arr);
    } else if (z.equals("form") && arr.length == 2) {
      FormFactory.openForm(arr[1]);
    } else if (z.equals("fs")) {
      getFs();
    } else if (z.equals("gen") && BeeUtils.isDigit(ArrayUtils.getQuietly(arr, 2))) {
      BeeKeeper.getRpc().sendText(Service.GENERATE, BeeUtils.concat(1, arr[1], arr[2]));
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
    } else if (z.equals("import")) {
      FormFactory.importForm(args);
    } else if (BeeUtils.inList(z, "inp", "input")) {
      showInput();
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
      doMenu(arr);
    } else if (z.equals("meter")) {
      showMeter(arr);
    } else if (z.equals("md5")) {
      digest(v);
    } else if (z.equals("nf") && arr.length >= 3) {
      BeeKeeper.getLog().info(NumberFormat.getFormat(arr[1]).format(BeeUtils.toDouble(arr[2])));
    } else if (z.equals("notify") && arr.length >= 2) {
      showNotes(args);
    } else if (BeeUtils.inList(z, "p", "prop")) {
      showProperties(v, arr);
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
      BeeKeeper.getScreen().showGrid(Settings.getSettings());
    } else if (z.equals("size") && arr.length >= 2) {
      showSize(arr);
    } else if (z.equals("slider")) {
      showSlider(arr);
    } else if (z.equals("sql")) {
      doSql(args);
    } else if (z.equals("stack")) {
      showStack();
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
      showTiles();
    } else if (z.startsWith("tran") || z.startsWith("detec")) {
      translate(arr, z.startsWith("detec"));
    } else if (z.equals("uc") || "unicode".startsWith(z)) {
      unicode(arr);
    } else if (z.startsWith("unit")) {
      showUnits(arr);
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
    } else if (z.equals("xml") && arr.length >= 2) {
      showXmlInfo(arr);

    } else {
      Global.showDialog("wtf", v);
    }
  }

  public static void doSql(String sql) {
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
                BeeKeeper.getScreen().showGrid(rs);
              }
            }
          }
        });
  }

  public static void getCharsets() {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_RESOURCE);
    params.addPositionalHeader("cs");

    BeeKeeper.getRpc().makeGetRequest(params);
  }

  public static void getFs() {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_RESOURCE);
    params.addPositionalHeader("fs");

    BeeKeeper.getRpc().makeGetRequest(params);
  }

  public static void getKeys(String[] arr) {
    int parCnt = BeeUtils.length(arr) - 1;
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

  public static void getResource(String[] arr) {
    if (BeeUtils.length(arr) < 2) {
      Global.sayHuh(ArrayUtils.join(arr, 1));
      return;
    }

    if (BeeUtils.same(arr[0], "download")) {
      String url = GWT.getModuleBaseURL() + "file/" +
          Codec.encodeBase64(ArrayUtils.join(arr, 1, 1));
      Window.open(url, "", "");
      return;
    }

    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_RESOURCE);
    params.addPositionalHeader(arr);

    BeeKeeper.getRpc().makeGetRequest(params);
  }

  public static void playAudio(final String src) {
    if (BeeUtils.isEmpty(src)) {
      BeeKeeper.getLog().warning("source not specified");
      return;
    }

    final Audio widget = Audio.createIfSupported();
    if (widget == null) {
      BeeKeeper.getLog().severe("audio not supported");
      return;
    }

    widget.getAudioElement().setSrc(src);
    widget.getAudioElement().setControls(true);

    widget.addDomHandler(new ErrorHandler() {
      public void onError(ErrorEvent event) {
        BeeKeeper.getScreen().notifyWarning(src, EventUtils.transformMediaError(widget.getError()));
      }
    }, ErrorEvent.getType());

    BeeKeeper.getScreen().updateActivePanel(widget, ScrollBars.BOTH);
  }

  public static void playVideo(String args) {
    if (!Video.isSupported()) {
      BeeKeeper.getLog().severe("video not supported");
      return;
    }

    final String src = BeeUtils.ifString(args,
        "http://people.opera.com/shwetankd/webm/sunflower.webm");

    final Video widget = Video.createIfSupported();
    Assert.notNull(widget);
    widget.getVideoElement().setSrc(src);
    widget.getVideoElement().setControls(true);

    widget.addDomHandler(new ErrorHandler() {
      public void onError(ErrorEvent event) {
        BeeKeeper.getScreen().notifyWarning(src, EventUtils.transformMediaError(widget.getError()));
      }
    }, ErrorEvent.getType());

    BeeKeeper.getScreen().updateActivePanel(widget, ScrollBars.BOTH);
  }

  public static void querySelector(String command, String selectors) {
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

    if (showModal(cnt)) {
      Global.modalGrid("Selectors", info);
    } else {
      BeeKeeper.getScreen().showGrid(info);
    }
  }

  public static void rebuildSomething(String args) {
    BeeKeeper.getRpc().sendText(Service.REBUILD, args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasResponse(BeeResource.class)) {
          final BeeLayoutPanel p = new BeeLayoutPanel();

          final InputArea area = new InputArea(new BeeResource((String) response.getResponse()));
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

                        if (resp.hasResponse(BeeResource.class)) {
                          p.getWidget(1).removeFromParent();
                          InputArea res =
                              new InputArea(new BeeResource((String) resp.getResponse()));
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

                          if (resp.hasResponse(BeeResource.class)) {
                            InputArea res =
                                new InputArea(new BeeResource((String) resp.getResponse()));
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
            FormFactory.parseForm("<BeeForm><ResizePanel><Frame url=\"" + url
                + "\" /></ResizePanel></BeeForm>");
          }
        }
      }
    });
  }

  public static void showBrowser(String[] arr) {
    boolean wnd = false;
    boolean loc = false;
    boolean nav = false;
    boolean scr = false;

    for (int i = 1; i < BeeUtils.length(arr); i++) {
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

    BeeKeeper.getScreen().showGrid(info);
  }

  public static void showClientLocation() {
    AjaxLoader.load(new Runnable() {
      public void run() {
        ClientLocation location = AjaxLoader.getClientLocation();

        BeeKeeper.getScreen().showGrid(
            PropertyUtils.createProperties("City", location.getCity(),
                "Country", location.getCountry(), "Country Code", location.getCountryCode(),
                "Latitude", location.getLatitude(), "Longitude", location.getLongitude(),
                "Region", location.getRegion()));
      }
    });
  }

  public static void showDate(String[] arr) {
    int len = BeeUtils.length(arr);
    JustDate d;
    DateTime t;

    if (len > 1) {
      String s = ArrayUtils.join(arr, 1, 1);
      if (BeeUtils.context("T", s)) {
        Date j;
        try {
          j = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).parse(s);
        } catch (IllegalArgumentException ex) {
          Global.showError(s, ex);
          j = null;
        }
        if (j == null) {
          return;
        }
        t = new DateTime(j);
        d = new JustDate(j);
      } else {
        t = DateTime.parse(s);
        d = JustDate.parse(s);
      }
    } else {
      d = new JustDate();
      t = new DateTime();
    }

    List<Property> lst = PropertyUtils.createProperties(
        "Day", d.getDay(),
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

    BeeKeeper.getScreen().showGrid(lst);
  }

  public static void showDateFormat() {
    int r = DateTimeFormat.PredefinedFormat.values().length;
    String[][] data = new String[r][3];

    Date d = new Date();
    int i = 0;
    for (DateTimeFormat.PredefinedFormat dtf : DateTimeFormat.PredefinedFormat.values()) {
      data[i][0] = dtf.toString();

      DateTimeFormat format = DateTimeFormat.getFormat(dtf);
      data[i][1] = format.getPattern();
      data[i][2] = format.format(d);
      i++;
    }

    BeeKeeper.getScreen().showGrid(data, "Format", "Pattern", "Value");
  }

  public static void showDimensions() {
    String caption = "Dimensions";
    List<Property> data = PropertyUtils.createProperties(
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
        "Scrollbar width", DomUtils.getScrollbarWidth(),
        "Scrollbar height", DomUtils.getScrollbarHeight());

    if (showModal(data.size())) {
      Global.modalGrid(caption, data);
    } else {
      BeeKeeper.getScreen().showGrid(data);
    }
  }

  public static void showDnd() {
    if (!EventUtils.supportsDnd()) {
      BeeKeeper.getLog().warning("dnd not supported");
      return;
    }

    List<Property> lst = EventUtils.showDnd();
    if (BeeUtils.isEmpty(lst)) {
      Global.showDialog("dnd mappings empty");
    } else if (showModal(lst.size())) {
      Global.modalGrid(BeeUtils.concat(1, "Dnd", BeeUtils.bracket(lst.size())), lst);
    } else {
      BeeKeeper.getScreen().showGrid(lst);
    }
  }

  public static void showElement(String v, String[] arr) {
    if (BeeUtils.length(arr) < 2) {
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

    JsData<?> table = (JsData<?>) DataUtils.createTable(prp, "property", "type", "value");
    table.sort(0);

    if (showModal(table.getNumberOfRows())) {
      Global.modalGrid(v, table);
    } else {
      BeeKeeper.getScreen().showGrid(table);
    }
  }

  public static void showFunctions(String v, String[] arr) {
    if (BeeUtils.length(arr) < 2) {
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
      Global.showDialog(v, fnc.join());
      return;
    }

    JsData<?> table = (JsData<?>) DataUtils.createTable(fnc, "function");
    table.sort(0);

    if (BeeUtils.same(arr[0], "f") && showModal(table.getNumberOfRows())) {
      Global.modalGrid(v, table);
    } else {
      BeeKeeper.getScreen().showGrid(table);
    }
  }

  public static void showGeo() {
    BeeLabel widget = new BeeLabel("Looking for location...");
    getGeo(widget.getElement());
    BeeKeeper.getScreen().updateActivePanel(widget);
  }

  public static void showGwt() {
    List<Property> data = PropertyUtils.createProperties(
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

    if (showModal(data.size())) {
      Global.modalGrid("GWT", data);
    } else {
      BeeKeeper.getScreen().showGrid(data);
    }
  }

  public static void showInput() {
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

  public static void showMeter(String[] arr) {
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
    value = BeeUtils.limit(value, min, max);
    low = BeeUtils.limit(low, min, max);
    high = BeeUtils.limit(high, low, max);
    optimum = BeeUtils.limit(optimum, min, max);

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

  public static void showNotes(String args) {
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

  public static void showProgress(String[] arr) {
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

    panel.add(new BeeLabel(BeeUtils.addName("steps", steps)), 10, 36);
    panel.add(new BeeLabel(BeeUtils.addName("millis", millis)), 10, 53);
    panel.add(new BeeLabel(BeeUtils.addName("max", max)), 10, 70);

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
        lbl.setText(BeeUtils.concat(3, BeeUtils.round(v, 1), BeeUtils.round(prg.getPosition(), 3)));
      }
    };
    timer.scheduleRepeating(millis);

    BeeKeeper.getScreen().updateActivePanel(panel, ScrollBars.BOTH);
  }

  public static void showProperties(String v, String[] arr) {
    if (BeeUtils.length(arr) < 2) {
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

    JsData<?> table = (JsData<?>) DataUtils.createTable(prp, "property", "type", "value");
    table.sort(0);

    if (BeeUtils.same(arr[0], "p") && showModal(table.getNumberOfRows())) {
      Global.modalGrid(v, table);
    } else {
      BeeKeeper.getScreen().showGrid(table);
    }
  }

  public static void showRpc() {
    if (BeeKeeper.getRpc().getRpcList().isEmpty()) {
      Global.showDialog("RpcList empty");
    } else {
      BeeKeeper.getScreen().showGrid(BeeKeeper.getRpc().getRpcList().getDefaultInfo(),
          RpcList.DEFAULT_INFO_COLUMNS);
    }
  }

  public static void showSize(String[] arr) {
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

    Global.showWidget(panel);
  }

  public static void showSlider(String[] arr) {
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

  public static void showStack() {
    BeeKeeper.getLog().stack();
    BeeKeeper.getLog().addSeparator();
  }

  public static void showSupport() {
    BeeKeeper.getScreen().showGrid(Features.getInfo());
  }

  public static void showSvg(String[] arr) {
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

  public static void showTableInfo(String args) {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_TABLE_INFO);
    if (!BeeUtils.isEmpty(args)) {
      params.addPositionalHeader(args.trim());
    }
    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        BeeKeeper.getScreen().showGrid(
            PropertyUtils.restoreExtended((String) response.getResponse()));
      }
    });
  }

  public static void showTiles() {
    Widget tiles = BeeKeeper.getScreen().getScreenPanel().getCenter();
    if (!(tiles instanceof TilePanel)) {
      Global.showDialog("no tiles vailable");
    }

    BeeTree tree = new BeeTree();
    tree.addItem(((TilePanel) tiles).getTree(null, true));

    Global.inform(tree);
  }

  public static void showUnits(String[] arr) {
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
    if (showModal(info.size())) {
      Global.modalGrid("Pixels", info);
    } else {
      BeeKeeper.getScreen().showGrid(info);
    }
  }

  public static void showVars(String[] arr) {
    int len = BeeUtils.length(arr);
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

  public static void showViewInfo(String args) {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_VIEW_INFO);
    if (!BeeUtils.isEmpty(args)) {
      params.addPositionalHeader(args.trim());
    }
    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        BeeKeeper.getScreen().showGrid(
            PropertyUtils.restoreExtended((String) response.getResponse()));
      }
    });
  }

  public static void showWidgetInfo(String[] arr) {
    String id = ArrayUtils.getQuietly(arr, 1);
    if (BeeUtils.isEmpty(id)) {
      Global.showError("widget id not specified");
      return;
    }

    Widget widget = DomUtils.getWidget(BeeKeeper.getScreen().getScreenPanel(), id);
    if (widget == null) {
      Global.showError(id, "widget not found");
      return;
    }

    String z = ArrayUtils.getQuietly(arr, 2);
    int depth = BeeUtils.isDigit(z) ? BeeUtils.toInt(z) : 0;

    List<ExtendedProperty> info = DomUtils.getInfo(widget, id, depth);
    BeeKeeper.getScreen().showGrid(info);
  }

  public static void showXmlInfo(String[] arr) {
    ParameterList params = BeeKeeper.getRpc().createParameters(Service.GET_RESOURCE);
    params.addPositionalHeader(arr);

    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        BeeKeeper.getScreen().showGrid(XmlUtils.getInfo((String) response.getResponse()));
      }
    });
  }

  public static void storage(String[] arr) {
    int parCnt = ArrayUtils.length(arr) - 1;
    int len = BeeKeeper.getStorage().length();

    if (parCnt <= 1 && len <= 0) {
      Global.inform("Storage empty");
      return;
    }

    if (parCnt <= 0) {
      BeeKeeper.getScreen().showGrid(BeeKeeper.getStorage().getAll());
      return;
    }

    String key = arr[1];

    if (parCnt == 1) {
      if (key.equals(BeeConst.STRING_MINUS)) {
        BeeKeeper.getStorage().clear();
        Global.inform(BeeUtils.concat(1, len, "items cleared"));
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

    String value = ArrayUtils.join(arr, 1, 2);

    if (key.equals(BeeConst.STRING_MINUS)) {
      BeeKeeper.getStorage().removeItem(value);
      Global.inform(value, "removed");
    } else {
      BeeKeeper.getStorage().setItem(key, value);
      Global.inform("Storage", BeeUtils.addName(key, value));
    }
  }

  public static void style(String v, String[] arr) {
    if (BeeUtils.length(arr) < 2) {
      NodeList<Element> nodes = Document.get().getElementsByTagName("style");
      if (nodes == null || nodes.getLength() <= 0) {
        Global.showDialog("styles not available");
        return;
      }
      int stCnt = nodes.getLength();

      List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();
      PropertyUtils.addExtended(lst, "Styles", "count", stCnt);

      for (int i = 0; i < stCnt; i++) {
        String ref = "$doc.getElementsByTagName('style').item(" + i + ").sheet.rules";
        int len = JsUtils.evalToInt(ref + ".length");
        PropertyUtils.addExtended(lst, "Style " + BeeUtils.progress(i + 1, stCnt), "rules", len);

        for (int j = 0; j < len; j++) {
          JavaScriptObject obj = JsUtils.eval(ref + "[" + j + "]");
          if (obj == null) {
            PropertyUtils.addExtended(lst, "Rule", BeeUtils.progress(j + 1, len), "not available");
            break;
          }

          JsArrayString prp = JsUtils.getProperties(obj, null);
          for (int k = 0; k < prp.length() - 2; k += 3) {
            PropertyUtils.addExtended(lst,
                BeeUtils.concat(1, "Rule", BeeUtils.progress(j + 1, len), prp.get(k * 3)),
                prp.get(k * 3 + 1), prp.get(k * 3 + 2));
          }
        }
      }

      BeeKeeper.getScreen().showGrid(lst);
      return;
    }

    if (!v.contains("{")) {
      Element elem = Document.get().getElementById(arr[1]);
      if (elem == null) {
        Global.showDialog("element id", arr[1], "not found");
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
        Global.showDialog("element id", arr[1], "has no style");
      } else {
        BeeKeeper.getScreen().showGrid(info);
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

    BeeKeeper.getLog().info(st);
    BeeKeeper.getLog().addSeparator();

    if (start) {
      StyleInjector.injectAtStart(st, immediate);
    } else if (end) {
      StyleInjector.injectAtEnd(st, immediate);
    } else {
      StyleInjector.inject(st, immediate);
    }
  }

  public static void translate(final String[] arr, boolean detect) {
    int len = BeeUtils.length(arr);
    if (len < 2) {
      Global.sayHuh((Object[]) arr);
      return;
    }

    if (detect) {
      final String detText = ArrayUtils.join(arr, 1, 1, len);
      Translation.detect(detText, new DetectionCallback() {
        @Override
        protected void onCallback(DetectionResult result) {
          Global.modalGrid("Language Detection",
              PropertyUtils.createProperties("Text", detText,
                  "Language Code", result.getLanguage(),
                  "Language", Language.getByCode(result.getLanguage()),
                  "Confidence", result.getConfidence(),
                  "Reliable", result.isReliable()));
        }
      });
      return;
    }

    final String text;
    final String codeFrom;
    final String codeTo;

    Language dst = Language.getByCode(arr[len - 1]);

    if (dst == null) {
      text = ArrayUtils.join(arr, 1, 1, len);
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
          text = ArrayUtils.join(arr, 1, 1, len - 1);
          codeFrom = BeeConst.STRING_EMPTY;
        } else {
          text = ArrayUtils.join(arr, 1, 1, len - 2);
          codeFrom = src.getLangCode();
        }
      }
    }

    if (BeeUtils.isEmpty(text) || BeeUtils.same(text, BeeConst.STRING_ALL)) {
      BeeKeeper.getLog().info("Translate", codeFrom, codeTo);
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
        if (BeeUtils.length(elTxt) < 3) {
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

    final String info = ArrayUtils.join(arr, 1, 1, len);

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

  public static void unicode(String[] arr) {
    StringBuilder sb = new StringBuilder();
    int len = BeeUtils.length(arr);

    if (len < 2 || len == 2 && BeeUtils.isDigit(ArrayUtils.getQuietly(arr, 1))) {
      int n = (len < 2) ? 10 : BeeUtils.toInt(arr[1]);
      for (int i = 0; i < n; i++) {
        sb.append((char) BeeUtils.randomInt(Character.MIN_VALUE, Character.MAX_VALUE + 1));
      }

    } else {
      for (int i = 1; i < len; i++) {
        String s = arr[i];

        if (s.length() > 1 && BeeUtils.inListIgnoreCase(s.substring(0, 1), "u", "x")
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

    int id = BeeKeeper.getRpc().invoke("stringInfo", ContentType.BINARY, s);
    BeeKeeper.getRpc().addUserData(id, "length", s.length(), "data", s,
        "adler32", Codec.adler32(bytes), "crc16", Codec.crc16(bytes),
        "crc32", Codec.crc32(bytes), "crc32d", Codec.crc32Direct(bytes));
  }

  public static void whereAmI() {
    BeeKeeper.getLog().info(BeeConst.whereAmI());
    BeeKeeper.getRpc().makeGetRequest(Service.WHERE_AM_I);
  }

  private static native void cornifyAdd() /*-{
    try {
      $wnd.cornify_add();
    } catch (err) {
    }
  }-*/;

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

  private static native void sampleCanvas(Element el) /*-{
    var ctx = el.getContext("2d");

    for ( var i = 0; i < 6; i++) {
      for ( var j = 0; j < 6; j++) {
        ctx.fillStyle = 'rgb(' + Math.floor(255 - 42.5 * i) + ', ' + Math.floor(255 - 42.5 * j) + ', 0)';
        ctx.fillRect(j * 25, i * 25, 25, 25);
      }
    }
  }-*/;

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

  private CliWorker() {
  }
}

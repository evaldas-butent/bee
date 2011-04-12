package com.butent.bee.client.cli;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.canvas.CanvasDemo;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.visualization.showcase.Showcase;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeTextBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class CliWidget extends BeeTextBox {
  public CliWidget() {
    super();
  }

  public CliWidget(Element element) {
    super(element);
  }

  @Override
  public boolean onBeeKey(KeyPressEvent event) {
    if (BeeUtils.isEmpty(getValue())) {
      return true;
    }

    String v = getValue().trim();
    String[] arr = BeeUtils.split(v, BeeConst.STRING_SPACE);
    Assert.notEmpty(arr);

    String z = arr[0].toLowerCase();

    if (z.equals("?")) {
      CliWorker.whereAmI();
    } else if (z.startsWith("ajaxk") || z.startsWith("apik") || z.startsWith("gook")) {
      CliWorker.doAjaxKeys(arr);
    } else if (z.equals("audio")) {
      CliWorker.playAudio(arr);
    } else if (z.equals("browser") || z.startsWith("wind")) {
      CliWorker.showBrowser(arr);
    } else if (z.equals("canvas")) {
      new CanvasDemo().start();
    } else if (BeeUtils.inList(z, "center", "east", "north", "south", "screen", "west")) {
      CliWorker.doScreen(arr);
    } else if (z.equals("charset")) {
      CliWorker.getCharsets();
    } else if (z.equals("clear")) {
      CliWorker.clearLog();
    } else if (z.startsWith("client")) {
      CliWorker.showClientLocation();
    } else if (z.startsWith("conf")) {
      BeeKeeper.getRpc().invoke("configInfo");
    } else if (z.startsWith("conn") || z.equals("http")) {
      BeeKeeper.getRpc().invoke("connectionInfo");
    } else if (z.equals("cornify")) {
      CliWorker.cornify(arr);
    } else if (z.equals("df")) {
      CliWorker.showDateFormat();
    } else if (z.equals("dnd")) {
      CliWorker.showDnd();
    } else if (z.equals("dt")) {
      CliWorker.showDate(arr);
    } else if (BeeUtils.inList(z, "dir", "file", "get", "download", "src")) {
      CliWorker.getResource(arr);
    } else if (z.equals("eval")) {
      CliWorker.eval(v, arr);
    } else if (BeeUtils.inList(z, "f", "func")) {
      CliWorker.showFunctions(v, arr);
    } else if (z.equals("fs")) {
      CliWorker.getFs();
    } else if (z.equals("geo")) {
      CliWorker.showGeo();
    } else if (z.equals("gwt")) {
      CliWorker.showGwt();
    } else if (BeeUtils.inList(z, "h5", "html5", "supp", "support")) {
      CliWorker.showSupport();
    } else if (z.equals("id")) {
      CliWorker.showElement(v, arr);
    } else if (BeeUtils.inList(z, "inp", "input")) {
      CliWorker.showInput();
    } else if (BeeUtils.inList(z, "keys", "pk")) {
      CliWorker.getKeys(arr);
    } else if (z.startsWith("like") && arr.length >= 3) {
      CliWorker.doLike(arr);
    } else if (z.equals("loaders")) {
      BeeKeeper.getRpc().invoke("loaderInfo");
    } else if (z.equals("locale")) {
      CliWorker.doLocale(arr);
    } else if (z.equals("log")) {
      CliWorker.doLog(arr);
    } else if (z.equals("menu")) {
      CliWorker.doMenu(arr);
    } else if (z.equals("meter")) {
      CliWorker.showMeter(arr);
    } else if (z.equals("md5")) {
      CliWorker.digest(v);
    } else if (z.equals("nf") && arr.length >= 3) {
      BeeKeeper.getLog().info(NumberFormat.getFormat(arr[1]).format(BeeUtils.toDouble(arr[2])));
    } else if (BeeUtils.inList(z, "p", "prop")) {
      CliWorker.showProperties(v, arr);
    } else if (z.equals("progress")) {
      CliWorker.showProgress(arr);
    } else if (z.equals("rpc")) {
      CliWorker.showRpc();
    } else if (z.equals("sb")) {
      Global.showDialog("Scrollbar",
          BeeUtils.addName("width", DomUtils.getScrollbarWidth()),
          BeeUtils.addName("height", DomUtils.getScrollbarHeight()));
    } else if (z.startsWith("serv") || z.startsWith("sys")) {
      BeeKeeper.getRpc().invoke("systemInfo");
    } else if (z.equals("slider")) {
      CliWorker.showSlider(arr);
    } else if (z.equals("stack")) {
      CliWorker.showStack();
    } else if (z.startsWith("stor")) {
      CliWorker.storage(arr);
    } else if (z.equals("style")) {
      CliWorker.style(v, arr);
    } else if (z.equals("svg")) {
      CliWorker.showSvg(arr);
    } else if (z.equals("tb")) {
      Global.showDialog("TextBox",
          BeeUtils.addName("client width", DomUtils.getTextBoxClientWidth()),
          BeeUtils.addName("client height", DomUtils.getTextBoxClientHeight()),
          BeeUtils.addName("offset width", DomUtils.getTextBoxOffsetWidth()),
          BeeUtils.addName("offset height", DomUtils.getTextBoxOffsetHeight()));
    } else if (z.equals("tiles")) {
      CliWorker.showTiles();
    } else if (z.startsWith("tran") || z.startsWith("detec")) {
      CliWorker.translate(arr, z.startsWith("detec"));
    } else if (z.equals("uc") || "unicode".startsWith(z)) {
      CliWorker.unicode(arr);
    } else if (z.equals("vars")) {
      CliWorker.showVars(arr);
    } else if (z.equals("video")) {
      CliWorker.playVideo(arr);
    } else if (z.startsWith("viz")) {
      Showcase.open();
    } else if (z.equals("vm")) {
      BeeKeeper.getRpc().invoke("vmInfo");

    } else if (z.equals("gen") && BeeUtils.isDigit(ArrayUtils.getQuietly(arr, 2))) {
      BeeKeeper.getRpc().sendText(Service.GENERATE, BeeUtils.concat(1, arr[1], arr[2]));
    } else if (z.equals("rebuild")) {
      BeeKeeper.getRpc().sendText(Service.REBUILD, v);
    } else if (z.equals("sql")) {
      BeeKeeper.getRpc().sendText(Service.DO_SQL, v,
          new ResponseCallback() {
            @Override
            public void onResponse(JsArrayString respArr) {
              BeeRowSet rs = BeeRowSet.restore(respArr.get(0));

              if (rs.isEmpty()) {
                BeeKeeper.getUi().updateActivePanel(new BeeLabel("RowSet is empty"));
              } else {
                BeeKeeper.getUi().showGrid(rs);
              }
            }
          });

    } else {
      Global.showDialog("wtf", v);
    }

    return false;
  }
}

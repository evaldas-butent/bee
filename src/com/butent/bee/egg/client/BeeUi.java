package com.butent.bee.egg.client;

import java.util.Date;

import com.butent.bee.egg.client.cli.CliWidget;
import com.butent.bee.egg.client.composite.ButtonGroup;
import com.butent.bee.egg.client.composite.RadioGroup;
import com.butent.bee.egg.client.layout.BeeFlow;
import com.butent.bee.egg.client.layout.BeeScroll;
import com.butent.bee.egg.client.layout.BeeSplit;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.client.widget.BeeButton;
import com.butent.bee.egg.client.widget.BeeCheckBox;
import com.butent.bee.egg.client.widget.BeeLabel;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeName;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.Pair;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class BeeUi implements BeeModule {
  private final HasWidgets rootUi;

  private String screenId = null;
  private String activeId = null;
  private Panel activePanel = null;

  private String elDsn = null;

  public BeeUi(HasWidgets root) {
    this.rootUi = root;
  }

  public String getScreenId() {
    return screenId;
  }

  public void setScreenId(String screenId) {
    this.screenId = screenId;
  }

  public String getActiveId() {
    return activeId;
  }

  public void setActiveId(String activeId) {
    this.activeId = activeId;
  }

  public String getElDsn() {
    return elDsn;
  }

  public void setElDsn(String elDsn) {
    this.elDsn = elDsn;
  }

  public Panel getActivePanel() {
    return activePanel;
  }

  public void setActivePanel(Panel activePanel) {
    this.activePanel = activePanel;
  }

  public HasWidgets getRootUi() {
    return rootUi;
  }

  public String getName() {
    return getClass().getName();
  }

  public int getPriority(int p) {
    switch (p) {
    case PRIORITY_INIT:
      return DO_NOT_CALL;
    case PRIORITY_START:
      return 10;
    case PRIORITY_END:
      return DO_NOT_CALL;
    default:
      return DO_NOT_CALL;
    }
  }

  public void init() {
  }

  public void start() {
    createUi();
  }

  public void end() {
  }

  public void createUi() {
    Widget w;
    BeeSplit p = new BeeSplit();

    w = initNorth();
    if (w != null)
      p.addNorth(w, 40);

    w = initSouth();
    if (w != null)
      p.addSouth(w, 28);

    w = initWest();
    if (w != null)
      p.addWest(w, 200);

    w = initEast();
    if (w != null)
      p.addEast(w, 200);

    w = initCenter();
    if (w != null) {
      setActiveWidget(w);
      p.add(w);
    }

    rootUi.add(p);

    setScreenId(p.getId());
  }

  private Widget initCenter() {
    int r = DateTimeFormat.PredefinedFormat.values().length;

    String[] cols = { "Format", "Value" };
    String[][] data = new String[r][2];

    int i = 0;
    for (DateTimeFormat.PredefinedFormat dtf : DateTimeFormat.PredefinedFormat
        .values()) {
      data[i][0] = dtf.toString();
      data[i][1] = DateTimeFormat.getFormat(dtf).format(new Date());
      i++;
    }

    return new BeeScroll(BeeGlobal.createSimpleGrid(cols, data));
  }

  private Widget initEast() {
    return new BeeScroll(BeeKeeper.getLog().getArea());
  }

  private Widget initNorth() {
    BeeFlow p = new BeeFlow();

    setElDsn(BeeDom.createUniqueName());
    p.add(new RadioGroup(getElDsn(), BeeConst.DS_TYPES));

    p.add(new ButtonGroup("Ping", BeeService.SERVICE_DB_PING, "Info",
        BeeService.SERVICE_DB_INFO, "Tables", BeeService.SERVICE_DB_TABLES));

    p.add(new ButtonGroup("Http", BeeService.SERVICE_TEST_CONNECTION, "Server",
        BeeService.SERVICE_SERVER_INFO, "VM", BeeService.SERVICE_VM_INFO,
        "Loaders", BeeService.SERVICE_LOADER_INFO));

    p.add(new BeeButton("Class", BeeService.SERVICE_GET_CLASS,
        BeeStage.STAGE_GET_PARAMETERS));
    p.add(new BeeButton("Xml", BeeService.SERVICE_GET_XML,
        BeeStage.STAGE_GET_PARAMETERS));
    p.add(new BeeButton("Jdbc", BeeService.SERVICE_GET_DATA,
        BeeStage.STAGE_GET_PARAMETERS));

    p.add(new BeeButton("Login", BeeService.SERVICE_LOGIN));

    p.add(new BeeCheckBox(new Pair<String, String>("Get", "Post"),
        BeeProperties.COMMUNICATION_METHOD));
    p.add(new BeeCheckBox(new BeeName(BeeGlobal.FIELD_DEBUG)));

    p.add(new BeeButton("North land", "comp_ui_form",
        BeeStage.STAGE_GET_PARAMETERS));

    return p;
  }

  private Widget initSouth() {
    return new CliWidget();
  }

  private Widget initWest() {
    return new BeeLabel("West");
  }

  private void setActiveWidget(Widget w) {
    if (w instanceof Panel) {
      setActivePanel((Panel) w);

      if (w instanceof HasId)
        setActiveId(((HasId) w).getId());
    }
  }

  public String getDsn() {
    return BeeUtils.getElement(BeeConst.DS_TYPES,
        RadioGroup.getValue(getElDsn()));
  }

  public void updateActivePanel(Widget w) {
    if (w == null)
      return;

    Panel p = getActivePanel();

    if (p instanceof SimplePanel)
      ((SimplePanel) p).setWidget(w);
  }

}

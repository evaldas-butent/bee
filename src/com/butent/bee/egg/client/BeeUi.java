package com.butent.bee.egg.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.cli.CliWidget;
import com.butent.bee.egg.client.composite.ButtonGroup;
import com.butent.bee.egg.client.composite.RadioGroup;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.grid.BeeFlexTable;
import com.butent.bee.egg.client.layout.BeeFlow;
import com.butent.bee.egg.client.layout.BeeLayoutPanel;
import com.butent.bee.egg.client.layout.BeeScroll;
import com.butent.bee.egg.client.layout.BeeSplit;
import com.butent.bee.egg.client.ui.GwtUiCreator;
import com.butent.bee.egg.client.widget.BeeButton;
import com.butent.bee.egg.client.widget.BeeCheckBox;
import com.butent.bee.egg.client.widget.BeeIntegerBox;
import com.butent.bee.egg.client.widget.BeeListBox;
import com.butent.bee.egg.client.widget.BeeSimpleCheckBox;
import com.butent.bee.egg.client.widget.BeeTextArea;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeName;
import com.butent.bee.egg.shared.BeeResource;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.Pair;
import com.butent.bee.egg.shared.menu.MenuConst;
import com.butent.bee.egg.shared.ui.UiComponent;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Date;

public class BeeUi implements BeeModule {
  private final HasWidgets rootUi;

  private BeeSplit screenPanel = null;
  private Panel activePanel = null;
  private Panel menuPanel = null;

  private String elDsn = null;

  public BeeUi(HasWidgets root) {
    this.rootUi = root;
  }

  public void end() {
  }

  public Panel getActivePanel() {
    return activePanel;
  }

  public String getDsn() {
    return BeeUtils.getElement(BeeConst.DS_TYPES,
        RadioGroup.getValue(getElDsn()));
  }

  public String getElDsn() {
    return elDsn;
  }

  public Panel getMenuPanel() {
    return menuPanel;
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

  public HasWidgets getRootUi() {
    return rootUi;
  }

  public BeeSplit getScreenPanel() {
    return screenPanel;
  }

  public void init() {
  }

  public void setActivePanel(Panel activePanel) {
    this.activePanel = activePanel;
  }

  public void setElDsn(String elDsn) {
    this.elDsn = elDsn;
  }

  public void setMenuPanel(Panel menuPanel) {
    this.menuPanel = menuPanel;
  }

  public void setScreenPanel(BeeSplit screenPanel) {
    this.screenPanel = screenPanel;
  }

  public void showGrid(Object data, String... cols) {
    Assert.notNull(data);
    updateActiveQuietly(BeeGlobal.simpleGrid(data, cols));
  }

  public void showResource(BeeResource resource) {
    Assert.notNull(resource);
    updateActivePanel(new BeeTextArea(resource));
  }
  
  public void start() {
    UiComponent.setCreator(new GwtUiCreator());
    createUi();
  }

  public void updateActivePanel(Widget w) {
    Assert.notNull(w);
    Panel p = getActivePanel();
    
    if (p == null) {
      BeeSplit screen = getScreenPanel();
      screen.updateCenter(w);
    } else {
      p.clear();
      p.add(w);
    }
  }

  public void updateActiveQuietly(Widget w) {
    if (w != null) {
      updateActivePanel(w);
    }
  }

  public void updateMenu(Widget w) {
    Assert.notNull(w);

    Panel p = getMenuPanel();
    Assert.notNull(p);

    p.clear();
    p.add(w);
  }
  
  private void createUi() {
    Widget w;
    BeeSplit p = new BeeSplit();

    w = initNorth();
    if (w != null) {
      p.addNorth(w, 40);
    }

    w = initSouth();
    if (w != null) {
      p.addSouth(w, 32);
    }

    w = initWest();
    if (w != null) {
      p.addWest(w, 400);
    }

    w = initEast();
    if (w != null) {
      p.addEast(w, 200);
    }

    w = initCenter();
    if (w != null) {
      p.add(w);
    }

    rootUi.add(p);

    setScreenPanel(p);
  }

  private Widget initCenter() {
    int r = DateTimeFormat.PredefinedFormat.values().length;
    String[][] data = new String[r][2];

    int i = 0;
    for (DateTimeFormat.PredefinedFormat dtf : DateTimeFormat.PredefinedFormat.values()) {
      data[i][0] = dtf.toString();
      data[i][1] = DateTimeFormat.getFormat(dtf).format(new Date());
      i++;
    }

    return BeeGlobal.simpleGrid(data, "Format", "Value");
  }

  private Widget initEast() {
    return new BeeScroll(BeeKeeper.getLog().getArea());
  }

  private Widget initNorth() {
    BeeFlow p = new BeeFlow();

    setElDsn(DomUtils.createUniqueName());
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

    p.add(new BeeButton("North land", "comp_ui_form", "stage_dummy"));

    return p;
  }

  private Widget initSouth() {
    return new BeeLayoutPanel(new CliWidget());
  }

  private Widget initWest() {
    BeeSplit spl = new BeeSplit();

    BeeFlexTable fp = new BeeFlexTable();
    fp.setCellSpacing(3);

    int r = MenuConst.MAX_MENU_DEPTH;
    String fld, cap;

    for (int i = MenuConst.ROOT_MENU_INDEX; i < r; i++) {
      fld = MenuConst.fieldMenuLayout(i);
      cap = BeeGlobal.getFieldCaption(fld);

      if (!BeeUtils.isEmpty(cap)) {
        fp.setText(i, 0, cap);
      }
      fp.setWidget(i, 1, new BeeListBox(fld));

      fld = MenuConst.fieldMenuBarType(i);
      fp.setWidget(i, 2, new BeeSimpleCheckBox(fld));
    }

    fp.setWidget(MenuConst.ROOT_MENU_INDEX, 3, new BeeIntegerBox(
        MenuConst.FIELD_ROOT_LIMIT));
    fp.setWidget(MenuConst.ROOT_MENU_INDEX + 1, 3, new BeeIntegerBox(
        MenuConst.FIELD_ITEM_LIMIT));

    fp.setWidget(r - 1, 3, new BeeButton("Refresh",
        BeeService.SERVICE_REFRESH_MENU));

    fp.setWidget(r - 1, 4, new BeeButton("BEE", "comp_ui_menu", "stage_dummy"));

    spl.addNorth(fp, 100);

    BeeLayoutPanel mp = new BeeLayoutPanel();
    spl.add(mp);

    setMenuPanel(mp);

    return spl;
  }

}

package com.butent.bee.egg.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.cli.CliWidget;
import com.butent.bee.egg.client.composite.ButtonGroup;
import com.butent.bee.egg.client.composite.RadioGroup;
import com.butent.bee.egg.client.composite.TextEditor;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.grid.BeeFlexTable;
import com.butent.bee.egg.client.layout.BeeDirection;
import com.butent.bee.egg.client.layout.BeeFlow;
import com.butent.bee.egg.client.layout.BeeHorizontal;
import com.butent.bee.egg.client.layout.BeeLayoutPanel;
import com.butent.bee.egg.client.layout.BeeSplit;
import com.butent.bee.egg.client.resources.Images;
import com.butent.bee.egg.client.ui.GwtUiCreator;
import com.butent.bee.egg.client.utils.BeeCommand;
import com.butent.bee.egg.client.widget.BeeButton;
import com.butent.bee.egg.client.widget.BeeCheckBox;
import com.butent.bee.egg.client.widget.BeeImage;
import com.butent.bee.egg.client.widget.BeeIntegerBox;
import com.butent.bee.egg.client.widget.BeeLabel;
import com.butent.bee.egg.client.widget.BeeListBox;
import com.butent.bee.egg.client.widget.BeeSimpleCheckBox;
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

public class BeeUi implements BeeModule {

  private class SplitCommand extends BeeCommand {
    BeeDirection direction = null;
    boolean collapse = false;
    
    public SplitCommand(BeeDirection direction) {
      super();
      this.direction = direction;
    }

    public SplitCommand(boolean collapse) {
      super();
      this.collapse = collapse;
    }

    @Override
    public void execute() {
      if (collapse) {
        closePanel();
      } else {
        createPanel(direction);
      }
    }
  }
  
  private final HasWidgets rootUi;

  private BeeSplit screenPanel = null;
  private Panel activePanel = null;
  private Panel menuPanel = null;
  
  private Images images = GWT.create(Images.class);

  private String elDsn = null;

  public BeeUi(HasWidgets root) {
    this.rootUi = root;
  }

  public void end() {
  }

  public Panel getActivePanel() {
    return activePanel;
  }

  public int getActivePanelHeight() {
    Panel p = getActivePanel();
    
    if (p == null) {
      return getScreenPanel().getCenterHeight();
    } else {
      return p.getOffsetHeight();
    }
  }

  public int getActivePanelWidth() {
    Panel p = getActivePanel();
    
    if (p == null) {
      return getScreenPanel().getCenterWidth();
    } else {
      return p.getOffsetWidth();
    }
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

  public void setActivePanel(Panel p) {
    if (activePanel != null) {
      activePanel.getElement().getStyle().clearBackgroundColor();
    }
    activePanel = p;
    if (p != null) {
      p.getElement().getStyle().setBackgroundColor("Honeydew");
    }
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
    updateActivePanel(new TextEditor(resource));
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
  
  private void closePanel() {
    Panel p = getActivePanel();
    if (p != null && p.getParent() instanceof HasWidgets) {
      if (p.getParent() == getScreenPanel()) {
        setActivePanel(null);
      } else {
        setActivePanel((Panel) p.getParent());
      }
      ((HasWidgets) p.getParent()).remove(p);
    } else if (getScreenPanel().getCenter() != null) {
      getScreenPanel().remove(getScreenPanel().getCenter());
      setActivePanel(null);
    }
  }

  @SuppressWarnings("incomplete-switch")
  private void createPanel(BeeDirection direction) {
    Assert.notNull(direction);
    Assert.isTrue(direction != BeeDirection.CENTER);
    
    BeeSplit active = getScreenPanel();
    while (active.getCenter() instanceof BeeSplit) {
      active = (BeeSplit) active.getCenter();
    }
    
    int w = active.getCenterWidth();
    int h = active.getCenterHeight();
    
    Widget oldCenter = active.getCenter();
    if (oldCenter == null) {
      oldCenter = new BeeLayoutPanel();
    } else {
      active.remove(oldCenter);
    }
    
    BeeSplit p = new BeeSplit();

    switch (direction) {
      case NORTH :
        p.addSouth(oldCenter, h / 2);
        break;
      case SOUTH :
        p.addNorth(oldCenter, h / 2);
        break;
      case EAST :
        p.addWest(oldCenter, w / 2);
        break;
      case WEST:
        p.addEast(oldCenter, w / 2);
        break;
    }
    
    BeeLayoutPanel z = new BeeLayoutPanel();
    setActivePanel(z);
    
    p.add(z);
    active.add(p);
  }

  private void createUi() {
    Widget w;
    BeeSplit p = new BeeSplit();

    w = initNorth();
    if (w != null) {
      p.addNorth(w, 52);
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
    return null;
  }

  private Widget initEast() {
    return BeeKeeper.getLog().getArea();
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
    
    BeeLayoutPanel blp = new BeeLayoutPanel();
    blp.add(p);

    BeeImage bee = new BeeImage(images.bee());
    blp.add(bee);
    
    blp.setWidgetLeftRight(p, 1, Unit.EM, 100, Unit.PX);
    blp.setWidgetTopBottom(p, 1, Unit.EM, 1, Unit.PX);
    blp.setWidgetRightWidth(bee, 10, Unit.PX, 64, Unit.PX);
    
    return blp;
  }

  private Widget initSouth() {
    BeeLayoutPanel p = new BeeLayoutPanel();

    CliWidget cli = new CliWidget();
    p.add(cli);
    
    BeeHorizontal hor = new BeeHorizontal();
    hor.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    hor.add(new BeeButton("N", new SplitCommand(BeeDirection.NORTH)));
    hor.add(new BeeButton("S", new SplitCommand(BeeDirection.SOUTH)));
    hor.add(new BeeButton("E", new SplitCommand(BeeDirection.EAST)));
    hor.add(new BeeButton("W", new SplitCommand(BeeDirection.WEST)));

    BeeImage close = new BeeImage(images.close(), new SplitCommand(true));
    hor.add(close);
    hor.setCellWidth(close, "32px");
    hor.setCellHorizontalAlignment(close, HasHorizontalAlignment.ALIGN_RIGHT);
    
    p.add(hor);
    
    BeeLabel ver = new BeeLabel("0.1.2");
    p.add(ver);
    
    p.setWidgetLeftWidth(cli, 1, Unit.EM, 50, Unit.PCT);
    p.setWidgetVerticalPosition(cli, Layout.Alignment.BEGIN);
    
    p.setWidgetLeftWidth(hor, 60, Unit.PCT, 200, Unit.PX);
    
    p.setWidgetRightWidth(ver, 1, Unit.EM, 5, Unit.EM);
    
    return p;
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

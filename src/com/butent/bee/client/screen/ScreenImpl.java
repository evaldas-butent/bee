package com.butent.bee.client.screen;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.Screen;
import com.butent.bee.client.Settings;
import com.butent.bee.client.cli.CliWidget;
import com.butent.bee.client.composite.ButtonGroup;
import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.client.composite.ResourceEditor;
import com.butent.bee.client.composite.ValueSpinner;
import com.butent.bee.client.composite.VolumeSlider;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.TextCellType;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.layout.BlankTile;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.layout.Stack;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.layout.TilePanel;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.ui.CompositeService;
import com.butent.bee.client.ui.DsnService;
import com.butent.bee.client.ui.FormService;
import com.butent.bee.client.ui.GwtUiCreator;
import com.butent.bee.client.ui.MenuService;
import com.butent.bee.client.ui.RowSetService;
import com.butent.bee.client.ui.StateService;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.View;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.SimpleBoolean;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Handles default (desktop) screen implementation.
 */

public class ScreenImpl implements Screen {

  private class SplitCommand extends BeeCommand {
    Direction direction = null;
    boolean close = false;

    public SplitCommand(boolean close) {
      super();
      this.close = close;
    }

    public SplitCommand(Direction direction) {
      super();
      this.direction = direction;
    }

    @Override
    public void execute() {
      if (close) {
        closePanel();
      } else {
        createPanel(direction);
      }
    }
  }

  private LayoutPanel rootPanel;

  private int minTileSize = 20;
  private boolean temporaryDetach = false;

  private Split screenPanel = null;
  private TilePanel activePanel = null;
  private HasWidgets menuPanel = null;
  private HasWidgets dataPanel = null;

  private BeeButton authButton = null;
  private Widget signature = null;

  private final String elGrid = "el-grid-type";
  private final String elCell = "el-cell-type";

  private Notification notification = null;

  public ScreenImpl() {
  }

  public void activatePanel(TilePanel np) {
    Assert.notNull(np);

    TilePanel op = getActivePanel();
    if (op == np) {
      return;
    }

    deactivatePanel();

    if (!isRootTile(np)) {
      Widget w = np.getCenter();

      if (w instanceof BlankTile) {
        w.addStyleName(StyleUtils.ACTIVE_BLANK);
      } else if (w != null) {
        np.getWidgetContainerElement(w).addClassName(StyleUtils.ACTIVE_CONTENT);
      }
    }

    setActivePanel(np);
  }

  public void closeView(View view) {
    Assert.notNull(view, "closeView: view is null");
    Widget widget = view.asWidget();
    Assert.notNull(widget, "closeView: view widget is null");

    TilePanel panel = getPanel(widget);
    if (panel == null) {
      notifyWarning("closeView: panel not found");
      return;
    }

    if (panel != getActivePanel()) {
      activatePanel(panel);
    }
    closePanel();
  }

  public void end() {
  }

  public TilePanel getActivePanel() {
    return activePanel;
  }

  public int getActivePanelHeight() {
    TilePanel p = getActivePanel();
    Assert.notNull(p);
    return p.getOffsetHeight();
  }

  public int getActivePanelWidth() {
    TilePanel p = getActivePanel();
    Assert.notNull(p);
    return p.getOffsetWidth();
  }

  public String getName() {
    return BeeUtils.getClassName(getClass());
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

  public Split getScreenPanel() {
    return screenPanel;
  }

  public void init() {
  }

  public boolean isTemporaryDetach() {
    return temporaryDetach;
  }

  public void notifyInfo(String... messages) {
    if (getNotification() != null) {
      getNotification().info(messages);
    }
  }

  public void notifySevere(String... messages) {
    if (getNotification() != null) {
      getNotification().severe(messages);
    }
  }

  public void notifyWarning(String... messages) {
    if (getNotification() != null) {
      getNotification().warning(messages);
    }
  }

  public void setRootPanel(LayoutPanel rootPanel) {
    this.rootPanel = rootPanel;
  }

  public void showGrid(Object data, String... cols) {
    Assert.notNull(data);
    Widget grd = null;
    ScrollBars scroll;

    switch (getDefaultGridType()) {
      case 1:
        grd = Global.scrollGrid(getActivePanelWidth(), data, cols);
        scroll = ScrollBars.NONE;
        break;
      case 2:
        grd = Global.cellTable(data, getDefaultCellType(), cols);
        scroll = ScrollBars.BOTH;
        break;
      default:
        grd = Global.simpleGrid(data, cols);
        scroll = ScrollBars.BOTH;
    }

    updateActiveQuietly(grd, scroll);
  }

  public void showResource(BeeResource resource) {
    Assert.notNull(resource);
    updateActivePanel(new ResourceEditor(resource));
  }

  public void start() {
    UiComponent.setUiCreator(new GwtUiCreator());
    createUi();
  }

  public void updateActivePanel(Widget w) {
    updateActivePanel(w, ScrollBars.NONE);
  }

  public void updateActivePanel(Widget w, ScrollBars scroll) {
    Assert.notNull(w);

    TilePanel p = getActivePanel();
    Assert.notNull(p, "panel not available");

    deactivatePanel();
    p.clear();
    p.add(w, scroll);
    activatePanel(p);
  }

  public void updateActiveQuietly(Widget w, ScrollBars scroll) {
    if (w != null) {
      updateActivePanel(w, scroll);
    }
  }

  public void updateData(Widget w) {
    updatePanel(getDataPanel(), w);
  }

  public void updateMenu(Widget w) {
    updatePanel(getMenuPanel(), w);
  }

  public void updateUser(String service) {
    if (getSignature() == null) {
      return;
    }

    String usr = BeeKeeper.getUser().getUserSign();
    if (BeeUtils.isEmpty(usr)) {
      updateAuthWidget(true);
      usr = Global.constants.notLoggedIn();
    } else {
      updateAuthWidget(false);
      if (BeeUtils.same(service, Service.LOGIN) && getMenuPanel() != null
          && DomUtils.isEmpty(getMenuPanel())) {
        BeeKeeper.getBus().dispatchService(Service.REFRESH_MENU, null, null);
      }
    }
    
    getSignature().getElement().setInnerHTML(usr);
  }

  protected void closePanel() {
    TilePanel op = getActivePanel();
    Assert.notNull(op, "active panel not available");

    if (!(op.getCenter() instanceof BlankTile)) {
      deactivatePanel();
      op.clear();
      op.add(new BlankTile());
      activatePanel(op);
      return;
    }

    if (!(op.getParent() instanceof TilePanel)) {
      return;
    }

    TilePanel parent = (TilePanel) op.getParent();
    TilePanel np = null;

    for (TilePanel w : parent.getPanels()) {
      if (w != op) {
        np = w;
        break;
      }
    }

    Assert.notNull(np, "sibling panel not found");

    deactivatePanel();

    setTemporaryDetach(true);
    np.move(parent);
    setTemporaryDetach(false);

    while (parent.getCenter() instanceof TilePanel) {
      parent = (TilePanel) parent.getCenter();
    }

    activatePanel(parent);
  }

  protected void createUi() {
    Widget w;
    Split p = new Split(2);

    w = initNorth();
    if (w != null) {
      p.addNorth(w, 64);
    }

    w = initSouth();
    if (w != null) {
      p.addSouth(w, 32);
    }

    w = initWest();
    if (w != null) {
      p.addWest(w, 280);
    }

    w = initEast();
    if (w != null) {
      p.addEast(w, 256, ScrollBars.BOTH);
    }

    w = initCenter();
    if (w != null) {
      p.add(w, ScrollBars.BOTH);
    }

    getRootPanel().add(p);

    setScreenPanel(p);
  }

  protected HasWidgets getDataPanel() {
    return dataPanel;
  }

  protected TextCellType getDefaultCellType() {
    return TextCellType.get(RadioGroup.getValue(getElCell()));
  }

  protected int getDefaultGridType() {
    return RadioGroup.getValue(getElGrid());
  }

  protected Notification getNotification() {
    return notification;
  }

  protected LayoutPanel getRootPanel() {
    return rootPanel;
  }

  protected Widget getSignature() {
    return signature;
  }

  protected Widget initCenter() {
    TilePanel p = new TilePanel();
    p.add(new BlankTile());

    setActivePanel(p);
    return p;
  }

  protected Widget initEast() {
    return BeeKeeper.getLog().getArea();
  }

  protected Widget initNorth() {
    Complex cp = new Complex();
    
    cp.addLeftTop(new BeeImage(Global.getImages().bee()), 1, 1);
   
    BeeButton auth = new BeeButton(true);
    cp.addLeftTop(auth, 80, 4);
    setAuthButton(auth);

    BeeLabel user = new BeeLabel();
    cp.addLeftTop(user, 80, 32);
    setSignature(user);
    
    updateUser(null);

    BeeLayoutPanel mp = new BeeLayoutPanel();
    cp.addLeftTop(mp, 280, 2);
    StyleUtils.setRight(mp, 1);
    StyleUtils.setBottom(mp, 1);
    
    setMenuPanel(mp);
    
    setNotification(new Notification());
    cp.addRightTop(getNotification(), 0, 0);

    return cp;
  }

  protected Widget initSouth() {
    BeeLayoutPanel p = new BeeLayoutPanel();

    CliWidget cli = new CliWidget();
    p.addLeftRightTop(cli, 1, Unit.EM, 500, Unit.PX, 3, Unit.PX);

    Horizontal hor = new Horizontal();
    hor.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    hor.add(new BeeButton("N", new SplitCommand(Direction.NORTH)));
    hor.add(new BeeButton("S", new SplitCommand(Direction.SOUTH)));
    hor.add(new BeeButton("E", new SplitCommand(Direction.EAST)));
    hor.add(new BeeButton("W", new SplitCommand(Direction.WEST)));

    hor.add(new BeeButton("+"));

    BeeImage close = new BeeImage(Global.getImages().close(), new SplitCommand(true));
    hor.add(close);
    hor.setCellWidth(close, "32px");
    hor.setCellHorizontalAlignment(close, HasHorizontalAlignment.ALIGN_RIGHT);

    p.add(hor);
    p.setWidgetRightWidth(hor, 240, Unit.PX, 200, Unit.PX);

    BeeLabel ver = new BeeLabel(Settings.getVersion());
    p.add(ver);
    p.setWidgetRightWidth(ver, 10, Unit.PX, 200, Unit.PX);
    p.setWidgetHorizontalPosition(ver, Layout.Alignment.END);

    return p;
  }

  protected Widget initWest() {
    TabbedPages tp = new TabbedPages(22, Unit.PX);
    
    Stack fav = new Stack(Unit.PX);
    double h = 20;

    fav.add(new BeeLabel(), "Menu", h);
    fav.add(new BeeLabel(), "Records", h);
    fav.add(new BeeLabel(), "Grids", h);
    fav.add(new BeeLabel(), "Forms", h);
    fav.add(new BeeLabel(), "Filters", h);
    fav.add(new BeeLabel(), "Reports", h);
    fav.add(new BeeLabel(), "Dashboards", h);
    
    tp.add(fav, new BeeImage(Global.getImages().bookmark()));
    
    tp.add(new BeeLabel(), "Recent");

    BeeLayoutPanel dp = new BeeLayoutPanel();
    tp.add(dp, Global.constants.data(), Global.getDataExplorer().getDataInfoCreator());
    setDataPanel(dp);
    
    FlexTable fp = new FlexTable();
    fp.setCellSpacing(3);

    int r = MenuConstants.MAX_MENU_DEPTH;
    String name;

    for (int i = MenuConstants.ROOT_MENU_INDEX; i < r; i++) {
      name = MenuConstants.varMenuLayout(i);
      fp.setWidget(i, 0, new BeeListBox(Global.getVar(name)));

      name = MenuConstants.varMenuBarType(i);
      fp.setWidget(i, 1, new SimpleBoolean(Global.getVar(name)));
    }

    ValueSpinner spinner = new ValueSpinner(Global.getVar(MenuConstants.VAR_ROOT_LIMIT), 0, 30, 3);
    DomUtils.setWidth(spinner, 60);
    fp.setWidget(r, 0, spinner);

    VolumeSlider slider = new VolumeSlider(Global.getVar(MenuConstants.VAR_ITEM_LIMIT), 0, 50, 5);
    slider.setPixelSize(80, 20);
    fp.setWidget(r + 1, 0, slider);

    fp.setWidget(r, 1, new BeeButton(Global.constants.refresh(), Service.REFRESH_MENU));
    fp.setWidget(r + 1, 1, new BeeButton("BEE", CompositeService.name(MenuService.class),
        "stage_dummy"));

    BeeCheckBox toggle = new BeeCheckBox("Log");
    toggle.setValue(true);
    toggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        if (event.getValue()) {
          BeeKeeper.getLog().show();
        } else {
          BeeKeeper.getLog().hide();
        }
      }
    });
    fp.setWidget(r + 2, 0, toggle);
    
    tp.add(fp, "Options");

    Vertical adm = new Vertical();
    adm.setSpacing(5);

    adm.add(new BeeButton("DSN", CompositeService.name(DsnService.class), DsnService.SVC_GET_DSNS));
    adm.add(new BeeButton("States", CompositeService.name(StateService.class),
        StateService.SVC_GET_STATES));

    adm.add(new ButtonGroup("Ping", Service.DB_PING,
        "Info", Service.DB_INFO,
        Global.constants.tables(), Service.DB_TABLES));

    adm.add(new BeeButton(Global.constants.clazz(), Service.GET_CLASS, Stage.STAGE_GET_PARAMETERS));
    adm.add(new BeeButton("Xml", Service.GET_XML, Stage.STAGE_GET_PARAMETERS));
    adm.add(new BeeButton("Jdbc", Service.GET_DATA, Stage.STAGE_GET_PARAMETERS));

    adm.add(new BeeCheckBox(Global.getVar(Global.VAR_DEBUG)));

    adm.add(new BeeButton("North land", CompositeService.name(FormService.class),
        FormService.Stages.CHOOSE_FORM.name()));
    adm.add(new BeeButton("CRUD", CompositeService.name(RowSetService.class),
        RowSetService.Stages.CHOOSE_TABLE.name()));

    adm.add(new RadioGroup(getElGrid(), false, BeeKeeper.getStorage().checkInt(getElGrid(), 2),
        Lists.newArrayList("simple", "scroll", "cell")));
    adm.add(new RadioGroup(getElCell(), false, BeeKeeper.getStorage().checkEnum(getElCell(),
        TextCellType.TEXT_EDIT), TextCellType.values()));
    
    tp.add(adm, "Admin");
    
    return tp;
  }

  protected void setAuthButton(BeeButton authButton) {
    this.authButton = authButton;
  }

  protected void setDataPanel(HasWidgets dataPanel) {
    this.dataPanel = dataPanel;
  }

  protected void setNotification(Notification notification) {
    this.notification = notification;
  }

  protected void setScreenPanel(Split screenPanel) {
    this.screenPanel = screenPanel;
  }

  protected void setSignature(Widget signature) {
    this.signature = signature;
  }

  protected void updateAuthWidget(boolean login) {
    if (getAuthButton() == null) {
      return;
    }
    getAuthButton().setHTML(login ? Global.constants.login() : Global.constants.logout());
    getAuthButton().setService(login ? Service.GET_LOGIN : Service.LOGOUT);
    getAuthButton().setStage(login ? Stage.STAGE_GET_PARAMETERS : null);
  }
  
  private void createPanel(Direction direction) {
    TilePanel p = getActivePanel();
    Assert.notNull(p);

    int z = direction.isHorizontal() ? p.getCenterWidth() : p.getCenterHeight();
    z = Math.round((z - p.getSplitterSize()) / 2);
    if (z < getMinTileSize()) {
      Global.showError(Global.constants.no(), z);
      return;
    }

    deactivatePanel();

    TilePanel center = new TilePanel();
    Widget w = p.getCenter();
    if (w != null) {
      ScrollBars scroll = p.getWidgetScroll(w);

      setTemporaryDetach(true);
      p.remove(w);
      setTemporaryDetach(false);

      center.add(w, scroll);
      center.onLayout();
    }

    TilePanel tp = new TilePanel();
    BlankTile bt = new BlankTile();
    tp.add(bt);

    p.insert(tp, direction, z, null, null, p.getSplitterSize());
    p.add(center);

    activatePanel(tp);
  }

  private void deactivatePanel() {
    TilePanel op = getActivePanel();

    if (op != null && !isRootTile(op)) {
      Widget w = op.getCenter();

      if (w instanceof BlankTile) {
        w.removeStyleName(StyleUtils.ACTIVE_BLANK);
      } else if (w != null) {
        op.getWidgetContainerElement(w).removeClassName(StyleUtils.ACTIVE_CONTENT);
      }
    }

    setActivePanel(null);
  }

  private BeeButton getAuthButton() {
    return authButton;
  }
  
  private String getElCell() {
    return elCell;
  }

  private String getElGrid() {
    return elGrid;
  }

  private HasWidgets getMenuPanel() {
    return menuPanel;
  }

  private int getMinTileSize() {
    return minTileSize;
  }

  private TilePanel getPanel(Widget w) {
    for (Widget p = w; p != null; p = p.getParent()) {
      if (p instanceof TilePanel) {
        return (TilePanel) p;
      }
    }
    return null;
  }

  private boolean isRootTile(TilePanel p) {
    if (p == null) {
      return false;
    } else {
      return !(p.getParent() instanceof TilePanel);
    }
  }

  private void setActivePanel(TilePanel p) {
    activePanel = p;
  }

  private void setMenuPanel(HasWidgets menuPanel) {
    this.menuPanel = menuPanel;
  }

  private void setTemporaryDetach(boolean temporaryDetach) {
    this.temporaryDetach = temporaryDetach;
  }

  private void updatePanel(HasWidgets p, Widget w) {
    if (p == null) {
      notifyWarning("updatePanel: panel is null");
      return;
    }
    if (w == null) {
      notifyWarning("updatePanel: widget is null");
      return;
    }

    p.clear();
    p.add(w);
  }
}

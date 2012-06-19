package com.butent.bee.client.screen;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.Screen;
import com.butent.bee.client.Settings;
import com.butent.bee.client.cli.Shell;
import com.butent.bee.client.composite.ButtonGroup;
import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.client.composite.ResourceEditor;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.layout.BlankTile;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.layout.TilePanel;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.ui.DsnService;
import com.butent.bee.client.ui.StateService;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.ServiceCommand;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.search.SearchBox;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

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

  private HasWidgets commandPanel = null;
  private HasWidgets menuPanel = null;

  private Widget signature = null;

  private final String elGrid = "el-grid-type";

  private BeeCheckBox logToggle = null;
  private final String logVisible = "log-visible";

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
  
  public void addCommandItem(Widget widget) {
    Assert.notNull(widget);
    widget.addStyleName("bee-MainCommandPanelItem");
    getCommandPanel().add(widget);
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

  public HasWidgets getCommandPanel() {
    return commandPanel;
  }

  public String getName() {
    return NameUtils.getClassName(getClass());
  }

  public int getPriority(int p) {
    switch (p) {
      case PRIORITY_INIT:
        return DO_NOT_CALL;
      case PRIORITY_START:
        return DO_NOT_CALL;
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
        grd = Global.cellTable(data, cols);
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

  public void updateCommandPanel(Widget w) {
    updatePanel(getCommandPanel(), w);
  }

  public void updateMenu(Widget w) {
    updatePanel(getMenuPanel(), w);
  }

  public void updateSignature(String userSign) {
    if (getSignature() != null) {
      getSignature().getElement().setInnerHTML(userSign);
    }
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
    p.addStyleName("bee-Screen");

    w = initNorth();
    if (w != null) {
      p.addNorth(w, 100);
    }

    w = initSouth();
    if (w != null) {
      p.addSouth(w, 32);
    }

    w = initWest();
    if (w != null) {
      p.addWest(w, 240);
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

    if (getLogToggle() != null && !getLogToggle().getValue()) {
      BeeKeeper.getLog().hide();
    }
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
    Complex panel = new Complex();
    panel.addStyleName("bee-NorthContainer");

    BeeImage img = new BeeImage(Global.getImages().logo2());
    img.addStyleName("bee-Logo");

    String ver = Settings.getVersion();
    if (!BeeUtils.isEmpty(ver)) {
      img.setTitle(ver);
    }
    img.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        Window.open("http://www.butent.com", "", "");
      }
    });

    panel.add(img);

    Flow searchContainer = new Flow();
    searchContainer.addStyleName("bee-MainSearchContainer");

    SearchBox box = new SearchBox("Search");
    box.addStyleName("bee-MainSearchBox");
    searchContainer.add(box);
    
    Simple optionsContainer = new Simple();
    optionsContainer.addStyleName("bee-MainSearchOptionsContainer");
    BeeImage options = new BeeImage(Global.getImages().searchOptions().getSafeUri());
    options.addStyleName("bee-MainSearchOptions");
    optionsContainer.setWidget(options);
    searchContainer.add(optionsContainer);

    Simple submitContainer = new Simple();
    submitContainer.addStyleName("bee-MainSearchSubmitContainer");
    BeeImage submit = new BeeImage(Global.getImages().search().getSafeUri());
    submit.addStyleName("bee-MainSearchSubmit");
    submitContainer.setWidget(submit);
    searchContainer.add(submitContainer);

    panel.add(searchContainer);
    
    Flow commandContainer = new Flow();
    commandContainer.addStyleName("bee-MainCommandPanel");
    panel.add(commandContainer);
    setCommandPanel(commandContainer);

    Flow menuContainer = new Flow();
    menuContainer.addStyleName("bee-MainMenu");
    panel.add(menuContainer);
    setMenuPanel(menuContainer);

    Flow userContainer = new Flow();
    userContainer.addStyleName("bee-UserContainer");
    
    BeeLabel user = new BeeLabel();
    user.addStyleName("bee-UserSignature");
    userContainer.add(user);
    setSignature(user);

    Simple exitContainer = new Simple();
    exitContainer.addStyleName("bee-UserExitContainer");
    BeeImage exit = new BeeImage(Global.getImages().exit().getSafeUri(), new BeeCommand() {
      @Override
      public void execute() {
        Global.confirm(BeeKeeper.getUser().getUserSign(), "Sign out",
            new ServiceCommand(Service.LOGOUT));
      }
    });
    exit.addStyleName("bee-UserExit");
    exitContainer.setWidget(exit);
    userContainer.add(exitContainer);
    
    panel.add(userContainer);

    Notification nw = new Notification();
    nw.addStyleName("bee-MainNotificationContainer");
    panel.add(nw);
    setNotification(nw);

    return panel;
  }

  protected Widget initSouth() {
    BeeLayoutPanel p = new BeeLayoutPanel();

    Horizontal hor = new Horizontal();
    hor.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    hor.add(new BeeButton("N", new SplitCommand(Direction.NORTH)));
    hor.add(new BeeButton("S", new SplitCommand(Direction.SOUTH)));
    hor.add(new BeeButton("E", new SplitCommand(Direction.EAST)));
    hor.add(new BeeButton("W", new SplitCommand(Direction.WEST)));

    hor.add(new BeeButton("+"));

    BeeImage close = new BeeImage(Global.getImages().close(), new SplitCommand(true));
    hor.add(close);
    hor.setCellWidth(close, 32);
    hor.setCellHorizontalAlignment(close, HasHorizontalAlignment.ALIGN_RIGHT);

    p.add(hor);
    p.setWidgetRightWidth(hor, 240, Unit.PX, 200, Unit.PX);

    return p;
  }

  protected Widget initWest() {
    TabbedPages tp = new TabbedPages();

    tp.add(Global.getFavorites(), new BeeImage(Global.getImages().bookmark()));

    Vertical adm = new Vertical();
    adm.setCellSpacing(5);

    adm.add(new BeeButton("DSN", new DsnService().name(), DsnService.SVC_GET_DSNS));
    adm.add(new BeeButton("States", new StateService().name(), StateService.SVC_GET_STATES));

    adm.add(new ButtonGroup("Ping", Service.DB_PING,
        "Info", Service.DB_INFO,
        Global.CONSTANTS.tables(), Service.DB_TABLES));

    adm.add(new BeeButton(Global.CONSTANTS.clazz(), Service.GET_CLASS, Stage.STAGE_GET_PARAMETERS));
    adm.add(new BeeButton("Xml", Service.GET_XML, Stage.STAGE_GET_PARAMETERS));
    adm.add(new BeeButton("Jdbc", Service.GET_DATA, Stage.STAGE_GET_PARAMETERS));

    adm.add(new BeeCheckBox(Global.getVar(Global.VAR_DEBUG)));

    adm.add(new RadioGroup(getElGrid(), false, BeeKeeper.getStorage().checkInt(getElGrid(), 2),
        Lists.newArrayList("simple", "scroll", "cell")));

    BeeCheckBox log = new BeeCheckBox("Log");
    log.setValue(BeeKeeper.getStorage().getBoolean(getLogVisible()));

    log.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        if (event.getValue()) {
          BeeKeeper.getLog().show();
        } else {
          BeeKeeper.getLog().hide();
        }
        BeeKeeper.getStorage().setItem(getLogVisible(), event.getValue());
      }
    });
    setLogToggle(log);
    
    adm.add(log);
    
    Flow admPanel = new Flow();
    admPanel.addStyleName(StyleUtils.NAME_FLEX_BOX_VERTICAL);

    admPanel.add(adm);

    Simple shellContainer = new Simple();
    StyleUtils.makeFlexible(shellContainer);
    StyleUtils.makeRelative(shellContainer);

    Shell shell = new Shell();
    shell.addStyleName(StyleUtils.NAME_OCCUPY);

    shellContainer.setWidget(shell);
    admPanel.add(shellContainer);

    tp.add(admPanel, "Admin");

    return tp;
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

  private void createPanel(Direction direction) {
    TilePanel p = getActivePanel();
    Assert.notNull(p);

    int z = direction.isHorizontal() ? p.getCenterWidth() : p.getCenterHeight();
    z = Math.round((z - p.getSplitterSize()) / 2);
    if (z < getMinTileSize()) {
      Global.showError(Global.CONSTANTS.no(), z);
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

  private String getElGrid() {
    return elGrid;
  }

  private BeeCheckBox getLogToggle() {
    return logToggle;
  }

  private String getLogVisible() {
    return logVisible;
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

  private void setCommandPanel(HasWidgets commandPanel) {
    this.commandPanel = commandPanel;
  }

  private void setLogToggle(BeeCheckBox logToggle) {
    this.logToggle = logToggle;
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

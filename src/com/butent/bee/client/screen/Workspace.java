package com.butent.bee.client.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.Historian;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.event.PreviewHandler;
import com.butent.bee.client.event.logical.ActiveWidgetChangeEvent;
import com.butent.bee.client.event.logical.CaptionChangeEvent;
import com.butent.bee.client.event.logical.HasActiveWidgetChangeHandlers;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Span;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.screen.TilePanel.Tile;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.ExtendedPropertiesData;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;

import java.util.List;
import java.util.Map;

public class Workspace extends TabbedPages implements CaptionChangeEvent.Handler,
    HasActiveWidgetChangeHandlers, ActiveWidgetChangeEvent.Handler, PreviewHandler {

  private enum TabAction {
    CREATE(new Image(Global.getImages().silverPlus()), null, null, "Naujas skirtukas"),

    NORTH(new CustomDiv(), Direction.NORTH, STYLE_GROUP_SPLIT, "Nauja sritis viršuje"),
    SOUTH(new CustomDiv(), Direction.SOUTH, STYLE_GROUP_SPLIT, "Nauja sritis apačioje"),
    WEST(new CustomDiv(), Direction.WEST, STYLE_GROUP_SPLIT, "Nauja sritis kairėje"),
    EAST(new CustomDiv(), Direction.EAST, STYLE_GROUP_SPLIT, "Nauja sritis dešinėje"),

    MAXIMIZE(new Image(Global.getImages().arrowOut()), null, STYLE_GROUP_RESIZE, "Max dydis"),
    RESTORE(new Image(Global.getImages().arrowIn()), null, STYLE_GROUP_RESIZE, "Atstatyti dydį"),

    UP(new Image(Global.getImages().arrowUp()), Direction.NORTH, STYLE_GROUP_RESIZE,
        "Didinti aukštyn"),
    DOWN(new Image(Global.getImages().arrowDown()), Direction.SOUTH, STYLE_GROUP_RESIZE,
        "Didinti žemyn"),
    LEFT(new Image(Global.getImages().arrowLeft()), Direction.WEST, STYLE_GROUP_RESIZE,
        "Didinti į kairę"),
    RIGHT(new Image(Global.getImages().arrowRight()), Direction.EAST, STYLE_GROUP_RESIZE,
        "Didinti į dešinę"),

    CLOSE(new Image(Global.getImages().silverMinus()), null, null, "Uždaryti");

    private static final String STYLE_NAME_PREFIX = Workspace.STYLE_PREFIX + "action-";

    private final IdentifiableWidget widget;
    private final Direction direction;

    private TabAction(IdentifiableWidget widget, Direction direction, String styleGroup,
        String title) {
      this.widget = widget;
      this.direction = direction;

      this.widget.asWidget().addStyleName(STYLE_NAME_PREFIX + this.name().toLowerCase());
      if (!BeeUtils.isEmpty(styleGroup)) {
        this.widget.asWidget().addStyleName(STYLE_NAME_PREFIX + styleGroup);
      }
      this.widget.asWidget().setTitle(title);
    }

    private Direction getDirection() {
      return direction;
    }

    private IdentifiableWidget getWidget() {
      return widget;
    }
  }

  private class TabWidget extends Span implements HasCaption {

    private InlineLabel closeTab;

    private TabWidget(String caption, boolean setClose) {
      super();

      InlineLabel dropDown = new InlineLabel(String.valueOf(BeeConst.DROP_DOWN));
      dropDown.addStyleName(getStylePrefix() + "dropDown");
      dropDown.setTitle("Skirtuko valdymas");
      add(dropDown);

      dropDown.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          event.stopPropagation();
          Workspace.this.showActions(TabWidget.this.getId());
        }
      });

      InlineLabel label = new InlineLabel(caption);
      label.addStyleName(getStylePrefix() + "caption");
      add(label);

      closeTab = new InlineLabel();
      closeTab.addStyleName(getStylePrefix() + "closeTab");
      closeTab.setTitle("Uždaryti skirtuką");
      closeTab.setVisible(setClose);
      add(closeTab);

      closeTab.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          doAction(TabAction.CLOSE, getTabIndex(TabWidget.this.getId()));
        }
      });
    }

    private TabWidget(String caption) {
      this(caption, true);
    }

    @Override
    public String getCaption() {
      return getCaptionWidget().getText();
    }

    public void setClose(boolean close) {
      closeTab.setVisible(close);
    }

    private InlineLabel getCaptionWidget() {
      return (InlineLabel) getWidget(1);
    }

    private void setCaption(String caption) {
      getCaptionWidget().setText(caption);
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(Workspace.class);

  private static final String STYLE_PREFIX = "bee-Workspace-";

  private static final String STYLE_GROUP_SPLIT = "split";
  private static final String STYLE_GROUP_RESIZE = "resize";

  private final Map<Direction, Integer> resized = Maps.newHashMap();

  Workspace() {
    super(STYLE_PREFIX);
    insertEmptyPanel(0);
  }

  @Override
  public HandlerRegistration addActiveWidgetChangeHandler(ActiveWidgetChangeEvent.Handler handler) {
    return addHandler(handler, ActiveWidgetChangeEvent.getType());
  }

  @Override
  public boolean isModal() {
    return false;
  }

  @Override
  public void onActiveWidgetChange(ActiveWidgetChangeEvent event) {
    fireEvent(event);
  }

  @Override
  public void onCaptionChange(CaptionChangeEvent event) {
    if (event.getSource() instanceof Tile) {
      updateCaption((Tile) event.getSource(), event.getCaption());
    }
  }

  @Override
  public void onEventPreview(NativePreviewEvent event) {
    EventTarget target = event.getNativeEvent().getEventTarget();
    if (target == null) {
      return;
    }

    Node node = Node.as(target);

    TilePanel activePanel = getActivePanel();
    if (activePanel == null || activePanel.getWidgetCount() <= 1
        || !activePanel.getElement().isOrHasChild(node)) {
      return;
    }

    Tile activeTile = activePanel.getActiveTile();
    if (activeTile == null || activeTile.getElement().isOrHasChild(node)) {
      return;
    }

    Tile eventTile = activePanel.getEventTile(node);
    if (eventTile == null || eventTile.getId().equals(activeTile.getId())) {
      return;
    }

    eventTile.activate(true);
  }

  public boolean randomClose(boolean debug) {
    TilePanel panel = getActivePanel();
    int tileCount = panel.getTileCount();
    if (tileCount <= 1) {
      return false;
    }

    List<ExtendedProperty> info = debug ? panel.getExtendedInfo() : null;

    int tileIndex = BeeUtils.randomInt(0, tileCount);

    int index = 0;
    for (Widget child : panel) {
      if (child instanceof Tile) {
        if (index == tileIndex) {
          if (debug) {
            info.add(new ExtendedProperty("close", child.getElement().getId(),
                BeeUtils.joinWords(panel.getWidgetDirection(child), panel.getWidgetSize(child))));
          }

          close((Tile) child);
          break;
        }
        index++;
      }
    }

    if (!debug) {
      return true;
    }

    boolean ok = true;
    int minSize = TilePanel.MIN_SIZE - TilePanel.TILE_MARGIN * 2;

    for (Widget child : panel) {
      if (child instanceof Tile) {
        if (child.getOffsetWidth() < minSize || child.getOffsetHeight() < minSize) {
          info.add(new ExtendedProperty("small", child.getElement().getId(),
              "W " + child.getOffsetWidth() + " H " + child.getOffsetHeight()));
          ok = false;
          break;
        }
      }
    }

    if (!ok) {
      Global.showModalGrid("close error", new ExtendedPropertiesData(info, false));
    }
    return ok;
  }

  public void randomSplit() {
    TilePanel panel = getActivePanel();
    int tileIndex = BeeUtils.randomInt(0, panel.getTileCount());

    int count = 0;
    for (Widget child : panel) {
      if (child instanceof Tile) {
        if (count >= tileIndex) {
          Tile tile = (Tile) child;

          Direction direction;
          int d = BeeUtils.randomInt(0, 4);
          switch (d) {
            case 0:
              direction = Direction.NORTH;
              break;
            case 1:
              direction = Direction.SOUTH;
              break;
            case 2:
              direction = Direction.WEST;
              break;
            case 3:
              direction = Direction.EAST;
              break;
            default:
              Assert.untouchable();
              direction = null;
          }

          int size = direction.isHorizontal() ? tile.getOffsetWidth() : tile.getOffsetHeight();
          size = (size + TilePanel.TILE_MARGIN * 2 - panel.getSplitterSize()) / 2;
          if (size < TilePanel.MIN_SIZE) {
            continue;
          }

          if (!tile.getId().equals(panel.getActiveTileId())) {
            tile.activate(false);
          }
          panel.addTile(this, direction);
          break;
        }

        count++;
      }
    }
  }

  @Override
  public void removePage(int index) {
    Widget widget = getContentWidget(index);
    if (widget instanceof TilePanel) {
      ((TilePanel) widget).onRemove();
    }

    super.removePage(index);

    if (getPageCount() == 1) {
      setStyleOne(true);
      TabWidget tab = (TabWidget) getTabWidget(0);
      tab.setClose(false);
    }
  }

  @Override
  public void selectPage(int index, SelectionOrigin origin) {
    super.selectPage(index, origin);

    if (SelectionOrigin.CLICK.equals(origin) || SelectionOrigin.INSERT.equals(origin)
        || SelectionOrigin.REMOVE.equals(origin)) {
      Tile tile = getActiveTile();

      Historian.goTo(tile.getId());
      tile.activateContent();
    }
  }

  void activateWidget(IdentifiableWidget widget) {
    if (widget == null) {
      return;
    }

    IdentifiableWidget activeContent = getActiveContent();
    if (activeContent != null && widget.getId().equals(activeContent.getId())) {
      return;
    }

    Tile tile = TilePanel.getTile(widget.asWidget());

    int index = getPageIndex(tile);
    if (index != getSelectedIndex()) {
      selectPage(index, SelectionOrigin.SCRIPT);
    }

    tile.activate(false);
  }

  void closeWidget(IdentifiableWidget widget) {
    Tile tile = TilePanel.getTile(widget.asWidget());
    close(tile);
  }

  IdentifiableWidget getActiveContent() {
    return getActiveTile().getContent();
  }

  Tile getActiveTile() {
    TilePanel panel = getActivePanel();
    if (panel == null) {
      return null;
    }

    return panel.getActiveTile();
  }

  int getPageIndex(Tile tile) {
    for (int i = 0; i < getPageCount(); i++) {
      if (getContentWidget(i).getElement().isOrHasChild(tile.getElement())) {
        return i;
      }
    }
    showError("page not found for tile" + tile.getId());
    return BeeConst.UNDEF;
  }

  void openInNewPlace(IdentifiableWidget widget) {
    Tile tile = getActiveTile();
    if (tile == null || !tile.isBlank()) {
      int index = getSelectedIndex();
      if (index < 0) {
        index = getPageCount() - 1;
      }
      insertEmptyPanel(index + 1);
    }

    updateActivePanel(widget);
  }

  void showInfo() {
    TilePanel panel = getActivePanel();
    if (panel == null) {
      return;
    }

    List<ExtendedProperty> info = panel.getExtendedInfo();
    Global.showModalGrid("Tiles", new ExtendedPropertiesData(info, false));
  }

  void updateActivePanel(IdentifiableWidget widget) {
    if (widget == null) {
      showError("widget is null");
      return;
    }

    Tile tile = getActiveTile();
    if (tile != null) {
      tile.updateContent(widget, true);
    }
  }

  private boolean canGrow(Direction direction) {
    return BeeUtils.isPositive(getSiblingSize(direction));
  }

  private void close(Tile tile) {
    TilePanel panel = tile.getPanel();
    int pageIndex = getPageIndex(tile);

    if (panel.getTileCount() > 1) {
      boolean wasActive = tile.getId().equals(panel.getActiveTileId());
      int tileIndex = panel.getWidgetIndex(tile);

      if (!tile.isBlank()) {
        tile.blank();
      }

      panel.remove(tile);
      resizePage(pageIndex);

      if (wasActive) {
        Tile nearestTile = panel.getNearestTile(tileIndex);
        panel.setActiveTileId(nearestTile.getId());
        nearestTile.activate(pageIndex == getSelectedIndex());
      }

    } else if (getPageCount() > 1) {
      removePage(pageIndex);

    } else if (!tile.isBlank()) {
      tile.blank();
    }
  }

  private void doAction(TabAction action, int index) {
    switch (action) {
      case CLOSE:
        if (index == getSelectedIndex()) {
          close(getActiveTile());
        } else {
          removePage(index);
        }
        break;

      case CREATE:
        insertEmptyPanel(index + 1);
        break;

      case EAST:
      case NORTH:
      case SOUTH:
      case WEST:
        splitActiveTile(action.getDirection());
        break;

      case MAXIMIZE:
        for (Direction direction : Direction.values()) {
          if (!Direction.CENTER.equals(direction)) {
            stretch(direction, false);
          }
        }
        getResizeContainer().doLayout();
        break;

      case RESTORE:
        for (Map.Entry<Direction, Integer> entry : resized.entrySet()) {
          getResizeContainer().setDirectionSize(entry.getKey(), entry.getValue(), false);
        }
        resized.clear();
        getResizeContainer().doLayout();
        break;

      case UP:
      case DOWN:
      case LEFT:
      case RIGHT:
        stretch(action.getDirection(), true);
        break;
    }
  }

  private TilePanel getActivePanel() {
    Widget widget = getSelectedWidget();
    if (widget instanceof TilePanel) {
      return (TilePanel) widget;
    } else {
      showError("selected panel not available");
      return null;
    }
  }

  private Split getResizeContainer() {
    for (Widget parent = this.getParent(); parent != null; parent = parent.getParent()) {
      if (parent instanceof Split) {
        return (Split) parent;
      }
    }
    return null;
  }

  private Integer getSiblingSize(Direction direction) {
    Split container = getResizeContainer();
    if (container == null) {
      return null;
    }
    return container.getDirectionSize(direction);
  }

  private int getTabIndex(String tabId) {
    for (int i = 0; i < getPageCount(); i++) {
      if (tabId.equals(getTabWidget(i).getElement().getId())) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  private void insertEmptyPanel(int before) {
    TilePanel panel = new TilePanel(this);
    TabWidget tab = new TabWidget(Localized.constants.newTab());

    if (getPageCount() == 1) {
      setStyleOne(false);
      tab.setClose(false);
    }

    insert(panel, tab, before);
    tab.setClose(isActionEnabled(TabAction.CLOSE, getTabIndex(tab.getId())));

    if (getPageCount() == 1) {
      setStyleOne(true);
      tab.setClose(false);
    }

    selectPage(before, SelectionOrigin.INSERT);
  }

  private boolean isActionEnabled(TabAction action, int index) {
    boolean enabled = false;

    switch (action) {
      case CREATE:
        enabled = true;
        break;

      case CLOSE:
        if (getPageCount() > 1) {
          enabled = true;
        } else {
          TilePanel panel = getActivePanel();
          enabled = panel.getTileCount() > 1 || !panel.getActiveTile().isBlank();
        }
        break;

      case EAST:
      case NORTH:
      case SOUTH:
      case WEST:
        enabled = (index == getSelectedIndex());
        break;

      case MAXIMIZE:
        enabled = (index == getSelectedIndex() && (canGrow(Direction.NORTH)
            || canGrow(Direction.SOUTH) || canGrow(Direction.WEST) || canGrow(Direction.EAST)));
        break;

      case RESTORE:
        enabled = (index == getSelectedIndex() && !resized.isEmpty());
        break;

      case UP:
      case DOWN:
      case LEFT:
      case RIGHT:
        enabled = (index == getSelectedIndex() && !resized.containsKey(action.getDirection())
            && canGrow(action.getDirection()));
        break;
    }

    return enabled;
  }

  private void setStyleOne(boolean add) {
    setTabStyle(0, getStylePrefix() + "one", add);
  }

  private void showActions(String tabId) {
    final int index = getTabIndex(tabId);
    if (BeeConst.isUndef(index)) {
      showError("tab widget not found, id: " + tabId);
      return;
    }

    final List<TabAction> actions = Lists.newArrayList();
    for (TabAction action : TabAction.values()) {
      if (isActionEnabled(action, index)) {
        actions.add(action);
      }
    }

    TabBar bar = new TabBar(STYLE_PREFIX + "actionMenu-", Orientation.HORIZONTAL);
    for (TabAction action : actions) {
      bar.addItem(action.getWidget().asWidget());
    }

    final Popup popup = new Popup(OutsideClick.CLOSE, STYLE_PREFIX + "actionPopup");

    bar.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        popup.close();
        doAction(actions.get(event.getSelectedItem()), index);
      }
    });

    popup.setWidget(bar);
    popup.showRelativeTo(getTabWidget(index).getElement());
  }

  private void showError(String message) {
    logger.severe(getClass().getName(), message);
  }

  private void splitActiveTile(Direction direction) {
    TilePanel panel = getActivePanel();
    if (panel != null) {
      panel.addTile(this, direction);
    }
  }

  private void stretch(Direction direction, boolean doLayout) {
    Integer size = getSiblingSize(direction);
    if (BeeUtils.isPositive(size)) {
      getResizeContainer().setDirectionSize(direction, 0, doLayout);
      resized.put(direction, size);
    }
  }

  private void updateCaption(Tile tile, String tileCaption) {
    String caption = tileCaption;
    if (BeeUtils.isEmpty(caption)) {
      caption = tile.getPanel().getCaption();
    }

    if (BeeUtils.isEmpty(caption)) {
      caption = Localized.constants.newTab();
    }

    int index = getPageIndex(tile);

    TabWidget tab = (TabWidget) getTabWidget(index);
    if (caption.equals(tab.getCaption())) {
      return;
    }

    boolean checkSize = isAttached() && getPageCount() > 1;
    if (checkSize) {
      saveLayout();
    }

    tab.setCaption(caption);

    tab.setClose(isActionEnabled(TabAction.CLOSE, getTabIndex(tab.getId())));

    if (checkSize) {
      checkLayout();
    }
  }
}

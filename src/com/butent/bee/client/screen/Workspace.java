package com.butent.bee.client.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.Historian;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Span;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

class Workspace extends TabbedPages implements SelectionHandler<TilePanel> {

  private enum TabAction {
    CREATE(new BeeImage(Global.getImages().add()), null, null, "Naujas skirtukas"),

    NORTH(new CustomDiv(), Direction.NORTH, STYLE_GROUP_SPLIT, "Nauja sritis viršuje"),
    SOUTH(new CustomDiv(), Direction.SOUTH, STYLE_GROUP_SPLIT, "Nauja sritis apačioje"),
    WEST(new CustomDiv(), Direction.WEST, STYLE_GROUP_SPLIT, "Nauja sritis kairėje"),
    EAST(new CustomDiv(), Direction.EAST, STYLE_GROUP_SPLIT, "Nauja sritis dešinėje"),

    MAXIMIZE(new BeeImage(Global.getImages().arrowOut()), null, STYLE_GROUP_RESIZE, "Max dydis"),
    RESTORE(new BeeImage(Global.getImages().arrowIn()), null, STYLE_GROUP_RESIZE, "Atstatyti dydį"),

    UP(new BeeImage(Global.getImages().arrowUp()), Direction.NORTH, STYLE_GROUP_RESIZE,
        "Didinti aukštyn"),
    DOWN(new BeeImage(Global.getImages().arrowDown()), Direction.SOUTH, STYLE_GROUP_RESIZE,
        "Didinti žemyn"),
    LEFT(new BeeImage(Global.getImages().arrowLeft()), Direction.WEST, STYLE_GROUP_RESIZE,
        "Didinti į kairę"),
    RIGHT(new BeeImage(Global.getImages().arrowRight()), Direction.EAST, STYLE_GROUP_RESIZE,
        "Didinti į dešinę"),

    CLOSE(new BeeImage(Global.getImages().noes()), null, null, "Uždaryti");

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

    private TabWidget(String caption) {
      super();
      InlineLabel label = new InlineLabel(caption);
      label.addStyleName(getStylePrefix() + "caption");
      add(label);

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
    }

    @Override
    public String getCaption() {
      return getCaptionWidget().getText();
    }

    private InlineLabel getCaptionWidget() {
      return (InlineLabel) getWidget(0);
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
  public void onSelection(SelectionEvent<TilePanel> event) {
    updateCaption(event.getSelectedItem());
  }

  @Override
  public void removePage(int index) {
    Widget pageContent = getContentWidget(index);
    if (pageContent instanceof TilePanel) {
      ((TilePanel) pageContent).clear();
    }
    
    super.removePage(index);

    if (getPageCount() == 1) {
      setStyleOne(true);
    }
  }

  @Override
  public void selectPage(int index, SelectionOrigin origin) {
    super.selectPage(index, origin);

    if (SelectionOrigin.CLICK.equals(origin)) {
      TilePanel activePanel = getActivePanel();
      if (activePanel != null) {
        Historian.goTo(activePanel.getId());
        activePanel.activateContent();
      }

    } else if (SelectionOrigin.REMOVE.equals(origin)) {
      TilePanel activePanel = getActivePanel();
      if (activePanel != null) {
        updateCaption(activePanel);
        activePanel.activateContent();
      }
    }
  }

  void activateWidget(IdentifiableWidget widget) {
    TilePanel tile = TilePanel.getParentTile(widget.asWidget());
    if (tile == null) {
      showError("activateWidget: panel not found for " + widget.getId());
      return;
    }
    
    int index = getPageIndex(tile);
    if (BeeConst.isUndef(index)) {
      showError("activateWidget: page not found for " + widget.getId());
      return;
    }
    
    if (index != getSelectedIndex()) {
      selectPage(index, SelectionOrigin.SCRIPT);
    }
    tile.activate(false);
  }

  void closeWidget(IdentifiableWidget widget) {
    TilePanel tile = TilePanel.getParentTile(widget.asWidget());

    if (tile == null) {
      showError("closeWidget: panel not found");
      return;
    }

    tile = tile.close();
    while (tile != null && tile.isBlank() && !tile.isRoot()) {
      tile = tile.close();
    }

    if (getPageCount() > 1 && tile.isBlank() && tile.isRoot()) {
      int index = getContentIndex(tile);
      if (index >= 0) {
        removePage(index);
      }
    }
  }

  IdentifiableWidget getActiveContent() {
    return getActivePanel().getContent();
  }

  TilePanel getActivePanel() {
    Widget widget = getSelectedWidget();
    if (!(widget instanceof TilePanel)) {
      showError("selected panel not available");
      return null;
    }

    TilePanel tile = ((TilePanel) widget).getActiveTile();
    if (tile == null) {
      showError("active panel not available");
    }
    return tile;
  }

  void openInNewPlace(IdentifiableWidget widget) {
    TilePanel tile = getActivePanel();
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
    Widget tiles = getSelectedWidget();
    if (!(tiles instanceof TilePanel)) {
      showError("tiles not available");
      return;
    }

    TreeItem item = ((TilePanel) tiles).getTree(null);
    item.setOpenRecursive(true, false);

    Tree tree = new Tree();
    tree.addItem(item);

    Global.showModalWidget(tree);
  }

  void updateActivePanel(IdentifiableWidget widget) {
    if (widget == null) {
      showError("widget is null");
      return;
    }

    TilePanel tile = getActivePanel();
    if (tile != null) {
      tile.updateContent(widget, true);
    }
  }

  private boolean canGrow(Direction direction) {
    return BeeUtils.isPositive(getSiblingSize(direction));
  }

  private void closeActivePanel() {
    TilePanel tile = getActivePanel();
    if (tile != null) {
      tile.close();
    }
  }

  private void doAction(TabAction action, int index) {
    switch (action) {
      case CLOSE:
        if (index == getSelectedIndex() && getActivePanel().closeable()) {
          closeActivePanel();
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
        splitActivePanel(action.getDirection());
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

  private int getPageIndex(TilePanel tile) {
    for (int i = 0; i < getPageCount(); i++) {
      if (getContentWidget(i).getElement().isOrHasChild(tile.getElement())) {
        return i;
      }
    }
    return BeeConst.UNDEF;
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
    TilePanel tile = TilePanel.newBlankTile(this);
    TabWidget tab = new TabWidget(Global.CONSTANTS.newTab());

    if (getPageCount() == 1) {
      setStyleOne(false);
    }

    insert(tile, tab, before);

    if (getPageCount() == 1) {
      setStyleOne(true);
    }

    selectPage(before, SelectionOrigin.INSERT);
    tile.activate(true);
  }

  private boolean isActionEnabled(TabAction action, int index) {
    boolean enabled = false;

    switch (action) {
      case CREATE:
        enabled = true;
        break;

      case CLOSE:
        enabled = getPageCount() > 1 || index != getSelectedIndex() || getActivePanel().closeable();
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

    final Popup popup = new Popup(true, true, STYLE_PREFIX + "actionPopup");

    bar.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        popup.hide();
        doAction(actions.get(event.getSelectedItem()), index);
      }
    });

    popup.setWidget(bar);
    popup.showRelativeTo(getTabWidget(index));
  }

  private void showError(String message) {
    logger.severe(getClass().getName(), message);
  }

  private void splitActivePanel(Direction direction) {
    TilePanel p = getActivePanel();
    if (p != null) {
      p.addTile(this, direction);
    }
  }

  private void stretch(Direction direction, boolean doLayout) {
    Integer size = getSiblingSize(direction);
    if (BeeUtils.isPositive(size)) {
      getResizeContainer().setDirectionSize(direction, 0, doLayout);
      resized.put(direction, size);
    }
  }

  private void updateCaption(TilePanel tile) {
    String caption = null;
    for (Widget p = tile; p instanceof TilePanel && BeeUtils.isEmpty(caption); p = p.getParent()) {
      caption = ((TilePanel) p).getCaption();
    }
    if (BeeUtils.isEmpty(caption)) {
      caption = Global.CONSTANTS.newTab();
    }
    
    int index = getPageIndex(tile);
    if (BeeConst.isUndef(index)) {
      return;
    }

    TabWidget tab = (TabWidget) getTabWidget(index);
    if (caption.equals(tab.getCaption())) {
      return;
    }

    boolean checkSize = isAttached() && getPageCount() > 1;
    if (checkSize) {
      saveLayout();
    }

    tab.setCaption(caption);

    if (checkSize) {
      checkLayout();
    }
  }
}

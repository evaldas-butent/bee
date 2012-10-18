package com.butent.bee.client.screen;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Span;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class Workspace extends TabbedPages implements SelectionHandler<TilePanel> {

  private enum TabAction {
    CREATE(new BeeImage(Global.getImages().add()), "create", false, "Naujas skirtukas"),
    NORTH(new CustomDiv(), "north", true, "Nauja sritis viršuje"),
    SOUTH(new CustomDiv(), "south", true, "Nauja sritis apačioje"),
    WEST(new CustomDiv(), "west", true, "Nauja sritis kairėje"),
    EAST(new CustomDiv(), "east", true, "Nauja sritis dešinėje"),
    CLOSE(new BeeImage(Global.getImages().noes()), "close", false, "Uždaryti");

    private final Widget widget;

    private TabAction(Widget widget, String style, boolean split, String title) {
      this.widget = widget;

      this.widget.addStyleName(Workspace.STYLE_PREFIX + "action-" + style);
      if (split) {
        this.widget.addStyleName(Workspace.STYLE_PREFIX + "action-split");
      }
      this.widget.setTitle(title);
    }

    private Widget getWidget() {
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
    super.removePage(index);
    if (getPageCount() == 1) {
      setStyleOne(true);
    }
  }

  void closeWidget(Widget widget) {
    TilePanel tile = TilePanel.getParentTile(widget);
    if (tile == null) {
      showError("closeWidget: panel not found");
    } else {
      tile.close();
    }
  }

  Widget getActiveContent() {
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

  void updateActivePanel(Widget widget, ScrollBars scroll) {
    if (widget == null) {
      showError("widget is null");
      return;
    }

    TilePanel tile = getActivePanel();
    if (tile != null) {
      tile.updateContent(widget, scroll);
    }
  }

  private void closeActivePanel() {
    TilePanel op = getActivePanel();
    if (op != null) {
      op.close();
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
        splitActivePanel(Direction.EAST);
        break;
      case NORTH:
        splitActivePanel(Direction.NORTH);
        break;
      case SOUTH:
        splitActivePanel(Direction.SOUTH);
        break;
      case WEST:
        splitActivePanel(Direction.WEST);
        break;
    }
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

    selectPage(before);
    tile.activate();
  }

  private boolean isActionEnabled(TabAction action, int index) {
    boolean enabled;

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

      default:
        Assert.untouchable();
        enabled = false;
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
      bar.addItem(action.getWidget());
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
      p.addTile(direction, this);
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
    
    TabWidget tab = (TabWidget) getTabWidget(getSelectedIndex());
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

package com.butent.bee.client.view;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.tree.HasTreeItems;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.CatchEvent.CatchHandler;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;

public class TreeContainer extends Flow implements TreeView, SelectionHandler<TreeItem>,
    CatchEvent.CatchHandler<TreeItem> {

  private class ActionListener extends BeeCommand {
    private final Action action;

    private ActionListener(Action action) {
      super();
      this.action = action;
    }

    @Override
    public void execute() {
      if (getViewPresenter() != null) {
        getViewPresenter().handleAction(action);
      }
    }
  }

  private static final String STYLE_NAME = "bee-TreeView";

  private Presenter viewPresenter = null;
  private boolean enabled = true;
  private final Tree tree;
  private final TreeItem rootItem;
  private final Map<Long, TreeItem> items = Maps.newHashMap();
  private final Panel noData = new SimplePanel();
  private final boolean hasActions;

  public TreeContainer(boolean hideActions, String root) {
    super();

    addStyleName(STYLE_NAME);
    this.hasActions = !hideActions;

    if (hasActions) {
      Flow hdr = new Flow();
      hdr.addStyleName(STYLE_NAME + "-actions");

      BeeImage img = new BeeImage(Global.getImages().editAdd(), new ActionListener(Action.ADD));
      img.addStyleName(STYLE_NAME + "-add");
      hdr.add(img);
      img = new BeeImage(Global.getImages().editDelete(), new ActionListener(Action.DELETE));
      img.addStyleName(STYLE_NAME + "-delete");
      hdr.add(img);
      img = new BeeImage(Global.getImages().edit(), new ActionListener(Action.EDIT));
      img.addStyleName(STYLE_NAME + "-edit");
      hdr.add(img);
      img = new BeeImage(Global.getImages().reload(), new ActionListener(Action.REQUERY));
      img.addStyleName(STYLE_NAME + "-requery");
      hdr.add(img);

      add(hdr);
    }
    noData.setStyleName(CellGrid.STYLE_EMPTY);
    add(noData);

    this.tree = new Tree();
    add(tree);

    getTree().addStyleName(STYLE_NAME + "-tree");
    getTree().addSelectionHandler(this);

    if (hasActions) {
      getTree().addCatchHandler(this);
    }
    if (!BeeUtils.isEmpty(root)) {
      this.rootItem = new TreeItem(root);
      this.rootItem.getContentElem().addClassName(STYLE_NAME + "-rootItem");
      getTree().addItem(this.rootItem);
    } else {
      this.rootItem = null;
    }
    showNoData();
  }

  @Override
  public HandlerRegistration addCatchHandler(CatchHandler<IsRow> handler) {
    return addHandler(handler, CatchEvent.getType());
  }

  @Override
  public void addItem(Long parentId, String text, IsRow item, boolean focus) {
    Assert.notNull(item);
    long id = item.getId();
    Assert.state(!items.containsKey(id), "Item already exists in a tree: " + id);

    TreeItem treeItem = new TreeItem(text, item);

    if (hasActions) {
      treeItem.makeDraggable();
    }
    items.put(id, treeItem);

    HasTreeItems parentItem = items.containsKey(parentId) ? items.get(parentId) : getRoot();
    parentItem.addItem(treeItem);

    if (focus) {
      getTree().setSelectedItem(treeItem);
      getTree().ensureSelectedItemVisible();

    } else if (rootItem != null && !rootItem.isOpen()) {
      rootItem.setOpen(true);
    }
    showNoData();
  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<IsRow> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  @Override
  public Collection<IsRow> getChildItems(IsRow item, boolean recurse) {
    Assert.notNull(item);
    Assert.contains(items, item.getId());

    TreeItem treeItem = items.get(item.getId());
    Collection<IsRow> childs = Lists.newArrayList();

    if (treeItem.getChildCount() > 0) {
      for (int i = 0; i < treeItem.getChildCount(); i++) {
        TreeItem childTree = treeItem.getChild(i);
        IsRow child = (IsRow) childTree.getUserObject();
        childs.add(child);

        if (recurse) {
          childs.addAll(getChildItems(child, recurse));
        }
      }
    }
    return childs;
  }

  @Override
  public IsRow getParentItem(IsRow item) {
    Assert.notNull(item);
    Assert.contains(items, item.getId());

    TreeItem treeItem = items.get(item.getId());

    if (treeItem.getParentItem() != null) {
      return (IsRow) treeItem.getParentItem().getUserObject();
    }
    return null;
  }

  @Override
  public IsRow getSelectedItem() {
    TreeItem selected = getTree().getSelectedItem();
    if (selected == null || !(selected.getUserObject() instanceof IsRow)) {
      return null;
    }
    return (IsRow) selected.getUserObject();
  }

  @Override
  public TreePresenter getTreePresenter() {
    if (viewPresenter instanceof TreePresenter) {
      return (TreePresenter) viewPresenter;
    }
    return null;
  }

  @Override
  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void onCatch(CatchEvent<TreeItem> event) {
    TreeItem destination = event.getDestination();

    CatchEvent.fire(this, (IsRow) event.getPacket().getUserObject(),
        destination == null ? null : (IsRow) destination.getUserObject());
  }

  @Override
  public void onSelection(SelectionEvent<TreeItem> event) {
    IsRow item = null;

    if (event == null) {
      getTree().setSelectedItem(null);
    } else {
      item = (IsRow) event.getSelectedItem().getUserObject();
    }
    SelectionEvent.fire(this, item);
  }

  @Override
  public void refresh(IsRow parentRow, Boolean parentEnabled) {
    Long id = null;

    if (parentRow != null) {
      id = parentRow.getId();
    }
    if (getTreePresenter() != null) {
      getTreePresenter().updateRelation(id);
    }
    setEnabled(parentEnabled);
  }

  @Override
  public void removeItem(IsRow item) {
    Assert.notNull(item);
    Assert.contains(items, item.getId());

    TreeItem treeItem = items.get(item.getId());

    if (treeItem.isSelected()) {
      onSelection(null);
    }
    removeFromCache(treeItem);
    treeItem.remove();
    showNoData();
  }

  @Override
  public void removeItems() {
    getRoot().removeItems();
    items.clear();

    onSelection(null);
    showNoData();
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled == isEnabled()) {
      return;
    }
    this.enabled = enabled;
    DomUtils.enableChildren(this, enabled);
  }

  @Override
  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
  }

  @Override
  public void updateItem(String text, IsRow item) {
    Assert.notNull(item);
    Assert.contains(items, item.getId());

    TreeItem treeItem = items.get(item.getId());

    treeItem.setHtml(text);
    treeItem.setUserObject(item);

    if (treeItem.isSelected()) {
      getTree().setSelectedItem(treeItem);
    }
  }

  private HasTreeItems getRoot() {
    return rootItem == null ? getTree() : rootItem;
  }

  private Tree getTree() {
    return tree;
  }

  private void removeFromCache(TreeItem item) {
    for (int i = 0; i < item.getChildCount(); i++) {
      removeFromCache(item.getChild(i));
    }
    items.remove(((IsRow) item.getUserObject()).getId());
  }

  private void showNoData() {
    noData.setVisible(BeeUtils.isEmpty(getTree().getItemCount()));
  }
}

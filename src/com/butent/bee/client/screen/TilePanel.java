package com.butent.bee.client.screen;

import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.Historian;
import com.butent.bee.client.Place;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.ui.HandlesHistory;
import com.butent.bee.client.ui.HasWidgetSupplier;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.WidgetFactory;
import com.butent.bee.client.view.View;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

class TilePanel extends Split implements HasSelectionHandlers<TilePanel>, HasCaption,
    HandlesHistory {

  private static class BlankTile extends CustomDiv {
    private BlankTile() {
      super("bee-BlankTile");
    }

    @Override
    public String getIdPrefix() {
      return "blank";
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(TilePanel.class);

  private static final int MIN_SIZE = 20;

  private static final String ACTIVE_BLANK = "bee-activeBlank";
  private static final String ACTIVE_CONTENT = "bee-activeContent";

  static TilePanel getParentTile(Widget widget) {
    for (Widget parent = widget; parent != null; parent = parent.getParent()) {
      if (parent instanceof TilePanel) {
        return (TilePanel) parent;
      }
    }
    return null;
  }

  static TilePanel newBlankTile(Workspace workspace) {
    return newTile(workspace, new BlankTile());
  }

  static TilePanel newTile(Workspace workspace, IdentifiableWidget widget) {
    TilePanel tile = new TilePanel();
    tile.add(widget);

    tile.addSelectionHandler(workspace);
    Historian.add(new Workplace(workspace, tile.getId()));
    
    return tile;
  }

  private static TilePanel locateActiveChild(TilePanel parent) {
    if (parent.isActive()) {
      return parent;
    }

    for (Widget child : parent.getChildren()) {
      if (child instanceof TilePanel) {
        TilePanel tile = locateActiveChild((TilePanel) child);
        if (tile != null) {
          return tile;
        }
      }
    }
    return null;
  }

  private boolean active = false;

  private final List<String> contentSuppliers = Lists.newArrayList();
  private int contentIndex = BeeConst.UNDEF;

  private TilePanel() {
    super(5);

    addStyleName("bee-tile");
    sinkEvents(Event.ONMOUSEDOWN);
  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<TilePanel> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  @Override
  public void clear() {
    if (!Global.isTemporaryDetach() && getContent() instanceof HandlesStateChange) {
      ((HandlesStateChange) getContent()).onStateChange(State.REMOVED);
    }
    
    for (Widget child : getChildren()) {
      if (child instanceof TilePanel) {
        ((TilePanel) child).clear();
      }
    }
    
    super.clear();
  }

  @Override
  public String getCaption() {
    String caption = null;

    for (Widget child : getChildren()) {
      if (child instanceof HasCaption) {
        caption = ((HasCaption) child).getCaption();

      } else if (child instanceof View) {
        Presenter presenter = ((View) child).getViewPresenter();
        if (presenter != null) {
          caption = presenter.getCaption();
        }
      }

      if (!BeeUtils.isEmpty(caption)) {
        break;
      }
    }
    return caption;
  }

  @Override
  public String getIdPrefix() {
    return "tile";
  }

  @Override
  public void onBrowserEvent(Event ev) {
    if (!isActive() && EventUtils.isMouseDown(ev.getType()) && isLeaf()) {
      ev.stopPropagation();
      activate(true);
    }
    super.onBrowserEvent(ev);
  }

  @Override
  public boolean onHistory(final Place place, boolean forward) {
    if (contentSuppliers.isEmpty()) {
      return false;
    }
    
    int oldIndex = getContentIndex();
    int size = contentSuppliers.size();
    
    if (Historian.getSize() > 1) {
      if (forward) {
        if (oldIndex == size - 1) {
          return false;
        }
      } else {
        if (oldIndex == 0) {
          return false;
        }
      }
    }
    
    final int newIndex;
    if (forward) {
      newIndex = BeeUtils.rotateForwardExclusive(oldIndex, 0, size);
    } else {
      newIndex = BeeUtils.rotateBackwardExclusive(oldIndex, 0, size);
    }
    
    if (newIndex == oldIndex) {
      return false;
    }
    
    WidgetFactory.create(contentSuppliers.get(newIndex), new Callback<IdentifiableWidget>() {
      @Override
      public void onFailure(String... reason) {
        contentSuppliers.remove(newIndex);
        if (getContentIndex() >= newIndex) {
          setContentIndex(getContentIndex() - 1);
        }
      }

      @Override
      public void onSuccess(IdentifiableWidget result) {
        setContentIndex(newIndex);
        updateContent(result, false);
      }
    });

    return true;
  }

  @Override
  protected void onUnload() {
    super.onUnload();
    
    if (!Global.isTemporaryDetach()) {
      Historian.remove(getId());
    }
  }

  void activate(boolean updateHistory) {
    if (isActive()) {
      return;
    }

    deactivate();

    setActiveStyle(true);
    setActive(true);

    SelectionEvent.fire(this, this);
    if (updateHistory) {
      Historian.goTo(getId());
    }
    
    activateContent();
  }
  
  void activateContent() {
    if (getContent() instanceof HandlesStateChange) {
      ((HandlesStateChange) getContent()).onStateChange(State.ACTIVATED);
    }
  }

  void addContentSupplier(String key) {
    if (WidgetFactory.hasSupplier(key)) {
      if (contentSuppliers.contains(key)) {
        contentSuppliers.remove(key);
      }
      contentSuppliers.add(key);

      setContentIndex(contentSuppliers.size() - 1);
    }
  }

  void addTile(Workspace workspace, Direction direction) {
    int size = direction.isHorizontal() ? getCenterWidth() : getCenterHeight();
    size = Math.round((size - getSplitterSize()) / 2);
    if (size < MIN_SIZE) {
      Global.showError("Sub-Planck length is not allowed in this universe");
      return;
    }

    deactivate();

    IdentifiableWidget oldCenter = getCenter();
    TilePanel newCenter;

    if (oldCenter != null) {
      Global.setTemporaryDetach(true);
      remove(oldCenter);
      Global.setTemporaryDetach(false);

      newCenter = newTile(workspace, oldCenter);
      newCenter.onLayout();
    } else {
      newCenter = newBlankTile(workspace);
    }
    
    if (!contentSuppliers.isEmpty()) {
      newCenter.contentSuppliers.addAll(contentSuppliers);
      newCenter.setContentIndex(getContentIndex());
      
      contentSuppliers.clear();
      setContentIndex(BeeConst.UNDEF);
    }

    TilePanel tile = newBlankTile(workspace);

    insert(tile, direction, size, null, null, getSplitterSize());
    add(newCenter);

    tile.activate(true);
  }

  TilePanel close() {
    boolean wasActive = isActive();

    if (!isBlank()) {
      if (wasActive) {
        deactivate();
      }
      blank();
      if (wasActive) {
        activate(false);
      }
      return this;
    }

    if (isRoot()) {
      return this;
    }

    TilePanel parent = (TilePanel) getParent();
    TilePanel entangled = null;

    for (Widget sibling : parent.getChildren()) {
      if (sibling instanceof TilePanel && sibling != this) {
        entangled = (TilePanel) sibling;
        break;
      }
    }
    if (entangled == null) {
      logger.severe("entangled tile not found");
      return this;
    }

    if (wasActive) {
      deactivate();
    }
    
    parent.contentSuppliers.clear();
    parent.contentSuppliers.addAll(entangled.contentSuppliers);
    parent.setContentIndex(entangled.getContentIndex());
    
    Historian.remove(getId());
    Historian.remove(entangled.getId());

    Global.setTemporaryDetach(true);
    entangled.moveTo(parent);
    Global.setTemporaryDetach(false);

    if (wasActive) {
      while (parent.getCenter() instanceof TilePanel) {
        parent = (TilePanel) parent.getCenter();
      }
      parent.activate(true);
    }
    return parent;
  }

  boolean closeable() {
    return !isRoot() || !isBlank();
  }

  TilePanel getActiveTile() {
    return isActive() ? this : locateActiveChild(getRoot());
  }

  IdentifiableWidget getContent() {
    return isBlank() ? null : getCenter();
  }

  List<IdentifiableWidget> getContentWidgets() {
    List<IdentifiableWidget> result = Lists.newArrayList();

    IdentifiableWidget content = getContent();
    if (content != null) {
      result.add(content);
    }

    for (Widget child : getChildren()) {
      if (child instanceof TilePanel) {
        result.addAll(((TilePanel) child).getContentWidgets());
      }
    }
    return result;
  }

  TilePanel getRoot() {
    return isRoot() ? this : ((TilePanel) getParent()).getRoot();
  }

  TreeItem getTree(String prefix) {
    TreeItem root = new TreeItem(BeeUtils.joinWords(prefix, getId()));

    if (isActive()) {
      root.addItem("active");
    }
    
    if (!contentSuppliers.isEmpty()) {
      root.addItem(BeeUtils.joinWords("Suppliers", contentSuppliers.size(),
          "index", getContentIndex()));
      for (String key : contentSuppliers) {
        root.addItem(key);
      }
    }
    
    for (Widget child : getChildren()) {
      if (isSplitter(child)) {
        continue;
      }

      String s = BeeUtils.joinWords(getWidgetDirection(child).brief(),
          getWidgetWidth(child), getWidgetHeight(child));

      if (child instanceof TilePanel) {
        root.addItem(((TilePanel) child).getTree(s));
      } else {
        root.addItem(BeeUtils.joinWords(s, NameUtils.getName(child), DomUtils.getId(child)));
      }
    }
    return root;
  }

  boolean isBlank() {
    return getCenter() instanceof BlankTile;
  }

  boolean isLeaf() {
    for (Widget child : getChildren()) {
      if (child instanceof TilePanel) {
        return false;
      }
    }
    return true;
  }

  boolean isRoot() {
    return !(getParent() instanceof TilePanel);
  }

  void updateContent(IdentifiableWidget widget, boolean addSupplier) {
    boolean wasActive = isActive();
    if (wasActive) {
      deactivate();
    }

    clear();
    add(widget);

    if (wasActive) {
      activate(false);
    }
    
    if (addSupplier) {
      if (widget instanceof HasWidgetSupplier) {
        addContentSupplier(((HasWidgetSupplier) widget).getSupplierKey());
      } else {
        setContentIndex(BeeConst.UNDEF);
      }
    }
  }

  private void blank() {
    clear();
    add(new BlankTile());
    
    setContentIndex(BeeConst.UNDEF);
  }

  private void deactivate() {
    TilePanel tile = getActiveTile();

    if (tile != null) {
      tile.setActiveStyle(false);
      tile.setActive(false);
    }
  }

  private int getContentIndex() {
    return contentIndex;
  }

  private boolean isActive() {
    return active;
  }

  private void moveTo(TilePanel parent) {
    IdentifiableWidget centerWidget = getCenter();

    List<IdentifiableWidget> children = Lists.newArrayList();
    for (Widget child : getChildren()) {
      if (!isSplitter(child) && child != centerWidget) {
        children.add((IdentifiableWidget) child);
      }
    }

    int c = children.size();
    if (c <= 0) {
      clear();
      parent.clear();

      if (centerWidget != null) {
        parent.add(centerWidget);
      }
      return;
    }

    Direction[] directions = new Direction[c];
    double[] sizes = new double[c];
    int[] splSizes = new int[c];

    for (int i = 0; i < c; i++) {
      Widget child = children.get(i).asWidget();

      directions[i] = getWidgetDirection(child);
      sizes[i] = directions[i].isHorizontal() ? getWidgetWidth(child) : getWidgetHeight(child);
      splSizes[i] = getWidgetSplitterSize(child);
    }

    clear();
    parent.clear();

    for (int i = 0; i < c; i++) {
      parent.insert(children.get(i), directions[i], sizes[i], null, null, splSizes[i]);
    }
    if (centerWidget != null) {
      parent.add(centerWidget);
    }
    parent.onLayout();
  }

  private void setActive(boolean active) {
    this.active = active;
  }

  private void setActiveStyle(boolean add) {
    if (isRoot()) {
      return;
    }

    IdentifiableWidget widget = getCenter();

    if (widget instanceof BlankTile) {
      widget.asWidget().setStyleName(ACTIVE_BLANK, add);
    } else if (widget != null) {
      if (add) {
        getWidgetContainerElement(widget.asWidget()).addClassName(ACTIVE_CONTENT);
      } else {
        getWidgetContainerElement(widget.asWidget()).removeClassName(ACTIVE_CONTENT);
      }
    }
  }

  private void setContentIndex(int contentIndex) {
    this.contentIndex = contentIndex;
  }
}

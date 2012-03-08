package com.butent.bee.client.tree;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;

import java.util.ArrayList;
import java.util.List;

public class TreeItem extends UIObject implements HasTreeItems, HasId {

  private static class TreeItemAnimation extends Animation {

    private static final int DURATION = 200;
    private static final int DURATION_PER_ITEM = 75;

    private Element element = null;
    private boolean opening = true;

    private int height = 0;

    private TreeItemAnimation() {
      super();
    }

    @Override
    protected void onComplete() {
      if (element != null) {
        setVisible(element, opening);
        if (opening) {
          onUpdate(1.0);
          StyleUtils.autoHeight(element);
        }

        element.getStyle().clearOverflow();
        StyleUtils.autoWidth(element);

        element = null;
      }
    }

    @Override
    protected void onStart() {
      if (element == null) {
        return;
      }

      height = opening ? 0 : element.getScrollHeight();
      StyleUtils.hideScroll(element);

      super.onStart();

      if (opening) {
        setVisible(element, true);
        height = element.getScrollHeight();
      }
    }

    @Override
    protected void onUpdate(double progress) {
      if (element == null) {
        return;
      }

      int h = (int) (progress * height);
      if (!opening) {
        h = height - h;
      }

      StyleUtils.setHeight(element, Math.max(h, 1));
      StyleUtils.setWidth(element, element.getScrollWidth());
    }

    private void setItemState(TreeItem item, boolean animate) {
      cancel();

      if (animate) {
        element = item.getChildSpanElem();
        opening = item.isOpen();

        run(Math.min(DURATION, DURATION_PER_ITEM * item.getChildCount()));
      } else {
        setVisible(item.getChildSpanElem(), item.isOpen());
      }
    }
  }

  private static final TreeItemAnimation itemAnimation = new TreeItemAnimation();
  
  private static final String STYLE_PREFIX = "bee-TreeItem-"; 
  private static final String STYLE_BRANCH_CONTAINER = STYLE_PREFIX + "branchContainer"; 
  private static final String STYLE_LEAF_CONTAINER = STYLE_PREFIX + "leafContainer"; 

  private static final Element BRANCH_ELEM;
  private static final Element CONTAINER_ELEM;

  static {
    BRANCH_ELEM = DOM.createTable();
    Element tbody = DOM.createTBody();
    Element tr = DOM.createTR();
    Element tdImg = DOM.createTD();
    Element tdContent = DOM.createTD();

    setStyleName(BRANCH_ELEM, STYLE_PREFIX + "branch");
    setStyleName(tdImg, STYLE_PREFIX + "imageCell");
    setStyleName(tdContent, STYLE_PREFIX + "contentCell");

    BRANCH_ELEM.appendChild(tbody);
    tbody.appendChild(tr);
    tr.appendChild(tdImg);
    tr.appendChild(tdContent);
    
    CONTAINER_ELEM = DOM.createDiv();
    Element content = DOM.createDiv();

    setStyleName(CONTAINER_ELEM, STYLE_LEAF_CONTAINER);
    setStyleName(content, STYLE_PREFIX + "content");

    CONTAINER_ELEM.appendChild(content);
  }

  private List<TreeItem> children = null;
  
  private final Element contentElem;
  private Element childSpanElem = null;
  private Element imageHolder = null;

  private final boolean isRoot;

  private boolean open = false;
  private boolean selected = false;

  private TreeItem parentItem = null;
  private Tree tree = null;

  private Widget widget = null;
  private Object userObject = null;

  public TreeItem() {
    this(false);
  }

  public TreeItem(String html) {
    this();
    setHtml(html);
  }

  public TreeItem(String html, Object obj) {
    this(html);
    setUserObject(obj);
  }

  public TreeItem(Widget widget) {
    this();
    setWidget(widget);
  }

  TreeItem(boolean isRoot) {
    this.isRoot = isRoot;

    Element elem = DOM.clone(CONTAINER_ELEM, true);
    setElement(elem);
    DomUtils.createId(elem, getIdPrefix() + "-container");

    this.contentElem = DOM.getFirstChild(elem);
    DomUtils.createId(this.contentElem, getIdPrefix() + "-content");

    if (isRoot) {
      initChildren();
    }
  }

  public TreeItem addItem(String itemHtml) {
    TreeItem ret = new TreeItem(itemHtml);
    addItem(ret);
    return ret;
  }

  public void addItem(TreeItem item) {
    maybeRemoveItemFromParent(item);
    insertItem(getChildCount(), item);
  }

  public TreeItem addItem(Widget w) {
    TreeItem ret = new TreeItem(w);
    addItem(ret);
    return ret;
  }

  public TreeItem getChild(int index) {
    if ((index < 0) || (index >= getChildCount())) {
      return null;
    }
    return getChildren().get(index);
  }

  public int getChildCount() {
    if (getChildren() == null) {
      return 0;
    }
    return getChildren().size();
  }

  public int getChildIndex(TreeItem child) {
    if (getChildren() == null) {
      return BeeConst.UNDEF;
    }
    return getChildren().indexOf(child);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "t-i";
  }

  public TreeItem getParentItem() {
    return parentItem;
  }

  public Object getUserObject() {
    return userObject;
  }

  public Widget getWidget() {
    return widget;
  }

  public TreeItem insertItem(int beforeIndex, String itemText) {
    TreeItem ret = new TreeItem(itemText);
    insertItem(beforeIndex, ret);
    return ret;
  }

  public void insertItem(int beforeIndex, TreeItem item) {
    maybeRemoveItemFromParent(item);

    int childCount = getChildCount();
    Assert.betweenInclusive(beforeIndex, 0, childCount);

    if (getChildren() == null) {
      initChildren();
    }
    
    setStyleName(item.getElement(), STYLE_PREFIX + "root", isRoot);
    setStyleName(item.getElement(), STYLE_PREFIX + "child", !isRoot);
    
    Element childContainer = isRoot ? getTree().getElement() : getChildSpanElem();
    if (beforeIndex == childCount) {
      childContainer.appendChild(item.getElement());
    } else {
      Element beforeElem = getChild(beforeIndex).getElement();
      childContainer.insertBefore(item.getElement(), beforeElem);
    }

    item.setParentItem(isRoot ? null : this);
    getChildren().add(beforeIndex, item);

    item.setTree(getTree());

    if (!isRoot && getChildren().size() == 1) {
      updateState(false, false);
    }
  }

  public TreeItem insertItem(int beforeIndex, Widget w) {
    TreeItem ret = new TreeItem(w);
    insertItem(beforeIndex, ret);
    return ret;
  }

  public boolean isOpen() {
    return open;
  }

  public boolean isSelected() {
    return selected;
  }

  public void remove() {
    if (getParentItem() != null) {
      getParentItem().removeItem(this);
    } else if (getTree() != null) {
      getTree().removeItem(this);
    }
  }

  public void removeItem(TreeItem item) {
    if (getChildren() == null || !getChildren().contains(item)) {
      return;
    }

    Tree oldTree = getTree();
    item.setTree(null);

    if (isRoot) {
      oldTree.getElement().removeChild(item.getElement());
    } else {
      getChildSpanElem().removeChild(item.getElement());
    }

    item.setParentItem(null);
    getChildren().remove(item);

    if (!isRoot && getChildren().isEmpty()) {
      updateState(false, false);
    }
  }

  public void removeItems() {
    while (getChildCount() > 0) {
      removeItem(getChild(0));
    }
  }

  public void setHtml(String html) {
    setWidget(null);
    contentElem.setInnerHTML(html);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setOpen(boolean open) {
    setOpen(open, true);
  }

  public void setOpen(boolean open, boolean fireEvents) {
    if (open && getChildCount() == 0) {
      return;
    }

    if (this.open != open) {
      this.open = open;
      updateState(true, true);

      if (fireEvents && getTree() != null) {
        getTree().fireStateChanged(this, open);
      }
    }
  }

  public void setOpenRecursive(boolean open, boolean fireEvents) {
    int n = getChildCount();
    if (n <= 0) {
      return;
    }
    
    setOpen(open, fireEvents);
    for (int i = 0; i < n; i++) {
      getChildren().get(i).setOpenRecursive(open, fireEvents);
    }
  }
  
  public void setSelected(boolean selected) {
    if (this.selected == selected) {
      return;
    }
    this.selected = selected;
    setStyleName(getContentElem(), STYLE_PREFIX + "selected", selected);
  }

  public void setUserObject(Object userObj) {
    userObject = userObj;
  }

  public void setWidget(Widget newWidget) {
    if (newWidget != null) {
      newWidget.removeFromParent();
    }

    if (getWidget() != null) {
      try {
        if (getTree() != null) {
          getTree().orphanWidget(getWidget());
        }
      } finally {
        contentElem.removeChild(getWidget().getElement());
        this.widget = null;
      }
    }

    contentElem.setInnerHTML(BeeConst.STRING_EMPTY);

    this.widget = newWidget;

    if (newWidget != null) {
      contentElem.appendChild(newWidget.getElement());

      if (getTree() != null) {
        getTree().adopt(newWidget, this);
      }

      if (Tree.shouldTreeDelegateFocusToElement(newWidget.getElement())) {
        newWidget.getElement().setTabIndex(-1);
      }
    }
  }

  protected Focusable getFocusable() {
    Widget w = getWidget();

    if (w instanceof Focusable) {
      return (Focusable) w;
    } else {
      return null;
    }
  }

  void addTreeItems(List<TreeItem> accum) {
    int size = getChildCount();
    for (int i = 0; i < size; i++) {
      TreeItem item = getChildren().get(i);
      accum.add(item);
      item.addTreeItems(accum);
    }
  }

  Element getContentElem() {
    return contentElem;
  }

  Element getImageElement() {
    return DOM.getFirstChild(getImageHolderElement());
  }

  Element getImageHolderElement() {
    if (!isBranch()) {
      convertToBranch();
    }
    return getImageHolder();
  }

  void setTree(Tree newTree) {
    if (getTree() == newTree) {
      return;
    }

    if (getTree() != null) {
      if (getTree().getSelectedItem() == this) {
        getTree().setSelectedItem(null);
      }

      if (getWidget() != null) {
        getTree().orphanWidget(getWidget());
      }
    }

    this.tree = newTree;
    for (int i = 0, n = getChildCount(); i < n; ++i) {
      getChildren().get(i).setTree(newTree);
    }
    updateState(false, true);

    if (newTree != null) {
      if (getWidget() != null) {
        newTree.adopt(getWidget(), this);
      }
    }
  }

  void updateStateRecursive() {
    updateStateRecursiveHelper();
    getTree().maybeUpdateSelection(this, isOpen());
  }

  private void convertToBranch() {
    if (getImageHolder() == null) {
      Element itemTable = DOM.clone(BRANCH_ELEM, true);
      getElement().appendChild(itemTable);
      Element tr = DOM.getFirstChild(DOM.getFirstChild(itemTable));
      Element tdImg = DOM.getFirstChild(tr);
      Element tdContent = DOM.getNextSibling(tdImg);

      tdContent.appendChild(contentElem);
      
      removeStyleName(STYLE_LEAF_CONTAINER);
      addStyleName(STYLE_BRANCH_CONTAINER);
      
      setImageHolder(tdImg);
    }
  }

  private List<TreeItem> getChildren() {
    return children;
  }

  private Element getChildSpanElem() {
    return childSpanElem;
  }

  private Element getImageHolder() {
    return imageHolder;
  }

  private Tree getTree() {
    return tree;
  }

  private void initChildren() {
    convertToBranch();

    setChildSpanElem(DOM.createDiv());
    getElement().appendChild(getChildSpanElem());
    setStyleName(getChildSpanElem(), STYLE_PREFIX + "children");
    
    this.children = new ArrayList<TreeItem>();
  }

  private boolean isBranch() {
    return getImageHolder() != null;
  }

  private void maybeRemoveItemFromParent(TreeItem item) {
    if ((item.getParentItem() != null) || (item.getTree() != null)) {
      item.remove();
    }
  }

  private void setChildSpanElem(Element childSpanElem) {
    this.childSpanElem = childSpanElem;
  }

  private void setImageHolder(Element imageHolder) {
    this.imageHolder = imageHolder;
  }

  private void setParentItem(TreeItem parentItem) {
    this.parentItem = parentItem;
  }

  private void updateState(boolean animate, boolean updateTreeSelection) {
    if (getTree() == null || !getTree().isAttached()) {
      return;
    }

    if (getChildCount() == 0) {
      if (getChildSpanElem() != null) {
        setVisible(getChildSpanElem(), false);
      }
      return;
    }

    if (animate && (getTree() != null) && (getTree().isAttached())) {
      itemAnimation.setItemState(this, getTree().isAnimationEnabled());
    } else {
      itemAnimation.setItemState(this, false);
    }

    if (isOpen()) {
      getTree().showOpenImage(this);
    } else {
      getTree().showClosedImage(this);
    }

    if (updateTreeSelection) {
      getTree().maybeUpdateSelection(this, isOpen());
    }
  }

  private void updateStateRecursiveHelper() {
    updateState(false, false);
    for (int i = 0, n = getChildCount(); i < n; ++i) {
      getChildren().get(i).updateStateRecursiveHelper();
    }
  }
}

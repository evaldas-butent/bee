package com.butent.bee.client.tree;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;

import java.util.ArrayList;
import java.util.List;

public class TreeItem extends UIObject implements HasTreeItems, HasId {

  public static class TreeItemImpl {
    public TreeItemImpl() {
      initializeClonableElements();
    }

    void convertToFullNode(TreeItem item) {
      if (item.imageHolder == null) {
        // Extract the Elements from the object
        Element itemTable = DOM.clone(BASE_INTERNAL_ELEM, true);
        DOM.appendChild(item.getElement(), itemTable);
        Element tr = DOM.getFirstChild(DOM.getFirstChild(itemTable));
        Element tdImg = DOM.getFirstChild(tr);
        Element tdContent = DOM.getNextSibling(tdImg);

        // Undoes padding from table element.
        DOM.setStyleAttribute(item.getElement(), "padding", "0px");
        DOM.appendChild(tdContent, item.contentElem);
        item.imageHolder = tdImg;
      }
    }

    void initializeClonableElements() {
      if (GWT.isClient()) {
        // Create the base table element that will be cloned.
        BASE_INTERNAL_ELEM = DOM.createTable();
        Element contentElem = DOM.createDiv();
        Element tbody = DOM.createTBody(), tr = DOM.createTR();
        Element tdImg = DOM.createTD(), tdContent = DOM.createTD();
        DOM.appendChild(BASE_INTERNAL_ELEM, tbody);
        DOM.appendChild(tbody, tr);
        DOM.appendChild(tr, tdImg);
        DOM.appendChild(tr, tdContent);
        DOM.setStyleAttribute(tdImg, "verticalAlign", "middle");
        DOM.setStyleAttribute(tdContent, "verticalAlign", "middle");
        DOM.appendChild(tdContent, contentElem);
        DOM.setStyleAttribute(contentElem, "display", "inline");
        setStyleName(contentElem, "bee-TreeItem");
        DOM.setStyleAttribute(BASE_INTERNAL_ELEM, "whiteSpace", "nowrap");

        // Create the base element that will be cloned
        BASE_BARE_ELEM = DOM.createDiv();

        // Simulates padding from table element.
        DOM.setStyleAttribute(BASE_BARE_ELEM, "padding", "3px");
        DOM.appendChild(BASE_BARE_ELEM, contentElem);
      }
    }
  }

  /**
   * An {@link Animation} used to open the child elements. If a {@link TreeItem} is in the process
   * of opening, it will immediately be opened and the new {@link TreeItem} will use this animation.
   */
  private static class TreeItemAnimation extends Animation {

    /**
     * The {@link TreeItem} currently being affected.
     */
    private TreeItem curItem = null;

    /**
     * Whether the item is being opened or closed.
     */
    private boolean opening = true;

    /**
     * The target height of the child items.
     */
    private int scrollHeight = 0;

    /**
     * Open the specified {@link TreeItem}.
     * 
     * @param item the {@link TreeItem} to open
     * @param animate true to animate, false to open instantly
     */
    public void setItemState(TreeItem item, boolean animate) {
      // Immediately complete previous open
      cancel();

      // Open the new item
      if (animate) {
        curItem = item;
        opening = item.open;
        run(Math.min(ANIMATION_DURATION, ANIMATION_DURATION_PER_ITEM
            * curItem.getChildCount()));
      } else {
        UIObject.setVisible(item.childSpanElem, item.open);
      }
    }

    @Override
    protected void onComplete() {
      if (curItem != null) {
        if (opening) {
          UIObject.setVisible(curItem.childSpanElem, true);
          onUpdate(1.0);
          DOM.setStyleAttribute(curItem.childSpanElem, "height", "auto");
        } else {
          UIObject.setVisible(curItem.childSpanElem, false);
        }
        DOM.setStyleAttribute(curItem.childSpanElem, "overflow", "visible");
        DOM.setStyleAttribute(curItem.childSpanElem, "width", "auto");
        curItem = null;
      }
    }

    @Override
    protected void onStart() {
      scrollHeight = 0;

      // If the TreeItem is already open, we can get its scrollHeight
      // immediately.
      if (!opening) {
        scrollHeight = curItem.childSpanElem.getScrollHeight();
      }
      DOM.setStyleAttribute(curItem.childSpanElem, "overflow", "hidden");

      // If the TreeItem is already open, onStart will set its height to its
      // natural height. If the TreeItem is currently closed, onStart will set
      // its height to 1px (see onUpdate below), and then we make the TreeItem
      // visible so we can get its correct scrollHeight.
      super.onStart();

      // If the TreeItem is currently closed, we need to make it visible before
      // we can get its height.
      if (opening) {
        UIObject.setVisible(curItem.childSpanElem, true);
        scrollHeight = curItem.childSpanElem.getScrollHeight();
      }
    }

    @Override
    protected void onUpdate(double progress) {
      int height = (int) (progress * scrollHeight);
      if (!opening) {
        height = scrollHeight - height;
      }

      // Issue 2338: If the height is 0px, IE7 will display all of the children
      // instead of hiding them completely.
      height = Math.max(height, 1);

      DOM.setStyleAttribute(curItem.childSpanElem, "height", height + "px");

      // We need to set the width explicitly of the item might be cropped
      int scrollWidth = DOM.getElementPropertyInt(curItem.childSpanElem,
          "scrollWidth");
      DOM.setStyleAttribute(curItem.childSpanElem, "width", scrollWidth + "px");
    }
  }

  /**
   * The margin applied to child items.
   */
  private static final double CHILD_MARGIN = 16.0;

  // By not overwriting the default tree padding and spacing, we traditionally
  // added 7 pixels between our image and content.
  // <2>|<1>image<1>|<2>|<1>content
  // So to preserve the current spacing we must add a 7 pixel pad when no image
  // is supplied.
  static final int IMAGE_PAD = 7;

  private static final int ANIMATION_DURATION = 200;

  /**
   * The duration of the animation per child {@link TreeItem}. If the per item
   * duration times the number of child items is less than the duration above,
   * the smaller duration will be used.
   */
  private static final int ANIMATION_DURATION_PER_ITEM = 75;

  /**
   * The static animation used to open {@link TreeItem TreeItems}.
   */
  private static TreeItemAnimation itemAnimation = new TreeItemAnimation();

  /**
   * The structured table to hold images.
   */
  private static Element BASE_INTERNAL_ELEM;

  /**
   * The base tree item element that will be cloned.
   */
  private static Element BASE_BARE_ELEM;

  private static TreeItemImpl impl = new TreeItemImpl();

  private ArrayList<TreeItem> children;
  private Element contentElem, childSpanElem, imageHolder;

  private boolean isRoot;

  private boolean open;
  private TreeItem parent;
  private boolean selected;

  private Object userObject;

  private Tree tree;

  private Widget widget;

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
    Element elem = DOM.clone(BASE_BARE_ELEM, true);
    setElement(elem);

    DomUtils.createId(this, getIdPrefix());
    
    contentElem = DOM.getFirstChild(elem);
    DOM.setElementAttribute(contentElem, "id", DOM.createUniqueId());

    // The root item always has children.
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
    // If this is the item's parent, removing the item will affect the child
    // count.
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
    return children.get(index);
  }

  public int getChildCount() {
    if (children == null) {
      return 0;
    }
    return children.size();
  }

  public int getChildIndex(TreeItem child) {
    if (children == null) {
      return -1;
    }
    return children.indexOf(child);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "treeitem";
  }

  public TreeItem getParentItem() {
    return parent;
  }

  public boolean getState() {
    return open;
  }

  public final Tree getTree() {
    return tree;
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
    // Detach item from existing parent.
    maybeRemoveItemFromParent(item);

    // Check the index after detaching in case this item was already the parent.
    int childCount = getChildCount();
    Assert.betweenInclusive(beforeIndex, 0, childCount);

    if (children == null) {
      initChildren();
    }

    // Set the margin.
    // Use no margin on top-most items.
    double margin = isRoot ? 0.0 : CHILD_MARGIN;
    if (LocaleInfo.getCurrentLocale().isRTL()) {
      item.getElement().getStyle().setMarginRight(margin, Unit.PX);
    } else {
      item.getElement().getStyle().setMarginLeft(margin, Unit.PX);
    }

    // Physical attach.
    Element childContainer = isRoot ? tree.getElement() : childSpanElem;
    if (beforeIndex == childCount) {
      childContainer.appendChild(item.getElement());
    } else {
      Element beforeElem = getChild(beforeIndex).getElement();
      childContainer.insertBefore(item.getElement(), beforeElem);
    }

    // Logical attach.
    // Explicitly set top-level items' parents to null if this is root.
    item.setParentItem(isRoot ? null : this);
    children.add(beforeIndex, item);

    // Adopt.
    item.setTree(tree);

    if (!isRoot && children.size() == 1) {
      updateState(false, false);
    }
  }

  public TreeItem insertItem(int beforeIndex, Widget w) {
    TreeItem ret = new TreeItem(w);
    insertItem(beforeIndex, ret);
    return ret;
  }

  public boolean isSelected() {
    return selected;
  }

  public void remove() {
    if (parent != null) {
      // If this item has a parent, remove self from it.
      parent.removeItem(this);
    } else if (tree != null) {
      // If the item has no parent, but is in the Tree, it must be a top-level
      // element.
      tree.removeItem(this);
    }
  }

  public void removeItem(TreeItem item) {
    // Validate.
    if (children == null || !children.contains(item)) {
      return;
    }

    // Orphan.
    Tree oldTree = tree;
    item.setTree(null);

    // Physical detach.
    if (isRoot) {
      oldTree.getElement().removeChild(item.getElement());
    } else {
      childSpanElem.removeChild(item.getElement());
    }

    // Logical detach.
    item.setParentItem(null);
    children.remove(item);

    if (!isRoot && children.size() == 0) {
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
    DOM.setInnerHTML(contentElem, html);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setSelected(boolean selected) {
    if (this.selected == selected) {
      return;
    }
    this.selected = selected;
    setStyleName(getContentElem(), "bee-TreeItem-selected", selected);
  }

  public void setState(boolean open) {
    setState(open, true);
  }

  public void setState(boolean open, boolean fireEvents) {
    if (open && getChildCount() == 0) {
      return;
    }

    // Only do the physical update if it changes
    if (this.open != open) {
      this.open = open;
      updateState(true, true);

      if (fireEvents && tree != null) {
        tree.fireStateChanged(this, open);
      }
    }
  }

  public void setUserObject(Object userObj) {
    userObject = userObj;
  }

  public void setWidget(Widget newWidget) {
    // Detach new child from old parent.
    if (newWidget != null) {
      newWidget.removeFromParent();
    }

    // Detach old child from tree.
    if (widget != null) {
      try {
        if (tree != null) {
          tree.orphanWidget(widget);
        }
      } finally {
        // Physical detach old child.
        contentElem.removeChild(widget.getElement());
        widget = null;
      }
    }

    // Clear out any existing content before adding a widget.
    DOM.setInnerHTML(contentElem, "");

    // Logical detach old/attach new.
    widget = newWidget;

    if (newWidget != null) {
      // Physical attach new.
      DOM.appendChild(contentElem, newWidget.getElement());

      // Attach child to tree.
      if (tree != null) {
        tree.adopt(widget, this);
      }

      // Set tabIndex on the widget to -1, so that it doesn't mess up the tab
      // order of the entire tree

      if (Tree.shouldTreeDelegateFocusToElement(widget.getElement())) {
        DOM.setElementAttribute(widget.getElement(), "tabIndex", "-1");
      }
    }
  }

  /**
   * Returns a suggested {@link Focusable} instance to use when this tree item
   * is selected. The tree maintains focus if this method returns null. By
   * default, if the tree item contains a focusable widget, that widget is
   * returned.
   * 
   * Note, the {@link Tree} will ignore this value if the user clicked on an
   * input element such as a button or text area when selecting this item.
   * 
   * @return the focusable item
   */
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
      TreeItem item = children.get(i);
      accum.add(item);
      item.addTreeItems(accum);
    }
  }

  ArrayList<TreeItem> getChildren() {
    return children;
  }

  Element getContentElem() {
    return contentElem;
  }

  Element getImageElement() {
    return DOM.getFirstChild(getImageHolderElement());
  }

  Element getImageHolderElement() {
    if (!isFullNode()) {
      convertToFullNode();
    }
    return imageHolder;
  }

  void initChildren() {
    convertToFullNode();
    childSpanElem = DOM.createDiv();
    DOM.appendChild(getElement(), childSpanElem);
    DOM.setStyleAttribute(childSpanElem, "whiteSpace", "nowrap");
    children = new ArrayList<TreeItem>();
  }

  boolean isFullNode() {
    return imageHolder != null;
  }

  /**
   * Remove a tree item from its parent if it has one.
   * 
   * @param item the tree item to remove from its parent
   */
  void maybeRemoveItemFromParent(TreeItem item) {
    if ((item.getParentItem() != null) || (item.getTree() != null)) {
      item.remove();
    }
  }

  void setParentItem(TreeItem parent) {
    this.parent = parent;
  }

  void setTree(Tree newTree) {
    // Early out.
    if (tree == newTree) {
      return;
    }

    // Remove this item from existing tree.
    if (tree != null) {
      if (tree.getSelectedItem() == this) {
        tree.setSelectedItem(null);
      }

      if (widget != null) {
        tree.orphanWidget(widget);
      }
    }

    tree = newTree;
    for (int i = 0, n = getChildCount(); i < n; ++i) {
      children.get(i).setTree(newTree);
    }
    updateState(false, true);

    if (newTree != null) {
      if (widget != null) {
        // Add my widget to the new tree.
        newTree.adopt(widget, this);
      }
    }
  }

  void updateState(boolean animate, boolean updateTreeSelection) {
    // If the tree hasn't been set, there is no visual state to update.
    // If the tree is not attached, then update will be called on attach.
    if (tree == null || tree.isAttached() == false) {
      return;
    }

    if (getChildCount() == 0) {
      if (childSpanElem != null) {
        UIObject.setVisible(childSpanElem, false);
      }
      tree.showLeafImage(this);
      return;
    }

    // We must use 'display' rather than 'visibility' here,
    // or the children will always take up space.
    if (animate && (tree != null) && (tree.isAttached())) {
      itemAnimation.setItemState(this, tree.isAnimationEnabled());
    } else {
      itemAnimation.setItemState(this, false);
    }

    // Change the status image
    if (open) {
      tree.showOpenImage(this);
    } else {
      tree.showClosedImage(this);
    }

    // We may need to update the tree's selection in response to a tree state
    // change. For example, if the tree's currently selected item is a
    // descendant of an item whose branch was just collapsed, then the item
    // itself should become the newly-selected item.
    if (updateTreeSelection) {
      tree.maybeUpdateSelection(this, this.open);
    }
  }

  void updateStateRecursive() {
    updateStateRecursiveHelper();
    tree.maybeUpdateSelection(this, this.open);
  }

  private void convertToFullNode() {
    impl.convertToFullNode(this);
  }

  private void updateStateRecursiveHelper() {
    updateState(false, false);
    for (int i = 0, n = getChildCount(); i < n; ++i) {
      children.get(i).updateStateRecursiveHelper();
    }
  }
}

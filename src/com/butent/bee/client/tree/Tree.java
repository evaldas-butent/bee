package com.butent.bee.client.tree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.AbstractImagePrototype.ImagePrototypeElement;
import com.google.gwt.user.client.ui.impl.FocusImpl;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Tree extends Panel implements HasTreeItems, Focusable, HasAnimation,
    HasAllKeyHandlers, HasAllFocusHandlers, HasSelectionHandlers<TreeItem>,
    HasOpenHandlers<TreeItem>, HasCloseHandlers<TreeItem>, HasAllMouseHandlers, HasId {

  public interface Resources extends ClientBundle {
    @Source("treeClosed.gif")
    ImageResource treeClosed();

    @Source("treeOpen.gif")
    ImageResource treeOpen();
  }

  public static AbstractImagePrototype treeClosed = null;
  public static AbstractImagePrototype treeOpen = null;

  static {
    Resources resources = GWT.create(Resources.class);
    treeClosed = AbstractImagePrototype.create(resources.treeClosed());
    treeOpen = AbstractImagePrototype.create(resources.treeOpen());
  }

  static boolean shouldTreeDelegateFocusToElement(Element elem) {
    if (elem == null) {
      return false;
    }
    return BeeUtils.inList(elem.getTagName(), DomUtils.TAG_SELECT, DomUtils.TAG_INPUT,
        DomUtils.TAG_TEXT_AREA, DomUtils.TAG_OPTION, DomUtils.TAG_BUTTON, DomUtils.TAG_LABEL);
  }

  private final Map<Widget, TreeItem> childWidgets = new HashMap<Widget, TreeItem>();

  private TreeItem selectedItem = null;

  private final Element focusable;

  private boolean isAnimationEnabled = false;

  private final TreeItem root;

  public Tree() {
    setElement(DOM.createDiv());

    getElement().getStyle().setPosition(Position.RELATIVE);

    focusable = FocusImpl.getFocusImplForPanel().createFocusable();

    StyleUtils.makeAbsolute(focusable);
    StyleUtils.hideOutline(focusable);
    StyleUtils.setZIndex(focusable, -1);

    getElement().appendChild(focusable);

    sinkEvents(Event.ONMOUSEDOWN | Event.ONCLICK | Event.KEYEVENTS);
    DOM.sinkEvents(focusable, Event.FOCUSEVENTS);

    root = new TreeItem(true);
    root.setTree(this);

    setStyleName("bee-Tree");
    DomUtils.createId(this, getIdPrefix());
  }

  public void add(Widget widget) {
    addItem(widget);
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  public HandlerRegistration addCloseHandler(CloseHandler<TreeItem> handler) {
    return addHandler(handler, CloseEvent.getType());
  }

  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return addDomHandler(handler, FocusEvent.getType());
  }

  public TreeItem addItem(String itemHtml) {
    return root.addItem(itemHtml);
  }

  public void addItem(TreeItem item) {
    root.addItem(item);
  }

  public TreeItem addItem(Widget widget) {
    return root.addItem(widget);
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
    return addDomHandler(handler, KeyPressEvent.getType());
  }

  public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
    return addDomHandler(handler, KeyUpEvent.getType());
  }

  public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
    return addDomHandler(handler, MouseDownEvent.getType());
  }

  public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
    return addDomHandler(handler, MouseMoveEvent.getType());
  }

  public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
    return addDomHandler(handler, MouseOutEvent.getType());
  }

  public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
    return addDomHandler(handler, MouseOverEvent.getType());
  }

  public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
    return addDomHandler(handler, MouseUpEvent.getType());
  }

  public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
    return addDomHandler(handler, MouseWheelEvent.getType());
  }

  public final HandlerRegistration addOpenHandler(OpenHandler<TreeItem> handler) {
    return addHandler(handler, OpenEvent.getType());
  }

  public HandlerRegistration addSelectionHandler(SelectionHandler<TreeItem> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  public void clear() {
    int size = root.getChildCount();
    for (int i = size - 1; i >= 0; i--) {
      root.getChild(i).remove();
    }
  }

  public void ensureSelectedItemVisible() {
    if (getSelectedItem() == null) {
      return;
    }

    TreeItem parent = getSelectedItem().getParentItem();
    while (parent != null) {
      parent.setOpen(true);
      parent = parent.getParentItem();
    }
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "tree";
  }

  public TreeItem getItem(int index) {
    return root.getChild(index);
  }

  public int getItemCount() {
    return root.getChildCount();
  }

  public TreeItem getSelectedItem() {
    return selectedItem;
  }

  public int getTabIndex() {
    return FocusImpl.getFocusImplForPanel().getTabIndex(focusable);
  }

  public TreeItem insertItem(int beforeIndex, String itemText) {
    return root.insertItem(beforeIndex, itemText);
  }

  public void insertItem(int beforeIndex, TreeItem item) {
    root.insertItem(beforeIndex, item);
  }

  public TreeItem insertItem(int beforeIndex, Widget widget) {
    return root.insertItem(beforeIndex, widget);
  }

  public boolean isAnimationEnabled() {
    return isAnimationEnabled;
  }

  public Iterator<Widget> iterator() {
    WidgetCollection widgetCollection = new WidgetCollection(this);
    for (Widget widget : childWidgets.keySet()) {
      widgetCollection.add(widget);
    }
    return widgetCollection.iterator();
  }

  @Override
  public void onBrowserEvent(Event event) {
    int eventType = event.getTypeInt();

    switch (eventType) {
      case Event.ONCLICK: {
        Element el = DOM.eventGetTarget(event);
        if (!shouldTreeDelegateFocusToElement(el) && (getSelectedItem() != null)
            && getSelectedItem().getContentElem().isOrHasChild(el)) {
          setFocus(true);
        }
        break;
      }

      case Event.ONMOUSEDOWN: {
        if ((DOM.eventGetCurrentTarget(event) == getElement())
            && (event.getButton() == Event.BUTTON_LEFT)) {
          elementClicked(DOM.eventGetTarget(event));
        }
        break;
      }

      case Event.ONKEYDOWN: {
        if (getSelectedItem() == null) {
          if (root.getChildCount() > 0) {
            onSelection(root.getChild(0), true, true);
          }
        } else if (isKeyboardNavigationEnabled() && EventUtils.isArrowKey(event) &&
            !event.getAltKey() && !event.getMetaKey()) {
          navigate(event);
          EventUtils.eatEvent(event);
        }
        break;
      }

      case Event.ONKEYUP: {
        if (event.getKeyCode() == KeyCodes.KEY_TAB) {
          List<Element> chain = new ArrayList<Element>();
          collectElementChain(chain, getElement(), DOM.eventGetTarget(event));
          TreeItem item = findItemByChain(chain, 0, root);
          if (item != getSelectedItem()) {
            setSelectedItem(item, true);
          }
        }
        break;
      }
    }

    super.onBrowserEvent(event);
  }

  public boolean remove(Widget w) {
    TreeItem item = childWidgets.get(w);
    if (item == null) {
      return false;
    }

    item.setWidget(null);
    return true;
  }

  public void removeItem(TreeItem item) {
    root.removeItem(item);
  }

  public void removeItems() {
    while (getItemCount() > 0) {
      removeItem(getItem(0));
    }
  }

  public void setAccessKey(char key) {
    FocusImpl.getFocusImplForPanel().setAccessKey(focusable, key);
  }

  public void setAnimationEnabled(boolean enable) {
    this.isAnimationEnabled = enable;
  }

  public void setFocus(boolean focus) {
    if (focus) {
      FocusImpl.getFocusImplForPanel().focus(focusable);
    } else {
      FocusImpl.getFocusImplForPanel().blur(focusable);
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setSelectedItem(TreeItem item) {
    setSelectedItem(item, true);
  }

  public void setSelectedItem(TreeItem item, boolean fireEvents) {
    if (item == null) {
      if (getSelectedItem() == null) {
        return;
      }
      getSelectedItem().setSelected(false);
      this.selectedItem = null;
      return;
    }

    onSelection(item, fireEvents, true);
  }

  public void setTabIndex(int index) {
    FocusImpl.getFocusImplForPanel().setTabIndex(focusable, index);
  }

  public Iterator<TreeItem> treeItemIterator() {
    List<TreeItem> accum = new ArrayList<TreeItem>();
    root.addTreeItems(accum);
    return accum.iterator();
  }

  @Override
  protected void doAttachChildren() {
    try {
      super.doAttachChildren();
    } finally {
      DOM.setEventListener(focusable, this);
    }
  }

  @Override
  protected void doDetachChildren() {
    try {
      super.doDetachChildren();
    } finally {
      DOM.setEventListener(focusable, null);
    }
  }

  protected boolean isKeyboardNavigationEnabled() {
    return true;
  }

  @Override
  protected void onLoad() {
    root.updateStateRecursive();
  }

  void adopt(Widget widget, TreeItem treeItem) {
    Assert.isFalse(childWidgets.containsKey(widget));
    childWidgets.put(widget, treeItem);
    adopt(widget);
  }

  void fireStateChanged(TreeItem item, boolean open) {
    if (open) {
      OpenEvent.fire(this, item);
    } else {
      CloseEvent.fire(this, item);
    }
  }

  void maybeUpdateSelection(TreeItem itemThatChangedState, boolean isItemOpening) {
    if (!isItemOpening) {
      TreeItem tempItem = getSelectedItem();
      while (tempItem != null) {
        if (tempItem == itemThatChangedState) {
          setSelectedItem(itemThatChangedState);
          return;
        }
        tempItem = tempItem.getParentItem();
      }
    }
  }

  void orphanWidget(Widget widget) {
    try {
      orphan(widget);
    } finally {
      childWidgets.remove(widget);
    }
  }

  void showClosedImage(TreeItem treeItem) {
    showImage(treeItem, treeClosed);
  }

  void showOpenImage(TreeItem treeItem) {
    showImage(treeItem, treeOpen);
  }

  private void collectElementChain(List<Element> chain, Element hRoot, Element hElem) {
    if ((hElem == null) || (hElem == hRoot)) {
      return;
    }

    collectElementChain(chain, hRoot, DOM.getParent(hElem));
    chain.add(hElem);
  }

  private boolean elementClicked(Element hElem) {
    List<Element> chain = new ArrayList<Element>();
    collectElementChain(chain, getElement(), hElem);

    TreeItem item = findItemByChain(chain, 0, root);
    if (item != null && item != root) {
      if (item.getChildCount() > 0 && DOM.isOrHasChild(item.getImageElement(), hElem)) {
        item.setOpen(!item.isOpen(), true);
        return true;
      } else if (DOM.isOrHasChild(item.getElement(), hElem)) {
        onSelection(item, true, !shouldTreeDelegateFocusToElement(hElem));
        return true;
      }
    }

    return false;
  }

  private TreeItem findDeepestOpenChild(TreeItem item) {
    if (!item.isOpen()) {
      return item;
    }
    return findDeepestOpenChild(item.getChild(item.getChildCount() - 1));
  }

  private TreeItem findItemByChain(List<Element> chain, int idx, TreeItem rootItem) {
    if (idx == chain.size()) {
      return rootItem;
    }

    Element hCurElem = chain.get(idx);
    for (int i = 0, n = rootItem.getChildCount(); i < n; ++i) {
      TreeItem child = rootItem.getChild(i);
      if (child.getElement() == hCurElem) {
        TreeItem retItem = findItemByChain(chain, idx + 1, rootItem.getChild(i));
        if (retItem == null) {
          return child;
        }
        return retItem;
      }
    }

    return findItemByChain(chain, idx + 1, rootItem);
  }

  private TreeItem getTopClosedParent(TreeItem item) {
    if (item == null) {
      return null;
    }

    TreeItem topClosedParent = null;
    TreeItem parent = item.getParentItem();
    while (parent != null && parent != root) {
      if (!parent.isOpen()) {
        topClosedParent = parent;
      }
      parent = parent.getParentItem();
    }
    return topClosedParent;
  }

  private void maybeCollapseTreeItem() {
    TreeItem topClosedParent = getTopClosedParent(getSelectedItem());
    if (topClosedParent != null) {
      setSelectedItem(topClosedParent);
    } else if (getSelectedItem().isOpen()) {
      getSelectedItem().setOpen(false);
    } else {
      TreeItem parent = getSelectedItem().getParentItem();
      if (parent != null) {
        setSelectedItem(parent);
      }
    }
  }

  private void maybeExpandTreeItem() {
    TreeItem topClosedParent = getTopClosedParent(getSelectedItem());
    if (topClosedParent != null) {
      setSelectedItem(topClosedParent);
    } else if (!getSelectedItem().isOpen()) {
      getSelectedItem().setOpen(true);
    } else if (getSelectedItem().getChildCount() > 0) {
      setSelectedItem(getSelectedItem().getChild(0));
    }
  }

  private void moveFocus() {
    Focusable focusableWidget = getSelectedItem().getFocusable();
    if (focusableWidget != null) {
      focusableWidget.setFocus(true);
      ((Widget) focusableWidget).getElement().scrollIntoView();
    } else {
      Element selectedElem = getSelectedItem().getContentElem();
      int containerLeft = getAbsoluteLeft();
      int containerTop = getAbsoluteTop();

      int left = selectedElem.getAbsoluteLeft() - containerLeft;
      int top = selectedElem.getAbsoluteTop() - containerTop;
      int width = selectedElem.getOffsetWidth();
      int height = selectedElem.getOffsetHeight();

      if (width <= 0 || height <= 0) {
        StyleUtils.setLeft(focusable, 0);
        StyleUtils.setTop(focusable, 0);
        return;
      }

      StyleUtils.setRectangle(focusable, left, top, width, height);
      focusable.scrollIntoView();

      setFocus(true);
    }
  }

  private void moveSelectionDown(TreeItem sel, boolean dig) {
    if (sel == root) {
      return;
    }

    TreeItem topClosedParent = getTopClosedParent(sel);
    if (topClosedParent != null) {
      moveSelectionDown(topClosedParent, false);
      return;
    }

    TreeItem parent = sel.getParentItem();
    if (parent == null) {
      parent = root;
    }
    int idx = parent.getChildIndex(sel);

    if (!dig || !sel.isOpen()) {
      if (idx < parent.getChildCount() - 1) {
        onSelection(parent.getChild(idx + 1), true, true);
      } else {
        moveSelectionDown(parent, false);
      }
    } else if (sel.getChildCount() > 0) {
      onSelection(sel.getChild(0), true, true);
    }
  }

  private void moveSelectionUp(TreeItem sel) {
    TreeItem topClosedParent = getTopClosedParent(sel);
    if (topClosedParent != null) {
      onSelection(topClosedParent, true, true);
      return;
    }

    TreeItem parent = sel.getParentItem();
    if (parent == null) {
      parent = root;
    }
    int idx = parent.getChildIndex(sel);

    if (idx > 0) {
      TreeItem sibling = parent.getChild(idx - 1);
      onSelection(findDeepestOpenChild(sibling), true, true);
    } else {
      onSelection(parent, true, true);
    }
  }

  private void navigate(Event event) {
    switch (event.getKeyCode()) {
      case KeyCodes.KEY_UP: {
        moveSelectionUp(getSelectedItem());
        break;
      }
      case KeyCodes.KEY_DOWN: {
        moveSelectionDown(getSelectedItem(), true);
        break;
      }
      case KeyCodes.KEY_LEFT: {
        maybeCollapseTreeItem();
        break;
      }
      case KeyCodes.KEY_RIGHT: {
        maybeExpandTreeItem();
        break;
      }
    }
  }

  private void onSelection(TreeItem item, boolean fireEvents, boolean moveFocus) {
    if (item == root) {
      return;
    }

    if (getSelectedItem() != null) {
      getSelectedItem().setSelected(false);
    }
    this.selectedItem = item;

    if (item != null) {
      if (moveFocus) {
        moveFocus();
      }

      item.setSelected(true);
      if (fireEvents) {
        SelectionEvent.fire(this, item);
      }
    }
  }

  private void showImage(TreeItem treeItem, AbstractImagePrototype proto) {
    Element holder = treeItem.getImageHolderElement();
    Element child = DOM.getFirstChild(holder);
    if (child == null) {
      DOM.appendChild(holder, proto.createElement().<Element> cast());
    } else {
      proto.applyTo(child.<ImagePrototypeElement> cast());
    }
  }
}

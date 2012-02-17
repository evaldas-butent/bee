package com.butent.bee.client.tree;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.i18n.client.LocaleInfo;
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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Tree extends Panel implements HasTreeItems,
    Focusable, HasAnimation, HasAllKeyHandlers,
    HasAllFocusHandlers, HasSelectionHandlers<TreeItem>,
    HasOpenHandlers<TreeItem>, HasCloseHandlers<TreeItem>,
    HasAllMouseHandlers, HasId {

  public interface Resources extends ClientBundle {
    ImageResource treeClosed();
    ImageResource treeLeaf();
    ImageResource treeOpen();
  }

  public static AbstractImagePrototype treeClosed = null;
  public static AbstractImagePrototype treeLeaf = null;
  public static AbstractImagePrototype treeOpen = null;

  private static final int OTHER_KEY_DOWN = 63233;
  private static final int OTHER_KEY_LEFT = 63234;
  private static final int OTHER_KEY_RIGHT = 63235;
  private static final int OTHER_KEY_UP = 63232;

  static {
    Resources resources = GWT.create(Resources.class);
    treeClosed = AbstractImagePrototype.create(resources.treeClosed());
    treeLeaf = AbstractImagePrototype.create(resources.treeLeaf());
    treeOpen = AbstractImagePrototype.create(resources.treeOpen());
  }

  static native boolean shouldTreeDelegateFocusToElement(Element elem) /*-{
    var name = elem.nodeName;
    return ((name == "SELECT") || (name == "INPUT") || (name == "TEXTAREA") 
      || (name == "OPTION") || (name == "BUTTON") || (name == "LABEL"));
  }-*/;

  private static boolean isArrowKey(int code) {
    switch (code) {
      case OTHER_KEY_DOWN:
      case OTHER_KEY_RIGHT:
      case OTHER_KEY_UP:
      case OTHER_KEY_LEFT:
      case KeyCodes.KEY_DOWN:
      case KeyCodes.KEY_RIGHT:
      case KeyCodes.KEY_UP:
      case KeyCodes.KEY_LEFT:
        return true;
      default:
        return false;
    }
  }

  private static int standardizeKeycode(int code) {
    switch (code) {
      case OTHER_KEY_DOWN:
        code = KeyCodes.KEY_DOWN;
        break;
      case OTHER_KEY_RIGHT:
        code = KeyCodes.KEY_RIGHT;
        break;
      case OTHER_KEY_UP:
        code = KeyCodes.KEY_UP;
        break;
      case OTHER_KEY_LEFT:
        code = KeyCodes.KEY_LEFT;
        break;
    }

    if (LocaleInfo.getCurrentLocale().isRTL()) {
      if (code == KeyCodes.KEY_RIGHT) {
        code = KeyCodes.KEY_LEFT;
      } else if (code == KeyCodes.KEY_LEFT) {
        code = KeyCodes.KEY_RIGHT;
      }
    }
    return code;
  }

  private final Map<Widget, TreeItem> childWidgets = new HashMap<Widget, TreeItem>();

  private TreeItem curSelection;

  private Element focusable;

  private String indentValue;

  private boolean isAnimationEnabled = false;

  private boolean lastWasKeyDown;

  private TreeItem root;

  private boolean useLeafImages;

  public Tree() {
    init(false);
  }

  public Tree(boolean useLeafImages) {
    init(useLeafImages);
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
    return addHandler(handler, MouseDownEvent.getType());
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
    if (curSelection == null) {
      return;
    }

    TreeItem parent = curSelection.getParentItem();
    while (parent != null) {
      parent.setState(true);
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
    return curSelection;
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
  @SuppressWarnings("fallthrough")
  public void onBrowserEvent(Event event) {
    int eventType = DOM.eventGetType(event);

    switch (eventType) {
      case Event.ONKEYDOWN: {
        // If nothing's selected, select the first item.
        if (curSelection == null) {
          if (root.getChildCount() > 0) {
            onSelection(root.getChild(0), true, true);
          }
          super.onBrowserEvent(event);
          return;
        }
      }

      // Intentional fallthrough.
      case Event.ONKEYPRESS:
      case Event.ONKEYUP:
        // Issue 1890: Do not block history navigation via alt+left/right
        if (DOM.eventGetAltKey(event) || DOM.eventGetMetaKey(event)) {
          super.onBrowserEvent(event);
          return;
        }
        break;
    }

    switch (eventType) {
      case Event.ONCLICK: {
        Element e = DOM.eventGetTarget(event);
        if (shouldTreeDelegateFocusToElement(e)) {
          // The click event should have given focus to this element already.
          // Avoid moving focus back up to the tree (so that focusable widgets
          // attached to TreeItems can receive keyboard events).
        } else if (curSelection != null) {
          setFocus(true);
        }
        break;
      }

      case Event.ONMOUSEDOWN: {
        // Currently, the way we're using image bundles causes extraneous events
        // to be sunk on individual items' open/close images. This leads to an
        // extra event reaching the Tree, which we will ignore here.
        // Also, ignore middle and right clicks here.
        if ((DOM.eventGetCurrentTarget(event) == getElement())
            && (event.getButton() == Event.BUTTON_LEFT)) {
          elementClicked(DOM.eventGetTarget(event));
        }
        break;
      }
      case Event.ONKEYDOWN: {
        keyboardNavigation(event);
        lastWasKeyDown = true;
        break;
      }

      case Event.ONKEYPRESS: {
        if (!lastWasKeyDown) {
          keyboardNavigation(event);
        }
        lastWasKeyDown = false;
        break;
      }

      case Event.ONKEYUP: {
        if (DOM.eventGetKeyCode(event) == KeyCodes.KEY_TAB) {
          ArrayList<Element> chain = new ArrayList<Element>();
          collectElementChain(chain, getElement(), DOM.eventGetTarget(event));
          TreeItem item = findItemByChain(chain, 0, root);
          if (item != getSelectedItem()) {
            setSelectedItem(item, true);
          }
        }
        lastWasKeyDown = false;
        break;
      }
    }

    switch (eventType) {
      case Event.ONKEYDOWN:
      case Event.ONKEYUP: {
        if (isArrowKey(DOM.eventGetKeyCode(event))) {
          DOM.eventCancelBubble(event, true);
          DOM.eventPreventDefault(event);
          return;
        }
      }
    }

    // We must call super for all handlers.
    super.onBrowserEvent(event);
  }

  public boolean remove(Widget w) {
    // Validate.
    TreeItem item = childWidgets.get(w);
    if (item == null) {
      return false;
    }

    // Delegate to TreeItem.setWidget, which performs correct removal.
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
    isAnimationEnabled = enable;
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
      if (curSelection == null) {
        return;
      }
      curSelection.setSelected(false);
      curSelection = null;
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
    /**
     * If we just closed the item, let's check to see if this item is the parent of the currently
     * selected item. If so, we should make this item the currently selected selected item.
     */
    if (!isItemOpening) {
      TreeItem tempItem = curSelection;
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

  void showLeafImage(TreeItem treeItem) {
    if (useLeafImages || treeItem.isFullNode()) {
      showImage(treeItem, treeLeaf);
    } else if (LocaleInfo.getCurrentLocale().isRTL()) {
      DOM.setStyleAttribute(treeItem.getElement(), "paddingRight", indentValue);
    } else {
      DOM.setStyleAttribute(treeItem.getElement(), "paddingLeft", indentValue);
    }
  }

  void showOpenImage(TreeItem treeItem) {
    showImage(treeItem, treeOpen);
  }

  private void collectElementChain(ArrayList<Element> chain, Element hRoot, Element hElem) {
    if ((hElem == null) || (hElem == hRoot)) {
      return;
    }

    collectElementChain(chain, hRoot, DOM.getParent(hElem));
    chain.add(hElem);
  }

  private boolean elementClicked(Element hElem) {
    ArrayList<Element> chain = new ArrayList<Element>();
    collectElementChain(chain, getElement(), hElem);

    TreeItem item = findItemByChain(chain, 0, root);
    if (item != null && item != root) {
      if (item.getChildCount() > 0
          && DOM.isOrHasChild(item.getImageElement(), hElem)) {
        item.setState(!item.getState(), true);
        return true;
      } else if (DOM.isOrHasChild(item.getElement(), hElem)) {
        onSelection(item, true, !shouldTreeDelegateFocusToElement(hElem));
        return true;
      }
    }

    return false;
  }

  private TreeItem findDeepestOpenChild(TreeItem item) {
    if (!item.getState()) {
      return item;
    }
    return findDeepestOpenChild(item.getChild(item.getChildCount() - 1));
  }

  private TreeItem findItemByChain(ArrayList<Element> chain, int idx, TreeItem rootItem) {
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
    TreeItem topClosedParent = null;
    TreeItem parent = item.getParentItem();
    while (parent != null && parent != root) {
      if (!parent.getState()) {
        topClosedParent = parent;
      }
      parent = parent.getParentItem();
    }
    return topClosedParent;
  }

  private void init(boolean useLeafImage) {
    setImages(useLeafImage);
    setElement(DOM.createDiv());

    DOM.setStyleAttribute(getElement(), "position", "relative");

    // Fix rendering problem with relatively-positioned elements and their
    // children by
    // forcing the element that is positioned relatively to 'have layout'
    DOM.setStyleAttribute(getElement(), "zoom", "1");

    focusable = FocusImpl.getFocusImplForPanel().createFocusable();
    DOM.setStyleAttribute(focusable, "fontSize", "0");
    DOM.setStyleAttribute(focusable, "position", "absolute");

    // Hide focus outline in Mozilla/Webkit/Opera
    DOM.setStyleAttribute(focusable, "outline", "0px");

    // Hide focus outline in IE 6/7
    DOM.setElementAttribute(focusable, "hideFocus", "true");

    DOM.setIntStyleAttribute(focusable, "zIndex", -1);
    DOM.appendChild(getElement(), focusable);

    sinkEvents(Event.ONMOUSEDOWN | Event.ONCLICK | Event.KEYEVENTS);
    DOM.sinkEvents(focusable, Event.FOCUSEVENTS);

    // The 'root' item is invisible and serves only as a container
    // for all top-level items.
    root = new TreeItem(true);
    root.setTree(this);
    setStyleName("bee-Tree");

    DomUtils.createId(this, getIdPrefix());
  }

  private void keyboardNavigation(Event event) {
    // Handle keyboard events if keyboard navigation is enabled
    if (isKeyboardNavigationEnabled()) {
      int code = DOM.eventGetKeyCode(event);

      switch (standardizeKeycode(code)) {
        case KeyCodes.KEY_UP: {
          moveSelectionUp(curSelection);
          break;
        }
        case KeyCodes.KEY_DOWN: {
          moveSelectionDown(curSelection, true);
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
        default: {
          return;
        }
      }
    }
  }

  private void maybeCollapseTreeItem() {

    TreeItem topClosedParent = getTopClosedParent(curSelection);
    if (topClosedParent != null) {
      // Select the first visible parent if curSelection is hidden
      setSelectedItem(topClosedParent);
    } else if (curSelection.getState()) {
      curSelection.setState(false);
    } else {
      TreeItem parent = curSelection.getParentItem();
      if (parent != null) {
        setSelectedItem(parent);
      }
    }
  }

  private void maybeExpandTreeItem() {

    TreeItem topClosedParent = getTopClosedParent(curSelection);
    if (topClosedParent != null) {
      // Select the first visible parent if curSelection is hidden
      setSelectedItem(topClosedParent);
    } else if (!curSelection.getState()) {
      curSelection.setState(true);
    } else if (curSelection.getChildCount() > 0) {
      setSelectedItem(curSelection.getChild(0));
    }
  }

  private void moveFocus() {
    Focusable focusableWidget = curSelection.getFocusable();
    if (focusableWidget != null) {
      focusableWidget.setFocus(true);
      DOM.scrollIntoView(((Widget) focusableWidget).getElement());
    } else {
      // Get the location and size of the given item's content element relative
      // to the tree.
      Element selectedElem = curSelection.getContentElem();
      int containerLeft = getAbsoluteLeft();
      int containerTop = getAbsoluteTop();

      int left = DOM.getAbsoluteLeft(selectedElem) - containerLeft;
      int top = DOM.getAbsoluteTop(selectedElem) - containerTop;
      int width = DOM.getElementPropertyInt(selectedElem, "offsetWidth");
      int height = DOM.getElementPropertyInt(selectedElem, "offsetHeight");

      // If the item is not visible, quite here
      if (width == 0 || height == 0) {
        DOM.setIntStyleAttribute(focusable, "left", 0);
        DOM.setIntStyleAttribute(focusable, "top", 0);
        return;
      }

      // Set the focusable element's position and size to exactly underlap the
      // item's content element.
      DOM.setStyleAttribute(focusable, "left", left + "px");
      DOM.setStyleAttribute(focusable, "top", top + "px");
      DOM.setStyleAttribute(focusable, "width", width + "px");
      DOM.setStyleAttribute(focusable, "height", height + "px");

      // Scroll it into view.
      DOM.scrollIntoView(focusable);

      // Ensure Focus is set, as focus may have been previously delegated by
      // tree.
      setFocus(true);
    }
  }

  private void moveSelectionDown(TreeItem sel, boolean dig) {
    if (sel == root) {
      return;
    }

    // Find a parent that is visible
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

    if (!dig || !sel.getState()) {
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
    // Find a parent that is visible
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

  private void onSelection(TreeItem item, boolean fireEvents, boolean moveFocus) {
    // 'root' isn't a real item, so don't let it be selected
    // (some cases in the keyboard handler will try to do this)
    if (item == root) {
      return;
    }

    if (curSelection != null) {
      curSelection.setSelected(false);
    }
    curSelection = item;

    if (curSelection != null) {
      if (moveFocus) {
        moveFocus();
      }
      // Select the item and fire the selection event.
      curSelection.setSelected(true);
      if (fireEvents) {
        SelectionEvent.fire(this, curSelection);
      }
    }
  }

  private void setImages(boolean useLeafImages) {
    this.useLeafImages = useLeafImages;

    if (!useLeafImages) {
      Image image = treeLeaf.createImage();
      DOM.setStyleAttribute(image.getElement(), "visibility", "hidden");
      RootPanel.get().add(image);
      int size = image.getWidth() + TreeItem.IMAGE_PAD;
      image.removeFromParent();
      indentValue = (size) + "px";
    }
  }

  private void showImage(TreeItem treeItem, AbstractImagePrototype proto) {
    Element holder = treeItem.getImageHolderElement();
    Element child = DOM.getFirstChild(holder);
    if (child == null) {
      // If no image element has been created yet, create one from the
      // prototype.
      DOM.appendChild(holder, proto.createElement().<Element> cast());
    } else {
      // Otherwise, simply apply the prototype to the existing element.
      proto.applyTo(child.<ImagePrototypeElement> cast());
    }
  }
}

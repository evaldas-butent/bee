package com.butent.bee.egg.client.menu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.BeeImpl;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.HasAfterAddHandler;
import com.butent.bee.egg.client.event.HasBeeBlurHandler;
import com.butent.bee.egg.client.layout.BeeLayoutPanel;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeWidget;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class BeeMenuBar extends Widget implements HasAnimation, HasId,
    HasAfterAddHandler, HasBeeBlurHandler {
  public static enum BAR_TYPE {
    TABLE, FLOW
  }

  public interface Resources extends ClientBundle {
    @ImageOptions(flipRtl = true)
    ImageResource subMenuIcon();
  }

  private static final String STYLENAME_DEFAULT = "bee-MenuBar";
  private ArrayList<UIObject> allItems = new ArrayList<UIObject>();

  private ArrayList<BeeMenuItem> items = new ArrayList<BeeMenuItem>();

  private Element body;

  private AbstractImagePrototype subMenuIcon = null;
  private boolean isAnimationEnabled = false;
  private BeeMenuBar parentMenu;
  private MenuPopup popup;
  private BeeMenuItem selectedItem;
  private BeeMenuBar shownChildMenu;

  private boolean vertical, autoOpen;
  private boolean focusOnHover = true;

  private boolean rtl = LocaleInfo.getCurrentLocale().isRTL();

  private BAR_TYPE barType = BAR_TYPE.TABLE;
  private BeeWidget defaultWidget = BeeMenuItem.DEFAULT_WIDGET;
  private String name = BeeUtils.createUniqueName("mb-");

  public BeeMenuBar() {
    this(false);
  }

  public BeeMenuBar(boolean vertical) {
    this(vertical, null, null);
  }

  public BeeMenuBar(boolean vert, BAR_TYPE bt) {
    this(vert, bt, null);
  }

  public BeeMenuBar(boolean vert, BAR_TYPE bt, BeeWidget defW) {
    this(vert, bt, defW, GWT.<Resources> create(Resources.class));
  }

  public BeeMenuBar(boolean vert, BAR_TYPE bt, BeeWidget defW,
      Resources resources) {
    init(vert, bt, defW, AbstractImagePrototype.create(resources.subMenuIcon()));
    createId();
  }

  public BeeMenuBar(boolean vert, BeeWidget defW) {
    this(vert, null, defW);
  }

  public BeeMenuBar(boolean vertical, Resources resources) {
    this(vertical, null, null, resources);
  }

  public BeeMenuBar(Resources resources) {
    this(false, resources);
  }

  public BeeMenuItem addItem(BeeMenuItem item) {
    return insertItem(item, allItems.size());
  }

  public BeeMenuItem addItem(String text, BeeMenuBar popup) {
    return addItem(new BeeMenuItem(this, text, defaultWidget, popup));
  }

  public BeeMenuItem addItem(String text, BeeWidget type, BeeMenuBar popup) {
    return addItem(new BeeMenuItem(this, text, type, popup));
  }

  public BeeMenuItem addItem(String text, BeeWidget type, MenuCommand cmd) {
    return addItem(new BeeMenuItem(this, text, type, cmd));
  }

  public BeeMenuItem addItem(String text, MenuCommand cmd) {
    return addItem(new BeeMenuItem(this, text, defaultWidget, cmd));
  }

  public BeeMenuItemSeparator addSeparator() {
    return addSeparator(new BeeMenuItemSeparator());
  }

  public BeeMenuItemSeparator addSeparator(BeeMenuItemSeparator separator) {
    return insertSeparator(separator, allItems.size());
  }

  public void createId() {
    DomUtils.createId(this, "menubar");
  }

  public void focus() {
    BeeImpl.focus(getElement());
  }

  public boolean getAutoOpen() {
    return autoOpen;
  }

  public BAR_TYPE getBarType() {
    return barType;
  }

  public BeeWidget getDefaultWidget() {
    return defaultWidget;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public int getItemIndex(BeeMenuItem item) {
    return allItems.indexOf(item);
  }

  public String getName() {
    return name;
  }

  public int getSeparatorIndex(BeeMenuItemSeparator item) {
    return allItems.indexOf(item);
  }

  public BeeMenuItem insertItem(BeeMenuItem item, int beforeIndex)
      throws IndexOutOfBoundsException {
    if (beforeIndex < 0 || beforeIndex > allItems.size()) {
      throw new IndexOutOfBoundsException();
    }

    allItems.add(beforeIndex, item);
    int itemsIndex = 0;
    for (int i = 0; i < beforeIndex; i++) {
      if (allItems.get(i) instanceof BeeMenuItem) {
        itemsIndex++;
      }
    }
    items.add(itemsIndex, item);

    addItemElement(beforeIndex, item.getElement());
    item.setParentMenu(this);
    item.setSelectionStyle(false);
    updateSubmenuIcon(item);
    return item;
  }

  public BeeMenuItemSeparator insertSeparator(BeeMenuItemSeparator separator,
      int beforeIndex) throws IndexOutOfBoundsException {
    if (beforeIndex < 0 || beforeIndex > allItems.size()) {
      throw new IndexOutOfBoundsException();
    }

    if (vertical) {
      setItemColSpan(separator, 2);
    }
    addItemElement(beforeIndex, separator.getElement());
    separator.setParentMenu(this);
    allItems.add(beforeIndex, separator);
    return separator;
  }

  public BeeMenuItemSeparator insertSeparator(int beforeIndex) {
    return insertSeparator(new BeeMenuItemSeparator(), beforeIndex);
  }

  public boolean isAnimationEnabled() {
    return isAnimationEnabled;
  }

  public boolean isFocusOnHoverEnabled() {
    return focusOnHover;
  }

  public void moveSelectionDown() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if (vertical) {
      selectNextItem();
    } else {
      if (selectedItem.getSubMenu() != null
          && !selectedItem.getSubMenu().getItems().isEmpty()
          && (shownChildMenu == null || shownChildMenu.getSelectedItem() == null)) {
        if (shownChildMenu == null) {
          doItemAction(selectedItem, false, true);
        }
        selectedItem.getSubMenu().focus();
      } else if (parentMenu != null) {
        if (parentMenu.vertical) {
          parentMenu.selectNextItem();
        } else {
          parentMenu.moveSelectionDown();
        }
      }
    }
  }

  public void moveSelectionUp() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if ((shownChildMenu == null) && vertical) {
      selectPrevItem();
    } else if ((parentMenu != null) && parentMenu.vertical) {
      parentMenu.selectPrevItem();
    } else {
      close(true);
    }
  }

  @Override
  public void onAfterAdd(HasWidgets parent) {
    if (parent instanceof BeeLayoutPanel) {
      if (isFlow() || vertical) {
        ((BeeLayoutPanel) parent).setWidgetHorizontalPosition(this,
            Layout.Alignment.BEGIN);
      }
      if (isFlow() || !vertical) {
        ((BeeLayoutPanel) parent).setWidgetVerticalPosition(this,
            Layout.Alignment.BEGIN);
      }
    }
  }

  public boolean onBeeBlur(BlurEvent event) {
    if (shownChildMenu == null) {
      selectItem(null);
    }

    return true;
  }

  @Override
  public void onBrowserEvent(Event event) {
    BeeMenuItem item = findItem(DOM.eventGetTarget(event));

    switch (DOM.eventGetType(event)) {
      case Event.ONCLICK:
        BeeImpl.focus(getElement());
        if (item != null) {
          doItemAction(item, true, true);
        }
        break;

      case Event.ONMOUSEOVER:
        if (item != null) {
          itemOver(item, true);
        }
        break;

      case Event.ONMOUSEOUT:
        if (item != null) {
          itemOver(null, true);
        }
        break;

      case Event.ONFOCUS:
        selectFirstItemIfNoneSelected();
        break;

      case Event.ONKEYDOWN:
        int keyCode = DOM.eventGetKeyCode(event);

        switch (keyCode) {
          case KeyCodes.KEY_LEFT:
            if (rtl) {
              moveToNextItem();
            } else {
              moveToPrevItem();
            }
            eatEvent(event);
            break;

          case KeyCodes.KEY_RIGHT:
            if (rtl) {
              moveToPrevItem();
            } else {
              moveToNextItem();
            }
            eatEvent(event);
            break;

          case KeyCodes.KEY_UP:
            moveSelectionUp();
            eatEvent(event);
            break;

          case KeyCodes.KEY_DOWN:
            moveSelectionDown();
            eatEvent(event);
            break;

          case KeyCodes.KEY_ESCAPE:
            closeAllParentsAndChildren();
            eatEvent(event);
            break;

          case KeyCodes.KEY_TAB:
            closeAllParentsAndChildren();
            break;

          case KeyCodes.KEY_ENTER:
            if (!selectFirstItemIfNoneSelected()) {
              doItemAction(selectedItem, true, true);
              eatEvent(event);
            }
            break;
        }
        break;
    }
    super.onBrowserEvent(event);
  }

  public void selectItem(BeeMenuItem item) {
    Assert.isTrue(item == null || item.getParentMenu() == this);

    if (item == selectedItem) {
      return;
    }

    if (selectedItem != null) {
      selectedItem.setSelectionStyle(false);

      if (vertical) {
        Element tr = DOM.getParent(selectedItem.getElement());
        if (DOM.getChildCount(tr) == 2) {
          Element td = DOM.getChild(tr, 1);
          setStyleName(td, "subMenuIcon-selected", false);
        }
      }
    }

    if (item != null) {
      item.setSelectionStyle(true);

      if (vertical) {
        Element tr = DOM.getParent(item.getElement());
        if (DOM.getChildCount(tr) == 2) {
          Element td = DOM.getChild(tr, 1);
          setStyleName(td, "subMenuIcon-selected", true);
        }
      }
    }

    selectedItem = item;
  }

  public void setAnimationEnabled(boolean enable) {
    isAnimationEnabled = enable;
  }

  public void setAutoOpen(boolean autoOpen) {
    this.autoOpen = autoOpen;
  }

  public void setBarType(BAR_TYPE barType) {
    this.barType = barType;
  }

  public void setDefaultWidget(BeeWidget defaultWidget) {
    this.defaultWidget = defaultWidget;
  }

  public void setFocusOnHoverEnabled(boolean enabled) {
    focusOnHover = enabled;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setName(String name) {
    this.name = name;
  }

  protected List<BeeMenuItem> getItems() {
    return this.items;
  }

  protected BeeMenuItem getSelectedItem() {
    return this.selectedItem;
  }

  @Override
  protected void onDetach() {
    if (popup != null) {
      popup.hide();
    }

    super.onDetach();
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    setMenuItemDebugIds(baseID);
  }

  protected void onShow() {
    selectItem(null);
  }

  void closeAllParents() {
    if (parentMenu != null) {
      close(false);
    } else {
      selectItem(null);
    }
  }

  void closeAllParentsAndChildren() {
    closeAllParents();
    if (parentMenu == null && popup != null) {
      popup.hide();
    }
  }

  void doItemAction(final BeeMenuItem item, boolean fireCommand, boolean focus) {
    selectItem(item);

    if (item != null) {
      if (fireCommand && item.getCommand() != null) {
        closeAllParents();

        final MenuCommand cmd = item.getCommand();
        Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
          public void execute() {
            cmd.execute();
          }
        });

        if (shownChildMenu != null) {
          shownChildMenu.onHide(focus);
          popup.hide();
          shownChildMenu = null;
          selectItem(null);
        }
      } else if (item.getSubMenu() != null) {
        if (shownChildMenu == null) {
          openPopup(item);
        } else if (item.getSubMenu() != shownChildMenu) {
          shownChildMenu.onHide(focus);
          popup.hide();
          openPopup(item);
        } else if (fireCommand && !autoOpen) {
          shownChildMenu.onHide(focus);
          popup.hide();
          shownChildMenu = null;
          selectItem(item);
        }
      } else if (autoOpen && shownChildMenu != null) {
        shownChildMenu.onHide(focus);
        popup.hide();
        shownChildMenu = null;
      }
    }
  }

  MenuPopup getPopup() {
    return popup;
  }

  void itemOver(BeeMenuItem item, boolean focus) {
    if (item == null) {
      if ((selectedItem != null)
          && (shownChildMenu == selectedItem.getSubMenu())) {
        return;
      }
    }

    selectItem(item);
    if (focus && focusOnHover) {
      focus();
    }

    if (item != null) {
      if ((shownChildMenu != null) || (parentMenu != null) || autoOpen) {
        doItemAction(item, false, focusOnHover);
      }
    }
  }

  void setMenuItemDebugIds(String baseID) {
    int itemCount = 0;
    for (BeeMenuItem item : items) {
      item.ensureDebugId(baseID + "-item" + itemCount);
      itemCount++;
    }
  }

  void updateSubmenuIcon(BeeMenuItem item) {
    if (!isTable()) {
      return;
    }
    if (!vertical) {
      return;
    }

    int idx = allItems.indexOf(item);
    if (idx == -1) {
      return;
    }

    Element container = getItemContainerElement();
    Element tr = DOM.getChild(container, idx);
    int tdCount = DOM.getChildCount(tr);
    BeeMenuBar submenu = item.getSubMenu();
    if (submenu == null) {
      if (tdCount == 2) {
        DOM.removeChild(tr, DOM.getChild(tr, 1));
      }
      setItemColSpan(item, 2);
    } else if (tdCount == 1) {
      setItemColSpan(item, 1);
      Element td = DOM.createTD();
      DOM.setElementProperty(td, "vAlign", "middle");
      DOM.setInnerHTML(td, subMenuIcon.getHTML());
      setStyleName(td, "subMenuIcon");
      DOM.appendChild(tr, td);
    }
  }

  private void addItemElement(int beforeIndex, Element elem) {
    if (isFlow()) {
      DOM.insertChild(body, elem, beforeIndex);
    } else if (vertical) {
      Element tr = DOM.createTR();
      DOM.insertChild(body, tr, beforeIndex);
      Element td = DOM.createTD();
      DOM.appendChild(td, elem);
      DOM.appendChild(tr, td);
    } else {
      Element tr = DOM.getChild(body, 0);
      Element td = DOM.createTD();
      DOM.appendChild(td, elem);
      DOM.insertChild(tr, td, beforeIndex);
    }
  }

  private void close(boolean focus) {
    if (parentMenu != null) {
      parentMenu.popup.hide(!focus);
      if (focus) {
        parentMenu.focus();
      }
    }
  }

  private void eatEvent(Event event) {
    DOM.eventCancelBubble(event, true);
    DOM.eventPreventDefault(event);
  }

  private BeeMenuItem findItem(Element hItem) {
    for (BeeMenuItem item : items) {
      if (DOM.isOrHasChild(item.getElement(), hItem)) {
        return item;
      }
    }
    return null;
  }

  private Element getItemContainerElement() {
    if (vertical) {
      return body;
    } else {
      return DOM.getChild(body, 0);
    }
  }

  private void init(boolean vert, BAR_TYPE bt, BeeWidget defW,
      AbstractImagePrototype subIcon) {
    this.vertical = vert;
    if (bt != null) {
      this.barType = bt;
    }
    if (defW != null) {
      this.defaultWidget = defW;
    }
    this.subMenuIcon = subIcon;

    Element elem;

    if (isFlow()) {
      elem = DOM.createDiv();
      body = elem;
    } else {
      elem = DOM.createTable();
      body = DOM.createTBody();
      DOM.appendChild(elem, body);

      if (!vert) {
        Element tr = DOM.createTR();
        DOM.appendChild(body, tr);
      }
    }

    Element outer = BeeImpl.createFocusable();
    DOM.appendChild(outer, elem);
    setElement(outer);

    sinkEvents(Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT
        | Event.ONFOCUS | Event.ONKEYDOWN);

    setStyleName(STYLENAME_DEFAULT);
    if (vertical) {
      addStyleDependentName("vertical");
    } else {
      addStyleDependentName("horizontal");
    }

    addStyleDependentName(barType.toString().toLowerCase());

    DOM.setStyleAttribute(getElement(), "outline", "0px");
    DOM.setElementAttribute(getElement(), "hideFocus", "true");

    BeeKeeper.getBus().addBlurHandler(this, true);
  }
  
  private boolean isFlow() {
    return barType == BAR_TYPE.FLOW;
  }

  private boolean isTable() {
    return barType == BAR_TYPE.TABLE;
  }

  private void moveToNextItem() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if (!vertical) {
      selectNextItem();
    } else {
      if (selectedItem.getSubMenu() != null
          && !selectedItem.getSubMenu().getItems().isEmpty()
          && (shownChildMenu == null || shownChildMenu.getSelectedItem() == null)) {
        if (shownChildMenu == null) {
          doItemAction(selectedItem, false, true);
        }
        selectedItem.getSubMenu().focus();
      } else if (parentMenu != null) {
        if (!parentMenu.vertical) {
          parentMenu.selectNextItem();
        } else {
          parentMenu.moveToNextItem();
        }
      }
    }
  }

  private void moveToPrevItem() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if (!vertical) {
      selectPrevItem();
    } else {
      if ((parentMenu != null) && (!parentMenu.vertical)) {
        parentMenu.selectPrevItem();
      } else {
        close(true);
      }
    }
  }

  private void onHide(boolean focus) {
    if (shownChildMenu != null) {
      shownChildMenu.onHide(focus);
      popup.hide();
      if (focus) {
        focus();
      }
    }
  }

  private void openPopup(BeeMenuItem item) {
    if (parentMenu != null && parentMenu.popup != null) {
      parentMenu.popup.setPreviewingAllNativeEvents(false);
    }

    popup = new MenuPopup(this, item, true, false);
    popup.setAnimationEnabled(isAnimationEnabled);

    popup.setStyleName(STYLENAME_DEFAULT + "Popup");
    String primaryStyleName = getStylePrimaryName();
    if (!STYLENAME_DEFAULT.equals(primaryStyleName)) {
      popup.addStyleName(primaryStyleName + "Popup");
    }

    shownChildMenu = item.getSubMenu();
    item.getSubMenu().parentMenu = this;

    popup.setPopupPositionAndShow(new MenuPositionCallback(item.getElement(),
        popup, vertical, rtl));
  }

  private boolean selectFirstItemIfNoneSelected() {
    if (selectedItem == null) {
      if (items.size() > 0) {
        BeeMenuItem nextItem = items.get(0);
        selectItem(nextItem);
      }
      return true;
    }
    return false;
  }

  private void selectNextItem() {
    if (selectedItem == null) {
      return;
    }

    int index = items.indexOf(selectedItem);
    Assert.isTrue(index != -1);

    BeeMenuItem itemToBeSelected;

    if (index < items.size() - 1) {
      itemToBeSelected = items.get(index + 1);
    } else {
      itemToBeSelected = items.get(0);
    }

    selectItem(itemToBeSelected);
    if (shownChildMenu != null) {
      doItemAction(itemToBeSelected, false, true);
    }
  }

  private void selectPrevItem() {
    if (selectedItem == null) {
      return;
    }

    int index = items.indexOf(selectedItem);
    Assert.isTrue(index != -1);

    BeeMenuItem itemToBeSelected;
    if (index > 0) {
      itemToBeSelected = items.get(index - 1);
    } else {
      itemToBeSelected = items.get(items.size() - 1);
    }

    selectItem(itemToBeSelected);
    if (shownChildMenu != null) {
      doItemAction(itemToBeSelected, false, true);
    }
  }

  private void setItemColSpan(UIObject item, int colspan) {
    if (isTable()) {
      DOM.setElementPropertyInt(item.getElement(), "colSpan", colspan);
    }
  }

}

package com.butent.bee.egg.client.menu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.BeeImpl;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.EventUtils;
import com.butent.bee.egg.client.event.HasAfterAddHandler;
import com.butent.bee.egg.client.event.HasBeeBlurHandler;
import com.butent.bee.egg.client.layout.BeeLayoutPanel;
import com.butent.bee.egg.client.layout.BeeStack;
import com.butent.bee.egg.client.layout.BeeTab;
import com.butent.bee.egg.client.menu.BeeMenuItem.ITEM_TYPE;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class BeeMenuBar extends Widget implements HasId, HasAfterAddHandler,
    HasBeeBlurHandler, CloseHandler<PopupPanel> {
  public static enum BAR_TYPE {
    TABLE, FLOW, LIST, OLIST, ULIST, DLIST
  }

  public static interface Resources extends ClientBundle {
    @ImageOptions(flipRtl = true)
    ImageResource subMenuIcon();
  }

  public static AbstractImagePrototype subMenuIcon = null;

  private static final String STYLENAME_DEFAULT = "bee-MenuBar";
  private static final String STYLENAME_ROOT = "bee-MenuRoot";

  static {
    Resources resources = GWT.create(Resources.class);
    subMenuIcon = AbstractImagePrototype.create(resources.subMenuIcon());
  }
  
  private List<UIObject> allItems = new ArrayList<UIObject>();
  private List<BeeMenuItem> items = new ArrayList<BeeMenuItem>();

  private Element body;

  private BeeMenuBar parentMenu;
  private MenuPopup popup;
  private BeeMenuItem selectedItem;
  private BeeMenuBar childMenu;

  private int level;
  private boolean vertical;

  private BAR_TYPE barType = BAR_TYPE.TABLE;
  private ITEM_TYPE defaultItemType = BeeMenuItem.defaultType;
  private String name = BeeUtils.createUniqueName("mb-");
  
  private boolean hoverEnabled = true;
  
  public BeeMenuBar() {
    this(0);
  }

  public BeeMenuBar(int level) {
    this(level, false);
  }

  public BeeMenuBar(int level, boolean vert) {
    this(level, vert, null);
  }

  public BeeMenuBar(int level, boolean vert, BAR_TYPE bt) {
    this(level, vert, bt, null);
  }

  public BeeMenuBar(int level, boolean vert, BAR_TYPE bt, ITEM_TYPE it) {
    this(level, vert, bt, it, false);
  }

  public BeeMenuBar(int level, boolean vert, BAR_TYPE bt, ITEM_TYPE it, boolean hover) {
    init(level, vert, bt, it, hover);
    createId();
  }

  public BeeMenuItem addItem(BeeMenuItem item) {
    return insertItem(item, allItems.size());
  }

  public BeeMenuItem addItem(String text, BeeMenuBar popup) {
    return addItem(new BeeMenuItem(this, text, defaultItemType, popup));
  }

  public BeeMenuItem addItem(String text, ITEM_TYPE type, BeeMenuBar popup) {
    return addItem(new BeeMenuItem(this, text, type, popup));
  }

  public BeeMenuItem addItem(String text, ITEM_TYPE type, MenuCommand cmd) {
    return addItem(new BeeMenuItem(this, text, type, cmd));
  }

  public BeeMenuItem addItem(String text, MenuCommand cmd) {
    return addItem(new BeeMenuItem(this, text, defaultItemType, cmd));
  }

  public BeeMenuItemSeparator addSeparator() {
    return addSeparator(new BeeMenuItemSeparator());
  }

  public BeeMenuItemSeparator addSeparator(BeeMenuItemSeparator separator) {
    return insertSeparator(separator, allItems.size());
  }

  public void closePopup() {
    if (popup != null) {
      popup.hide();
    }
  }

  public void createId() {
    DomUtils.createId(this, "menubar");
  }

  public ITEM_TYPE getDefaultItemType() {
    return defaultItemType;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public int getLevel() {
    return level;
  }

  public String getName() {
    return name;
  }

  public boolean isVertical() {
    return vertical;
  }

  public void onAfterAdd(HasWidgets parent) {
    if (parent instanceof BeeLayoutPanel) {
      if (vertical) {
        ((BeeLayoutPanel) parent).setWidgetHorizontalPosition(this,
            Layout.Alignment.BEGIN);
      }
      if (!vertical || barType != BAR_TYPE.TABLE) {
        ((BeeLayoutPanel) parent).setWidgetVerticalPosition(this,
            Layout.Alignment.BEGIN);
      }

    } else if (parent instanceof BeeStack || parent instanceof BeeTab) {
      setStylePrimaryName(STYLENAME_ROOT);
    }
  }

  public boolean onBeeBlur(BlurEvent event) {
    if (childMenu == null) {
      selectItem(null);
    }

    return true;
  }

  @Override
  public void onBrowserEvent(Event event) {
    Element target = DOM.eventGetTarget(event);
    int type = DOM.eventGetType(event);
    
    if (EventUtils.isKeyEvent(type)) {
      hoverEnabled = false;
    } else if (type == Event.ONCLICK || type == Event.ONMOUSEWHEEL) {
      hoverEnabled = true;
    }

    BeeMenuItem item = findItem(target);
    if (item == null && !EventUtils.isKeyEvent(type)
        && type != Event.ONMOUSEWHEEL) {
      super.onBrowserEvent(event);
      return;
    }

    switch (type) {
      case Event.ONCLICK:
        if (!DomUtils.isLabelElement(target)) {
          if (hasCommand(item)) {
            doCommand(item);
          } else if (hasSubMenu(item)) {
            selectItem(item);
            openSubMenu(item);
          }
        }
        break;

      case Event.ONMOUSEOVER:
        if (hoverEnabled) { 
          itemOver(item);
        }
        break;

      case Event.ONMOUSEOUT:
        if (hoverEnabled) {
          itemOver(null);
        }
        break;

      case Event.ONMOUSEWHEEL:
        int mwv = event.getMouseWheelVelocityY();
        if (mwv != 0) {
          if (mwv > 0) {
            selectNextItem();
          } else {
            selectPrevItem();
          }
          eatEvent(event);
        }
        break;

      case Event.ONKEYPRESS:
        if (moveTo(event.getCharCode())) {
          eatEvent(event);
        }
        break;

      case Event.ONKEYDOWN:
        int keyCode = event.getKeyCode();

        switch (keyCode) {
          case KeyCodes.KEY_LEFT:
            moveLeft();
            eatEvent(event);
            break;

          case KeyCodes.KEY_RIGHT:
            moveRight();
            eatEvent(event);
            break;

          case KeyCodes.KEY_UP:
            moveUp();
            eatEvent(event);
            break;

          case KeyCodes.KEY_DOWN:
            moveDown();
            eatEvent(event);
            break;

          case KeyCodes.KEY_HOME:
          case KeyCodes.KEY_PAGEUP:
            if (items.size() > 1) {
              selectItem(0);
              eatEvent(event);
            }
            break;

          case KeyCodes.KEY_END:
          case KeyCodes.KEY_PAGEDOWN:
            if (items.size() > 1) {
              selectItem(items.size() - 1);
              eatEvent(event);
            }
            break;

          case KeyCodes.KEY_ESCAPE:
            closeAll();
            eatEvent(event);
            break;

          case KeyCodes.KEY_TAB:
            closeAll();
            break;

          case KeyCodes.KEY_DELETE:
          case KeyCodes.KEY_BACKSPACE:
            if (parentMenu != null) {
              parentMenu.focus();
              parentMenu.closeChildMenu();
              eatEvent(event);
            }
            break;

          case KeyCodes.KEY_ENTER:
            if (!selectFirstItemIfNoneSelected()) {
              if (hasCommand(selectedItem)) {
                doCommand(selectedItem);
              } else if (hasSubMenu(selectedItem)) {
                activateSubMenu(selectedItem);
              }
              eatEvent(event);
            }
            break;
        }
        break;
    }

    super.onBrowserEvent(event);
  }

  @Override
  public void onClose(CloseEvent<PopupPanel> event) {
    childMenu = null;
    popup = null;
    
    if (event.isAutoClosed() && parentMenu == null) {
      selectItem(null);
    }
  }
  
  public void prepare() {
    if (barType == BAR_TYPE.LIST && items.size() > 1) {
      SelectElement.as(body).setSize(items.size());
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void updateSubmenuIcon(BeeMenuItem item) {
    if (barType != BAR_TYPE.TABLE) {
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

  @Override
  protected void onDetach() {
    closePopup();
    super.onDetach();
  }

  private void activateSubMenu(BeeMenuItem item) {
    activateSubMenu(item, false);
  }

  private void activateSubMenu(BeeMenuItem item, boolean end) {
    if (!hasSubMenu(item)) {
      return;
    }
    
    openSubMenu(item);

    if (end) {
      item.getSubMenu().selectLastItemIfNoneSelected();
    } else {
      item.getSubMenu().selectFirstItemIfNoneSelected();
    }
    
    if (hasSubMenu(item.getSubMenu().selectedItem)) {
      item.getSubMenu().openSubMenu(item.getSubMenu().selectedItem);
    }

    item.getSubMenu().focus();
  }

  private void addItemElement(int beforeIndex, Element elem) {
    if (barType != BAR_TYPE.TABLE) {
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

  private void closeAll() {
    selectItem(null);
    closeChildMenu();
    if (parentMenu != null) {
      parentMenu.closeAll();
    }
  }

  private void closeChildMenu() {
    if (childMenu != null) {
      childMenu.closeChildMenu();
    }
    closePopup();
    childMenu = null;
  }

  private void doCommand(BeeMenuItem item) {
    closeAll();
    Scheduler.get().scheduleDeferred(item.getCommand());
  }
  
  private void eatEvent(Event event) {
    event.stopPropagation();
    event.preventDefault();
  }

  private BeeMenuItem findItem(Element elem) {
    for (BeeMenuItem item : items) {
      if (DOM.isOrHasChild(item.getElement(), elem)) {
        return item;
      }
    }

    return null;
  }

  private void focus() {
    BeeImpl.focus(getElement());
  }

  private Element getItemContainerElement() {
    if (vertical) {
      return body;
    } else {
      return DOM.getChild(body, 0);
    }
  }

  private boolean hasCommand(BeeMenuItem item) {
    if (item == null) {
      return false;
    } else {
      return item.getCommand() != null;
    }
  }

  private boolean hasSubMenu(BeeMenuItem item) {
    if (item == null) {
      return false;
    } else {
      return item.getSubMenu() != null;
    }
  }

  private void init(int level, boolean vert, BAR_TYPE bt, ITEM_TYPE it, boolean hover) {
    this.level = level;
    this.vertical = vert;
    if (bt != null) {
      this.barType = bt;
    }
    if (it != null) {
      this.defaultItemType = it;
    }
    
    Element elem;
    
    switch (barType) {
      case FLOW:
        elem = DOM.createDiv();
        body = elem;
        break;
      
      case LIST:
        elem = DOM.createSelect();
        body = elem;
        break;
      
      case OLIST:
        elem = Document.get().createOLElement().cast();
        body = elem;
        break;

      case ULIST:
        elem = Document.get().createULElement().cast();
        body = elem;
        break;
        
      case DLIST:
        elem = Document.get().createDLElement().cast();
        body = elem;
        break;

      default:
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

    sinkEvents(Event.ONCLICK | Event.ONKEYDOWN | Event.ONKEYPRESS
        | Event.ONMOUSEWHEEL);
    if (hover) {
      sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
    }

    setStyleName((level == 0) ? STYLENAME_ROOT : STYLENAME_DEFAULT);
    if (level > 0) {
      addStyleDependentName("level-" + level);
    }
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

  private BeeMenuItem insertItem(BeeMenuItem item, int beforeIndex) {
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
    item.setSelected(false);
    updateSubmenuIcon(item);

    return item;
  }

  private BeeMenuItemSeparator insertSeparator(BeeMenuItemSeparator separator,
      int beforeIndex) {
    if (vertical) {
      setItemColSpan(separator, 2);
    }

    addItemElement(beforeIndex, separator.getElement());
    separator.setParentMenu(this);
    allItems.add(beforeIndex, separator);

    return separator;
  }

  private void itemOver(BeeMenuItem item) {
    if (item == null && childMenu != null) {
      return;
    }
    
    if (item != null) {
      focus();
    }

    selectItem(item);

    if (hasSubMenu(item)) {
      openSubMenu(item);
    }
  }

  private void moveDown() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if (vertical) {
      selectNextItem();
    } else if (hasSubMenu(selectedItem)) {
      activateSubMenu(selectedItem);
    } else if (parentMenu != null) {
      parentMenu.focus();
      parentMenu.selectNextItem();
    }
  }

  private void moveLeft() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if (!vertical) {
      selectPrevItem();
    } else if (parentMenu != null) {
      parentMenu.focus();
      parentMenu.selectPrevItem();
    } else if (hasSubMenu(selectedItem)) {
      activateSubMenu(selectedItem, true);
    }
  }

  private void moveRight() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if (!vertical) {
      selectNextItem();
    } else if (hasSubMenu(selectedItem)) {
      activateSubMenu(selectedItem);
    } else if (parentMenu != null) {
      parentMenu.focus();
      parentMenu.selectNextItem();
    }
  }

  private boolean moveTo(int code) {
    boolean ok = false;
    if (code <= BeeConst.CHAR_SPACE) {
      return ok;
    }

    int n = items.size();
    if (n <= 1) {
      return ok;
    }

    int oldIdx = (selectedItem == null) ? -1 : items.indexOf(selectedItem);
    int idx = oldIdx;

    String search = new String(new char[]{(char) code});
    String txt;

    for (int i = 0; i < ((oldIdx >= 0) ? n - 1 : n); i++) {
      idx++;
      if (idx < 0 || idx >= n) {
        idx = 0;
      }
      if (idx == oldIdx) {
        continue;
      }

      txt = items.get(idx).getElement().getInnerText();
      if (BeeUtils.isEmpty(txt)) {
        continue;
      }
      if (txt.substring(0, 1).equalsIgnoreCase(search)) {
        ok = true;
        break;
      }
    }

    if (ok) {
      selectItem(idx);
    }
    return ok;
  }

  private void moveUp() {
    if (selectFirstItemIfNoneSelected()) {
      return;
    }

    if (vertical) {
      selectPrevItem();
    } else if (parentMenu != null) {
      parentMenu.focus();
      parentMenu.selectPrevItem();
    } else if (hasSubMenu(selectedItem)) {
      activateSubMenu(selectedItem, true);
    }
  }

  private void openPopup(BeeMenuItem item) {
    popup = new MenuPopup(this, item);

    childMenu = item.getSubMenu();
    childMenu.parentMenu = this;
    childMenu.selectItem(null);

    popup.addCloseHandler(this);

    popup.setPopupPositionAndShow(new MenuPositionCallback(getElement(),
        item.getElement(), popup, vertical));
  }

  private void openSubMenu(BeeMenuItem item) {
    if (childMenu == null) {
      openPopup(item);
    } else if (item.getSubMenu() != childMenu) {
      closeChildMenu();
      openPopup(item);
    }
  }

  private boolean selectFirstItemIfNoneSelected() {
    if (selectedItem == null) {
      if (items.size() > 0) {
        BeeMenuItem item = items.get(0);
        selectItem(item);
      }
      return true;
    }
    return false;
  }

  private void selectItem(BeeMenuItem item) {
    if (item == selectedItem) {
      return;
    }

    if (selectedItem != null) {
      selectedItem.setSelected(false);
      closeChildMenu();
    }

    if (item == null) {
      hoverEnabled = true;
    } else {
      DOM.scrollIntoView(item.getElement());
      item.setSelected(true);
    }

    selectedItem = item;
  }

  private void selectItem(int index) {
    Assert.isIndex(items, index);
    if (selectedItem != null && index == items.indexOf(selectedItem)) {
      return;
    }

    BeeMenuItem item = items.get(index);
    selectItem(item);
    
    if (hasSubMenu(item)) {
      openSubMenu(item);
    }
  }

  private boolean selectLastItemIfNoneSelected() {
    if (selectedItem == null) {
      if (items.size() > 0) {
        BeeMenuItem item = items.get(items.size() - 1);
        selectItem(item);
      }
      return true;
    }
    return false;
  }

  private void selectNextItem() {
    if (selectedItem == null || items.size() <= 1) {
      return;
    }

    int index = items.indexOf(selectedItem);
    if (index < items.size() - 1) {
      index++;
    } else {
      index = 0;
    }

    selectItem(index);
  }

  private void selectPrevItem() {
    if (selectedItem == null || items.size() <= 1) {
      return;
    }

    int index = items.indexOf(selectedItem);
    if (index > 0) {
      index--;
    } else {
      index = items.size() - 1;
    }

    selectItem(index);
  }

  private void setItemColSpan(UIObject item, int colspan) {
    if (barType == BAR_TYPE.TABLE) {
      DOM.setElementPropertyInt(item.getElement(), "colSpan", colspan);
    }
  }

}

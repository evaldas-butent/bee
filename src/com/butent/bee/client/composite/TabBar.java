package com.butent.bee.client.composite;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasWordWrap;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.ui.AcceptsCaptions;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class TabBar extends Composite implements HasBeforeSelectionHandlers<Integer>,
    HasSelectionHandlers<Integer>, HasId, HasItems, AcceptsCaptions {

  public interface Tab extends HasAllKeyHandlers, HasClickHandlers, HasWordWrap, HasEnabled {
    boolean hasWordWrap();
  }

  private class DelegatePanel extends Composite implements Tab {
    private boolean enabled = true;

    private DelegatePanel(Widget widget) {
      DomUtils.makeFocusable(widget);
      initWidget(widget);

      sinkEvents(Event.ONCLICK | Event.ONKEYDOWN);
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
      return addHandler(handler, ClickEvent.getType());
    }

    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
      return addHandler(handler, KeyDownEvent.getType());
    }

    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
      return addDomHandler(handler, KeyPressEvent.getType());
    }

    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
      return addDomHandler(handler, KeyUpEvent.getType());
    }

    public boolean getWordWrap() {
      if (hasWordWrap()) {
        return ((HasWordWrap) getWidget()).getWordWrap();
      } else {
        return false;
      }
    }

    public boolean hasWordWrap() {
      return getWidget() instanceof HasWordWrap;
    }

    public boolean isEnabled() {
      return enabled;
    }

    @Override
    public void onBrowserEvent(Event event) {
      if (!isEnabled()) {
        return;
      }
      
      int index = TabBar.this.getTabIndex(this);
      
      switch (event.getTypeInt()) {
        case Event.ONCLICK:
          selectTab(index);
          break;

        case Event.ONKEYDOWN:
          int size = TabBar.this.getItemCount();
          int navigateTo = BeeConst.UNDEF;

          switch (event.getKeyCode()) {
            case KeyCodes.KEY_ENTER:
              selectTab(index);
              break;
            case KeyCodes.KEY_LEFT:
              navigateTo = BeeUtils.rotateBackwardExclusive(index, 0, size);
              break;
            case KeyCodes.KEY_RIGHT:
              navigateTo = BeeUtils.rotateForwardExclusive(index, 0, size);
              break;
            case KeyCodes.KEY_HOME:
              navigateTo = 0;
              break;
            case KeyCodes.KEY_END:
              navigateTo = size - 1;
              break;
          }
          
          if (!BeeConst.isUndef(navigateTo) && navigateTo != index) {
            TabBar.this.focusTab(navigateTo);
          }
          break;
      }
      super.onBrowserEvent(event);
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public void setWordWrap(boolean wrap) {
      if (hasWordWrap()) {
        ((HasWordWrap) getWidget()).setWordWrap(wrap);
      } else {
        Assert.unsupported("Widget does not implement HasWordWrap");
      }
    }

    @Override
    protected Widget getWidget() {
      return super.getWidget();
    }
  }

  private static final String DEFAULT_STYLE_PREFIX = "bee-TabBar-";

  private final Horizontal panel = new Horizontal();
  private DelegatePanel selectedTab = null;

  private final String stylePrefix;
  
  public TabBar() {
    this(DEFAULT_STYLE_PREFIX);
  }
  
  public TabBar(String stylePrefix) {
    this.stylePrefix = stylePrefix;
    initWidget(panel);
    sinkEvents(Event.ONCLICK);
    setStyleName(stylePrefix + "panel");

    panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

    Html first = new Html(BeeConst.HTML_NBSP, true);
    Html rest = new Html(BeeConst.HTML_NBSP, true);

    first.setStyleName(stylePrefix + "first");
    rest.setStyleName(stylePrefix + "rest");
    first.setHeight("100%");
    rest.setHeight("100%");

    panel.add(first);
    panel.add(rest);
  }

  public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler) {
    return addHandler(handler, BeforeSelectionEvent.getType());
  }

  public void addCaptions(Class<? extends Enum<?>> clazz) {
    addItems(UiHelper.getCaptions(clazz));
  }

  public void addCaptions(String captionKey) {
    addItems(Global.getCaptions(captionKey));
  }

  public void addItem(SafeHtml html) {
    addItem(html.asString(), true);
  }

  public void addItem(String text) {
    insertTab(text, getItemCount());
  }

  public void addItem(String text, boolean asHTML) {
    insertTab(text, asHTML, getItemCount());
  }

  public void addItem(Widget widget) {
    insertTab(widget, getItemCount());
  }

  public void addItems(Collection<String> items) {
    Assert.notNull(items);
    for (String it : items) {
      addItem(it);
    }
  }

  public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }
  
  public void clear() {
    for (int i = getItemCount(); i >= 0; i--) {
      removeTab(i);
    }
  }

  public void focusTab(int index) {
    checkTabIndex(index, 0);
    DomUtils.setFocus(getTabWidget(index), true);
  }

  public String getId() {
    return panel.getId();
  }

  public String getIdPrefix() {
    return panel.getIdPrefix();
  }

  public int getItemCount() {
    return panel.getWidgetCount() - 2;
  }
  
  public List<String> getItems() {
    List<String> items = Lists.newArrayList();
    for (int i = 0; i < getItemCount(); i++) {
      items.add(getTabWidget(i).getElement().getInnerHTML());
    }
    return items;
  }

  public int getSelectedTab() {
    if (selectedTab == null) {
      return BeeConst.UNDEF;
    }
    return panel.getWidgetIndex(selectedTab) - 1;
  }

  public Widget getSelectedWidget() {
    if (selectedTab == null) {
      return null;
    }
    return selectedTab.getWidget();
  }

  public Tab getTab(int index) {
    if (index < 0 || index >= getItemCount()) {
      return null;
    }
    return getWrapper(index);
  }

  public Widget getTabWidget(int index)  {
    if (index < 0 || index >= getItemCount()) {
      return null;
    } else {
      return getWrapper(index).getWidget();
    }
  }

  public void insertTab(SafeHtml html, int beforeIndex) {
    insertTab(html.asString(), true, beforeIndex);
  }

  public void insertTab(String text, boolean asHTML, int beforeIndex) {
    checkInsertBeforeTabIndex(beforeIndex);

    Label item;
    if (asHTML) {
      item = new Html(text);
    } else {
      item = new BeeLabel(text);
    }

    item.setWordWrap(false);
    insertTabWidget(item, beforeIndex);
  }

  public void insertTab(String text, int beforeIndex) {
    insertTab(text, false, beforeIndex);
  }

  public void insertTab(Widget widget, int beforeIndex) {
    insertTabWidget(widget, beforeIndex);
  }
  
  public boolean isIndex(int index) {
    return index >= 0 && index < getItemCount();
  }

  public boolean isTabEnabled(int index) {
    checkTabIndex(index, 0);
    return getWrapper(index).isEnabled();
  }

  public void removeTab(int index) {
    checkTabIndex(index, 0);

    Widget toRemove = panel.getWidget(index + 1);
    if (toRemove == selectedTab) {
      selectedTab = null;
    }
    panel.remove(toRemove);
  }

  public boolean selectTab(int index) {
    return selectTab(index, true);
  }

  public boolean selectTab(int index, boolean fireEvents) {
    checkTabIndex(index, 0);

    if (fireEvents) {
      BeforeSelectionEvent<?> event = BeforeSelectionEvent.fire(this, index);
      if (event != null && event.isCanceled()) {
        return false;
      }
    }

    setSelectionStyle(selectedTab, false);
    if (BeeConst.isUndef(index)) {
      selectedTab = null;
      return true;
    }

    selectedTab = getWrapper(index);
    setSelectionStyle(selectedTab, true);
    if (fireEvents) {
      SelectionEvent.fire(this, index);
    }
    return true;
  }

  public void setId(String id) {
    panel.setId(id);
  }

  public void setItems(Collection<String> items) {
    if (getItemCount() > 0) {
      clear();
    }
    if (items != null) {
      addItems(items);
    }
  }

  public void setTabEnabled(int index, boolean enabled) {
    checkTabIndex(index, 0);

    DelegatePanel delPanel = getWrapper(index);
    delPanel.setEnabled(enabled);
    setStyleName(delPanel.getElement(), stylePrefix + "item-disabled", !enabled);
  }

  protected void insertTabWidget(Widget widget, int beforeIndex) {
    checkInsertBeforeTabIndex(beforeIndex);

    DelegatePanel delWidget = new DelegatePanel(widget);
    delWidget.setStyleName(stylePrefix + "item");

    panel.insert(delWidget, beforeIndex + 1);
  }

  private void checkInsertBeforeTabIndex(int beforeIndex) {
    Assert.betweenInclusive(beforeIndex, 0, getItemCount());
  }

  private void checkTabIndex(int index, int min) {
    Assert.betweenExclusive(index, min, getItemCount());
  }
  
  private int getTabIndex(Widget wrapper) {
    for (int i = 0; i < getItemCount(); i++) {
      if (getWrapper(i) == wrapper) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }
  
  private DelegatePanel getWrapper(int index) {
    return (DelegatePanel) panel.getWidget(index + 1);
  }

  private void setSelectionStyle(Widget item, boolean selected) {
    if (item != null) {
      setStyleName(item.getElement(), stylePrefix + "item-selected", selected);
    }
  }
}

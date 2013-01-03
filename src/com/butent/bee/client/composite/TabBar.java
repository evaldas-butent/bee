package com.butent.bee.client.composite;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.KeyCodes;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.CellVector;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.IsHtmlTable;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.ui.AcceptsCaptions;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class TabBar extends Composite implements HasBeforeSelectionHandlers<Integer>,
    HasSelectionHandlers<Integer>, IdentifiableWidget, HasItems, AcceptsCaptions, IsHtmlTable {

  private class Tab extends Simple implements HasEnabled {
    private boolean enabled = true;

    private Tab(Widget widget) {
      super();
      setWidget(widget);

      DomUtils.makeFocusable(this);
      sinkEvents(Event.ONCLICK | Event.ONKEYDOWN);
    }

    @Override
    public String getIdPrefix() {
      return "tab";
    }

    @Override
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
            case KeyCodes.KEY_UP:
              navigateTo = BeeUtils.rotateBackwardExclusive(index, 0, size);
              break;
            case KeyCodes.KEY_RIGHT:
            case KeyCodes.KEY_DOWN:
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

    @Override
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  private static final String DEFAULT_STYLE_PREFIX = "bee-TabBar-";

  private static final String STYLE_PANEL = "panel";
  private static final String STYLE_ITEM = "item";

  private static final String STYLE_DISABLED = "item-disabled";
  private static final String STYLE_SELECTED = "item-selected";

  private static final String STYLE_SUFFIX_HORIZONTAL = "-horizontal";
  private static final String STYLE_SUFFIX_VERTICAL = "-vertical";

  private final CellVector panel;
  private final Orientation orientation;

  private final String stylePrefix;

  private Tab selectedTab = null;

  public TabBar(Orientation orientation) {
    this(DEFAULT_STYLE_PREFIX, orientation);
  }
  
  public TabBar(String stylePrefix, Orientation orientation) {
    Assert.notNull(orientation);

    this.panel = orientation.isVertical() ? new Vertical() : new Horizontal();
    this.orientation = orientation;
    this.stylePrefix = stylePrefix;

    initWidget(panel.asWidget());

    addStyleName(stylePrefix + STYLE_PANEL);
    addStyleName(getStyle(STYLE_PANEL));
    
    panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
    
    sinkEvents(Event.ONCLICK);
  }

  @Override
  public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler) {
    return addHandler(handler, BeforeSelectionEvent.getType());
  }

  @Override
  public void addCaptions(Class<? extends Enum<?>> clazz) {
    addItems(Captions.getCaptions(clazz));
  }

  @Override
  public void addCaptions(String captionKey) {
    addItems(Captions.getCaptions(captionKey));
  }

  public void addItem(SafeHtml html) {
    addItem(html.asString(), true);
  }

  @Override
  public void addItem(String text) {
    insertTab(text, getItemCount());
  }

  public void addItem(String text, boolean asHTML) {
    insertTab(text, asHTML, getItemCount());
  }

  public void addItem(Widget widget) {
    insertTab(widget, getItemCount());
  }

  @Override
  public void addItems(Collection<String> items) {
    Assert.notNull(items);
    for (String it : items) {
      addItem(it);
    }
  }

  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }
  
  public void clear() {
    for (int i = getItemCount() - 1; i >= 0; i--) {
      removeTab(i);
    }
  }

  public void focusTab(int index) {
    checkTabIndex(index, 0);
    DomUtils.setFocus(getWrapper(index), true);
  }

  @Override
  public String getId() {
    return panel.getId();
  }

  @Override
  public String getIdPrefix() {
    return panel.getIdPrefix();
  }

  @Override
  public int getItemCount() {
    return panel.getWidgetCount();
  }
  
  @Override
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
    return panel.getWidgetIndex(selectedTab);
  }

  public Widget getSelectedWidget() {
    if (selectedTab == null) {
      return null;
    }
    return selectedTab.getWidget();
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

  @Override
  public boolean isEmpty() {
    return getItemCount() <= 0;
  }
  
  @Override
  public boolean isIndex(int index) {
    return index >= 0 && index < getItemCount();
  }

  public boolean isTabEnabled(int index) {
    checkTabIndex(index, 0);
    return getWrapper(index).isEnabled();
  }

  public void removeTab(int index) {
    checkTabIndex(index, 0);

    Widget toRemove = panel.getWidget(index);
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

  @Override
  public void setBorderSpacing(int spacing) {
    panel.setBorderSpacing(spacing);
  }

  @Override
  public void setDefaultCellClasses(String classes) {
    panel.setDefaultCellClasses(classes);
  }

  @Override
  public void setDefaultCellStyles(String styles) {
    panel.setDefaultCellStyles(styles);
  }

  @Override
  public void setId(String id) {
    panel.setId(id);
  }

  @Override
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

    Tab wrapper = getWrapper(index);
    wrapper.setEnabled(enabled);
    setStyleName(wrapper.getElement(), stylePrefix + STYLE_DISABLED, !enabled);
  }
  
  protected void insertTabWidget(Widget widget, int beforeIndex) {
    checkInsertBeforeTabIndex(beforeIndex);

    Tab wrapper = new Tab(widget);
    wrapper.addStyleName(stylePrefix + STYLE_ITEM);
    wrapper.addStyleName(getStyle(STYLE_ITEM));

    panel.insert(wrapper, beforeIndex);
  }
  
  private void checkInsertBeforeTabIndex(int beforeIndex) {
    Assert.betweenInclusive(beforeIndex, 0, getItemCount());
  }
  
  private void checkTabIndex(int index, int min) {
    Assert.betweenExclusive(index, min, getItemCount());
  }

  private String getStyle(String stem) {
    return stylePrefix + stem 
        + (orientation.isVertical() ? STYLE_SUFFIX_VERTICAL : STYLE_SUFFIX_HORIZONTAL);
  }

  private int getTabIndex(Widget wrapper) {
    for (int i = 0; i < getItemCount(); i++) {
      if (getWrapper(i) == wrapper) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  private Tab getWrapper(int index) {
    return (Tab) panel.getWidget(index);
  }

  private void setSelectionStyle(Widget item, boolean selected) {
    if (item != null) {
      setStyleName(item.getElement(), stylePrefix + STYLE_SELECTED, selected);
    }
  }
}

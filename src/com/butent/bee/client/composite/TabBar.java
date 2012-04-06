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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasWordWrap;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.FocusImpl;

import com.butent.bee.client.Global;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
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

  public interface Tab extends HasAllKeyHandlers, HasClickHandlers, HasWordWrap {
    boolean hasWordWrap();
  }

  private class ClickDelegatePanel extends Composite implements Tab {
    private Simple focusablePanel;
    private boolean enabled = true;

    ClickDelegatePanel(Widget child) {
      focusablePanel = new Simple(FocusImpl.getFocusImplForPanel().createFocusable());
      focusablePanel.setWidget(child);
      initWidget(focusablePanel);

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

    public Simple getFocusablePanel() {
      return focusablePanel;
    }

    public boolean getWordWrap() {
      if (hasWordWrap()) {
        return ((HasWordWrap) focusablePanel.getWidget()).getWordWrap();
      } else {
        return false;
      }
    }

    public boolean hasWordWrap() {
      return focusablePanel.getWidget() instanceof HasWordWrap;
    }

    public boolean isEnabled() {
      return enabled;
    }

    @Override
    public void onBrowserEvent(Event event) {
      if (!enabled) {
        return;
      }

      switch (DOM.eventGetType(event)) {
        case Event.ONCLICK:
          TabBar.this.selectTabByTabWidget(this);
          break;

        case Event.ONKEYDOWN:
          if (DOM.eventGetKeyCode(event) == KeyCodes.KEY_ENTER) {
            TabBar.this.selectTabByTabWidget(this);
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
        ((HasWordWrap) focusablePanel.getWidget()).setWordWrap(wrap);
      } else {
        Assert.unsupported("Widget does not implement HasWordWrap");
      }
    }
  }

  private static final String DEFAULT_STYLE_PREFIX = "bee-TabBar-";

  private final Horizontal panel = new Horizontal();
  private ClickDelegatePanel selectedTab = null;

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

    setStyleName(first.getElement().getParentElement(), stylePrefix + "first-wrapper");
    setStyleName(rest.getElement().getParentElement(), stylePrefix + "rest-wrapper");
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

  public String getId() {
    return panel.getId();
  }

  public String getIdPrefix() {
    return panel.getIdPrefix();
  }

  public int getIndex(String html) {
    for (int i = 0; i < getItemCount(); i++) {
      if (BeeUtils.same(html, getTabHtml(i))) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  public int getItemCount() {
    return panel.getWidgetCount() - 2;
  }
  
  public List<String> getItems() {
    List<String> items = Lists.newArrayList();
    for (int i = 0; i < getItemCount(); i++) {
      items.add(getTabHtml(i));
    }
    return items;
  }

  public int getSelectedTab() {
    if (selectedTab == null) {
      return -1;
    }
    return panel.getWidgetIndex(selectedTab) - 1;
  }

  public Widget getSelectedWidget() {
    if (selectedTab == null) {
      return null;
    }
    return selectedTab.getFocusablePanel().getWidget();
  }

  public Tab getTab(int index) {
    if (index < 0 || index >= getItemCount()) {
      return null;
    }
    ClickDelegatePanel p = (ClickDelegatePanel) panel.getWidget(index + 1);
    return p;
  }

  public String getTabHtml(int index) {
    if (index < 0 || index >= getItemCount()) {
      return null;
    }

    ClickDelegatePanel delPanel = (ClickDelegatePanel) panel.getWidget(index + 1);
    Simple focusablePanel = delPanel.getFocusablePanel();
    Widget widget = focusablePanel.getWidget();

    if (widget instanceof Html) {
      return ((Html) widget).getHTML();
    } else if (widget instanceof HasText) {
      return ((HasText) widget).getText();
    } else {
      return focusablePanel.getElement().getParentElement().getInnerHTML();
    }
  }

  public Widget getTabWidget(int index)  {
    if (index < 0 || index >= getItemCount()) {
      return null;
    }

    ClickDelegatePanel delPanel = (ClickDelegatePanel) panel.getWidget(index + 1);
    Simple focusablePanel = delPanel.getFocusablePanel();
    return focusablePanel.getWidget();
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

  public boolean isTabEnabled(int index) {
    checkTabIndex(index, 0);
    ClickDelegatePanel delPanel = (ClickDelegatePanel) panel.getWidget(index + 1);
    return delPanel.isEnabled();
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
    if (index == -1) {
      selectedTab = null;
      return true;
    }

    selectedTab = (ClickDelegatePanel) panel.getWidget(index + 1);
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

    ClickDelegatePanel delPanel = (ClickDelegatePanel) panel.getWidget(index + 1);
    delPanel.setEnabled(enabled);
    setStyleName(delPanel.getElement(), stylePrefix + "item-disabled", !enabled);
    setStyleName(delPanel.getElement().getParentElement(), stylePrefix + "wrapper-disabled",
        !enabled);
  }

  public void setTabHTML(int index, SafeHtml html) {
    setTabHTML(index, html.asString());
  }

  public void setTabHTML(int index, String html) {
    checkTabIndex(index, 0);

    ClickDelegatePanel delPanel = (ClickDelegatePanel) panel.getWidget(index + 1);
    Simple focusablePanel = delPanel.getFocusablePanel();
    focusablePanel.setWidget(new Html(html, false));
  }

  public void setTabText(int index, String text) {
    checkTabIndex(index, 0);

    ClickDelegatePanel delPanel = (ClickDelegatePanel) panel.getWidget(index + 1);
    Simple focusablePanel = delPanel.getFocusablePanel();
    focusablePanel.setWidget(new BeeLabel(text, false));
  }

  protected void insertTabWidget(Widget widget, int beforeIndex) {
    checkInsertBeforeTabIndex(beforeIndex);

    ClickDelegatePanel delWidget = new ClickDelegatePanel(widget);
    delWidget.setStyleName(stylePrefix + "item");

    panel.insert(delWidget, beforeIndex + 1);

    setStyleName(DOM.getParent(delWidget.getElement()), stylePrefix + "wrapper", true);
  }

  private void checkInsertBeforeTabIndex(int beforeIndex) {
    Assert.betweenInclusive(beforeIndex, 0, getItemCount());
  }

  private void checkTabIndex(int index, int min) {
    Assert.betweenExclusive(index, min, getItemCount());
  }
  
  private boolean selectTabByTabWidget(Widget tabWidget) {
    int numTabs = panel.getWidgetCount() - 1;

    for (int i = 1; i < numTabs; ++i) {
      if (panel.getWidget(i) == tabWidget) {
        return selectTab(i - 1);
      }
    }
    return false;
  }

  private void setSelectionStyle(Widget item, boolean selected) {
    if (item != null) {
      setStyleName(item.getElement(), stylePrefix + "item-selected", selected);
      setStyleName(DOM.getParent(item.getElement()), stylePrefix + "wrapper-selected", selected);
    }
  }
}

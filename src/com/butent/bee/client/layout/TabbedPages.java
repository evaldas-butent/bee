package com.butent.bee.client.layout;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.layout.client.Layout.AnimationCallback;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.AnimatedLayout;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TabbedPages extends Flow implements AnimatedLayout,
    HasBeforeSelectionHandlers<Integer>, HasSelectionHandlers<Integer> {

  private class PageHandler implements BeforeSelectionHandler<Integer> {
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
      String id = DomUtils.getId(getWidget(event.getItem()));
      if (BeeUtils.isEmpty(id)) {
        return;
      }
      BeeCommand onPage = pageCommands.get(id);
      if (onPage != null) {
        onPage.execute();
      }
    }
  }

  private class Tab extends Simple implements HasClickHandlers {
    private boolean replacingWidget = false;

    public Tab(Widget child) {
      setWidget(child);
      setStyleName(getStylePrefix() + "tab");
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
      return addDomHandler(handler, ClickEvent.getType());
    }
    
    @Override
    public String getIdPrefix() {
      return "tab";
    }

    @Override
    public boolean remove(Widget w) {
      int index = tabs.indexOf(this);
      if (replacingWidget || index < 0) {
        return super.remove(w);
      } else {
        return TabbedPages.this.remove(index);
      }
    }

    public void setSelected(boolean selected) {
      setStyleName(getStylePrefix() + "tabSelected", selected);
    }

    @Override
    public void setWidget(Widget w) {
      replacingWidget = true;
      super.setWidget(w);
      replacingWidget = false;
    }
  }

  public static final String DEFAULT_STYLE_PREFIX = "bee-TabbedPages-";

  private static final String CONTENT_STYLE_SUFFIX = "content";

  private final DeckLayoutPanel deckPanel = new DeckLayoutPanel();
  private final Flow tabBar = new Flow();

  private final List<Tab> tabs = Lists.newArrayList();
  private int selectedIndex = BeeConst.UNDEF;

  private final Map<String, BeeCommand> pageCommands = Maps.newHashMap();

  private final String stylePrefix;
  
  public TabbedPages() {
    this(DEFAULT_STYLE_PREFIX);
  }
  
  public TabbedPages(String stylePrefix) {
    super();
    this.stylePrefix = stylePrefix;

    tabBar.setStyleName(stylePrefix + "tabPanel");
    super.add(tabBar);

    deckPanel.addStyleName(stylePrefix + "contentPanel");
    super.add(deckPanel);

    setStyleName(stylePrefix + "container");
    DomUtils.createId(this, getIdPrefix());
  }

  @Override
  public void add(Widget w) {
    Assert.untouchable(getClass().getName() + ": cannot add widget without tab");
  }

  public void add(Widget child, SafeHtml html) {
    add(child, html.asString(), true);
  }

  public void add(Widget child, String text) {
    insert(child, text, getWidgetCount());
  }

  public void add(Widget child, String text, BeeCommand onPage) {
    add(child, text);
    addCommand(child, onPage);
  }

  public void add(Widget child, String text, boolean asHtml) {
    insert(child, text, asHtml, getWidgetCount());
  }

  public void add(Widget child, Widget tab) {
    insert(child, tab, getWidgetCount());
  }

  public void add(Widget child, Widget tab, BeeCommand onPage) {
    add(child, tab);
    addCommand(child, onPage);
  }

  public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler) {
    return addHandler(handler, BeforeSelectionEvent.getType());
  }

  public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
    return addHandler(handler, SelectionEvent.getType());
  }

  public void addTabStyle(int index, String style) {
    checkIndex(index);
    tabs.get(index).addStyleName(style);
  }
  
  public void animate(int duration) {
    animate(duration, null);
  }

  public void animate(int duration, AnimationCallback callback) {
    deckPanel.animate(duration, callback);
  }

  @Override
  public void clear() {
    Iterator<Widget> it = iterator();
    while (it.hasNext()) {
      it.next();
      it.remove();
    }
  }

  public void forceLayout() {
    deckPanel.forceLayout();
  }

  public int getAnimationDuration() {
    return deckPanel.getAnimationDuration();
  }

  @Override
  public String getIdPrefix() {
    return "tabbed";
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public Widget getTabWidget(int index) {
    checkIndex(index);
    return tabs.get(index).getWidget();
  }

  public Widget getTabWidget(Widget child) {
    checkChild(child);
    return getTabWidget(getWidgetIndex(child));
  }

  @Override
  public Widget getWidget(int index) {
    return deckPanel.getWidget(index);
  }

  @Override
  public int getWidgetCount() {
    return deckPanel.getWidgetCount();
  }

  @Override
  public int getWidgetIndex(Widget child) {
    return deckPanel.getWidgetIndex(child);
  }

  public void insert(Widget child, SafeHtml html, int beforeIndex) {
    insert(child, html.asString(), true, beforeIndex);
  }

  public void insert(Widget child, String text, boolean asHtml, int beforeIndex) {
    Widget contents;
    if (asHtml) {
      contents = new Html(text);
    } else {
      contents = new BeeLabel(text);
    }
    insert(child, contents, beforeIndex);
  }

  public void insert(Widget child, String text, int beforeIndex) {
    insert(child, text, false, beforeIndex);
  }

  public void insert(Widget child, Widget tab, int beforeIndex) {
    insert(child, new Tab(tab), beforeIndex);
  }

  public boolean isAnimationVertical() {
    return deckPanel.isAnimationVertical();
  }
  
  @Override
  public Iterator<Widget> iterator() {
    return deckPanel.iterator();
  }

  @Override
  public boolean remove(int index) {
    if ((index < 0) || (index >= getWidgetCount())) {
      return false;
    }

    Widget child = getWidget(index);

    tabBar.remove(index);
    Tab tab = tabs.remove(index);
    tab.getWidget().removeFromParent();
    
    deckPanel.remove(child);
    child.removeStyleName(stylePrefix + CONTENT_STYLE_SUFFIX);

    if (index == getSelectedIndex()) {
      setSelectedIndex(BeeConst.UNDEF);
      if (getWidgetCount() > 0) {
        selectTab(0);
      }
    } else if (index < getSelectedIndex()) {
      setSelectedIndex(getSelectedIndex() - 1);
    }
    return true;
  }

  @Override
  public boolean remove(Widget w) {
    int index = getWidgetIndex(w);
    if (BeeConst.isUndef(index)) {
      return false;
    }

    return remove(index);
  }

  public void selectTab(int index) {
    selectTab(index, true);
  }

  public void selectTab(int index, boolean fireEvents) {
    checkIndex(index);
    if (index == getSelectedIndex()) {
      return;
    }

    if (fireEvents) {
      BeforeSelectionEvent<Integer> event = BeforeSelectionEvent.fire(this, index);
      if ((event != null) && event.isCanceled()) {
        return;
      }
    }

    if (!BeeConst.isUndef(getSelectedIndex())) {
      tabs.get(getSelectedIndex()).setSelected(false);
    }

    deckPanel.showWidget(index);
    tabs.get(index).setSelected(true);

    setSelectedIndex(index);

    if (fireEvents) {
      SelectionEvent.fire(this, index);
    }
  }

  public void selectTab(Widget child) {
    selectTab(getWidgetIndex(child));
  }

  public void selectTab(Widget child, boolean fireEvents) {
    selectTab(getWidgetIndex(child), fireEvents);
  }

  public void setAnimationDuration(int duration) {
    deckPanel.setAnimationDuration(duration);
  }

  public void setAnimationVertical(boolean isVertical) {
    deckPanel.setAnimationVertical(isVertical);
  }

  public void setTabHtml(int index, SafeHtml html) {
    setTabHtml(index, html.asString());
  }

  public void setTabHtml(int index, String html) {
    checkIndex(index);
    tabs.get(index).setWidget(new Html(html));
  }

  public void setTabText(int index, String text) {
    checkIndex(index);
    tabs.get(index).setWidget(new BeeLabel(text));
  }

  @Override
  protected void doAttachChildren() {
    super.doAttachChildren();
    tabBar.onAttach();
  }

  @Override
  protected void doDetachChildren() {
    super.doDetachChildren();
    tabBar.onDetach();
  }

  private void addCommand(Widget child, BeeCommand onPage) {
    if (onPage == null) {
      return;
    }
    String id = DomUtils.getId(child);
    Assert.notEmpty(id, "page widget has no id");

    if (pageCommands.isEmpty()) {
      addBeforeSelectionHandler(new PageHandler());
    }
    pageCommands.put(id, onPage);
  }

  private void checkChild(Widget child) {
    Assert.nonNegative(getWidgetIndex(child), "Child is not a part of this panel");
  }

  private void checkIndex(int index) {
    Assert.betweenExclusive(index, 0, getWidgetCount(), "Index out of bounds");
  }

  private String getStylePrefix() {
    return stylePrefix;
  }

  private void insert(final Widget child, Tab tab, int beforeIndex) {
    Assert.notNull(child, "widget is null");
    Assert.notNull(tab, "tab is null");
    Assert.betweenInclusive(beforeIndex, 0, getWidgetCount(), "beforeIndex out of bounds");
    
    int index = beforeIndex;
    int x = getWidgetIndex(child);
    if (!BeeConst.isUndef(x)) {
      remove(child);
      if (x < index) {
        index--;
      }
    }

    tabs.add(index, tab);
    tabBar.insert(tab, index);

    tab.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        selectTab(child);
      }
    });

    deckPanel.insert(child, index);
    child.addStyleName(stylePrefix + CONTENT_STYLE_SUFFIX);

    if (BeeConst.isUndef(getSelectedIndex())) {
      selectTab(0);
    } else if (getSelectedIndex() >= index) {
      setSelectedIndex(getSelectedIndex() + 1);
    }
  }

  private void setSelectedIndex(int selectedIndex) {
    this.selectedIndex = selectedIndex;
  }
}

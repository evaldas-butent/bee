package com.butent.bee.client.view.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.FocusImpl;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.HasKeyProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;

import com.butent.bee.client.presenter.DataPresenter;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.HasKeyboardPaging;
import com.butent.bee.shared.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractHasData<T> extends Widget implements HasData<T>,
    HasKeyProvider<T>, Focusable, HasKeyboardPaging, HasKeyboardSelectionPolicy {

  private static class View<T> implements DataView<T> {

    private final AbstractHasData<T> hasData;
    private boolean wasFocused;

    public View(AbstractHasData<T> hasData) {
      this.hasData = hasData;
    }

    public <H extends EventHandler> HandlerRegistration addHandler(H handler, Type<H> type) {
      return hasData.addHandler(handler, type);
    }

    public void render(SafeHtmlBuilder sb, List<T> values, int start,
        SelectionModel<? super T> selectionModel) {
      hasData.renderRowValues(sb, values, start, selectionModel);
    }

    public void replaceAllChildren(List<T> values, SafeHtml html, boolean stealFocus) {
      hasData.isFocused = hasData.isFocused || stealFocus;
      wasFocused = hasData.isFocused;
      hasData.isRefreshing = true;
      hasData.replaceAllChildren(html);
      hasData.isRefreshing = false;
      fireValueChangeEvent();
    }

    public void replaceChildren(List<T> values, int start, SafeHtml html, boolean stealFocus) {
      hasData.isFocused = hasData.isFocused || stealFocus;
      wasFocused = hasData.isFocused;
      hasData.isRefreshing = true;
      hasData.replaceChildren(start, html);
      hasData.isRefreshing = false;
      fireValueChangeEvent();
    }

    public void resetFocus() {
      if (wasFocused) {
        CellBasedWidgetImpl.get().resetFocus(new Scheduler.ScheduledCommand() {
          public void execute() {
            if (!hasData.resetFocusOnCell()) {
              Element elem = hasData.getKeyboardSelectedElement();
              if (elem != null) {
                elem.focus();
              }
            }
          }
        });
      }
    }

    public void setKeyboardSelected(int index, boolean seleted, boolean stealFocus) {
      hasData.isFocused = hasData.isFocused || stealFocus;
      hasData.setKeyboardSelected(index, seleted, stealFocus);
    }

    public void setLoadingState(LoadingState state) {
      hasData.isRefreshing = true;
      hasData.onLoadingStateChanged(state);
      hasData.isRefreshing = false;
    }

    private void fireValueChangeEvent() {
      hasData.fireEvent(new ValueChangeEvent<List<T>>(hasData.getVisibleItems()) {
      });
    }
  }

  private static com.google.gwt.user.client.Element tmpElem;

  static Element convertToElements(Widget widget,
      com.google.gwt.user.client.Element elem, SafeHtml html) {
    DOM.setEventListener(elem, widget);

    elem.setInnerHTML(html.asString());

    DOM.setEventListener(elem, null);

    return elem;
  }

  static void replaceAllChildren(Widget widget, Element childContainer, SafeHtml html) {
    if (!widget.isAttached()) {
      DOM.setEventListener(widget.getElement(), widget);
    }

    childContainer.setInnerHTML(CellBasedWidgetImpl.get().processHtml(html).asString());

    if (!widget.isAttached()) {
      DOM.setEventListener(widget.getElement(), null);
    }
  }

  static void replaceChildren(Element childContainer, Element newChildren, int start) {
    int childCount = childContainer.getChildCount();
    Element toReplace = null;
    if (start < childCount) {
      toReplace = childContainer.getChild(start).cast();
    }

    int count = newChildren.getChildCount();
    for (int i = 0; i < count; i++) {
      if (toReplace == null) {
        childContainer.appendChild(newChildren.getChild(0));
      } else {
        Element nextSibling = toReplace.getNextSiblingElement();
        childContainer.replaceChild(newChildren.getChild(0), toReplace);
        toReplace = nextSibling;
      }
    }
  }

  private static com.google.gwt.user.client.Element getTmpElem() {
    if (tmpElem == null) {
      tmpElem = Document.get().createDivElement().cast();
    }
    return tmpElem;
  }

  boolean isFocused;

  private char accessKey = 0;

  private boolean isRefreshing;

  private final DataPresenter<T> presenter;
  private HandlerRegistration selectionManagerReg; 
  private int tabIndex;

  public AbstractHasData(Element elem, final int pageSize, final ProvidesKey<T> keyProvider) {
    setElement(elem);
    this.presenter = new DataPresenter<T>(this, new View<T>(this), pageSize, keyProvider);

    Set<String> eventTypes = new HashSet<String>();
    eventTypes.add("focus");
    eventTypes.add("blur");
    eventTypes.add("keydown");
    eventTypes.add("keyup");
    eventTypes.add("click");
    eventTypes.add("mousedown");
    CellBasedWidgetImpl.get().sinkEvents(this, eventTypes);

    selectionManagerReg = 
      addCellPreviewHandler(DefaultSelectionEventManager.<T>createDefaultManager());
  }

  public HandlerRegistration addCellPreviewHandler(CellPreviewEvent.Handler<T> handler) {
    return presenter.addCellPreviewHandler(handler);
  }

  public HandlerRegistration addLoadingStateChangeHandler(LoadingStateChangeEvent.Handler handler) {
    return presenter.addLoadingStateChangeHandler(handler);
  }

  public HandlerRegistration addRangeChangeHandler(RangeChangeEvent.Handler handler) {
    return presenter.addRangeChangeHandler(handler);
  }

  public HandlerRegistration addRowCountChangeHandler(RowCountChangeEvent.Handler handler) {
    return presenter.addRowCountChangeHandler(handler);
  }

  public char getAccessKey() {
    return accessKey;
  }

  public KeyboardPagingPolicy getKeyboardPagingPolicy() {
    return presenter.getKeyboardPagingPolicy();
  }

  public KeyboardSelectionPolicy getKeyboardSelectionPolicy() {
    return presenter.getKeyboardSelectionPolicy();
  }

  public ProvidesKey<T> getKeyProvider() {
    return presenter.getKeyProvider();
  }

  public final int getPageSize() {
    return getVisibleRange().getLength();
  }

  public final int getPageStart() {
    return getVisibleRange().getStart();
  }

  public Element getRowContainer() {
    presenter.flush();
    return getChildContainer();
  }

  public int getRowCount() {
    return presenter.getRowCount();
  }

  public SelectionModel<? super T> getSelectionModel() {
    return presenter.getSelectionModel();
  }

  public int getTabIndex() {
    return tabIndex;
  }

  public T getVisibleItem(int indexOnPage) {
    checkRowBounds(indexOnPage);
    return presenter.getVisibleItem(indexOnPage);
  }

  public int getVisibleItemCount() {
    return presenter.getVisibleItemCount();
  }

  public List<T> getVisibleItems() {
    return presenter.getVisibleItems();
  }

  public Range getVisibleRange() {
    return presenter.getVisibleRange();
  }

  public boolean isRowCountExact() {
    return presenter.isRowCountExact();
  }

  @Override
  public final void onBrowserEvent(Event event) {
    CellBasedWidgetImpl.get().onBrowserEvent(this, event);

    if (isRefreshing) {
      return;
    }

    EventTarget eventTarget = event.getEventTarget();
    if (!Element.is(eventTarget) || !getElement().isOrHasChild(Element.as(eventTarget))) {
      return;
    }
    super.onBrowserEvent(event);

    String eventType = event.getType();
    if ("focus".equals(eventType)) {
      isFocused = true;
      onFocus();
    } else if ("blur".equals(eventType)) {
      isFocused = false;
      onBlur();
    } else if ("keydown".equals(eventType) && !isKeyboardNavigationSuppressed()) {
      isFocused = true;

      int keyCode = event.getKeyCode();
      switch (keyCode) {
        case KeyCodes.KEY_DOWN:
          presenter.keyboardNext();
          event.preventDefault();
          return;
        case KeyCodes.KEY_UP:
          presenter.keyboardPrev();
          event.preventDefault();
          return;
        case KeyCodes.KEY_PAGEDOWN:
          presenter.keyboardNextPage();
          event.preventDefault();
          return;
        case KeyCodes.KEY_PAGEUP:
          presenter.keyboardPrevPage();
          event.preventDefault();
          return;
        case KeyCodes.KEY_HOME:
          presenter.keyboardHome();
          event.preventDefault();
          return;
        case KeyCodes.KEY_END:
          presenter.keyboardEnd();
          event.preventDefault();
          return;
        case 32:
          event.preventDefault();
          return;
      }
    }

    onBrowserEvent2(event);
  }

  public void redraw() {
    presenter.redraw();
  }

  public void setAccessKey(char key) {
    this.accessKey = key;
    setKeyboardSelected(getKeyboardSelectedRow(), true, false);
  }

  public void setFocus(boolean focused) {
    Element elem = getKeyboardSelectedElement();
    if (elem != null) {
      if (focused) {
        elem.focus();
      } else {
        elem.blur();
      }
    }
  }

  public void setKeyboardPagingPolicy(KeyboardPagingPolicy policy) {
    presenter.setKeyboardPagingPolicy(policy);
  }

  public void setKeyboardSelectionPolicy(KeyboardSelectionPolicy policy) {
    presenter.setKeyboardSelectionPolicy(policy);
  }

  public final void setPageSize(int pageSize) {
    setVisibleRange(getPageStart(), pageSize);
  }

  public final void setPageStart(int pageStart) {
    setVisibleRange(pageStart, getPageSize());
  }

  public final void setRowCount(int count) {
    setRowCount(count, true);
  }

  public void setRowCount(int size, boolean isExact) {
    presenter.setRowCount(size, isExact);
  }

  public final void setRowData(List<? extends T> values) {
    setRowCount(values.size());
    setVisibleRange(0, values.size());
    setRowData(0, values);
  }

  public void setRowData(int start, List<? extends T> values) {
    presenter.setRowData(start, values);
  }

  public void setSelectionModel(SelectionModel<? super T> selectionModel) {
    presenter.setSelectionModel(selectionModel);
  }

  public void setSelectionModel(SelectionModel<? super T> selectionModel,
      CellPreviewEvent.Handler<T> selectionEventManager) {
    if (this.selectionManagerReg != null) {
      this.selectionManagerReg.removeHandler();
      this.selectionManagerReg = null;
    }

    if (selectionEventManager != null) {
      this.selectionManagerReg = addCellPreviewHandler(selectionEventManager);
    }

    setSelectionModel(selectionModel);
  }

  public void setTabIndex(int index) {
    this.tabIndex = index;
    setKeyboardSelected(getKeyboardSelectedRow(), true, false);
  }

  public final void setVisibleRange(int start, int length) {
    setVisibleRange(new Range(start, length));
  }

  public void setVisibleRange(Range range) {
    presenter.setVisibleRange(range);
  }

  public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
    presenter.setVisibleRangeAndClearData(range, forceRangeChangeEvent);
  }

  protected boolean cellConsumesEventType(Cell<?> cell, String eventType) {
    Set<String> consumedEvents = cell.getConsumedEvents();
    return consumedEvents != null && consumedEvents.contains(eventType);
  }

  protected void checkRowBounds(int row) {
    Assert.isTrue(isRowWithinBounds(row), "Row index: " + row + ", Row size: " + getRowCount());
  }

  protected Element convertToElements(SafeHtml html) {
    return convertToElements(this, getTmpElem(), html);
  }

  protected abstract boolean dependsOnSelection();

  protected abstract Element getChildContainer();

  protected abstract Element getKeyboardSelectedElement();

  protected int getKeyboardSelectedRow() {
    return presenter.getKeyboardSelectedRow();
  }

  protected Object getValueKey(T value) {
    ProvidesKey<T> keyProvider = getKeyProvider();
    return (keyProvider == null || value == null) ? value : keyProvider.getKey(value);
  }

  protected abstract boolean isKeyboardNavigationSuppressed();

  protected boolean isRowWithinBounds(int row) {
    return row >= 0 && row < presenter.getVisibleItemCount();
  }

  protected void onBlur() {
  }

  protected abstract void onBrowserEvent2(Event event);

  protected void onFocus() {
  }

  protected void onLoadingStateChanged(LoadingState state) {
    fireEvent(new LoadingStateChangeEvent(state));
  }

  @Override
  protected void onUnload() {
    isFocused = false;
    super.onUnload();
  }

  protected abstract void renderRowValues(SafeHtmlBuilder sb, List<T> values,
      int start, SelectionModel<? super T> selectionModel);

  protected void replaceAllChildren(SafeHtml html) {
    replaceAllChildren(this, getChildContainer(), html);
  }

  protected void replaceChildren(int start, SafeHtml html) {
    Element newChildren = convertToElements(html);
    replaceChildren(getChildContainer(), newChildren, start);
  }

  protected abstract boolean resetFocusOnCell();

  protected void setFocusable(Element elem, boolean focusable) {
    if (focusable) {
      FocusImpl focusImpl = FocusImpl.getFocusImplForWidget();
      com.google.gwt.user.client.Element rowElem = elem.cast();
      focusImpl.setTabIndex(rowElem, getTabIndex());
      if (accessKey != 0) {
        focusImpl.setAccessKey(rowElem, accessKey);
      }
    } else {
      elem.setTabIndex(-1);
      elem.removeAttribute("tabIndex");
      elem.removeAttribute("accessKey");
    }
  }

  protected abstract void setKeyboardSelected(int index, boolean selected, boolean stealFocus);

  final HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<T>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  native void adopt(Widget child) /*-{
    child.@com.google.gwt.user.client.ui.Widget::setParent(Lcom/google/gwt/user/client/ui/Widget;)(this);
  }-*/;

  native void doAttach(Widget child) /*-{
    child.@com.google.gwt.user.client.ui.Widget::onAttach()();
  }-*/;

  native void doDetach(Widget child) /*-{
    child.@com.google.gwt.user.client.ui.Widget::onDetach()();
  }-*/;

  DataPresenter<T> getPresenter() {
    return presenter;
  }

  void showOrHide(Element element, boolean show) {
    if (element == null) {
      return;
    }
    if (show) {
      element.getStyle().clearDisplay();
    } else {
      element.getStyle().setDisplay(Display.NONE);
    }
  }
}

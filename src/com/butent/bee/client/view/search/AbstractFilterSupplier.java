package com.butent.bee.client.view.search;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcParameter;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.CloseEvent;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public abstract class AbstractFilterSupplier implements HasViewName, HasOptions {

  protected enum SupplierAction implements HasCaption {
    ALL("Visi"),
    CLEAR(Localized.constants.clear()),
    COMMIT(Localized.constants.doFilter()),
    CANCEL(Localized.constants.cancel());

    private final String caption;

    private SupplierAction(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    private String getStyleSuffix() {
      return name().toLowerCase();
    }
  }

  protected static final String NULL_VALUE_LABEL = "[tuščia]";

  protected static final String DEFAULT_STYLE_PREFIX = "bee-FilterSupplier-";
  
  private static final String BIN_SIZE_CELL_STYLE_SUFFIX = "binSizeCell";

  private final String viewName;
  private final BeeColumn column;

  private String options;

  private Filter filter = null;
  private boolean filterChanged = false;

  private Popup dialog = null;

  private Filter effectiveFilter = null;

  private String counterId = null;
  private int counterValue = 0;

  private final List<Integer> selectedItems = Lists.newArrayList();

  private String displayId = null;

  public AbstractFilterSupplier(String viewName, BeeColumn column, String options) {
    this.viewName = viewName;
    this.column = column;
    this.options = options;
  }

  public abstract String getDisplayHtml();

  public String getDisplayTitle() {
    if (Global.isDebug()) {
      return (getFilter() == null) ? null : getFilter().toString();
    } else {
      return getDisplayHtml();
    }
  }

  public Filter getFilter() {
    return filter;
  }

  @Override
  public String getOptions() {
    return options;
  }

  @Override
  public String getViewName() {
    return viewName;
  }

  public boolean isEmpty() {
    return getFilter() == null;
  }

  public abstract void onRequest(Element target, NotificationListener notificationListener,
      Callback<Boolean> callback);
  
  public abstract Filter parse(String value);

  public boolean reset() {
    clearSelection();
    setCounter(0);

    if (getFilter() == null) {
      return false;
    } else {
      setFilter(null);
      return true;
    }
  }

  public void setEffectiveFilter(Filter effectiveFilter) {
    this.effectiveFilter = effectiveFilter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  protected void addBinSize(HtmlTable display, int row, int col, String text) {
    display.setText(row, col, text);
    display.getCellFormatter().addStyleName(row, col,
        getStylePrefix() + BIN_SIZE_CELL_STYLE_SUFFIX);
  }

  protected void clearDisplay() {
    HtmlTable display = getDisplayAsTable();
    if (display == null) {
      return;
    }

    for (int row : getSelectedItems()) {
      display.getRowFormatter().removeStyleName(row, getStyleSelected());
    }
  }

  protected void clearSelection() {
    selectedItems.clear();
  }

  protected void closeDialog() {
    if (getDialog() != null) {
      getDialog().close();
      setDialog(null);
    }
  }

  protected void countSelection() {
    setCounter(selectedItems.size());
  }

  protected HtmlTable createDisplay(boolean addSelectionHandler) {
    final HtmlTable display = new HtmlTable();
    display.addStyleName(getStylePrefix() + "display");

    if (addSelectionHandler) {
      Binder.addClickHandler(display, new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Element target = EventUtils.getEventTargetElement(event);
          TableRowElement rowElement = DomUtils.getParentRow(target, true);

          if (rowElement != null && display.getElement().isOrHasChild(rowElement)) {
            int rc = display.getRowCount();
            if (EventUtils.hasModifierKey(event.getNativeEvent()) && rc > 1) {
              for (int i = 0; i < rc; i++) {
                invertSelection(i, display.getRow(i));
              }
              
            } else {
              invertSelection(rowElement.getRowIndex(), rowElement);
            }

            countSelection();
          }
        }
      });
    }

    setDisplayId(display.getId());
    return display;
  }

  protected void decrementCounter() {
    setCounter(getCounterValue() - 1);
  }

  protected boolean deselect(Integer item) {
    return selectedItems.remove(item);
  }

  protected void doAction(SupplierAction action) {
    switch (action) {
      case ALL:
        doAll();
        break;

      case CLEAR:
        doClear();
        break;

      case COMMIT:
        doCommit();
        break;

      case CANCEL:
        doCancel();
        break;
    }
  }

  protected void doAll() {
  }

  protected void doCancel() {
    closeDialog();
  }

  protected void doClear() {
    clearSelection();
    setCounter(0);
  }

  protected void doCommit() {
  }

  protected abstract List<SupplierAction> getActions();

  protected BeeColumn getColumn() {
    return column;
  }

  protected String getColumnId() {
    return (getColumn() == null) ? null : getColumn().getId();
  }

  protected String getColumnLabel() {
    return (getColumn() == null) ? null : LocaleUtils.getLabel(getColumn());
  }

  protected ValueType getColumnType() {
    return (getColumn() == null) ? null : getColumn().getType();
  }

  protected Widget getCommandWidgets(boolean addCounter) {
    Flow panel = new Flow();
    panel.addStyleName(getStylePrefix() + "commandPanel");

    List<SupplierAction> actions = getActions();
    for (final SupplierAction action : actions) {
      BeeButton actionWidget = new BeeButton(action.getCaption());
      actionWidget.addStyleName(getStylePrefix() + action.getStyleSuffix());

      actionWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          doAction(action);
        }
      });

      panel.add(actionWidget);
    }

    if (addCounter) {
      Html counter = new Html();
      counter.addStyleName(getStylePrefix() + "counter");

      panel.add(counter);
      setCounterId(counter.getId());
    }

    return panel;
  }

  protected Widget getDialogChild(String id) {
    return DomUtils.getChildQuietly(getDialog(), id);
  }

  protected String getDialogStyle() {
    return getStylePrefix() + "dialog";
  }

  protected HtmlTable getDisplayAsTable() {
    Widget widget = getDialogChild(getDisplayId());
    if (widget instanceof HtmlTable) {
      return (HtmlTable) widget;
    } else {
      return null;
    }
  }

  protected String getDisplayId() {
    return displayId;
  }

  protected Filter getEffectiveFilter() {
    return effectiveFilter;
  }

  protected void getHistogram(final Callback<SimpleRowSet> callback) {
    List<Property> props = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, getViewName());
    if (getEffectiveFilter() != null) {
      PropertyUtils.addProperties(props, Service.VAR_VIEW_WHERE, getEffectiveFilter().serialize());
    }
    String columns = NameUtils.join(getHistogramColumns());
    PropertyUtils.addProperties(props, Service.VAR_VIEW_COLUMNS, columns);

    List<String> order = getHistogramOrder();
    if (!BeeUtils.isEmpty(order)) {
      PropertyUtils.addProperties(props, Service.VAR_VIEW_ORDER, NameUtils.join(order));
    }

    if (!BeeUtils.isEmpty(getOptions())) {
      PropertyUtils.addProperties(props, Service.VAR_OPTIONS, getOptions());
    }

    ParameterList params = new ParameterList(Service.HISTOGRAM, RpcParameter.Section.DATA, props);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (Queries.checkResponse(Service.HISTOGRAM, getViewName(), response, SimpleRowSet.class,
            callback)) {
          SimpleRowSet rs = SimpleRowSet.restore((String) response.getResponse());
          callback.onSuccess(rs);
        }
      }
    });
  }

  protected List<String> getHistogramColumns() {
    return Lists.newArrayList(getColumnId());
  }

  protected List<String> getHistogramOrder() {
    return Lists.newArrayList(getColumnId());
  }

  protected List<Integer> getSelectedItems() {
    return selectedItems;
  }

  protected String getStylePrefix() {
    return DEFAULT_STYLE_PREFIX;
  }

  protected void incrementCounter() {
    setCounter(getCounterValue() + 1);
  }

  protected void invertSelection(int index, Element element) {
    boolean wasSelected = isSelected(index);

    if (wasSelected) {
      element.removeClassName(getStyleSelected());
      deselect(index);
    } else {
      element.addClassName(getStyleSelected());
      select(index);
    }
  }

  protected boolean isSelected(Integer item) {
    return selectedItems.contains(item);
  }

  protected boolean isSelectionEmpty() {
    return selectedItems.isEmpty();
  }

  protected String messageAllEmpty(String count) {
    return BeeUtils.joinWords(getColumnLabel() + ":", "visos reikšmės tuščios",
        BeeUtils.bracket(count));
  }

  protected String messageOneValue(String value, String count) {
    return BeeUtils.joinWords(getColumnLabel() + ":", "visos reikšmės lygios", value,
        BeeUtils.bracket(count));
  }

  protected void openDialog(Element target, Widget widget, final Callback<Boolean> callback) {
    Popup popup = new Popup(OutsideClick.CLOSE, getDialogStyle());

    popup.setWidget(widget);
    popup.setHideOnEscape(true);

    popup.addCloseHandler(new CloseEvent.Handler() {
      @Override
      public void onClose(CloseEvent event) {
        callback.onSuccess(filterChanged());
      }
    });

    setDialog(popup);
    setFilterChanged(false);

    popup.showOnTop(target, 5);
  }

  protected void select(Integer item) {
    selectedItems.add(item);
  }

  protected void selectDisplayRow(HtmlTable display, int row) {
    display.getRowFormatter().addStyleName(row, getStyleSelected());
  }

  protected void setCounter(int count) {
    if (count != getCounterValue()) {
      setCounterValue(count);

      if (!BeeUtils.isEmpty(getCounterId()) && getDialog() != null) {
        Widget counter = DomUtils.getChildQuietly(getDialog(), getCounterId());
        if (counter != null) {
          String text = (count > 0) ? BeeUtils.toString(count) : BeeConst.STRING_EMPTY;
          counter.getElement().setInnerText(text);
        }
      }
    }
  }

  protected void setDisplayId(String displayId) {
    this.displayId = displayId;
  }

  protected void update(Filter newFilter) {
    setFilterChanged(!Objects.equal(getFilter(), newFilter));

    setFilter(newFilter);
    closeDialog();
  }

  protected Widget wrapDisplay(HtmlTable display, boolean addCounter) {
    Flow container = new Flow();
    container.addStyleName(getStylePrefix() + "container");

    Flow panel = new Flow();
    panel.addStyleName(getStylePrefix() + "panel");
    panel.add(display);

    container.add(panel);
    container.add(getCommandWidgets(addCounter));

    return container;
  }

  private boolean filterChanged() {
    return filterChanged;
  }

  private String getCounterId() {
    return counterId;
  }

  private int getCounterValue() {
    return counterValue;
  }

  private Popup getDialog() {
    return dialog;
  }

  private String getStyleSelected() {
    return getStylePrefix() + "selected";
  }

  private void setCounterId(String counterId) {
    this.counterId = counterId;
  }

  private void setCounterValue(int counterValue) {
    this.counterValue = counterValue;
  }

  private void setDialog(Popup dialog) {
    this.dialog = dialog;
  }

  private void setFilterChanged(boolean filterChanged) {
    this.filterChanged = filterChanged;
  }
}

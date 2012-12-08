package com.butent.bee.client.view.search;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcParameter;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.Modality;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public abstract class AbstractFilterSupplier implements HasViewName, HasOptions {
  
  protected static final String NULL_VALUE_LABEL = "[tuštuma]";

  private static final String STYLE_PREFIX = "bee-FilterSupplier-";
  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";

  private final String viewName;
  private final Filter immutableFilter;

  private final BeeColumn column;

  private String options;

  private Filter filter = null;
  private boolean filterChanged = false;
  
  private Popup dialog = null;

  public AbstractFilterSupplier(String viewName, Filter immutableFilter, BeeColumn column,
      String options) {
    this.viewName = viewName;
    this.immutableFilter = immutableFilter;
    this.column = column;
    this.options = options;
  }

  public abstract String getDisplayHtml();

  public String getDisplayTitle() {
    return (getFilter() == null) ? null : getFilter().toString();
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

  public boolean reset() {
    if (getFilter() == null) {
      return false;
    } else {
      setFilter(null);
      return true;
    }
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }
  
  protected void closeDialog() {
    if (getDialog() != null) {
      getDialog().hide();
      setDialog(null);
    }
  }
  
  protected void doFilterCommand() {
  }
  
  protected void doResetCommand() {
  }

  protected BeeColumn getColumn() {
    return column;
  }
  
  protected String getColumnId() {
    return getColumn().getId();
  }
  
  protected String getColumnLabel() {
    return getColumn().getLabel();
  }
  
  protected ValueType getColumnType() {
    return getColumn().getType();
  }

  protected Widget getCommandWidgets() {
    Flow panel = new Flow();
    
    BeeButton resetWidget = new BeeButton("Išvalyti");
    resetWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doResetCommand();
      }
    });
    panel.add(resetWidget);

    BeeButton filterWidget = new BeeButton("Filtruoti");
    filterWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doFilterCommand();
      }
    });
    panel.add(filterWidget);
    
    return panel;
  }
  
  protected String getDialogStyle() {
    return STYLE_DIALOG;
  }

  protected void getHistogram(final Callback<SimpleRowSet> callback) {
    List<Property> props = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, getViewName());
    if (getImmutableFilter() != null) {
      PropertyUtils.addProperties(props, Service.VAR_VIEW_WHERE, getImmutableFilter().serialize());
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
  
  protected String messageAllEmpty(String count) {
    return BeeUtils.joinWords(getColumnLabel() + ":", "visos reikšmės tuščios",
        BeeUtils.bracket(count));
  }

  protected String messageOneValue(String value, String count) {
    return BeeUtils.joinWords(getColumnLabel() + ":", "visos reikšmės lygios", value,
        BeeUtils.bracket(count));
  }
  
  protected void openDialog(Element target, Widget widget, final Callback<Boolean> callback) {
    Popup popup = new Popup(OutsideClick.CLOSE, Modality.MODAL, getDialogStyle());
    
    popup.setWidget(widget);
    popup.setHideOnEscape(true);
    
    popup.addCloseHandler(new CloseHandler<Popup>() {
      @Override
      public void onClose(CloseEvent<Popup> event) {
        callback.onSuccess(filterChanged());
      }
    });
    
    setDialog(popup);
    setFilterChanged(false);

    popup.showOnTop(target, 5);
  }

  protected void update(Filter newFilter) {
    setFilterChanged(!Objects.equal(getFilter(), newFilter));

    setFilter(newFilter);
    closeDialog();
  }

  private boolean filterChanged() {
    return filterChanged;
  }

  private Popup getDialog() {
    return dialog;
  }

  private Filter getImmutableFilter() {
    return immutableFilter;
  }

  private void setDialog(Popup dialog) {
    this.dialog = dialog;
  }

  private void setFilter(Filter filter) {
    this.filter = filter;
  }
  
  private void setFilterChanged(boolean filterChanged) {
    this.filterChanged = filterChanged;
  }
}

package com.butent.bee.client.modules.service;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.css.values.Display;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.service.ServiceConstants.ObjectStatus;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class ObjectInvoicesGrid extends AbstractGridInterceptor {

  private static final String FILTER_KEY = "f1";
  private static final LocalizableConstants localizableConstants = Localized.getConstants();

  private final String idColumnName;

  private boolean checked;
  private Long pendingId;
  private CheckBox showAllCheckBox;

  ObjectInvoicesGrid() {
    this.idColumnName = Data.getIdColumn(VIEW_SERVICE_INVOICES);
    this.checked = false;
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    renderHeaderView(presenter);
    maybeRefresh(presenter, getPendingId());
    setPendingId(null);
  }

  @Override
  public void afterRender(GridView gridView, RenderingEvent event) {
    super.afterRender(gridView, event);
    displayElements(getGridPresenter());
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    if (action == Action.ADD) {
      InvoiceBuilder.start(getGridView());
      return false;
    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public void beforeRefresh(GridPresenter presenter) {
    FormView form = ViewHelper.getForm(presenter.getMainView().asWidget());
    if (form != null && !BeeUtils.isEmpty(form.getViewName()) && form.getActiveRow() != null) {
      DataInfo dataInfo = Data.getDataInfo(form.getViewName());

      if (dataInfo != null) {
        int objStatus = BeeUtils.unbox(
            form.getActiveRow().getInteger(dataInfo.getColumnIndex(COL_OBJECT_STATUS)));

        int filteredValue = ObjectStatus.SERVICE_OBJECT.ordinal() == objStatus
            ? 1 : 2;

        Filter filter =
            Filter.isEqual(TradeConstants.COL_TRADE_KIND,
                Value.getValue(Integer.valueOf(filteredValue)));

        if (filteredValue > 1 || isChecked()) {
          filter =
              Filter.or(filter, Filter.isEqual(TradeConstants.COL_TRADE_KIND, Value
                  .getValue(Integer.valueOf(2))));
        }

        presenter.getDataProvider().setParentFilter(COL_OBJECT_STATUS, filter);
      }
    }
  }

  @Override
  public Map<String, Filter> getInitialParentFilters() {
    return ImmutableMap.of(FILTER_KEY, getFilter(getPendingId()));
  }

  @Override
  public GridInterceptor getInstance() {
    return new ObjectInvoicesGrid();
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    if (getGridPresenter() == null) {
      setPendingId(event.getRowId());
    } else {
      maybeRefresh(getGridPresenter(), event.getRowId());
    }
  }

  private void displayElements(GridPresenter presenter) {

    FormView form = ViewHelper.getForm(presenter.getMainView().asWidget());
    if (form != null && !BeeUtils.isEmpty(form.getViewName()) && form.getActiveRow() != null) {
      DataInfo dataInfo = Data.getDataInfo(form.getViewName());

      if (dataInfo != null) {
        Integer objStatus =
            form.getActiveRow().getInteger(dataInfo.getColumnIndex(COL_OBJECT_STATUS));

        if (BeeUtils.unbox(objStatus) != ObjectStatus.SERVICE_OBJECT.ordinal()
            && showAllCheckBox != null) {
          StyleUtils.setDisplay(showAllCheckBox, Display.NONE);
        } else if (showAllCheckBox != null) {
          StyleUtils.setDisplay(showAllCheckBox, Display.INLINE);
        }
      }
    }
  }

  private Filter getFilter(Long parentId) {
    Filter filter;

    if (DataUtils.isId(parentId)) {
      filter = Filter.in(idColumnName, VIEW_MAINTENANCE, COL_MAINTENANCE_INVOICE,
          Filter.equals(COL_SERVICE_OBJECT, parentId));
    } else {
      filter = Filter.isFalse();
    }

    return filter;
  }

  private Long getPendingId() {
    return pendingId;
  }

  private boolean isChecked() {
    return checked;
  }

  private void maybeRefresh(GridPresenter presenter, Long parentId) {
    if (presenter != null) {
      Filter filter = getFilter(parentId);
      boolean changed = presenter.getDataProvider().setParentFilter(FILTER_KEY, filter);

      if (changed) {
        presenter.handleAction(Action.REFRESH);
      }
    }
  }

  private void setChecked(boolean checked) {
    this.checked = checked;
  }

  private void renderHeaderView(final GridPresenter presenter) {
    HeaderView header = presenter.getHeader();
    header.clearCommandPanel();

    showAllCheckBox = new CheckBox(localizableConstants.svcActionShowFromProjects());

    header.addCommandItem(showAllCheckBox);
    showAllCheckBox.setChecked(isChecked());
    showAllCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        Assert.notNull(event.getValue());

        if (event.getValue().booleanValue()) {
          setChecked(true);
        } else {
          setChecked(false);
        }

        presenter.handleAction(Action.REFRESH);
      }
    });
  }

  private void setPendingId(Long pendingId) {
    this.pendingId = pendingId;
  }
}

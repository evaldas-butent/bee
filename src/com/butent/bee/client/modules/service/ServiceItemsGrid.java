package com.butent.bee.client.modules.service;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.FormView;
import com.google.common.collect.ImmutableMap;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.PRM_COMPANY;
import static com.butent.bee.shared.modules.cars.CarsConstants.COL_RESERVE;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.COL_COMPANY;
import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.modules.classifiers.ItemsPicker;
import com.butent.bee.client.modules.orders.OrderItemsGrid;
import com.butent.bee.client.modules.transport.InvoiceCreator;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;
import java.util.Objects;

public class ServiceItemsGrid extends OrderItemsGrid {

  private ServiceItemsPicker picker;

  @Override
  public void afterCreateEditor(String source, Editor editor, boolean embedded) {
    if (BeeUtils.same(source, COL_REPAIRER) && editor instanceof DataSelector) {
      Long company = Global.getParameterRelation(PRM_COMPANY);

      if (DataUtils.isId(company)) {
        ((DataSelector) editor).setAdditionalFilter(Filter.and(Filter.equals(COL_COMPANY, company),
            Filter.or(Filter.isNull(PayrollConstants.COL_DATE_OF_DISMISSAL),
                Filter.compareWithValue(PayrollConstants.COL_DATE_OF_DISMISSAL, Operator.GT,
                    new DateValue(new JustDate())))));
      }
    }

    super.afterCreateEditor(source, editor, embedded);
  }

  @Override
  public ItemsPicker ensurePicker() {
    if (picker == null) {
      picker = new ServiceItemsPicker(Module.SERVICE);
      picker.addSelectionHandler(this);
    }
    return picker;
  }

  @Override
  public Map<String, String> getAdditionalColumns() {
    FormView parentForm = ViewHelper.getForm(getGridView());

    Long repairer = BeeKeeper.getUser().getUserData().getCompanyPerson();
    if (parentForm != null && DataUtils.isId(parentForm.getLongValue(COL_REPAIRER))) {
      repairer = Data.getLong("ServiceMaintenance", parentForm.getActiveRow(),
        "RepairerCompanyPerson");
    }
    return ImmutableMap.of(COL_SERVICE_OBJECT, BeeConst.STRING_EMPTY,
        COL_REPAIRER, BeeUtils.toString(repairer));
  }

  @Override
  public GridInterceptor getInstance() {
    return new ServiceItemsGrid();
  }

  @Override
  public String getParentDateColumnName() {
    return COL_MAINTENANCE_DATE;
  }

  @Override
  public String getParentRelationColumnName() {
    return COL_SERVICE_MAINTENANCE;
  }

  @Override
  public String getParentViewName() {
    return TBL_SERVICE_MAINTENANCE;
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (Objects.equals(event.getColumnId(), COL_RESERVE)) {
      event.consume();
      IsRow row = event.getRowValue();
      String value = Data.getString(getViewName(), row, COL_RESERVE);

      Queries.updateCellAndFire(getViewName(), row.getId(), row.getVersion(), COL_RESERVE,
          value, BeeUtils.toString(!BeeUtils.toBoolean(value)));
    } else {
      super.onEditStart(event);
    }
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    setOrderForm(event.getRowId());
    getInvoice().clear();

    if (DataUtils.isId(getOrderForm())) {
      getInvoice().add(new InvoiceCreator(VIEW_SERVICE_SALES,
          Filter.equals(COL_SERVICE_MAINTENANCE, getOrderForm())));
    }
  }

  @Override
  public boolean validParentState(IsRow parentRow) {
    String endingDate = parentRow.getString(Data.getColumnIndex(getParentViewName(),
        COL_ENDING_DATE));
    return BeeUtils.isEmpty(endingDate);
  }

  @Override
  protected double calculateItemPrice(Pair<Double, Double> pair, BeeRow row, int unpackingIdx,
      int qtyIndex) {
    double price = super.calculateItemPrice(pair, row, unpackingIdx, qtyIndex);

    if (picker.isCheckedFilterService()) {
      IsRow parentRow = ViewHelper.getFormRow(getGridView());

      if (parentRow != null) {
        price = ServiceUtils.calculateServicePrice(price, parentRow);
      }
    }
    return price;
  }
}
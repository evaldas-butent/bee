package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.HasDataProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RelationUtils;
import com.butent.bee.client.dialog.DialogCallback;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.edit.SelectorEvent;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

class SelectorHandler implements SelectorEvent.Handler {

  SelectorHandler() {
    super();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (BeeUtils.same(event.getRelatedViewName(), VIEW_EXTENDED_PROPERTIES)) {
      handleExtendedProperties(event);
    } else if (BeeUtils.same(event.getRelatedViewName(), CommonsConstants.VIEW_COMPANIES)) {
      handleCompany(event);
    } else if (BeeUtils.same(event.getRelatedViewName(), TransportConstants.VIEW_VEHICLES)) {
      handleVehicle(event);
    }
  }

  private void chooseVehicle(final DataView dataView, final BeeRowSet rowSet, String companyName,
      final long companyId) {

    List<String> options = Lists.newArrayList();

    int numberIndex = rowSet.getColumnIndex(TransportConstants.COL_NUMBER);
    int parentModelIndex = rowSet.getColumnIndex(TransportConstants.COL_PARENT_MODEL_NAME);
    int modelIndex = rowSet.getColumnIndex(TransportConstants.COL_MODEL_NAME);

    for (IsRow row : rowSet.getRows()) {
      options.add(BeeUtils.concat(1, row.getString(numberIndex),
          row.getString(parentModelIndex), row.getString(modelIndex)));
    }
    options.add("Nauja");

    Global.choice("Pasirinkite transporto priemonę", companyName, options,
        new DialogCallback<Integer>() {

          @Override
          public void onSuccess(Integer value) {
            if (value < rowSet.getNumberOfRows()) {
              RelationUtils.updateRow(VIEW_APPOINTMENTS, COL_VEHICLE, dataView.getActiveRow(),
                  TransportConstants.VIEW_VEHICLES, rowSet.getRow(value), true);
              dataView.refresh(false);
            } else {
              CalendarKeeper.createVehicle(companyId);
            }
          }
        });
  }

  private int getCompanyIndex() {
    return Data.getColumnIndex(VIEW_APPOINTMENTS, COL_COMPANY);
  }

  private int getCompanyNameIndex() {
    return Data.getColumnIndex(VIEW_APPOINTMENTS, COL_COMPANY_NAME);
  }

  private int getVehicleOwnerIndex() {
    return Data.getColumnIndex(VIEW_APPOINTMENTS, COL_VEHICLE_OWNER);
  }

  private void handleCompany(SelectorEvent event) {
    if (!event.isChanged()) {
      return;
    }
    final long companyId = event.getValue();
    if (!DataUtils.isId(companyId)) {
      return;
    }

    final DataView dataView = UiHelper.getDataView(event.getSelector());
    if (dataView == null) {
      return;
    }
    if (!BeeUtils.same(dataView.getViewName(), VIEW_APPOINTMENTS)) {
      return;
    }

    IsRow row = dataView.getActiveRow();
    if (row == null) {
      return;
    }

    Long ownerId = row.getLong(getVehicleOwnerIndex());
    if (ownerId != null && ownerId == companyId) {
      return;
    }

    Queries.getRowSet(TransportConstants.VIEW_VEHICLES, null,
        ComparisonFilter.isEqual(TransportConstants.COL_OWNER, new LongValue(companyId)),
        new Queries.RowSetCallback() {

          @Override
          public void onSuccess(BeeRowSet result) {
            int rowCount = result.getNumberOfRows();
            String companyName = dataView.getActiveRow().getString(getCompanyNameIndex());

            if (rowCount <= 0) {
              dataView.notifyWarning(companyName, "neturi transporto priemonių");
            } else if (rowCount == 1) {
              RelationUtils.updateRow(VIEW_APPOINTMENTS, COL_VEHICLE, dataView.getActiveRow(),
                  TransportConstants.VIEW_VEHICLES, result.getRow(0), true);
              dataView.refresh(false);
            } else {
              chooseVehicle(dataView, result, companyName, companyId);
            }
          }
        });
  }

  private void handleExtendedProperties(SelectorEvent event) {
    if (!event.isOpened()) {
      return;
    }

    DataView dataView = UiHelper.getDataView(event.getSelector());
    if (dataView == null) {
      return;
    }
    IsRow row = dataView.getActiveRow();
    if (row == null) {
      return;
    }

    long id = row.getId();
    Filter filter = null;

    if (BeeUtils.same(dataView.getViewName(), VIEW_PROPERTY_GROUPS)) {
      if (DataUtils.isId(id)) {
        filter = ComparisonFilter.isEqual(COL_PROPERTY_GROUP, new LongValue(id));
      } else {
        filter = Filter.isEmpty(COL_PROPERTY_GROUP);
      }

    } else if (BeeUtils.same(dataView.getViewName(), VIEW_ATTENDEE_PROPS)) {
      if (dataView.getViewPresenter() instanceof HasDataProvider) {
        Provider provider = ((HasDataProvider) dataView.getViewPresenter()).getDataProvider();

        if (provider != null) {
          int index = provider.getColumnIndex(COL_PROPERTY);
          Long exclude = DataUtils.isId(id) ? row.getLong(index) : null;
          Set<Long> used = DataUtils.getDistinct(dataView.getRowData(), index, exclude);

          if (used.isEmpty()) {
            filter = provider.getImmutableFilter();
          } else {
            CompoundFilter and = Filter.and();
            and.add(provider.getImmutableFilter());

            for (Long value : used) {
              and.add(ComparisonFilter.compareId(Operator.NE, value));
            }
            filter = and;
          }
        }
      }
    }

    event.getSelector().setAdditionalFilter(filter);
  }

  private void handleVehicle(SelectorEvent event) {
    if (!event.isOpened()) {
      return;
    }

    DataView dataView = UiHelper.getDataView(event.getSelector());
    if (dataView == null) {
      return;
    }
    if (!BeeUtils.same(dataView.getViewName(), VIEW_APPOINTMENTS)) {
      return;
    }
    
    IsRow row = dataView.getActiveRow();
    if (row == null) {
      return;
    }
    
    Long owner = row.getLong(getCompanyIndex());
    if (DataUtils.isId(owner)) {
      Filter filter = ComparisonFilter.isEqual(TransportConstants.COL_OWNER, new LongValue(owner));
      event.getSelector().setAdditionalFilter(filter);
    }
  }
}

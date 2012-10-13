package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.HasDataProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RelationUtils;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class SelectorHandler implements SelectorEvent.Handler {

  private boolean companyHandlerEnabled = false;
  private boolean vehicleHandlerEnabled = false;

  SelectorHandler() {
    super();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (BeeUtils.same(event.getRelatedViewName(), VIEW_EXTENDED_PROPERTIES)) {
      handleExtendedProperties(event);

    } else if (BeeUtils.same(event.getRelatedViewName(), CommonsConstants.VIEW_COMPANIES)) {
      if (isCompanyHandlerEnabled()) {
        handleCompany(event);
      }

    } else if (BeeUtils.same(event.getRelatedViewName(), TransportConstants.VIEW_VEHICLES)) {
      if (isVehicleHandlerEnabled()) {
        handleVehicle(event);
      }
    }
  }

  boolean isCompanyHandlerEnabled() {
    return companyHandlerEnabled;
  }

  boolean isVehicleHandlerEnabled() {
    return vehicleHandlerEnabled;
  }

  void setCompanyHandlerEnabled(boolean companyHandlerEnabled) {
    this.companyHandlerEnabled = companyHandlerEnabled;
  }

  void setVehicleHandlerEnabled(boolean vehicleHandlerEnabled) {
    this.vehicleHandlerEnabled = vehicleHandlerEnabled;
  }

  private void chooseVehicle(final DataView dataView, final BeeRowSet rowSet, String companyName,
      final IsRow owner) {

    List<String> options = Lists.newArrayList();

    int numberIndex = rowSet.getColumnIndex(TransportConstants.COL_NUMBER);
    int parentModelIndex = rowSet.getColumnIndex(TransportConstants.COL_PARENT_MODEL_NAME);
    int modelIndex = rowSet.getColumnIndex(TransportConstants.COL_MODEL_NAME);

    for (IsRow row : rowSet.getRows()) {
      options.add(BeeUtils.joinWords(row.getString(numberIndex),
          row.getString(parentModelIndex), row.getString(modelIndex)));
    }
    options.add("Nauja");

    Global.choice("Pasirinkite transporto priemonę", companyName, options, new ChoiceCallback() {
      @Override
      public void onSuccess(int value) {
        if (value < rowSet.getNumberOfRows()) {
          RelationUtils.updateRow(VIEW_APPOINTMENTS, COL_VEHICLE, dataView.getActiveRow(),
              TransportConstants.VIEW_VEHICLES, rowSet.getRow(value), true);
          dataView.refresh(false);
        } else {
          createVehicle(owner, dataView);
        }
      }
    });
  }

  private void createVehicle(IsRow owner, final DataView dataView) {
    DataInfo dataInfo = Data.getDataInfo(TransportConstants.VIEW_VEHICLES);
    BeeRow row = RowFactory.createEmptyRow(dataInfo, true);
    if (owner != null) {
      RelationUtils.updateRow(TransportConstants.VIEW_VEHICLES, TransportConstants.COL_OWNER, row,
          CommonsConstants.VIEW_COMPANIES, owner, true);
    }

    RowFactory.createRow(TransportConstants.FORM_NEW_VEHICLE, "Nauja transporto priemonė",
        dataInfo, row, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            RelationUtils.updateRow(VIEW_APPOINTMENTS, COL_VEHICLE, dataView.getActiveRow(),
                TransportConstants.VIEW_VEHICLES, result, true);
            dataView.refresh(false);
          }
        });
  }

  private int getCompanyIndex() {
    return Data.getColumnIndex(VIEW_APPOINTMENTS, COL_COMPANY);
  }

  private int getCompanyNameIndex() {
    return Data.getColumnIndex(VIEW_APPOINTMENTS, COL_COMPANY_NAME);
  }

  private void getCompanyRow(Long company, final RowCallback callback) {
    Queries.getRow(CommonsConstants.VIEW_COMPANIES, company, new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        callback.onSuccess(result);
      }
    });
  }

  private int getVehicleOwnerIndex() {
    return Data.getColumnIndex(VIEW_APPOINTMENTS, COL_VEHICLE_OWNER);
  }

  private void handleCompany(final SelectorEvent event) {
    if (!event.isChanged()) {
      return;
    }
    long companyId = event.getValue();
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
              createVehicle(event.getRelatedRow(), dataView);
            } else if (rowCount == 1) {
              RelationUtils.updateRow(VIEW_APPOINTMENTS, COL_VEHICLE, dataView.getActiveRow(),
                  TransportConstants.VIEW_VEHICLES, result.getRow(0), true);
              dataView.refresh(false);
            } else {
              chooseVehicle(dataView, result, companyName, event.getRelatedRow());
            }
          }
        });
  }

  private void handleExtendedProperties(SelectorEvent event) {
    if (!event.isOpened()) {
      return;
    }

    GridView gridView = UiHelper.getGrid(event.getSelector());
    if (gridView == null) {
      return;
    }
    IsRow row = gridView.getActiveRow();
    if (row == null) {
      return;
    }

    long id = row.getId();
    Filter filter = null;

    if (BeeUtils.same(gridView.getViewName(), VIEW_PROPERTY_GROUPS)) {
      if (DataUtils.isId(id)) {
        filter = ComparisonFilter.isEqual(COL_PROPERTY_GROUP, new LongValue(id));
      } else {
        filter = Filter.isEmpty(COL_PROPERTY_GROUP);
      }

    } else if (BeeUtils.same(gridView.getViewName(), VIEW_ATTENDEE_PROPS)) {
      if (gridView.getViewPresenter() instanceof HasDataProvider) {
        Provider provider = ((HasDataProvider) gridView.getViewPresenter()).getDataProvider();

        if (provider != null) {
          int index = provider.getColumnIndex(COL_PROPERTY);
          Long exclude = DataUtils.isId(id) ? row.getLong(index) : null;
          List<Long> used = DataUtils.getDistinct(gridView.getRowData(), index, exclude);

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

  private void handleVehicle(final SelectorEvent event) {
    final DataView dataView = UiHelper.getDataView(event.getSelector());
    if (dataView == null) {
      return;
    }
    if (!BeeUtils.same(dataView.getViewName(), VIEW_APPOINTMENTS)) {
      return;
    }

    final IsRow row = dataView.getActiveRow();
    if (row == null) {
      return;
    }
    Long company = row.getLong(getCompanyIndex());

    if (event.isOpened()) {
      if (DataUtils.isId(company)) {
        Filter filter = ComparisonFilter.isEqual(TransportConstants.COL_OWNER,
            new LongValue(company));
        event.getSelector().setAdditionalFilter(filter);
      }

    } else if (event.isChanged()) {
      Long owner = Data.getLong(event.getRelatedViewName(), event.getRelatedRow(),
          TransportConstants.COL_OWNER);

      if (DataUtils.isId(owner) && !owner.equals(company)) {
        getCompanyRow(owner, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            RelationUtils.updateRow(VIEW_APPOINTMENTS, COL_COMPANY, row,
                CommonsConstants.VIEW_COMPANIES, result, true);
            dataView.refresh(false);
          }
        });
      }

    } else if (event.isNewRow()) {
      if (DataUtils.isId(company)) {
        Data.setValue(event.getRelatedViewName(), event.getNewRow(),
            TransportConstants.COL_OWNER, company);
        Data.setValue(event.getRelatedViewName(), event.getNewRow(),
            TransportConstants.COL_OWNER_NAME, row.getString(getCompanyNameIndex()));
      }
    }
  }
}

package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.ModificationEvent;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public class CargoHandlingGrid extends AbstractGridInterceptor {

  private String parentView;
  private int handlingIdx = BeeConst.UNDEF;
  private IsRow parentRow;

  @Override
  public void afterDeleteRow(long rowId) {
    if (!BeeConst.isUndef(handlingIdx) && Objects.equals(rowId, parentRow.getLong(handlingIdx))) {
      IsRow row = BeeUtils.peek(getGridView().getRowData());

      if (row != null) {
        parentRow.setValue(handlingIdx, row.getId());

        Queries.updateAndFire(parentView, parentRow.getId(), parentRow.getVersion(),
            COL_CARGO_HANDLING, null, parentRow.getString(handlingIdx),
            ModificationEvent.Kind.UPDATE_ROW);
      } else {
        parentRow.clearCell(handlingIdx);
        DataChangeEvent.fireLocalRefresh(BeeKeeper.getBus(), parentView);
      }
    }
    super.afterDeleteRow(rowId);
  }

  @Override
  public void afterInsertRow(IsRow result) {
    if (!BeeConst.isUndef(handlingIdx) && parentRow.isNull(handlingIdx)) {
      parentRow.setValue(handlingIdx, result.getId());

      Queries.updateAndFire(parentView, parentRow.getId(), parentRow.getVersion(),
          COL_CARGO_HANDLING, null, parentRow.getString(handlingIdx),
          ModificationEvent.Kind.UPDATE_ROW);
    }
    super.afterInsertRow(result);
  }

  @Override
  public void afterUpdateRow(IsRow result) {
    if (!BeeConst.isUndef(handlingIdx)
        && Objects.equals(result.getId(), parentRow.getLong(handlingIdx))) {

      DataChangeEvent.fireLocalRefresh(BeeKeeper.getBus(), parentView);
    }
    super.afterUpdateRow(result);
  }

  @Override
  public GridInterceptor getInstance() {
    return new CargoHandlingGrid();
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    if (parentRow != null && gridView.isEmpty()) {
      for (String prefix : new String[] {VAR_LOADING, VAR_UNLOADING}) {
        for (String col : new String[] {
            COL_PLACE_DATE, COL_PLACE_ADDRESS, COL_PLACE_POST_INDEX, COL_PLACE_COMPANY,
            COL_PLACE_CONTACT, COL_PLACE_CITY, ALS_CITY_NAME, COL_PLACE_COUNTRY, ALS_COUNTRY_NAME,
            "CountryCode", COL_NOTE, COL_NUMBER}) {

          newRow.setValue(gridView.getDataIndex(prefix + col),
              parentRow.getString(Data.getColumnIndex(parentView, prefix + col)));
        }
      }
      for (String col : new String[] {
          COL_LOADED_KILOMETERS, COL_EMPTY_KILOMETERS, COL_ROUTE_WEIGHT}) {

        newRow.setValue(gridView.getDataIndex(col),
            parentRow.getString(Data.getColumnIndex(parentView, col)));
      }
    }
    return super.onStartNewRow(gridView, oldRow, newRow);
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    if (BeeUtils.isEmpty(parentView)) {
      parentView = event.getViewName();

      if (Data.containsColumn(parentView, COL_CARGO_HANDLING)) {
        handlingIdx = Data.getColumnIndex(parentView, COL_CARGO_HANDLING);
      }
    }
    if (!BeeConst.isUndef(handlingIdx)) {
      parentRow = DataUtils.cloneRow(event.getRow());
    }
    super.onParentRow(event);
  }
}

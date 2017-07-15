package com.butent.bee.client.modules.classifiers;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

public final class CompanyContactsGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new CompanyContactsGrid();
  }

  @Override
  public void onEditStart(final EditStartEvent event) {
    final IsRow row = event.getRowValue();

    if (row == null) {
      return;
    }

    if (COL_EMAIL_ID.equals(event.getColumnId())) {
      int idxEmailId = getDataIndex(COL_EMAIL_ID);

      if (idxEmailId < 0) {
        return;
      }

      if (!DataUtils.isId(row.getLong(idxEmailId))) {
        return;
      }

      final int idxRemindEmail = getDataIndex(COL_REMIND_EMAIL);
      final int idxMailInvoices = getDataIndex(COL_EMAIL_INVOICES);

      if (idxRemindEmail < 0 && idxMailInvoices < 0) {
        return;
      }

      if (row.getBoolean(idxRemindEmail) != null) {
        Queries.update(getViewName(), Filter.compareId(row.getId()), COL_REMIND_EMAIL, Value
            .getNullValueFromValueType(ValueType.BOOLEAN), new Queries.IntCallback() {

          @Override
          public void onSuccess(Integer result) {
            CellUpdateEvent.fire(BeeKeeper.getBus(), getViewName(),
                row.getId(), event.getRowValue().getVersion(),
                CellSource.forColumn(getDataColumns().get(idxRemindEmail), idxRemindEmail), null);
          }
        });
      }

      if (row.getBoolean(idxMailInvoices) != null) {
        Queries.update(getViewName(), Filter.compareId(row.getId()), COL_EMAIL_INVOICES, Value
            .getNullValueFromValueType(ValueType.BOOLEAN), new Queries.IntCallback() {

          @Override
          public void onSuccess(Integer result) {
            CellUpdateEvent.fire(BeeKeeper.getBus(), getViewName(),
                row.getId(), event.getRowValue().getVersion(),
                CellSource.forColumn(getDataColumns().get(idxMailInvoices), idxMailInvoices), null);
          }
        });
      }
    }
  }
}

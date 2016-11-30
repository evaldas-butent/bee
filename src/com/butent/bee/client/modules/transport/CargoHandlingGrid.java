package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.ParentRowCreator;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;
import java.util.Objects;

public class CargoHandlingGrid extends AbstractGridInterceptor {

  @Override
  public void afterCreate(GridView gridView) {
    gridView.getGrid().addMutationHandler(event -> {
      FormView parentForm = ViewHelper.getForm(gridView);

      if (Objects.nonNull(parentForm) && DataUtils.isId(parentForm.getActiveRowId())) {
        String view = parentForm.getViewName();
        String table = null;

        if (parentForm.getViewPresenter() instanceof HasGridView) {
          DataChangeEvent.fireLocalRefresh(BeeKeeper.getBus(), view);
        }
        switch (view) {
          case VIEW_ORDER_CARGO:
            table = TBL_CARGO_TRIPS;
            break;
          case VIEW_ASSESSMENTS:
            table = TBL_ASSESSMENT_FORWARDERS;
            break;
        }
        if (!BeeUtils.isEmpty(table)) {
          Data.onTableChange(table, EnumSet.of(DataChangeEvent.Effect.REFRESH));
        }
      }
    });
    super.afterCreate(gridView);
  }

  @Override
  public GridInterceptor getInstance() {
    return new CargoHandlingGrid();
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    if (gridView.isEmpty()) {
      IsRow parentRow = ViewHelper.getFormRow(gridView);

      if (DataUtils.isNewRow(parentRow)) {
        FormView parentForm = ViewHelper.getForm(gridView);

        if (parentForm.getViewPresenter() instanceof ParentRowCreator) {
          ((ParentRowCreator) parentForm.getViewPresenter()).createParentRow(parentForm,
              result -> {
                fillValuesByParameters(newRow, result);
                FormView gridForm = gridView.getActiveForm();

                if (Objects.nonNull(gridForm)) {
                  gridForm.refresh();
                }
              });
        }
      } else {
        fillValuesByParameters(newRow, parentRow);
      }
    }
    return super.onStartNewRow(gridView, oldRow, newRow);
  }

  private void fillValuesByParameters(IsRow newRow, IsRow parentRow) {
    if (parentRow != null) {
      String prefix = BeeUtils.removePrefix(getViewName(), COL_CARGO);

      parentRow.getProperties().forEach((name, value) -> {
        if (BeeUtils.isPrefix(name, prefix)) {
          int colIndex = getDataIndex(BeeUtils.removePrefix(name, prefix));

          if (!BeeConst.isUndef(colIndex)) {
            newRow.setValue(colIndex, value);
          }
        }
      });
    }
  }
}

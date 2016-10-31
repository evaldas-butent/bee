package com.butent.bee.client.modules.transport;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public class AssessmentForwarderForm extends PrintFormInterceptor {

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, VAR_INCOME) && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(event -> {
        if (event.isOpened()) {
          FormView form = ViewHelper.getForm(getGridView());

          if (form != null) {
            event.getSelector().setAdditionalFilter(Filter.equals(COL_CARGO,
                form.getLongValue(COL_CARGO)));
          }
        }
      });
    }
    if (BeeUtils.inList(name, TBL_CARGO_LOADING, TBL_CARGO_UNLOADING)) {
      ((ChildGrid) widget).setGridInterceptor(new AbstractGridInterceptor() {

        @Override
        public GridInterceptor getInstance() {
          return null;
        }

        @Override
        public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
          if (gridView.isEmpty()) {
            FormView form = ViewHelper.getForm(AssessmentForwarderForm.this.getGridView());

            if (Objects.nonNull(form)) {
              Widget grid = form.getWidgetByName(gridView.getViewName());
              IsRow parentRow = grid instanceof ChildGrid
                  ? BeeUtils.peek(((ChildGrid) grid).getGridView().getRowData()) : null;

              if (Objects.nonNull(parentRow)) {
                for (BeeColumn column : gridView.getDataColumns()) {
                  String col = column.getId();

                  if (!BeeUtils.inList(col, COL_CARGO, COL_CARGO_TRIP)) {
                    int idx = gridView.getDataIndex(col);
                    newRow.setValue(idx, parentRow.getValue(idx));
                  }
                }
              }
            }
          }
          return true;
        }
      });
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    if (!TransportHandler.bindExpensesToIncomes()
        && BeeUtils.inListSame(name, VAR_INCOME + "Label", VAR_INCOME)) {
      return false;
    }
    return super.beforeCreateWidget(name, description);
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new AssessmentForwarderPrintForm();
  }

  @Override
  public FormInterceptor getInstance() {
    return new AssessmentForwarderForm();
  }

}

package com.butent.bee.client.modules.transport;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.utils.BeeUtils;

public class OrderAssessmentForm extends AbstractFormInterceptor {

  private abstract class AssessmentGrid extends AbstractGridInterceptor {
    @Override
    public void beforeRefresh(GridPresenter presenter) {
      presenter.getDataProvider()
          .setParentFilter(COL_ASSESSOR, ComparisonFilter.isEqual(COL_ASSESSOR,
              new LongValue(getFormView().getActiveRow().getId())));
    }

    @Override
    public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
      newRow.setValue(gridView.getDataIndex(COL_ASSESSOR), getFormView().getActiveRow().getId());
      return true;
    }
  }

  private class ServicesGrid extends AssessmentGrid {
    final boolean expense;

    public ServicesGrid(boolean expense) {
      this.expense = expense;
    }

    @Override
    public void beforeRefresh(GridPresenter presenter) {
      super.beforeRefresh(presenter);

      IsRow row = getFormView().getActiveRow();
      final Widget cap = getFormView().getWidgetByName(expense ? "ExpenseTotal" : "IncomeTotal");

      if (cap == null) {
        return;
      }
      ParameterList args = TransportHandler.createArgs(SVC_GET_ASSESSMENT_INFO);
      args.addDataItem(COL_ASSESSOR, row.getId());
      args.addDataItem(COL_CARGO, row.getLong(getFormView().getDataIndex(COL_CARGO)));
      args.addDataItem(COL_SERVICE_EXPENSE, BooleanValue.pack(expense));

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(getFormView());

          if (response.hasErrors()) {
            return;
          }
          double total = BeeUtils.round(BeeUtils.toDouble((String) response.getResponse()), 2);

          cap.getElement().setInnerText(total != 0 ? BeeUtils.parenthesize(total) : "");
        }
      });
    }

    @Override
    public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
      if (expense) {
        newRow.setValue(gridView.getDataIndex(COL_SERVICE_EXPENSE), true);
      }
      return super.onStartNewRow(gridView, oldRow, newRow);
    }
  }

  private class AssessorGrid extends AssessmentGrid {
    @Override
    public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
      newRow.setValue(gridView.getDataIndex(COL_ASSESSOR_NOTES),
          getFormView().getActiveRow().getString(getFormView().getDataIndex("AssessorNotes")));

      return super.onStartNewRow(gridView, oldRow, newRow);
    }
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      AssessmentGrid interceptor = null;

      if (BeeUtils.same(name, "AssessmentExpenses")) {
        interceptor = new ServicesGrid(true);

      } else if (BeeUtils.same(name, "AssessmentIncomes")) {
        interceptor = new ServicesGrid(false);

      } else if (BeeUtils.same(name, TBL_CARGO_ASSESSORS)) {
        interceptor = new AssessorGrid();
      }
      if (interceptor != null) {
        ((ChildGrid) widget).setGridInterceptor(interceptor);
      }
    }
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (row == null) {
      return;
    }
    header.addCommandItem(new BeeButton("Rašyti laišką", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
      }
    }));
  }

  @Override
  public FormInterceptor getInstance() {
    return this;
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    form.setEnabled(!DataUtils.isId(row.getLong(form.getDataIndex(COL_ASSESSOR))));
    return true;
  }
}

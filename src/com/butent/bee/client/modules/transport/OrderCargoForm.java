package com.butent.bee.client.modules.transport;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasClickHandlers;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.modules.transport.TransportHandler.Profit;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

class OrderCargoForm extends AbstractFormInterceptor {
  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, "profit") && widget instanceof HasClickHandlers) {
      ((HasClickHandlers) widget).addClickHandler(new Profit(VAR_CARGO_ID));
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new OrderCargoForm();
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    Presenter presenter = form.getViewPresenter();
    presenter.getHeader().clearCommandPanel();
    presenter.getHeader().addCommandItem(new InvoiceCreator(ComparisonFilter.isEqual(COL_CARGO,
        Value.getValue(row.getId()))));

    return true;
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    form.getViewPresenter().getHeader().clearCommandPanel();
  }
}
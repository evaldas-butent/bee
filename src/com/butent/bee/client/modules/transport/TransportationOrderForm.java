package com.butent.bee.client.modules.transport;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.modules.transport.TransportHandler.Profit;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

class TransportationOrderForm extends AbstractFormInterceptor implements ClickHandler {

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (BeeUtils.same(name, "profit") && widget instanceof HasClickHandlers) {
      ((HasClickHandlers) widget).addClickHandler(new Profit(COL_ORDER));
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new TransportationOrderForm();
  }

  @Override
  public void onClick(ClickEvent event) {
    final FormView form = getFormView();
    long orderId = form.getActiveRowId();

    if (!DataUtils.isId(orderId)) {
      return;
    }
    ParameterList args = TransportHandler.createArgs(SVC_GET_UNASSIGNED_CARGOS);
    args.addDataItem(COL_ORDER, orderId);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasErrors()) {
          response.notify(form);
          return;
        }
        String[] cargos = Codec.beeDeserializeCollection(response.getResponseAsString());

        if (ArrayUtils.isEmpty(cargos)) {
          form.notifyWarning(Localized.getMessages()
              .dataNotAvailable(Localized.getConstants().cargos()));
          return;
        }
        TripSelector.select(cargos, null, form.getElement());
      }
    });
  }

  @Override
  public boolean onStartEdit(FormView form, final IsRow row, ScheduledCommand focusCommand) {
    HeaderView hdr = form.getViewPresenter().getHeader();
    hdr.clearCommandPanel();

    hdr.addCommandItem(new InvoiceCreator(ComparisonFilter.isEqual(COL_ORDER,
        Value.getValue(row.getId()))));

    hdr.addCommandItem(new Button(Localized.getConstants().trAssignTrip(), this));
    return true;
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    form.getViewPresenter().getHeader().clearCommandPanel();
  }
}
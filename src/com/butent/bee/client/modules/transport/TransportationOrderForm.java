package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.modules.transport.TransportHandler.Profit;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;

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
  public void onReadyForInsert(ReadyForInsertEvent event) {
    Long customer = null;
    List<BeeColumn> cols = event.getColumns();

    for (int i = 0; i < cols.size(); i++) {
      if (BeeUtils.inListSame(cols.get(i).getId(), COL_CUSTOMER, COL_PAYER)) {
        customer = BeeUtils.toLongOrNull(event.getValues().get(i));

        if (BeeUtils.same(cols.get(i).getId(), COL_PAYER)) {
          break;
        }
      }
    }
    if (DataUtils.isId(customer)) {
      event.consume();
      checkCreditInfo(event, customer);
    }
  }

  @Override
  public void onSaveChanges(final SaveChangesEvent event) {
    Long customer = null;
    int custIdx = -1;
    int payerIdx = -1;
    List<BeeColumn> cols = event.getColumns();

    for (int i = 0; i < cols.size(); i++) {
      if (BeeUtils.same(cols.get(i).getId(), COL_CUSTOMER)) {
        custIdx = i;
      } else if (BeeUtils.same(cols.get(i).getId(), COL_PAYER)) {
        payerIdx = i;
      }
    }
    Long cust = Data.getLong(getViewName(), event.getNewRow(), COL_CUSTOMER);
    Long payer = Data.getLong(getViewName(), event.getNewRow(), COL_PAYER);

    if (BeeUtils.isNonNegative(payerIdx)
        || !DataUtils.isId(payer) && BeeUtils.isNonNegative(custIdx)) {
      customer = BeeUtils.nvl(payer, cust);
    }
    if (DataUtils.isId(customer)) {
      event.consume();
      checkCreditInfo(event, customer);
    }
  }

  @Override
  public boolean onStartEdit(FormView form, final IsRow row, ScheduledCommand focusCommand) {
    HeaderView hdr = form.getViewPresenter().getHeader();
    hdr.clearCommandPanel();

    if (Data.isViewEditable(VIEW_CARGO_INVOICES)) {
      hdr.addCommandItem(new InvoiceCreator(ComparisonFilter.isEqual(COL_ORDER,
          Value.getValue(row.getId()))));
    }
    if (Data.isViewEditable(VIEW_CARGO_TRIPS)) {
      hdr.addCommandItem(new Button(Localized.getConstants().trAssignTrip(), this));
    }

    return true;
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    form.getViewPresenter().getHeader().clearCommandPanel();
  }

  private void checkCreditInfo(final GwtEvent<?> event, Long customer) {
    ParameterList args = TransportHandler.createArgs(SVC_GET_CREDIT_INFO);
    args.addDataItem(COL_CUSTOMER, customer);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          response.notify(getFormView());
          return;
        }
        Map<String, String> result = Codec.beeDeserializeMap(response.getResponseAsString());

        if (BeeUtils.isEmpty(result)) {
          getGridView().fireEvent(event);
        } else {
          String cap = null;
          List<String> msgs = Lists.newArrayList();

          for (String key : result.keySet()) {
            if (BeeUtils.same(key, CommonsConstants.COL_COMPANY_NAME)) {
              cap = result.get(key);
              continue;
            }
            msgs.add(key + ": " + result.get(key));
          }
          Global.confirm(cap, null, msgs, Localized.getConstants().ok(),
              Localized.getConstants().cancel(), new ConfirmationCallback() {
                @Override
                public void onConfirm() {
                  getGridView().fireEvent(event);
                }
              });
        }
      }
    });
  }
}
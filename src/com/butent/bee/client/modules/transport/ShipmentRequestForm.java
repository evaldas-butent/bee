package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

// import static com.butent.bee.shared.modules.transport.TransportConstants.COL_CARGO_SHIPPING_TERM;
// import static com.butent.bee.shared.modules.transport.TransportConstants.COL_QUERY_EXPEDITION;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_QUERY_HOST;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_QUERY_STATUS;

import com.butent.bee.client.Callback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.modules.commons.CommonsUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.transport.TransportConstants.CargoRequestStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

class ShipmentRequestForm extends AbstractFormInterceptor {

  private Button activateCommand;
  private Button blockCommand;

  ShipmentRequestForm() {
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    CargoRequestStatus status;
    if (DataUtils.hasId(row)) {
      status = EnumUtils.getEnumByIndex(CargoRequestStatus.class,
          Data.getInteger(getViewName(), row, COL_QUERY_STATUS));
    } else {
      status = null;
    }

    refreshCommands(status);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ShipmentRequestForm();
  }

  // @Override
  // public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
  // SelfServiceUtils.setDefaultExpeditionType(form, newRow, COL_QUERY_EXPEDITION);
  // SelfServiceUtils.setDefaultShippingTerm(form, newRow, COL_CARGO_SHIPPING_TERM);
  //
  // super.onStartNewRow(form, oldRow, newRow);
  // }

  private void onBlock() {
    String host = getDataValue(COL_QUERY_HOST);
    if (BeeUtils.isEmpty(host)) {
      return;
    }

    String caption = Localized.getConstants().trCommandBlockIpAddress();
    CommonsUtils.blockHost(caption, host, getFormView(), new Callback<String>() {
      @Override
      public void onSuccess(String result) {
        if (getFormView().isInteractive()) {
          getHeaderView().clearCommandPanel();
        }
        // updateStatus(CargoRequestStatus.REJECTED);
      }
    });
  }

  private void onCreateOrder() {
  }

  private void refreshCommands(CargoRequestStatus status) {
    HeaderView header = getHeaderView();
    if (header == null) {
      return;
    }

    if (header.hasCommands()) {
      header.clearCommandPanel();
    }

    if (status == null) {
      return;
    }

    if (status == CargoRequestStatus.NEW) {
      if (this.activateCommand == null) {
        this.activateCommand =
            new Button(Localized.getConstants().trCommandCreateNewOrder(), new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                onCreateOrder();
              }
            });
      }
      header.addCommandItem(this.activateCommand);

      if (this.blockCommand == null) {
        this.blockCommand =
            new Button(Localized.getConstants().trCommandBlockIpAddress(), new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                onBlock();
              }
            });
      }
      header.addCommandItem(this.blockCommand);
    }
  }

  // private void updateStatus(CargoRequestStatus status) {
  // SelfServiceUtils.updateStatus(getFormView(), COL_QUERY_STATUS, status);
  // }
}

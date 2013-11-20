package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;

class CargoRequestForm extends AbstractFormInterceptor {

  @Override
  public void afterRefresh(final FormView form, IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (form.isEnabled()) {
      // if (!DataUtils.isId(currentRow.getLong(form.getDataIndex(COL_ORDER)))) {
      header.addCommandItem(new Button(Localized.getConstants().trCargoRequestReturnToOrder(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              // requestToOrders();
            }
          }));
      // }
      // boolean finished =
      // currentRow.getDateTime(form.getDataIndex(CrmConstants.COL_REQUEST_FINISHED)) != null;

      boolean finished = false;
      if (finished) {
        header.addCommandItem(new Button(Localized.getConstants().trCargoRequestReturn(),
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                // restoreRequest();
              }
            }));
        form.setEnabled(false);

      } else {
        header.addCommandItem(new Button(Localized.getConstants().trCargoRequestFinish(),
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                // finishRequest();
              }
            }));
      }

      header.addCommandItem(new Button(Localized.getConstants().trCommandCopyRequest()));
      header.addCommandItem(new Button(Localized.getConstants().trCommandSaveRequestAsTemplate()));
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoRequestForm();
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    SelfServiceUtils.setDefaultExpeditionType(form, newRow, COL_CARGO_REQUEST_EXPEDITION);
    SelfServiceUtils.setDefaultShippingTerm(form, newRow, COL_CARGO_SHIPPING_TERM);

    super.onStartNewRow(form, oldRow, newRow);
  }
  
  CargoRequestForm() {
  }
}

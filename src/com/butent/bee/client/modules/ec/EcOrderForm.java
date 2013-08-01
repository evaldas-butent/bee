package com.butent.bee.client.modules.ec;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;

public class EcOrderForm extends AbstractFormInterceptor {

  private boolean initialized;

  @Override
  public FormInterceptor getInstance() {
    return new EcOrderForm();
  }

  @Override
  public boolean onStartEdit(final FormView form, final IsRow row, ScheduledCommand focusCommand) {
    if (!initialized) {
      HeaderView header = form.getViewPresenter().getHeader();
      header.clearCommandPanel();

      header.addCommandItem(new Button(Localized.getConstants().ecSendToERP(), new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Global.confirm(Localized.getConstants().ecSendToERPConfirm(), new ConfirmationCallback() {
            @Override
            public void onConfirm() {
              ParameterList args = EcKeeper.createArgs(SVC_SEND_TO_ERP);
              args.addDataItem(COL_ORDER_ITEM_ORDER, row.getId());

              BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
                @Override
                public void onResponse(ResponseObject response) {
                  response.notify(getFormView());

                  if (response.hasErrors()) {
                    return;
                  }
                  Queries.getRow(form.getViewName(), row.getId(), new RowCallback() {
                    @Override
                    public void onSuccess(BeeRow result) {
                      if (row.getId() == form.getActiveRow().getId()) {
                        form.updateRow(result, false);
                      }
                    }
                  });
                }
              });
            }
          });
        }
      }));
      initialized = true;
    }
    return initialized;
  }
}

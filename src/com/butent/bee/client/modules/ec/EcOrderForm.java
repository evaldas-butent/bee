package com.butent.bee.client.modules.ec;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants.EcOrderStatus;
import com.butent.bee.shared.utils.NameUtils;

public class EcOrderForm extends AbstractFormInterceptor implements ClickHandler {

  private final Button action = new Button(Localized.getConstants().ecSendToERP(), this);

  @Override
  public FormInterceptor getInstance() {
    return new EcOrderForm();
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    EcOrderStatus status = NameUtils.getEnumByIndex(EcOrderStatus.class,
        Data.getInteger(form.getViewName(), row, COL_ORDER_STATUS));

    if (status == EcOrderStatus.NEW) {
      header.addCommandItem(action);
    }
    return true;
  }

  @Override
  public void onClick(ClickEvent event) {
    Global.confirm(Localized.getConstants().ecSendToERPConfirm(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        final FormView form = getFormView();
        final HeaderView header = form.getViewPresenter().getHeader();
        final long rowId = form.getActiveRow().getId();

        header.clearCommandPanel();
        header.addCommandItem(new Image(Global.getImages().loading()));

        ParameterList args = EcKeeper.createArgs(SVC_SEND_TO_ERP);
        args.addDataItem(COL_ORDER_ITEM_ORDER, rowId);

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            header.clearCommandPanel();
            response.notify(getFormView());

            if (response.hasErrors()) {
              header.addCommandItem(action);
              return;
            }
            Queries.getRow(form.getViewName(), rowId, new RowCallback() {
              @Override
              public void onSuccess(BeeRow result) {
                if (rowId == form.getActiveRow().getId()) {
                  form.updateRow(result, false);
                }
                BeeKeeper.getBus().fireEvent(new RowUpdateEvent(form.getViewName(), result));
              }
            });
          }
        });
      }
    });
  }
}

package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.modules.commons.CommonsUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.transport.TransportConstants.TranspRegStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

class TransportRegistrationForm extends AbstractFormInterceptor {

  private Button register;
  private Button block;

  @Override
  public void afterRefresh(final FormView form, IsRow row) {
    TranspRegStatus status;
    if (DataUtils.hasId(row)) {
      status = EnumUtils.getEnumByIndex(TranspRegStatus.class,
          Data.getInteger(getViewName(), row, COL_REGISTRATION_STATUS));
    } else {
      status = null;
    }

    refreshCommands(status);
  }

  @Override
  public FormInterceptor getInstance() {
    return new TransportRegistrationForm();
  }

  private void refreshCommands(TranspRegStatus status) {
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

    if (status == TranspRegStatus.NEW) {
      if (this.register == null) {
        this.register = new Button(Localized.getConstants().trCommandCreateNewUser());
      }
      header.addCommandItem(this.register);

      if (this.block == null) {
        String label = Localized.getConstants().trCommandBlockIpAddress();
        this.block = new Button(label, new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            String host = getDataValue(COL_REGISTRATION_HOST);
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
                updateStatus(TranspRegStatus.REJECTED);
              }
            });
          }
        });
      }
      header.addCommandItem(this.block);
    }
  }

  private void updateStatus(final TranspRegStatus status) {
    BeeRow row = DataUtils.cloneRow(getActiveRow());
    row.setValue(getDataIndex(COL_REGISTRATION_STATUS), status.ordinal());

    Queries.update(getViewName(), getFormView().getDataColumns(), getFormView().getOldRow(),
        row, getFormView().getChildrenForUpdate(), new RowCallback() {
          @Override
          public void onFailure(String... reason) {
            getFormView().notifySevere(reason);
          }

          @Override
          public void onSuccess(BeeRow result) {
            if (DataUtils.sameId(result, getActiveRow()) && !getFormView().observesData()) {
              getFormView().updateRow(result, false);
            }
            BeeKeeper.getBus().fireEvent(new RowUpdateEvent(getViewName(), result));
          }
        });
  }

  TransportRegistrationForm() {
  }
}

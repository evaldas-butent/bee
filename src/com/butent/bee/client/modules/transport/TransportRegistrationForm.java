package com.butent.bee.client.modules.transport;

import com.google.common.collect.Maps;
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
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.TranspRegStatus;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Map;

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
        this.register =
            new Button(Localized.getConstants().trCommandCreateNewUser(), new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                onCreateUser();
              }
            });
      }
      header.addCommandItem(this.register);

      if (this.block == null) {
        this.block =
            new Button(Localized.getConstants().trCommandBlockIpAddress(), new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                onBlock();
              }
            });
      }
      header.addCommandItem(this.block);
    }
  }

  private void onBlock() {
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

  private void onCreateUser() {
    String email = BeeUtils.trim(getDataValue(COL_REGISTRATION_EMAIL));
    if (BeeUtils.isEmpty(email)) {
      return;
    }

    String login = BeeUtils.notEmpty(BeeUtils.getPrefix(email, BeeConst.CHAR_AT), email);

    Map<String, String> parameters = Maps.newHashMap();
    parameters.put(CommonsConstants.COL_EMAIL, email);

    putUserField(parameters, COL_REGISTRATION_COMPANY_NAME, CommonsConstants.ALS_COMPANY_NAME);
    putUserField(parameters, COL_REGISTRATION_COMPANY_CODE, CommonsConstants.ALS_COMPANY_CODE);
    putUserField(parameters, COL_REGISTRATION_VAT_CODE, CommonsConstants.COL_COMPANY_VAT_CODE);
    putUserField(parameters, COL_REGISTRATION_EXCHANGE_CODE,
        CommonsConstants.COL_COMPANY_EXCHANGE_CODE);
    
    String contact = BeeUtils.trim(getDataValue(COL_REGISTRATION_CONTACT));
    if (!BeeUtils.isEmpty(contact)) {
      int p = contact.lastIndexOf(BeeConst.CHAR_SPACE);
      if (p > 0) {
        parameters.put(CommonsConstants.COL_FIRST_NAME, contact.substring(0, p).trim());
        parameters.put(CommonsConstants.COL_LAST_NAME, contact.substring(p + 1).trim());
      } else {
        parameters.put(CommonsConstants.COL_FIRST_NAME, contact);
      }
    }
    putUserField(parameters, COL_REGISTRATION_CONTACT_POSITION, CommonsConstants.COL_POSITION);

    putUserField(parameters, COL_REGISTRATION_ADDRESS, CommonsConstants.COL_ADDRESS);
    putUserField(parameters, COL_REGISTRATION_CITY, CommonsConstants.COL_CITY);
    putUserField(parameters, COL_REGISTRATION_COUNTRY, CommonsConstants.COL_COUNTRY);

    putUserField(parameters, COL_REGISTRATION_PHONE, CommonsConstants.COL_PHONE);
    putUserField(parameters, COL_REGISTRATION_MOBILE, CommonsConstants.COL_MOBILE);
    putUserField(parameters, COL_REGISTRATION_FAX, CommonsConstants.COL_FAX);

    String caption = Localized.getConstants().trCommandCreateNewUser();
    CommonsUtils.createUser(caption, login, null, UserInterface.SELF_SERVICE, parameters,
        getFormView(), new Callback<String>() {
          @Override
          public void onSuccess(String result) {
            if (getFormView().isInteractive()) {
              getHeaderView().clearCommandPanel();
            }
            updateStatus(TranspRegStatus.CONFIRMED);
          }
        });
  }

  private void putUserField(Map<String, String> parameters, String source, String destination) {
    String value = getDataValue(source);
    if (!BeeUtils.isEmpty(value)) {
      parameters.put(destination, value.trim());
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

package com.butent.bee.client.modules.transport;

import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Callback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.modules.commons.CommonsUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.TranspRegStatus;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Map;

class TransportRegistrationForm extends AbstractFormInterceptor {

  private Button registerCommand;
  private Button blockCommand;

  TransportRegistrationForm() {
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
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
      if (Data.isViewEditable(CommonsConstants.VIEW_USERS)) {
        if (this.registerCommand == null) {
          this.registerCommand =
              new Button(Localized.getConstants().trCommandCreateNewUser(), new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  onCreateUser();
                }
              });
        }
        header.addCommandItem(this.registerCommand);
      }

      if (!BeeUtils.isEmpty(getDataValue(COL_REGISTRATION_HOST)) 
          && Data.isViewEditable(CommonsConstants.VIEW_IP_FILTERS)) {
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
  }

  private void updateStatus(final TranspRegStatus status) {
    SelfServiceUtils.updateStatus(getFormView(), COL_REGISTRATION_STATUS, status);
  }
}

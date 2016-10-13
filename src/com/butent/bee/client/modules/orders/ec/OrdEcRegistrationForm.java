package com.butent.bee.client.modules.orders.ec;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.modules.administration.AdministrationUtils;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.ec.EcConstants.EcClientType;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.HashMap;
import java.util.Map;

public class OrdEcRegistrationForm extends AbstractFormInterceptor {

  private Button registerCommand;

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    refreshCommands(row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new OrdEcRegistrationForm();
  }

  private void onCreateUser() {
    String email = BeeUtils.trim(getStringValue(COL_REGISTRATION_EMAIL));
    if (BeeUtils.isEmpty(email)) {
      notifyRequired(Localized.dictionary().email());
      return;
    }

    String firstName = getStringValue(COL_REGISTRATION_FIRST_NAME);
    if (BeeUtils.isEmpty(firstName)) {
      notifyRequired(Localized.dictionary().ecClientFirstName());
      return;
    }

    String companyName = getStringValue(COL_REGISTRATION_COMPANY_NAME);
    if (BeeUtils.isEmpty(companyName)) {
      companyName = BeeUtils.joinWords(firstName, getStringValue(COL_REGISTRATION_LAST_NAME));
    }

    String companyType = getStringValue(COL_REGISTRATION_COMPANY_TYPE);
    if (BeeUtils.isEmpty(companyType) && !BeeUtils.isEmpty(companyName)) {
      notifyRequired(Localized.dictionary().companyStatus());
      return;
    }

    final EcClientType type = EnumUtils.getEnumByIndex(EcClientType.class,
        getIntegerValue(COL_REGISTRATION_TYPE));
    if (type == null) {
      notifyRequired(Localized.dictionary().ecClientType());
      return;
    }

    Map<String, String> userFields = new HashMap<>();
    userFields.put(ClassifierConstants.COL_EMAIL, email);
    userFields.put(ClassifierConstants.COL_FIRST_NAME, firstName.trim());
    userFields.put(ClassifierConstants.ALS_COMPANY_NAME, companyName.trim());
    userFields.put(ClassifierConstants.COL_COMPANY_TYPE, companyType.trim());

    putUserField(userFields, COL_REGISTRATION_COMPANY_CODE, ClassifierConstants.ALS_COMPANY_CODE);
    putUserField(userFields, COL_REGISTRATION_VAT_CODE, ClassifierConstants.COL_COMPANY_VAT_CODE);

    putUserField(userFields, COL_REGISTRATION_LAST_NAME, ClassifierConstants.COL_LAST_NAME);

    putUserField(userFields, COL_REGISTRATION_ADDRESS, ClassifierConstants.COL_ADDRESS);
    putUserField(userFields, COL_REGISTRATION_CITY, ClassifierConstants.COL_CITY);
    putUserField(userFields, COL_REGISTRATION_COUNTRY, ClassifierConstants.COL_COUNTRY);

    putUserField(userFields, COL_REGISTRATION_PHONE, ClassifierConstants.COL_PHONE);
    putUserField(userFields, COL_REGISTRATION_POST_INDEX, ClassifierConstants.COL_POST_INDEX);

    String login = BeeUtils.notEmpty(BeeUtils.getPrefix(email, BeeConst.CHAR_AT), email);
    final String password = BeeUtils.left(login, 1);

    String caption = Localized.dictionary().ecRegistrationCommandCreate();
    AdministrationUtils.createUser(caption, login, password, UserInterface.ORDERS, userFields,
        getFormView(), result -> {
          if (getFormView().isInteractive()) {
            getHeaderView().clearCommandPanel();

            Queries.update(VIEW_ORD_EC_REGISTRATIONS, Filter.compareId(getActiveRowId()),
                COL_REGISTRATION_REGISTERED, "t", new Queries.IntCallback() {
                  @Override
                  public void onSuccess(Integer result) {
                    final String msgLogin = Localized.dictionary().userLogin() + ": "
                        + login.trim();
                    final String msgPswd = Localized.dictionary().password() + ": "
                        + password.trim();

                    NewMailMessage.create(email, null, msgLogin + " " + msgPswd, null, null);
                  }
                });
          }
        });
  }

  private void putUserField(Map<String, String> parameters, String source, String destination) {
    String value = getStringValue(source);
    if (!BeeUtils.isEmpty(value)) {
      parameters.put(destination, value.trim());
    }
  }

  private void refreshCommands(IsRow row) {
    HeaderView header = getHeaderView();
    if (header == null) {
      return;
    }

    if (header.hasCommands()) {
      header.clearCommandPanel();
    }

    if (DataUtils.hasId(row)) {
      if (Data.isViewEditable(AdministrationConstants.VIEW_USERS)) {
        if (this.registerCommand == null) {
          this.registerCommand =
              new Button(Localized.dictionary().ordRegistrationCommandCreate(),
                  event -> onCreateUser());
        }
        header.addCommandItem(this.registerCommand);
      }
    }
  }
}
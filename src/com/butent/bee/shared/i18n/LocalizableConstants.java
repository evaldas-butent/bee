package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Constants;

import com.butent.bee.shared.modules.calendar.LocalizableCalendarConstants;
import com.butent.bee.shared.modules.transport.LocalizableTransportConstants;

public interface LocalizableConstants extends Constants, LocalizableCalendarConstants,
    LocalizableTransportConstants {

  String cancel();
  String changedValues();
  String changePassword();
  String createNewRow();
  String newPassword();
  String newPasswordIsRequired();
  String newPasswordsDoesNotMatch();
  String newTab();
  String newValues();
  String no();
  String ok();
  String oldPassword();
  String oldPasswordIsInvalid();
  String oldPasswordIsRequired();
  String questionLogout();  
  String repeatNewPassword();
  String saveChanges();
  String sorry();
  String yes();
}

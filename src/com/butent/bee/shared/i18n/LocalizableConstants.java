package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Constants;

import com.butent.bee.shared.modules.calendar.LocalizableCalendarConstants;
import com.butent.bee.shared.modules.transport.LocalizableTransportConstants;

public interface LocalizableConstants extends Constants, LocalizableCalendarConstants,
    LocalizableTransportConstants {

  String cancel();
  String changedValues();
  String changePassword();
  String clear();
  String createNewRow();
  String deselectAll();
  String doFilter();
  String filter();
  String newPassword();
  String newPasswordIsRequired();
  String newPasswordsDoesNotMatch();
  String newTab();
  String newValues();
  String no();
  String nothingFound();
  String ok();
  String oldPassword();
  String oldPasswordIsInvalid();
  String oldPasswordIsRequired();
  String questionLogout();
  String removeFilter();
  String repeatNewPassword();
  String saveChanges();
  String selectAll();
  String sorry();
  String tooLittleData();
  String yes();
}

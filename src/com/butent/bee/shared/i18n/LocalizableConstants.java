package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Constants;

/**
 * Determines a list of frequently used constants in the system, which must be translated into local
 * language, for example login, logout, no, refresh.
 */

public interface LocalizableConstants extends Constants {
  String cancel();
  String changePassword();
  String logout();
  String newPassword();
  String newPasswordIsRequired();
  String newPasswordsDoesNotMatch();
  String newTab();
  String no();
  String ok();
  String oldPassword();
  String oldPasswordIsInvalid();
  String oldPasswordIsRequired();
  String overlappingAppointments();
  String repeatNewPassword();
  String selectAppointment();
}

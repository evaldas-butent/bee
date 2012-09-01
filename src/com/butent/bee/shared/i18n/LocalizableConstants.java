package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Constants;

/**
 * Determines a list of frequently used constants in the system, which must be translated into local
 * language, for example login, logout, no, refresh.
 */

public interface LocalizableConstants extends Constants {
  String cancel();
  String changePassword();
  @Key("class")
  String clazz();
  String data();
  String login();
  String logout();
  String menu();
  String newPassword();
  String newPasswordIsRequired();
  String newPasswordsDoesNotMatch();
  String no();
  String notLoggedIn();
  String ok();
  String oldPassword();
  String oldPasswordIsInvalid();
  String oldPasswordIsRequired();
  String overlappingAppointments();
  String refresh();
  String repeatNewPassword();
  String selectAppointment();
  String tables();
  String user();
  String views();  
}

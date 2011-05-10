package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Constants;

/**
 * Determines a list of frequently used constants in the system, which must be translated into local
 * language, for example login, logout, no, refresh.
 */

public interface LocalizableConstants extends Constants {
  @Key("class")
  String clazz();

  String data();

  String login();

  String logout();

  String menu();

  String no();

  String notLoggedIn();

  String refresh();

  String tables();

  String user();

  String views();
}

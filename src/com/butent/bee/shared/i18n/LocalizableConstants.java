package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Constants;

import com.butent.bee.shared.modules.calendar.LocalizableCalendarConstants;
import com.butent.bee.shared.modules.crm.LocalizableCrmConstants;
import com.butent.bee.shared.modules.ec.LocalizableEcConstants;
import com.butent.bee.shared.modules.trade.LocalizableTradeConstants;
import com.butent.bee.shared.modules.transport.LocalizableTransportConstants;

public interface LocalizableConstants extends Constants, LocalizableCalendarConstants,
    LocalizableTransportConstants, LocalizableTradeConstants, LocalizableCrmConstants,
    LocalizableEcConstants {

  String address();

  String allowPhotoSize();

  String article();

  String cancel();

  String changedValues();

  String changePassword();

  String chooseFiles();

  String city();

  String clear();

  String client();

  String clients();

  String closeTab();

  String color();

  String colorIsInvalid();

  String companies();

  String company();

  String companyActivities();

  String companyCode();

  String companySize();

  String companyVATCode();

  String contact();

  String country();

  String createNewRow();

  String creator();

  String date();

  String description();

  String deselectAll();

  String doFilter();

  String email();

  String ended();

  String exchangeCode();

  String fax();

  String fileOriginalName();

  String fileSize();

  String fileType();

  String filter();

  String filterAll();

  String filterNotNullLabel();

  String filterNullLabel();

  String group();

  String initialFilter();

  String item();

  String location();

  String mobile();

  String name();

  String newPassword();

  String newPasswordIsRequired();

  String newPasswordsDoesNotMatch();

  String newTab();

  String newValues();

  String no();

  String noData();

  String nothingFound();

  String ok();

  String oldPassword();

  String oldPasswordIsInvalid();

  String oldPasswordIsRequired();

  String ordinal();

  String owner();

  String period();

  String persons();

  String phone();

  String postIndex();

  String priority();

  String questionLogout();

  String registered();

  String removeFilter();

  String repeatNewPassword();

  String resource();

  String responsibleEmployee();

  String responsiblePerson();

  String saveChanges();

  String saveFilter();

  String scheduledEndingDate();

  String scheduledEndingTime();

  String scheduledStartingDate();

  String scheduledStartingTime();

  String selectAll();

  String settings();

  String sorry();

  String startDate();

  String status();

  String style();

  String summary();

  String tabControl();

  String tooLittleData();

  String totalOf();

  String type();

  String unit();

  String userFullName();

  String valueRequired();

  String yes();
}

package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Constants;

import com.butent.bee.shared.modules.calendar.LocalizableCalendarConstants;
import com.butent.bee.shared.modules.crm.LocalizableCrmConstants;
import com.butent.bee.shared.modules.trade.LocalizableTradeConstants;
import com.butent.bee.shared.modules.transport.LocalizableTransportConstants;

public interface LocalizableConstants extends Constants, LocalizableCalendarConstants,
    LocalizableTransportConstants, LocalizableTradeConstants, LocalizableCrmConstants {

  String address();

  String allowablePhotoSize();

  String article();

  String cancel();

  String changedValues();

  String changePassword();

  String chooseFiles();

  String city();

  String clear();

  String client();

  String clients();

  String companies();

  String company();

  String companyCode();

  String companyVATCode();

  String contact();

  String country();

  String createNewRow();

  String date();

  String deselectAll();

  String doFilter();

  String email();

  String fax();

  String fileOriginalName();

  String fileSize();

  String fileType();

  String filter();

  String item();

  String mobile();

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

  String period();

  String persons();

  String phone();

  String postIndex();

  String questionLogout();

  String removeFilter();

  String repeatNewPassword();

  String saveChanges();

  String selectAll();

  String settings();

  String sorry();

  String tooLittleData();

  String totalOf();

  String unit();

  String userFullName();

  String valueRequired();

  String yes();
}

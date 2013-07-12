package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Messages;

import com.butent.bee.shared.modules.ec.LocalizableEcMessages;
import com.butent.bee.shared.modules.transport.LocalizableTransportMessages;

public interface LocalizableMessages extends Messages, LocalizableEcMessages,
    LocalizableTransportMessages {
  
  String allValuesEmpty(String label, String count);

  String allValuesIdentical(String label, String value, String count);

  String dataNotAvailable(String key);

  String endSession(String appName);
  
  String fileSizeExceeded(long size, long max);

  String invalidImageFileType(String fileName, String type);

  String keyNotFound(String key);

  String minSearchQueryLength(int min);

  String not(String value);

  String rowsRetrieved(int cnt);
}

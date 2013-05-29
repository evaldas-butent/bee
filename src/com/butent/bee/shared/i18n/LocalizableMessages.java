package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Messages;

public interface LocalizableMessages extends Messages {
  
  String allValuesEmpty(String label, String count);

  String allValuesIdentical(String label, String value, String count);

  String endSession(String appName);
  
  String exceededFileSize();

  String imageUploadFailed();

  String invalidImageFileType(String fileName);

  String keyNotFound(String key);

  String not(String value);

  String rowsRetrieved(int cnt);
}

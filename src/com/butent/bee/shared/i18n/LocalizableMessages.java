package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Messages;

import com.butent.bee.shared.modules.ec.LocalizableEcMessages;
import com.butent.bee.shared.modules.mail.LocalizableMailMessages;
import com.butent.bee.shared.modules.tasks.LocalizableTasksMessages;
import com.butent.bee.shared.modules.transport.LocalizableTransportMessages;

public interface LocalizableMessages extends Messages, LocalizableTasksMessages,
    LocalizableEcMessages, LocalizableMailMessages, LocalizableTransportMessages {

  String allValuesEmpty(String label, String count);

  String allValuesIdentical(String label, String value, String count);

  String dataNotAvailable(String key);

  String deletedRows(int count);

  String deleteSelectedRows(int count);

  String endSession(String appName);

  String fileNotFound(String file);

  String fileSizeExceeded(long size, long max);

  String invalidImageFileType(String fileName, String type);

  String keyNotFound(String key);

  String minSearchQueryLength(int min);

  String not(String value);

  String recordIsInUse(String place);

  String rowsRetrieved(int cnt);

  String rowsUpdated(int cnt);

  String valueExists(String value);
}

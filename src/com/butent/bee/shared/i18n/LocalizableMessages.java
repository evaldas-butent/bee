package com.butent.bee.shared.i18n;

import com.google.gwt.i18n.client.Messages;

/**
 * Determines necessary attributes for messages which can be localized to specified languages.
 */

public interface LocalizableMessages extends Messages {
  String keyNotFound(String key);

  String rowsRetrieved(int cnt);
}

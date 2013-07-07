package com.butent.bee.client.logging;

import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Formats log records depending on formatting settings.
 */

public class LogFormatter extends Formatter {

  public static final Level LOG_SEPARATOR_LEVEL = Level.INFO;
  public static final String LOG_SEPARATOR_TAG = "-";

  @Override
  public String format(LogRecord record) {
    if (record == null) {
      return null;
    } else if (isSeparator(record)) {
      return LOG_SEPARATOR_TAG;
    } else {
      return BeeUtils.joinWords(JsUtils.toTime(record.getMillis()),
          BeeUtils.clip(record.getMessage(), 256), record.getThrown());
    }
  }

  public boolean isSeparator(LogRecord record) {
    if (record == null) {
      return false;
    } else {
      return LOG_SEPARATOR_TAG.equals(record.getMessage());
    }
  }
}

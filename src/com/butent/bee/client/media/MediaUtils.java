package com.butent.bee.client.media;

import com.google.gwt.core.client.JavaScriptException;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.StringList;

public final class MediaUtils {

  private static final BeeLogger logger = LogUtils.getLogger(MediaUtils.class);

  public static String format(MediaStreamError error) {
    if (error == null) {
      return BeeConst.STRING_EMPTY;
    }

    StringList messages = StringList.uniqueCaseInsensitive();

    try {
      messages.add(error.getName());
    } catch (JavaScriptException ex) {
      logger.error(ex, NameUtils.getClassName(MediaStreamError.class), "getName");
    }

    try {
      messages.add(error.getMessage());
    } catch (JavaScriptException ex) {
      logger.error(ex, NameUtils.getClassName(MediaStreamError.class), "getMessage");
    }

    try {
      messages.add(error.getConstraintName());
    } catch (JavaScriptException ex) {
      logger.error(ex, NameUtils.getClassName(MediaStreamError.class), "getConstraintName");
    }

    return BeeUtils.joinWords(messages);
  }

  private MediaUtils() {
  }
}

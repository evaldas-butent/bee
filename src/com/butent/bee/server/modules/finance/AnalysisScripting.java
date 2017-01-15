package com.butent.bee.server.modules.finance;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.regex.Pattern;

final class AnalysisScripting {

  static final String VAR_IS_BUDGET = "_b";
  static final String VAR_CURRENT_VALUE = "_v";

  private static final Pattern currentValuePattern = getDetectionPattern(VAR_CURRENT_VALUE);

  static boolean find(String s, Pattern p) {
    if (s == null || p == null) {
      return false;
    } else {
      return p.matcher(s).find();
    }
  }

  static Pattern getDetectionPattern(String variable) {
    return BeeUtils.isEmpty(variable) ? null : Pattern.compile("\\b" + variable.trim() + "\\b");
  }

  static boolean isScriptPrimary(String script) {
    return find(script, currentValuePattern);
  }

  private AnalysisScripting() {
  }
}

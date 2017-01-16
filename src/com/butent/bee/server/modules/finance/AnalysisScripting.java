package com.butent.bee.server.modules.finance;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.finance.analysis.AnalysisUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

final class AnalysisScripting {

  static final String VAR_IS_BUDGET = "_b";
  static final String VAR_CURRENT_VALUE = "_v";

  private static final Pattern currentValuePattern = getDetectionPattern(VAR_CURRENT_VALUE);

  private static final int ROOT_LEVEL = 0;

  static Multimap<Integer, Long> buildCalculationSequence(Collection<BeeRow> input,
      int indicatorIndex, int abbreviationIndex, int scriptIndex, Consumer<String> errorHandler) {

    Multimap<Integer, Long> result = ArrayListMultimap.create();
    if (BeeUtils.isEmpty(input)) {
      return result;
    }

    Map<Long, String> scripts = new HashMap<>();
    Map<Long, Pattern> variables = new HashMap<>();

    for (BeeRow row : input) {
      if (!DataUtils.isId(row.getLong(indicatorIndex))) {
        String script = row.getString(scriptIndex);

        if (!BeeUtils.isEmpty(script) && !isScriptPrimary(script)) {
          scripts.put(row.getId(), script);

          String abbreviation = row.getString(abbreviationIndex);
          if (AnalysisUtils.isValidAbbreviation(abbreviation)) {
            variables.put(row.getId(), getDetectionPattern(abbreviation));
          }
        }
      }
    }

    if (scripts.isEmpty()) {
      return result;
    }

    if (variables.isEmpty()) {
      for (Long id : scripts.keySet()) {
        result.put(ROOT_LEVEL, id);
      }
      return result;
    }

    int level = ROOT_LEVEL;

    while (!scripts.isEmpty()) {
      Set<Long> ids = new HashSet<>(scripts.keySet());
      Set<Long> levelIds = new HashSet<>();

      for (Long id : ids) {
        String script = scripts.get(id);

        boolean free = true;
        for (Pattern pattern : variables.values()) {
          if (find(script, pattern)) {
            free = false;
            break;
          }
        }

        if (free) {
          levelIds.add(id);
        }
      }

      if (levelIds.isEmpty()) {
        break;
      }

      for (Long id : levelIds) {
        result.put(level, id);

        scripts.remove(id);
        variables.remove(id);
      }

      if (scripts.isEmpty()) {
        break;
      }

      level++;
    }

    if (!scripts.isEmpty() && errorHandler != null) {
      errorHandler.accept(BeeUtils.joinWords("recursive scripts:", scripts));
    }

    return result;
  }

  static boolean containsVariable(String script, String variable) {
    return find(script, getDetectionPattern(variable));
  }

  private static boolean find(String s, Pattern p) {
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

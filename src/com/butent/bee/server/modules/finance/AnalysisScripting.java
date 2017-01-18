package com.butent.bee.server.modules.finance;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.butent.bee.server.utils.ScriptUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplitType;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplitValue;
import com.butent.bee.shared.modules.finance.analysis.AnalysisUtils;
import com.butent.bee.shared.modules.finance.analysis.AnalysisValue;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptEngine;

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

  static AnalysisValue calculateUnboundValue(ScriptEngine engine, String script,
      long columnId, long rowId, boolean needsActual, boolean needsBudget,
      ResponseObject errorCollector) {

    String actualValue;
    if (needsActual) {
      actualValue = ScriptUtils.evalToString(engine,
          AnalysisScripting.createActualBindings(engine), script, errorCollector);
    } else {
      actualValue = null;
    }

    String budgetValue;
    if (needsBudget) {
      budgetValue = ScriptUtils.evalToString(engine,
          AnalysisScripting.createBudgetBindings(engine), script, errorCollector);
    } else {
      budgetValue = null;
    }

    if (BeeUtils.anyNotEmpty(actualValue, budgetValue)) {
      return AnalysisValue.of(columnId, rowId, actualValue, budgetValue);
    } else {
      return null;
    }
  }

  static List<AnalysisValue> calculateValues(ScriptEngine engine, String script,
      long columnId, long rowId,
      Collection<String> variables, Multimap<String, AnalysisValue> input,
      List<AnalysisSplitType> columnSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues,
      List<AnalysisSplitType> rowSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues,
      boolean needsActual, boolean needsBudget, ResponseObject errorCollector) {

    List<AnalysisValue> values = new ArrayList<>();

    boolean hasColumnSplits = !BeeUtils.isEmpty(columnSplitTypes)
        && !BeeUtils.isEmpty(columnSplitValues);
    boolean hasRowSplits = !BeeUtils.isEmpty(rowSplitTypes) && !BeeUtils.isEmpty(rowSplitValues);

    if (hasColumnSplits && hasRowSplits) {
      AnalysisSplitValue.getPermutations(null, columnSplitTypes, 0, columnSplitValues, 0);
      AnalysisSplitValue.getPermutations(null, rowSplitTypes, 0, rowSplitValues, 0);

    } else if (hasColumnSplits) {
      AnalysisSplitValue.getPermutations(null, columnSplitTypes, 0, columnSplitValues, 0);

    } else if (hasRowSplits) {
      AnalysisSplitValue.getPermutations(null, rowSplitTypes, 0, rowSplitValues, 0);

    } else {
      Map<String, Double> actualValues = new HashMap<>();
      Map<String, Double> budgetValues = new HashMap<>();

      variables.forEach(key -> {
        actualValues.put(key, BeeConst.DOUBLE_ZERO);
        budgetValues.put(key, BeeConst.DOUBLE_ZERO);
      });

      input.forEach((key, value) -> {
        if (needsActual && value.hasActualValue()) {
          actualValues.merge(key, value.getActualNumber(), Double::sum);
        }
        if (needsBudget && value.hasBudgetValue()) {
          budgetValues.merge(key, value.getBudgetNumber(), Double::sum);
        }
      });

      Double actualValue;
      if (needsActual) {
        Bindings actualBindings = createActualBindings(engine);
        actualBindings.putAll(actualValues);

        actualValue = ScriptUtils.evalToDouble(engine, actualBindings, script, errorCollector);
      } else {
        actualValue = null;
      }

      Double budgetValue;
      if (needsBudget) {
        Bindings budgetBindings = createBudgetBindings(engine);
        budgetBindings.putAll(budgetValues);

        budgetValue = ScriptUtils.evalToDouble(engine, budgetBindings, script, errorCollector);
      } else {
        budgetValue = null;
      }

      if (BeeUtils.nonZero(actualValue) || BeeUtils.nonZero(budgetValue)) {
        values.add(AnalysisValue.of(columnId, rowId, actualValue, budgetValue));
      }
    }

    return values;
  }

  static Bindings createActualBindings(ScriptEngine engine) {
    return createActualOrBudgetBindings(engine, false);
  }

  static Bindings createBudgetBindings(ScriptEngine engine) {
    return createActualOrBudgetBindings(engine, true);
  }

  private static Bindings createActualOrBudgetBindings(ScriptEngine engine, boolean isBudget) {
    Bindings bindings = (engine == null) ? null : engine.createBindings();
    if (bindings != null) {
      bindings.put(VAR_IS_BUDGET, isBudget);
    }
    return bindings;
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

  static Multimap<String, AnalysisValue> transformInput(Multimap<Long, AnalysisValue> values,
      Map<Long, String> variables) {

    Multimap<String, AnalysisValue> result = ArrayListMultimap.create();

    for (long id : values.keySet()) {
      String variable = variables.get(id);
      if (!BeeUtils.isEmpty(variable)) {
        result.putAll(variable, values.get(id));
      }
    }

    return result;
  }

  private AnalysisScripting() {
  }
}

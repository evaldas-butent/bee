package com.butent.bee.client.ui;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JavaScriptObject;

import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.ConditionalStyleDeclaration;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class ConditionalStyle {
  
  private class Entry {
    private final StyleDescriptor styleDescriptor;
    private final Evaluator evaluator;
    
    private final JavaScriptObject styleInterpreter;

    private Entry(StyleDescriptor styleDescriptor, Evaluator evaluator) {
      this.styleDescriptor = styleDescriptor;
      this.evaluator = evaluator;
      this.styleInterpreter = initInterpreter(styleDescriptor);
    }

    private Evaluator getEvaluator() {
      return evaluator;
    }

    private StyleDescriptor getStyleDescriptor() {
      return styleDescriptor;
    }

    private JavaScriptObject getStyleInterpreter() {
      return styleInterpreter;
    }
    
    private boolean hasStyleInterpreter() {
      return getStyleInterpreter() != null;
    }
    
    private boolean hasStyleReplacement() {
      if (getStyleDescriptor() == null) {
        return false;
      }
      String s = getStyleDescriptor().getInline();
      if (BeeUtils.isEmpty(s)) {
        return false;
      }
      return s.contains(Evaluator.DEFAULT_REPLACE_PREFIX)
          && s.contains(Evaluator.DEFAULT_REPLACE_SUFFIX);
    }

    private JavaScriptObject initInterpreter(StyleDescriptor sd) {
      if (sd == null || BeeUtils.isEmpty(STYLE_INTERPRETER_PREFIX)) {
        return null;
      }
      String s = sd.getInline();
      if (BeeUtils.isEmpty(s) || !s.startsWith(STYLE_INTERPRETER_PREFIX)) {
        return null;
      }

      String xpr = s.substring(STYLE_INTERPRETER_PREFIX.length());
      if (BeeUtils.isEmpty(xpr)) {
        return null;
      }
      return Evaluator.createFuncInterpreter("return " + xpr.trim());
    }
  }

  private static final String STYLE_INTERPRETER_PREFIX = "=";

  public static ConditionalStyle create(Collection<ConditionalStyleDeclaration> declarations,
      String colName, List<? extends IsColumn> dataColumns) {
    if (declarations == null || declarations.isEmpty()) {
      return null;
    }

    ConditionalStyle conditionalStyle = null;
    for (ConditionalStyleDeclaration csd : declarations) {
      if (csd == null || !csd.validState()) {
        continue;
      }
      if (conditionalStyle == null) {
        conditionalStyle = new ConditionalStyle();
      }
      conditionalStyle.addStyle(StyleDescriptor.copyOf(csd.getStyle()),
          Evaluator.create(csd.getCondition(), colName, dataColumns));
    }
    return conditionalStyle;
  }

  private final List<Entry> entries = Lists.newArrayList();

  private ConditionalStyle() {
  }

  public StyleDescriptor getStyleDescriptor(IsRow rowValue, int rowIndex, int colIndex) {
    return getStyleDescriptor(rowValue, rowIndex, colIndex, false, null, null);
  }

  public StyleDescriptor getStyleDescriptor(IsRow rowValue, int rowIndex, int colIndex,
      ValueType cellType, String cellValue) {
    return getStyleDescriptor(rowValue, rowIndex, colIndex, true, cellType, cellValue);
  }

  private void addEntry(Entry entry) {
    if (entry != null) {
      getEntries().add(entry);
    }
  }

  private void addStyle(StyleDescriptor sd, Evaluator ev) {
    if (sd != null && ev != null) {
      addEntry(new Entry(sd, ev));
    }
  }

  private List<Entry> getEntries() {
    return entries;
  }

  private StyleDescriptor getStyleDescriptor(IsRow rowValue, int rowIndex, int colIndex,
      boolean updateCell, ValueType cellType, String cellValue) {
    if (rowValue == null || getEntries().isEmpty()) {
      return null;
    }

    for (Entry entry : getEntries()) {
      if (updateCell) {
        entry.getEvaluator().update(rowValue, rowIndex, colIndex, cellType, cellValue);
      } else {
        entry.getEvaluator().update(rowValue, rowIndex, colIndex);
      }

      String z = entry.getEvaluator().evaluate();
      if (!BeeUtils.toBoolean(z)) {
        continue;
      }

      if (!entry.hasStyleInterpreter() && !entry.hasStyleReplacement()) {
        return entry.getStyleDescriptor();
      }

      StyleDescriptor copy = StyleDescriptor.copyOf(entry.getStyleDescriptor());
      String replaced;
      if (entry.hasStyleInterpreter()) {
        replaced = entry.getEvaluator().evaluate(entry.getStyleInterpreter());
      } else {  
        replaced = entry.getEvaluator().replace(copy.getInline());
      }
      copy.setInline(replaced);

      return copy;
    }
    return null;
  }
}

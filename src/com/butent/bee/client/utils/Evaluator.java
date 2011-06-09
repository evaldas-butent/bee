package com.butent.bee.client.utils;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Implements management of an old and new values for cells in user interface.
 */

public class Evaluator extends Calculation {

  /**
   * Manages default values for such parameters as rowId, rowVersion or CellValue.
   */

  public class DefaultParameters implements Parameters {

    private final List<? extends IsColumn> dataColumns;

    private final JavaScriptObject rowValues;
    private double rowId;
    private JsDate rowVersion;
    private double rowIndex;

    private final String colName;
    private double colIndex;

    private final JavaScriptObject cellValues;

    private IsRow lastRow = null;
    private String lastCellValue = null;
    private String lastOldValue = null;
    private String lastNewValue = null;

    {
      cellValues = JavaScriptObject.createObject();
      JsUtils.setPropertyToNull(cellValues, PROPERTY_VALUE);
      JsUtils.setPropertyToNull(cellValues, PROPERTY_OLD_VALUE);
      JsUtils.setPropertyToNull(cellValues, PROPERTY_NEW_VALUE);
    }

    public DefaultParameters(String colName, List<? extends IsColumn> dataColumns) {
      this.colName = colName;
      this.dataColumns = dataColumns;
      this.rowValues = EvalHelper.createJso(dataColumns);
    }

    public JavaScriptObject getCellValues() {
      return cellValues;
    }

    public double getColIndex() {
      return colIndex;
    }

    public String getColName() {
      return colName;
    }

    public List<? extends IsColumn> getDataColumns() {
      return dataColumns;
    }

    public String getLastCellValue() {
      return lastCellValue;
    }

    public String getLastNewValue() {
      return lastNewValue;
    }

    public String getLastOldValue() {
      return lastOldValue;
    }

    public IsRow getLastRow() {
      return lastRow;
    }

    public double getRowId() {
      return rowId;
    }

    public double getRowIndex() {
      return rowIndex;
    }

    public JavaScriptObject getRowValues() {
      return rowValues;
    }

    public JsDate getRowVersion() {
      return rowVersion;
    }

    public void setCellNewValue(ValueType type, String newValue) {
      setLastNewValue(newValue);
      EvalHelper.setJsoProperty(cellValues, PROPERTY_NEW_VALUE, type, newValue);
    }

    public void setCellOldValue(ValueType type, String oldValue) {
      setLastOldValue(oldValue);
      EvalHelper.setJsoProperty(cellValues, PROPERTY_OLD_VALUE, type, oldValue);
    }

    public void setCellValue(ValueType type, String value) {
      setLastCellValue(value);
      EvalHelper.setJsoProperty(cellValues, PROPERTY_VALUE, type, value);
    }

    public void setColIndex(double colIndex) {
      this.colIndex = colIndex;
    }

    public void setRowIndex(double rowIndex) {
      this.rowIndex = rowIndex;
    }

    public void updateRow(IsRow row) {
      setLastRow(row);
      if (row == null) {
        return;
      }

      setRowId(row.getId());
      setRowVersion(JsDate.create(row.getVersion()));

      EvalHelper.toJso(dataColumns, row, rowValues);
    }

    private void setLastCellValue(String lastCellValue) {
      this.lastCellValue = lastCellValue;
    }

    private void setLastNewValue(String lastNewValue) {
      this.lastNewValue = lastNewValue;
    }

    private void setLastOldValue(String lastOldValue) {
      this.lastOldValue = lastOldValue;
    }

    private void setLastRow(IsRow lastRow) {
      this.lastRow = lastRow;
    }

    private void setRowId(double rowId) {
      this.rowId = rowId;
    }

    private void setRowVersion(JsDate rowVersion) {
      this.rowVersion = rowVersion;
    }
  }

  public interface Parameters {

    JavaScriptObject getCellValues();

    double getColIndex();

    String getColName();

    List<? extends IsColumn> getDataColumns();

    String getLastCellValue();

    String getLastNewValue();

    String getLastOldValue();

    IsRow getLastRow();

    double getRowId();

    double getRowIndex();

    JavaScriptObject getRowValues();

    JsDate getRowVersion();

    void setCellNewValue(ValueType type, String newValue);

    void setCellOldValue(ValueType type, String oldValue);

    void setCellValue(ValueType type, String value);

    void setColIndex(double colIndex);

    void setRowIndex(double rowIndex);

    void updateRow(IsRow rowValue);
  }

  public static final String CELL_OBJECT = "cell";
  public static final String ROW_OBJECT = "row";
  public static final String PROPERTY_VALUE = "value";

  public static final String PROPERTY_OLD_VALUE = "oldValue";
  public static final String PROPERTY_NEW_VALUE = "newValue";
  public static final String VAR_COL_ID = "colName";

  public static final String VAR_ROW_ID = "rowId";
  public static final String VAR_ROW_VERSION = "rowVersion";

  public static final String VAR_ROW_INDEX = "rowIndex";

  public static final String VAR_COL_INDEX = "colIndex";

  public static final String DEFAULT_REPLACE_PREFIX = "[";
  public static final String DEFAULT_REPLACE_SUFFIX = "]";
  public static final String PROPERTY_SEPARATOR = ".";

  public static Evaluator create(Calculation calc, String colName,
      List<? extends IsColumn> dataColumns) {
    if (calc == null || calc.isEmpty()) {
      return null;
    }

    Evaluator evaluator = new Evaluator(calc.getExpression(), calc.getFunction());
    if (dataColumns != null && !dataColumns.isEmpty()) {
      evaluator.init(colName, dataColumns);
    }
    return evaluator;
  }

  public static native JavaScriptObject createExprInterpreter(String xpr) /*-{
    return new Function("row", "rowId", "rowVersion", "rowIndex", "colName", "colIndex", "cell",
        "return eval(" + xpr + ");");
  }-*/;

  public static native JavaScriptObject createFuncInterpreter(String fnc) /*-{
    return new Function("row", "rowId", "rowVersion", "rowIndex", "colName", "colIndex", "cell",
        fnc);
  }-*/;

  private Parameters parameters = null;

  private final JavaScriptObject interpeter;

  private Evaluator(String expression, String function) {
    super(expression, function);

    if (!BeeUtils.isEmpty(expression)) {
      this.interpeter = createExprInterpreter(expression);
    } else if (!BeeUtils.isEmpty(function)) {
      this.interpeter = createFuncInterpreter(function);
    } else {
      this.interpeter = null;
    }
  }

  public String evaluate() {
    return evaluate(getInterpeter());
  }

  public String evaluate(JavaScriptObject fnc) {
    if (getParameters() == null || fnc == null) {
      return null;
    }

    String s;
    try {
      s = doEval(fnc, getParameters().getRowValues(), getParameters().getRowId(),
          getParameters().getRowVersion(), getParameters().getRowIndex(),
          getParameters().getColName(), getParameters().getColIndex(),
          getParameters().getCellValues());
    } catch (JavaScriptException ex) {
      BeeKeeper.getLog().warning("Evaluator:", ex.getMessage(), getExpression(), getFunction());
      s = null;
    }
    return s;
  }

  public void init(String colName, List<? extends IsColumn> dataColumns) {
    Assert.notNull(dataColumns);
    setParameters(new DefaultParameters(colName, dataColumns));
  }

  public String replace(String src) {
    return replace(src, DEFAULT_REPLACE_PREFIX, DEFAULT_REPLACE_SUFFIX);
  }

  public String replace(String src, String prefix, String suffix) {
    if (BeeUtils.isEmpty(src) || getParameters() == null) {
      return src;
    }
    String pfx = Strings.nullToEmpty(prefix);
    String sfx = Strings.nullToEmpty(suffix);

    if (pfx.length() > 0 && !src.contains(pfx)) {
      return src;
    }
    if (sfx.length() > 0 && !src.contains(sfx)) {
      return src;
    }

    String result = src.trim();
    String value;

    IsRow row = getParameters().getLastRow();
    if (row != null) {
      result = BeeUtils.replace(result, pfx + VAR_ROW_ID + sfx, BeeUtils.toString(row.getId()));
      result = BeeUtils.replace(result, pfx + VAR_ROW_VERSION + sfx,
          BeeUtils.toString(row.getVersion()));

      List<? extends IsColumn> columns = getParameters().getDataColumns();
      for (int i = 0; i < columns.size(); i++) {
        IsColumn column = columns.get(i);
        value = BeeUtils.ifString(row.getString(i), BeeConst.NULL);
        result = BeeUtils.replace(result,
            pfx + ROW_OBJECT + PROPERTY_SEPARATOR + column.getLabel() + sfx, value);
      }
    }

    result = BeeUtils.replace(result, pfx + VAR_ROW_INDEX + sfx,
        BeeUtils.toString(getParameters().getRowIndex()));
    result = BeeUtils.replace(result, pfx + VAR_COL_INDEX + sfx,
        BeeUtils.toString(getParameters().getColIndex()));

    value = BeeUtils.ifString(getParameters().getColName(), BeeConst.NULL);
    result = BeeUtils.replace(result, pfx + VAR_COL_ID + sfx, value);

    value = BeeUtils.ifString(getParameters().getLastCellValue(), BeeConst.NULL);
    result = BeeUtils.replace(result,
        pfx + CELL_OBJECT + PROPERTY_SEPARATOR + PROPERTY_VALUE + sfx, value);
    value = BeeUtils.ifString(getParameters().getLastOldValue(), BeeConst.NULL);
    result = BeeUtils.replace(result,
        pfx + CELL_OBJECT + PROPERTY_SEPARATOR + PROPERTY_OLD_VALUE + sfx, value);
    value = BeeUtils.ifString(getParameters().getLastNewValue(), BeeConst.NULL);
    result = BeeUtils.replace(result,
        pfx + CELL_OBJECT + PROPERTY_SEPARATOR + PROPERTY_NEW_VALUE + sfx, value);

    return result;
  }

  public void setParameters(Parameters parameters) {
    this.parameters = parameters;
  }

  public void update(IsRow rowValue) {
    if (rowValue == null || getParameters() == null) {
      return;
    }
    getParameters().updateRow(rowValue);
  }

  public void update(IsRow rowValue, int rowIndex, int colIndex) {
    if (getParameters() == null) {
      return;
    }
    update(rowValue);
    getParameters().setRowIndex(rowIndex);
    getParameters().setColIndex(colIndex);
  }

  public void update(IsRow rowValue, int rowIndex, int colIndex, ValueType type, String value) {
    if (getParameters() == null) {
      return;
    }
    update(rowValue, rowIndex, colIndex);
    getParameters().setCellValue(type, value);
  }

  public void update(IsRow rowValue, int rowIndex, int colIndex, ValueType type,
      String oldValue, String newValue) {
    if (getParameters() == null) {
      return;
    }
    update(rowValue, rowIndex, colIndex);

    getParameters().setCellValue(type, newValue);
    getParameters().setCellOldValue(type, oldValue);
    getParameters().setCellNewValue(type, newValue);
  }

  private native String doEval(JavaScriptObject fnc, JavaScriptObject row, double rowId,
      JsDate rowVersion, double rowIndex, String colName, double colIndex,
      JavaScriptObject cell) /*-{
    var result = fnc(row, rowId, rowVersion, rowIndex, colName, colIndex, cell);
    if (result == null) {
      return result;
    }
    if (result.getTime) {
      return String(result.getTime());
    }
    return String(result);
  }-*/;

  private JavaScriptObject getInterpeter() {
    return interpeter;
  }

  private Parameters getParameters() {
    return parameters;
  }
}

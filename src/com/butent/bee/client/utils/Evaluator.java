package com.butent.bee.client.utils;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class Evaluator<T extends Value> extends Calculation {
  
  public class DefaultParameters implements Parameters {
    
    private final List<? extends IsColumn> dataColumns;
    
    private final JavaScriptObject rowValues;
    private double rowId;
    private JsDate rowVersion;
    private double rowIndex;
    
    private final String colName;
    private double colIndex;
    
    private final JavaScriptObject cellValues;
    
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
    
    public void setColIndex(double colIndex) {
      this.colIndex = colIndex;
    }

    public void setRowIndex(double rowIndex) {
      this.rowIndex = rowIndex;
    }

    public void updateRow(IsRow row) {
      if (row == null) {
        return;
      }
      setRowId(row.getId());
      setRowVersion(JsDate.create(row.getVersion()));
      
      EvalHelper.toJso(dataColumns, row, rowValues);
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
    
    double getRowId();
    
    double getRowIndex();
    
    JavaScriptObject getRowValues();

    JsDate getRowVersion();

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
  
  public static <T extends Value> Evaluator<T> create(Calculation calc) {
    if (calc == null || calc.isEmpty()) {
      return null;
    }
    return new Evaluator<T>(calc.getType(), calc.getExpression(), calc.getFunction());
  }
  
  private static native JavaScriptObject createExprInterpreter(String xpr) /*-{
    return new Function("row", "rowId", "rowVersion", "rowIndex",
      "colName", "colIndex", "cell", "return eval(" + xpr + ");");
  }-*/;
  
  private static native JavaScriptObject createFuncInterpreter(String fnc) /*-{
    return new Function("row", "rowId", "rowVersion", "rowIndex",
      "colName", "colIndex", "cell", fnc);
  }-*/;
  private Parameters parameters = null;
  
  private final JavaScriptObject interpeter;

  private Evaluator(ValueType type, String expression, String function) {
    super(type, expression, function);

    if (!BeeUtils.isEmpty(expression)) {
      this.interpeter = createExprInterpreter(expression);
    } else if (!BeeUtils.isEmpty(function)) {
      this.interpeter = createFuncInterpreter(function);
    } else {
      this.interpeter = null;
    }
  }
  
  public T evaluate() {
    if (getParameters() == null || getInterpeter() == null) {
      return null;
    }
    
    JavaScriptObject rowValues = getParameters().getRowValues();
    double rowId = getParameters().getRowId();
    JsDate rowVersion = getParameters().getRowVersion();
    double rowIndex = getParameters().getRowIndex();
    
    String colName = getParameters().getColName();
    double colIndex = getParameters().getColIndex();
    JavaScriptObject cellValues = getParameters().getCellValues();
    
    String s = doEval(getInterpeter(), rowValues, rowId, rowVersion, rowIndex, colName, colIndex,
        cellValues);
    if (s == null) {
      return null;
    } else {
      return (T) Value.parseValue(getType(), s);
    }
  }
  
  public void init(String colName, List<? extends IsColumn> dataColumns) {
    Assert.notNull(dataColumns);
    setParameters(new DefaultParameters(colName, dataColumns));
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
  
  private native String doEval(JavaScriptObject fnc, JavaScriptObject row, double rowId,
      JsDate rowVersion, double rowIndex, String colName, double colIndex,
      JavaScriptObject cell) /*-{
    var result = null;
    try {
      result = fnc(row, rowId, rowVersion, rowIndex, colName, colIndex, cell);
    } catch (err) {
      result = err.toString()
    }
    
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

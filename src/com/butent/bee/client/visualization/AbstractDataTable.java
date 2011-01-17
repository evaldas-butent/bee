package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;

import com.butent.bee.client.visualization.TimeOfDay.BadTimeException;

import java.util.Date;

public class AbstractDataTable extends JavaScriptObject {
  public enum ColumnType {
    BOOLEAN("boolean"),
    DATE("date"),
    DATETIME("datetime"),
    NUMBER("number"),
    STRING("string"),
    TIMEOFDAY("timeofday");

    static ColumnType getColumnTypeFromString(String parameter) {
      return ColumnType.valueOf(parameter.toUpperCase());
    }

    private final String parameter;
    ColumnType(String parameter) {
      this.parameter = parameter;
    }

    String getParameter() {
      return parameter;
    }
  }

  protected AbstractDataTable() {
  }

  public final native String getColumnId(int columnIndex) /*-{
    return this.getColumnId(columnIndex);
  }-*/;
  
  public final native int getColumnIndex(String columnId) /*-{
    return this.getColumnIndex(columnId);
  }-*/;

  public final native String getColumnLabel(int columnIndex) /*-{
    return this.getColumnLabel(columnIndex);
  }-*/;

  public final native String getColumnPattern(int columnIndex) /*-{
    return this.getColumnPattern(columnIndex);
  }-*/;

  public final native Range getColumnRange(int columnIndex) /*-{
    return this.getColumnRange(columnIndex);
  }-*/;

  public final ColumnType getColumnType(int columnIndex) {
    return ColumnType.getColumnTypeFromString(getColumnTypeAsString(columnIndex));
  }

  public final native String getFormattedValue(int rowIndex, int columnIndex) /*-{
    return this.getFormattedValue(rowIndex, columnIndex);
  }-*/;

  public final native int getNumberOfColumns() /*-{
    return this.getNumberOfColumns();
  }-*/;

  public final native int getNumberOfRows() /*-{
    return this.getNumberOfRows();
  }-*/;

  public final native String getProperty(int rowIndex, int columnIndex,
      String name) /*-{
    return this.getProperty(rowIndex, columnIndex, name);
  }-*/;

  public final native boolean getValueBoolean(int rowIndex, int columnIndex) /*-{
    return this.getValue(rowIndex, columnIndex);
  }-*/;

  public final Date getValueDate(int rowIndex, int columnIndex) {
    JsArrayNumber timevalue = getValueTimevalue(rowIndex, columnIndex);
    if (timevalue.length() == 0) {
      return null;
    } else {
      return new Date((long) timevalue.get(0));
    }
  }

  public final native double getValueDouble(int rowIndex, int columnIndex) /*-{
    return this.getValue(rowIndex, columnIndex);
  }-*/;

  public final native int getValueInt(int rowIndex, int columnIndex) /*-{
    return this.getValue(rowIndex, columnIndex);
  }-*/;

  public final native String getValueString(int rowIndex, int columnIndex) /*-{
    return this.getValue(rowIndex, columnIndex);
  }-*/;

  public final TimeOfDay getValueTimeOfDay(int rowIndex, int columnIndex) {
    JsArrayInteger jsArray = getValueArrayInteger(rowIndex, columnIndex);
    if (jsArray == null) {
      return null;
    }
    TimeOfDay result = new TimeOfDay();
    try {
      result.setHour(jsArray.get(0));
      result.setMinute(jsArray.get(1));
      result.setSecond(jsArray.get(2));
      result.setMillisecond(jsArray.get(3));
    } catch (BadTimeException e) {
      throw new RuntimeException("Invalid time of day.");
    }
    return result;
  }

  public final native boolean isValueNull(int rowIndex, int columnIndex) /*-{
    return this.getValue(rowIndex, columnIndex) == null;
  }-*/;

  private native String getColumnTypeAsString(int columnIndex)/*-{
    return this.getColumnType(columnIndex);
  }-*/;

  private native JsArrayInteger getValueArrayInteger(int rowIndex, int columnIndex) /*-{
    return this.getValue(rowIndex, columnIndex);
  }-*/;

  private native JsArrayNumber getValueTimevalue(int rowIndex, int columnIndex) /*-{
    var value = this.getValue(rowIndex, columnIndex);
    if (value == null) {
      return [];
    } else {
      return [value.getTime()];
    }
  }-*/;
}

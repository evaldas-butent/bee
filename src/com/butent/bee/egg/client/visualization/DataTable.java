package com.butent.bee.egg.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;

import com.butent.bee.egg.client.ajaxloader.ArrayHelper;
import com.butent.bee.egg.client.ajaxloader.Properties;
import com.butent.bee.egg.shared.BeeDate;

import java.util.Date;

public class DataTable extends AbstractDataTable {

  public static native DataTable create() /*-{
    return new $wnd.google.visualization.DataTable();
  }-*/;

  public static native DataTable create(JavaScriptObject jso, double version) /*-{
    return new $wnd.google.visualization.DataTable(jso, version);
  }-*/;

  public static native DataTable create(JavaScriptObject jso) /*-{
    return new $wnd.google.visualization.DataTable(jso);
  }-*/;

  protected DataTable() {
  }

  public final int addColumn(ColumnType type) {
    return addColumn(type.getParameter());
  }

  public final int addColumn(ColumnType type, String label) {
    return addColumn(type.getParameter(), label);
  }

  public final int addColumn(ColumnType type, String label, String id) {
    return addColumn(type.getParameter(), label, id);
  }

  public final native int addRow() /*-{
    return this.addRow();
  }-*/;

  public final native int addRows(int number) /*-{
    return this.addRows(number);
  }-*/;

  public final void insertColumn(int columnIndex, ColumnType type) {
    insertColumn(columnIndex, type.getParameter());
  }

  public final void insertColumn(int columnIndex, ColumnType type, String label) {
    insertColumn(columnIndex, type.getParameter(), label);
  }

  public final void insertColumn(int columnIndex, ColumnType type, String label, String id) {
    insertColumn(columnIndex, type.getParameter(), label, id);
  }

  public final native void insertRows(int rowIndex, int numberOfRows) /*-{
    this.insertRows(rowIndex, numberOfRows);
  }-*/;

  public final native void removeColumn(int columnIndex) /*-{
    this.removeColumn(columnIndex);
  }-*/;

  public final native void removeColumns(int columnIndex, int numberOfColumns) /*-{
    this.removeColumns(columnIndex, numberOfColumns);
  }-*/;

  public final native void removeRow(int rowIndex) /*-{
    this.removeRow(rowIndex);
  }-*/;

  public final native void removeRows(int rowIndex, int numberOfRows) /*-{
    this.removeRows(rowIndex, numberOfRows);
  }-*/;

  public final native void setCell(int rowIndex, int columnIndex,
      boolean value, String formattedValue, Properties properties) /*-{
    this.setCell(rowIndex, columnIndex, value, formattedValue, properties);
  }-*/;

  public final void setCell(int rowIndex, int columnIndex, Date value,
      String formattedValue, Properties properties) {
    if (value == null) {
      setCellNull(rowIndex, columnIndex, formattedValue, properties);
    } else {
      setCellDate(rowIndex, columnIndex, value.getTime(), formattedValue, properties);
    }
  }

  public final native void setCell(int rowIndex, int columnIndex, double value,
      String formattedValue, Properties properties) /*-{
    this.setCell(rowIndex, columnIndex, value, formattedValue, properties);
  }-*/;

  public final native void setCell(int rowIndex, int columnIndex, int value,
      String formattedValue, Properties properties) /*-{
    this.setCell(rowIndex, columnIndex, value, formattedValue, properties);
  }-*/;

  public final native void setCell(int rowIndex, int columnIndex, String value,
      String formattedValue, Properties properties) /*-{
    this.setCell(rowIndex, columnIndex, value, formattedValue, properties);
  }-*/;

  public final void setCell(int rowIndex, int columnIndex, TimeOfDay value,
      String formattedValue, Properties properties) {
    if (value == null) {
      setCellNull(rowIndex, columnIndex, formattedValue, properties);
    } else {
      int[] integers = new int[]{
          value.getHour(), value.getMinute(), value.getSecond(), value.getMillisecond()};
      setCell(rowIndex, columnIndex, ArrayHelper.toJsArrayInteger(integers),
          formattedValue, properties);
    }
  }

  public final native void setCellNull(int rowIndex, int columnIndex,
      String formattedValue, Properties properties) /*-{
    this.setCell(rowIndex, columnIndex, null, formattedValue, properties);
  }-*/;

  public final native void setColumnLabel(int columnIndex, String label) /*-{
    this.setColumnLabel(columnIndex, label);
  }-*/;

  public final void setDate(int rowIndex, int columnIndex, BeeDate date) {
    if (date == null) {
      setValueNull(rowIndex, columnIndex);
    } else {
      setValueDate(rowIndex, columnIndex, date.getTime());
    }
  }

  public final void setDate(int rowIndex, int columnIndex, double time) {
    setValueDate(rowIndex, columnIndex, time);
  }

  public final native void setFormattedValue(int rowIndex, int columnIndex, String value) /*-{
    this.setFormattedValue(rowIndex, columnIndex, value);
  }-*/;

  public final native void setProperties(int rowIndex, int columnIndex, Properties properties) /*-{
    this.setProperties(rowIndex, columnIndex, properties);
  }-*/;

  public final native void setProperty(int rowIndex, int columnIndex,
      String name, String value) /*-{
    this.setProperty(rowIndex, columnIndex, name, value);
  }-*/;

  public final native void setValue(int rowIndex, int columnIndex, boolean value) /*-{
    this.setValue(rowIndex, columnIndex, value);
  }-*/;

  public final void setValue(int rowIndex, int columnIndex, Date date) {
    if (date == null) {
      setValueNull(rowIndex, columnIndex);
    } else {
      setValueDate(rowIndex, columnIndex, date.getTime());
    }
  }

  public final native void setValue(int rowIndex, int columnIndex, double value) /*-{
    this.setValue(rowIndex, columnIndex, value);
  }-*/;

  public final native void setValue(int rowIndex, int columnIndex, int value) /*-{
    this.setValue(rowIndex, columnIndex, value);
  }-*/;

  public final native void setValue(int rowIndex, int columnIndex, String value) /*-{
    this.setValue(rowIndex, columnIndex, value);
  }-*/;

  public final void setValue(int rowIndex, int columnIndex, TimeOfDay value) {
    if (value == null) {
      setValueNull(rowIndex, columnIndex);
    } else {
      setValue(rowIndex, columnIndex, value.getHour(), value.getMinute(),
          value.getSecond(), value.getMillisecond());
    }
  }

  final native void setValueNull(int rowIndex, int columnIndex) /*-{
    this.setValue(rowIndex, columnIndex, null);
  }-*/;

  private native int addColumn(String type) /*-{
    return this.addColumn(type);
  }-*/;

  private native int addColumn(String type, String label) /*-{
    return this.addColumn(type, label);
  }-*/;

  private native int addColumn(String type, String label, String id) /*-{
    return this.addColumn(type, label, id);
  }-*/;

  private native void insertColumn(int columnIndex, String type) /*-{
    return this.insertColumn(columnIndex, type);
  }-*/;

  private native void insertColumn(int columnIndex, String type, String label) /*-{
    return this.insertColumn(columnIndex, type, label);
  }-*/;

  private native void insertColumn(int columnIndex, String type, String label, String id) /*-{
    return this.insertColumn(columnIndex, type, label, id);
  }-*/;

  private native void setCell(int rowIndex, int columnIndex,
      JsArrayInteger value, String formattedValue, Properties properties) /*-{
    this.setCell(rowIndex, columnIndex, value, formattedValue, properties);
  }-*/;

  private native void setCellDate(int rowIndex, int columnIndex, double value,
      String formattedValue, Properties properties) /*-{
    var newDate = new $wnd.Date(value);
    newDate.constructor = $wnd.Date;
    this.setCell(rowIndex, columnIndex, newDate, formattedValue, properties);
  }-*/;

  private native void setValue(int rowIndex, int columnIndex, int hour,
      int minute, int second, int millis) /*-{
    var newArray = new $wnd.Array(hour, minute, second, millis);
    newArray.constructor = $wnd.Array;
    this.setValue(rowIndex, columnIndex, newArray);
  }-*/;

  private native void setValueDate(int rowIndex, int columnIndex, double value) /*-{
    var newDate = new $wnd.Date(value);
    newDate.constructor = $wnd.Date;
    this.setValue(rowIndex, columnIndex, newDate);
  }-*/;
}

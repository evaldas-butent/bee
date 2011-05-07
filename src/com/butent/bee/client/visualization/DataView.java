package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JsArrayInteger;

import com.butent.bee.client.ajaxloader.ArrayHelper;

/**
 * Allows to use a live windows to view contents of {@code DataTable} objects.
 */

public class DataView extends AbstractDataTable {

  public static native DataView create(AbstractDataTable table) /*-{
    return new $wnd.google.visualization.DataView(table);
  }-*/;

  protected DataView() {
  }

  public final native int getTableColumnIndex(int viewColumnIndex) /*-{
    return this.getTableColumnIndex(viewColumnIndex);
  }-*/;

  public final native int getTableRowIndex(int viewRowIndex) /*-{
    return this.getTableRowIndex(viewRowIndex);
  }-*/;

  public final native int getViewColumnIndex(int tableColumnIndex) /*-{
    return this.getViewColumnIndex(tableColumnIndex);
  }-*/;

  public final native JsArrayInteger getViewColumns() /*-{
    return this.getViewColumns();
  }-*/;

  public final native int getViewRowIndex(int tableRowIndex) /*-{
    return this.getViewRowIndex(tableRowIndex);
  }-*/;

  public final native JsArrayInteger getViewRows() /*-{
    return this.getViewRows();
  }-*/;

  public final void hideColumns(int[] columnIndices) {
    hideColumns(ArrayHelper.toJsArrayInteger(columnIndices));
  }

  public final native void hideColumns(JsArrayInteger columnIndices) /*-{
    this.hideColumns(columnIndices);
  }-*/;

  public final native void hideRows(int from, int to) /*-{
    this.hideRows(from, to);
  }-*/;

  public final void hideRows(int[] rowsIndices) {
    hideRows(ArrayHelper.toJsArrayInteger(rowsIndices));
  }

  public final native void hideRows(JsArrayInteger rowsIndices) /*-{
    this.hideRows(rowsIndices);
  }-*/;

  public final void setColumns(int[] columnIndices) {
    setColumns(ArrayHelper.toJsArrayInteger(columnIndices));
  }

  public final native void setColumns(JsArrayInteger columnIndices) /*-{
    this.setColumns(columnIndices);
  }-*/;

  public final native void setRows(int from, int to) /*-{
    this.setRows(from, to);
  }-*/;

  public final void setRows(int[] rowsIndices) {
    setRows(ArrayHelper.toJsArrayInteger(rowsIndices));
  }

  public final native void setRows(JsArrayInteger rowsIndices) /*-{
    this.setRows(rowsIndices);
  }-*/;
}

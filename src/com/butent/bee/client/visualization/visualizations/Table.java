package com.butent.bee.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.visualization.AbstractDataTable;
import com.butent.bee.client.visualization.AbstractDrawOptions;
import com.butent.bee.client.visualization.Selectable;
import com.butent.bee.client.visualization.Selection;
import com.butent.bee.client.visualization.events.Handler;
import com.butent.bee.client.visualization.events.PageHandler;
import com.butent.bee.client.visualization.events.SelectHandler;
import com.butent.bee.client.visualization.events.SortHandler;
import com.butent.bee.shared.Assert;

/**
 * Implements data table visualization for representing data set in rows and columns.
 */
public class Table extends Visualization<Table.Options> implements Selectable {

  /**
   * Sets option values for a data table visualization.
   */

  public static class Options extends AbstractDrawOptions {

    /**
     * Sets Css class names values for a data table visualization .
     */
    public static class CssClassNames extends JavaScriptObject {
      protected CssClassNames() {
      }

      public final native void setHeaderCell(String headerCell) /*-{
        this.headerCell = headerCell;
      }-*/;

      public final native void setHeaderRow(String headerRow) /*-{
        this.headerRow = headerRow;
      }-*/;

      public final native void setHoverTableRow(String hoverTableRow) /*-{
        this.hoverTableRow = hoverTableRow;
      }-*/;

      public final native void setOddTableRow(String oddTableRow) /*-{
        this.oddTableRow = oddTableRow;
      }-*/;

      public final native void setRowNumberCell(String rowNumberCell) /*-{
        this.rowNumberCell = rowNumberCell;
      }-*/;

      public final native void setSelectedTableRow(String selectedTableRow) /*-{
        this.selectedTableRow = selectedTableRow;
      }-*/;

      public final native void setTableCell(String tableCell) /*-{
        this.tableCell = tableCell;
      }-*/;

      public final native void setTableRow(String tableRow) /*-{
        this.tableRow = tableRow;
      }-*/;
    }

    /**
     * Contains a list of possible policies (disable, enable, event).
     */

    public enum Policy {
      DISABLE, ENABLE, EVENT;

      @Override
      public String toString() {
        switch (this) {
          case ENABLE:
            return "enable";
          case EVENT:
            return "event";
          case DISABLE:
            return "disable";
          default:
            Assert.untouchable();
            throw new RuntimeException();
        }
      }
    }

    public static final Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setAllowHtml(boolean allowHtml) /*-{
      this.allowHtml = allowHtml;
    }-*/;

    public final native void setAlternatingRowStyle(boolean alternatingRowStyle) /*-{
      this.alternatingRowStyle = alternatingRowStyle;
    }-*/;

    public final native void setCssClassNames(CssClassNames cssClassNames) /*-{
      this.cssClassNames = cssClassNames;
    }-*/;

    public final native void setFirstRowNumber(int rowNumber) /*-{
      this.firstRowNumber = rowNumber;
    }-*/;

    public final native void setHeight(String height) /*-{
      this.height = height;
    }-*/;

    public final void setPage(Policy policy) {
      setPage(policy.toString());
    }

    public final native void setPageSize(int pageSize) /*-{
      this.pageSize = pageSize;
    }-*/;

    public final native void setRtlTable(boolean rtlTable) /*-{
      this.rtlTable = rtlTable;
    }-*/;

    public final native void setScrollLeftStartPosition(int pixels) /*-{
      this.scrollLeftStartPosition = pixels;
    }-*/;

    public final native void setShowRowNumber(boolean showRowNumber) /*-{
      this.showRowNumber = showRowNumber;
    }-*/;

    public final void setSort(Policy policy) {
      setSort(policy.toString());
    }

    public final native void setSortAscending(boolean sortAscending) /*-{
      this.sortAscending = sortAscending;
    }-*/;

    public final native void setSortColumn(int sortColumn) /*-{
      this.sortColumn = sortColumn;
    }-*/;

    public final native void setStartPage(int startPage) /*-{
      this.startPage = startPage;
    }-*/;

    public final native void setWidth(String width) /*-{
      this.width = width;
    }-*/;

    private native void setPage(String page) /*-{
      this.page = page;
    }-*/;

    private native void setSort(String sort) /*-{
      this.sort = sort;
    }-*/;
  }

  public static final String PACKAGE = "table";

  public Table() {
    super();
  }

  public Table(AbstractDataTable data, Options options) {
    super(data, options);
  }

  public final void addPageHandler(PageHandler handler) {
    Handler.addHandler(this, "page", handler);
  }

  public final void addSelectHandler(SelectHandler handler) {
    Selection.addSelectHandler(this, handler);
  }

  public final void addSortHandler(SortHandler handler) {
    Handler.addHandler(this, "sort", handler);
  }

  public final JsArray<Selection> getSelections() {
    return Selection.getSelections(this);
  }

  public final void setSelections(JsArray<Selection> sel) {
    Selection.setSelections(this, sel);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.Table(parent);
  }-*/;
}

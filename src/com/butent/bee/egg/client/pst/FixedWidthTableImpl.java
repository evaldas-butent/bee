package com.butent.bee.egg.client.pst;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import com.butent.bee.egg.client.grid.BeeHtmlTable;

/**
 * Implementation class that handles common code shared between the
 * {@link FixedWidthGrid} and {@link FixedWidthFlexTable}.
 */
class FixedWidthTableImpl {
  /**
   * The implementation singleton.
   */
  private static Impl impl;

  /**
   * Get the implementation singleton.
   */
  public static Impl get() {
    if (impl == null) {
      impl = GWT.create(Impl.class);
    }
    return impl;
  }

  /**
   * Information used to calculate the ideal column widths of a table.
   */
  public static class IdealColumnWidthInfo {
    private BeeHtmlTable table;
    private TableRowElement tr;
    private int columnCount;
    private int offset;

    public IdealColumnWidthInfo(BeeHtmlTable table, TableRowElement tr,
        int columnCount, int offset) {
      this.table = table;
      this.tr = tr;
      this.columnCount = columnCount;
      this.offset = offset;
    }
  }

  /**
   * An implementation used accommodate differences in column width
   * implementations between browsers.
   */
  public static class Impl {

    /**
     * Create a cell to use in the ghost row.
     * 
     * @param td the optional td to modify
     * @return the cell
     */
    public Element createGhostCell(Element td) {
      if (td == null) {
        td = DOM.createTD();
      }
      td.getStyle().setPropertyPx("height", 0);
      td.getStyle().setProperty("overflow", "hidden");
      td.getStyle().setPropertyPx("paddingTop", 0);
      td.getStyle().setPropertyPx("paddingBottom", 0);
      td.getStyle().setPropertyPx("borderTop", 0);
      td.getStyle().setPropertyPx("borderBottom", 0);
      td.getStyle().setPropertyPx("margin", 0);
      return td;
    }

    /**
     * Create the ghost row.
     * 
     * @return the ghost row element
     */
    public Element createGhostRow() {
      Element ghostRow = DOM.createTR();
      ghostRow.getStyle().setPropertyPx("margin", 0);
      ghostRow.getStyle().setPropertyPx("padding", 0);
      ghostRow.getStyle().setPropertyPx("height", 0);
      ghostRow.getStyle().setProperty("overflow", "hidden");
      return ghostRow;
    }

    /**
     * Returns a cell in the ghost row.
     * 
     * @param column the cell's column
     * @return the ghost cell
     */
    public Element getGhostCell(Element ghostRow, int column) {
      return DOM.getChild(ghostRow, column);
    }

    /**
     * Recalculate the ideal column widths of each column in the data table.
     * This method assumes that the tableLayout has already been changed.
     * 
     * @param info the {@link IdealColumnWidthInfo}
     * @return the ideal column widths
     */
    public int[] recalculateIdealColumnWidths(IdealColumnWidthInfo info) {
      // Workaround for IE re-entrant bug on window resize
      if (info == null) {
        return new int[0];
      }

      // We need at least one cell to do any calculations
      int columnCount = info.columnCount;
      BeeHtmlTable table = info.table;
      if (!table.isAttached() || table.getRowCount() == 0 || columnCount < 1) {
        return new int[0];
      }

      // Determine the width of each column
      int[] idealWidths = new int[columnCount];
      com.google.gwt.dom.client.Element td = info.tr.getFirstChildElement();
      for (int i = 0; i < info.offset; i++) {
        td = td.getNextSiblingElement();
      }
      for (int i = 0; i < columnCount; i++) {
        idealWidths[i] = td.getClientWidth();
        td = td.getNextSiblingElement();
      }
      return idealWidths;
    }

    /**
     * Setup to recalculate column widths.
     * 
     * @param table the table
     * @param columnCount the number of columns in the table
     * @param offset the offset of the first logical column
     * @return info used to calculate ideal column widths
     */
    public IdealColumnWidthInfo recalculateIdealColumnWidthsSetup(
        BeeHtmlTable table, int columnCount, int offset) {
      // Switch to normal layout
      table.getElement().getStyle().setProperty("tableLayout", "");

      // Add a full row to get ideal widths
      TableRowElement tr = Document.get().createTRElement();
      TableCellElement td = Document.get().createTDElement();
      td.setInnerHTML("<div style=\"height:1px;width:1px;\"></div>");
      for (int i = 0; i < columnCount + offset; i++) {
        tr.appendChild(td.cloneNode(true));
      }
      getTableBody(table).appendChild(tr);
      return new IdealColumnWidthInfo(table, tr, columnCount, offset);
    }

    /**
     * Tear down after recalculating column widths.
     * 
     * @param info the {@link IdealColumnWidthInfo}
     */
    public void recalculateIdealColumnWidthsTeardown(IdealColumnWidthInfo info) {
      // Workaround for IE re-entrant bug on window resize
      if (info == null) {
        return;
      }
      info.table.getElement().getStyle().setProperty("tableLayout", "fixed");
      getTableBody(info.table).removeChild(info.tr);
    }

    /**
     * Set the width of a column using the ghost row.
     * 
     * @param table the table containing the column
     * @param ghostRow the ghost row element
     * @param column the index of the column
     * @param width the width in pixels
     * @throws IndexOutOfBoundsException
     */
    public void setColumnWidth(BeeHtmlTable table, Element ghostRow, int column,
        int width) {
      getGhostCell(ghostRow, column).getStyle().setPropertyPx("width", width);
    }

    private Element getTableBody(BeeHtmlTable table) {
      return table.getBodyElement();
    }
  }

}

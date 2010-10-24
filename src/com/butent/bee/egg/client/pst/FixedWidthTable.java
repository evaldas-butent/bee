package com.butent.bee.egg.client.pst;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.grid.BeeHtmlTable;

public class FixedWidthTable {

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

  public static Element createGhostCell(Element td) {
    if (td == null) {
      td = DOM.createTD();
    }
    td.getStyle().setHeight(0, Unit.PX);
    td.getStyle().setOverflow(Overflow.HIDDEN);
    td.getStyle().setPaddingTop(0, Unit.PX);
    td.getStyle().setPaddingBottom(0, Unit.PX);
    BeeKeeper.getStyle().setBorderTopWidth(td, 0);
    BeeKeeper.getStyle().setBorderBottomWidth(td, 0);
    td.getStyle().setMargin(0, Unit.PX);
    return td;
  }

  public static Element createGhostRow() {
    Element ghostRow = DOM.createTR();
    ghostRow.getStyle().setMargin(0, Unit.PX);
    ghostRow.getStyle().setPadding(0, Unit.PX);
    ghostRow.getStyle().setHeight(0, Unit.PX);
    ghostRow.getStyle().setOverflow(Overflow.HIDDEN);
    return ghostRow;
  }

  public static Element getGhostCell(Element ghostRow, int column) {
    return DOM.getChild(ghostRow, column);
  }

  public static int[] recalculateIdealColumnWidths(IdealColumnWidthInfo info) {
    if (info == null) {
      return new int[0];
    }

    int columnCount = info.columnCount;
    BeeHtmlTable table = info.table;
    if (!table.isAttached() || table.getRowCount() == 0 || columnCount < 1) {
      return new int[0];
    }

    int[] idealWidths = new int[columnCount];
    Element td = info.tr.getFirstChildElement().cast();
    for (int i = 0; i < info.offset; i++) {
      td = td.getNextSiblingElement().cast();
    }
    for (int i = 0; i < columnCount; i++) {
      idealWidths[i] = td.getClientWidth();
      td = td.getNextSiblingElement().cast();
    }
    return idealWidths;
  }

  public static IdealColumnWidthInfo recalculateIdealColumnWidthsSetup(
      BeeHtmlTable table, int columnCount, int offset) {
    BeeKeeper.getStyle().clearTableLayout(table);

    TableRowElement tr = Document.get().createTRElement();
    TableCellElement td = Document.get().createTDElement();
    td.setInnerHTML("<div style=\"height:1px;width:1px;\"></div>");

    for (int i = 0; i < columnCount + offset; i++) {
      tr.appendChild(td.cloneNode(true));
    }
    getTableBody(table).appendChild(tr);

    return new IdealColumnWidthInfo(table, tr, columnCount, offset);
  }

  public static void recalculateIdealColumnWidthsTeardown(
      IdealColumnWidthInfo info) {
    if (info == null) {
      return;
    }
    BeeKeeper.getStyle().fixedTableLayout(info.table);
    getTableBody(info.table).removeChild(info.tr);
  }

  public static void setColumnWidth(Element ghostRow, int column, int width) {
    getGhostCell(ghostRow, column).getStyle().setWidth(width, Unit.PX);
  }

  private static Element getTableBody(BeeHtmlTable table) {
    return table.getBodyElement();
  }

}

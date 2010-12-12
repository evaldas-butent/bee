package com.butent.bee.egg.client.grid;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GridUtils {
  public static class IdealColumnWidthInfo {
    private HtmlTable table;
    private TableRowElement tr;
    private int columnCount;
    private int offset;

    public IdealColumnWidthInfo(HtmlTable table, TableRowElement tr, int columnCount, int offset) {
      this.table = table;
      this.tr = tr;
      this.columnCount = columnCount;
      this.offset = offset;
    }
  }

  private static Comparator<ColumnWidth> growComparator = new Comparator<ColumnWidth>() {
    public int compare(ColumnWidth o1, ColumnWidth o2) {
      double diff1 = o1.getDifference();
      double diff2 = o2.getDifference();
      if (diff1 < diff2) {
        return -1;
      } else if (diff1 == diff2) {
        return 0;
      } else {
        return 1;
      }
    }
  };
  
  private static Comparator<ColumnWidth> shrinkComparator = new Comparator<ColumnWidth>() {
    public int compare(ColumnWidth o1, ColumnWidth o2) {
      double diff1 = o1.getDifference();
      double diff2 = o2.getDifference();
      if (diff1 > diff2) {
        return -1;
      } else if (diff1 == diff2) {
        return 0;
      } else {
        return 1;
      }
    }
  };

  public static void clearColumnWidth(Element ghostRow, int column) {
    getGhostCell(ghostRow, column).getStyle().clearWidth();
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

  public static int distributeWidth(List<ColumnWidth> columns, int width) {
    Assert.notNull(columns);

    for (ColumnWidth info : columns) {
      int curWidth = info.getCurWidth();
      if (info.hasMinWidth() && curWidth < info.getMinWidth()) {
        curWidth = info.getMinWidth();
      } else if (info.hasMaxWidth() && curWidth > info.getMaxWidth()) {
        curWidth = info.getMaxWidth();
      }
      width -= (curWidth - info.getCurWidth());
      info.setNewWidth(curWidth);
    }
    if (width == 0) {
      return 0;
    }

    List<ColumnWidth> orderedColumns = new ArrayList<ColumnWidth>(columns);
    if (width > 0) {
      Collections.sort(orderedColumns, growComparator);
    } else {
      Collections.sort(orderedColumns, shrinkComparator);
    }

    return distributeOrderedWidth(orderedColumns, width);
  }

  public static Element getGhostCell(Element ghostRow, int column) {
    Assert.notNull(ghostRow, "ghost row cannot be null");
    Element cell = DOM.getChild(ghostRow, column);
    Assert.notNull(cell, "ghost cell not found for column " + column);
    return cell;
  }

  public static int[] recalculateIdealColumnWidths(IdealColumnWidthInfo info) {
    if (info == null) {
      return new int[0];
    }

    int columnCount = info.columnCount;
    HtmlTable table = info.table;
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
      HtmlTable table, int columnCount, int offset) {
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

  public static void recalculateIdealColumnWidthsTeardown(IdealColumnWidthInfo info) {
    if (info == null) {
      return;
    }
    BeeKeeper.getStyle().fixedTableLayout(info.table);
    getTableBody(info.table).removeChild(info.tr);
  }

  public static void setColumnWidth(Element ghostRow, int column, int width) {
    getGhostCell(ghostRow, column).getStyle().setWidth(width, Unit.PX);
  }

  private static int distributeOrderedWidth(List<ColumnWidth> columns, int width) {
    boolean growing = (width > 0);
    boolean fullySynced = false;
    int syncedColumns = 1;

    while (columns.size() > 0 && width != 0) {
      double targetDiff = getTargetDiff(columns, syncedColumns, width);

      int totalRequired = 0;
      for (int curIndex = 0; curIndex < syncedColumns; curIndex++) {
        ColumnWidth curInfo = columns.get(curIndex);
        int target = curInfo.getTargetWidth();
        int newWidth = (int) (targetDiff * target) + target;

        if (growing) {
          newWidth = Math.max(newWidth, curInfo.getCurWidth());
          if (curInfo.hasMaxWidth()) {
            newWidth = Math.min(newWidth, curInfo.getMaxWidth());
          }
        } else {
          newWidth = Math.min(newWidth, curInfo.getCurWidth());
          if (curInfo.hasMinWidth()) {
            newWidth = Math.max(newWidth, curInfo.getMinWidth());
          }
        }

        curInfo.setRequiredWidth(newWidth - curInfo.getNewWidth());
        totalRequired += curInfo.getRequiredWidth();
      }

      double diffAvailable = 1.0;
      if (totalRequired != 0) {
        diffAvailable = Math.min(1.0, width / (double) totalRequired);
      }
      for (int curIndex = 0; curIndex < syncedColumns; curIndex++) {
        ColumnWidth curInfo = columns.get(curIndex);
        int required = (int) (diffAvailable * curInfo.getRequiredWidth());

        if (fullySynced) {
          if (growing) {
            required = Math.max(1, required);
          } else {
            required = Math.min(-1, required);
          }
        }

        if (growing && required > width) {
          required = width;
        } else if (!growing && required < width) {
          required = width;
        }

        curInfo.setNewWidth(curInfo.getNewWidth() + required);
        width -= required;

        boolean maxedOut = false;
        if (growing && curInfo.hasMaxWidth()) {
          maxedOut = (curInfo.getNewWidth() >= curInfo.getMaxWidth());
        } else if (!growing && curInfo.hasMinWidth()) {
          maxedOut = (curInfo.getNewWidth() <= curInfo.getMinWidth());
        }
        if (maxedOut) {
          columns.remove(curIndex);
          curIndex--;
          syncedColumns--;
        }
      }

      if (!fullySynced && syncedColumns < columns.size()) {
        syncedColumns++;
      } else {
        fullySynced = true;
      }
    }
    return width;
  }

  private static Element getTableBody(HtmlTable table) {
    return table.getBodyElement();
  }
  
  private static double getTargetDiff(List<ColumnWidth> columns, int syncedColumns, int width) {
    if (syncedColumns < columns.size()) {
      return columns.get(syncedColumns).getDifference();
    } else {
      int totalNew = width;
      int totalTarget = 0;
      for (ColumnWidth info : columns) {
        totalNew += info.getNewWidth();
        totalTarget += info.getTargetWidth();
      }
      if (totalTarget == 0) {
        return BeeConst.DOUBLE_ZERO;
      }
      return (totalNew - totalTarget) / (double) totalTarget;
    }
  }
}

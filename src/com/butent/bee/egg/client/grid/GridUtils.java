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

  public static int distributeWidth(List<ColumnWidthInfo> columns, int width) {
    Assert.notNull(columns);

    for (ColumnWidthInfo info : columns) {
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

    List<ColumnWidthInfo> orderedColumns = new ArrayList<ColumnWidthInfo>(columns);

    if (width > 0) {
      Comparator<ColumnWidthInfo> comparator = new Comparator<ColumnWidthInfo>() {
        public int compare(ColumnWidthInfo o1, ColumnWidthInfo o2) {
          double diff1 = o1.getPercentageDifference();
          double diff2 = o2.getPercentageDifference();
          if (diff1 < diff2) {
            return -1;
          } else if (diff1 == diff2) {
            return 0;
          } else {
            return 1;
          }
        }
      };
      Collections.sort(orderedColumns, comparator);

    } else if (width < 0) {
      Comparator<ColumnWidthInfo> comparator = new Comparator<ColumnWidthInfo>() {
        public int compare(ColumnWidthInfo o1, ColumnWidthInfo o2) {
          double diff1 = o1.getPercentageDifference();
          double diff2 = o2.getPercentageDifference();
          if (diff1 > diff2) {
            return -1;
          } else if (diff1 == diff2) {
            return 0;
          } else {
            return 1;
          }
        }
      };
      Collections.sort(orderedColumns, comparator);
    }

    return distributeWidthImpl(orderedColumns, width);
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

  private static int distributeWidthImpl(List<ColumnWidthInfo> columns, int width) {
    boolean growing = (width > 0);
    boolean fullySynced = false;
    int syncedColumns = 1;

    while (columns.size() > 0 && width != 0) {
      double targetDiff = getTargetDiff(columns, syncedColumns, width);

      int totalRequired = 0;
      for (int curIndex = 0; curIndex < syncedColumns; curIndex++) {
        ColumnWidthInfo curInfo = columns.get(curIndex);
        int preferredWidth = curInfo.getPrefWidth();
        int newWidth = (int) (targetDiff * preferredWidth) + preferredWidth;

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

      double percentAvailable = 1.0;
      if (totalRequired != 0) {
        percentAvailable = Math.min(1.0, width / (double) totalRequired);
      }
      for (int curIndex = 0; curIndex < syncedColumns; curIndex++) {
        ColumnWidthInfo curInfo = columns.get(curIndex);
        int required = (int) (percentAvailable * curInfo.getRequiredWidth());

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
  
  private static double getTargetDiff(List<ColumnWidthInfo> columns, int syncedColumns, int width) {
    if (syncedColumns < columns.size()) {
      return columns.get(syncedColumns).getPercentageDifference();
    } else {
      int totalNewWidth = width;
      int totalPreferredWidth = 0;
      for (ColumnWidthInfo info : columns) {
        totalNewWidth += info.getNewWidth();
        totalPreferredWidth += info.getPrefWidth();
      }
      return (totalNewWidth - totalPreferredWidth) / (double) totalPreferredWidth;
    }
  }
}

package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.grid.ColumnWidthInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScrollTable extends AbstractScrollTable {
  private Map<Integer, ColumnWidthInfo> columnWidthInfos = new HashMap<Integer, ColumnWidthInfo>();

  private Set<Integer> untruncatableColumns = new HashSet<Integer>();
  private Set<Integer> untruncatableFooters = new HashSet<Integer>();
  private Set<Integer> untruncatableHeaders = new HashSet<Integer>();

  private Set<Integer> unsortableColumns = new HashSet<Integer>();

  public ScrollTable(FixedWidthGrid dataTable, FixedWidthFlexTable headerTable) {
    super(dataTable, headerTable);
  }

  public ScrollTable(FixedWidthGrid dataTable, FixedWidthFlexTable headerTable,
      ScrollTableImages images) {
    super(dataTable, headerTable, images);
  }

  public void createId() {
    DomUtils.createId(this, "st");
  }
  
  @Override
  public int getMaximumColumnWidth(int column) {
    return getColumnWidthInfo(column).getMaximumWidth();
  }

  @Override
  public int getMinimumColumnWidth(int column) {
    return getColumnWidthInfo(column).getMinimumWidth();
  }

  @Override
  public int getPreferredColumnWidth(int column) {
    return getColumnWidthInfo(column).getPreferredWidth();
  }

  @Override
  public boolean isColumnSortable(int column) {
    return (getSortPolicy() != SortPolicy.DISABLED && !unsortableColumns.contains(column));
  }

  @Override
  public boolean isColumnTruncatable(int column) {
    return !untruncatableColumns.contains(column);
  }

  @Override
  public boolean isFooterColumnTruncatable(int column) {
    return !untruncatableFooters.contains(column);
  }

  @Override
  public boolean isHeaderColumnTruncatable(int column) {
    return !untruncatableHeaders.contains(column);
  }

  public void setColumnSortable(int column, boolean sortable) {
    if (sortable) {
      unsortableColumns.remove(column);
    } else {
      unsortableColumns.add(column);
    }
  }

  public void setColumnTruncatable(int column, boolean truncatable) {
    if (truncatable) {
      untruncatableColumns.remove(column);
    } else {
      untruncatableColumns.add(column);
    }
  }

  public void setFooterColumnTruncatable(int column, boolean truncatable) {
    if (truncatable) {
      untruncatableFooters.remove(column);
    } else {
      untruncatableFooters.add(column);
    }
  }

  public void setHeaderColumnTruncatable(int column, boolean truncatable) {
    if (truncatable) {
      untruncatableHeaders.remove(column);
    } else {
      untruncatableHeaders.add(column);
    }
  }

  public void setMaximumColumnWidth(int column, int maxWidth) {
    getColumnWidthInfo(column).setMaximumWidth(maxWidth);
  }

  public void setMinimumColumnWidth(int column, int minWidth) {
    minWidth = Math.max(minWidth, FixedWidthGrid.MIN_COLUMN_WIDTH);
    getColumnWidthInfo(column).setMinimumWidth(minWidth);
  }

  public void setPreferredColumnWidth(int column, int preferredWidth) {
    getColumnWidthInfo(column).setPreferredWidth(preferredWidth);
  }

  private ColumnWidthInfo getColumnWidthInfo(int column) {
    int curWidth = getColumnWidth(column);
    ColumnWidthInfo info = columnWidthInfos.get(new Integer(column));
    if (info == null) {
      info = new ColumnWidthInfo(FixedWidthGrid.MIN_COLUMN_WIDTH, -1,
          FixedWidthGrid.DEFAULT_COLUMN_WIDTH, curWidth);
      columnWidthInfos.put(new Integer(column), info);
    } else {
      info.setCurrentWidth(curWidth);
    }
    return info;
  }
}

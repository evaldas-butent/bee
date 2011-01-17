package com.butent.bee.client.grid.model;

import com.butent.bee.shared.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class TableModelHelper {

  public static class ColumnSortInfo {
    private boolean ascending;
    private int column;

    public ColumnSortInfo() {
      this(0, true);
    }

    public ColumnSortInfo(int column, boolean ascending) {
      this.column = column;
      this.ascending = ascending;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ColumnSortInfo) {
        return equals((ColumnSortInfo) obj);
      }
      return false;
    }

    public boolean equals(ColumnSortInfo csi) {
      if (csi == null) {
        return false;
      }
      return getColumn() == csi.getColumn()
          && isAscending() == csi.isAscending();
    }

    public int getColumn() {
      return column;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    public boolean isAscending() {
      return ascending;
    }

    public void setAscending(boolean ascending) {
      this.ascending = ascending;
    }

    public void setColumn(int column) {
      this.column = column;
    }
  }

  public static class ColumnSortList implements Iterable<ColumnSortInfo> {
    private List<ColumnSortInfo> infos = new ArrayList<ColumnSortInfo>();

    public void add(ColumnSortInfo sortInfo) {
      add(0, sortInfo);
    }

    public void add(int index, ColumnSortInfo sortInfo) {
      int column = sortInfo.getColumn();
      for (int i = 0; i < infos.size(); i++) {
        ColumnSortInfo curInfo = infos.get(i);
        if (curInfo.getColumn() == column) {
          infos.remove(i);
          i--;
          if (column < index) {
            index--;
          }
        }
      }

      infos.add(index, sortInfo);
    }

    public void clear() {
      infos.clear();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ColumnSortList) {
        return equals((ColumnSortList) obj);
      }
      return false;
    }

    public boolean equals(ColumnSortList csl) {
      if (csl == null) {
        return false;
      }

      int size = size();
      if (size != csl.size()) {
        return false;
      }

      for (int i = 0; i < size; i++) {
        if (!infos.get(i).equals(csl.infos.get(i))) {
          return false;
        }
      }

      return true;
    }

    public int getPrimaryColumn() {
      ColumnSortInfo primaryInfo = getPrimaryColumnSortInfo();
      if (primaryInfo == null) {
        return -1;
      }
      return primaryInfo.getColumn();
    }

    public ColumnSortInfo getPrimaryColumnSortInfo() {
      if (infos.size() > 0) {
        return infos.get(0);
      }
      return null;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    public boolean isPrimaryAscending() {
      ColumnSortInfo primaryInfo = getPrimaryColumnSortInfo();
      if (primaryInfo == null) {
        return true;
      }
      return primaryInfo.isAscending();
    }

    public Iterator<ColumnSortInfo> iterator() {
      return new ImmutableIterator<ColumnSortInfo>(infos.iterator());
    }

    public boolean remove(Object sortInfo) {
      return infos.remove(sortInfo);
    }

    public int size() {
      return infos.size();
    }

    ColumnSortList copy() {
      ColumnSortList copy = new ColumnSortList();
      for (ColumnSortInfo info : this) {
        copy.infos.add(new ColumnSortInfo(info.getColumn(), info.isAscending()));
      }
      return copy;
    }
  }

  public static class Request {
    private int startRow;
    private int numRows;
    private ColumnSortList columnSortList;

    public Request() {
      this(0, 0, null);
    }

    public Request(int startRow, int numRows) {
      this(startRow, numRows, null);
    }

    public Request(int startRow, int numRows, ColumnSortList columnSortList) {
      this.startRow = startRow;
      this.numRows = numRows;
      this.columnSortList = columnSortList;
    }

    public ColumnSortList getColumnSortList() {
      return columnSortList;
    }

    public int getNumRows() {
      return numRows;
    }

    public int getStartRow() {
      return startRow;
    }
  }

  public abstract static class Response<RowType> {
    public abstract Iterator<RowType> getRowValues();
  }

  public static class SerializableResponse<RowType> extends Response<RowType> {
    private Collection<RowType> rowValues;

    public SerializableResponse() {
      this(null);
    }

    public SerializableResponse(Collection<RowType> rowValues) {
      this.rowValues = rowValues;
    }

    @Override
    public Iterator<RowType> getRowValues() {
      return rowValues.iterator();
    }
  }

  private static class ImmutableIterator<E> implements Iterator<E> {
    private Iterator<E> iterator;

    public ImmutableIterator(Iterator<E> iterator) {
      this.iterator = iterator;
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public E next() {
      return iterator.next();
    }

    public void remove() {
      Assert.unsupported();
    }
  }
}

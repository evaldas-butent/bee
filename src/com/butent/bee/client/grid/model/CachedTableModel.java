package com.butent.bee.client.grid.model;

import com.butent.bee.client.grid.model.TableModelHelper.Request;
import com.butent.bee.client.grid.model.TableModelHelper.Response;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsRow;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Enables to store and access grids data in memory for quicker grids performance.
 */

public class CachedTableModel extends TableModel {

  private class CacheCallback implements Callback {
    private Callback actualCallback;
    private int actualNumRows;
    private Request actualRequest;
    private int actualStartRow;

    public CacheCallback(Request request, Callback callback, int startRow, int numRows) {
      actualRequest = request;
      actualCallback = callback;
      actualStartRow = startRow;
      actualNumRows = numRows;
    }

    public void onFailure(Throwable caught) {
      actualCallback.onFailure(caught);
    }

    public void onRowsReady(Request request, Response response) {
      if (response != null) {
        Iterator<IsRow> rowValues = response.getRowValues();
        if (rowValues != null) {
          int curRow = request.getStartRow();
          while (rowValues.hasNext()) {
            rowValuesMap.put(new Integer(curRow), rowValues.next());
            curRow++;
          }
        }
      }

      actualCallback.onRowsReady(actualRequest, new CacheResponse(
          actualStartRow, actualStartRow + actualNumRows - 1));
    }
  }

  private class CacheIterator implements Iterator<IsRow> {
    int curRow;
    int lastRow;

    public CacheIterator(int firstRow, int lastRow) {
      this.curRow = firstRow - 1;
      this.lastRow = lastRow;
    }

    public boolean hasNext() {
      return curRow < lastRow && rowValuesMap.containsKey(new Integer(curRow + 1));
    }

    public IsRow next() {
      if (!hasNext()) {
        Assert.unsupported();
      }

      curRow++;
      return rowValuesMap.get(new Integer(curRow));
    }

    public void remove() {
      Assert.unsupported("Remove not supported");
    }
  }

  private class CacheResponse extends Response {
    private CacheIterator it;

    public CacheResponse(int firstRow, int lastRow) {
      it = new CacheIterator(firstRow, lastRow);
    }

    public Iterator<IsRow> getRowValues() {
      return it;
    }
  }

  private int postCacheRows = 0;
  private int preCacheRows = 0;

  private HashMap<Integer, IsRow> rowValuesMap = new HashMap<Integer, IsRow>();

  private TableModel tableModel;

  public CachedTableModel(TableModel tableModel) {
    this.tableModel = tableModel;
  }

  public void clearCache() {
    rowValuesMap.clear();
  }

  public int getPostCachedRowCount() {
    return postCacheRows;
  }

  public int getPreCachedRowCount() {
    return preCacheRows;
  }

  @Override
  public int getRowCount() {
    return tableModel.getRowCount();
  }

  @Override
  public void requestRows(Request request, Callback callback) {
    int startRow = request.getStartRow();
    int numRows = request.getNumRows();
    int lastRow = startRow + numRows - 1;
    int totalNumRows = getRowCount();
    if (totalNumRows != UNKNOWN_ROW_COUNT) {
      lastRow = Math.min(lastRow, totalNumRows - 1);
    }
    boolean fullyCached = true;
    for (int row = startRow; row <= lastRow; row++) {
      if (!rowValuesMap.containsKey(new Integer(row))) {
        fullyCached = false;
        break;
      }
    }

    if (fullyCached) {
      callback.onRowsReady(request, new CacheResponse(startRow, lastRow));
      return;
    }

    int uncachedFirstRow = Math.max(0, startRow - preCacheRows);
    int uncachedLastRow = lastRow + postCacheRows;

    if (totalNumRows != UNKNOWN_ROW_COUNT) {
      lastRow = Math.min(totalNumRows - 1, lastRow);
      uncachedLastRow = Math.min(totalNumRows - 1, uncachedLastRow);
    }

    for (int row = uncachedFirstRow; row <= lastRow; row++) {
      if (rowValuesMap.containsKey(new Integer(row))) {
        uncachedFirstRow++;
      } else {
        break;
      }
    }

    for (int row = uncachedLastRow; row >= startRow; row--) {
      if (rowValuesMap.containsKey(new Integer(row))) {
        uncachedLastRow--;
      } else {
        break;
      }
    }

    int uncachedNumRows = uncachedLastRow - uncachedFirstRow + 1;
    Request newRequest = new Request(uncachedFirstRow, uncachedNumRows);
    tableModel.requestRows(newRequest, new CacheCallback(request, callback,
        startRow, lastRow - startRow + 1));
  }

  public void setPostCachedRowCount(int postCacheRows) {
    this.postCacheRows = postCacheRows;
  }

  public void setPreCachedRowCount(int preCacheRows) {
    this.preCacheRows = preCacheRows;
  }

  @Override
  public void setRowCount(int rowCount) {
    tableModel.setRowCount(rowCount);
    super.setRowCount(rowCount);
  }
}

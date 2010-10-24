package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.pst.TableModelHelper.Request;
import com.butent.bee.egg.client.pst.TableModelHelper.Response;

import java.util.Iterator;

public class IterableTableModel<RowType> extends TableModel<RowType> {
  private Iterable<RowType> rows;

  public IterableTableModel(Iterable<RowType> rows) {
    this.rows = rows;
  }

  @Override
  public void requestRows(Request request, TableModel.Callback<RowType> callback) {

    callback.onRowsReady(request, new Response<RowType>() {
      @Override
      public Iterator<RowType> getRowValues() {
        return rows.iterator();
      }
    });
  }
}

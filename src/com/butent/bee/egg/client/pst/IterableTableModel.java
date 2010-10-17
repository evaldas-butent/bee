package com.butent.bee.egg.client.pst;

import com.butent.bee.egg.client.pst.TableModelHelper.Request;
import com.butent.bee.egg.client.pst.TableModelHelper.Response;

import java.util.Iterator;

/**
 * A class to retrieve row data to be used in a table.
 * 
 * @param <RowType> the data type of the row values
 */
public class IterableTableModel<RowType> extends TableModel<RowType> {
  /**
   * The values associated with each row.
   */
  private Iterable<RowType> rows;

  /**
   * Create a new {@link IterableTableModel}.
   * 
   * @param rows the values associated with each row.
   */
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

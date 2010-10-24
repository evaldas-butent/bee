package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;

import com.butent.bee.egg.client.pst.TableModelHelper.Request;
import com.butent.bee.egg.client.pst.TableModelHelper.Response;

public abstract class TableModel<RowType> implements HasRowCountChangeHandlers {

  public static interface Callback<RowType> {
    void onFailure(Throwable caught);
    void onRowsReady(Request request, Response<RowType> response);
  }

  public static final int ALL_ROWS = -1;
  public static final int UNKNOWN_ROW_COUNT = -1;

  private SimpleEventBus handlers = new SimpleEventBus();

  private int rowCount = UNKNOWN_ROW_COUNT;

  public HandlerRegistration addRowCountChangeHandler(RowCountChangeHandler handler) {
    return addHandler(RowCountChangeEvent.getType(), handler);
  }

  public void fireEvent(GwtEvent<?> event) {
    handlers.fireEvent(event);
  }

  public int getRowCount() {
    return rowCount;
  }

  public abstract void requestRows(Request request, Callback<RowType> callback);

  public void setRowCount(int rowCount) {
    if (this.rowCount != rowCount) {
      int oldRowCount = this.rowCount;
      this.rowCount = rowCount;
      fireEvent(new RowCountChangeEvent(oldRowCount, rowCount));
    }
  }

  protected <H extends EventHandler> HandlerRegistration addHandler(
      GwtEvent.Type<H> key, final H handler) {
    return handlers.addHandler(key, handler);
  }

  protected final SimpleEventBus getHandlerManager() {
    return handlers;
  }

}

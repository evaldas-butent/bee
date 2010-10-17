package com.butent.bee.egg.client.pst;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;

import com.butent.bee.egg.client.pst.TableModelHelper.Request;
import com.butent.bee.egg.client.pst.TableModelHelper.Response;

/**
 * A class to retrieve row data to be used in a table.
 * 
 * @param <RowType> the data type of the row values
 */
public abstract class TableModel<RowType> implements HasRowCountChangeHandlers {
  /**
   * Callback for {@link TableModel}. Every {@link Request} should be associated
   * with a {@link TableModel.Callback} that should be called after a
   * {@link Response} is generated.
   * 
   * @param <RowType> the data type of the row values
   */
  public static interface Callback<RowType> {
    /**
     * Called when an error occurs and the rows cannot be loaded.
     * 
     * @param caught the exception that was thrown
     */
    void onFailure(Throwable caught);

    /**
     * Consume the data created by {@link TableModel} in response to a Request.
     * 
     * @param request the request
     * @param response the response
     */
    void onRowsReady(Request request, Response<RowType> response);
  }

  /**
   * Use the ALL_ROWS value in place of the numRows variable when requesting all
   * rows.
   */
  public static final int ALL_ROWS = -1;

  /**
   * Indicates that the number of rows is unknown, and therefore unbounded.
   */
  public static final int UNKNOWN_ROW_COUNT = -1;

  /**
   * The manager of events.
   */
  private SimpleEventBus handlers = new SimpleEventBus();

  /**
   * The total number of rows available in the model.
   */
  private int rowCount = UNKNOWN_ROW_COUNT;

  public HandlerRegistration addRowCountChangeHandler(RowCountChangeHandler handler) {
    return addHandler(RowCountChangeEvent.getType(), handler);
  }

  /**
   * Fires an event.
   * 
   * @param event the event
   */
  public void fireEvent(GwtEvent<?> event) {
    handlers.fireEvent(event);
  }

  /**
   * Return the total number of rows. If the number is not known, return
   * {@link #UNKNOWN_ROW_COUNT}.
   * 
   * @return the total number of rows, or {@link #UNKNOWN_ROW_COUNT}
   */
  public int getRowCount() {
    return rowCount;
  }

  /**
   * Generate a {@link Response} based on a specific {@link Request}. The
   * response is passed into the {@link Callback}.
   * 
   * @param request the {@link Request} for row data
   * @param callback the {@link Callback} to use for the {@link Response}
   */
  public abstract void requestRows(Request request, Callback<RowType> callback);

  /**
   * Set the total number of rows.
   * 
   * @param rowCount the row count
   */
  public void setRowCount(int rowCount) {
    if (this.rowCount != rowCount) {
      int oldRowCount = this.rowCount;
      this.rowCount = rowCount;
      fireEvent(new RowCountChangeEvent(oldRowCount, rowCount));
    }
  }

  /**
   * Adds this handler to the widget.
   * 
   * @param key the event key
   * @param handler the handler
   */
  protected <H extends EventHandler> HandlerRegistration addHandler(
      GwtEvent.Type<H> key, final H handler) {
    return handlers.addHandler(key, handler);
  }

  /**
   * Returns this widget's {@link HandlerManager} used for event management.
   */
  protected final SimpleEventBus getHandlerManager() {
    return handlers;
  }

}

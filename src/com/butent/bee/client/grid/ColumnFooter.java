package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.client.Callback;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.grid.cell.FooterCell;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.client.view.search.FilterChangeHandler;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

public class ColumnFooter extends Header<AbstractFilterSupplier> {

  private static final int CLOSE_WIDTH = 16;

  private final AbstractFilterSupplier filterSupplier;
  private final NotificationListener notificationListener;

  private FilterChangeHandler filterChangeHandler = null;

  private State state = State.CLOSED;

  public ColumnFooter(AbstractFilterSupplier filterSupplier,
      NotificationListener notificationListener) {
    super(new FooterCell());

    this.filterSupplier = filterSupplier;
    this.notificationListener = notificationListener;
  }

  public Filter getFilter() {
    return filterSupplier.getFilter();
  }

  @Override
  public AbstractFilterSupplier getValue() {
    return filterSupplier;
  }

  public boolean isEmpty() {
    return filterSupplier.isEmpty();
  }
  
  @Override
  public void onBrowserEvent(Context context, final Element elem, NativeEvent event) {
    if (State.CLOSED.equals(getState())) {
      setState(State.PENDING);

      int x = elem.getAbsoluteRight() - event.getClientX();
      if (x >= 0 && x <= CLOSE_WIDTH && x <= elem.getOffsetWidth() / 2) {
        onResponse(elem, filterSupplier.reset());
        close();

      } else {
        filterSupplier.onRequest(elem, notificationListener, new Callback<Boolean>() {
          @Override
          public void onFailure(String... reason) {
            ColumnFooter.this.notificationListener.notifySevere(reason);
            ColumnFooter.this.close();
          }

          @Override
          public void onSuccess(Boolean result) {
            ColumnFooter.this.onResponse(elem, result);
            ColumnFooter.this.close();
          }
        });
      }
    }
  }

  public void setFilterChangeHandler(FilterChangeHandler filterChangeHandler) {
    this.filterChangeHandler = filterChangeHandler;
  }

  private void close() {
    setState(State.CLOSED);
  }

  private FilterChangeHandler getFilterChangeHandler() {
    return filterChangeHandler;
  }

  private FooterCell getFooterCell() {
    return (FooterCell) getCell();
  }

  private State getState() {
    return state;
  }

  private void onResponse(Element elem, Boolean filterChanged) {
    if (BeeUtils.isTrue(filterChanged)) {
      getFooterCell().update(elem, getValue());
      if (getFilterChangeHandler() != null) {
        getFilterChangeHandler().onFilterChange();
      }
    }
  }

  private void setState(State state) {
    this.state = state;
  }
}

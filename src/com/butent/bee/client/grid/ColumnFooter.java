package com.butent.bee.client.grid;

import com.google.common.collect.ImmutableSet;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Header;

import com.butent.bee.client.Callback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.cell.FooterCell;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.client.view.search.FilterHandler;
import com.butent.bee.client.view.search.HasFilterHandler;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

public class ColumnFooter extends Header<AbstractFilterSupplier> implements HasFilterHandler {

  private static final int CLOSE_WIDTH = 16;

  private final AbstractFilterSupplier filterSupplier;
  private final NotificationListener notificationListener;

  private FilterHandler filterHandler = null;

  private State state = State.CLOSED;
  
  private final String id;
  private final ImmutableSet<String> exclusion;

  public ColumnFooter(AbstractFilterSupplier filterSupplier,
      NotificationListener notificationListener) {
    super(new FooterCell());

    this.filterSupplier = filterSupplier;
    this.notificationListener = notificationListener;
    
    this.id = DomUtils.createUniqueId("cf");
    this.exclusion = ImmutableSet.of(this.id);
  }

  public Filter getFilter() {
    return filterSupplier.getFilter();
  }

  public String getId() {
    return id;
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
        filterSupplier.setEffectiveFilter((getFilterHandler() == null 
            ? null : getFilterHandler().getEffectiveFilter(exclusion)));
        
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

  public void reset() {
    filterSupplier.reset();    
    getFooterCell().update(null, getValue());
  }

  @Override
  public void setFilterHandler(FilterHandler filterHandler) {
    this.filterHandler = filterHandler;
  }

  private void close() {
    setState(State.CLOSED);
  }

  private FilterHandler getFilterHandler() {
    return filterHandler;
  }

  private FooterCell getFooterCell() {
    return (FooterCell) getCell();
  }

  private State getState() {
    return state;
  }
  
  private void onResponse(final Element elem, Boolean filterChanged) {
    if (BeeUtils.isTrue(filterChanged) && getFilterHandler() != null) {
      getFilterHandler().onFilterChange(new Consumer<Boolean>() {
        @Override
        public void accept(Boolean parameter) {
          if (BeeUtils.isTrue(parameter)) {
            getFooterCell().update(elem, getValue());
          }
        }
      });
    }
  }

  private void setState(State state) {
    this.state = state;
  }
}

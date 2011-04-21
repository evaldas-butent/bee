package com.butent.bee.client.presenter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.AsyncProvider;
import com.butent.bee.client.data.CachedProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.view.GridContainerImpl;
import com.butent.bee.client.view.GridContainerView;
import com.butent.bee.client.view.GridContentView;
import com.butent.bee.client.view.HasSearch;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GridPresenter implements Presenter {
  
  private class FilterCallback implements Queries.IntCallback {
    private Filter filter;

    private FilterCallback(Filter filter) {
      this.filter = filter;
    }

    public void onResponse(int value) {
      BeeKeeper.getLog().info("filter callback", filter);
      BeeKeeper.getLog().info("row count", value);

      if (!Objects.equal(filter, getLastFilter())) {
        BeeKeeper.getLog().warning("filter not the same");
        BeeKeeper.getLog().warning(getLastFilter());
        return;
      }

      if (value > 0) {
        if (isAsync()) {
          ((AsyncProvider) getDataProvider()).setFilter(filter);
          getView().getContent().setRowCount(value, true);
          getView().getContent().setVisibleRangeAndClearData(
              getView().getContent().getVisibleRange(), true);
        }
      }
    }
  }
  
  private final DataInfo dataInfo;
  private final boolean async;
  private final List<BeeColumn> dataColumns;

  private final GridContainerView gridContainer;
  private final Provider dataProvider;

  private final Set<HandlerRegistration> filterChangeHandlers = Sets.newHashSet();
  private Filter lastFilter = null;

  public GridPresenter(DataInfo dataInfo, BeeRowSet rowSet, boolean async) {
    this.dataInfo = dataInfo;
    this.async = async;
    this.dataColumns = rowSet.getColumns();

    int rowCount = async ? dataInfo.getRowCount() : rowSet.getNumberOfRows();

    this.gridContainer = createView(dataInfo.getName(), dataColumns, rowCount);
    this.dataProvider = createProvider(gridContainer, dataInfo, rowSet, async);

    bind();
  }

  public List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  public DataInfo getDataInfo() {
    return dataInfo;
  }

  public Provider getDataProvider() {
    return dataProvider;
  }

  public Filter getLastFilter() {
    return lastFilter;
  }

  public GridContainerView getView() {
    return gridContainer;
  }

  public Widget getWidget() {
    return getView().asWidget();
  }

  public boolean isAsync() {
    return async;
  }

  public void onViewUnload() {
    if (BeeKeeper.getUi().isTemporaryDetach()) {
      return;
    }
    getView().setViewPresenter(null);

    for (HandlerRegistration hr : filterChangeHandlers) {
      hr.removeHandler();
    }
    filterChangeHandlers.clear();
    
    getDataProvider().onUnload();
  }

  private void bind() {
    GridContainerView view = getView();
    view.setViewPresenter(this);

    Collection<SearchView> searchers = getSearchers();
    if (searchers != null) {
      for (SearchView search : searchers) {
        filterChangeHandlers.add(search.addChangeHandler(new ChangeHandler() {
          public void onChange(ChangeEvent event) {
            updateFilter();
          }
        }));
      }
    }
  }

  private Provider createProvider(GridContainerView view, DataInfo info, BeeRowSet rowSet,
      boolean isAsync) {
    Provider provider;
    GridContentView display = view.getContent();

    if (isAsync) {
      provider = new AsyncProvider(display, info);
    } else {
      provider = new CachedProvider(display, rowSet);
      Assert.notNull(rowSet);
      display.setRowData(0, rowSet.getRows().getList());
      display.setRowCount(rowSet.getNumberOfRows());
    }
    return provider;
  }

  private GridContainerView createView(String caption, List<BeeColumn> columns, int rc) {
    GridContainerView view = new GridContainerImpl();
    view.create(caption, columns, rc);

    return view;
  }

  private String getDataName() {
    return getDataInfo().getName();
  }

  private Collection<SearchView> getSearchers() {
    Collection<SearchView> searchers;

    if (getView() instanceof HasSearch) {
      searchers = ((HasSearch) getView()).getSearchers();
    } else {
      searchers = null;
    }
    return searchers;
  }
  
  private void updateFilter() {
    Collection<SearchView> searchers = getSearchers();
    Assert.notNull(searchers);
    
    List<Filter> filters = Lists.newArrayListWithCapacity(searchers.size());
    for (SearchView search : searchers) {
      Filter flt = search.getFilter(getDataColumns());
      if (flt != null && !filters.contains(flt)) {
        filters.add(flt);
      }
    }

    Filter filter;
    switch (filters.size()) {
      case 0:
        filter = null;
        break;
      case 1:
        filter = filters.get(0);
        break;
      default:
        filter = CompoundFilter.and(filters.toArray(new Filter[filters.size()]));
    }
    
    if (Objects.equal(filter, getLastFilter())) {
      BeeKeeper.getLog().info("filter not changed", filter);
      return;
    }
    
    lastFilter = filter;
    Queries.getRowCount(getDataName(), filter, new FilterCallback(filter));
  }
}

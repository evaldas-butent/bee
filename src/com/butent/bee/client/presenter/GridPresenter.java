package com.butent.bee.client.presenter;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.AsyncProvider;
import com.butent.bee.client.data.CachedProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.view.GridContainerImpl;
import com.butent.bee.client.view.GridContainerView;
import com.butent.bee.client.view.SearchView;
import com.butent.bee.client.view.View;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.view.Filter;
import com.butent.bee.shared.data.view.DataInfo;

import java.util.List;

public class GridPresenter implements Presenter {
  private final DataInfo dataInfo;
  private final boolean async;
  private final List<BeeColumn> dataColumns;

  private int rowCount;

  private final GridContainerView gridContainer;
  private final Provider dataProvider;

  public GridPresenter(DataInfo dataInfo, BeeRowSet rowSet, boolean async) {
    this.dataInfo = dataInfo;
    this.async = async;
    this.dataColumns = rowSet.getColumns();
    this.rowCount = async ? dataInfo.getRowCount() : rowSet.getNumberOfRows();

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

  public int getRowCount() {
    return rowCount;
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

  public void onUnload(View view) {
  }

  public void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }

  public void start(int containerWidth, int containerHeight) {
    int pageSize = getView().estimatePageSize(containerWidth, containerHeight);
    if (pageSize > 0) {
      getView().updatePageSize(pageSize);
    }
  }

  private void bind() {
    final SearchView search = getView().getSearchView();

    if (getRowCount() > 1 && getDataProvider() instanceof AsyncProvider && search != null) {
      search.addChangeHandler(new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          updateFilter(search.getFilter());
        }
      });
    }
  }

  private Provider createProvider(GridContainerView view, DataInfo info, BeeRowSet rowSet,
      boolean isAsync) {
    Provider provider;
    if (isAsync) {
      provider = new AsyncProvider(view.getContent(), info);
    } else {
      provider = new CachedProvider(view.getContent(), rowSet);
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

  private void updateFilter(final Filter filter) {
    BeeKeeper.getLog().info(filter == null ? "no filter" : filter);

    Queries.getRowCount(getDataName(), filter, new Queries.IntCallback() {
      public void onResponse(int value) {
        BeeKeeper.getLog().info(value);

        if (value > 0 && value != getRowCount()) {
          setRowCount(value);
          if (isAsync()) {
            ((AsyncProvider) getDataProvider()).setFilter(filter);
            getView().getContent().setRowCount(value, true);
            getView().getContent().setVisibleRangeAndClearData(
                getView().getContent().getVisibleRange(), true);
          }
        }
      }
    });
  }
}

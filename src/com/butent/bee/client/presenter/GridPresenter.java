package com.butent.bee.client.presenter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.AsyncProvider;
import com.butent.bee.client.data.CachedProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.GridContainerImpl;
import com.butent.bee.client.view.GridContainerView;
import com.butent.bee.client.view.HasSearch;
import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.BeeGrid;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Contains necessary methods for implementing grid presentation on the client side (view, filters,
 * content etc).
 */

public class GridPresenter implements Presenter, EditEndEvent.Handler {

  private class DeleteCallback extends BeeCommand {
    private final Collection<RowInfo> rows;

    private DeleteCallback(Collection<RowInfo> rows) {
      this.rows = rows;
    }

    private DeleteCallback(long rowId, long version) {
      this(Lists.newArrayList(new RowInfo(rowId, version)));
    }

    @Override
    public void execute() {
      Assert.notNull(rows);
      int count = rows.size();
      Assert.isPositive(count);

      setLoadingState(LoadingStateChangeEvent.LoadingState.LOADING);

      if (count == 1) {
        RowInfo rowInfo = BeeUtils.peek(rows);
        final long rowId = rowInfo.getId();
        long version = rowInfo.getVersion();

        Queries.deleteRow(getDataName(), rowId, version, new Queries.IntCallback() {
          public void onFailure(String reason) {
            showFailure("Delete Row", reason);
            setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
          }

          public void onSuccess(Integer result) {
            BeeKeeper.getBus().fireEvent(new RowDeleteEvent(getDataName(), rowId));
          }
        });

      } else if (count > 1) {
        Queries.deleteRows(getDataName(), rows, new Queries.IntCallback() {
          public void onFailure(String reason) {
            showFailure("Delete Rows", reason);
            setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
          }

          public void onSuccess(Integer result) {
            BeeKeeper.getBus().fireEvent(new MultiDeleteEvent(getDataName(), rows));
            showInfo("Deleted " + result + " rows");
          }
        });
      }
    }
  }

  private class FilterCallback implements Queries.IntCallback {
    private Filter filter;

    private FilterCallback(Filter filter) {
      this.filter = filter;
    }

    public void onFailure(String reason) {
      showFailure("Filter", reason);
    }

    public void onSuccess(Integer result) {
      if (!Objects.equal(filter, getLastFilter())) {
        BeeKeeper.getLog().warning("filter not the same");
        BeeKeeper.getLog().warning(getLastFilter());
        return;
      }

      if (result > 0) {
        getDataProvider().onFilterChanged(filter, result);
      } else if (filter != null) {
        showWarning("Filter: " + filter.transform(), "no data found");
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

  public GridPresenter(DataInfo dataInfo, BeeRowSet rowSet, boolean async, BeeGrid descr) {
    this.dataInfo = dataInfo;
    this.async = async;
    this.dataColumns = rowSet.getColumns();

    int rowCount = async ? dataInfo.getRowCount() : rowSet.getNumberOfRows();

    this.gridContainer = createView(dataInfo.getName(), dataColumns, rowCount, rowSet, descr);
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

  @Override
  public void handleAction(Action action) {
    Assert.notNull(action);

    switch (action) {
      case CLOSE:
        BeeKeeper.getUi().closeView(getView());
        break;

      case CONFIGURE:
        String options = Window.prompt("Options", "");
        if (!BeeUtils.isEmpty(options)) {
          getView().getContent().applyOptions(options);
        }
        break;

      case DELETE:
        RowInfo activeRowInfo = getView().getContent().getActiveRowInfo();
        if (activeRowInfo != null) {
          if (getView().getContent().isRowSelected(activeRowInfo.getId())) {
            deleteRows(getView().getContent().getSelectedRows());
          } else {
            deleteRow(activeRowInfo.getId(), activeRowInfo.getVersion());
          }
        }
        break;

      case REFRESH:
        getDataProvider().refresh();
        break;

      default:
        BeeKeeper.getLog().info(action, "not implemented");
    }
  }

  public boolean isAsync() {
    return async;
  }

  public void onEditEnd(EditEndEvent event) {
    final String viewName = getDataName();
    final long rowId = event.getRowValue().getId();
    final long version = event.getRowValue().getVersion();
    final String columnId = event.getColumn().getLabel();
    final String newValue = event.getNewValue();

    BeeRowSet rs = new BeeRowSet(new BeeColumn(event.getColumn().getType(), columnId));
    rs.setViewName(viewName);
    rs.addRow(rowId, version, new String[] {event.getOldValue()});
    rs.setValue(0, 0, newValue);

    Queries.updateRow(rs,
        new Queries.RowCallback() {
          public void onFailure(String reason) {
            getView().getContent().refreshCell(rowId, columnId);
            showFailure("Update Row", reason);
          }

          public void onSuccess(BeeRow row) {
            BeeKeeper.getLog().info("cell updated:", viewName, rowId, columnId, newValue);
            BeeKeeper.getBus().fireEvent(
                new CellUpdateEvent(viewName, rowId, row.getVersion(), columnId, newValue));
          }
        });
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
    view.bind();

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

    view.getContent().addEditEndHandler(this);
  }
  
  private Provider createProvider(GridContainerView view, DataInfo info, BeeRowSet rowSet,
      boolean isAsync) {
    Provider provider;
    GridView display = view.getContent();

    if (isAsync) {
      provider = new AsyncProvider(display.getGrid(), info);
    } else {
      provider = new CachedProvider(display.getGrid(), info.getName(), rowSet);
    }
    return provider;
  }

  private GridContainerView createView(String dataName, List<BeeColumn> columns, int rc,
      BeeRowSet rowSet, BeeGrid descr) {
    GridContainerView view = new GridContainerImpl();
    
    String caption = (descr == null) ? dataName : BeeUtils.ifString(descr.getCaption(), dataName);
    view.create(caption, columns, rc, rowSet, descr);

    return view;
  }

  private void deleteRow(long rowId, long version) {
    Global.getMsgBoxen().confirm("Delete Row ?", new DeleteCallback(rowId, version),
        StyleUtils.NAME_SCARY);
  }

  private void deleteRows(Collection<RowInfo> rows) {
    Assert.notNull(rows);
    int count = rows.size();
    Assert.isPositive(count);
    if (count == 1) {
      RowInfo rowInfo = BeeUtils.peek(rows);
      deleteRow(rowInfo.getId(), rowInfo.getVersion());
      return;
    }

    Global.getMsgBoxen().confirm(BeeUtils.concat(1, "Delete", count, "rows"),
        Lists.newArrayList("SRSLY ?"), new DeleteCallback(rows), StyleUtils.NAME_SUPER_SCARY);
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

  private void setLoadingState(LoadingStateChangeEvent.LoadingState loadingState) {
    if (loadingState != null) {
      getView().getContent().getGrid().fireLoadingStateChange(loadingState);
    }
  }

  private void showFailure(String activity, String reason) {
    getView().getContent().notifySevere(activity, reason);
  }

  private void showInfo(String... messages) {
    getView().getContent().notifyInfo(messages);
  }

  private void showWarning(String... messages) {
    getView().getContent().notifyWarning(messages);
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

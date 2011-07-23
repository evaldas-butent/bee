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
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.FormContainerImpl;
import com.butent.bee.client.view.FormContainerView;
import com.butent.bee.client.view.HasSearch;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class FormPresenter implements Presenter, ReadyForInsertEvent.Handler,
    ReadyForUpdateEvent.Handler, HasViewName {

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

        Queries.deleteRow(getViewName(), rowId, version, new Queries.IntCallback() {
          public void onFailure(String[] reason) {
            setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
            showFailure("Delete Row", reason);
          }

          public void onSuccess(Integer result) {
            BeeKeeper.getBus().fireEvent(new RowDeleteEvent(getViewName(), rowId));
          }
        });

      } else if (count > 1) {
        Queries.deleteRows(getViewName(), rows, new Queries.IntCallback() {
          public void onFailure(String[] reason) {
            showFailure("Delete Rows", reason);
            setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
          }

          public void onSuccess(Integer result) {
            BeeKeeper.getBus().fireEvent(new MultiDeleteEvent(getViewName(), rows));
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

    public void onFailure(String[] reason) {
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

  private final String viewName;
  private final boolean async;
  private final List<BeeColumn> dataColumns;

  private final FormContainerView formContainer;
  private final Provider dataProvider;

  private final Set<HandlerRegistration> filterChangeHandlers = Sets.newHashSet();
  private Filter lastFilter = null;
  
  public FormPresenter(FormDescription formDescription, String viewName, int rowCount,
      BeeRowSet rowSet, boolean async) {
    this.viewName = viewName;
    this.async = async;
    this.dataColumns = (rowSet == null) ? null : rowSet.getColumns();

    this.formContainer = createView(formDescription, dataColumns, rowCount);
    this.dataProvider = createProvider(formContainer, viewName, rowSet, async);

    bind();
  }

  public List<BeeColumn> getDataColumns() {
    return dataColumns;
  }

  public Provider getDataProvider() {
    return dataProvider;
  }

  public Filter getLastFilter() {
    return lastFilter;
  }

  public FormContainerView getView() {
    return formContainer;
  }

  public String getViewName() {
    return viewName;
  }

  public Widget getWidget() {
    return getView().asWidget();
  }

  @Override
  public void handleAction(Action action) {
    Assert.notNull(action);

    switch (action) {
      case CLOSE:
        BeeKeeper.getScreen().closeView(getView());
        break;

      case CONFIGURE:
        String options = Window.prompt("Options", "");
        if (!BeeUtils.isEmpty(options)) {
          getView().getContent().applyOptions(options);
        }
        break;

      case DELETE:
        RowInfo activeRowInfo = getView().getContent().getActiveRowInfo();
        if (activeRowInfo != null && getView().getContent().isRowEditable(true)) {
          deleteRow(activeRowInfo.getId(), activeRowInfo.getVersion());
        }
        break;

      case REFRESH:
        if (getDataProvider() != null) {
          getDataProvider().refresh();
        }
        break;

      case ADD:
        getView().getContent().startNewRow();
        break;

      default:
        BeeKeeper.getLog().info(action, "not implemented");
    }
  }

  public boolean isAsync() {
    return async;
  }

  public void onReadyForInsert(ReadyForInsertEvent event) {
    setLoadingState(LoadingStateChangeEvent.LoadingState.LOADING);

    Queries.insert(getViewName(), event.getColumns(), event.getValues(), new Queries.RowCallback() {
      public void onFailure(String[] reason) {
        setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
        showFailure("Insert Row", reason);
        getView().getContent().finishNewRow(null);
      }

      public void onSuccess(BeeRow result) {
        BeeKeeper.getBus().fireEvent(new RowInsertEvent(getViewName(), result));
        getView().getContent().finishNewRow(result);
      }
    });
  }

  public void onReadyForUpdate(ReadyForUpdateEvent event) {
    final long rowId = event.getRowValue().getId();
    final long version = event.getRowValue().getVersion();
    final String columnId = event.getColumn().getLabel();
    final String newValue = event.getNewValue();

    BeeRowSet rs = new BeeRowSet(new BeeColumn(event.getColumn().getType(), columnId));
    rs.setViewName(getViewName());
    rs.addRow(rowId, version, new String[]{event.getOldValue()});
    rs.getRow(0).preliminaryUpdate(0, newValue);

    final boolean rowMode = event.isRowMode();

    Queries.update(rs, rowMode,
        new Queries.RowCallback() {
          public void onFailure(String[] reason) {
            getView().getContent().refreshCellContent(columnId);
            showFailure("Update Cell", reason);
          }

          public void onSuccess(BeeRow row) {
            BeeKeeper.getLog().info("cell updated:", getViewName(), rowId, columnId, newValue);
            if (rowMode) {
              BeeKeeper.getBus().fireEvent(new RowUpdateEvent(getViewName(), row));
            } else {
              BeeKeeper.getBus().fireEvent(
                  new CellUpdateEvent(getViewName(), rowId, row.getVersion(), columnId, newValue));
            }
          }
        });
  }

  public void onViewUnload() {
    if (BeeKeeper.getScreen().isTemporaryDetach()) {
      return;
    }
    getView().setViewPresenter(null);

    for (HandlerRegistration hr : filterChangeHandlers) {
      hr.removeHandler();
    }
    filterChangeHandlers.clear();
    
    if (getDataProvider() != null) {
      getDataProvider().onUnload();
    }
  }
  
  private void bind() {
    FormContainerView view = getView();
    view.setViewPresenter(this);
    view.bind();
    
    if (hasData()) {
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

      view.getContent().addReadyForUpdateHandler(this);
      view.getContent().addReadyForInsertHandler(this);
    }
  }

  private Provider createProvider(FormContainerView view, String dataName, BeeRowSet rowSet,
      boolean isAsync) {
    if (BeeUtils.isEmpty(dataName)) {
      return null;
    }
    Provider provider;
    FormView content = view.getContent();

    if (isAsync) {
      provider = new AsyncProvider(content.getDisplay(), dataName);
    } else {
      provider = new CachedProvider(content.getDisplay(), dataName, rowSet);
    }
    return provider;
  }

  private FormContainerView createView(FormDescription formDescription,
      List<BeeColumn> columns, int rowCount) {
    FormContainerView view = new FormContainerImpl();

    view.create(formDescription, columns, rowCount);
    return view;
  }

  private void deleteRow(long rowId, long version) {
    Global.getMsgBoxen().confirm("Delete Row ?", new DeleteCallback(rowId, version),
        StyleUtils.NAME_SCARY);
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
  
  private boolean hasData() {
    return !BeeUtils.isEmpty(getViewName());
  }

  private void setLoadingState(LoadingStateChangeEvent.LoadingState loadingState) {
    if (loadingState != null) {
      getView().getContent().getDisplay().fireLoadingStateChange(loadingState);
    }
  }

  private void showFailure(String activity, String... reasons) {
    List<String> messages = Lists.newArrayList(activity);
    if (reasons != null) {
      messages.addAll(Lists.newArrayList(reasons));
    }
    getView().getContent().notifySevere(messages.toArray(new String[0]));
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
    Queries.getRowCount(getViewName(), filter, new FilterCallback(filter));
  }
}

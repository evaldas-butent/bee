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
import com.butent.bee.client.ui.FormFactory.FormCallback;
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
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Contains necessary methods for implementing form presentation on the client side (filter, view
 * name, widgets etc).
 */

public class FormPresenter implements Presenter, ReadyForInsertEvent.Handler,
    ReadyForUpdateEvent.Handler, HasViewName {

  private class DeleteCallback extends BeeCommand {
    private final long rowId;
    private final long version;

    private DeleteCallback(long rowId, long version) {
      this.rowId = rowId;
      this.version = version;
    }

    @Override
    public void execute() {
      setLoadingState(LoadingStateChangeEvent.LoadingState.LOADING);

      Queries.deleteRow(getViewName(), rowId, version, new Queries.IntCallback() {
        public void onFailure(String[] reason) {
          setLoadingState(LoadingStateChangeEvent.LoadingState.LOADED);
          showFailure("Delete Record", reason);
        }

        public void onSuccess(Integer result) {
          BeeKeeper.getBus().fireEvent(new RowDeleteEvent(getViewName(), rowId));
        }
      });
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

  private final FormContainerView formContainer;
  private final Provider dataProvider;

  private final Set<HandlerRegistration> filterChangeHandlers = Sets.newHashSet();
  private Filter lastFilter = null;

  public FormPresenter(FormDescription formDescription, String viewName, int rowCount,
      BeeRowSet rowSet, boolean async, FormCallback callback) {
    List<BeeColumn> columns = (rowSet == null) ? null : rowSet.getColumns();

    this.formContainer = createView(formDescription, columns, rowCount, callback);
    this.dataProvider = createProvider(formContainer, viewName, columns, null, rowSet, async);

    bind();
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
    if (getDataProvider() == null) {
      return null;
    }
    return getDataProvider().getViewName();
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
    rs.addRow(rowId, version, new String[] {event.getOldValue()});
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

  private Provider createProvider(FormContainerView view, String dataName, List<BeeColumn> columns,
      Filter dataFilter, BeeRowSet rowSet, boolean isAsync) {
    if (BeeUtils.isEmpty(dataName)) {
      return null;
    }
    Provider provider;
    FormView content = view.getContent();

    if (isAsync) {
      provider = new AsyncProvider(content.getDisplay(), dataName, columns, null, null, dataFilter);
    } else {
      provider = new CachedProvider(content.getDisplay(), dataName, columns, null, null,
          dataFilter, rowSet);
    }
    return provider;
  }

  private FormContainerView createView(FormDescription formDescription,
      List<BeeColumn> columns, int rowCount, FormCallback callback) {
    FormContainerView view = new FormContainerImpl();

    view.create(formDescription, columns, rowCount, callback);
    return view;
  }

  private void deleteRow(long rowId, long version) {
    Global.getMsgBoxen().confirm("Delete Record ?", new DeleteCallback(rowId, version),
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
    return getDataProvider() != null;
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

  private void showWarning(String... messages) {
    getView().getContent().notifyWarning(messages);
  }

  private void updateFilter() {
    Collection<SearchView> searchers = getSearchers();
    Assert.notNull(searchers);

    List<Filter> filters = Lists.newArrayListWithCapacity(searchers.size());
    for (SearchView search : searchers) {
      Filter flt = search.getFilter(getDataProvider().getColumns(), null, null);
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
        filter = Filter.and(filters);
    }

    if (Objects.equal(filter, getLastFilter())) {
      BeeKeeper.getLog().info("filter not changed", filter);
      return;
    }

    lastFilter = filter;
    Queries.getRowCount(getViewName(), filter, new FilterCallback(filter));
  }
}

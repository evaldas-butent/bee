package com.butent.bee.client.presenter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.AsyncProvider;
import com.butent.bee.client.data.CachedProvider;
import com.butent.bee.client.data.HasActiveRow;
import com.butent.bee.client.data.HasDataProvider;
import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.data.LocalProvider;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.view.FormContainerImpl;
import com.butent.bee.client.view.FormContainerView;
import com.butent.bee.client.view.HasSearch;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class FormPresenter extends AbstractPresenter implements ReadyForInsertEvent.Handler,
    ReadyForUpdateEvent.Handler, HasViewName, HasSearch, HasDataProvider, HasActiveRow {

  private class DeleteCallback extends ConfirmationCallback {
    private final long rowId;
    private final long version;

    private DeleteCallback(long rowId, long version) {
      this.rowId = rowId;
      this.version = version;
    }

    @Override
    public void onConfirm() {
      Queries.deleteRow(getViewName(), rowId, version, new Queries.IntCallback() {
        @Override
        public void onFailure(String... reason) {
          showFailure("Delete Record", reason);
        }

        @Override
        public void onSuccess(Integer result) {
          BeeKeeper.getBus().fireEvent(new RowDeleteEvent(getViewName(), rowId));
        }
      });
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(FormPresenter.class);
  
  private final FormContainerView formContainer;
  private final Provider dataProvider;

  private final Set<HandlerRegistration> filterChangeHandlers = Sets.newHashSet();
  private Filter lastFilter = null;

  public FormPresenter(FormDescription formDescription, String viewName, int rowCount,
      BeeRowSet rowSet, Provider.Type providerType, CachingPolicy cachingPolicy,
      FormCallback callback) {

    List<BeeColumn> columns = (rowSet == null) ? null : rowSet.getColumns();

    this.formContainer = createView(formDescription, columns, rowCount, callback);
    this.dataProvider = createProvider(formContainer, viewName, columns, rowSet, providerType,
        cachingPolicy);

    bind();
  }

  @Override
  public IsRow getActiveRow() {
    return getView().getContent().getActiveRow();
  }

  @Override
  public String getCaption() {
    return getView().getCaption();
  }
  
  @Override
  public Provider getDataProvider() {
    return dataProvider;
  }

  @Override
  public HeaderView getHeader() {
    return getView().getHeader();
  }

  public Filter getLastFilter() {
    return lastFilter;
  }

  public NotificationListener getNotificationListener() {
    return getView().getContent();
  }

  @Override
  public Collection<SearchView> getSearchers() {
    Collection<SearchView> searchers;

    if (getView() instanceof HasSearch) {
      searchers = ((HasSearch) getView()).getSearchers();
    } else {
      searchers = null;
    }
    return searchers;
  }

  public FormContainerView getView() {
    return formContainer;
  }

  @Override
  public String getViewName() {
    if (getDataProvider() == null) {
      return null;
    }
    return getDataProvider().getViewName();
  }

  @Override
  public Widget getWidget() {
    return getView().asWidget();
  }

  @Override
  public void handleAction(Action action) {
    Assert.notNull(action);

    if (getFormCallback() != null && !getFormCallback().beforeAction(action, this)) {
      return;
    }

    switch (action) {
      case CLOSE:
        BeeKeeper.getScreen().closeWidget(getView().asWidget());
        break;

      case CONFIGURE:
        Global.inputString("Options", new StringCallback() {
          @Override
          public void onSuccess(String value) {
            getView().getContent().applyOptions(value);
          }
        });
        break;

      case DELETE:
        if (hasData() && getView().getContent().isRowEditable(true)) {
          IsRow row = getView().getContent().getActiveRow();
          deleteRow(row.getId(), row.getVersion());
        }
        break;

      case REFRESH:
        if (hasData()) {
          getDataProvider().refresh(true);
        }
        break;

      case ADD:
        if (hasData()) {
          getView().getContent().startNewRow();
        }
        break;

      case PRINT:
        Printer.print(getView());
        break;
        
      default:
        logger.info(action, "not implemented");
    }

    if (getFormCallback() != null) {
      getFormCallback().afterAction(action, this);
    }
  }

  @Override
  public boolean onReadyForInsert(final ReadyForInsertEvent event) {
    Queries.insert(getViewName(), event.getColumns(), event.getValues(), new RowCallback() {
      @Override
      public void onFailure(String... reason) {
        if (event.getCallback() == null) {
          showFailure("Insert Row", reason);
        } else {
          event.getCallback().onFailure(reason);
        }
      }

      @Override
      public void onSuccess(BeeRow result) {
        BeeKeeper.getBus().fireEvent(new RowInsertEvent(getViewName(), result));
        if (event.getCallback() != null) {
          event.getCallback().onSuccess(result);
        }
      }
    });

    return true;
  }

  @Override
  public boolean onReadyForUpdate(final ReadyForUpdateEvent event) {
    final long rowId = event.getRowValue().getId();
    final long version = event.getRowValue().getVersion();
    final String columnId = event.getColumn().getId();
    final String newValue = event.getNewValue();

    BeeRowSet rs = new BeeRowSet(new BeeColumn(event.getColumn().getType(), columnId));
    rs.setViewName(getViewName());
    rs.addRow(rowId, version, new String[] {event.getOldValue()});
    rs.getRow(0).preliminaryUpdate(0, newValue);

    final boolean rowMode = event.isRowMode();

    RowCallback rowCallback = new RowCallback() {
      @Override
      public void onFailure(String... reason) {
        if (event.getCallback() != null) {
          event.getCallback().onFailure(reason);
        }
      }

      @Override
      public void onSuccess(BeeRow row) {
        if (event.getCallback() != null) {
          event.getCallback().onSuccess(row);
        }

        if (rowMode) {
          BeeKeeper.getBus().fireEvent(new RowUpdateEvent(getViewName(), row));
        } else {
          String value = row.getString(0);
          BeeKeeper.getBus().fireEvent(
              new CellUpdateEvent(getViewName(), rowId, row.getVersion(), columnId,
                  getDataProvider().getColumnIndex(columnId), value));
        }
      }
    };
    
    if (rowMode) {
      Queries.updateRow(rs, rowCallback);
    } else {
      Queries.updateCell(rs, rowCallback);
    }
    return true;
  }

  @Override
  public void onViewUnload() {
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
          filterChangeHandlers.add(search.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
              updateFilter();
            }
          }));
        }
      }

      view.getContent().addReadyForUpdateHandler(this);
      view.getContent().addReadyForInsertHandler(this);
    }
  }

  private Provider createProvider(FormContainerView view, String viewName, List<BeeColumn> columns,
      BeeRowSet rowSet, Provider.Type providerType, CachingPolicy cachingPolicy) {
    if (BeeUtils.isEmpty(viewName) || providerType == null) {
      return null;
    }

    HasDataTable display = view.getContent().getDisplay();
    NotificationListener notificationListener = view.getContent();
    Provider provider;

    switch (providerType) {
      case ASYNC:
        provider = new AsyncProvider(display, notificationListener, viewName, columns,
            null, null, null, cachingPolicy);
        break;
      case CACHED:
        provider = new CachedProvider(display, notificationListener, viewName, columns, rowSet);
        break;
      case LOCAL:
        provider = new LocalProvider(display, notificationListener, viewName, columns, rowSet);
        break;
      default:
        Assert.untouchable();
        provider = null;
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
    Global.getMsgBoxen().confirm(null, "Delete Record ?", new DeleteCallback(rowId, version),
        StyleUtils.NAME_SCARY, null);
  }

  private FormCallback getFormCallback() {
    return getView().getContent().getFormCallback();
  }

  private boolean hasData() {
    return getDataProvider() != null;
  }

  private void showFailure(String activity, String... reasons) {
    List<String> messages = Lists.newArrayList(activity);
    if (reasons != null) {
      messages.addAll(Lists.newArrayList(reasons));
    }
    getNotificationListener().notifySevere(ArrayUtils.toArray(messages));
  }

  private void updateFilter() {
    Filter filter = ViewHelper.getFilter(this, getDataProvider());
    if (Objects.equal(filter, getLastFilter())) {
      logger.info("filter not changed", filter);
    } else {
      lastFilter = filter;
      getDataProvider().onFilterChange(filter, true);
    }
  }
}

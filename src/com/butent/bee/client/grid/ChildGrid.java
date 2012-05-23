package com.butent.bee.client.grid;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.layout.ResizePanel;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.HasFosterParent;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Launchable;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ParentRowEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;
import java.util.Map;

/**
 * Enables using data grids with data related to another source.
 */

public class ChildGrid extends ResizePanel implements HasEnabled, Launchable, HasFosterParent,
    ParentRowEvent.Handler {

  private final String gridName;

  private final int parentIndex;
  private final String relSource;

  private GridFactory.GridOptions gridOptions;

  private GridCallback gridCallback = null;
  private GridDescription gridDescription = null;
  private GridPresenter presenter = null;

  private IsRow pendingRow = null;
  private Boolean pendingEnabled = null;

  private String parentId = null;
  private HandlerRegistration parentRowReg = null;

  public ChildGrid(String gridName, int parentIndex, String relSource,
      GridFactory.GridOptions gridOptions) {
    super();

    this.gridName = gridName;
    this.parentIndex = parentIndex;
    this.relSource = relSource;
    this.gridOptions = gridOptions;

    this.gridCallback = GridFactory.getGridCallback(gridName);

    addStyleName("bee-child-grid");
  }

  public GridFactory.GridOptions getGridOptions() {
    return gridOptions;
  }

  @Override
  public String getIdPrefix() {
    return "child-grid";
  }

  public String getParentId() {
    return parentId;
  }

  public GridPresenter getPresenter() {
    return presenter;
  }

  public boolean isEnabled() {
    if (getPresenter() == null) {
      return false;
    }
    return getPresenter().getView().isEnabled();
  }

  public void launch() {
    GridFactory.getGrid(gridName, new Callback<GridDescription>() {
      public void onSuccess(GridDescription result) {
        if (getGridCallback() != null && !getGridCallback().onLoad(result)) {
          return;
        }
        setGridDescription(result);
        resolveState();
      }
    });
  }

  public void onParentRow(ParentRowEvent event) {
    if (getGridCallback() != null) {
      getGridCallback().onParentRow(event);
    }

    setPendingRow(event.getRow());
    setPendingEnabled(event.isEnabled());
    
    resolveState();
  }

  public void setEnabled(boolean enabled) {
    if (getPresenter() != null) {
      getPresenter().getView().setEnabled(enabled);
    }
  }

  public void setGridCallback(GridCallback gridCallback) {
    this.gridCallback = gridCallback;
  }

  public void setGridOptions(GridFactory.GridOptions gridOptions) {
    this.gridOptions = gridOptions;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
    if (isAttached()) {
      register();
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    register();
  }

  @Override
  protected void onUnload() {
    unregister();
    super.onUnload();
  }

  private void createPresenter(IsRow row, BeeRowSet rowSet, Filter immutableFilter,
      Map<String, Filter> initialFilters, Order order) {

    GridPresenter gp = new GridPresenter(getGridDescription(), rowSet.getNumberOfRows(), rowSet,
        Provider.Type.ASYNC, EnumSet.of(UiOption.CHILD), getGridCallback(), immutableFilter,
        initialFilters, order, getGridOptions());

    gp.getView().getContent().setRelColumn(getRelSource());
    gp.getView().getContent().getGrid().setPageSize(BeeConst.UNDEF, false, false);
    gp.setEventSource(getId());

    setWidget(gp.getWidget());
    setPresenter(gp);

    if (BeeUtils.equals(row, getPendingRow())) {
      updateFilter(row);
      resetState();
      if (row == null) {
        setEnabled(false);
      }
    } else {
      resolveState();
    }
  }

  private Filter getFilter(IsRow row) {
    return ComparisonFilter.isEqual(getRelSource(), new LongValue(getParentValue(row)));
  }

  private GridCallback getGridCallback() {
    return gridCallback;
  }

  private GridDescription getGridDescription() {
    return gridDescription;
  }

  private void getInitialRowSet(final IsRow row) {
    final Filter immutableFilter =
        GridFactory.getImmutableFilter(getGridDescription(), getGridOptions());
    final Map<String, Filter> initialFilters =
        (getGridCallback() == null) ? null : getGridCallback().getInitialFilters();

    final Order order = GridFactory.getOrder(getGridDescription(), getGridOptions());

    if (row == null) {
      DataInfo dataInfo = Global.getDataInfo(getGridDescription().getViewName());
      if (dataInfo != null) {
        BeeRowSet rowSet = new BeeRowSet(dataInfo.getColumns());
        createPresenter(row, rowSet, immutableFilter, initialFilters, order);
        return;
      }
    }

    Filter queryFilter = Filter.and(getFilter(row),
        GridFactory.getInitialQueryFilter(immutableFilter, initialFilters));

    Queries.getRowSet(getGridDescription().getViewName(), null, queryFilter, order,
        getGridDescription().getCachingPolicy(), new Queries.RowSetCallback() {
          public void onSuccess(BeeRowSet rowSet) {
            createPresenter(row, rowSet, immutableFilter, initialFilters, order);
          }
        });
  }

  private int getParentIndex() {
    return parentIndex;
  }

  private HandlerRegistration getParentRowReg() {
    return parentRowReg;
  }

  private long getParentValue(IsRow row) {
    if (row == null) {
      return 0;
    } else if (getParentIndex() >= 0) {
      return row.getLong(getParentIndex());
    } else {
      return row.getId();
    }
  }

  private Boolean getPendingEnabled() {
    return pendingEnabled;
  }

  private IsRow getPendingRow() {
    return pendingRow;
  }

  private String getRelSource() {
    return relSource;
  }

  private boolean hasParentValue(IsRow row) {
    return getParentValue(row) != 0;
  }

  private void register() {
    unregister();
    if (!BeeUtils.isEmpty(getParentId())) {
      setParentRowReg(BeeKeeper.getBus().registerParentRowHandler(getParentId(), this));
    }
  }

  private void resetState() {
    if (getPendingEnabled() != null) {
      setEnabled(getPendingEnabled());
      setPendingEnabled(null);
    }
    setPendingRow(null);
  }

  private void resolveState() {
    if (getGridDescription() == null) {
      return;
    }

    if (getPresenter() == null) {
      getInitialRowSet(getPendingRow());

    } else {
      getPresenter().getView().getContent().getGrid().deactivate();
      getPresenter().getView().getContent().ensureGridVisible();

      updateFilter(getPendingRow());

      if (hasParentValue(getPendingRow())) {
        getPresenter().requery(false);
      } else {
        setEnabled(false);
        setPendingEnabled(null);
        getPresenter().getDataProvider().clear();
      }

      resetState();
    }
  }

  private void setGridDescription(GridDescription gridDescription) {
    this.gridDescription = gridDescription;
  }

  private void setParentRowReg(HandlerRegistration parentRowReg) {
    this.parentRowReg = parentRowReg;
  }

  private void setPendingEnabled(Boolean pendingEnabled) {
    this.pendingEnabled = pendingEnabled;
  }

  private void setPendingRow(IsRow pendingRow) {
    this.pendingRow = pendingRow;
  }

  private void setPresenter(GridPresenter presenter) {
    this.presenter = presenter;
  }

  private void unregister() {
    if (getParentRowReg() != null) {
      getParentRowReg().removeHandler();
      setParentRowReg(null);
    }
  }

  private void updateFilter(IsRow row) {
    if (getPresenter() != null) {
      getPresenter().getDataProvider().setParentFilter(getId(), getFilter(row));
      getPresenter().getView().getContent().setRelId(getParentValue(row));
    }
  }
}

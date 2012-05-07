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
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Launchable;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ParentRowEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;
import java.util.Map;

/**
 * Enables using data grids with data related to another source.
 */

public class ChildGrid extends ResizePanel implements HasEnabled, Launchable, HasFosterParent,
    ParentRowEvent.Handler, HasViewName {

  private String viewName = null;
  private String caption = null;

  private Filter filter = null;
  private Order order = null;
  
  private final int parentIndex;
  private final String relSource;

  private GridCallback gridCallback = null;
  private GridDescription gridDescription = null;
  private GridPresenter presenter = null;

  private IsRow pendingRow = null;
  private Boolean pendingEnabled = null;

  private String parentId = null;
  private HandlerRegistration parentRowReg = null;

  public ChildGrid(String name, int parentIndex, String relSource, Map<String, String> attributes) {
    super();
    this.parentIndex = parentIndex;
    this.relSource = relSource;
    
    setAttributes(name, attributes);

    this.gridCallback = GridFactory.getGridCallback(getViewName());

    addStyleName("bee-child-grid");
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

  public String getViewName() {
    return viewName;
  }

  public boolean isEnabled() {
    if (getPresenter() == null) {
      return false;
    }
    return getPresenter().getView().isEnabled();
  }

  public void launch() {
    GridFactory.getGrid(getViewName(), new Callback<GridDescription>() {
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
    Assert.notNull(event);
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

  public void setParentId(String parentId) {
    this.parentId = parentId;
    if (isAttached()) {
      register();
    }
  }

  public void setViewName(String viewName) {
    this.viewName = viewName;
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

  private void createPresenter(final IsRow row) {
    final Map<String, Filter> initialFilters =
        (getGridCallback() == null) ? null : getGridCallback().getInitialFilters();
    Filter flt = Filter.and(getFilter(row),
        GridFactory.getInitialQueryFilter(getGridDescription(), initialFilters));

    Queries.getRowSet(getViewName(), null, flt, getGridDescription().getOrder(),
        getGridDescription().getCachingPolicy(), new Queries.RowSetCallback() {
          public void onSuccess(BeeRowSet rowSet) {
            Assert.notNull(rowSet);
            GridPresenter gp = new GridPresenter(getViewName(), rowSet.getNumberOfRows(),
                rowSet, Provider.Type.ASYNC, getGridDescription(), getGridCallback(),
                initialFilters, EnumSet.of(UiOption.CHILD));

            gp.getView().getContent().setRelColumn(getRelSource());
            gp.getView().getContent().getGrid().setPageSize(BeeConst.UNDEF, false, false);
            gp.setEventSource(getId());

            setWidget(gp.getWidget());
            setPresenter(gp);

            if (row == getPendingRow()) {
              updateFilter(row);
              resetState();
            } else {
              resolveState();
            }
          }
        });
  }

  private String getCaption() {
    return caption;
  }

  private Filter getFilter() {
    return filter;
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

  private Order getOrder() {
    return order;
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

    boolean hasParent = hasParentValue(getPendingRow());

    if (getPresenter() == null) {
      if (hasParent) {
        createPresenter(getPendingRow());
      }
    } else {
      getPresenter().getView().getContent().getGrid().deactivate();
      getPresenter().getView().getContent().ensureGridVisible();

      updateFilter(getPendingRow());

      if (hasParent) {
        getPresenter().requery(false);
      } else {
        setEnabled(false);
        setPendingEnabled(null);
        getPresenter().getDataProvider().clear();
      }

      resetState();
    }
  }

  private void setAttributes(String name, Map<String, String> attributes) {
    if (attributes == null) {
      return;
    }
    
    String view = attributes.get(UiConstants.ATTR_VIEW_NAME);
    if (BeeUtils.isEmpty(view)) {
      setViewName(name.trim());
    } else {
      setViewName(view.trim());
    }

    String cap = attributes.get(UiConstants.ATTR_CAPTION);
    if (!BeeUtils.isEmpty(cap)) {
      setCaption(cap.trim());
    }

    String flt = attributes.get(UiConstants.ATTR_FILTER);
    String ord = attributes.get(UiConstants.ATTR_ORDER);
    
    if (!BeeUtils.allEmpty(flt, ord)) {
      DataInfo viewInfo = Global.getDataInfo(getViewName(), true);

      if (viewInfo != null && !BeeUtils.isEmpty(flt)) {
        setFilter(DataUtils.parseCondition(flt, viewInfo.getColumns(),
            viewInfo.getIdColumn(), viewInfo.getVersionColumn()));
      }
      if (viewInfo != null && !BeeUtils.isEmpty(ord)) {
        setOrder(Order.parse(ord, viewInfo.getColumnNames()));
      }
    }
  }

  private void setCaption(String caption) {
    this.caption = caption;
  }

  private void setFilter(Filter filter) {
    this.filter = filter;
  }

  private void setGridDescription(GridDescription gridDescription) {
    this.gridDescription = gridDescription;
    
    if (!BeeUtils.isEmpty(getCaption())) {
      this.gridDescription.setCaption(getCaption());
    }

    if (getFilter() != null) {
      this.gridDescription.setFilter(getFilter());
    }
    if (getOrder() != null) {
      this.gridDescription.setOrder(getOrder());
    }
  }

  private void setOrder(Order order) {
    this.order = order;
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

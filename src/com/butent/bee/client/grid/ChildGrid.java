package com.butent.bee.client.grid;

import com.google.gwt.user.client.ui.HasEnabled;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.layout.ResizePanel;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables using data grids with data related to another source.
 */

public class ChildGrid extends ResizePanel implements HasEnabled {

  public static int initialRowSetSize = 1;
  
  private String viewName = null;
  private final String relSource;

  private GridPresenter presenter = null;

  private Long pendingId = null;
  private Boolean pendingEnabled = null;

  public ChildGrid(String gridName, String relSource) {
    super();
    this.relSource = relSource;

    GridFactory.getGrid(gridName, new GridFactory.DescriptionCallback() {
      public void onFailure(String[] reason) {
        BeeKeeper.getScreen().notifySevere(reason);
      }

      public void onSuccess(GridDescription result) {
        setViewName(result.getViewName());
        getInitialRowSet(result);
      }
    });
  }

  @Override
  public String getIdPrefix() {
    return "child-grid";
  }

  public boolean isEnabled() {
    if (getPresenter() == null) {
      return false;
    }
    return getPresenter().getView().isEnabled();
  }

  public void refresh(final long parentId, final Boolean parentEnabled) {
    if (getPresenter() == null || getPresenter().getDataProvider() == null) {
      setPendingId(parentId);
      if (parentEnabled != null) {
        setPendingEnabled(parentEnabled);
      }
      return;
    }

    final Filter filter = new ColumnValueFilter(getRelSource(), Operator.EQ,
        new LongValue(parentId));
    Queries.getRowCount(getViewName(), filter, new Queries.IntCallback() {
      public void onFailure(String[] reason) {
      }

      public void onSuccess(Integer result) {
        getPresenter().getView().getContent().setRelId(parentId);
        getPresenter().getView().getContent().getGrid().setRowCount(result);
        getPresenter().getView().getContent().getGrid().setPageSize(result);
        getPresenter().getDataProvider().onFilterChanged(filter, result);

        if (parentEnabled != null) {
          setEnabled(parentEnabled);
        }
      }
    });
  }

  public void setEnabled(boolean enabled) {
    if (getPresenter() != null) {
      getPresenter().getView().setEnabled(enabled);
    }
  }

  private void getInitialRowSet(final GridDescription gridDescription) {
    int limit = BeeUtils.unbox(gridDescription.getInitialRowSetSize());
    if (limit <= 0) {
      limit = initialRowSetSize;
    }
    
    Queries.getRowSet(getViewName(), null, null, null, 0, limit, CachingPolicy.NONE,
        new Queries.RowSetCallback() {
          public void onFailure(String[] reason) {
          }

          public void onSuccess(BeeRowSet rowSet) {
            GridPresenter gp = new GridPresenter(getViewName(), rowSet.getNumberOfRows(), 
                rowSet, true, gridDescription, null, true);
            setWidget(gp.getWidget());
            setPresenter(gp);
            gp.getView().getContent().setRelColumn(getRelSource());

            if (getPendingId() != null) {
              refresh(getPendingId(), getPendingEnabled());
              setPendingId(null);
              setPendingEnabled(null);
            }
          }
        });
  }

  private Boolean getPendingEnabled() {
    return pendingEnabled;
  }

  private Long getPendingId() {
    return pendingId;
  }

  private GridPresenter getPresenter() {
    return presenter;
  }

  private String getRelSource() {
    return relSource;
  }

  private String getViewName() {
    return viewName;
  }

  private void setPendingEnabled(Boolean pendingEnabled) {
    this.pendingEnabled = pendingEnabled;
  }

  private void setPendingId(Long pendingId) {
    this.pendingId = pendingId;
  }

  private void setPresenter(GridPresenter presenter) {
    this.presenter = presenter;
  }

  private void setViewName(String viewName) {
    this.viewName = viewName;
  }
}

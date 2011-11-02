package com.butent.bee.client.grid;

import com.google.gwt.user.client.ui.HasEnabled;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.layout.ResizePanel;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.ui.GridDescription;

import java.util.EnumSet;

/**
 * Enables using data grids with data related to another source.
 */

public class ChildGrid extends ResizePanel implements HasEnabled {

  private final int parentIndex;
  private final String relSource;

  private final GridCallback gridCallback;  
  private GridDescription gridDescription = null;
  private GridPresenter presenter = null;

  private IsRow pendingRow = null;
  private Boolean pendingEnabled = null;
  
  public ChildGrid(String gridName, int parentIndex, String relSource) {
    super();
    this.parentIndex = parentIndex;
    this.relSource = relSource;
    
    this.gridCallback = GridFactory.getGridCallback(gridName);
    
    addStyleName("bee-child-grid");

    GridFactory.getGrid(gridName, new GridFactory.DescriptionCallback() {
      public void onFailure(String[] reason) {
        BeeKeeper.getScreen().notifySevere(reason);
      }

      public void onSuccess(GridDescription result) {
        if (getGridCallback() != null && !getGridCallback().onLoad(result)) {
          return;
        }
        setGridDescription(result);
        resolveState();
      }
    });
  }

  @Override
  public String getIdPrefix() {
    return "child-grid";
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

  public void refresh(IsRow parentRow, Boolean parentEnabled) {
    setPendingRow(parentRow);
    setPendingEnabled(parentEnabled);
    resolveState();
  }

  public void setEnabled(boolean enabled) {
    if (getPresenter() != null) {
      getPresenter().getView().setEnabled(enabled);
    }
  }

  private void createPresenter(final IsRow row) {
    final String viewName = getGridDescription().getViewName();
    Filter filter = CompoundFilter.and(getGridDescription().getFilter(), getFilter(row));
    
    Queries.getRowSet(viewName, null, filter, getGridDescription().getOrder(), 
        getGridDescription().getCachingPolicy(), new Queries.RowSetCallback() {
          public void onFailure(String[] reason) {
          }

          public void onSuccess(BeeRowSet rowSet) {
            Assert.notNull(rowSet);
            GridPresenter gp = new GridPresenter(viewName, rowSet.getNumberOfRows(), 
                rowSet, true, getGridDescription(), getGridCallback(), EnumSet.of(UiOption.CHILD));
            
            gp.getView().getContent().setRelColumn(getRelSource());
            gp.getView().getContent().getGrid().setPageSize(BeeConst.UNDEF, false, false);

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

  private Filter getFilter(IsRow row) {
    return new ColumnValueFilter(getRelSource(), Operator.EQ, new LongValue(getParentValue(row)));
  }

  private GridCallback getGridCallback() {
    return gridCallback;
  }

  private GridDescription getGridDescription() {
    return gridDescription;
  }

  private int getParentIndex() {
    return parentIndex;
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

  private void resetState() {
    if (getPendingEnabled() != null) {
      setEnabled(getPendingEnabled());
    }
    setPendingRow(null);
    setPendingEnabled(null);
  }
  
  private void resolveState() {
    if (getGridDescription() == null || getPendingRow() == null) {
      return;
    }
    
    if (getPresenter() == null) {
      createPresenter(getPendingRow());
    } else {
      updateFilter(getPendingRow());
      getPresenter().getDataProvider().requery(false);
      resetState();
    }
  }
  
  private void setGridDescription(GridDescription gridDescription) {
    this.gridDescription = gridDescription;
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
  
  private void updateFilter(IsRow row) {
    if (getPresenter() != null) {
      getPresenter().getDataProvider().setParentFilter(getId(), getFilter(row), false);
      getPresenter().getView().getContent().setRelId(getParentValue(row));
    }
  }
}

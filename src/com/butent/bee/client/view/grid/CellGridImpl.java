package com.butent.bee.client.view.grid;

import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.view.client.MultiSelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.KeyProvider;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.CellColumn;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.RowIdColumn;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.IsRow;

import java.util.List;

public class CellGridImpl extends CellGrid implements GridView {

  private Presenter viewPresenter = null;

  public CellGridImpl(int pageSize) {
    super(pageSize);
    
    setHeaderCellHeight(25);
    setBodyCellHeight(24);
  }

  public void create(List<BeeColumn> dataCols, int rowCount) {
    RowIdColumn idColumn = new RowIdColumn();
    addColumn(idColumn, new TextHeader("Id"));
    setColumnWidth(idColumn, 40);

    BeeColumn dataColumn;
    CellColumn<?> column;
    for (int i = 0; i < dataCols.size(); i++) {
      dataColumn = dataCols.get(i);
      column = GridFactory.createColumn(dataColumn, i);
      addColumn(column, new ColumnHeader(dataColumn));
    }

    MultiSelectionModel<IsRow> selector = new MultiSelectionModel<IsRow>(new KeyProvider());
    setSelectionModel(selector);

    setRowCount(rowCount, true);
    setKeyboardPagingPolicy(KeyboardPagingPolicy.CHANGE_PAGE);
  }
  
  public int estimatePageSize(int containerWidth, int containerHeight) {
    int rh = getBodyCellHeight();

    int z = containerHeight;
    if (getHeaderCellHeight() > 0) {
      z -= getHeaderCellHeight();
    }
    if (getFooterCellHeight() > 0) {
      z -= getFooterCellHeight();
    }
    
    int width = getOffsetWidth();
    if (width <= 0 || width > containerWidth) {
      z -= DomUtils.getScrollbarHeight();
    }
    
    BeeKeeper.getLog().info("estimate", containerWidth, containerHeight, width, z, rh, z / rh);

    if (rh > 0 && z > rh) {
      return z / rh;
    }
    return BeeConst.SIZE_UNKNOWN;
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  public String getWidgetId() {
    return getId();
  }

  public void setViewPresenter(Presenter presenter) {
    this.viewPresenter = presenter;
  }

  public void updatePageSize(int pageSize, boolean init) {
    Assert.isPositive(pageSize);
    int oldSize = getPageSize();
    
    BeeKeeper.getLog().info("page size", oldSize, "/", pageSize, init);

    if (oldSize == pageSize) {
      if (init) {
        setVisibleRangeAndClearData(getVisibleRange(), true);
      }
    } else {
      setPageSize(pageSize);
    }
  }
}

package com.butent.bee.client.view.grid;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.view.client.MultiSelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.KeyProvider;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.CellColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.RowIdColumn;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class CellGridImpl extends CellGrid implements GridView, SearchView {

  private class FilterUpdater implements ValueUpdater<String> {
    public void update(String value) {
      if (filterChangeHandler != null) {
        filterChangeHandler.onChange(null);
      }
    }
  }

  private Presenter viewPresenter = null;

  private ChangeHandler filterChangeHandler = null;
  private final FilterUpdater filterUpdater = new FilterUpdater();

  public CellGridImpl() {
    super();
  }

  public void create(List<BeeColumn> dataCols, int rowCount, BeeRowSet rowSet) {
    setHeaderCellHeight(25);
    setBodyCellHeight(24);

    boolean footers = rowCount > 10;
    if (footers) {
      setFooterCellHeight(25);
    }

    RowIdColumn idColumn = new RowIdColumn();
    String id = "row-id";
    addColumn(id, idColumn, new TextHeader("Id"));
    setColumnWidth(id, 40);

    BeeColumn dataColumn;
    CellColumn<?> column;
    for (int i = 0; i < dataCols.size(); i++) {
      dataColumn = dataCols.get(i);
      column = GridFactory.createColumn(dataColumn, i);
      column.setSortable(true);

      if (footers) {
        addColumn(dataColumn.getLabel(), column, new ColumnHeader(dataColumn),
            new ColumnFooter(dataColumn, filterUpdater));
      } else {
        addColumn(dataColumn.getLabel(), column, new ColumnHeader(dataColumn));
      }
    }

    MultiSelectionModel<IsRow> selector = new MultiSelectionModel<IsRow>(new KeyProvider());
    setSelectionModel(selector);

    setRowCount(rowCount, true);

    if (rowSet != null) {
      estimateColumnWidths(rowSet.getRows().getList(), Math.min(rowSet.getNumberOfRows(), 3));
    }
    estimateHeaderWidths();
  }

  public int estimatePageSize(int containerWidth, int containerHeight) {
    int rh = getBodyCellHeight() + getBodyCellHeightIncrement();

    int z = containerHeight - getHeaderHeight() - getFooterHeight();

    int width = getBodyWidth();
    if (width <= 0 || width > containerWidth) {
      z -= DomUtils.getScrollbarHeight();
    }

    if (Global.isDebug()) {
      BeeKeeper.getLog().info("estimate", containerWidth, containerHeight, width, z, rh, z / rh);
    }

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

    if (Global.isDebug()) {
      BeeKeeper.getLog().info("page size", oldSize, "/", pageSize, init);
    }

    if (oldSize == pageSize) {
      if (init) {
        setVisibleRangeAndClearData(getVisibleRange(), true);
      }
    } else {
      setPageSize(pageSize);
    }
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    filterChangeHandler = handler;
    return new HandlerRegistration() {
      public void removeHandler() {
        filterChangeHandler = null;
      }
    };
  }

  public Filter getFilter(List<? extends IsColumn> columns) {
    List<Header<?>> footers = getFooters();

    if (footers == null || footers.size() <= 0) {
      return null;
    }
    Filter filter = null;

    for (Header<?> footer : footers) {
      if (!(footer instanceof ColumnFooter)) {
        continue;
      }
      String input = BeeUtils.trim(((ColumnFooter) footer).getValue());
      if (BeeUtils.isEmpty(input)) {
        continue;
      }
      IsColumn dataColumn = ((ColumnFooter) footer).getDataColumn();
      if (dataColumn == null) {
        continue;
      }
      Filter flt = DataUtils.parseExpression(dataColumn.getId() + " " + input, columns);

      if (flt == null) {
        continue;
      }
      if (filter == null) {
        filter = flt;
      } else {
        filter = CompoundFilter.and(filter, flt);
      }
    }
    return filter;
  }
}

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
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ColumnIsEmptyFilter;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.ValueType;
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

  public CellGridImpl(int pageSize) {
    super(pageSize);
  }

  public void create(List<BeeColumn> dataCols, int rowCount, BeeRowSet rowSet) {
    setHeaderCellHeight(25);
    setBodyCellHeight(24);

    boolean footers = rowCount > 10;
    if (footers) {
      setFooterCellHeight(25);
    }
    
    RowIdColumn idColumn = new RowIdColumn();
    addColumn(idColumn, new TextHeader("Id"));
    setColumnWidth(idColumn, 40);

    BeeColumn dataColumn;
    CellColumn<?> column;
    for (int i = 0; i < dataCols.size(); i++) {
      dataColumn = dataCols.get(i);
      column = GridFactory.createColumn(dataColumn, i);
      column.setSortable(true);
      
      if (footers) {
        addColumn(column, new ColumnHeader(dataColumn),
            new ColumnFooter(dataColumn, filterUpdater));
      } else {
        addColumn(column, new ColumnHeader(dataColumn));
      }
    }

    MultiSelectionModel<IsRow> selector = new MultiSelectionModel<IsRow>(new KeyProvider());
    setSelectionModel(selector);

    setRowCount(rowCount, true);
    setKeyboardPagingPolicy(KeyboardPagingPolicy.CHANGE_PAGE);
    
    if (rowSet != null) {
      estimateColumnWidths(rowSet.getRows().getList(), Math.min(rowSet.getNumberOfRows(), 10));
    }
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
      
      String column = dataColumn.getLabel();
      Operator operator = null;
      String value = null;
      
      if (input.length() > 2) {
        operator = Operator.getOperator(BeeUtils.left(input, 2));
        if (operator != null) {
          value = input.substring(2).trim();
        }
      }
      if (operator == null && input.length() > 1) {
        operator = Operator.getOperator(BeeUtils.left(input, 1));
        if (operator != null) {
          value = input.substring(1).trim();
        }
      }
      
      if (operator == null) {
        if (ValueType.TEXT.equals(dataColumn.getType())) {
          operator = Operator.LIKE;
        } else {
          operator = Operator.EQ;
        }
        value = input;
      }
      
      Filter flt = null;
      if (BeeUtils.isEmpty(value)) {
        flt = new ColumnIsEmptyFilter(column);
      } else {
        flt = ComparisonFilter.compareWithValue(column, operator.toTextString(), 
            BeeUtils.isNumeric(value) ? BeeUtils.toDouble(value) : value);
      }
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

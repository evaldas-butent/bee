package com.butent.bee.client.presenter;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.AsyncProvider;
import com.butent.bee.client.data.DataProvider;
import com.butent.bee.client.data.KeyProvider;
import com.butent.bee.client.data.PageResizer;
import com.butent.bee.client.data.Pager;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.ScrollPager;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.grid.CellColumn;
import com.butent.bee.client.grid.CellGrid;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.RowIdColumn;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.view.SearchBox;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.Filter;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.ViewInfo;

import java.util.List;

public class GridPresenter {
  private final ViewInfo viewInfo;
  private final boolean async;
  private final List<BeeColumn> dataColumns;

  private int rowCount;

  private final CellGrid cellGrid;
  private final AbstractDataProvider<IsRow> dataProvider;
  private final Split gridView;

  public GridPresenter(ViewInfo viewInfo, BeeRowSet rowSet, boolean async) {
    super();
    this.viewInfo = viewInfo;
    this.async = async;
    this.dataColumns = rowSet.getColumns();
    this.rowCount = async ? viewInfo.getRowCount() : rowSet.getNumberOfRows();

    this.cellGrid = createGrid(dataColumns, rowCount);
    this.dataProvider = createProvider(viewInfo, rowSet, cellGrid, async);
    this.gridView = createView(cellGrid, rowCount, async);
  }

  public Widget getGridView() {
    return gridView;
  }

  private CellGrid createGrid(List<BeeColumn> dataCols, int pageSize) {
    CellGrid grid = new CellGrid(pageSize);
    grid.setTableLayoutFixed(true);

    grid.insertColumn(0, new RowIdColumn(), "Row Id");

    BeeColumn dataColumn;
    CellColumn<?> column;
    for (int i = 0; i < dataCols.size(); i++) {
      dataColumn = dataCols.get(i);
      column = GridFactory.createColumn(dataColumn, i);
      column.setSortable(true);
      grid.addColumn(column, new ColumnHeader(dataColumn));
    }

    MultiSelectionModel<IsRow> selector = new MultiSelectionModel<IsRow>(new KeyProvider());
    grid.setSelectionModel(selector);

    return grid;
  }

  private AbstractDataProvider<IsRow> createProvider(ViewInfo ti, BeeRowSet data, CellGrid grid,
      boolean isAsync) {
    AbstractDataProvider<IsRow> provider;
    if (isAsync) {
      provider = new AsyncProvider(ti, null, null);
    } else {
      provider = new DataProvider(data);
    }

    provider.addDataDisplay(grid);
    if (provider instanceof ColumnSortEvent.Handler) {
      grid.addColumnSortHandler((ColumnSortEvent.Handler) provider);
    }
    return provider;
  }

  private Split createView(CellGrid grid, int rows, boolean isAsync) {
    int pageSize = -1;

    if (isAsync) {
      grid.setRowCount(rows, true);
      AsyncHandler sorter = new AsyncHandler(grid);
      grid.addColumnSortHandler(sorter);
    }

    if (rows > 30) {
      pageSize = 20;
      grid.setPageSize(pageSize);
      grid.setKeyboardPagingPolicy(KeyboardPagingPolicy.CHANGE_PAGE);
    }

    Split panel = new Split(2);
    int x;
    int y;
    int w;

    BeeLayoutPanel header = new BeeLayoutPanel();
    header.addStyleName(StyleUtils.WINDOW_HEADER);
    x = 2;
    y = 2;
    w = 32;

    header.addRightWidthTop(new BeeImage(Global.getImages().close()), x, w, y);
    header.addRightWidthTop(new BeeImage(Global.getImages().configure()), x += w * 2, w, y);
    header.addRightWidthTop(new BeeImage(Global.getImages().save()), x += w, w, y);
    header.addRightWidthTop(new BeeImage(Global.getImages().bookmarkAdd()), x += w, w, y);
    header.addRightWidthTop(new BeeImage(Global.getImages().editDelete()), x += w, w, y);
    header.addRightWidthTop(new BeeImage(Global.getImages().editAdd()), x += w, w, y);
    header.addRightWidthTop(new BeeImage(Global.getImages().reload()), x += w, w, y);

    BeeLabel caption = new BeeLabel(getViewName());
    caption.addStyleName(StyleUtils.WINDOW_CAPTION);
    header.addLeftWidthTop(caption, 16, 200, 0);

    panel.addNorth(header, 22, null, -1);

    BeeLayoutPanel footer = new BeeLayoutPanel();
    footer.addStyleName(StyleUtils.WINDOW_FOOTER);
    x = 0;
    y = 2;

    if (rowCount > 10) {
      x += 4;
      Pager sp = new Pager(rowCount);
      sp.setDisplay(grid);
      w = 256;
      footer.addLeftWidthTop(sp, x, w, y);
      x += w;

      PageResizer psz = new PageResizer(grid.getPageSize(), 1, rowCount, 1);
      psz.setDisplay(grid);
      w = 64;
      footer.addLeftWidthTop(psz, x, w, y);
      x += w;
    }

    if (rowCount > 1) {
      x += 16;
      final SearchBox search = new SearchBox();
      footer.addLeftTop(search, x, y + 1);
      footer.setWidgetLeftRight(search, x, Unit.PX, 16, Unit.PX);

      if (dataProvider instanceof AsyncProvider) {
        search.addChangeHandler(new ChangeHandler() {
          @Override
          public void onChange(ChangeEvent event) {
            updateCondition(search.getCondition());
          }
        });
      }
    }

    panel.addSouth(footer, 32, null, -1);

    if (pageSize > 0) {
      ScrollPager scroll = new ScrollPager(pageSize, rowCount);
      scroll.setDisplay(grid);
      panel.addEast(scroll, DomUtils.getScrollbarWidth() + 1, null, -1);

      grid.sinkEvents(Event.ONMOUSEWHEEL);
    }

    panel.add(grid, ScrollBars.HORIZONTAL);

    return panel;
  }

  private String getViewName() {
    return viewInfo.getName();
  }

  private void updateCondition(final Filter condition) {
    BeeKeeper.getLog().info(condition == null ? "no condition" : condition);

    Queries.getRowCount(getViewName(), condition, new Queries.IntCallback() {
      public void onResponse(int value) {
        BeeKeeper.getLog().info(value);

        if (value > 0 && value != rowCount) {
          rowCount = value;
          if (async) {
            ((AsyncProvider) dataProvider).setWhere(condition);
            cellGrid.setRowCount(value, true);
            if (cellGrid.getPageSize() > value) {
              cellGrid.setPageSize(value);
            }
            cellGrid.setVisibleRangeAndClearData(cellGrid.getVisibleRange(), true);
          }
        }
      }
    });
  }
}

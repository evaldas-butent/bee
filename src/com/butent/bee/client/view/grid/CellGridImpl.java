package com.butent.bee.client.view.grid;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Font;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.CellColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.RowIdColumn;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Creates cell grid elements, connecting view and presenter elements of them.
 */

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

  public void applyOptions(String options) {
    if (BeeUtils.isEmpty(options)) {
      return;
    }
    
    boolean redraw = false;
    String[] opt = BeeUtils.split(options, ";");

    for (int i = 0; i < opt.length; i++) {
      String[] arr = BeeUtils.split(opt[i], " ");
      int len = arr.length;
      if (len <= 1) {
        continue;
      }
      String cmd = arr[0].trim().toLowerCase();
      String args = opt[i].trim().substring(cmd.length() + 1).trim();

      int[] xp = new int[len - 1];
      String[] sp = new String[len - 1];

      for (int j = 1; j < len; j++) {
        sp[j - 1] = arr[j].trim();
        if (BeeUtils.isDigit(arr[j])) {
          xp[j - 1] = BeeUtils.toInt(arr[j]);
        } else {
          xp[j - 1] = 0;
        }
      }

      Edges edges = null;
      switch (len - 1) {
        case 1:
          edges = new Edges(xp[0]);
          break;
        case 2:
          edges = new Edges(xp[0], xp[1]);
          break;
        case 3:
          edges = new Edges(xp[0], xp[1], xp[2]);
          break;
        default:
          edges = new Edges(xp[0], xp[1], xp[2], xp[3]);
      }

      String colId = sp[0];
      if (BeeUtils.isDigit(colId) && xp[0] < getColumnCount()) {
        colId = getColumnId(xp[0]);
      }
      int cc = getColumnCount();
      
      Font font = null;
      String msg = null;

      if (cmd.startsWith("bh")) {
        msg = "setBodyCellHeight " + xp[0];
        setBodyCellHeight(xp[0]);
        redraw = true;
      } else if (cmd.startsWith("bp")) {
        msg = "setBodyCellPadding " + edges.getCssValue();
        setBodyCellPadding(edges);
        redraw = true;
      } else if (cmd.startsWith("bw")) {
        msg = "setBodyBorderWidth " + edges.getCssValue();
        setBodyBorderWidth(edges);
        redraw = true;
      } else if (cmd.startsWith("bm")) {
        msg = "setBodyCellMargin " + edges.getCssValue();
        setBodyCellMargin(edges);
        redraw = true;
      } else if (cmd.startsWith("bf")) {
        font = Font.parse(args);
        msg = "setColumnBodyFont " + font.transform();
        for (int c = 0; c < cc; c++) {
          setColumnBodyFont(getColumnId(c), font);
        }
        redraw = true;

      } else if (cmd.startsWith("hh")) {
        msg = "setHeaderCellHeight " + xp[0];
        setHeaderCellHeight(xp[0]);
        redraw = true;
      } else if (cmd.startsWith("hp")) {
        msg = "setHeaderCellPadding " + edges.getCssValue();
        setHeaderCellPadding(edges);
        redraw = true;
      } else if (cmd.startsWith("hw")) {
        msg = "setHeaderBorderWidth " + edges.getCssValue();
        setHeaderBorderWidth(edges);
        redraw = true;
      } else if (cmd.startsWith("hm")) {
        msg = "setHeaderCellMargin " + edges.getCssValue();
        setHeaderCellMargin(edges);
        redraw = true;
      } else if (cmd.startsWith("hf")) {
        font = Font.parse(args);
        msg = "setColumnHeaderFont " + font.transform();
        for (int c = 0; c < cc; c++) {
          setColumnHeaderFont(getColumnId(c), font);
        }
        redraw = true;

      } else if (cmd.startsWith("fh")) {
        msg = "setFooterCellHeight " + xp[0];
        setFooterCellHeight(xp[0]);
        redraw = true;
      } else if (cmd.startsWith("fp")) {
        msg = "setFooterCellPadding " + edges.getCssValue();
        setFooterCellPadding(edges);
        redraw = true;
      } else if (cmd.startsWith("fw")) {
        msg = "setFooterBorderWidth " + edges.getCssValue();
        setFooterBorderWidth(edges);
        redraw = true;
      } else if (cmd.startsWith("fm")) {
        msg = "setFooterCellMargin " + edges.getCssValue();
        setFooterCellMargin(edges);
        redraw = true;
      } else if (cmd.startsWith("ff")) {
        font = Font.parse(args);
        msg = "setColumnFooterFont " + font.transform();
        for (int c = 0; c < cc; c++) {
          setColumnFooterFont(getColumnId(c), font);
        }
        redraw = true;

      } else if (cmd.startsWith("chw") && len > 2) {
        msg = "setColumnHeaderWidth " + colId + " " + xp[1];
        setColumnHeaderWidth(colId, xp[1]);
        redraw = true;
      } else if (cmd.startsWith("chf") && len > 2) {
        font = Font.parse(ArrayUtils.join(sp, " ", 1));
        msg = "setColumnHeaderFont " + colId + " " + font.transform();
        setColumnHeaderFont(colId, font);
        redraw = true;

      } else if (cmd.startsWith("cbw") && len > 2) {
        msg = "setColumnBodyWidth " + colId + " " + xp[1];
        setColumnBodyWidth(colId, xp[1]);
        redraw = true;
      } else if (cmd.startsWith("cbf") && len > 2) {
        font = Font.parse(ArrayUtils.join(sp, " ", 1));
        msg = "setColumnBodyFont " + colId + " " + font.transform();
        setColumnBodyFont(colId, font);
        redraw = true;

      } else if (cmd.startsWith("cfw") && len > 2) {
        msg = "setColumnFooterWidth " + colId + " " + xp[1];
        setColumnFooterWidth(colId, xp[1]);
        redraw = true;
      } else if (cmd.startsWith("cff") && len > 2) {
        font = Font.parse(ArrayUtils.join(sp, " ", 1));
        msg = "setColumnFooterFont " + colId + " " + font.transform();
        setColumnFooterFont(colId, font);
        redraw = true;

      } else if (cmd.startsWith("cw") && len > 2) {
        if (len <= 3) {
          msg = "setColumnWidth " + colId + " " + xp[1];
          setColumnWidth(colId, xp[1]);
          redraw = true;
        } else {
          msg = "setColumnWidth " + colId + " " + xp[1] + " " + StyleUtils.parseUnit(sp[2]);
          setColumnWidth(colId, xp[1], StyleUtils.parseUnit(sp[2]));
          redraw = true;
        }

      } else if (cmd.startsWith("minw")) {
        msg = "setMinCellWidth " + xp[0];
        setMinCellWidth(xp[0]);
      } else if (cmd.startsWith("maxw")) {
        msg = "setMaxCellWidth " + xp[0];
        setMaxCellWidth(xp[0]);
      } else if (cmd.startsWith("minh")) {
        msg = "setMinCellHeight " + xp[0];
        setMinCellHeight(xp[0]);
      } else if (cmd.startsWith("maxh")) {
        msg = "setMaxCellHeight " + xp[0];
        setMaxCellHeight(xp[0]);

      } else if (cmd.startsWith("zm")) {
        msg = "setResizerMoveSensitivityMillis " + xp[0];
        setResizerMoveSensitivityMillis(xp[0]);
      } else if (cmd.startsWith("zs")) {
        msg = "setResizerShowSensitivityMillis " + xp[0];
        setResizerShowSensitivityMillis(xp[0]);
        
      } else if (cmd.startsWith("fit")) {
        if (contains(colId)) {
          autoFitColumn(colId);
          msg = "autoFitColumn " + colId;
        } else {
          autoFit();
          msg = "autoFit";
        }
      
      } else if (cmd.startsWith("ps")) {
        if (xp[0] > 0) {
          updatePageSize(xp[0], false);
          msg = "updatePageSize " + xp[0];
        } else {
          int oldPageSize = getPageSize();
          int newPageSize = estimatePageSize();
          if (newPageSize > 0) {
            updatePageSize(newPageSize, false);
          }
          msg = "page size: old " + oldPageSize + " new " + newPageSize;
        }
      }

      if (msg == null) {
        BeeKeeper.getLog().warning("unrecognized command", opt[i]);
      } else {
        BeeKeeper.getLog().info(msg);
      }
    }

    if (redraw) {
      redraw();
    }
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

    setRowCount(rowCount);

    if (rowSet != null) {
      estimateColumnWidths(rowSet.getRows().getList(), Math.min(rowSet.getNumberOfRows(), 3));
    }
    estimateHeaderWidths();
  }
  
  @Override
  public int estimatePageSize(int containerWidth, int containerHeight) {
    return super.estimatePageSize(containerWidth, containerHeight);
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

    if (oldSize == pageSize) {
      if (init) {
        setVisibleRangeAndClearData(getVisibleRange(), true);
      }
    } else {
      setVisibleRange(getPageStart(), pageSize);
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

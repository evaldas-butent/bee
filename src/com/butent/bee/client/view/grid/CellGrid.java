package com.butent.bee.client.view.grid;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dom.Box;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Font;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.view.event.SortEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

public class CellGrid extends Widget implements HasId, HasDataTable {

  public interface Template extends SafeHtmlTemplates {
    @Template("<div data-row=\"{0}\" data-col=\"{1}\" class=\"{2}\" style=\"{3}position:absolute;\">{4}</div>")
    SafeHtml cell(String rowIdx, int colIdx, String classes, SafeStyles styles, SafeHtml contents);

    @Template("<div data-row=\"{0}\" data-col=\"{1}\" class=\"{2}\" style=\"{3}position:absolute;\" tabindex=\"{4}\">{5}</div>")
    SafeHtml cellFocusable(String rowIdx, int colIdx, String classes, SafeStyles styles,
        int tabIndex, SafeHtml contents);
  }

  private class ColumnInfo {
    private Font bodyFont = null;
    private int bodyWidth = BeeConst.UNDEF;
    private final Column<IsRow, ?> column;
    private final String columnId;

    private final Header<?> footer;
    private Font footerFont = null;
    private int footerWidth = BeeConst.UNDEF;
    private final Header<?> header;

    private Font headerFont = null;
    private int headerWidth = BeeConst.UNDEF;
    private int width = BeeConst.UNDEF;

    private ColumnInfo(String columnId, Column<IsRow, ?> column) {
      this(columnId, column, null, null, BeeConst.UNDEF);
    }

    private ColumnInfo(String columnId, Column<IsRow, ?> column, Header<?> header) {
      this(columnId, column, header, null, BeeConst.UNDEF);
    }

    private ColumnInfo(String columnId, Column<IsRow, ?> column,
        Header<?> header, Header<?> footer) {
      this(columnId, column, header, footer, BeeConst.UNDEF);
    }

    private ColumnInfo(String columnId, Column<IsRow, ?> column,
        Header<?> header, Header<?> footer, int width) {
      this.columnId = columnId;
      this.column = column;
      this.header = header;
      this.footer = footer;
      this.width = width;
    }

    private ColumnInfo(String columnId, Column<IsRow, ?> column, Header<?> header, int width) {
      this(columnId, column, header, null, width);
    }

    private ColumnInfo(String columnId, Column<IsRow, ?> column, int width) {
      this(columnId, column, null, null, width);
    }

    private void ensureBodyWidth(int w) {
      if (w > 0) {
        setBodyWidth(Math.max(getBodyWidth(), w));
      }
    }

    private void ensureFooterWidth(int w) {
      if (w > 0) {
        setFooterWidth(Math.max(getFooterWidth(), w));
      }
    }

    private void ensureHeaderWidth(int w) {
      if (w > 0) {
        setHeaderWidth(Math.max(getHeaderWidth(), w));
      }
    }

    private Font getBodyFont() {
      return bodyFont;
    }

    private int getBodyWidth() {
      return bodyWidth;
    }

    private Column<IsRow, ?> getColumn() {
      return column;
    }

    private String getColumnId() {
      return columnId;
    }

    private int getColumnWidth() {
      if (getWidth() > 0) {
        return getWidth();
      } else if (getBodyWidth() > 0) {
        return getBodyWidth();
      } else if (getHeaderWidth() > 0) {
        return getHeaderWidth();
      } else {
        return getFooterWidth();
      }
    }

    private Header<?> getFooter() {
      return footer;
    }

    private Font getFooterFont() {
      return footerFont;
    }

    private int getFooterWidth() {
      return footerWidth;
    }

    private Header<?> getHeader() {
      return header;
    }

    private Font getHeaderFont() {
      return headerFont;
    }

    private int getHeaderWidth() {
      return headerWidth;
    }

    private int getWidth() {
      return width;
    }

    private boolean is(String id) {
      return BeeUtils.same(getColumnId(), id);
    }

    private void setBodyFont(Font bodyFont) {
      this.bodyFont = bodyFont;
    }

    private void setBodyWidth(int bodyWidth) {
      this.bodyWidth = bodyWidth;
    }

    private void setFooterFont(Font footerFont) {
      this.footerFont = footerFont;
    }

    private void setFooterWidth(int footerWidth) {
      this.footerWidth = footerWidth;
    }

    private void setHeaderFont(Font headerFont) {
      this.headerFont = headerFont;
    }

    private void setHeaderWidth(int headerWidth) {
      this.headerWidth = headerWidth;
    }

    private void setWidth(int width) {
      this.width = width;
    }
  }

  public static int defaultBodyCellHeight = BeeConst.UNDEF;
  public static Edges defaultBodyCellPadding = new Edges(2, 3);
  public static Edges defaultBodyBorderWidth = new Edges(1);
  public static Edges defaultBodyCellMargin = null;

  public static int defaultFooterCellHeight = BeeConst.UNDEF;
  public static Edges defaultFooterCellPadding = new Edges(1, 2, 0);
  public static Edges defaultFooterBorderWidth = new Edges(1);
  public static Edges defaultFooterCellMargin = null;

  public static int defaultHeaderCellHeight = BeeConst.UNDEF;
  public static Edges defaultHeaderCellPadding = null;
  public static Edges defaultHeaderBorderWidth = new Edges(1);
  public static Edges defaultHeaderCellMargin = null;

  public static int defaultMaxCellHeight = 256;

  public static int defaultMaxCellWidth = 1024;
  public static int defaultMinCellHeight = 8;
  public static int defaultMinCellWidth = 16;

  public static String STYLE_GRID = "bee-CellGrid";

  public static String STYLE_CELL = "bee-CellGridCell";
  public static String STYLE_HEADER = "bee-CellGridHeader";
  public static String STYLE_BODY = "bee-CellGridBody";
  public static String STYLE_FOOTER = "bee-CellGridFooter";

  public static String STYLE_EVEN_ROW = "bee-CellGridEvenRow";
  public static String STYLE_ODD_ROW = "bee-CellGridOddRow";
  public static String STYLE_SELECTED_ROW = "bee-CellGridSelectedRow";
  public static String STYLE_ACTIVE_ROW = "bee-CellGridActiveRow";
  public static String STYLE_ACTIVE_CELL = "bee-CellGridActiveCell";

  private static final String HEADER_ROW = "header";
  private static final String FOOTER_ROW = "footer";

  private static Template template = null;

  private final List<ColumnInfo> columns = Lists.newArrayList();

  private int bodyCellHeight = defaultBodyCellHeight;
  private Edges bodyCellPadding = defaultBodyCellPadding;
  private Edges bodyBorderWidth = defaultBodyBorderWidth;
  private Edges bodyCellMargin = defaultBodyCellMargin;

  private int footerCellHeight = defaultFooterCellHeight;
  private Edges footerBorderWidth = defaultFooterBorderWidth;
  private Edges footerCellPadding = defaultFooterCellPadding;
  private Edges footerCellMargin = defaultFooterCellMargin;

  private int headerCellHeight = defaultHeaderCellHeight;
  private Edges headerBorderWidth = defaultHeaderBorderWidth;
  private Edges headerCellPadding = defaultHeaderCellPadding;
  private Edges headerCellMargin = defaultHeaderCellMargin;

  private boolean cellIsEditing = false;

  private int activeRow = BeeConst.UNDEF;
  private int activeColumn = BeeConst.UNDEF;

  private int minCellHeight = defaultMinCellHeight;
  private int maxCellHeight = defaultMaxCellHeight;
  private int minCellWidth = defaultMinCellWidth;
  private int maxCellWidth = defaultMaxCellWidth;

  private int pageSize = BeeConst.UNDEF;
  private int pageStart = 0;

  private int rowCount = BeeConst.UNDEF;
  private boolean rowCountIsExact = true;

  private List<IsRow> rowData = Lists.newArrayList();

  private RowStyles<IsRow> rowStyles = null;

  private SelectionModel<? super IsRow> selectionModel;
  private final Set<Long> selectedRows = Sets.newHashSet();

  private final Order sortOrder = new Order();

  private int tabIndex = 0;
  private int zIndex = 0;

  private boolean hasCellPreview = false;
  
  public CellGrid() {
    setElement(Document.get().createDivElement());

    Set<String> eventTypes = Sets.newHashSet(EventUtils.EVENT_TYPE_KEY_DOWN,
        EventUtils.EVENT_TYPE_KEY_PRESS, EventUtils.EVENT_TYPE_CLICK);
    sinkEvents(eventTypes);

    if (template == null) {
      template = GWT.create(Template.class);
    }

    setStyleName(STYLE_GRID);
    createId();
  }

  public void activateRow(int index) {
    int rc = getRowCount();
    if (rc <= 0) {
      return;
    }
    if (rc <= 1) {
      setActiveRow(0);
      return;
    }

    int absIndex = BeeUtils.limit(index, 0, rc - 1);
    int oldPageStart = getPageStart();
    if (oldPageStart + getActiveRow() == absIndex) {
      return;
    }

    int size = getPageSize();
    if (size <= 0 || size >= rc) {
      setActiveRow(absIndex);
      return;
    }
    if (size == 1) {
      setActiveRow(0);
      setPageStart(absIndex);
      return;
    }

    int newPageStart = Math.min((absIndex / size) * size, rc - size);
    setActiveRow(absIndex - newPageStart);

    if (newPageStart != oldPageStart) {
      setPageStart(newPageStart);
    }
  }

  public HandlerRegistration addCellPreviewHandler(CellPreviewEvent.Handler<IsRow> handler) {
    hasCellPreview = true;
    return addHandler(handler, CellPreviewEvent.getType());
  }

  public void addColumn(String columnId, Column<IsRow, ?> col) {
    insertColumn(getColumnCount(), columnId, col);
  }

  public void addColumn(String columnId, Column<IsRow, ?> col, Header<?> header) {
    insertColumn(getColumnCount(), columnId, col, header);
  }

  public void addColumn(String columnId, Column<IsRow, ?> col, Header<?> header, Header<?> footer) {
    insertColumn(getColumnCount(), columnId, col, header, footer);
  }

  public void addColumn(String columnId, Column<IsRow, ?> col, SafeHtml headerHtml) {
    insertColumn(getColumnCount(), columnId, col, headerHtml);
  }

  public void addColumn(String columnId, Column<IsRow, ?> col,
      SafeHtml headerHtml, SafeHtml footerHtml) {
    insertColumn(getColumnCount(), columnId, col, headerHtml, footerHtml);
  }

  public void addColumn(String columnId, Column<IsRow, ?> col, String headerString) {
    insertColumn(getColumnCount(), columnId, col, headerString);
  }

  public void addColumn(String columnId, Column<IsRow, ?> col,
      String headerString, String footerString) {
    insertColumn(getColumnCount(), columnId, col, headerString, footerString);
  }

  public HandlerRegistration addRangeChangeHandler(RangeChangeEvent.Handler handler) {
    return addHandler(handler, RangeChangeEvent.getType());
  }

  public HandlerRegistration addRowCountChangeHandler(RowCountChangeEvent.Handler handler) {
    return addHandler(handler, RowCountChangeEvent.getType());
  }

  public HandlerRegistration addSortHandler(SortEvent.Handler handler) {
    return addHandler(handler, SortEvent.getType());
  }

  public void apply(String options, boolean refresh) {
    if (BeeUtils.isEmpty(options)) {
      return;
    }

    String[] opt = BeeUtils.split(options, ";");
    for (int i = 0; i < opt.length; i++) {
      String[] arr = BeeUtils.split(opt[i], " ");
      int len = arr.length;
      if (len <= 1) {
        continue;
      }
      String cmd = arr[0].trim().toLowerCase();

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

      String msg = null;

      if (cmd.contains("bh")) {
        msg = "setBodyCellHeight " + xp[0];
        setBodyCellHeight(xp[0]);
      } else if (cmd.contains("bp")) {
        msg = "setBodyCellPadding " + edges.getCssValue();
        setBodyCellPadding(edges);
      } else if (cmd.contains("bw")) {
        msg = "setBodyBorderWidth " + edges.getCssValue();
        setBodyBorderWidth(edges);
      } else if (cmd.contains("bm")) {
        msg = "setBodyCellMargin " + edges.getCssValue();
        setBodyCellMargin(edges);

      } else if (cmd.contains("hh")) {
        msg = "setHeaderCellHeight " + xp[0];
        setHeaderCellHeight(xp[0]);
      } else if (cmd.contains("hp")) {
        msg = "setHeaderCellPadding " + edges.getCssValue();
        setHeaderCellPadding(edges);
      } else if (cmd.contains("hw")) {
        msg = "setHeaderBorderWidth " + edges.getCssValue();
        setHeaderBorderWidth(edges);
      } else if (cmd.contains("hm")) {
        msg = "setHeaderCellMargin " + edges.getCssValue();
        setHeaderCellMargin(edges);

      } else if (cmd.contains("fh")) {
        msg = "setFooterCellHeight " + xp[0];
        setFooterCellHeight(xp[0]);
      } else if (cmd.contains("fp")) {
        msg = "setFooterCellPadding " + edges.getCssValue();
        setFooterCellPadding(edges);
      } else if (cmd.contains("fw")) {
        msg = "setFooterBorderWidth " + edges.getCssValue();
        setFooterBorderWidth(edges);
      } else if (cmd.contains("fm")) {
        msg = "setFooterCellMargin " + edges.getCssValue();
        setFooterCellMargin(edges);

      } else if (cmd.contains("chw") && len > 2) {
        msg = "setColumnHeaderWidth " + colId + " " + xp[1];
        setColumnHeaderWidth(colId, xp[1]);
      } else if (cmd.contains("cbw") && len > 2) {
        msg = "setColumnBodyWidth " + colId + " " + xp[1];
        setColumnBodyWidth(colId, xp[1]);
      } else if (cmd.contains("cfw") && len > 2) {
        msg = "setColumnFooterWidth " + colId + " " + xp[1];
        setColumnFooterWidth(colId, xp[1]);

      } else if (cmd.contains("cw") && len > 2) {
        if (len <= 3) {
          msg = "setColumnWidth " + colId + " " + xp[1];
          setColumnWidth(colId, xp[1]);
        } else {
          msg = "setColumnWidth " + colId + " " + xp[1] + " " + StyleUtils.parseUnit(sp[2]);
          setColumnWidth(colId, xp[1], StyleUtils.parseUnit(sp[2]));
        }

      } else if (cmd.contains("minw")) {
        msg = "setMinCellWidth " + xp[0];
        setMinCellWidth(xp[0]);
      } else if (cmd.contains("maxw")) {
        msg = "setMaxCellWidth " + xp[0];
        setMaxCellWidth(xp[0]);
      } else if (cmd.contains("minh")) {
        msg = "setMinCellHeight " + xp[0];
        setMinCellHeight(xp[0]);
      } else if (cmd.contains("maxh")) {
        msg = "setMaxCellHeight " + xp[0];
        setMaxCellHeight(xp[0]);
      }

      if (msg == null) {
        BeeKeeper.getLog().warning("unrecognized command", opt[i]);
      } else {
        BeeKeeper.getLog().info(msg);
      }
    }

    if (refresh) {
      redraw();
    }
  }

  public void clearColumnWidth(String columnId) {
    setColumnWidth(columnId, BeeConst.UNDEF);
  }

  public boolean contains(String columnId) {
    if (BeeUtils.isEmpty(columnId)) {
      return false;
    }

    for (ColumnInfo info : columns) {
      if (info.is(columnId)) {
        return true;
      }
    }
    return false;
  }

  public void createId() {
    DomUtils.createId(this, "cell-grid");
  }

  public <T extends IsRow> void estimateColumnWidths(List<T> values) {
    Assert.notNull(values);
    estimateColumnWidths(values, values.size());
  }

  public <T extends IsRow> void estimateColumnWidths(List<T> values, int length) {
    Assert.notNull(values);

    for (int i = 0; i < length; i++) {
      IsRow value = values.get(i);
      if (value == null) {
        continue;
      }

      int col = 0;
      for (ColumnInfo columnInfo : columns) {
        Column<IsRow, ?> column = columnInfo.getColumn();

        SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
        CellContext context = new CellContext(i, col, getRowId(value), this);
        column.render(context, value, cellBuilder);
        SafeHtml cellHtml = cellBuilder.toSafeHtml();

        int cellWidth = Rulers.getLineWidth(cellHtml.asString(), columnInfo.getBodyFont());
        if (cellWidth > 0) {
          columnInfo.ensureBodyWidth(cellWidth);
        }
        col++;
      }
    }
  }

  public void estimateFooterWidths() {
    for (int i = 0; i < getColumnCount(); i++) {
      ColumnInfo columnInfo = columns.get(i);
      Header<?> footer = columnInfo.getFooter();

      SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
      CellContext context = new CellContext(0, i, footer.getKey(), this);
      footer.render(context, cellBuilder);
      SafeHtml cellHtml = cellBuilder.toSafeHtml();

      int cellWidth = Rulers.getLineWidth(cellHtml.asString(), columnInfo.getFooterFont());
      if (cellWidth > 0) {
        columnInfo.ensureFooterWidth(cellWidth);
      }
    }
  }

  public void estimateHeaderWidths() {
    for (int i = 0; i < getColumnCount(); i++) {
      ColumnInfo columnInfo = columns.get(i);
      Header<?> header = columnInfo.getHeader();

      SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
      CellContext context = new CellContext(0, i, header.getKey(), this);
      header.render(context, cellBuilder);
      SafeHtml cellHtml = cellBuilder.toSafeHtml();

      int cellWidth = Rulers.getLineWidth(cellHtml.asString(), columnInfo.getHeaderFont());
      if (cellWidth > 0) {
        columnInfo.ensureHeaderWidth(cellWidth);
      }
    }
  }

  public int getActiveColumn() {
    return activeColumn;
  }

  public int getActiveRow() {
    return activeRow;
  }

  public Edges getBodyBorderWidth() {
    return bodyBorderWidth;
  }

  public int getBodyCellHeight() {
    return bodyCellHeight;
  }

  public int getBodyCellHeightIncrement() {
    return getHeightIncrement(getBodyCellPadding(), getBodyBorderWidth(), getBodyCellMargin());
  }

  public Edges getBodyCellMargin() {
    return bodyCellMargin;
  }

  public Edges getBodyCellPadding() {
    return bodyCellPadding;
  }

  public int getBodyCellWidthIncrement() {
    return getWidthIncrement(getBodyCellPadding(), getBodyBorderWidth(), getBodyCellMargin());
  }

  public int getBodyHeight() {
    return getVisibleItemCount() * (getBodyCellHeight() + getBodyCellHeightIncrement());
  }

  public int getBodyWidth() {
    int width = 0;
    int incr = getBodyCellWidthIncrement();

    for (ColumnInfo columnInfo : columns) {
      int w = columnInfo.getColumnWidth();
      if (w <= 0) {
        width = BeeConst.UNDEF;
        break;
      }
      width += w + incr;
    }
    return width;
  }

  public Column<IsRow, ?> getColumn(int col) {
    return getColumnInfo(col).getColumn();
  }

  public int getColumnCount() {
    return columns.size();
  }

  public String getColumnId(int col) {
    return getColumnInfo(col).getColumnId();
  }

  public int getColumnIndex(String columnId) {
    Assert.notEmpty(columnId);
    for (int i = 0; i < getColumnCount(); i++) {
      if (columns.get(i).is(columnId)) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  public int getColumnWidth(int col) {
    return getColumnInfo(col).getColumnWidth();
  }

  public int getColumnWidth(String columnId) {
    ColumnInfo info = getColumnInfo(columnId);
    if (info == null) {
      return BeeConst.UNDEF;
    }
    return info.getColumnWidth();
  }

  public int getFootCellWidthIncrement() {
    return getWidthIncrement(getFooterCellPadding(), getFooterBorderWidth(), getFooterCellMargin());
  }

  public Edges getFooterBorderWidth() {
    return footerBorderWidth;
  }

  public int getFooterCellHeight() {
    return footerCellHeight;
  }

  public int getFooterCellHeightIncrement() {
    return getHeightIncrement(getFooterCellPadding(), getFooterBorderWidth(), getFooterCellMargin());
  }

  public Edges getFooterCellMargin() {
    return footerCellMargin;
  }

  public Edges getFooterCellPadding() {
    return footerCellPadding;
  }

  public int getFooterHeight() {
    if (hasFooters()) {
      return getFooterCellHeight() + getFooterCellHeightIncrement();
    } else {
      return 0;
    }
  }

  public List<Header<?>> getFooters() {
    List<Header<?>> lst = Lists.newArrayList();
    for (ColumnInfo info : columns) {
      if (info.getFooter() != null) {
        lst.add(info.getFooter());
      }
    }
    return lst;
  }

  public Edges getHeaderBorderWidth() {
    return headerBorderWidth;
  }

  public int getHeaderCellHeight() {
    return headerCellHeight;
  }

  public int getHeaderCellHeightIncrement() {
    return getHeightIncrement(getHeaderCellPadding(), getHeaderBorderWidth(), getHeaderCellMargin());
  }

  public Edges getHeaderCellMargin() {
    return headerCellMargin;
  }

  public Edges getHeaderCellPadding() {
    return headerCellPadding;
  }

  public int getHeaderCellWidthIncrement() {
    return getWidthIncrement(getHeaderCellPadding(), getHeaderBorderWidth(), getHeaderCellMargin());
  }

  public int getHeaderHeight() {
    if (hasHeaders()) {
      return getHeaderCellHeight() + getHeaderCellHeightIncrement();
    } else {
      return 0;
    }
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public int getMaxCellHeight() {
    return maxCellHeight;
  }

  public int getMaxCellWidth() {
    return maxCellWidth;
  }

  public int getMinCellHeight() {
    return minCellHeight;
  }

  public int getMinCellWidth() {
    return minCellWidth;
  }

  public int getPageSize() {
    return pageSize;
  }

  public int getPageStart() {
    return pageStart;
  }

  public int getRowCount() {
    return rowCount;
  }

  public SelectionModel<? super IsRow> getSelectionModel() {
    return selectionModel;
  }

  public int getSortIndex(String columnId) {
    return getSortOrder().getIndex(columnId);
  }

  public Order getSortOrder() {
    return sortOrder;
  }

  public int getTabIndex() {
    return tabIndex;
  }

  public IsRow getVisibleItem(int indexOnPage) {
    checkRowBounds(indexOnPage);
    return rowData.get(indexOnPage);
  }

  public int getVisibleItemCount() {
    return rowData.size();
  }

  public List<IsRow> getVisibleItems() {
    return rowData;
  }

  public Range getVisibleRange() {
    return new Range(getPageStart(), getPageSize());
  }

  public int getZIndex() {
    return zIndex;
  }

  public boolean hasFooters() {
    for (ColumnInfo info : columns) {
      if (info.getFooter() != null) {
        return true;
      }
    }
    return false;
  }

  public boolean hasHeaders() {
    for (ColumnInfo info : columns) {
      if (info.getHeader() != null) {
        return true;
      }
    }
    return false;
  }

  public boolean hasKeyboardNext() {
    return getActiveRow() + getPageStart() < getRowCount() - 1;
  }

  public boolean hasKeyboardPrev() {
    return getActiveRow() > 0 || getPageStart() > 0;
  }

  public void insertColumn(int beforeIndex, String columnId, Column<IsRow, ?> column) {
    insertColumn(beforeIndex, columnId, column, (Header<?>) null, (Header<?>) null);
  }

  public void insertColumn(int beforeIndex, String columnId, Column<IsRow, ?> column,
      Header<?> header) {
    insertColumn(beforeIndex, columnId, column, header, null);
  }

  public void insertColumn(int beforeIndex, String columnId, Column<IsRow, ?> column,
      Header<?> header, Header<?> footer) {
    if (beforeIndex != getColumnCount()) {
      checkColumnBounds(beforeIndex);
    }
    checkColumnId(columnId);

    columns.add(beforeIndex, new ColumnInfo(columnId, column, header, footer));

    Set<String> consumedEvents = Sets.newHashSet();
    {
      Set<String> cellEvents = column.getCell().getConsumedEvents();
      if (cellEvents != null) {
        consumedEvents.addAll(cellEvents);
      }
    }
    if (header != null) {
      Set<String> headerEvents = header.getCell().getConsumedEvents();
      if (headerEvents != null) {
        consumedEvents.addAll(headerEvents);
      }
    }
    if (footer != null) {
      Set<String> footerEvents = footer.getCell().getConsumedEvents();
      if (footerEvents != null) {
        consumedEvents.addAll(footerEvents);
      }
    }
    if (!consumedEvents.isEmpty()) {
      sinkEvents(consumedEvents);
    }
  }

  public void insertColumn(int beforeIndex, String columnId, Column<IsRow, ?> column,
      SafeHtml headerHtml) {
    insertColumn(beforeIndex, columnId, column, new SafeHtmlHeader(headerHtml), null);
  }

  public void insertColumn(int beforeIndex, String columnId, Column<IsRow, ?> column,
      SafeHtml headerHtml, SafeHtml footerHtml) {
    insertColumn(beforeIndex, columnId, column,
        new SafeHtmlHeader(headerHtml), new SafeHtmlHeader(footerHtml));
  }

  public void insertColumn(int beforeIndex, String columnId, Column<IsRow, ?> column,
      String headerString) {
    insertColumn(beforeIndex, columnId, column, new TextHeader(headerString), null);
  }

  public void insertColumn(int beforeIndex, String columnId, Column<IsRow, ?> column,
      String headerString, String footerString) {
    insertColumn(beforeIndex, columnId, column,
        new TextHeader(headerString), new TextHeader(footerString));
  }

  public boolean isCellEditing() {
    return cellIsEditing;
  }

  public boolean isRowCountExact() {
    return rowCountIsExact;
  }

  public boolean isSortable(String columnId) {
    ColumnInfo info = getColumnInfo(columnId);
    if (info == null) {
      return false;
    }
    return info.getColumn().isSortable();
  }

  public boolean isSortAscending(String columnId) {
    return getSortOrder().isAscending(columnId);
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);

    EventTarget eventTarget = event.getEventTarget();
    if (!Element.is(eventTarget)) {
      return;
    }
    Element cell = Element.as(eventTarget);
    if (!getElement().isOrHasChild(cell)) {
      return;
    }

    String eventType = event.getType();

    String rowIdx = null;
    while (cell != null && cell != getElement()) {
      rowIdx = DomUtils.getDataRow(cell);
      if (!BeeUtils.isEmpty(rowIdx)) {
        break;
      }
      cell = cell.getParentElement();
    }
    if (BeeUtils.isEmpty(rowIdx)) {
      return;
    }
    int col = BeeUtils.toInt(DomUtils.getDataColumn(cell));

    if (BeeUtils.same(rowIdx, HEADER_ROW)) {
      Header<?> header = columns.get(col).getHeader();
      if (header != null && cellConsumesEventType(header.getCell(), eventType)) {
        CellContext context = new CellContext(0, col, header.getKey(), this);
        header.onBrowserEvent(context, cell, event);
      }
    } else if (BeeUtils.same(rowIdx, FOOTER_ROW)) {
      Header<?> footer = columns.get(col).getFooter();
      if (footer != null && cellConsumesEventType(footer.getCell(), eventType)) {
        CellContext context = new CellContext(0, col, footer.getKey(), this);
        footer.onBrowserEvent(context, cell, event);
      }

    } else if (BeeUtils.isDigit(rowIdx)) {
      int row = BeeUtils.toInt(rowIdx);
      if (!isRowWithinBounds(row)) {
        return;
      }
      IsRow value = getVisibleItem(row);

      if (!isNavigationSuppressed(event)) {
        if (EventUtils.isClick(eventType)) {
          if (EventUtils.hasModifierKey(event)) {
            selectRow(row, value);
            return;
          }
          if (getActiveRow() != row || getActiveColumn() != col) {
            activateCell(row, col);
            return;
          }

        } else if (EventUtils.isKeyDown(eventType)) {
          if (handleKey(event.getKeyCode())) {
            EventUtils.eatEvent(event);
            return;
          }
        } else if (EventUtils.isKeyPress(eventType)) {
          if (handleChar(event.getCharCode(), row, col, value, cell)) {
            EventUtils.eatEvent(event);
            return;
          }
        }
      }

      Column<IsRow, ?> column = columns.get(col).getColumn();
      CellContext context = new CellContext(row, col, getRowId(value), this);

      if (hasCellPreview) {
        CellPreviewEvent<IsRow> previewEvent = CellPreviewEvent.fire(this, event, this, context,
            value, cellIsEditing, column.getCell().handlesSelection());
        if (previewEvent.isCanceled()) {
          return;
        }
      }
      fireEventToCell(event, eventType, cell, value, context, column);
    }
  }

  public void redraw() {
    SafeHtmlBuilder sb = new SafeHtmlBuilder();
    renderRowValues(sb, rowData);
    replaceAllChildren(sb.toSafeHtml());

    this.zIndex = 0;

    if (getActiveRow() >= 0 && getActiveColumn() >= 0) {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        public void execute() {
          Element cellElement = getActiveCellElement();
          if (cellElement != null) {
            cellElement.getStyle().setZIndex(++zIndex);
            cellElement.focus();
          }
        }
      });
    }
  }

  public void removeColumn(int index) {
    Assert.isIndex(columns, index);
    columns.remove(index);
  }

  public int resizeColumnWidth(int col, int incr) {
    if (incr == 0) {
      return BeeConst.UNDEF;
    }
    int oldWidth = getColumnWidth(col);
    if (oldWidth <= 0) {
      return BeeConst.UNDEF;
    }

    int newWidth = BeeUtils.limit(oldWidth + incr, getMinCellWidth(), getMaxCellWidth());
    if (newWidth <= 0 || !BeeUtils.sameSign(newWidth - oldWidth, incr)) {
      return BeeConst.UNDEF;
    }

    setColumnWidth(col, newWidth);

    NodeList<Element> nodes = getColumnElements(col);
    for (int i = 0; i < nodes.getLength(); i++) {
      Element el = nodes.getItem(i);
      int width = StyleUtils.getWidth(el);
      if (width > 0) {
        StyleUtils.setWidth(el, width + newWidth - oldWidth);
      }
    }

    if (col < getColumnCount() - 1) {
      for (int i = col + 1; i < getColumnCount(); i++) {
        nodes = getColumnElements(i);
        if (nodes == null || nodes.getLength() <= 0) {
          continue;
        }
        int left = StyleUtils.getLeft(nodes.getItem(0));
        if (left > 0) {
          StyleUtils.setStylePropertyPx(nodes, StyleUtils.STYLE_LEFT, left + newWidth - oldWidth);
        }
      }
    }
    return newWidth;
  }

  public void setActiveColumn(int activeColumn) {
    if (this.activeColumn == activeColumn) {
      return;
    }
    onActivateCell(false);

    this.activeColumn = activeColumn;

    onActivateCell(true);
  }

  public void setActiveRow(int activeRow) {
    if (this.activeRow == activeRow) {
      return;
    }
    onActivateCell(false);
    onActivateRow(false);

    this.activeRow = activeRow;

    onActivateRow(true);
    onActivateCell(true);
  }

  public void setBodyBorderWidth(Edges bodyBorderWidth) {
    this.bodyBorderWidth = bodyBorderWidth;
  }

  public void setBodyCellHeight(int bodyCellHeight) {
    this.bodyCellHeight = bodyCellHeight;
  }

  public void setBodyCellMargin(Edges bodyCellMargin) {
    this.bodyCellMargin = bodyCellMargin;
  }

  public void setBodyCellPadding(Edges bodyCellPadding) {
    this.bodyCellPadding = bodyCellPadding;
  }

  public void setColumnBodyFont(String columnId, Font font) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setBodyFont(font);
  }

  public void setColumnBodyWidth(String columnId, int width) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setBodyWidth(width);
  }

  public void setColumnFooterFont(String columnId, Font font) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setFooterFont(font);
  }

  public void setColumnFooterWidth(String columnId, int width) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setFooterWidth(width);
  }

  public void setColumnHeaderFont(String columnId, Font font) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setHeaderFont(font);
  }

  public void setColumnHeaderWidth(String columnId, int width) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setHeaderWidth(width);
  }

  public void setColumnWidth(int col, int width) {
    getColumnInfo(col).setWidth(width);
  }

  public void setColumnWidth(String columnId, double width, Unit unit) {
    int containerSize = getOffsetWidth();
    Assert.isPositive(containerSize);
    setColumnWidth(columnId, width, unit, containerSize);
  }

  public void setColumnWidth(String columnId, double width, Unit unit, int containerSize) {
    setColumnWidth(columnId, Rulers.getIntPixels(width, unit, containerSize));
  }

  public void setColumnWidth(String columnId, int width) {
    ColumnInfo info = getColumnInfo(columnId);
    Assert.notNull(info);

    info.setWidth(width);
  }

  public void setFooterBorderWidth(Edges footerBorderWidth) {
    this.footerBorderWidth = footerBorderWidth;
  }

  public void setFooterCellHeight(int footerCellHeight) {
    this.footerCellHeight = footerCellHeight;
  }

  public void setFooterCellMargin(Edges footerCellMargin) {
    this.footerCellMargin = footerCellMargin;
  }

  public void setFooterCellPadding(Edges footerCellPadding) {
    this.footerCellPadding = footerCellPadding;
  }

  public void setHeaderBorderWidth(Edges headerBorderWidth) {
    this.headerBorderWidth = headerBorderWidth;
  }

  public void setHeaderCellHeight(int headerCellHeight) {
    this.headerCellHeight = headerCellHeight;
  }

  public void setHeaderCellMargin(Edges headerCellMargin) {
    this.headerCellMargin = headerCellMargin;
  }

  public void setHeaderCellPadding(Edges headerCellPadding) {
    this.headerCellPadding = headerCellPadding;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setMaxCellHeight(int maxCellHeight) {
    this.maxCellHeight = maxCellHeight;
  }

  public void setMaxCellWidth(int maxCellWidth) {
    this.maxCellWidth = maxCellWidth;
  }

  public void setMinCellHeight(int minCellHeight) {
    this.minCellHeight = minCellHeight;
  }

  public void setMinCellWidth(int minCellWidth) {
    this.minCellWidth = minCellWidth;
  }

  public void setPageSize(int pageSize) {
    setVisibleRange(getPageStart(), pageSize);
  }

  public void setPageStart(int pageStart) {
    setVisibleRange(pageStart, getPageSize());
  }

  public void setRowCount(int count) {
    setRowCount(count, true);
  }

  public void setRowCount(int size, boolean isExact) {
    if (size == getRowCount() && isExact == isRowCountExact()) {
      return;
    }
    rowCount = size;
    rowCountIsExact = isExact;

    RowCountChangeEvent.fire(this, size, isExact);
  }

  public void setRowData(int start, List<? extends IsRow> values) {
    Assert.nonNegative(start);
    Assert.notNull(values);

    int size = values.size();
    Assert.isPositive(size);
    Assert.isTrue(size == getPageSize(), "setRowData: data size " + size
        + " does not match page size " + getPageSize());

    if (rowData.size() == size) {
      for (int i = 0; i < size; i++) {
        rowData.set(i, values.get(i));
      }
    } else {
      rowData.clear();
      for (int i = 0; i < size; i++) {
        rowData.add(values.get(i));
      }
    }
    redraw();
  }

  public void setRowData(List<? extends IsRow> values) {
    setRowCount(values.size());
    setVisibleRange(0, values.size());
    setRowData(0, values);
  }

  public void setRowStyles(RowStyles<IsRow> rowStyles) {
    this.rowStyles = rowStyles;
  }

  public void setSelectionModel(SelectionModel<? super IsRow> selectionModel) {
    this.selectionModel = selectionModel;
  }

  public void setTabIndex(int index) {
    this.tabIndex = index;
  }

  public void setVisibleRange(int start, int length) {
    setVisibleRange(new Range(start, length));
  }

  public void setVisibleRange(Range range) {
    setVisibleRange(range, false, false);
  }

  public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
    setVisibleRange(range, true, forceRangeChangeEvent);
  }

  public void updateOrder(int col, NativeEvent event) {
    checkColumnBounds(col);
    if (getColumn(col).isSortable() && getRowCount() > 1) {
      updateOrder(getColumnId(col), EventUtils.hasModifierKey(event));
      SortEvent.fire(this, getSortOrder());
    }
  }

  private void activateCell(int row, int col) {
    if (getActiveRow() == row) {
      setActiveColumn(col);
      return;
    }
    onActivateCell(false);
    onActivateRow(false);

    this.activeRow = row;
    this.activeColumn = col;

    onActivateRow(true);
    onActivateCell(true);
  }

  private boolean cellConsumesEventType(Cell<?> cell, String eventType) {
    Set<String> consumedEvents = cell.getConsumedEvents();
    return consumedEvents != null && consumedEvents.contains(eventType);
  }

  private void checkColumnBounds(int col) {
    Assert.betweenExclusive(col, 0, getColumnCount());
  }

  private void checkColumnId(String columnId) {
    Assert.notNull(columnId);
    Assert.isFalse(contains(columnId), "Duplicate Column Id " + columnId);
  }

  private void checkRowBounds(int row) {
    Assert.isTrue(isRowWithinBounds(row));
  }

  private <C> void fireEventToCell(Event event, String eventType,
      Element parentElem, IsRow value, CellContext context, Column<IsRow, C> column) {
    Cell<C> cell = column.getCell();
    if (cellConsumesEventType(cell, eventType)) {
      C cellValue = column.getValue(value);
      column.onBrowserEvent(context, parentElem, value, event);
      cellIsEditing = cell.isEditing(context, parentElem, cellValue);
    }
  }

  private Element getActiveCellElement() {
    return Selectors.getElement(Selectors.conjunction(
        Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, getActiveRow()),
        Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_COLUMN, getActiveColumn())));
  }

  private NodeList<Element> getActiveRowElements() {
    return getRowElements(getActiveRow());
  }

  private Box getCellBox(int row, int col) {
    if (!isRowWithinBounds(row) || !isColumnWithinBounds(col)) {
      return null;
    }
    
    int left = 0;
    if (col > 0) {
      int xIncr = getBodyCellWidthIncrement();
      for (int i = 0; i < col; i++) {
        left += getColumnWidth(i) + xIncr;
      }
    }
    int width = getColumnWidth(col);
    
    int top = getHeaderHeight();
    if (row > 0) {
      top += (getBodyCellHeight() + getBodyCellHeightIncrement()) * row; 
    }
    int height = getBodyCellHeight();

    return new Box(left, width, top, height);
  }

  private NodeList<Element> getColumnElements(int col) {
    return Selectors.getNodes(Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_COLUMN, col));
  }

  private ColumnInfo getColumnInfo(int col) {
    checkColumnBounds(col);
    return columns.get(col);
  }

  private ColumnInfo getColumnInfo(String columnId) {
    Assert.notEmpty(columnId);
    for (ColumnInfo info : columns) {
      if (info.is(columnId)) {
        return info;
      }
    }
    return null;
  }

  private String getCssValue(Edges edges) {
    if (edges == null) {
      return Edges.EMPTY_CSS_VALUE;
    } else {
      return edges.getCssValue();
    }
  }

  private int getHeightIncrement(Edges padding, Edges border, Edges margin) {
    int incr = 0;
    if (padding != null) {
      incr += BeeUtils.toNonNegativeInt(padding.getTopValue());
      incr += BeeUtils.toNonNegativeInt(padding.getBottomValue());
    }
    if (border != null) {
      incr += BeeUtils.toNonNegativeInt(border.getTopValue());
      incr += BeeUtils.toNonNegativeInt(border.getBottomValue());
    }
    if (margin != null) {
      incr += BeeUtils.toNonNegativeInt(margin.getTopValue());
      incr += BeeUtils.toNonNegativeInt(margin.getBottomValue());
    }
    return incr;
  }

  private NodeList<Element> getRowElements(int row) {
    return Selectors.getNodes(Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, row));
  }

  private Object getRowId(IsRow value) {
    return (value == null) ? value : value.getId();
  }
  
  private int getWidthIncrement(Edges padding, Edges border, Edges margin) {
    int incr = 0;
    if (padding != null) {
      incr += BeeUtils.toNonNegativeInt(padding.getLeftValue());
      incr += BeeUtils.toNonNegativeInt(padding.getRightValue());
    }
    if (border != null) {
      incr += BeeUtils.toNonNegativeInt(border.getLeftValue());
      incr += BeeUtils.toNonNegativeInt(border.getRightValue());
    }
    if (margin != null) {
      incr += BeeUtils.toNonNegativeInt(margin.getLeftValue());
      incr += BeeUtils.toNonNegativeInt(margin.getRightValue());
    }
    return incr;
  }

  private boolean handleChar(int charCode, int row, int col, IsRow rowValue, Element cell) {
    switch (charCode) {
      case BeeConst.CHAR_SPACE:
        selectRow(row, rowValue);
        return true;
      
      case BeeConst.CHAR_PLUS:
      case BeeConst.CHAR_MINUS:
      case BeeConst.CHAR_ZERO:
        if (cell == null) {
          return false;
        }
        int left = StyleUtils.getLeft(cell);
        int width = StyleUtils.getWidth(cell);
        int top = StyleUtils.getTop(cell);
        int height = StyleUtils.getHeight(cell);
        if (width <= 0 || height <= 0) {
          return false;
        }
        
        Box cellBox = null;
        if (charCode == BeeConst.CHAR_PLUS) {
          cellBox = new Box(--left, width += 2, --top, height += 2);
        } else if (charCode == BeeConst.CHAR_MINUS) {
          cellBox = new Box(++left, width -= 2, ++top, height -= 2);
        } else {
          cellBox = getCellBox(row, col);
        }
        
        if (cellBox == null 
            || width <= 0 || width < getMinCellWidth() || width > getMaxCellWidth()
            || height <= 0 || height < getMinCellHeight() || height > getMaxCellHeight()) {
          return false;
        }
        if (left < 0) {
          cellBox.setLeft(0);
        }
        if (top < 0) {
          cellBox.setTop(0);
        }
        cellBox.applyTo(cell);
       
        return true;

      default:
        return false;
    }
  }

  private boolean handleKey(int keyCode) {
    switch (keyCode) {
      case KeyCodes.KEY_DOWN:
        keyboardNext();
        return true;
      case KeyCodes.KEY_UP:
        keyboardPrev();
        return true;
      case KeyCodes.KEY_PAGEDOWN:
        keyboardNextPage();
        return true;
      case KeyCodes.KEY_PAGEUP:
        keyboardPrevPage();
        return true;
      case KeyCodes.KEY_HOME:
        keyboardHome();
        return true;
      case KeyCodes.KEY_END:
        keyboardEnd();
        return true;
      case KeyCodes.KEY_LEFT:
      case KeyCodes.KEY_BACKSPACE:
        keyboardLeft();
        return true;
      case KeyCodes.KEY_RIGHT:
      case KeyCodes.KEY_TAB:
        keyboardRight();
        return true;
      default:
        return false;
    }
  }

  private boolean isColumnWithinBounds(int col) {
    return col >= 0 && col < getColumnCount();
  }

  private boolean isNavigationSuppressed(Event event) {
    if (cellIsEditing) {
      return true;
    }
    if (event != null) {
      return EventUtils.isInputElement(event.getEventTarget());
    }
    return false;
  }

  private boolean isRowSelected(IsRow rowValue) {
    if (rowValue == null) {
      return false;
    } else {
      return isRowSelected(rowValue.getId());
    }
  }

  private boolean isRowSelected(long rowId) {
    return selectedRows.contains(rowId);
  }

  private boolean isRowWithinBounds(int row) {
    return row >= 0 && row < getVisibleItemCount();
  }

  private void keyboardEnd() {
    activateRow(getRowCount() - 1);
  }

  private void keyboardHome() {
    activateRow(0);
  }

  private void keyboardLeft() {
    int prevColumn = getActiveColumn() - 1;
    if (prevColumn < 0) {
      if (hasKeyboardPrev()) {
        setActiveColumn(getColumnCount() - 1);
        keyboardPrev();
      }
    } else {
      setActiveColumn(prevColumn);
    }
  }

  private void keyboardNext() {
    activateRow(getPageStart() + getActiveRow() + 1);
  }

  private void keyboardNextPage() {
    activateRow(getPageStart() + getPageSize());
  }

  private void keyboardPrev() {
    activateRow(getPageStart() + getActiveRow() - 1);
  }

  private void keyboardPrevPage() {
    activateRow(getPageStart() - 1);
  }
  
  private void keyboardRight() {
    int nextColumn = getActiveColumn() + 1;
    if (nextColumn >= getColumnCount()) {
      if (hasKeyboardNext()) {
        setActiveColumn(0);
        keyboardNext();
      }
    } else {
      setActiveColumn(nextColumn);
    }
  }

  private void onActivateCell(boolean activate) {
    if (getActiveRow() >= 0 && getActiveColumn() >= 0) {
      Element activeCell = getActiveCellElement();
      if (activeCell != null) {
        if (activate) {
          activeCell.getStyle().setZIndex(++zIndex);
          activeCell.addClassName(STYLE_ACTIVE_CELL);
          activeCell.focus();
        } else {
          activeCell.removeClassName(STYLE_ACTIVE_CELL);
        }
      }
    }
  }

  private void onActivateRow(boolean activate) {
    if (getActiveRow() >= 0) {
      NodeList<Element> rowElements = getActiveRowElements();
      if (rowElements != null && rowElements.getLength() > 0) {
        if (activate) {
          StyleUtils.addClassName(rowElements, STYLE_ACTIVE_ROW);
        } else {
          StyleUtils.removeClassName(rowElements, STYLE_ACTIVE_ROW);
        }
      }
    }
  }

  private void onSelectRow(int row, boolean select) {
    NodeList<Element> rowElements = getRowElements(row);
    if (rowElements != null && rowElements.getLength() > 0) {
      if (select) {
        StyleUtils.addClassName(rowElements, STYLE_SELECTED_ROW);
      } else {
        StyleUtils.removeClassName(rowElements, STYLE_SELECTED_ROW);
      }
    }
  }

  private void renderBody(SafeHtmlBuilder sb, List<IsRow> values) {
    int start = getPageStart();
    int actRow = getActiveRow();
    int actCol = getActiveColumn();

    String classes = StyleUtils.buildClasses(STYLE_CELL, STYLE_BODY);

    int cellHeight = getBodyCellHeight();
    Edges padding = getBodyCellPadding();
    Edges borderWidth = getBodyBorderWidth();
    Edges margin = getBodyCellMargin();

    SafeStylesBuilder stylesBuilder = new SafeStylesBuilder();
    stylesBuilder.append(StyleUtils.buildPadding(getCssValue(padding)));
    stylesBuilder.append(StyleUtils.buildBorderWidth(getCssValue(borderWidth)));
    stylesBuilder.append(StyleUtils.buildMargin(getCssValue(margin)));
    SafeStyles styles = stylesBuilder.toSafeStyles();

    int top = getHeaderHeight();

    int xIncr = getWidthIncrement(padding, borderWidth, margin);
    int yIncr = getHeightIncrement(padding, borderWidth, margin);

    for (int i = 0; i < values.size(); i++) {
      IsRow value = values.get(i);
      Assert.notNull(value);

      boolean isSelected = isRowSelected(value);
      boolean isActive = i == actRow;

      String rowClasses = StyleUtils.buildClasses(classes,
          ((i + start) % 2 == 1) ? STYLE_EVEN_ROW : STYLE_ODD_ROW);
      if (isActive) {
        rowClasses = StyleUtils.buildClasses(rowClasses, STYLE_ACTIVE_ROW);
      }
      if (isSelected) {
        rowClasses = StyleUtils.buildClasses(rowClasses, STYLE_SELECTED_ROW);
      }

      if (rowStyles != null) {
        String extraRowStyles = rowStyles.getStyleNames(value, i);
        if (extraRowStyles != null) {
          rowClasses = StyleUtils.buildClasses(rowClasses, extraRowStyles);
        }
      }

      SafeHtmlBuilder trBuilder = new SafeHtmlBuilder();
      int col = 0;
      int left = 0;
      String rowIdx = BeeUtils.toString(i);

      for (ColumnInfo columnInfo : columns) {
        Column<IsRow, ?> column = columnInfo.getColumn();

        String cellClasses = rowClasses;
        if (isActive && col == actCol) {
          cellClasses = StyleUtils.buildClasses(cellClasses, STYLE_ACTIVE_CELL);
        }

        SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
        CellContext context = new CellContext(i, col, getRowId(value), this);
        column.render(context, value, cellBuilder);
        SafeHtml cellHtml = cellBuilder.toSafeHtml();

        int width = columnInfo.getColumnWidth();
        SafeHtml html = renderCell(rowIdx, col, cellClasses, left, top, width,
            cellHeight, styles, null, column.getHorizontalAlignment(), cellHtml, true);

        trBuilder.append(html);
        left += width + xIncr;
        col++;
      }

      sb.append(trBuilder.toSafeHtml());
      top += cellHeight + yIncr;
    }
  }

  private SafeHtml renderCell(String rowIdx, int col, String classes, int left, int top,
      int width, int height, SafeStyles styles, SafeStyles extraStyles,
      HorizontalAlignmentConstant hAlign, SafeHtml cellContent, boolean focusable) {
    SafeHtml result = SafeHtmlUtils.EMPTY_SAFE_HTML;

    SafeStylesBuilder stylesBuilder = new SafeStylesBuilder();
    if (styles != null) {
      stylesBuilder.append(styles);
    }
    if (extraStyles != null) {
      stylesBuilder.append(extraStyles);
    }
    if (hAlign != null) {
      stylesBuilder.append(StyleUtils.buildStyle(StyleUtils.CSS_TEXT_ALIGN,
          hAlign.getTextAlignString()));
    }

    stylesBuilder.append(StyleUtils.buildLeft(left));
    stylesBuilder.append(StyleUtils.buildTop(top));

    if (width > 0) {
      stylesBuilder.append(StyleUtils.buildWidth(width));
    }

    if (height > 0) {
      stylesBuilder.append(StyleUtils.buildHeight(height));
    }

    if (focusable) {
      result = template.cellFocusable(rowIdx, col, classes, stylesBuilder.toSafeStyles(),
          getTabIndex(), cellContent);
    } else {
      result = template.cell(rowIdx, col, classes, stylesBuilder.toSafeStyles(), cellContent);
    }
    return result;
  }

  private void renderHeaders(SafeHtmlBuilder sb, boolean isHeader) {
    if (isHeader ? !hasHeaders() : !hasFooters()) {
      return;
    }
    int columnCount = getColumnCount();

    String classes = StyleUtils.buildClasses(STYLE_CELL, isHeader
        ? StyleUtils.buildClasses(STYLE_HEADER, StyleUtils.NAME_UNSELECTABLE) : STYLE_FOOTER);

    int cellHeight = isHeader ? getHeaderCellHeight() : getFooterCellHeight();
    Edges padding = isHeader ? getHeaderCellPadding() : getFooterCellPadding();
    Edges borderWidth = Edges.copyOf(isHeader ? getHeaderBorderWidth() : getFooterBorderWidth());
    Edges margin = isHeader ? getHeaderCellMargin() : getFooterCellMargin();

    SafeStylesBuilder stylesBuilder = new SafeStylesBuilder();
    stylesBuilder.append(StyleUtils.buildPadding(getCssValue(padding)));
    stylesBuilder.append(StyleUtils.buildMargin(getCssValue(margin)));

    SafeStyles firstColumnStyles = null;
    SafeStyles defaultColumnStyles = null;
    SafeStyles lastColumnStyles = null;

    int firstColumnWidthIncr = 0;
    int defaultColumnWidthIncr = 0;
    int lastColumnWidthIncr = 0;

    if (columnCount > 1 && borderWidth != null && !Edges.hasPositiveHorizontalValue(margin)) {
      int borderLeft = borderWidth.getIntLeft();
      int borderRight = borderWidth.getIntRight();

      if (borderLeft > 0 && borderRight > 0) {
        firstColumnStyles = StyleUtils.buildBorderWidth(getCssValue(borderWidth));
        firstColumnWidthIncr = getWidthIncrement(padding, borderWidth, margin);

        borderWidth.setLeft(0);
        lastColumnStyles = StyleUtils.buildBorderWidth(getCssValue(borderWidth));
        lastColumnWidthIncr = getWidthIncrement(padding, borderWidth, margin);

        borderWidth.setRight(Math.max(borderLeft, borderRight));
        defaultColumnStyles = StyleUtils.buildBorderWidth(getCssValue(borderWidth));
        defaultColumnWidthIncr = getWidthIncrement(padding, borderWidth, margin);
      }
    }

    if (defaultColumnStyles == null) {
      stylesBuilder.append(StyleUtils.buildBorderWidth(getCssValue(borderWidth)));
      firstColumnWidthIncr =
          defaultColumnWidthIncr =
              lastColumnWidthIncr = getWidthIncrement(padding, borderWidth, margin);
    }
    SafeStyles styles = stylesBuilder.toSafeStyles();
    SafeStyles extraStyles;

    int top = isHeader ? 0 : getHeaderHeight() + getBodyHeight();
    int left = 0;

    int xIncr = getBodyCellWidthIncrement();
    int widthIncr;

    String rowIdx = isHeader ? HEADER_ROW : FOOTER_ROW;

    for (int i = 0; i < columnCount; i++) {
      ColumnInfo columnInfo = columns.get(i);
      Header<?> header = isHeader ? columnInfo.getHeader() : columnInfo.getFooter();

      SafeHtmlBuilder headerBuilder = new SafeHtmlBuilder();
      if (header != null) {
        CellContext context = new CellContext(0, i, header.getKey(), this);
        header.render(context, headerBuilder);
      }

      int width = columnInfo.getColumnWidth();

      extraStyles = (i == 0) ? firstColumnStyles
          : (i == columnCount - 1) ? lastColumnStyles : defaultColumnStyles;
      widthIncr = (i == 0) ? firstColumnWidthIncr
          : (i == columnCount - 1) ? lastColumnWidthIncr : defaultColumnWidthIncr;

      SafeHtml contents = renderCell(rowIdx, i, classes, left, top,
          width + xIncr - widthIncr, cellHeight, styles, extraStyles, null,
          headerBuilder.toSafeHtml(), false);
      sb.append(contents);

      left += width + xIncr;
    }
  }

  private void renderRowValues(SafeHtmlBuilder sb, List<IsRow> values) {
    renderHeaders(sb, true);
    renderBody(sb, values);
    renderHeaders(sb, false);
  }

  private void replaceAllChildren(SafeHtml html) {
    getElement().setInnerHTML(html.asString());
  }

  private void selectRow(int visibleIndex, IsRow rowValue) {
    if (rowValue == null) {
      return;
    }
    long rowId = rowValue.getId();
    boolean wasSelected = isRowSelected(rowId);

    if (wasSelected) {
      selectedRows.remove(rowId);
    } else {
      selectedRows.add(rowId);
    }
    if (getSelectionModel() != null) {
      getSelectionModel().setSelected(rowValue, !wasSelected);
    }
    onSelectRow(visibleIndex, !wasSelected);
  }

  private void setVisibleRange(Range range, boolean clearData, boolean forceRangeChangeEvent) {
    int start = range.getStart();
    int length = range.getLength();
    Assert.nonNegative(start);
    Assert.isPositive(length);

    int oldStart = getPageStart();
    int oldSize = getPageSize();

    boolean pageStartChanged = (oldStart != start);
    boolean pageSizeChanged = (oldSize != length);

    if (pageStartChanged) {
      this.pageStart = start;
    }
    if (pageSizeChanged) {
      this.pageSize = length;
    }

    if (clearData) {
      rowData.clear();
    }

    if (pageStartChanged || pageSizeChanged || forceRangeChangeEvent) {
      RangeChangeEvent.fire(this, getVisibleRange());
    }
  }

  private void sinkEvents(Set<String> typeNames) {
    if (typeNames == null) {
      return;
    }

    int eventsToSink = 0;
    for (String typeName : typeNames) {
      int typeInt = Event.getTypeInt(typeName);
      if (typeInt > 0) {
        eventsToSink |= typeInt;
      }
    }
    if (eventsToSink > 0) {
      sinkEvents(eventsToSink);
    }
  }

  private void updateOrder(String columnId, boolean hasModifiers) {
    Assert.notEmpty(columnId);
    Order ord = getSortOrder();
    int size = ord.getSize();

    if (size <= 0) {
      ord.add(columnId, true);
      return;
    }

    int index = ord.getIndex(columnId);
    if (BeeConst.isUndef(index)) {
      if (!hasModifiers) {
        ord.clear();
      }
      ord.add(columnId, true);
      return;
    }

    boolean asc = ord.isAscending(columnId);

    if (hasModifiers) {
      if (size == 1) {
        ord.setAscending(columnId, !asc);
      } else if (index == size - 1) {
        if (asc) {
          ord.setAscending(columnId, !asc);
        } else {
          ord.remove(columnId);
        }
      } else {
        ord.remove(columnId);
        ord.add(columnId, true);
      }

    } else if (size > 1) {
      ord.clear();
      ord.add(columnId, true);
    } else if (asc) {
      ord.setAscending(columnId, !asc);
    } else {
      ord.clear();
    }
  }
}

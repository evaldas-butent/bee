package com.butent.bee.client.view.grid;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Font;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.Modifiers;
import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.view.event.SortEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the structure and behavior of a cell grid user interface component.
 */

public class CellGrid extends Widget implements HasId, HasDataTable {

  public enum ResizerMode {
    HORIZONTAL(10, 4), VERTICAL(10, 4);

    int handlePx;
    int barPx;

    private ResizerMode(int handlePx, int barPx) {
      this.handlePx = handlePx;
      this.barPx = barPx;
    }

    public int getBarPx() {
      return barPx;
    }

    public int getHandlePx() {
      return handlePx;
    }

    public void setBarPx(int barPx) {
      this.barPx = barPx;
    }

    public void setHandlePx(int handlePx) {
      this.handlePx = handlePx;
    }
  }

  /**
   * Contains templates which facilitates compile-time binding of HTML templates to generate
   * SafeHtml strings.
   */

  public interface Template extends SafeHtmlTemplates {
    @Template("<div data-row=\"{0}\" data-col=\"{1}\" class=\"{2}\" style=\"{3}position:absolute;\">{4}</div>")
    SafeHtml cell(String rowIdx, int colIdx, String classes, SafeStyles styles, SafeHtml contents);

    @Template("<div data-row=\"{0}\" data-col=\"{1}\" class=\"{2}\" style=\"{3}position:absolute;\" tabindex=\"{4}\">{5}</div>")
    SafeHtml cellFocusable(String rowIdx, int colIdx, String classes, SafeStyles styles,
        int tabIndex, SafeHtml contents);

    @Template("<div id=\"{0}\" style=\"position:absolute; top:-64px; left:-64px;\">{1}{2}</div>")
    SafeHtml resizer(String id, SafeHtml handle, SafeHtml bar);

    @Template("<div id=\"{0}\" style=\"position:absolute;\"></div>")
    SafeHtml resizerBar(String id);

    @Template("<div id=\"{0}\" style=\"position:absolute;\"></div>")
    SafeHtml resizerHandle(String id);
  }

  private class CellSize {
    private int width;
    private int height;

    private CellSize(int width, int height) {
      this.width = width;
      this.height = height;
    }

    private int getHeight() {
      return height;
    }

    private int getWidth() {
      return width;
    }

    private void setHeight(int height) {
      this.height = height;
    }
    
    private void setSize(int width, int height) {
      setWidth(width);
      setHeight(height);
    }
    
    private void setWidth(int width) {
      this.width = width;
    }
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
  
  private class ResizerMoveTimer extends Timer {
    private boolean pending = false;
    private int pendingMove = 0;

    @Override
    public void cancel() {
      if (pending) {
        pending = false;
        super.cancel();
      }
    }

    @Override
    public void run() {
      pending = false;
      doResize();
    }

    @Override
    public void schedule(int delayMillis) {
      pending = true;
      super.schedule(delayMillis);
    }
    
    private void doResize() {
      if (pendingMove == 0) {
        return;
      }
      switch (getResizerStatus()) {
        case HORIZONTAL:
          resizeHorizontal(pendingMove);
          break;
        case VERTICAL:
          resizeVertical(pendingMove);
          break;
      }
      pendingMove = 0;
    }
    
    private void handleMove(int by, int millis) {
      if (by == 0) {
        return;
      }
      pendingMove += by;
      if (!pending && pendingMove != 0) {
        schedule(millis);
      }
    }
    
    private void reset() {
      cancel();
      pendingMove = 0;
    }
    
    private void stop() {
      cancel();
      doResize();
    }
  }
  
  private class ResizerShowTimer extends Timer {
    private boolean pending = false;

    private Element element;
    private String rowIdx = null;
    private int colIdx = BeeConst.UNDEF;
    
    private ResizerMode resizerMode = null;
    private Rectangle rectangle = null; 
    
    private ResizerShowTimer() {
      super();
    }

    @Override
    public void cancel() {
      if (pending) {
        pending = false;
        super.cancel();
      }
    }

    public void run() {
      pending = false;

      switch (resizerMode) {
        case HORIZONTAL:
          showColumnResizer(element, colIdx);
          break;
        case VERTICAL:
          showRowResizer(element, rowIdx);
          break;
      }
    }

    @Override
    public void schedule(int delayMillis) {
      pending = true;
      super.schedule(delayMillis);
    }

    private void handleEvent(Event event) {
      if (!pending) {
        return;
      }
      if (!EventUtils.isMouseMove(event.getType())) {
        cancel();
        return;
      }
      
      if (!rectangle.contains(event.getClientX(), event.getClientY())) {
        cancel();
      }
    }
    
    private void start(Element el, String row, int col, ResizerMode resizer, Rectangle rect,
        int millis) {
      this.element = el;
      this.rowIdx = row;
      this.colIdx = col;
      this.resizerMode = resizer;
      this.rectangle = rect;
      
      schedule(millis);
    }
  }
  
  private enum TargetType {
    CONTAINER, RESIZER, HEADER, BODY, FOOTER;
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

  public static int defaultMinCellWidth = 16;
  public static int defaultMaxCellWidth = 1024;
  public static int defaultMinCellHeight = 8;
  public static int defaultMaxCellHeight = 256;

  public static int defaultResizerShowSensitivityMillis = 100;
  public static int defaultResizerMoveSensitivityMillis = 0;

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

  public static String STYLE_RESIZER = "bee-CellGridResizer";
  public static String STYLE_RESIZER_HANDLE = "bee-CellGridResizerHandle";
  public static String STYLE_RESIZER_BAR = "bee-CellGridResizerBar";

  public static String STYLE_RESIZER_HORIZONTAL = "bee-CellGridResizerHorizontal";
  public static String STYLE_RESIZER_HANDLE_HORIZONTAL = "bee-CellGridResizerHandleHorizontal";
  public static String STYLE_RESIZER_BAR_HORIZONTAL = "bee-CellGridResizerBarHorizontal";

  public static String STYLE_RESIZER_VERTICAL = "bee-CellGridResizerVertical";
  public static String STYLE_RESIZER_HANDLE_VERTICAL = "bee-CellGridResizerHandleVertical";
  public static String STYLE_RESIZER_BAR_VERTICAL = "bee-CellGridResizerBarVertical";

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

  private final String resizerId = DomUtils.createUniqueId("resizer");
  private final String resizerHandleId = DomUtils.createUniqueId("resizer-handle");
  private final String resizerBarId = DomUtils.createUniqueId("resizer-bar");

  private ResizerMode resizerStatus = null;
  private boolean isResizing = false;
  private String resizerRow = null;
  private int resizerCol = BeeConst.UNDEF;
  private Modifiers resizerModifiers = null;
  
  private int resizerPosition = BeeConst.UNDEF;
  private int resizerPositionMin = BeeConst.UNDEF;
  private int resizerPositionMax = BeeConst.UNDEF;

  private int resizerShowSensitivityMillis = defaultResizerShowSensitivityMillis;
  private int resizerMoveSensitivityMillis = defaultResizerMoveSensitivityMillis;
  
  private final ResizerShowTimer resizerShowTimer = new ResizerShowTimer();
  private final ResizerMoveTimer resizerMoveTimer = new ResizerMoveTimer();
  
  private final Map<Long, Integer> resizedRows = Maps.newHashMap();
  private final Table<Long, String, CellSize> resizedCells = HashBasedTable.create();

  public CellGrid() {
    setElement(Document.get().createDivElement());

    Set<String> eventTypes = Sets.newHashSet(EventUtils.EVENT_TYPE_KEY_DOWN,
        EventUtils.EVENT_TYPE_KEY_PRESS, EventUtils.EVENT_TYPE_CLICK,
        EventUtils.EVENT_TYPE_MOUSE_DOWN, EventUtils.EVENT_TYPE_MOUSE_MOVE,
        EventUtils.EVENT_TYPE_MOUSE_UP, EventUtils.EVENT_TYPE_MOUSE_OUT);
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
    setHasCellPreview(true);
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
    return getPageSize();
  }

  public List<IsRow> getVisibleItems() {
    return rowData;
  }

  public Range getVisibleRange() {
    return new Range(getPageStart(), getPageSize());
  }

  public long getVisibleRowId(int indexOnPage) {
    return getVisibleItem(indexOnPage).getId();
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
    String eventType = event.getType();

    EventTarget eventTarget = event.getEventTarget();
    if (!Element.is(eventTarget)) {
      return;
    }
    Element targetElement = Element.as(eventTarget);

    TargetType targetType = null;
    String rowIdx = null;
    int row = BeeConst.UNDEF;
    int col = BeeConst.UNDEF;

    if (targetElement == getElement()) {
      targetType = TargetType.CONTAINER;
    } else if (isResizerOrResizerChild(targetElement)) {
      targetType = TargetType.RESIZER;
    } else if (getElement().isOrHasChild(targetElement)) {
      while (targetElement != null && targetElement != getElement()) {
        rowIdx = DomUtils.getDataRow(targetElement);
        if (!BeeUtils.isEmpty(rowIdx)) {
          break;
        }
        targetElement = targetElement.getParentElement();
      }
      if (!BeeUtils.isEmpty(rowIdx)) {
        col = BeeUtils.toInt(DomUtils.getDataColumn(targetElement));
        checkColumnBounds(col);

        if (isHeaderRow(rowIdx)) {
          targetType = TargetType.HEADER;
        } else if (isFooterRow(rowIdx)) {
          targetType = TargetType.FOOTER;
        } else if (isBodyRow(rowIdx)) {
          targetType = TargetType.BODY;
          row = BeeUtils.toInt(rowIdx);
          checkRowBounds(row);
        }
      }
    }

    if (targetType == null) {
      EventUtils.logEvent(event, "unkown event target");
      return;
    }
    
    getResizerShowTimer().handleEvent(event);

    if (EventUtils.isMouseMove(eventType)) {
      if (handleMouseMove(event, targetElement, targetType, rowIdx, col)) {
        return;
      }
    } else if (EventUtils.isMouseDown(eventType)) {
      if (targetType == TargetType.RESIZER) {
        startResizing(event);
        EventUtils.eatEvent(event);
        return;
      }
    } else if (EventUtils.isMouseUp(eventType)) {
      if (isResizing()) {
        stopResizing();
        EventUtils.eatEvent(event);
        return;
      }
      if (isCellActive(row, col)) {
        checkCellSize(targetElement, row, col);
      }
    } else if (EventUtils.isMouseOut(eventType)) {
      if (targetType == TargetType.RESIZER && !isResizing()) {
        hideResizer();
        return;
      }
      if (event.getRelatedEventTarget() != null 
          && !getElement().isOrHasChild(Node.as(event.getRelatedEventTarget()))) {
        if (isResizing()) {
          stopResizing();
        } else if (isResizerVisible()) {
          hideResizer();
        }
        return;
      }
    }

    if (targetType == TargetType.HEADER) {
      Header<?> header = columns.get(col).getHeader();
      if (header != null && cellConsumesEventType(header.getCell(), eventType)) {
        CellContext context = new CellContext(0, col, header.getKey(), this);
        header.onBrowserEvent(context, targetElement, event);
      }

    } else if (targetType == TargetType.FOOTER) {
      Header<?> footer = columns.get(col).getFooter();
      if (footer != null && cellConsumesEventType(footer.getCell(), eventType)) {
        CellContext context = new CellContext(0, col, footer.getKey(), this);
        footer.onBrowserEvent(context, targetElement, event);
      }

    } else if (targetType == TargetType.BODY) {
      IsRow rowValue = getVisibleItem(row);
      Column<IsRow, ?> column = columns.get(col).getColumn();
      CellContext context = new CellContext(row, col, getRowId(rowValue), this);
      boolean isEditing = isCellEditing(targetElement, rowValue, context, column);

      if (!isEditing) {
        if (EventUtils.isClick(eventType)) {
          if (EventUtils.hasModifierKey(event)) {
            selectRow(row, rowValue);
            return;
          }
          if (!isCellActive(row, col)) {
            activateCell(row, col);
            return;
          }

        } else if (EventUtils.isKeyDown(eventType)) {
          if (handleKey(event.getKeyCode())) {
            EventUtils.eatEvent(event);
            return;
          }
        } else if (EventUtils.isKeyPress(eventType)) {
          if (handleChar(event.getCharCode(), row, col, rowValue, targetElement)) {
            EventUtils.eatEvent(event);
            return;
          }
        }
      }

      if (hasCellPreview()) {
        CellPreviewEvent<IsRow> previewEvent = CellPreviewEvent.fire(this, event, this, context,
            rowValue, isEditing, column.getCell().handlesSelection());
        if (previewEvent.isCanceled()) {
          return;
        }
      }
      fireEventToCell(event, eventType, targetElement, rowValue, context, column);
    }
  }

  public void redraw() {
    SafeHtmlBuilder sb = new SafeHtmlBuilder();
    renderRowValues(sb, rowData);
    renderResizer(sb);
    replaceAllChildren(sb.toSafeHtml());

    setZIndex(0);

    if (getActiveRow() >= 0 && getActiveColumn() >= 0) {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        public void execute() {
          Element cellElement = getActiveCellElement();
          if (cellElement != null) {
            cellElement.getStyle().setZIndex(incrementZIndex());
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

    int oldRow = getActiveRow();
    if (oldRow >= 0 && oldRow < rowData.size()) {
      int newRow = 0;
      long id = rowData.get(oldRow).getId();
      for (int i = 0; i < size; i++) {
        if (values.get(i).getId() == id) {
          newRow = i;
          break;
        }
      }
      this.activeRow = newRow;
    }
    
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

  @Override
  protected void onUnload() {
    getResizerShowTimer().cancel();
    getResizerMoveTimer().cancel();
    super.onUnload();
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

  private void checkCellSize(Element element, int row, int col) {
    int width = StyleUtils.getWidth(element);
    int height = StyleUtils.getHeight(element);
    if (width <= 0 || height <= 0) {
      return;
    }
    
    long rowId = getVisibleRowId(row);
    String columnId = getColumnId(col);
    
    if (width == getColumnWidth(col) && height == getRowHeightById(rowId)) {
      getResizedCells().remove(rowId, columnId);
    } else {
      CellSize size = getResizedCells().get(rowId, columnId);
      if (size == null) {
        getResizedCells().put(rowId, columnId, new CellSize(width, height));
      } else {
        size.setSize(width, height);
      }
    }
  }

  private void checkColumnBounds(int col) {
    Assert.isTrue(isColumnWithinBounds(col));
  }

  private void checkColumnId(String columnId) {
    Assert.notNull(columnId);
    Assert.isFalse(contains(columnId), "Duplicate Column Id " + columnId);
  }

  private boolean checkResizerBounds(int position) {
    return position >= getResizerPositionMin() && position <= getResizerPositionMax();
  }

  private void checkRowBounds(int row) {
    Assert.isTrue(isRowWithinBounds(row));
  }

  private void clearRowResized(int row) {
    getResizedRows().remove(getVisibleRowId(row));
  }

  private <C> void fireEventToCell(Event event, String eventType,
      Element parentElem, IsRow value, CellContext context, Column<IsRow, C> column) {
    Cell<C> cell = column.getCell();
    if (cellConsumesEventType(cell, eventType)) {
      column.onBrowserEvent(context, parentElem, value, event);
    }
  }

  private Element getActiveCellElement() {
    return getBodyCellElement(getActiveRow(), getActiveColumn());
  }

  private NodeList<Element> getActiveRowElements() {
    return getRowElements(getActiveRow());
  }

  private Element getBodyCellElement(int row, int col) {
    return Selectors.getElement(getBodyCellSelector(row, col));
  }

  private String getBodyCellSelector(int row, int col) {
    return Selectors.conjunction(getBodyRowSelector(row), getColumnSelector(col));
  }

  private int getBodyHeight() {
    int height = 0;
    int increment = getBodyCellHeightIncrement();
    for (int i = 0; i < getPageSize(); i++) {
      height += getRowHeight(i) + increment;
    }
    return height;
  }

  private String getBodyRowSelector(int visibleIndex) {
    return Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, visibleIndex);
  }

  private Element getCellElement(String rowIdx, int col) {
    return Selectors.getElement(getCellSelector(rowIdx, col));
  }

  private String getCellSelector(String rowIdx, int col) {
    return Selectors.conjunction(getRowSelector(rowIdx), getColumnSelector(col));
  }

  private CellSize getCellSize(Long rowId, String columnId) {
    CellSize size = getResizedCells().get(rowId, columnId);
    if (size == null) {
      size = new CellSize(getColumnWidth(columnId), getRowHeightById(rowId));
    }
    return size;
  }

  private int getChildrenHeight() {
    return getHeaderHeight() + getBodyHeight() + getFooterHeight();
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

  private String getColumnSelector(int col) {
    return Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_COLUMN, col);
  }

  private String getCssValue(Edges edges) {
    if (edges == null) {
      return Edges.EMPTY_CSS_VALUE;
    } else {
      return edges.getCssValue();
    }
  }

  private Element getFooterCellElement(int col) {
    return Selectors.getElement(getFooterCellSelector(col));
  }

  private String getFooterCellSelector(int col) {
    return Selectors.conjunction(getFooterRowSelector(), getColumnSelector(col));
  }

  private NodeList<Element> getFooterElements() {
    if (hasFooters()) {
      return getRowElements(FOOTER_ROW);
    } else {
      return null;
    }
  }

  private String getFooterRowSelector() {
    return Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, FOOTER_ROW);
  }

  private Element getHeaderCellElement(int col) {
    return Selectors.getElement(getHeaderCellSelector(col));
  }

  private String getHeaderCellSelector(int col) {
    return Selectors.conjunction(getHeaderRowSelector(), getColumnSelector(col));
  }

  private NodeList<Element> getHeaderElements() {
    if (hasHeaders()) {
      return getRowElements(HEADER_ROW);
    } else {
      return null;
    }
  }

  private String getHeaderRowSelector() {
    return Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, HEADER_ROW);
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

  private Table<Long, String, CellSize> getResizedCells() {
    return resizedCells;
  }

  private Map<Long, Integer> getResizedRows() {
    return resizedRows;
  }

  private Element getResizerBar() {
    return Document.get().getElementById(resizerBarId);
  }

  private int getResizerCol() {
    return resizerCol;
  }

  private Element getResizerContainer() {
    return Document.get().getElementById(resizerId);
  }

  private Element getResizerHandle() {
    return Document.get().getElementById(resizerHandleId);
  }

  private Modifiers getResizerModifiers() {
    return resizerModifiers;
  }

  private int getResizerMoveSensitivityMillis() {
    return resizerMoveSensitivityMillis;
  }

  private ResizerMoveTimer getResizerMoveTimer() {
    return resizerMoveTimer;
  }

  private int getResizerPosition() {
    return resizerPosition;
  }

  private int getResizerPositionMax() {
    return resizerPositionMax;
  }

  private int getResizerPositionMin() {
    return resizerPositionMin;
  }

  private String getResizerRow() {
    return resizerRow;
  }

  private int getResizerShowSensitivityMillis() {
    return resizerShowSensitivityMillis;
  }

  private ResizerShowTimer getResizerShowTimer() {
    return resizerShowTimer;
  }

  private ResizerMode getResizerStatus() {
    return resizerStatus;
  }

  private NodeList<Element> getRowElements(int row) {
    return Selectors.getNodes(Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, row));
  }

  private NodeList<Element> getRowElements(String rowIdx) {
    return Selectors.getNodes(Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, rowIdx));
  }

  private int getRowHeight(int row) {
    return getRowHeightById(getVisibleRowId(row));
  }

  private int getRowHeight(IsRow rowValue) {
    return getRowHeightById(rowValue.getId());
  }

  private int getRowHeight(String rowIdx) {
    if (isHeaderRow(rowIdx)) {
      return getHeaderCellHeight();
    }
    if (isFooterRow(rowIdx)) {
      return getFooterCellHeight();
    }
    return getRowHeight(BeeUtils.toInt(rowIdx));
  }

  private int getRowHeightById(long id) {
    Integer height = getResizedRows().get(id);
    if (height == null) {
      return getBodyCellHeight();
    } else {
      return height;
    }
  }

  private Object getRowId(IsRow value) {
    return (value == null) ? value : value.getId();
  }

  private String getRowSelector(String rowIdx) {
    return Selectors.attributeEquals(DomUtils.ATTRIBUTE_DATA_ROW, rowIdx);
  }

  private int getRowWidth(String rowIdx) {
    int col = getColumnCount() - 1;
    if (col < 0) {
      return 0;
    }
    Element element = null;
    if (!BeeUtils.isEmpty(rowIdx)) {
      element = getCellElement(rowIdx, col);
    }

    if (element == null && hasHeaders()) {
      element = getHeaderCellElement(col);
    }
    if (element == null && hasFooters()) {
      element = getFooterCellElement(col);
    }
    if (element == null && getPageSize() > 0) {
      for (int i = 0; i < getPageSize(); i++) {
        element = getBodyCellElement(i, col);
        if (element != null) {
          break;
        }
      }
    }

    if (element == null) {
      return 0;
    } else {
      return element.getOffsetLeft() + element.getOffsetWidth();
    }
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
      case BeeConst.CHAR_TWO:
      case BeeConst.CHAR_FOUR:
      case BeeConst.CHAR_SIX:
      case BeeConst.CHAR_EIGHT:
        if (cell == null) {
          return false;
        }
        int oldWidth = StyleUtils.getWidth(cell);
        int oldHeight = StyleUtils.getHeight(cell);
        if (oldWidth <= 0 || oldHeight <= 0) {
          return false;
        }

        int newWidth = oldWidth;
        int newHeight = oldHeight;

        switch (charCode) {
          case BeeConst.CHAR_PLUS:
            newWidth++;
            newHeight++;
            break;
          case BeeConst.CHAR_MINUS:
            newWidth--;
            newHeight--;
            break;
          case BeeConst.CHAR_ZERO:
            newWidth = getColumnWidth(col);
            newHeight = getRowHeight(row);
            break;
          case BeeConst.CHAR_TWO:
            newHeight++;
            break;
          case BeeConst.CHAR_FOUR:
            newWidth--;
            break;
          case BeeConst.CHAR_SIX:
            newWidth++;
            break;
          case BeeConst.CHAR_EIGHT:
            newHeight--;
            break;
        }

        newWidth = BeeUtils.limit(newWidth, getMinCellWidth(), getMaxCellWidth());
        newHeight = BeeUtils.limit(newHeight, getMinCellHeight(), getMaxCellHeight());
        if (newWidth <= 0) {
          newWidth = oldWidth;
        }
        if (newHeight <= 0) {
          newHeight = oldHeight;
        }

        if (newWidth == oldWidth && newHeight == oldHeight) {
          return false;
        }
        StyleUtils.setWidth(cell, newWidth);
        StyleUtils.setHeight(cell, newHeight);
        
        checkCellSize(cell, row, col);
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
        if (getActiveColumn() > 0) {
          setActiveColumn(getActiveColumn() - 1);
        }
        return true;
      case KeyCodes.KEY_BACKSPACE:
        keyboardLeft();
        return true;
      case KeyCodes.KEY_RIGHT:
        if (getActiveColumn() < getColumnCount() - 1) {
          setActiveColumn(getActiveColumn() + 1);
        }
        return true;
      case KeyCodes.KEY_TAB:
        keyboardRight();
        return true;
      default:
        return false;
    }
  }

  private boolean handleMouseMove(Event event, Element element, TargetType targetType,
      String eventRow, int eventCol) {
    int x = event.getClientX();
    int y = event.getClientY();

    if (!isResizerVisible()) {
      int millis = getResizerShowSensitivityMillis();
      
      if (isResizeAllowed(targetType, eventRow, eventCol, ResizerMode.HORIZONTAL)) {
        int size = ResizerMode.HORIZONTAL.getHandlePx();
        int right = element.getAbsoluteRight();

        if (BeeUtils.betweenInclusive(right - x, 0, size / 2)) {
          if (millis > 0) {
            getResizerShowTimer().start(element, eventRow, eventCol, ResizerMode.HORIZONTAL,
                new Rectangle(right - size / 2, y, size, size), millis);
          } else {
            showColumnResizer(element, eventCol);
          }
           return true;
        }
      }

      if (isResizeAllowed(targetType, eventRow, eventCol, ResizerMode.VERTICAL)) {
        int size = ResizerMode.VERTICAL.getHandlePx();
        int bottom = element.getAbsoluteBottom();
        
        if (BeeUtils.betweenInclusive(bottom - y, 0,size / 2)) {
          if (millis > 0) {
            getResizerShowTimer().start(element, eventRow, eventCol, ResizerMode.VERTICAL,
                new Rectangle(x, bottom - size / 2, size, size), millis);
          } else {
            showRowResizer(element, eventRow);
          }
          return true;
        }
      }

    } else if (isResizing()) {
      int position = getResizerPosition();
      int millis = getResizerMoveSensitivityMillis();
      
      switch (getResizerStatus()) {
        case HORIZONTAL:
          if (checkResizerBounds(x)) {
            if (millis > 0) {
              getResizerMoveTimer().handleMove(x - position, millis);
            } else {
              resizeHorizontal(x - position);
            }
          }
          break;
        case VERTICAL:
          if (checkResizerBounds(y)) {
            if (millis > 0) {
              getResizerMoveTimer().handleMove(y - position, millis);
            } else {
              resizeVertical(y - position);
            }
          }
          break;
        default:
          Assert.untouchable();
      }

    } else {
      if (!Rectangle.createFromAbsoluteCoordinates(getResizerContainer()).contains(x, y)) {
        hideResizer();
      }
    }

    return true;
  }
  
  private boolean hasCellPreview() {
    return hasCellPreview;
  }

  private void hideResizer() {
    StyleUtils.hideDisplay(resizerId);
    StyleUtils.hideDisplay(resizerHandleId);
    StyleUtils.hideDisplay(resizerBarId);

    setResizerStatus(null);
    setResizerRow(null);
    setResizerCol(BeeConst.UNDEF);
  }
  
  private int incrementZIndex() {
    int z = getZIndex() + 1;
    setZIndex(z);
    return z;
  }
  
  private boolean isBodyRow(String rowIdx) {
    return BeeUtils.isDigit(rowIdx);
  }

  private boolean isCellActive(int row, int col) {
    return getActiveRow() == row && getActiveColumn() == col;
  }
  
  private <C> boolean isCellEditing(Element parentElem, IsRow rowValue, CellContext context,
      Column<IsRow, C> column) {
    Cell<C> cell = column.getCell();
    return cell.isEditing(context, parentElem, column.getValue(rowValue));
  }

  private boolean isCellResized(int row, int col) {
    return getResizedCells().contains(getVisibleRowId(row), getColumnId(col));
  }
  
  private boolean isColumnWithinBounds(int col) {
    return col >= 0 && col < getColumnCount();
  }

  private boolean isFooterRow(String rowIdx) {
    return BeeUtils.same(rowIdx, FOOTER_ROW);
  }

  private boolean isHeaderRow(String rowIdx) {
    return BeeUtils.same(rowIdx, HEADER_ROW);
  }
  
  private boolean isResizeAllowed(TargetType target, String rowIdx, int col, ResizerMode resizer) {
    if (resizer.getHandlePx() <= 0) {
      return false;
    }
    if (target == TargetType.HEADER || target == TargetType.FOOTER) {
      return true;
    }
    if (target != TargetType.BODY) {
      return false;
    }
    
    int row = BeeUtils.toInt(rowIdx);
    if (isCellActive(row, col)) {
      return false;
    }
    
    switch (resizer) {
      case HORIZONTAL:
        return row == 0 && !hasHeaders() || row == getPageSize() - 1 && !hasFooters();
      case VERTICAL:
        return col == 0 || col == getColumnCount() - 1;
      default:
        Assert.untouchable();
        return false;
    }
  }

  private boolean isResizerOrResizerChild(Element element) {
    if (element == null) {
      return false;
    }
    String id = element.getId();
    if (BeeUtils.isEmpty(id)) {
      return false;
    }
    return BeeUtils.inListSame(id, resizerId, resizerHandleId, resizerBarId);
  }

  private boolean isResizerVisible() {
    return getResizerStatus() != null;
  }

  private boolean isResizing() {
    return isResizing;
  }

  private boolean isRowResized(int row) {
    return getResizedRows().containsKey(getVisibleRowId(row));
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
    return row >= 0 && row < getPageSize();
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
          activeCell.getStyle().setZIndex(incrementZIndex());
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

  private void refreshHeader(int col) {
    Header<?> header = getColumnInfo(col).getHeader();
    if (header == null) {
      return;
    }
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    CellContext context = new CellContext(0, col, header.getKey(), this);
    header.render(context, builder);
    
    getHeaderCellElement(col).setInnerHTML(builder.toSafeHtml().asString());
  }

  private void renderBody(SafeHtmlBuilder sb, List<IsRow> values) {
    int start = getPageStart();
    int actRow = getActiveRow();
    int actCol = getActiveColumn();

    String classes = StyleUtils.buildClasses(STYLE_CELL, STYLE_BODY);

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
      SafeStyles extraStyles = null;

      int col = 0;
      int left = 0;

      String rowIdx = BeeUtils.toString(i);
      long valueId = value.getId();
      int rowHeight = getRowHeight(value);

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
        
        int columnWidth = columnInfo.getColumnWidth();
        CellSize cellSize = getCellSize(valueId, columnInfo.getColumnId());
        int cellWidth = cellSize.getWidth();
        int cellHeight = cellSize.getHeight();
        
        if (cellWidth <= columnWidth && cellHeight <= rowHeight) {
          extraStyles = null;
        } else {
          extraStyles = StyleUtils.buildZIndex(incrementZIndex());
        }

        SafeHtml html = renderCell(rowIdx, col, cellClasses, left, top, cellWidth, cellHeight,
            styles, extraStyles, column.getHorizontalAlignment(), cellHtml, true);

        trBuilder.append(html);
        left += columnWidth + xIncr;
        col++;
      }

      sb.append(trBuilder.toSafeHtml());
      top += rowHeight + yIncr;
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

    String classes = StyleUtils.buildClasses(STYLE_CELL, isHeader ? STYLE_HEADER : STYLE_FOOTER);

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

  private void renderResizer(SafeHtmlBuilder sb) {
    sb.append(template.resizer(resizerId,
        template.resizerHandle(resizerHandleId), template.resizerBar(resizerBarId)));
  }

  private void renderRowValues(SafeHtmlBuilder sb, List<IsRow> values) {
    renderHeaders(sb, true);
    renderBody(sb, values);
    renderHeaders(sb, false);
  }

  private void replaceAllChildren(SafeHtml html) {
    getElement().setInnerHTML(html.asString());
  }

  private int resizeColumnWidth(int col, int oldWidth, int incr) {
    if (incr == 0 || oldWidth <= 0) {
      return BeeConst.UNDEF;
    }

    int newWidth = BeeUtils.limit(oldWidth + incr, getMinCellWidth(), getMaxCellWidth());
    if (newWidth <= 0 || !BeeUtils.sameSign(newWidth - oldWidth, incr)) {
      return BeeConst.UNDEF;
    }

    setColumnWidth(col, newWidth);

    NodeList<Element> nodes = getColumnElements(col);
    if (getResizedCells().containsColumn(getColumnId(col))) {
      for (int i = 0; i < nodes.getLength(); i++) {
        Element cellElement = nodes.getItem(i);
        String rowIdx = DomUtils.getDataRow(cellElement);
        if (isBodyRow(rowIdx) && isCellResized(BeeUtils.toInt(rowIdx), col)) {
          continue;
        }
        DomUtils.resizeHorizontalBy(cellElement, newWidth - oldWidth);
      }
    } else {
      DomUtils.resizeHorizontalBy(nodes, newWidth - oldWidth);
    }
    
    refreshHeader(col);

    if (col < getColumnCount() - 1) {
      for (int i = col + 1; i < getColumnCount(); i++) {
        nodes = getColumnElements(i);
        if (nodes == null || nodes.getLength() <= 0) {
          continue;
        }
        DomUtils.moveHorizontalBy(nodes, newWidth - oldWidth);
      }
    }
    return newWidth;
  }

  private void resizeHorizontal(int by) {
    if (by == 0) {
      return;
    }

    int col = getResizerCol();
    int oldWidth = getColumnWidth(col);
    int newWidth = resizeColumnWidth(col, oldWidth, by);
    if (BeeConst.isUndef(newWidth)) {
      return;
    }

    int incr = newWidth - oldWidth;
    if (incr != 0) {
      DomUtils.moveHorizontalBy(resizerId, incr);
      setResizerPosition(getResizerPosition() + incr);
    }
  }

  private void resizeRowElements(int row, NodeList<Element> nodes, int dh) {
    if (getResizedCells().containsRow(getVisibleRowId(row))) {
      for (int i = 0; i < nodes.getLength(); i++) {
        Element cellElement = nodes.getItem(i);
        int col = BeeUtils.toInt(DomUtils.getDataColumn(cellElement));
        if (isCellResized(row, col)) {
          continue;
        }
        DomUtils.resizeVerticalBy(cellElement, dh);
      }
    } else {
      DomUtils.resizeVerticalBy(nodes, dh);
    }
  }

  private int resizeRowHeight(String rowIdx, int oldHeight, int incr, Modifiers modifiers) {
    if (oldHeight <= 0 || incr == 0) {
      return BeeConst.UNDEF;
    }
    int newHeight = BeeUtils.limit(oldHeight + incr, getMinCellHeight(), getMaxCellHeight());
    if (newHeight <= 0 || !BeeUtils.sameSign(newHeight - oldHeight, incr)) {
      return BeeConst.UNDEF;
    }

    int dh = newHeight - oldHeight;
    int rc = getPageSize();
    NodeList<Element> nodes;

    if (isBodyRow(rowIdx) && Modifiers.isNotEmpty(modifiers)) {
      setBodyCellHeight(newHeight);
      for (int i = 0; i < rc; i++) {
        nodes = getRowElements(i);
        if (i > 0) {
          DomUtils.moveVerticalBy(nodes, i * dh);
        }
        resizeRowElements(i, nodes, dh);
        if (isRowResized(i)) {
          setRowHeight(i, getRowHeight(i) + dh);
        }
      }
      nodes = getFooterElements();
      if (nodes != null) {
        DomUtils.moveVerticalBy(nodes, rc * dh);
      }

    } else if (isHeaderRow(rowIdx)) {
      DomUtils.resizeVerticalBy(getHeaderElements(), dh);
      for (int i = 0; i < rc; i++) {
        DomUtils.moveVerticalBy(getRowElements(i), dh);
      }
      nodes = getFooterElements();
      if (nodes != null) {
        DomUtils.moveVerticalBy(nodes, dh);
      }
      setHeaderCellHeight(newHeight);

    } else if (isBodyRow(rowIdx)) {
      int row = BeeUtils.toInt(rowIdx);
      resizeRowElements(row, getRowElements(row), dh);
      if (row < rc - 1) {
        for (int i = row + 1; i < rc; i++) {
          DomUtils.moveVerticalBy(getRowElements(i), dh);
        }
      }
      nodes = getFooterElements();
      if (nodes != null) {
        DomUtils.moveVerticalBy(nodes, dh);
      }
      setRowHeight(row, newHeight);

    } else if (isFooterRow(rowIdx)) {
      DomUtils.resizeVerticalBy(getFooterElements(), dh);
      setFooterCellHeight(newHeight);
    }

    return newHeight;
  }

  private void resizeVertical(int by) {
    if (by == 0) {
      return;
    }

    String rowIdx = getResizerRow();
    Element cellElement = getCellElement(rowIdx, 0);
    int oldHeight = getRowHeight(rowIdx);
    int oldTop = cellElement.getOffsetTop();

    int newHeight = resizeRowHeight(rowIdx, oldHeight, by, getResizerModifiers());
    if (BeeConst.isUndef(newHeight)) {
      return;
    }

    int incr = cellElement.getOffsetTop() - oldTop + newHeight - oldHeight;
    if (incr != 0) {
      DomUtils.moveVerticalBy(resizerId, incr);
      setResizerPosition(getResizerPosition() + incr);
    }
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

  private void setHasCellPreview(boolean hasCellPreview) {
    this.hasCellPreview = hasCellPreview;
  }

  private void setResizerBounds(int min, int max) {
    setResizerPositionMin(min);
    setResizerPositionMax(max);
  }

  private void setResizerCol(int resizerCol) {
    this.resizerCol = resizerCol;
  }

  private void setResizerModifiers(Modifiers resizerModifiers) {
    this.resizerModifiers = resizerModifiers;
  }
  
  private void setResizerMoveSensitivityMillis(int resizerMoveSensitivityMillis) {
    this.resizerMoveSensitivityMillis = resizerMoveSensitivityMillis;
  }

  private void setResizerPosition(int resizerPosition) {
    this.resizerPosition = resizerPosition;
  }

  private void setResizerPositionMax(int resizerPositionMax) {
    this.resizerPositionMax = resizerPositionMax;
  }

  private void setResizerPositionMin(int resizerPositionMin) {
    this.resizerPositionMin = resizerPositionMin;
  }

  private void setResizerRow(String resizerRow) {
    this.resizerRow = resizerRow;
  }

  private void setResizerShowSensitivityMillis(int resizerShowSensitivityMillis) {
    this.resizerShowSensitivityMillis = resizerShowSensitivityMillis;
  }

  private void setResizerStatus(ResizerMode resizerStatus) {
    this.resizerStatus = resizerStatus;
  }
  
  private void setResizing(boolean isResizing) {
    this.isResizing = isResizing;
  }
  
  private void setRowHeight(int row, int height) {
    Assert.isPositive(height);
    long id = getVisibleRowId(row);
    if (height == getBodyCellHeight()) {
      getResizedRows().remove(id);
    } else {
      getResizedRows().put(id, height);
    }
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
  
  private void setZIndex(int zIndex) {
    this.zIndex = zIndex;
  }
  
  private void showColumnResizer(Element cellElement, int col) {
    int x = cellElement.getOffsetLeft() + cellElement.getOffsetWidth();
    int y = cellElement.getOffsetTop();
    int h = cellElement.getOffsetHeight();

    int handleWidth = ResizerMode.HORIZONTAL.getHandlePx();
    int barWidth = ResizerMode.HORIZONTAL.getBarPx();
    int width = Math.max(handleWidth, barWidth);
    int left = Math.max(x - width / 2, 0);

    int top = (barWidth > 0) ? 0 : y;
    int height = (barWidth > 0) ? getChildrenHeight() : h;

    Element resizerElement = getResizerContainer();
    StyleUtils.setRectangle(resizerElement, left, top, width, height);
    StyleUtils.setZIndex(resizerElement, incrementZIndex());
    resizerElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER, STYLE_RESIZER_HORIZONTAL));

    if (barWidth > 0) {
      Element barElement = getResizerBar();
      StyleUtils.setRectangle(barElement, (width - barWidth) / 2, 0, barWidth, height);
      barElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER_BAR,
          STYLE_RESIZER_BAR_HORIZONTAL));
      if (handleWidth > 0) {
        StyleUtils.hideDisplay(barElement);
      } else {
        StyleUtils.unhideDisplay(barElement);
      }
    }

    if (handleWidth > 0) {
      Element handleElement = getResizerHandle();
      StyleUtils.setRectangle(handleElement, (width - handleWidth) / 2, y, handleWidth, h);
      handleElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER_HANDLE,
          STYLE_RESIZER_HANDLE_HORIZONTAL));
      StyleUtils.unhideDisplay(handleElement);
    }
    
    int absLeft = cellElement.getAbsoluteLeft();
    int cellWidth = cellElement.getOffsetWidth();
    int min = absLeft + Math.min(Math.max(getMinCellWidth(), 0), cellWidth);
    int max = absLeft + Math.max(getMaxCellWidth(), cellWidth);
    setResizerBounds(min, max);
    setResizerPosition(absLeft + cellWidth);

    StyleUtils.unhideDisplay(resizerElement);
    setResizerStatus(ResizerMode.HORIZONTAL);
    setResizerCol(col);
  }
  
  private void showRowResizer(Element cellElement, String rowIdx) {
    int x = cellElement.getOffsetLeft();
    int y = cellElement.getOffsetTop() + cellElement.getOffsetHeight();
    int w = cellElement.getOffsetWidth();

    int handleHeight = ResizerMode.VERTICAL.getHandlePx();
    int barHeight = ResizerMode.VERTICAL.getBarPx();
    int height = Math.max(handleHeight, barHeight);
    int top = Math.max(y - height / 2, 0);

    int left = (barHeight > 0) ? 0 : x;
    int width = (barHeight > 0) ? getRowWidth(rowIdx) : w;

    Element resizerElement = getResizerContainer();
    StyleUtils.setRectangle(resizerElement, left, top, width, height);
    StyleUtils.setZIndex(resizerElement, incrementZIndex());
    resizerElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER, STYLE_RESIZER_VERTICAL));

    if (barHeight > 0) {
      Element barElement = getResizerBar();
      StyleUtils.setRectangle(barElement, 0, (height - barHeight) / 2, width, barHeight);
      barElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER_BAR,
          STYLE_RESIZER_BAR_VERTICAL));
      if (handleHeight > 0) { 
        StyleUtils.hideDisplay(barElement);
      } else {
        StyleUtils.unhideDisplay(barElement);
      }
    }

    if (handleHeight > 0) {
      Element handleElement = getResizerHandle();
      StyleUtils.setRectangle(handleElement, x, (height - handleHeight) / 2, w, handleHeight);
      handleElement.setClassName(StyleUtils.buildClasses(STYLE_RESIZER_HANDLE,
          STYLE_RESIZER_HANDLE_VERTICAL));
      StyleUtils.unhideDisplay(handleElement);
    }

    int absTop = cellElement.getAbsoluteTop();
    int cellHeight = cellElement.getOffsetHeight();
    int min = absTop + Math.min(Math.max(getMinCellHeight(), 0), cellHeight);
    int max = absTop + Math.max(getMaxCellHeight(), cellHeight);
    setResizerBounds(min, max);
    setResizerPosition(absTop + cellHeight);
    
    StyleUtils.unhideDisplay(resizerElement);
    setResizerStatus(ResizerMode.VERTICAL);
    setResizerRow(rowIdx);
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

  private void startResizing(Event event) {
    setResizerModifiers(new Modifiers(event));
    
    if (getResizerStatus().getBarPx() > 0 && getResizerStatus().getHandlePx() > 0) {
      StyleUtils.unhideDisplay(resizerBarId);
    }
    
    getResizerMoveTimer().reset();
    setResizing(true);
  }
  
  private void stopResizing() {
    getResizerMoveTimer().stop();
    setResizing(false);
    hideResizer();

    setResizerModifiers(null);
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

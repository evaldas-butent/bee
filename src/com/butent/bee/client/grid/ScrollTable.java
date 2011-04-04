package com.butent.bee.client.grid;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.HasScrollHandlers;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.BeeStyle;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.DndEvent;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.HasAllDndHandlers;
import com.butent.bee.client.grid.FlexTable.FlexCellFormatter;
import com.butent.bee.client.grid.model.TableModel;
import com.butent.bee.client.grid.model.TableModel.Callback;
import com.butent.bee.client.grid.model.TableModelHelper.Request;
import com.butent.bee.client.grid.model.TableModelHelper.Response;
import com.butent.bee.client.grid.property.FooterProperty;
import com.butent.bee.client.grid.property.HeaderProperty;
import com.butent.bee.client.grid.render.FixedWidthGridBulkRenderer;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScrollTable extends ComplexPanel implements
    HasId, HasScrollHandlers, HasTableDefinition, RequiresResize {

  public static enum ResizePolicy {
    UNCONSTRAINED(false, false), FLOW(false, true), FIXED_WIDTH(true, false),
        FILL_WIDTH(true, true);

    private boolean isSacrificial;
    private boolean isFixedWidth;

    private ResizePolicy(boolean isFixedWidth, boolean isSacrificial) {
      this.isFixedWidth = isFixedWidth;
      this.isSacrificial = isSacrificial;
    }

    public boolean isFixedWidth() {
      return isFixedWidth;
    }

    public boolean isSacrificial() {
      return isSacrificial;
    }
  }

  protected static class ScrollTableCellView extends AbstractCellView {
    private ScrollTable table;

    public ScrollTableCellView(ScrollTable table) {
      super(table);
      this.table = table;
    }

    @Override
    public void setHorizontalAlignment(HorizontalAlignmentConstant align) {
      table.getDataTable().getCellFormatter().setHorizontalAlignment(
          getRowIndex(), getCellIndex(), align);
    }

    @Override
    public void setHTML(String html) {
      table.getDataTable().setHTML(getRowIndex(), getCellIndex(), html);
    }

    @Override
    public void setText(String text) {
      table.getDataTable().setText(getRowIndex(), getCellIndex(), text);
    }

    @Override
    public void setVerticalAlignment(VerticalAlignmentConstant align) {
      table.getDataTable().getCellFormatter().setVerticalAlignment(
          getRowIndex(), getCellIndex(), align);
    }

    @Override
    public void setWidget(Widget widget) {
      table.getDataTable().setWidget(getRowIndex(), getCellIndex(), widget);
    }
  }

  private static class ColumnHeaderInfo {
    private int rowSpan = 1;
    private Object header;
    private int columnId;

    public ColumnHeaderInfo(int id, Object header) {
      this.columnId = id;
      this.header = (header == null) ? "" : header;
    }

    public ColumnHeaderInfo(int id, Object header, int rowSpan) {
      this.columnId = id;
      this.header = (header == null) ? BeeConst.HTML_NBSP : header;
      this.rowSpan = rowSpan;
    }

    public int getColumnId() {
      return columnId;
    }

    public Object getHeader() {
      return header;
    }

    public int getRowSpan() {
      return rowSpan;
    }

    public void incrementRowSpan() {
      rowSpan++;
    }
  }

  private class DndWorker implements HasAllDndHandlers {
    private String sourceId;
    private String parentId;
    private String targetId;
    
    private int left;
    private int width;
    private int right;

    public boolean onDrag(DndEvent event) {
      int x = event.getClientX();
      if (x < left && lastScrollLeft > 0) {
        lastScrollLeft = Math.max(lastScrollLeft + x - left, 0);
        doScroll();
      } else if (x > right && left + width > right + 5 && lastScrollLeft < left + width - right) {
        lastScrollLeft = Math.min(lastScrollLeft + x - right, left + width - right);
        doScroll();
      }
      return true;
    }

    public boolean onDragEnd(DndEvent event) {
      event.getElement().removeClassName(BeeStyle.DND_SOURCE);
      
      if (BeeUtils.context(columnIdSeparator, sourceId)
          && BeeUtils.context(columnIdSeparator, targetId)) {
        int srcId = BeeUtils.toInt(BeeUtils.getSuffix(sourceId, columnIdSeparator));
        int dstId = BeeUtils.toInt(BeeUtils.getSuffix(targetId, columnIdSeparator));
        
        tableDefinition.moveColumnDef(srcId, dstId);
        reload();
      }
      
      return true;
    }

    public boolean onDragEnter(DndEvent event) {
      Element elem = event.getElement();
      if (isTarget(elem)) {
        elem.addClassName(BeeStyle.DND_OVER);
      }
      return true;
    }

    public boolean onDragLeave(DndEvent event) {
      Element elem = event.getElement();
      if (isTarget(elem)) {
        elem.removeClassName(BeeStyle.DND_OVER);
      }
      return true;
    }

    public boolean onDragOver(DndEvent event) {
      event.setDropEffect(DndEvent.EFFECT_MOVE);
      event.preventDefault();
      return false;
    }

    public boolean onDragStart(DndEvent event) {
      Element elem = event.getElement();
      sourceId = elem.getId();
      parentId = DomUtils.getParentId(elem, true);
      targetId = null;
      
      left = dataWrapper.getAbsoluteLeft();
      width = dataTable.getElement().getScrollWidth();
      right = dataWrapper.getAbsoluteRight();

      if (BeeUtils.isEmpty(sourceId) || BeeUtils.isEmpty(parentId)) {
        event.preventDefault();
      } else {
        elem.addClassName(BeeStyle.DND_SOURCE);
        event.setData(sourceId);
        event.setEffectAllowed(DndEvent.EFFECT_MOVE);
      }

      return true;
    }

    public boolean onDrop(DndEvent event) {
      Element elem = event.getElement();
      event.stopPropagation();

      if (isTarget(elem)) {
        elem.removeClassName(BeeStyle.DND_OVER);
        targetId = elem.getId();
      }
      return false;
    }

    private boolean isTarget(Element elem) {
      if (elem == null) {
        return false;
      }

      String id = elem.getId();
      if (!BeeUtils.context(columnIdSeparator, id) || BeeUtils.same(id, sourceId)) {
        return false;
      }

      return BeeUtils.same(DomUtils.getParentId(elem, true), parentId);
    }
  }

  private class MouseResizeWorker {
    private class ResizeCommand implements Scheduler.ScheduledCommand {
      @Override
      public void execute() {
        resize();
      }
    }

    private static final int RESIZE_CURSOR_WIDTH = 15;

    private Element curCell = null;
    private List<ColumnWidth> curCells = new ArrayList<ColumnWidth>();
    private int curCellIndex = 0;

    private int mouseXCurrent = 0;
    private int mouseXLast = 0;
    private int mouseXStart = 0;

    private boolean resizing = false;

    private int sacrificeCellIndex = -1;
    private List<ColumnWidth> sacrificeCells = new ArrayList<ColumnWidth>();

    private ScrollTable table = null;

    private ResizeCommand command = new ResizeCommand();

    public Element getCurrentCell() {
      return curCell;
    }

    public boolean isResizing() {
      return resizing;
    }

    public void resizeColumn(Event event) {
      mouseXCurrent = DOM.eventGetClientX(event);
      Scheduler.get().scheduleDeferred(command);
    }

    public boolean setCurrentCell(Event event) {
      Element cell = table.headerTable.getEventTargetCell(event);

      int clientX = event.getClientX();
      if (cell != null) {
        int absLeft = cell.getAbsoluteLeft() - Window.getScrollLeft();
        int absRight = absLeft + cell.getOffsetWidth();
        if (clientX < absRight - RESIZE_CURSOR_WIDTH || clientX > absRight) {
          cell = null;
        }
      }

      if (cell != curCell) {
        if (curCell != null) {
          curCell.getStyle().clearCursor();
        }

        curCell = cell;
        if (curCell != null) {
          curCellIndex = getCellIndex(curCell);
          if (curCellIndex < 0) {
            curCell = null;
            return false;
          }

          boolean resizable = false;
          int colSpan = DomUtils.getColSpan(cell);
          curCells = table.getColumnWidthInfo(curCellIndex, colSpan);
          for (ColumnWidth info : curCells) {
            if (info.getMaxWidth() != info.getMinWidth()) {
              resizable = true;
            }
          }
          if (!resizable) {
            curCell = null;
            curCells = null;
            return false;
          }

          curCell.getStyle().setCursor(Cursor.E_RESIZE);
        }
        return true;
      }

      return false;
    }

    public void setScrollTable(ScrollTable table) {
      this.table = table;
    }

    public void startResizing(Event event) {
      if (curCell != null) {
        resizing = true;
        mouseXStart = event.getClientX();
        mouseXLast = mouseXStart;
        mouseXCurrent = mouseXStart;

        int numColumns = table.getDataTable().getColumnCount();
        int colSpan = DomUtils.getColSpan(curCell);
        sacrificeCellIndex = curCellIndex + colSpan;
        sacrificeCells = table.getColumnWidthInfo(sacrificeCellIndex,
            numColumns - sacrificeCellIndex);

        DOM.setCapture(table.headerWrapper);
      }
    }

    public void stopResizing() {
      if (curCell != null && resizing) {
        resizing = false;

        DOM.releaseCapture(table.headerWrapper);
        curCell.getStyle().clearCursor();

        curCell = null;
        curCells = null;
        sacrificeCells = null;

        curCellIndex = 0;
        sacrificeCellIndex = -1;
      }
    }

    private int getCellIndex(Element cell) {
      int row = TableRowElement.as(DOM.getParent(cell)).getRowIndex() - 1;
      int column = TableCellElement.as(cell).getCellIndex();

      return table.headerTable.getColumnIndex(row, column) - table.getHeaderOffset();
    }

    private void resize() {
      if (mouseXLast != mouseXCurrent && resizing) {
        mouseXLast = mouseXCurrent;

        int totalDelta = mouseXCurrent - mouseXStart;
        totalDelta -= GridUtils.distributeWidth(curCells, totalDelta);

        if (table.resizePolicy.isSacrificial()) {
          int remaining = GridUtils.distributeWidth(sacrificeCells, -totalDelta);

          if (remaining != 0 && table.resizePolicy.isFixedWidth()) {
            totalDelta += remaining;
            GridUtils.distributeWidth(curCells, totalDelta);
          }

          table.applyNewColumnWidths(sacrificeCellIndex, sacrificeCells, true);
        }

        table.applyNewColumnWidths(curCellIndex, curCells, true);
        table.doScroll();
      }
    }
  }

  private class TableHeightInfo {
    private int headerTableHeight;
    private int dataTableHeight;
    private int footerTableHeight;

    public TableHeightInfo() {
      int totalHeight = getElement().getClientHeight();

      headerTableHeight = headerTable.getOffsetHeight();
      if (footerTable != null) {
        footerTableHeight = footerTable.getOffsetHeight();
      }
      dataTableHeight = totalHeight - headerTableHeight - footerTableHeight;
    }
  }

  private class VisibleRowsIterator implements Iterator<IsRow> {
    private Iterator<IsRow> rows;
    private int curRow;
    private int lastVisibleRow;

    public VisibleRowsIterator(Iterator<IsRow> rows, int firstRow,
        int firstVisibleRow, int lastVisibleRow) {
      this.curRow = firstRow;
      this.lastVisibleRow = lastVisibleRow;

      while (curRow < firstVisibleRow && rows.hasNext()) {
        rows.next();
        curRow++;
      }
      this.rows = rows;
    }

    public boolean hasNext() {
      return (curRow <= lastVisibleRow && rows.hasNext());
    }

    public IsRow next() {
      if (!hasNext()) {
        Assert.untouchable("no such element");
      }
      return rows.next();
    }

    public void remove() {
      Assert.unsupported("Remove not supported");
    }
  }

  public static final String DEFAULT_STYLE_NAME = "bee-ScrollTable";

  private TableDefinition tableDefinition = null;
  private FixedWidthGridBulkRenderer bulkRenderer = null;
  private TableModel tableModel;

  private boolean loading;
  private Request lastRequest = null;

  private List<IsRow> rowValues = new ArrayList<IsRow>();
  private RowView rowView = new RowView(new ScrollTableCellView(this));

  private List<ColumnDefinition> visibleColumns = new ArrayList<ColumnDefinition>();

  private Element absoluteElem;

  private FixedWidthGrid dataTable;
  private Element dataWrapper;

  private FixedWidthFlexTable footerTable = null;
  private Element footerWrapper = null;

  private FixedWidthFlexTable headerTable = null;
  private Element headerWrapper;

  private boolean headersObsolete;
  private int lastScrollLeft = 0;

  private MouseResizeWorker resizeWorker = new MouseResizeWorker();
  private DndWorker dndWorker = new DndWorker();

  private ResizePolicy resizePolicy = ResizePolicy.UNCONSTRAINED;

  private int defaultColumnWidth = 80;
  private int minColumnWidth = 1;
  private int maxColumnWidth = DomUtils.getClientWidth() / 2;

  private String columnIdSeparator = "_";

  private Callback loadCallback = new Callback() {
    public void onFailure(Throwable caught) {
      loading = false;
      BeeKeeper.getLog().severe("Loading Failure", caught);
    }

    public void onRowsReady(Request request, Response response) {
      if (lastRequest == request) {
        setData(request.getStartRow(), response.getRowValues());
        lastRequest = null;
      }
    }
  };

  private ScheduledCommand redrawCommand = new ScheduledCommand() {
    @Override
    public void execute() {
      redraw();
    }
  };

  public ScrollTable(TableModel tableModel, TableDefinition tableDefinition) {
    super();
    this.dataTable = new FixedWidthGrid(defaultColumnWidth);
    this.headerTable = new FixedWidthFlexTable(defaultColumnWidth);
    resizeWorker.setScrollTable(this);

    prepareTable(dataTable, "dataTable");
    prepareTable(headerTable, "headerTable");

    Element mainElem = DOM.createDiv();
    setElement(mainElem);
    setStylePrimaryName(DEFAULT_STYLE_NAME);
    mainElem.getStyle().setPadding(0, Unit.PX);
    mainElem.getStyle().setOverflow(Overflow.HIDDEN);
    mainElem.getStyle().setPosition(Position.RELATIVE);
    createId();

    absoluteElem = DOM.createDiv();
    absoluteElem.getStyle().setPosition(Position.ABSOLUTE);
    absoluteElem.getStyle().setTop(0, Unit.PX);
    absoluteElem.getStyle().setLeft(0, Unit.PX);
    absoluteElem.getStyle().setWidth(100, Unit.PCT);

    absoluteElem.getStyle().setPadding(0, Unit.PX);
    absoluteElem.getStyle().setMargin(0, Unit.PX);
    absoluteElem.getStyle().setBorderWidth(0, Unit.PX);
    absoluteElem.getStyle().setOverflow(Overflow.HIDDEN);

    setStyleName(absoluteElem, "absolute");
    DomUtils.createId(absoluteElem, "st-absolute");
    mainElem.appendChild(absoluteElem);

    headerWrapper = createWrapper("headerWrapper", "st-header");
    dataWrapper = createWrapper("dataWrapper", "st-data");

    setCellSpacing(0);
    setCellPadding(2);

    adoptTable(headerTable, headerWrapper, 0);
    adoptTable(dataTable, dataWrapper, 1);

    DOM.setEventListener(dataWrapper, this);
    DOM.sinkEvents(dataWrapper, Event.ONSCROLL);
    DOM.setEventListener(headerWrapper, this);
    DOM.sinkEvents(headerWrapper, Event.ONMOUSEMOVE | Event.ONMOUSEDOWN | Event.ONMOUSEUP);

    this.tableModel = tableModel;
    setTableDefinition(tableDefinition);
    refreshVisibleColumnDefinitions();
  }

  public HandlerRegistration addScrollHandler(ScrollHandler handler) {
    return addDomHandler(handler, ScrollEvent.getType());
  }

  public void createFooterTable() {
    setFooterTable(new FixedWidthFlexTable(defaultColumnWidth));
  }

  public void createId() {
    DomUtils.createId(this, "scroll-table");
  }

  public void fillWidth() {
    List<ColumnWidth> colWidths = getFillColumnWidths();
    applyNewColumnWidths(0, colWidths, false);
    doScroll();
  }

  public int getAbsoluteFirstRowIndex() {
    return 0;
  }

  public int getAbsoluteLastRowIndex() {
    return tableModel.getRowCount() - 1;
  }

  public int getAvailableWidth() {
    int clientWidth = absoluteElem.getClientWidth();
    int scrollbarWidth = DomUtils.getScrollbarWidth();
    clientWidth -= scrollbarWidth + 1;

    return clientWidth;
  }

  public int getCellPadding() {
    return dataTable.getCellPadding();
  }

  public int getCellSpacing() {
    return dataTable.getCellSpacing();
  }

  public String getColumnCaption(int column, boolean visible) {
    ColumnDefinition colDef = getColumnDefinition(column, visible);
    Object header = (colDef == null) ? null : colDef.getHeader();
    return BeeUtils.isEmpty(header) ? "Column " + column : BeeUtils.transform(header);
  }

  public int getColumnWidth(int column) {
    return dataTable.getColumnWidth(column);
  }

  public ColumnWidth getColumnWidthInfo(int column, boolean visible) {
    if (visible) {
      return getColumnWidthInfo(column);
    }
    int minWidth = getMinimumColumnWidth(column, visible);
    int maxWidth = getMaximumColumnWidth(column, visible);
    int prefWidth = getPreferredColumnWidth(column, visible);
    int curWidth, dataWidth, headerWidth, footerWidth;

    int idx = getVisibleColumnIndex(tableDefinition.getColumnId(column));
    if (idx >= 0) {
      curWidth = getColumnWidth(idx);
      dataWidth = getIdealDataColumnWidth(idx);
      headerWidth = getIdealHeaderColumnWidth(idx);
      footerWidth = getIdealFooterColumnWidth(idx);
    } else {
      curWidth = 0;
      dataWidth = 0;
      headerWidth = 0;
      footerWidth = 0;
    }

    return new ColumnWidth(minWidth, maxWidth, prefWidth,
        curWidth, dataWidth, headerWidth, footerWidth);
  }

  public FixedWidthGrid getDataTable() {
    return dataTable;
  }
  
  public int getDataTableWidth() {
    return dataTable.getElement().getScrollWidth();
  }

  public int getDefaultColumnWidth() {
    return defaultColumnWidth;
  }

  public FixedWidthFlexTable getFooterTable() {
    return footerTable;
  }

  public FixedWidthFlexTable getHeaderTable() {
    return headerTable;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public int getMaxColumnWidth() {
    return maxColumnWidth;
  }

  public int getMaximumColumnWidth(int column, boolean visible) {
    ColumnDefinition colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return maxColumnWidth;
    }
    return colDef.getMaximumColumnWidth(maxColumnWidth);
  }

  public int getMinColumnWidth() {
    return minColumnWidth;
  }

  public int getMinimumColumnWidth(int column, boolean visible) {
    ColumnDefinition colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return minColumnWidth;
    }
    int minWidth = colDef.getMinimumColumnWidth(minColumnWidth);
    return Math.max(minColumnWidth, minWidth);
  }

  public int getPreferredColumnWidth(int column, boolean visible) {
    ColumnDefinition colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return getDefaultColumnWidth();
    }
    return colDef.getPreferredColumnWidth(getDefaultColumnWidth());
  }

  public ResizePolicy getResizePolicy() {
    return resizePolicy;
  }

  public IsRow getRowValue(int row) {
    if (rowValues.size() <= row) {
      return null;
    }
    return rowValues.get(row);
  }

  public TableDefinition getTableDefinition() {
    return tableDefinition;
  }

  public TableModel getTableModel() {
    return tableModel;
  }

  public int getVisibleColumnIndex(int colId) {
    int idx = -1;
    for (int i = 0; i < visibleColumns.size(); i++) {
      if (visibleColumns.get(i).getColumnId() == colId) {
        idx = i;
        break;
      }
    }
    return idx;
  }

  public boolean isColumnSortable(int column, boolean visible) {
    ColumnDefinition colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return true;
    }
    return colDef.isColumnSortable(true);
  }

  public boolean isColumnTruncatable(int column, boolean visible) {
    ColumnDefinition colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return true;
    }
    return colDef.isColumnTruncatable(true);
  }

  public boolean isFooterColumnTruncatable(int column, boolean visible) {
    ColumnDefinition colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return true;
    }
    return colDef.isFooterTruncatable(true);
  }

  public boolean isHeaderColumnTruncatable(int column, boolean visible) {
    ColumnDefinition colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return true;
    }
    return colDef.isHeaderTruncatable(true);
  }

  public boolean isHeadersObsolete() {
    return headersObsolete;
  }

  public boolean isLoading() {
    return loading;
  }

  public void load() {
    loading = true;
    FixedWidthGrid data = getDataTable();

    if (bulkRenderer == null) {
      int rowCount = getAbsoluteLastRowIndex() - getAbsoluteFirstRowIndex() + 1;
      if (rowCount != data.getRowCount()) {
        data.resizeRows(rowCount);
      }
      data.clear(true);
    }

    int firstRow = getAbsoluteFirstRowIndex();
    int lastRow = tableModel.getRowCount();
    lastRequest = new Request(firstRow, lastRow);
    tableModel.requestRows(lastRequest, loadCallback);
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    Element target = DOM.eventGetTarget(event);

    switch (DOM.eventGetType(event)) {
      case Event.ONSCROLL:
        lastScrollLeft = dataWrapper.getScrollLeft();
        doScroll();
        if (dataWrapper.isOrHasChild(target)) {
          DomEvent.fireNativeEvent(event, this);
        }
        break;
      case Event.ONMOUSEDOWN:
        if (DOM.eventGetButton(event) != Event.BUTTON_LEFT) {
          return;
        }
        if (resizeWorker.getCurrentCell() != null) {
          event.preventDefault();
          event.stopPropagation();
          resizeWorker.startResizing(event);
        }
        break;
      case Event.ONMOUSEUP:
        if (DOM.eventGetButton(event) != Event.BUTTON_LEFT) {
          return;
        }
        if (resizeWorker.isResizing()) {
          resizeWorker.stopResizing();
          return;
        }

        if (!DomUtils.isTdElement(target) || EventUtils.hasModifierKey(event)) {
          ScrollTableConfig config = new ScrollTableConfig(this);
          config.show();
        }

        break;
      case Event.ONMOUSEMOVE:
        if (resizeWorker.isResizing()) {
          resizeWorker.resizeColumn(event);
        } else {
          resizeWorker.setCurrentCell(event);
        }
        break;
    }
  }

  public void onResize() {
    scheduleRedraw();
  }

  public void recalculateColumnWidths(boolean current, boolean fill) {
    if (!isAttached()) {
      return;
    }

    int width = fill ? getAvailableWidth() : getDataTableWidth();
    int cnt = dataTable.getColumnCount();
    if (cnt <= 0 || width < cnt) {
      return;
    }
    List<ColumnWidth> colWidths = getColumnWidthInfo(0, cnt);

    for (ColumnWidth info : colWidths) {
      int w = info.limit(current ? info.getCurWidth() : info.getPrefWidth());
      info.setDistrWidth(w);
      info.setCurWidth(0);
    }
    
    GridUtils.distributeWidth(colWidths, width);
    applyNewColumnWidths(0, colWidths, false);

    for (ColumnWidth info : colWidths) {
      info.setDistrWidth(0);
    }
    doScroll();
  }

  public void recalculateIdealColumnWidths() {
    FixedWidthFlexTable ht = getHeaderTable();
    FixedWidthFlexTable ft = getFooterTable();
    FixedWidthGrid dt = getDataTable();

    dt.recalculateIdealColumnWidthsSetup();
    ht.recalculateIdealColumnWidthsSetup();
    if (ft != null) {
      ft.recalculateIdealColumnWidthsSetup();
    }

    dt.recalculateIdealColumnWidthsImpl();
    ht.recalculateIdealColumnWidthsImpl();
    if (ft != null) {
      ft.recalculateIdealColumnWidthsImpl();
    }

    dt.recalculateIdealColumnWidthsTeardown();
    ht.recalculateIdealColumnWidthsTeardown();
    if (ft != null) {
      ft.recalculateIdealColumnWidthsTeardown();
    }
  }

  public void redraw() {
    if (!isAttached()) {
      return;
    }

    List<ColumnWidth> colWidths;
    if (resizePolicy == ResizePolicy.FILL_WIDTH) {
      colWidths = getFillColumnWidths();
    } else {
      colWidths = getBoundedColumnWidths(true);
    }
    applyNewColumnWidths(0, colWidths, true);

    resizeTablesVertically();
    doScroll();
  }

  public void reload() {
    load();
  }

  @Override
  public boolean remove(Widget child) {
    Assert.unsupported("This panel does not support remove()");
    return false;
  }

  public void scheduleRedraw() {
    Scheduler.get().scheduleDeferred(redrawCommand);
  }
  
  public void setBulkRenderer(FixedWidthGridBulkRenderer bulkRenderer) {
    this.bulkRenderer = bulkRenderer;
  }

  public void setCellPadding(int padding) {
    headerTable.setCellPadding(padding);
    dataTable.setCellPadding(padding);
    if (footerTable != null) {
      footerTable.setCellPadding(padding);
    }
  }

  public void setCellSpacing(int spacing) {
    headerTable.setCellSpacing(spacing);
    dataTable.setCellSpacing(spacing);
    if (footerTable != null) {
      footerTable.setCellSpacing(spacing);
    }
  }

  public void setColumnWidth(int column, int width) {
    int offset = getHeaderOffset();
    dataTable.setColumnWidth(column, width);
    headerTable.setColumnWidth(column + offset, width);
    if (footerTable != null) {
      footerTable.setColumnWidth(column + offset, width);
    }

    repositionSpacer();
    resizeTablesVertically();
    doScroll();
  }

  public void setDefaultColumnWidth(int defaultColumnWidth) {
    this.defaultColumnWidth = defaultColumnWidth;

    getDataTable().setDefaultColumnWidth(defaultColumnWidth);
    if (getHeaderTable() != null) {
      getHeaderTable().setDefaultColumnWidth(defaultColumnWidth);
    }
    if (getFooterTable() != null) {
      getFooterTable().setDefaultColumnWidth(defaultColumnWidth);
    }
  }

  public void setHeadersObsolete(boolean headersObsolete) {
    this.headersObsolete = headersObsolete;
  }

  @Override
  public void setHeight(String height) {
    super.setHeight(height);
    resizeTablesVertically();
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setMaxColumnWidth(int maxColumnWidth) {
    this.maxColumnWidth = maxColumnWidth;
  }

  public void setMinColumnWidth(int minColumnWidth) {
    this.minColumnWidth = minColumnWidth;
  }

  public void setResizePolicy(ResizePolicy resizePolicy) {
    this.resizePolicy = resizePolicy;
  }

  public void setRowValue(int row, IsRow value) {
    for (int i = rowValues.size(); i <= row; i++) {
      rowValues.add(null);
    }

    rowValues.set(row, value);
    refreshRow(row);
  }

  public void setTableDefinition(TableDefinition tableDefinition) {
    Assert.notNull(tableDefinition, "tableDefinition cannot be null");
    this.tableDefinition = tableDefinition;
  }

  protected Element createWrapper(String cssName, String idPrefix) {
    Element wrapper = DOM.createDiv();

    wrapper.getStyle().setWidth(100, Unit.PCT);
    wrapper.getStyle().setOverflow(Overflow.HIDDEN);
    wrapper.getStyle().setPadding(0, Unit.PX);
    wrapper.getStyle().setMargin(0, Unit.PX);
    wrapper.getStyle().setBorderWidth(0, Unit.PX);

    if (cssName != null) {
      setStyleName(wrapper, cssName);
    }
    DomUtils.createId(wrapper, idPrefix);

    return wrapper;
  }

  protected ColumnDefinition getColumnDefinition(int colIndex, boolean visible) {
    if (visible) {
      if (colIndex < visibleColumns.size()) {
        return visibleColumns.get(colIndex);
      }
      return null;
    } else {
      return tableDefinition.getColumnDefinition(colIndex);
    }
  }

  protected Element getDataWrapper() {
    return dataWrapper;
  }

  protected List<IsRow> getRowValues() {
    return rowValues;
  }

  protected List<ColumnDefinition> getVisibleColumnDefinitions() {
    return visibleColumns;
  }

  protected void onDataTableRendered() {
    if (headersObsolete) {
      EventUtils.removeDndHandler(dndWorker);
      refreshHeaderTable();
      refreshFooterTable();
      headersObsolete = false;
    }

    FixedWidthGrid data = getDataTable();
    
    data.clearIdealWidths();
    redraw();
    loading = false;
  }

  @Override
  protected void onLoad() {
    super.onLoad();

    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        load();
      }
    });
  }

  @Override
  protected void onUnload() {
    if (!BeeKeeper.getUi().isTemporaryDetach()) {
      EventUtils.removeDndHandler(dndWorker);
    }
    super.onUnload();
  }

  protected void refreshFooterTable() {
    if (getFooterTable() == null) {
      return;
    }
    List<List<ColumnHeaderInfo>> allInfos = new ArrayList<List<ColumnHeaderInfo>>();
    int columnCount = visibleColumns.size();
    int footerCounts[] = new int[columnCount];
    int maxFooterCount = 0;

    for (int col = 0; col < columnCount; col++) {
      ColumnDefinition colDef = visibleColumns.get(col);
      FooterProperty prop = colDef.getColumnProperty(FooterProperty.NAME);
      if (prop == null) {
        footerCounts[col] = 0;
        continue;
      }
      int footerCount = prop.getFooterCount();
      footerCounts[col] = footerCount;
      maxFooterCount = Math.max(maxFooterCount, footerCount);

      List<ColumnHeaderInfo> infos = new ArrayList<ColumnHeaderInfo>();
      ColumnHeaderInfo prev = null;

      for (int row = 0; row < footerCount; row++) {
        Object footer = prop.getFooter(row);
        if (prev != null && prev.header.equals(footer)) {
          prev.incrementRowSpan();
        } else {
          prev = new ColumnHeaderInfo(colDef.getColumnId(), footer);
          infos.add(prev);
        }
      }
      allInfos.add(infos);
    }

    if (maxFooterCount == 0) {
      return;
    }

    for (int col = 0; col < columnCount; col++) {
      int footerCount = footerCounts[col];
      if (footerCount < maxFooterCount) {
        allInfos.get(col).add(new ColumnHeaderInfo(-1, null, maxFooterCount - footerCount));
      }
    }
    refreshHeaderTable(getFooterTable(), allInfos, false);
  }

  protected void refreshHeaderTable() {
    if (getHeaderTable() == null) {
      return;
    }
    List<List<ColumnHeaderInfo>> allInfos = new ArrayList<List<ColumnHeaderInfo>>();
    int columnCount = visibleColumns.size();
    int headerCounts[] = new int[columnCount];
    int maxHeaderCount = 0;

    for (int col = 0; col < columnCount; col++) {
      ColumnDefinition colDef = visibleColumns.get(col);
      HeaderProperty prop = colDef.getColumnProperty(HeaderProperty.NAME);
      if (prop == null) {
        headerCounts[col] = 0;
        continue;
      }
      int headerCount = prop.getHeaderCount();
      headerCounts[col] = headerCount;
      maxHeaderCount = Math.max(maxHeaderCount, headerCount);

      List<ColumnHeaderInfo> infos = new ArrayList<ColumnHeaderInfo>();
      ColumnHeaderInfo prev = null;
      for (int row = 0; row < headerCount; row++) {
        Object header = prop.getHeader(row);
        if (prev != null && prev.header.equals(header)) {
          prev.incrementRowSpan();
        } else {
          prev = new ColumnHeaderInfo(colDef.getColumnId(), header);
          infos.add(0, prev);
        }
      }
      allInfos.add(infos);
    }

    if (maxHeaderCount == 0) {
      return;
    }

    for (int col = 0; col < columnCount; col++) {
      int headerCount = headerCounts[col];
      if (headerCount < maxHeaderCount) {
        allInfos.get(col).add(0, new ColumnHeaderInfo(-1, null, maxHeaderCount - headerCount));
      }
    }
    refreshHeaderTable(getHeaderTable(), allInfos, true);
  }

  protected void refreshVisibleColumnDefinitions() {
    List<ColumnDefinition> colDefs = new ArrayList<ColumnDefinition>(
        tableDefinition.getVisibleColumnDefinitions());
    if (!colDefs.equals(visibleColumns)) {
      visibleColumns = colDefs;
      headersObsolete = true;
    } else {
      for (ColumnDefinition colDef : colDefs) {
        if (colDef.isHeaderDynamic(false) || colDef.isFooterDynamic(false)) {
          headersObsolete = true;
          return;
        }
      }
    }
  }

  protected void removeAbsoluteRow(int row) {
    int firstRow = getAbsoluteFirstRowIndex();
    int lastRow = getAbsoluteLastRowIndex();
    if (row <= lastRow && row >= firstRow) {
      FixedWidthGrid data = getDataTable();
      int relativeRow = row - firstRow;
      if (relativeRow < data.getRowCount()) {
        data.removeRow(relativeRow);
      }
    }
  }

  protected void resizeTablesVertically() {
    applyTableWrapperSizes(getTableWrapperSizes());
    dataWrapper.getStyle().setWidth(100, Unit.PCT);
  }

  protected void setData(int firstRow, Iterator<IsRow> rows) {
    rowValues = new ArrayList<IsRow>();

    if (rows != null && rows.hasNext()) {
      int firstVisibleRow = getAbsoluteFirstRowIndex();
      int lastVisibleRow = getAbsoluteLastRowIndex();
      Iterator<IsRow> visibleIter = new VisibleRowsIterator(rows, firstRow,
          firstVisibleRow, lastVisibleRow);

      while (visibleIter.hasNext()) {
        rowValues.add(visibleIter.next());
      }

      refreshVisibleColumnDefinitions();

      if (bulkRenderer != null) {
        bulkRenderer.renderRows(rowValues.iterator());
        onDataTableRendered();
        return;
      }

      int rowCount = rowValues.size();
      int colCount = visibleColumns.size();
      getDataTable().resize(rowCount, colCount);

      tableDefinition.renderRows(0, rowValues.iterator(), rowView);
    }
    onDataTableRendered();
  }

  Element getAbsoluteElement() {
    return absoluteElem;
  }

  private void adoptTable(Widget table, Element wrapper, int index) {
    DOM.insertChild(absoluteElem, wrapper, index);
    add(table, wrapper);
  }

  private void applyNewColumnWidths(int startIndex, List<ColumnWidth> widths, boolean forced) {
    if (widths == null) {
      return;
    }

    int offset = getHeaderOffset();
    int numColumns = widths.size();

    for (int i = 0; i < numColumns; i++) {
      ColumnWidth info = widths.get(i);
      int newWidth = info.getNewWidth();

      if (forced || info.getCurWidth() != newWidth) {
        dataTable.setColumnWidth(startIndex + i, newWidth);
        headerTable.setColumnWidth(startIndex + i + offset, newWidth);
        if (footerTable != null) {
          footerTable.setColumnWidth(startIndex + i + offset, newWidth);
        }
      }
    }
    repositionSpacer();
  }

  private void applyTableWrapperSizes(TableHeightInfo sizes) {
    if (sizes == null) {
      return;
    }

    headerWrapper.getStyle().setHeight(sizes.headerTableHeight, Unit.PX);
    if (footerWrapper != null) {
      footerWrapper.getStyle().setHeight(sizes.footerTableHeight, Unit.PX);
    }
    dataWrapper.getStyle().setHeight(Math.max(sizes.dataTableHeight, 0), Unit.PX);
    dataWrapper.getStyle().setOverflow(Overflow.HIDDEN);
    dataWrapper.getStyle().setOverflow(Overflow.AUTO);
  }

  private void doScroll() {
    if (lastScrollLeft >= 0) {
      headerWrapper.setScrollLeft(lastScrollLeft);
      dataWrapper.setScrollLeft(lastScrollLeft);
      if (footerWrapper != null) {
        footerWrapper.setScrollLeft(lastScrollLeft);
      }
    }
  }

  private String generateHeaderId(int colId, boolean isHeader) {
    return DomUtils.createUniqueId(isHeader ? "ch" : "cf") + columnIdSeparator
        + BeeUtils.toString(colId);
  }

  private List<ColumnWidth> getBoundedColumnWidths(boolean boundsOnly) {
    if (!isAttached()) {
      return null;
    }

    int numColumns = dataTable.getColumnCount();
    int totalWidth = 0;
    List<ColumnWidth> colWidths = getColumnWidthInfo(0, numColumns);

    if (!boundsOnly) {
      for (ColumnWidth info : colWidths) {
        totalWidth += info.getCurWidth();
        info.setCurWidth(0);
      }
    }
    
    GridUtils.distributeWidth(colWidths, totalWidth);
    return colWidths;
  }

  private ColumnWidth getColumnWidthInfo(int column) {
    boolean visible = true;
    int minWidth = getMinimumColumnWidth(column, visible);
    int maxWidth = getMaximumColumnWidth(column, visible);
    int prefWidth = getPreferredColumnWidth(column, visible);

    int curWidth = getColumnWidth(column);
    int dataWidth = getIdealDataColumnWidth(column);
    int headerWidth = getIdealHeaderColumnWidth(column);
    int footerWidth = getIdealFooterColumnWidth(column);

    int w;

    if (dataWidth > 0 && !isColumnTruncatable(column, visible)) {
      w = dataWidth;
      if (maxWidth > 0) {
        w = Math.min(w, maxWidth);
      }
      minWidth = Math.max(minWidth, w);
    }

    if (headerWidth > 0 && !isHeaderColumnTruncatable(column, visible)) {
      w = headerWidth;
      if (maxWidth > 0) {
        w = Math.min(w, maxWidth);
      }
      minWidth = Math.max(minWidth, w);
    }

    if (footerWidth > 0 && !isFooterColumnTruncatable(column, visible)) {
      w = dataWidth;
      if (maxWidth > 0) {
        w = Math.min(w, maxWidth);
      }
      minWidth = Math.max(minWidth, w);
    }

    return new ColumnWidth(minWidth, maxWidth, prefWidth, 
        curWidth, dataWidth, headerWidth, footerWidth);
  }

  private List<ColumnWidth> getColumnWidthInfo(int column, int numColumns) {
    List<ColumnWidth> infos = new ArrayList<ColumnWidth>();
    for (int i = 0; i < numColumns; i++) {
      infos.add(getColumnWidthInfo(column + i));
    }
    return infos;
  }

  private List<ColumnWidth> getFillColumnWidths() {
    if (!isAttached()) {
      return null;
    }

    int width = getAvailableWidth();
    if (width <= 0) {
      return null;
    }
    int diff = width - getDataTableWidth();

    int numColumns = dataTable.getColumnCount();
    if (numColumns <= 0) {
      return null;
    }

    List<ColumnWidth> colWidthInfos = getColumnWidthInfo(0, numColumns);
    GridUtils.distributeWidth(colWidthInfos, diff);
    return colWidthInfos;
  }

  private int getHeaderOffset() {
    return 0;
  }
  
  private int getIdealDataColumnWidth(int column) {
    return getDataTable().getIdealColumnWidth(column);
  }

  private int getIdealFooterColumnWidth(int column) {
    if (getFooterTable() != null) {
      return getFooterTable().getIdealColumnWidth(column + getHeaderOffset());
    }
    return -1;
  }
  
  private int getIdealHeaderColumnWidth(int column) {
    return getHeaderTable().getIdealColumnWidth(column + getHeaderOffset());
  }

  private int getSpacerWidth(FixedWidthFlexTable table) {
    return BeeUtils.val(table.getElement().getStyle().getPaddingRight());
  }

  private TableHeightInfo getTableWrapperSizes() {
    if (!isAttached()) {
      return null;
    }

    return new TableHeightInfo();
  }

  private void prepareTable(Widget table, String cssName) {
    Element tableElem = table.getElement();
    tableElem.getStyle().setMargin(0, Unit.PX);
    tableElem.getStyle().setBorderWidth(0, Unit.PX);
    table.addStyleName(cssName);
  }

  private void refreshHeaderTable(FixedWidthFlexTable table,
      List<List<ColumnHeaderInfo>> allInfos, boolean isHeader) {
    if (visibleColumns == null) {
      return;
    }

    int rowCount = table.getRowCount();
    for (int i = 0; i < rowCount; i++) {
      table.removeRow(0);
    }

    int columnCount = allInfos.size();
    FlexCellFormatter formatter = table.getFlexCellFormatter();
    List<ColumnHeaderInfo> prevInfos = null;

    for (int col = 0; col < columnCount; col++) {
      List<ColumnHeaderInfo> infos = allInfos.get(col);
      int row = 0;
      for (ColumnHeaderInfo info : infos) {
        int rowSpan = info.getRowSpan();
        int cell = 0;
        if (table.getRowCount() > row) {
          cell = table.getCellCount(row);
        }

        if (prevInfos != null) {
          boolean headerAdded = false;
          int prevRow = 0;
          for (ColumnHeaderInfo prevInfo : prevInfos) {
            if (prevRow == row && info.equals(prevInfo)) {
              int colSpan = formatter.getColSpan(row, cell - 1);
              formatter.setColSpan(row, cell - 1, colSpan + 1);
              headerAdded = true;
              break;
            }
            prevRow += prevInfo.getRowSpan();
          }

          if (headerAdded) {
            row += rowSpan;
            continue;
          }
        }

        Object header = info.getHeader();
        if (header instanceof Widget) {
          table.setWidget(row, cell, (Widget) header);
        } else {
          String cap = header.toString();
          int id = info.getColumnId();
          table.setHTML(row, cell, cap);

          if (!BeeUtils.isEmpty(cap) && id >= 0) {
            Element elem = formatter.getElement(row, cell);
            elem.setId(generateHeaderId(id, isHeader));
            EventUtils.makeDndSource(elem, dndWorker);
            EventUtils.makeDndTarget(elem, dndWorker);
          }
        }

        if (rowSpan > 1) {
          formatter.setRowSpan(row, cell, rowSpan);
        }
        row += rowSpan;
      }
      prevInfos = infos;
    }
  }

  private void refreshRow(int rowIndex) {
    final IsRow rowValue = getRowValue(rowIndex);
    Iterator<IsRow> singleIterator = new Iterator<IsRow>() {
      private boolean nextCalled = false;

      public boolean hasNext() {
        return !nextCalled;
      }

      public IsRow next() {
        if (!hasNext()) {
          Assert.untouchable("no such element");
        }
        nextCalled = true;
        return rowValue;
      }

      public void remove() {
        Assert.untouchable();
      }
    };
    tableDefinition.renderRows(rowIndex, singleIterator, rowView);
  }

  private void repositionSpacer() {
    Element wrapper = dataWrapper;
    int spacerWidth = wrapper.getOffsetWidth() - dataWrapper.getClientWidth();
    resizeSpacer(headerTable, spacerWidth);

    if (footerTable != null) {
      resizeSpacer(footerTable, spacerWidth);
    }
  }

  private void resizeSpacer(FixedWidthFlexTable table, int spacerWidth) {
    if (spacerWidth >= 0 && spacerWidth != getSpacerWidth(table)) {
      table.getElement().getStyle().setPaddingRight(spacerWidth, Unit.PX);
    }
  }

  private void setFooterTable(FixedWidthFlexTable footerTable) {
    if (this.footerTable != null) {
      super.remove(this.footerTable);
      DOM.removeChild(absoluteElem, footerWrapper);
    }

    this.footerTable = footerTable;
    if (footerTable != null) {
      footerTable.setCellSpacing(getCellSpacing());
      footerTable.setCellPadding(getCellPadding());
      prepareTable(footerTable, "footerTable");

      if (footerWrapper == null) {
        footerWrapper = createWrapper("footerWrapper", "st-footer");
        DOM.setEventListener(footerWrapper, this);
        DOM.sinkEvents(footerWrapper, Event.ONMOUSEUP);
      }
      adoptTable(footerTable, footerWrapper, absoluteElem.getChildNodes().getLength());
    }
  }
}

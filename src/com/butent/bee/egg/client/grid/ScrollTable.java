package com.butent.bee.egg.client.grid;

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
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.BeeStyle;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.DndEvent;
import com.butent.bee.egg.client.event.EventUtils;
import com.butent.bee.egg.client.event.HasAllDndHandlers;
import com.butent.bee.egg.client.grid.FlexTable.FlexCellFormatter;
import com.butent.bee.egg.client.grid.SelectionGrid.SelectionPolicy;
import com.butent.bee.egg.client.grid.event.HasPageChangeHandlers;
import com.butent.bee.egg.client.grid.event.HasPageCountChangeHandlers;
import com.butent.bee.egg.client.grid.event.HasPagingFailureHandlers;
import com.butent.bee.egg.client.grid.event.HasRowInsertionHandlers;
import com.butent.bee.egg.client.grid.event.HasRowRemovalHandlers;
import com.butent.bee.egg.client.grid.event.HasRowValueChangeHandlers;
import com.butent.bee.egg.client.grid.event.PageChangeEvent;
import com.butent.bee.egg.client.grid.event.PageChangeHandler;
import com.butent.bee.egg.client.grid.event.PageCountChangeEvent;
import com.butent.bee.egg.client.grid.event.PageCountChangeHandler;
import com.butent.bee.egg.client.grid.event.PagingFailureEvent;
import com.butent.bee.egg.client.grid.event.PagingFailureHandler;
import com.butent.bee.egg.client.grid.event.RowCountChangeEvent;
import com.butent.bee.egg.client.grid.event.RowCountChangeHandler;
import com.butent.bee.egg.client.grid.event.RowInsertionEvent;
import com.butent.bee.egg.client.grid.event.RowInsertionHandler;
import com.butent.bee.egg.client.grid.event.RowRemovalEvent;
import com.butent.bee.egg.client.grid.event.RowRemovalHandler;
import com.butent.bee.egg.client.grid.event.RowSelectionEvent;
import com.butent.bee.egg.client.grid.event.RowSelectionHandler;
import com.butent.bee.egg.client.grid.event.RowValueChangeEvent;
import com.butent.bee.egg.client.grid.event.RowValueChangeHandler;
import com.butent.bee.egg.client.grid.event.TableEvent.Row;
import com.butent.bee.egg.client.grid.model.TableModel;
import com.butent.bee.egg.client.grid.model.TableModel.Callback;
import com.butent.bee.egg.client.grid.model.TableModelHelper.Request;
import com.butent.bee.egg.client.grid.model.TableModelHelper.Response;
import com.butent.bee.egg.client.grid.property.FooterProperty;
import com.butent.bee.egg.client.grid.property.HeaderProperty;
import com.butent.bee.egg.client.grid.render.FixedWidthGridBulkRenderer;
import com.butent.bee.egg.client.widget.BeeSimpleCheckBox;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ScrollTable<RowType> extends ComplexPanel implements
    HasId, HasScrollHandlers, HasTableDefinition<RowType>, HasPageCountChangeHandlers,
    HasPageChangeHandlers, HasPagingFailureHandlers, RequiresResize {

  public static enum ColumnResizePolicy {
    DISABLED, SINGLE_CELL, MULTI_CELL
  }

  public static enum ResizePolicy {
    UNCONSTRAINED(false, false), FLOW(false, true), FIXED_WIDTH(true, true),
        FILL_WIDTH(true, true);

    private boolean isSacrificial;
    private boolean isFixedWidth;

    private ResizePolicy(boolean isFixedWidth, boolean isSacrificial) {
      this.isFixedWidth = isFixedWidth;
      this.isSacrificial = isSacrificial;
    }

    private boolean isFixedWidth() {
      return isFixedWidth;
    }

    private boolean isSacrificial() {
      return isSacrificial;
    }
  }

  protected static class ScrollTableCellView<RowType> extends AbstractCellView<RowType> {
    private ScrollTable<RowType> table;

    public ScrollTableCellView(ScrollTable<RowType> table) {
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
    public void setStyleAttribute(String attr, String value) {
      table.getDataTable().getFixedWidthGridCellFormatter().getRawElement(
          getRowIndex(), getCellIndex()).getStyle().setProperty(attr, value);
    }

    @Override
    public void setStyleName(String stylename) {
      table.getDataTable().getCellFormatter().setStyleName(getRowIndex(),
          getCellIndex(), stylename);
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

  protected static class ScrollTableRowView<RowType> extends AbstractRowView<RowType> {
    private ScrollTable<RowType> table;

    public ScrollTableRowView(ScrollTable<RowType> table) {
      super(new ScrollTableCellView<RowType>(table));
      this.table = table;
    }

    @Override
    public void setStyleAttribute(String attr, String value) {
      table.getDataTable().getFixedWidthGridRowFormatter().getRawElement(
          getRowIndex()).getStyle().setProperty(attr, value);
    }

    @Override
    public void setStyleName(String stylename) {
      if (table.getDataTable().isRowSelected(getRowIndex())) {
        stylename += " selected";
      }
      table.getDataTable().getRowFormatter().setStyleName(getRowIndex(), stylename);
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
        reloadPage();
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
    private List<ColumnWidthInfo> curCells = new ArrayList<ColumnWidthInfo>();
    private int curCellIndex = 0;

    private int mouseXCurrent = 0;
    private int mouseXLast = 0;
    private int mouseXStart = 0;

    private boolean resizing = false;

    private int sacrificeCellIndex = -1;
    private List<ColumnWidthInfo> sacrificeCells = new ArrayList<ColumnWidthInfo>();

    private ScrollTable<?> table = null;

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
      Element cell = null;
      if (table.columnResizePolicy == ColumnResizePolicy.MULTI_CELL) {
        cell = table.headerTable.getEventTargetCell(event);
      } else if (table.columnResizePolicy == ColumnResizePolicy.SINGLE_CELL) {
        cell = table.headerTable.getEventTargetCell(event);
        if (cell != null && DomUtils.getColSpan(cell) > 1) {
          cell = null;
        }
      }

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
          for (ColumnWidthInfo info : curCells) {
            if (!info.hasMaximumWidth() || !info.hasMinimumWidth()
                || info.getMaximumWidth() != info.getMinimumWidth()) {
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

    public void setScrollTable(ScrollTable<?> table) {
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
        totalDelta -= table.columnResizer.distributeWidth(curCells, totalDelta);

        if (table.resizePolicy.isSacrificial()) {
          int remaining = table.columnResizer.distributeWidth(sacrificeCells, -totalDelta);

          if (remaining != 0 && table.resizePolicy.isFixedWidth()) {
            totalDelta += remaining;
            table.columnResizer.distributeWidth(curCells, totalDelta);
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

  private class TableWidthInfo {
    private int headerTableWidth;
    private int dataTableWidth;
    private int footerTableWidth;
    private int availableWidth;

    public TableWidthInfo(boolean includeSpacer) {
      availableWidth = getAvailableWidth();
      headerTableWidth = getTableWidth(headerTable, includeSpacer);
      dataTableWidth = dataTable.getElement().getScrollWidth();
      if (dataTableWidth <= 0) {
        dataTableWidth = availableWidth;
      }
      if (footerTable != null) {
        footerTableWidth = getTableWidth(footerTable, includeSpacer);
      }
    }
  }

  private class VisibleRowsIterator implements Iterator<RowType> {
    private Iterator<RowType> rows;
    private int curRow;
    private int lastVisibleRow;

    public VisibleRowsIterator(Iterator<RowType> rows, int firstRow,
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

    public RowType next() {
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

  private FixedWidthGridBulkRenderer<RowType> bulkRenderer = null;

  private TableDefinition<RowType> tableDefinition = null;
  private int currentPage = -1;

  private Request lastRequest = null;

  private boolean isCrossPageSelectionEnabled;

  private Set<RowType> selectedRowValues = new HashSet<RowType>();

  private boolean isPageLoading;

  private int oldPageCount;

  private int pageSize = 0;

  private List<RowType> rowValues = new ArrayList<RowType>();

  private AbstractRowView<RowType> rowView = new ScrollTableRowView<RowType>(this);

  private BeeSimpleCheckBox selectAllWidget;

  private TableModel<RowType> tableModel;

  private List<ColumnDefinition<RowType, ?>> visibleColumns =
      new ArrayList<ColumnDefinition<RowType, ?>>();

  private boolean headersObsolete;

  private Element absoluteElem;

  private ColumnResizer columnResizer = new ColumnResizer();

  private ColumnResizePolicy columnResizePolicy = ColumnResizePolicy.MULTI_CELL;

  private FixedWidthGrid dataTable;
  private Element dataWrapper;

  private FixedWidthFlexTable footerTable = null;
  private Element footerWrapper = null;

  private Element headerSpacer;

  private FixedWidthFlexTable headerTable = null;
  private Element headerWrapper;

  private int lastScrollLeft = 0;

  private MouseResizeWorker resizeWorker = new MouseResizeWorker();
  private DndWorker dndWorker = new DndWorker();

  private ResizePolicy resizePolicy = ResizePolicy.UNCONSTRAINED;

  private int defaultColumnWidth = 80;
  private int minColumnWidth = 10;
  private int maxColumnWidth = DomUtils.getClientWidth() / 2;

  private String columnIdSeparator = "_";

  private Callback<RowType> pagingCallback = new Callback<RowType>() {
    public void onFailure(Throwable caught) {
      isPageLoading = false;
      fireEvent(new PagingFailureEvent(caught));
    }

    public void onRowsReady(Request request, Response<RowType> response) {
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

  @SuppressWarnings("unchecked")
  public ScrollTable(TableModel<RowType> tableModel, TableDefinition<RowType> tableDefinition) {
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
    headerSpacer = createSpacer(headerTable);
    dataWrapper = createWrapper("dataWrapper", "st-data");

    setCellSpacing(0);
    setCellPadding(2);

    adoptTable(headerTable, headerWrapper, 0);
    adoptTable(dataTable, dataWrapper, 1);

    sinkEvents(Event.ONMOUSEOUT);
    DOM.setEventListener(dataWrapper, this);
    DOM.sinkEvents(dataWrapper, Event.ONSCROLL);
    DOM.setEventListener(headerWrapper, this);
    DOM.sinkEvents(headerWrapper, Event.ONMOUSEMOVE | Event.ONMOUSEDOWN | Event.ONMOUSEUP);

    this.tableModel = tableModel;
    setTableDefinition(tableDefinition);
    refreshVisibleColumnDefinitions();
    oldPageCount = getPageCount();

    tableModel.addRowCountChangeHandler(new RowCountChangeHandler() {
      public void onRowCountChange(RowCountChangeEvent event) {
        int pageCount = getPageCount();
        if (pageCount != oldPageCount) {
          fireEvent(new PageCountChangeEvent(oldPageCount, pageCount));
          oldPageCount = pageCount;
        }
      }
    });

    if (tableModel instanceof HasRowInsertionHandlers) {
      ((HasRowInsertionHandlers) tableModel).addRowInsertionHandler(new RowInsertionHandler() {
        public void onRowInsertion(RowInsertionEvent event) {
          insertAbsoluteRow(event.getRowIndex());
        }
      });
    }

    if (tableModel instanceof HasRowRemovalHandlers) {
      ((HasRowRemovalHandlers) tableModel).addRowRemovalHandler(new RowRemovalHandler() {
        public void onRowRemoval(RowRemovalEvent event) {
          removeAbsoluteRow(event.getRowIndex());
        }
      });
    }

    if (tableModel instanceof HasRowValueChangeHandlers) {
      ((HasRowValueChangeHandlers<RowType>) tableModel).addRowValueChangeHandler(new RowValueChangeHandler<RowType>() {
        public void onRowValueChange(RowValueChangeEvent<RowType> event) {
          int rowIndex = event.getRowIndex();
          if (rowIndex < getAbsoluteFirstRowIndex()
              || rowIndex > getAbsoluteLastRowIndex()) {
            return;
          }
          setRowValue(rowIndex - getAbsoluteFirstRowIndex(), event.getRowValue());
        }
      });
    }

    dataTable.addRowSelectionHandler(new RowSelectionHandler() {
      public void onRowSelection(RowSelectionEvent event) {
        if (isPageLoading) {
          return;
        }
        Set<Row> deselected = event.getDeselectedRows();
        for (Row row : deselected) {
          selectedRowValues.remove(getRowValue(row.getRowIndex()));
        }
        Set<Row> selected = event.getSelectedRows();
        for (Row row : selected) {
          selectedRowValues.add(getRowValue(row.getRowIndex()));
        }
      }
    });
  }

  public HandlerRegistration addPageChangeHandler(PageChangeHandler handler) {
    return addHandler(handler, PageChangeEvent.getType());
  }

  public HandlerRegistration addPageCountChangeHandler(PageCountChangeHandler handler) {
    return addHandler(handler, PageCountChangeEvent.getType());
  }

  public HandlerRegistration addPagingFailureHandler(PagingFailureHandler handler) {
    return addHandler(handler, PagingFailureEvent.getType());
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

  public Element createSpacer(FixedWidthFlexTable table) {
    resizeSpacer(table, 15);
    return null;
  }

  public void fillWidth() {
    List<ColumnWidthInfo> colWidths = getFillColumnWidths(null);
    applyNewColumnWidths(0, colWidths, false);
    doScroll();
  }

  public int getAbsoluteFirstRowIndex() {
    return currentPage * pageSize;
  }

  public int getAbsoluteLastRowIndex() {
    if (tableModel.getRowCount() < 0) {
      return (currentPage + 1) * pageSize - 1;
    } else if (pageSize == 0) {
      return tableModel.getRowCount() - 1;
    }
    return Math.min(tableModel.getRowCount(), (currentPage + 1) * pageSize) - 1;
  }

  public int getAvailableWidth() {
    int clientWidth = absoluteElem.getClientWidth();
    int scrollbarWidth = DomUtils.getScrollbarWidth();
    clientWidth -= scrollbarWidth + 1;

    return Math.max(clientWidth, -1);
  }

  public int getCellPadding() {
    return dataTable.getCellPadding();
  }

  public int getCellSpacing() {
    return dataTable.getCellSpacing();
  }

  public String getColumnCaption(int column, boolean visible) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column, visible);
    Object header = (colDef == null) ? null : colDef.getHeader();
    return BeeUtils.isEmpty(header) ? "Column " + column : BeeUtils.transform(header);
  }

  public ColumnResizePolicy getColumnResizePolicy() {
    return columnResizePolicy;
  }

  public int getColumnWidth(int column) {
    return dataTable.getColumnWidth(column);
  }

  public ColumnWidthInfo getColumnWidthInfo(int column, boolean visible) {
    if (visible) {
      return getColumnWidthInfo(column);
    }
    int minWidth = getMinimumColumnWidth(column, visible);
    int maxWidth = getMaximumColumnWidth(column, visible);
    int prefWidth = getPreferredColumnWidth(column, visible);
    int curWidth;

    int idx = getVisibleColumnIndex(tableDefinition.getColumnId(column));
    if (idx >= 0) {
      curWidth = getColumnWidth(idx);
    } else {
      curWidth = 0;
    }

    return new ColumnWidthInfo(minWidth, maxWidth, prefWidth, curWidth);
  }

  public int getCurrentPage() {
    return currentPage;
  }

  public FixedWidthGrid getDataTable() {
    return dataTable;
  }

  public int getDefaultColumnWidth() {
    return defaultColumnWidth;
  }

  public FixedWidthFlexTable getFooterTable() {
    return footerTable;
  }

  public Element getHeaderSpacer() {
    return headerSpacer;
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
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return maxColumnWidth;
    }
    return colDef.getMaximumColumnWidth(maxColumnWidth);
  }

  public int getMinColumnWidth() {
    return minColumnWidth;
  }

  public int getMinimumColumnWidth(int column, boolean visible) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return minColumnWidth;
    }
    int minWidth = colDef.getMinimumColumnWidth(minColumnWidth);
    return Math.max(minColumnWidth, minWidth);
  }

  public int getPageCount() {
    if (pageSize < 1) {
      return 1;
    } else {
      int numDataRows = tableModel.getRowCount();
      if (numDataRows < 0) {
        return -1;
      }
      return (int) Math.ceil(numDataRows / (pageSize + 0.0));
    }
  }

  public int getPageSize() {
    return pageSize;
  }

  public int getPreferredColumnWidth(int column, boolean visible) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return getDefaultColumnWidth();
    }
    return colDef.getPreferredColumnWidth(getDefaultColumnWidth());
  }

  public ResizePolicy getResizePolicy() {
    return resizePolicy;
  }

  public RowType getRowValue(int row) {
    if (rowValues.size() <= row) {
      return null;
    }
    return rowValues.get(row);
  }

  public Set<RowType> getSelectedRowValues() {
    return selectedRowValues;
  }

  public TableDefinition<RowType> getTableDefinition() {
    return tableDefinition;
  }

  public TableModel<RowType> getTableModel() {
    return tableModel;
  }

  public int getTableWidth(FixedWidthFlexTable table, boolean includeSpacer) {
    int scrollWidth = table.getElement().getScrollWidth();
    if (!includeSpacer) {
      int spacerWidth = getSpacerWidth(table);
      if (spacerWidth > 0) {
        scrollWidth -= spacerWidth;
      }
    }
    return scrollWidth;
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

  public void gotoFirstPage() {
    gotoPage(0, false);
  }

  public void gotoLastPage() {
    if (getPageCount() >= 0) {
      gotoPage(getPageCount(), false);
    }
  }

  public void gotoNextPage() {
    gotoPage(currentPage + 1, false);
  }

  public void gotoPage(int page, boolean forced) {
    int oldPage = currentPage;
    int numPages = getPageCount();
    if (numPages >= 0) {
      currentPage = Math.max(0, Math.min(page, numPages - 1));
    } else {
      currentPage = page;
    }

    if (currentPage != oldPage || forced) {
      isPageLoading = true;

      FixedWidthGrid data = getDataTable();
      data.deselectAllRows();
      if (!isCrossPageSelectionEnabled) {
        selectedRowValues = new HashSet<RowType>();
      }

      fireEvent(new PageChangeEvent(oldPage, currentPage));

      if (bulkRenderer == null) {
        int rowCount = getAbsoluteLastRowIndex() - getAbsoluteFirstRowIndex() + 1;
        if (rowCount != data.getRowCount()) {
          data.resizeRows(rowCount);
        }
        data.clear(true);
      }

      int firstRow = getAbsoluteFirstRowIndex();
      int lastRow = pageSize == 0 ? tableModel.getRowCount() : pageSize;
      lastRequest = new Request(firstRow, lastRow, data.getColumnSortList());
      tableModel.requestRows(lastRequest, pagingCallback);
    }
  }

  public void gotoPreviousPage() {
    gotoPage(currentPage - 1, false);
  }

  public boolean isColumnSortable(int column, boolean visible) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return true;
    }
    return colDef.isColumnSortable(true);
  }

  public boolean isColumnTruncatable(int column, boolean visible) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return true;
    }
    return colDef.isColumnTruncatable(true);
  }

  public boolean isCrossPageSelectionEnabled() {
    return isCrossPageSelectionEnabled;
  }

  public boolean isFooterColumnTruncatable(int column, boolean visible) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return true;
    }
    return colDef.isFooterTruncatable(true);
  }

  public boolean isHeaderColumnTruncatable(int column, boolean visible) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column, visible);
    if (colDef == null) {
      return true;
    }
    return colDef.isHeaderTruncatable(true);
  }

  public boolean isHeadersObsolete() {
    return headersObsolete;
  }

  public boolean isPageLoading() {
    return isPageLoading;
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

        if (selectAllWidget != null && selectAllWidget.getElement().isOrHasChild(target)) {
          if (selectAllWidget.isChecked()) {
            getDataTable().deselectAllRows();
          } else {
            getDataTable().selectAllRows();
          }
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

      case Event.ONMOUSEOUT:
        Element toElem = DOM.eventGetToElement(event);
        if (toElem == null || !dataWrapper.isOrHasChild(toElem)) {
          int clientX = event.getClientX() + Window.getScrollLeft();
          int clientY = event.getClientY() + Window.getScrollTop();
          int tableLeft = dataWrapper.getAbsoluteLeft();
          int tableTop = dataWrapper.getAbsoluteTop();
          int tableWidth = dataWrapper.getOffsetWidth();
          int tableHeight = dataWrapper.getOffsetHeight();
          int tableBottom = tableTop + tableHeight;
          int tableRight = tableLeft + tableWidth;
          if (clientX > tableLeft && clientX < tableRight && clientY > tableTop
              && clientY < tableBottom) {
            return;
          }

          dataTable.highlightCell(null);
        }
        break;
    }
  }

  public void onResize() {
    scheduleRedraw();
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

    TableWidthInfo redrawInfo = new TableWidthInfo(false);

    maybeRecalculateIdealColumnWidths();

    List<ColumnWidthInfo> colWidths = null;
    if (resizePolicy == ResizePolicy.FILL_WIDTH) {
      colWidths = getFillColumnWidths(redrawInfo);
    } else {
      colWidths = getBoundedColumnWidths(true);
    }
    applyNewColumnWidths(0, colWidths, true);

    resizeTablesVertically();
    doScroll();
  }

  public void reloadPage() {
    if (currentPage >= 0) {
      gotoPage(currentPage, true);
    } else {
      gotoPage(0, true);
    }
  }

  @Override
  public boolean remove(Widget child) {
    Assert.unsupported("This panel does not support remove()");
    return false;
  }

  public void repositionSpacer() {
    Element wrapper = dataWrapper;
    int spacerWidth = wrapper.getOffsetWidth() - dataWrapper.getClientWidth();
    resizeSpacer(headerTable, spacerWidth);

    if (footerTable != null) {
      resizeSpacer(footerTable, spacerWidth);
    }
  }

  public void resetColumnWidths() {
    applyNewColumnWidths(0, getBoundedColumnWidths(false), false);
    doScroll();
  }

  public void scheduleRedraw() {
    Scheduler.get().scheduleDeferred(redrawCommand);
  }
  
  public void setBulkRenderer(FixedWidthGridBulkRenderer<RowType> bulkRenderer) {
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

  public void setColumnResizePolicy(ColumnResizePolicy columnResizePolicy) {
    this.columnResizePolicy = columnResizePolicy;
  }

  public int setColumnWidth(int column, int width) {
    ColumnWidthInfo info = getColumnWidthInfo(column);
    if (info.hasMaximumWidth()) {
      width = Math.min(width, info.getMaximumWidth());
    }
    if (info.hasMinimumWidth()) {
      width = Math.max(width, info.getMinimumWidth());
    }

    if (resizePolicy.isSacrificial()) {
      int sacrificeColumn = column + 1;
      int numColumns = dataTable.getColumnCount();
      int remainingColumns = numColumns - sacrificeColumn;
      List<ColumnWidthInfo> infos = getColumnWidthInfo(sacrificeColumn, remainingColumns);

      int diff = width - getColumnWidth(column);
      int undistributed = columnResizer.distributeWidth(infos, -diff);

      applyNewColumnWidths(sacrificeColumn, infos, false);

      if (resizePolicy.isFixedWidth()) {
        width += undistributed;
      }
    }

    int offset = getHeaderOffset();
    dataTable.setColumnWidth(column, width);
    headerTable.setColumnWidth(column + offset, width);
    if (footerTable != null) {
      footerTable.setColumnWidth(column + offset, width);
    }

    repositionSpacer();
    resizeTablesVertically();
    doScroll();
    return width;
  }

  public void setCrossPageSelectionEnabled(boolean enabled) {
    if (isCrossPageSelectionEnabled != enabled) {
      this.isCrossPageSelectionEnabled = enabled;

      if (!enabled) {
        selectedRowValues = new HashSet<RowType>();
        Set<Integer> selectedRows = getDataTable().getSelectedRows();
        for (Integer selectedRow : selectedRows) {
          selectedRowValues.add(getRowValue(selectedRow));
        }
      }
    }
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

  public void setPageSize(int pageSize) {
    pageSize = Math.max(0, pageSize);
    this.pageSize = pageSize;

    int pageCount = getPageCount();
    if (pageCount != oldPageCount) {
      fireEvent(new PageCountChangeEvent(oldPageCount, pageCount));
      oldPageCount = pageCount;
    }

    if (currentPage >= 0) {
      gotoPage(currentPage, true);
    }
  }

  public void setResizePolicy(ResizePolicy resizePolicy) {
    this.resizePolicy = resizePolicy;
  }

  public void setRowValue(int row, RowType value) {
    for (int i = rowValues.size(); i <= row; i++) {
      rowValues.add(null);
    }

    rowValues.set(row, value);
    refreshRow(row);
  }

  public void setTableDefinition(TableDefinition<RowType> tableDefinition) {
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

  protected ColumnDefinition<RowType, ?> getColumnDefinition(int colIndex, boolean visible) {
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

  protected List<RowType> getRowValues() {
    return rowValues;
  }

  protected List<ColumnDefinition<RowType, ?>> getVisibleColumnDefinitions() {
    return visibleColumns;
  }

  protected void insertAbsoluteRow(int beforeRow) {
    int lastRow = getAbsoluteLastRowIndex() + 1;
    if (beforeRow <= lastRow) {
      int firstRow = getAbsoluteFirstRowIndex();
      if (beforeRow >= firstRow) {
        getDataTable().insertRow(beforeRow - firstRow);
      } else {
        getDataTable().insertRow(0);
      }
      if (getDataTable().getRowCount() > pageSize) {
        getDataTable().removeRow(pageSize);
      }
    }
  }

  protected void onDataTableRendered() {
    if (headersObsolete) {
      EventUtils.removeDndHandler(dndWorker);
      refreshHeaderTable();
      refreshFooterTable();
      headersObsolete = false;
    }

    FixedWidthGrid data = getDataTable();
    int rowCount = data.getRowCount();
    for (int i = 0; i < rowCount; i++) {
      if (selectedRowValues.contains(getRowValue(i))) {
        data.selectRow(i, false);
      }
    }

    data.clearIdealWidths();
    redraw();
    isPageLoading = false;
  }

  @Override
  protected void onLoad() {
    super.onLoad();

    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        gotoFirstPage();
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
      ColumnDefinition<RowType, ?> colDef = visibleColumns.get(col);
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
      ColumnDefinition<RowType, ?> colDef = visibleColumns.get(col);
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
    List<ColumnDefinition<RowType, ?>> colDefs = new ArrayList<ColumnDefinition<RowType, ?>>(
        tableDefinition.getVisibleColumnDefinitions());
    if (!colDefs.equals(visibleColumns)) {
      visibleColumns = colDefs;
      headersObsolete = true;
    } else {
      for (ColumnDefinition<RowType, ?> colDef : colDefs) {
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

  protected void setData(int firstRow, Iterator<RowType> rows) {
    getDataTable().deselectAllRows();
    rowValues = new ArrayList<RowType>();

    if (rows != null && rows.hasNext()) {
      int firstVisibleRow = getAbsoluteFirstRowIndex();
      int lastVisibleRow = getAbsoluteLastRowIndex();
      Iterator<RowType> visibleIter = new VisibleRowsIterator(rows, firstRow,
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

  void resizeSpacer(FixedWidthFlexTable table, int spacerWidth) {
    if (spacerWidth == getSpacerWidth(table)) {
      return;
    }

    table.getElement().getStyle().setPaddingRight(spacerWidth, Unit.PX);
  }

  private void adoptTable(Widget table, Element wrapper, int index) {
    DOM.insertChild(absoluteElem, wrapper, index);
    add(table, wrapper);
  }

  private void applyNewColumnWidths(int startIndex, List<ColumnWidthInfo> infos, boolean forced) {
    if (infos == null) {
      return;
    }

    int offset = getHeaderOffset();
    int numColumns = infos.size();

    for (int i = 0; i < numColumns; i++) {
      ColumnWidthInfo info = infos.get(i);
      int newWidth = info.getNewWidth();

      if (forced || info.getCurrentWidth() != newWidth) {
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

  private List<ColumnWidthInfo> getBoundedColumnWidths(boolean boundsOnly) {
    if (!isAttached()) {
      return null;
    }

    int numColumns = dataTable.getColumnCount();
    int totalWidth = 0;
    List<ColumnWidthInfo> colWidthInfos = getColumnWidthInfo(0, numColumns);

    if (!boundsOnly) {
      for (ColumnWidthInfo info : colWidthInfos) {
        totalWidth += info.getCurrentWidth();
        info.setCurrentWidth(0);
      }
    }
    columnResizer.distributeWidth(colWidthInfos, totalWidth);

    return colWidthInfos;
  }

  private ColumnWidthInfo getColumnWidthInfo(int column) {
    boolean visible = true;
    int minWidth = getMinimumColumnWidth(column, visible);
    int maxWidth = getMaximumColumnWidth(column, visible);
    int preferredWidth = getPreferredColumnWidth(column, visible);
    int curWidth = getColumnWidth(column);

    if (!isColumnTruncatable(column, visible)) {
      maybeRecalculateIdealColumnWidths();
      int idealWidth = getDataTable().getIdealColumnWidth(column);
      if (maxWidth > 0) {
        idealWidth = Math.min(idealWidth, maxWidth);
      }
      minWidth = Math.max(minWidth, idealWidth);
    }

    if (!isHeaderColumnTruncatable(column, visible)) {
      maybeRecalculateIdealColumnWidths();
      int idealWidth = getHeaderTable().getIdealColumnWidth(column + getHeaderOffset());
      if (maxWidth > 0) {
        idealWidth = Math.min(idealWidth, maxWidth);
      }
      minWidth = Math.max(minWidth, idealWidth);
    }

    if (footerTable != null && !isFooterColumnTruncatable(column, visible)) {
      maybeRecalculateIdealColumnWidths();
      int idealWidth = getFooterTable().getIdealColumnWidth(column + getHeaderOffset());
      if (maxWidth > 0) {
        idealWidth = Math.min(idealWidth, maxWidth);
      }
      minWidth = Math.max(minWidth, idealWidth);
    }

    return new ColumnWidthInfo(minWidth, maxWidth, preferredWidth, curWidth);
  }

  private List<ColumnWidthInfo> getColumnWidthInfo(int column, int numColumns) {
    List<ColumnWidthInfo> infos = new ArrayList<ColumnWidthInfo>();
    for (int i = 0; i < numColumns; i++) {
      infos.add(getColumnWidthInfo(column + i));
    }
    return infos;
  }

  private List<ColumnWidthInfo> getFillColumnWidths(TableWidthInfo info) {
    if (!isAttached()) {
      return null;
    }

    if (info == null) {
      info = new TableWidthInfo(false);
    }

    int clientWidth = info.availableWidth;
    if (clientWidth <= 0) {
      return null;
    }

    int diff = 0;
    int numColumns = 0;
    {
      int numHeaderCols = 0;
      int numDataCols = 0;
      int numFooterCols = 0;
      if (info.headerTableWidth > 0) {
        numHeaderCols = headerTable.getColumnCount() - getHeaderOffset();
      }
      if (info.dataTableWidth > 0) {
        numDataCols = dataTable.getColumnCount();
      }
      if (footerTable != null && info.footerTableWidth > 0) {
        numFooterCols = footerTable.getColumnCount() - getHeaderOffset();
      }

      if (numHeaderCols >= numDataCols && numHeaderCols >= numFooterCols) {
        numColumns = numHeaderCols;
        diff = clientWidth - info.headerTableWidth;
      } else if (numFooterCols >= numDataCols && numFooterCols >= numHeaderCols) {
        numColumns = numFooterCols;
        diff = clientWidth - info.footerTableWidth;
      } else if (numDataCols > 0) {
        numColumns = numDataCols;
        diff = clientWidth - info.dataTableWidth;
      }
    }
    if (numColumns <= 0) {
      return null;
    }

    List<ColumnWidthInfo> colWidthInfos = getColumnWidthInfo(0, numColumns);
    columnResizer.distributeWidth(colWidthInfos, diff);

    return colWidthInfos;
  }

  private int getHeaderOffset() {
    if (dataTable.getSelectionPolicy().hasInputColumn()) {
      return 1;
    }
    return 0;
  }

  private BeeSimpleCheckBox getSelectAllWidget() {
    if (selectAllWidget == null) {
      selectAllWidget = new BeeSimpleCheckBox();
    }
    return selectAllWidget;
  }

  private int getSpacerWidth(FixedWidthFlexTable table) {
    String paddingStr = table.getElement().getStyle().getPaddingRight();

    if (paddingStr == null || paddingStr.length() < 3) {
      return -1;
    }

    try {
      return Integer.parseInt(paddingStr.substring(0, paddingStr.length() - 2));
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private TableHeightInfo getTableWrapperSizes() {
    if (!isAttached()) {
      return null;
    }

    return new TableHeightInfo();
  }

  private void maybeRecalculateIdealColumnWidths() {
    if (!isAttached()) {
      return;
    }

    if (headerTable.isIdealColumnWidthsCalculated() && dataTable.isIdealColumnWidthsCalculated()
        && (footerTable == null || footerTable.isIdealColumnWidthsCalculated())) {
      return;
    }
    recalculateIdealColumnWidths();
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

    SelectionPolicy selectionPolicy = getDataTable().getSelectionPolicy();
    if (selectionPolicy.hasInputColumn()) {
      BeeSimpleCheckBox box = null;
      if (isHeader && getDataTable().getSelectionPolicy() == SelectionPolicy.CHECKBOX) {
        box = getSelectAllWidget();
        box.setChecked(false);
      }

      table.insertCell(0, 0);
      if (box != null) {
        table.setWidget(0, 0, box);
      } else {
        table.setHTML(0, 0, BeeConst.HTML_NBSP);
      }

      formatter.setRowSpan(0, 0, table.getRowCount());
      formatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
      table.setColumnWidth(0, getDataTable().getInputColumnWidth());
    }
  }

  private void refreshRow(int rowIndex) {
    final RowType rowValue = getRowValue(rowIndex);
    Iterator<RowType> singleIterator = new Iterator<RowType>() {
      private boolean nextCalled = false;

      public boolean hasNext() {
        return !nextCalled;
      }

      public RowType next() {
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

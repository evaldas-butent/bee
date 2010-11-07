package com.butent.bee.egg.client.pst;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
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
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.grid.BeeHtmlTable.BeeCellFormatter;
import com.butent.bee.egg.client.grid.ColumnWidthInfo;
import com.butent.bee.egg.client.grid.ScrollTableConfig;
import com.butent.bee.egg.client.pst.TableModelHelper.ColumnSortList;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScrollTable extends ComplexPanel implements
    HasId, HasScrollHandlers, RequiresResize {

  public static enum ColumnResizePolicy {
    DISABLED, SINGLE_CELL, MULTI_CELL
  }

  public static enum ResizePolicy {
    UNCONSTRAINED(false, false), FLOW(false, true), FIXED_WIDTH(true, true), FILL_WIDTH(true, true);

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

  public static enum ScrollPolicy {
    HORIZONTAL, BOTH, DISABLED
  }

  public static interface ScrollTableImages extends ClientBundle {
    ImageResource scrollTableAscending();
    ImageResource scrollTableDescending();
  }

  public static enum SortPolicy {
    DISABLED, SINGLE_CELL, MULTI_CELL
  }

  private static class MouseResizeWorker {
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

    private AbstractScrollTable table = null;
    
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

    public void setScrollTable(AbstractScrollTable table) {
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
        table.scrollTables(false);
      }
    }
  }

  private class TableHeightInfo {
    private int headerTableHeight;
    private int dataTableHeight;
    private int footerTableHeight;

    public TableHeightInfo() {
      Element elem = getElement();
      int totalHeight = elem.getClientHeight();
      while (totalHeight <= 0 && elem.getParentElement() != null) {
        elem = elem.getParentElement().cast();
        totalHeight = elem.getClientHeight();
      }

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

  public static final String DEFAULT_STYLE_NAME = "bee-ScrollTable";
  private Element absoluteElem;

  private ColumnResizer columnResizer = new ColumnResizer();
  private ColumnResizePolicy columnResizePolicy = ColumnResizePolicy.MULTI_CELL;

  private FixedWidthGrid dataTable;
  private Element dataWrapper;

  private ScrollTableImages images;

  private FixedWidthFlexTable footerTable = null;
  private Element footerWrapper = null;

  private Element headerSpacer;
  private FixedWidthFlexTable headerTable = null;
  private Element headerWrapper;

  private String lastHeight = null;
  private int lastScrollLeft;

  private MouseResizeWorker resizeWorker = GWT.create(MouseResizeWorker.class);

  private ResizePolicy resizePolicy = ResizePolicy.FILL_WIDTH;
  private ScrollPolicy scrollPolicy = ScrollPolicy.BOTH;
  private SortPolicy sortPolicy = SortPolicy.MULTI_CELL;

  private int sortedCellIndex = -1;
  private int sortedRowIndex = -1;

  private Element sortedColumnWrapper = null;

  public AbstractScrollTable(FixedWidthGrid dataTable, FixedWidthFlexTable headerTable) {
    this(dataTable, headerTable,
        (ScrollTableImages) GWT.create(ScrollTableImages.class));
  }

  public AbstractScrollTable(FixedWidthGrid dataTable,
      final FixedWidthFlexTable headerTable, ScrollTableImages images) {
    super();
    this.dataTable = dataTable;
    this.headerTable = headerTable;
    this.images = images;
    resizeWorker.setScrollTable(this);

    prepareTable(dataTable, "dataTable");
    prepareTable(headerTable, "headerTable");
    if (dataTable.getSelectionPolicy().hasInputColumn()) {
      headerTable.setColumnWidth(0, dataTable.getInputColumnWidth());
    }

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

    sortedColumnWrapper = DOM.createSpan();

    sinkEvents(Event.ONMOUSEOUT);
    DOM.setEventListener(dataWrapper, this);
    DOM.sinkEvents(dataWrapper, Event.ONSCROLL);
    DOM.setEventListener(headerWrapper, this);
    DOM.sinkEvents(headerWrapper, Event.ONMOUSEMOVE | Event.ONMOUSEDOWN | Event.ONMOUSEUP);

    dataTable.addColumnSortHandler(new ColumnSortHandler() {
      public void onColumnSorted(ColumnSortEvent event) {
        int column = -1;
        boolean ascending = true;
        ColumnSortList sortList = event.getColumnSortList();
        if (sortList != null) {
          column = sortList.getPrimaryColumn();
          ascending = sortList.isPrimaryAscending();
        }

        if (isColumnSortable(column)) {
          Element parent = DOM.getParent(sortedColumnWrapper);
          if (parent != null) {
            parent.removeChild(sortedColumnWrapper);
          }

          if (column < 0) {
            sortedCellIndex = -1;
            sortedRowIndex = -1;
          } else if (sortedCellIndex >= 0 && sortedRowIndex >= 0
              && headerTable.getRowCount() > sortedRowIndex
              && headerTable.getCellCount(sortedRowIndex) > sortedCellIndex) {
            BeeCellFormatter formatter = headerTable.getCellFormatter();
            Element td = formatter.getElement(sortedRowIndex, sortedCellIndex);
            applySortedColumnIndicator(td, ascending);
          }
        }
      }
    });
  }

  public HandlerRegistration addScrollHandler(ScrollHandler handler) {
    return addDomHandler(handler, ScrollEvent.getType());
  }

  public abstract void createId();

  public Element createSpacer(FixedWidthFlexTable table) {
    resizeSpacer(table, 15);
    return null;
  }

  public void fillWidth() {
    List<ColumnWidthInfo> colWidths = getFillColumnWidths(null);
    applyNewColumnWidths(0, colWidths, false);
    scrollTables(false);
  }

  public int getAvailableWidth() {
    Element elem = absoluteElem;
    int clientWidth = absoluteElem.getClientWidth();

    while (clientWidth <= 0 && elem.getParentElement() != null) {
      elem = elem.getParentElement().cast();
      clientWidth = elem.getClientWidth();
    }

    if (scrollPolicy == ScrollPolicy.BOTH) {
      int scrollbarWidth = DomUtils.getScrollbarWidth();
      clientWidth -= scrollbarWidth + 1;
    }
    return Math.max(clientWidth, -1);
  }

  public int getCellPadding() {
    return dataTable.getCellPadding();
  }

  public int getCellSpacing() {
    return dataTable.getCellSpacing();
  }

  public ColumnResizePolicy getColumnResizePolicy() {
    return columnResizePolicy;
  }

  public int getColumnWidth(int column) {
    return dataTable.getColumnWidth(column);
  }

  public FixedWidthGrid getDataTable() {
    return dataTable;
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

  public abstract int getMaximumColumnWidth(int column);

  public abstract int getMinimumColumnWidth(int column);

  public abstract int getPreferredColumnWidth(int column);

  public ResizePolicy getResizePolicy() {
    return resizePolicy;
  }

  public ScrollPolicy getScrollPolicy() {
    return scrollPolicy;
  }

  public SortPolicy getSortPolicy() {
    return sortPolicy;
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

  public abstract boolean isColumnSortable(int column);

  public abstract boolean isColumnTruncatable(int column);

  public abstract boolean isFooterColumnTruncatable(int column);

  public abstract boolean isHeaderColumnTruncatable(int column);

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    Element target = DOM.eventGetTarget(event);

    switch (DOM.eventGetType(event)) {
      case Event.ONSCROLL:
        lastScrollLeft = dataWrapper.getScrollLeft();
        scrollTables(false);
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

        Element cellElem = headerTable.getEventTargetCell(event);
        int column = -1;
        if (cellElem != null) {
          int rowIdx = TableRowElement.as(cellElem.getParentElement()).getRowIndex() - 1;
          int cellIdx = TableCellElement.as(cellElem).getCellIndex();
          column = headerTable.getColumnIndex(rowIdx, cellIdx) - getHeaderOffset();
        }
        
        if (BeeUtils.betweenExclusive(column, 0, dataTable.getColumnCount())) {
          
        } else {
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
    redraw();
  }

  public void recalculateIdealColumnWidths(AbstractScrollTable scrollTable) {
    FixedWidthFlexTable ht = scrollTable.getHeaderTable();
    FixedWidthFlexTable ft = scrollTable.getFooterTable();
    FixedWidthGrid dt = scrollTable.getDataTable();

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

    scrollTables(false);
  }

  @Override
  public boolean remove(Widget child) {
    Assert.unsupported("This panel does not support remove()");
    return false;
  }

  public void repositionSpacer(AbstractScrollTable scrollTable, boolean force) {
    if (!force && scrollTable.scrollPolicy != ScrollPolicy.BOTH) {
      return;
    }

    Element wrapper = scrollTable.dataWrapper;
    int spacerWidth = wrapper.getOffsetWidth() - dataWrapper.getClientWidth();
    resizeSpacer(scrollTable.headerTable, spacerWidth);

    if (scrollTable.footerTable != null) {
      resizeSpacer(scrollTable.footerTable, spacerWidth);
    }
  }

  public void resetColumnWidths() {
    applyNewColumnWidths(0, getBoundedColumnWidths(false), false);
    scrollTables(false);
  }

  public void setCellPadding(int padding) {
    headerTable.setCellPadding(padding);
    dataTable.setCellPadding(padding);
    if (footerTable != null) {
      footerTable.setCellPadding(padding);
    }
    redraw();
  }

  public void setCellSpacing(int spacing) {
    headerTable.setCellSpacing(spacing);
    dataTable.setCellSpacing(spacing);
    if (footerTable != null) {
      footerTable.setCellSpacing(spacing);
    }
    redraw();
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
      List<ColumnWidthInfo> infos = getColumnWidthInfo(sacrificeColumn,
          remainingColumns);

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

    repositionSpacer(this, false);
    resizeTablesVertically();
    scrollTables(false);
    return width;
  }

  public void setFooterTable(FixedWidthFlexTable footerTable) {
    if (this.footerTable != null) {
      super.remove(this.footerTable);
      DOM.removeChild(absoluteElem, footerWrapper);
    }

    this.footerTable = footerTable;
    if (footerTable != null) {
      footerTable.setCellSpacing(getCellSpacing());
      footerTable.setCellPadding(getCellPadding());
      prepareTable(footerTable, "footerTable");
      if (dataTable.getSelectionPolicy().hasInputColumn()) {
        footerTable.setColumnWidth(0, dataTable.getInputColumnWidth());
      }

      if (footerWrapper == null) {
        footerWrapper = createWrapper("footerWrapper", "st-footer");
        DOM.setEventListener(footerWrapper, this);
        DOM.sinkEvents(footerWrapper, Event.ONMOUSEUP);
      }

      adoptTable(footerTable, footerWrapper, absoluteElem.getChildNodes().getLength());
    }
    redraw();
  }

  @Override
  public void setHeight(String height) {
    this.lastHeight = height;
    super.setHeight(height);
    resizeTablesVertically();
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setResizePolicy(ResizePolicy resizePolicy) {
    this.resizePolicy = resizePolicy;
    redraw();
  }

  public void setScrollPolicy(ScrollPolicy scrollPolicy) {
    if (scrollPolicy == this.scrollPolicy) {
      return;
    }
    this.scrollPolicy = scrollPolicy;

    headerWrapper.getStyle().clearHeight();
    dataWrapper.getStyle().clearHeight();
    if (footerWrapper != null) {
      footerWrapper.getStyle().clearHeight();
    }

    if (scrollPolicy == ScrollPolicy.DISABLED) {
      BeeKeeper.getStyle().autoHeight(dataWrapper);
      dataWrapper.getStyle().clearOverflow();
    } else if (scrollPolicy == ScrollPolicy.HORIZONTAL) {
      BeeKeeper.getStyle().autoHeight(dataWrapper);
      dataWrapper.getStyle().setOverflow(Overflow.AUTO);
    } else if (scrollPolicy == ScrollPolicy.BOTH) {
      if (lastHeight != null) {
        super.setHeight(lastHeight);
      } else {
        getElement().getStyle().clearHeight();
      }
      dataWrapper.getStyle().setOverflow(Overflow.AUTO);
    }

    repositionSpacer(this, true);
    redraw();
  }

  public void setSortPolicy(SortPolicy sortPolicy) {
    this.sortPolicy = sortPolicy;
    applySortedColumnIndicator(null, true);
  }

  protected void applySortedColumnIndicator(Element tdElem, boolean ascending) {
    if (tdElem == null) {
      Element parent = DOM.getParent(sortedColumnWrapper);
      if (parent != null) {
        parent.removeChild(sortedColumnWrapper);
        headerTable.clearIdealWidths();
      }
      return;
    }

    tdElem.appendChild(sortedColumnWrapper);
    if (ascending) {
      sortedColumnWrapper.setInnerHTML(BeeConst.HTML_NBSP
          + AbstractImagePrototype.create(images.scrollTableAscending()).getHTML());
    } else {
      sortedColumnWrapper.setInnerHTML(BeeConst.HTML_NBSP
          + AbstractImagePrototype.create(images.scrollTableDescending()).getHTML());
    }
    sortedRowIndex = -1;
    sortedCellIndex = -1;

    headerTable.clearIdealWidths();
    redraw();
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

  protected Element getDataWrapper() {
    return dataWrapper;
  }

  @Override
  protected void onLoad() {
    redraw();
  }

  protected void resizeTablesVertically() {
    if (scrollPolicy == ScrollPolicy.DISABLED) {
      dataWrapper.getStyle().setOverflow(Overflow.AUTO);
      dataWrapper.getStyle().clearOverflow();
      int height = Math.max(1, absoluteElem.getOffsetHeight());
      getElement().getStyle().setHeight(height, Unit.PX);

    } else if (scrollPolicy == ScrollPolicy.HORIZONTAL) {
      dataWrapper.getStyle().setOverflow(Overflow.HIDDEN);
      dataWrapper.getStyle().setOverflow(Overflow.AUTO);
      int height = Math.max(1, absoluteElem.getOffsetHeight());
      getElement().getStyle().setHeight(height, Unit.PX);

    } else {
      applyTableWrapperSizes(getTableWrapperSizes());
      dataWrapper.getStyle().setWidth(100, Unit.PCT);
    }
  }

  protected void scrollTables(boolean baseHeader) {
    if (scrollPolicy == ScrollPolicy.DISABLED) {
      return;
    }

    if (lastScrollLeft >= 0) {
      headerWrapper.setScrollLeft(lastScrollLeft);
      if (baseHeader) {
        dataWrapper.setScrollLeft(lastScrollLeft);
      }
      if (footerWrapper != null) {
        footerWrapper.setScrollLeft(lastScrollLeft);
      }
    }
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
    repositionSpacer(this, false);
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
    int minWidth = getMinimumColumnWidth(column);
    int maxWidth = getMaximumColumnWidth(column);
    int preferredWidth = getPreferredColumnWidth(column);
    int curWidth = getColumnWidth(column);

    if (!isColumnTruncatable(column)) {
      maybeRecalculateIdealColumnWidths();
      int idealWidth = getDataTable().getIdealColumnWidth(column);
      if (maxWidth != MaximumWidthProperty.NO_MAXIMUM_WIDTH) {
        idealWidth = Math.min(idealWidth, maxWidth);
      }
      minWidth = Math.max(minWidth, idealWidth);
    }
    if (!isHeaderColumnTruncatable(column)) {
      maybeRecalculateIdealColumnWidths();
      int idealWidth = getHeaderTable().getIdealColumnWidth(
          column + getHeaderOffset());
      if (maxWidth != MaximumWidthProperty.NO_MAXIMUM_WIDTH) {
        idealWidth = Math.min(idealWidth, maxWidth);
      }
      minWidth = Math.max(minWidth, idealWidth);
    }
    if (footerTable != null && !isFooterColumnTruncatable(column)) {
      maybeRecalculateIdealColumnWidths();
      int idealWidth = getFooterTable().getIdealColumnWidth(
          column + getHeaderOffset());
      if (maxWidth != MaximumWidthProperty.NO_MAXIMUM_WIDTH) {
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

    if (scrollPolicy == ScrollPolicy.DISABLED
        || scrollPolicy == ScrollPolicy.HORIZONTAL) {
      return null;
    }

    return new TableHeightInfo();
  }

  private void maybeRecalculateIdealColumnWidths() {
    if (!isAttached()) {
      return;
    }

    if (headerTable.isIdealColumnWidthsCalculated()
        && dataTable.isIdealColumnWidthsCalculated()
        && (footerTable == null || footerTable.isIdealColumnWidthsCalculated())) {
      return;
    }

    recalculateIdealColumnWidths(this);
  }

  private void prepareTable(Widget table, String cssName) {
    Element tableElem = table.getElement();
    tableElem.getStyle().setMargin(0, Unit.PX);
    tableElem.getStyle().setBorderWidth(0, Unit.PX);
    table.addStyleName(cssName);
  }
}

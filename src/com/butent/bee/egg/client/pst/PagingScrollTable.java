package com.butent.bee.egg.client.pst;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

import com.butent.bee.egg.client.grid.BeeFlexTable.BeeFlexCellFormatter;
import com.butent.bee.egg.client.grid.BeeHtmlTable;
import com.butent.bee.egg.client.grid.BeeHtmlTable.BeeCell;
import com.butent.bee.egg.client.pst.CellEditor.CellEditInfo;
import com.butent.bee.egg.client.pst.SelectionGrid.SelectionPolicy;
import com.butent.bee.egg.client.pst.SortableGrid.ColumnSorter;
import com.butent.bee.egg.client.pst.SortableGrid.ColumnSorterCallback;
import com.butent.bee.egg.client.pst.TableDefinition.AbstractCellView;
import com.butent.bee.egg.client.pst.TableDefinition.AbstractRowView;
import com.butent.bee.egg.client.pst.TableEvent.Row;
import com.butent.bee.egg.client.pst.TableModel.Callback;
import com.butent.bee.egg.client.pst.TableModelHelper.ColumnSortList;
import com.butent.bee.egg.client.pst.TableModelHelper.Request;
import com.butent.bee.egg.client.pst.TableModelHelper.Response;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PagingScrollTable<RowType> extends AbstractScrollTable implements
    HasTableDefinition<RowType>, HasPageCountChangeHandlers,
    HasPageLoadHandlers, HasPageChangeHandlers, HasPagingFailureHandlers {

  protected static class PagingScrollTableCellView<RowType> extends
      AbstractCellView<RowType> {
    private PagingScrollTable<RowType> table;

    public PagingScrollTableCellView(PagingScrollTable<RowType> table) {
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

  protected static class PagingScrollTableRowView<RowType> extends
      AbstractRowView<RowType> {
    private PagingScrollTable<RowType> table;

    public PagingScrollTableRowView(PagingScrollTable<RowType> table) {
      super(new PagingScrollTableCellView<RowType>(table));
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

    public ColumnHeaderInfo(Object header) {
      this.header = (header == null) ? "" : header;
    }

    public ColumnHeaderInfo(Object header, int rowSpan) {
      this.header = (header == null) ? BeeConst.HTML_NBSP : header;
      this.rowSpan = rowSpan;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) {
        return false;
      }
      if (o instanceof ColumnHeaderInfo) {
        ColumnHeaderInfo info = (ColumnHeaderInfo) o;
        return (rowSpan == info.rowSpan) && header.equals(info.header);
      }
      return false;
    }

    public Object getHeader() {
      return header;
    }

    public int getRowSpan() {
      return rowSpan;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    public void incrementRowSpan() {
      rowSpan++;
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

  private FixedWidthGridBulkRenderer<RowType> bulkRenderer = null;

  private SimplePanel emptyTableWidgetWrapper = new SimplePanel();

  private TableDefinition<RowType> tableDefinition = null;

  private int currentPage = -1;

  private Request lastRequest = null;

  private boolean isCrossPageSelectionEnabled;

  private Set<RowType> selectedRowValues = new HashSet<RowType>();

  private boolean isFooterGenerated;
  private boolean isHeaderGenerated;

  private boolean isPageLoading;

  private int oldPageCount;
  private int pageSize = 0;

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

  private List<RowType> rowValues = new ArrayList<RowType>();

  private AbstractRowView<RowType> rowView = new PagingScrollTableRowView<RowType>(this);

  private Widget selectAllWidget;

  private TableModel<RowType> tableModel;

  private RendererCallback tableRendererCallback = new RendererCallback() {
    public void onRendered() {
      onDataTableRendered();
    }
  };

  private List<ColumnDefinition<RowType, ?>> visibleColumns = new ArrayList<ColumnDefinition<RowType, ?>>();

  private boolean headersObsolete;

  public PagingScrollTable(TableModel<RowType> tableModel,
      TableDefinition<RowType> tableDefinition) {
    this(tableModel, new FixedWidthGrid(), new FixedWidthFlexTable(),
        tableDefinition);
    isHeaderGenerated = true;
    isFooterGenerated = true;
  }

  public PagingScrollTable(TableModel<RowType> tableModel,
      FixedWidthGrid dataTable, FixedWidthFlexTable headerTable,
      TableDefinition<RowType> tableDefinition) {
    this(tableModel, dataTable, headerTable, tableDefinition,
        GWT.<ScrollTableImages> create(ScrollTableImages.class));
  }

  @SuppressWarnings("unchecked")
  public PagingScrollTable(TableModel<RowType> tableModel,
      FixedWidthGrid dataTable, FixedWidthFlexTable headerTable,
      TableDefinition<RowType> tableDefinition, ScrollTableImages images) {
    super(dataTable, headerTable, images);
    this.tableModel = tableModel;
    setTableDefinition(tableDefinition);
    refreshVisibleColumnDefinitions();
    oldPageCount = getPageCount();

    emptyTableWidgetWrapper.getElement().getStyle().setWidth(100, Unit.PCT);
    emptyTableWidgetWrapper.getElement().getStyle().setOverflow(Overflow.HIDDEN);
    emptyTableWidgetWrapper.getElement().getStyle().setBorderWidth(0, Unit.PX);
    emptyTableWidgetWrapper.getElement().getStyle().setMargin(0, Unit.PX);
    emptyTableWidgetWrapper.getElement().getStyle().setPadding(0, Unit.PX);
    insert(emptyTableWidgetWrapper, getAbsoluteElement(), 2, true);
    setEmptyTableWidgetVisible(false);

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
          setRowValue(rowIndex - getAbsoluteFirstRowIndex(),
              event.getRowValue());
        }
      });
    }

    dataTable.addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (event.getSource() instanceof BeeHtmlTable) {
          BeeCell cell = ((BeeHtmlTable) event.getSource()).getCellForEvent(event);
          if (cell != null) {
            editCell(cell.getRowIndex(), cell.getCellIndex());
          }
        }
      }
    }, ClickEvent.getType());

    if (dataTable.getColumnSorter() == null) {
      ColumnSorter sorter = new ColumnSorter() {
        @Override
        public void onSortColumn(SortableGrid grid, ColumnSortList sortList,
            ColumnSorterCallback callback) {
          reloadPage();
          callback.onSortingComplete();
        }
      };
      dataTable.setColumnSorter(sorter);
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

  public HandlerRegistration addPageLoadHandler(PageLoadHandler handler) {
    return addHandler(handler, PageLoadEvent.getType());
  }

  public HandlerRegistration addPagingFailureHandler(PagingFailureHandler handler) {
    return addHandler(handler, PagingFailureEvent.getType());
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

  public int getCurrentPage() {
    return currentPage;
  }

  public Widget getEmptyTableWidget() {
    return emptyTableWidgetWrapper.getWidget();
  }

  @Override
  public int getMaximumColumnWidth(int column) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column);
    if (colDef == null) {
      return -1;
    }
    return colDef.getColumnProperty(MaximumWidthProperty.TYPE).getMaximumColumnWidth();
  }

  @Override
  public int getMinimumColumnWidth(int column) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column);
    if (colDef == null) {
      return FixedWidthGrid.MIN_COLUMN_WIDTH;
    }
    int minWidth = colDef.getColumnProperty(MinimumWidthProperty.TYPE).getMinimumColumnWidth();
    return Math.max(FixedWidthGrid.MIN_COLUMN_WIDTH, minWidth);
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

  @Override
  public int getPreferredColumnWidth(int column) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column);
    if (colDef == null) {
      return FixedWidthGrid.DEFAULT_COLUMN_WIDTH;
    }
    return colDef.getColumnProperty(PreferredWidthProperty.TYPE).getPreferredColumnWidth();
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

      FixedWidthGrid dataTable = getDataTable();
      dataTable.deselectAllRows();
      if (!isCrossPageSelectionEnabled) {
        selectedRowValues = new HashSet<RowType>();
      }

      fireEvent(new PageChangeEvent(oldPage, currentPage));

      if (bulkRenderer == null) {
        int rowCount = getAbsoluteLastRowIndex() - getAbsoluteFirstRowIndex() + 1;
        if (rowCount != dataTable.getRowCount()) {
          dataTable.resizeRows(rowCount);
        }
        dataTable.clear(true);
      }

      int firstRow = getAbsoluteFirstRowIndex();
      int lastRow = pageSize == 0 ? tableModel.getRowCount() : pageSize;
      lastRequest = new Request(firstRow, lastRow, dataTable.getColumnSortList());
      tableModel.requestRows(lastRequest, pagingCallback);
    }
  }

  public void gotoPreviousPage() {
    gotoPage(currentPage - 1, false);
  }

  @Override
  public boolean isColumnSortable(int column) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column);
    if (colDef == null) {
      return true;
    }
    if (getSortPolicy() == SortPolicy.DISABLED) {
      return false;
    }
    return colDef.getColumnProperty(SortableProperty.TYPE).isColumnSortable();
  }

  @Override
  public boolean isColumnTruncatable(int column) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column);
    if (colDef == null) {
      return true;
    }
    return colDef.getColumnProperty(TruncationProperty.TYPE).isColumnTruncatable();
  }

  public boolean isCrossPageSelectionEnabled() {
    return isCrossPageSelectionEnabled;
  }

  @Override
  public boolean isFooterColumnTruncatable(int column) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column);
    if (colDef == null) {
      return true;
    }
    return colDef.getColumnProperty(TruncationProperty.TYPE).isFooterTruncatable();
  }

  public boolean isFooterGenerated() {
    return isFooterGenerated;
  }

  @Override
  public boolean isHeaderColumnTruncatable(int column) {
    ColumnDefinition<RowType, ?> colDef = getColumnDefinition(column);
    if (colDef == null) {
      return true;
    }
    return colDef.getColumnProperty(TruncationProperty.TYPE).isHeaderTruncatable();
  }

  public boolean isHeaderGenerated() {
    return isHeaderGenerated;
  }

  public boolean isPageLoading() {
    return isPageLoading;
  }

  public void reloadPage() {
    if (currentPage >= 0) {
      gotoPage(currentPage, true);
    } else {
      gotoPage(0, true);
    }
  }

  public void setBulkRenderer(FixedWidthGridBulkRenderer<RowType> bulkRenderer) {
    this.bulkRenderer = bulkRenderer;
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

  public void setEmptyTableWidget(Widget emptyTableWidget) {
    emptyTableWidgetWrapper.setWidget(emptyTableWidget);
  }

  public void setFooterGenerated(boolean isGenerated) {
    this.isFooterGenerated = isGenerated;
    if (isGenerated) {
      refreshFooterTable();
    }
  }

  public void setHeaderGenerated(boolean isGenerated) {
    this.isHeaderGenerated = isGenerated;
    if (isGenerated) {
      refreshHeaderTable();
    }
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

  @SuppressWarnings({"rawtypes", "unchecked"})
  protected void editCell(int row, int column) {
    final ColumnDefinition colDef = getColumnDefinition(column);
    if (colDef == null) {
      return;
    }
    CellEditor cellEditor = colDef.getCellEditor();
    if (cellEditor == null) {
      return;
    }

    final RowType rowValue = getRowValue(row);
    CellEditInfo editInfo = new CellEditInfo(getDataTable(), row, column);
    cellEditor.editCell(editInfo, colDef.getCellValue(rowValue),
        new CellEditor.Callback() {
          public void onCancel(CellEditInfo cellEditInfo) {
          }

          public void onComplete(CellEditInfo cellEditInfo, Object cellValue) {
            colDef.setCellValue(rowValue, cellValue);
            if (tableModel instanceof MutableTableModel) {
              int row = getAbsoluteFirstRowIndex() + cellEditInfo.getRowIndex();
              ((MutableTableModel<RowType>) tableModel).setRowValue(row,
                  rowValue);
            } else {
              refreshRow(cellEditInfo.getRowIndex());
            }
          }
        });
  }

  protected ColumnDefinition<RowType, ?> getColumnDefinition(int colIndex) {
    if (colIndex < visibleColumns.size()) {
      return visibleColumns.get(colIndex);
    }
    return null;
  }

  protected List<RowType> getRowValues() {
    return rowValues;
  }

  protected Widget getSelectAllWidget() {
    if (selectAllWidget == null) {
      final CheckBox box = new CheckBox();
      selectAllWidget = box;
      box.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          if (box.getValue()) {
            getDataTable().selectAllRows();
          } else {
            getDataTable().deselectAllRows();
          }
        }
      });
    }
    return selectAllWidget;
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
      refreshHeaderTable();
      refreshFooterTable();
      headersObsolete = false;
    }

    FixedWidthGrid dataTable = getDataTable();
    int rowCount = dataTable.getRowCount();
    for (int i = 0; i < rowCount; i++) {
      if (selectedRowValues.contains(getRowValue(i))) {
        dataTable.selectRow(i, false);
      }
    }

    dataTable.clearIdealWidths();
    redraw();
    isPageLoading = false;
    fireEvent(new PageLoadEvent(currentPage));
  }

  @Override
  protected void onLoad() {
    gotoFirstPage();
    super.onLoad();
  }

  protected void refreshFooterTable() {
    if (!isFooterGenerated) {
      return;
    }

    List<List<ColumnHeaderInfo>> allInfos = new ArrayList<List<ColumnHeaderInfo>>();
    int columnCount = visibleColumns.size();
    int footerCounts[] = new int[columnCount];
    int maxFooterCount = 0;

    for (int col = 0; col < columnCount; col++) {
      ColumnDefinition<RowType, ?> colDef = visibleColumns.get(col);
      FooterProperty prop = colDef.getColumnProperty(FooterProperty.TYPE);
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
          prev = new ColumnHeaderInfo(footer);
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
        allInfos.get(col).add(
            new ColumnHeaderInfo(null, maxFooterCount - footerCount));
      }
    }

    if (getFooterTable() == null) {
      setFooterTable(new FixedWidthFlexTable());
    }

    refreshHeaderTable(getFooterTable(), allInfos, false);
  }

  protected void refreshHeaderTable() {
    if (!isHeaderGenerated) {
      return;
    }

    List<List<ColumnHeaderInfo>> allInfos = new ArrayList<List<ColumnHeaderInfo>>();
    int columnCount = visibleColumns.size();
    int headerCounts[] = new int[columnCount];
    int maxHeaderCount = 0;

    for (int col = 0; col < columnCount; col++) {
      ColumnDefinition<RowType, ?> colDef = visibleColumns.get(col);
      HeaderProperty prop = colDef.getColumnProperty(HeaderProperty.TYPE);
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
          prev = new ColumnHeaderInfo(header);
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
        allInfos.get(col).add(0,
            new ColumnHeaderInfo(null, maxHeaderCount - headerCount));
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
        if (colDef.getColumnProperty(HeaderProperty.TYPE).isDynamic()
            || colDef.getColumnProperty(FooterProperty.TYPE).isDynamic()) {
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
      FixedWidthGrid dataTable = getDataTable();
      int relativeRow = row - firstRow;
      if (relativeRow < dataTable.getRowCount()) {
        dataTable.removeRow(relativeRow);
      }
    }
  }

  protected void setData(int firstRow, Iterator<RowType> rows) {
    getDataTable().deselectAllRows();
    rowValues = new ArrayList<RowType>();
    if (rows != null && rows.hasNext()) {
      setEmptyTableWidgetVisible(false);

      int firstVisibleRow = getAbsoluteFirstRowIndex();
      int lastVisibleRow = getAbsoluteLastRowIndex();
      Iterator<RowType> visibleIter = new VisibleRowsIterator(rows, firstRow,
          firstVisibleRow, lastVisibleRow);

      while (visibleIter.hasNext()) {
        rowValues.add(visibleIter.next());
      }

      refreshVisibleColumnDefinitions();

      if (bulkRenderer != null) {
        bulkRenderer.renderRows(rowValues.iterator(), tableRendererCallback);
        return;
      }

      int rowCount = rowValues.size();
      int colCount = visibleColumns.size();
      getDataTable().resize(rowCount, colCount);

      tableDefinition.renderRows(0, rowValues.iterator(), rowView);
    } else {
      setEmptyTableWidgetVisible(true);
    }

    onDataTableRendered();
  }

  protected void setEmptyTableWidgetVisible(boolean visible) {
    emptyTableWidgetWrapper.setVisible(visible);
    if (visible) {
      getDataWrapper().getStyle().setDisplay(Display.NONE);
    } else {
      getDataWrapper().getStyle().clearDisplay();
    }
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
    BeeFlexCellFormatter formatter = table.getFlexCellFormatter();
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
          table.setHTML(row, cell, header.toString());
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
      Widget box = null;
      if (isHeader
          && getDataTable().getSelectionPolicy() == SelectionPolicy.CHECKBOX) {
        box = getSelectAllWidget();
      }

      table.insertCell(0, 0);
      if (box != null) {
        table.setWidget(0, 0, box);
      } else {
        table.setHTML(0, 0, BeeConst.HTML_NBSP);
      }
      formatter.setRowSpan(0, 0, table.getRowCount());
      formatter.setHorizontalAlignment(0, 0,
          HasHorizontalAlignment.ALIGN_CENTER);
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

}

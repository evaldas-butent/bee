package com.butent.bee.client.grid.render;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

import com.butent.bee.client.grid.AbstractCellView;
import com.butent.bee.client.grid.ColumnDefinition;
import com.butent.bee.client.grid.HasTableDefinition;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.grid.RowView;
import com.butent.bee.client.grid.TableDefinition;
import com.butent.bee.client.grid.model.TableModel;
import com.butent.bee.client.grid.model.TableModelHelper.Request;
import com.butent.bee.client.grid.model.TableModelHelper.Response;
import com.butent.bee.shared.data.IsRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class TableBulkRenderer implements HasTableDefinition {

  private static class DelayedWidget {
    public int cellIndex;
    public int rowIndex;
    public Widget widget;

    public DelayedWidget(int rowIndex, int cellIndex, Widget widget) {
      this.rowIndex = rowIndex;
      this.cellIndex = cellIndex;
      this.widget = widget;
    }
  }

  protected static class BulkCellView extends AbstractCellView {
    private StringBuffer buffer = null;

    private Element htmlCleaner = Document.get().createDivElement().cast();

    private String curCellHtml = null;
    private Widget curCellWidget = null;

    private HorizontalAlignmentConstant curCellHorizontalAlign = null;
    private VerticalAlignmentConstant curCellVerticalAlign = null;

    private List<DelayedWidget> delayedWidgets = new ArrayList<DelayedWidget>();

    public BulkCellView(TableBulkRenderer bulkRenderer) {
      super((bulkRenderer.source == null) ? bulkRenderer : bulkRenderer.source);
    }

    @Override
    public void setHorizontalAlignment(HorizontalAlignmentConstant align) {
      curCellHorizontalAlign = align;
    }

    @Override
    public void setHTML(String html) {
      curCellWidget = null;
      curCellHtml = html;
    }

    @Override
    public void setText(String text) {
      htmlCleaner.setInnerText(text);
      setHTML(htmlCleaner.getInnerHTML());
    }

    @Override
    public void setVerticalAlignment(VerticalAlignmentConstant align) {
      curCellVerticalAlign = align;
    }

    @Override
    public void setWidget(Widget widget) {
      curCellHtml = null;
      curCellWidget = widget;
    }

    protected StringBuffer getStringBuffer() {
      return buffer;
    }

    @Override
    protected  void renderRowValue(IsRow rowValue, ColumnDefinition columnDef) {
      curCellHtml = null;
      curCellWidget = null;
      curCellHorizontalAlign = null;
      curCellVerticalAlign = null;
      super.renderRowValue(rowValue, columnDef);

      if (curCellWidget != null) {
        int row = getRowIndex();
        int cell = getCellIndex();
        delayedWidgets.add(new DelayedWidget(row, cell, curCellWidget));
      }

      buffer.append("<td");
      if (curCellHorizontalAlign != null) {
        buffer.append(" align=\"");
        buffer.append(curCellHorizontalAlign.getTextAlignString());
        buffer.append("\"");
      }
      if (curCellVerticalAlign != null) {
        buffer.append(" style=\"verticalAlign:");
        buffer.append(curCellVerticalAlign.getVerticalAlignString());
        buffer.append(";\"");
      }
      buffer.append(">");

      if (curCellHtml != null) {
        buffer.append(curCellHtml);
      }

      buffer.append("</td>");
    }

    String getHtml() {
      return curCellHtml;
    }
  }

  protected static class BulkRowView extends RowView {
    private StringBuffer buffer;

    private TableBulkRenderer bulkRenderer;
    private BulkCellView cellView;

    private RenderingOptions options;

    private int rowIndex = 0;

    public BulkRowView(BulkCellView cellView, TableBulkRenderer bulkRenderer,
        RenderingOptions options) {
      super(cellView);
      this.bulkRenderer = bulkRenderer;
      this.cellView = cellView;
      this.options = options;

      buffer = new StringBuffer();
      cellView.buffer = buffer;
    }

    protected StringBuffer getStringBuffer() {
      return buffer;
    }

    @Override
    protected void renderRowImpl(int rowIdx, IsRow rowValue,
        List<ColumnDefinition> visibleColumns) {
      buffer.append("<tr>");
      super.renderRowImpl(rowIdx, rowValue, visibleColumns);
      buffer.append("</tr>");
    }

    @Override
    protected void renderRowsImpl(int startRowIndex, final Iterator<IsRow> rowValues,
        final List<ColumnDefinition> visibleColumns) {
      buffer.append("<table><tbody>");
      if (options.headerRow != null) {
        buffer.append(options.headerRow);
      }

      rowIndex = startRowIndex;
      while (rowValues.hasNext()) {
        renderRowImpl(rowIndex, rowValues.next(), visibleColumns);
        rowIndex++;
      }

      if (options.footerRow != null) {
        buffer.append(options.footerRow);
      }

      buffer.append("</tbody></table>");
      bulkRenderer.renderRows(buffer.toString());

      for (DelayedWidget dw : cellView.delayedWidgets) {
        bulkRenderer.setWidgetRaw(bulkRenderer.getTable(),
            dw.rowIndex, dw.cellIndex, dw.widget);
      }

      if (options.callback != null) {
        options.callback.onRendered();
      }
    }
  }

  protected static class RenderingOptions {
    public int startRow = 0;
    public int numRows = TableModel.ALL_ROWS;
    public boolean syncCall = false;
    public String headerRow = null;
    public String footerRow = null;
    public RendererCallback callback = null;
  }

  private static Element WRAPPER_DIV;

  private HasTableDefinition source = null;

  private final HtmlTable table;

  private TableDefinition tableDefinition;

  public TableBulkRenderer(HtmlTable table, TableDefinition tableDefinition) {
    this.table = table;
    this.tableDefinition = tableDefinition;
  }

  public TableBulkRenderer(HtmlTable table, HasTableDefinition sourceTableDef) {
    this(table, sourceTableDef.getTableDefinition());
    this.source = sourceTableDef;
  }

  public TableDefinition getTableDefinition() {
    return (source == null) ? tableDefinition : source.getTableDefinition();
  }

  public final void renderRows(Iterator<IsRow> rows) {
    RenderingOptions options = createRenderingOptions();
    renderRows(rows, options);
  }

  public final void renderRows(TableModel tableModel,
      int startRow, int numRows, RendererCallback callback) {
    RenderingOptions options = createRenderingOptions();
    options.startRow = startRow;
    options.numRows = numRows;
    options.callback = callback;
    renderRows(tableModel, options);
  }

  public final void renderRows(TableModel tableModel, RendererCallback callback) {
    renderRows(tableModel, 0, TableModel.ALL_ROWS, callback);
  }

  protected RenderingOptions createRenderingOptions() {
    return new RenderingOptions();
  }

  protected RowView createRowView(final RenderingOptions options) {
    BulkCellView cellView = new BulkCellView(this);
    return new BulkRowView(cellView, this, options);
  }

  protected HtmlTable getTable() {
    return table;
  }

  protected void renderRows(final Iterator<IsRow> rows, final RenderingOptions options) {
    getTableDefinition().renderRows(0, rows, createRowView(options));
  }

  protected final void renderRows(TableModel tableModel, final RenderingOptions options) {
    TableModel.Callback requestCallback = new TableModel.Callback() {
      public void onFailure(Throwable caught) {
      }

      public void onRowsReady(Request request, final Response response) {
        final Iterator<IsRow> rows = response.getRowValues();
        renderRows(rows, options);
      }
    };

    tableModel.requestRows(new Request(options.startRow, options.numRows), requestCallback);
  }

  protected void renderRows(String rawHTMLTable) {
    DOM.setInnerHTML(getWrapperDiv(), rawHTMLTable);
    Element tableElement = DOM.getFirstChild(getWrapperDiv());
    Element newBody = replaceBodyElement(table.getElement(), tableElement);
    setBodyElement(table, newBody);
  }

  private Element getWrapperDiv() {
    if (WRAPPER_DIV == null) {
      WRAPPER_DIV = DOM.createDiv();
    }
    return WRAPPER_DIV;
  }

  private native Element replaceBodyElement(Element tbl, Element thatBody) /*-{
    tbl.removeChild(tbl.tBodies[0]);
    var thatChild = thatBody.tBodies[0];
    tbl.appendChild(thatChild);
    return thatChild;
  }-*/;

  private void setBodyElement(HtmlTable tbl, Element newBody) {
    tbl.setBodyElement(newBody);
  }

  private void setWidgetRaw(HtmlTable tbl, int row, int cell, Widget widget) {
    tbl.setWidget(row, cell, widget);
  }
}

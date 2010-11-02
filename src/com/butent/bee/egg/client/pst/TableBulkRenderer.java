package com.butent.bee.egg.client.pst;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

import com.butent.bee.egg.client.grid.BeeHtmlTable;
import com.butent.bee.egg.client.pst.TableDefinition.AbstractCellView;
import com.butent.bee.egg.client.pst.TableDefinition.AbstractRowView;
import com.butent.bee.egg.client.pst.TableModelHelper.Request;
import com.butent.bee.egg.client.pst.TableModelHelper.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class TableBulkRenderer<RowType> implements HasTableDefinition<RowType> {

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

  protected static class BulkCellView<RowType> extends AbstractCellView<RowType> {
    private StringBuffer buffer = null;

    private Element htmlCleaner = Document.get().createDivElement().cast();

    private HorizontalAlignmentConstant curCellHorizontalAlign = null;
    private String curCellHtml = null;
    private Widget curCellWidget = null;

    private Map<String, String> curCellStyles = new HashMap<String, String>();
    private String curCellStyleName = null;
    private VerticalAlignmentConstant curCellVerticalAlign = null;

    private List<DelayedWidget> delayedWidgets = new ArrayList<DelayedWidget>();

    public BulkCellView(TableBulkRenderer<RowType> bulkRenderer) {
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
    public void setStyleAttribute(String attr, String value) {
      curCellStyles.put(attr, value);
    }

    @Override
    public void setStyleName(String stylename) {
      curCellStyleName = stylename;
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
    protected <ColType> void renderRowValue(RowType rowValue, ColumnDefinition<RowType, ColType> columnDef) {
      curCellHtml = null;
      curCellWidget = null;
      curCellStyleName = null;
      curCellHorizontalAlign = null;
      curCellVerticalAlign = null;
      curCellStyles.clear();
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
        curCellStyles.put("verticalAlign",
            curCellVerticalAlign.getVerticalAlignString());
      }
      if (curCellStyleName != null) {
        buffer.append(" class=\"");
        buffer.append(curCellStyleName);
        buffer.append("\"");
      }
      if (curCellStyles.size() > 0) {
        buffer.append(" style=\"");
        for (Map.Entry<String, String> entry : curCellStyles.entrySet()) {
          buffer.append(entry.getKey());
          buffer.append(":");
          buffer.append(entry.getValue());
          buffer.append(";");
        }
        buffer.append("\"");
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

  protected static class BulkRowView<RowType> extends AbstractRowView<RowType> {
    private StringBuffer buffer;

    private TableBulkRenderer<RowType> bulkRenderer;
    private BulkCellView<RowType> cellView;

    private Map<String, String> curRowStyles = new HashMap<String, String>();
    private String curRowStyleName = null;

    private RenderingOptions options;

    private int rowIndex = 0;

    public BulkRowView(BulkCellView<RowType> cellView,
        TableBulkRenderer<RowType> bulkRenderer, RenderingOptions options) {
      super(cellView);
      this.bulkRenderer = bulkRenderer;
      this.cellView = cellView;
      this.options = options;

      buffer = new StringBuffer();
      cellView.buffer = buffer;
    }

    @Override
    public void setStyleAttribute(String attr, String value) {
      curRowStyles.put(attr, value);
    }

    @Override
    public void setStyleName(String stylename) {
      curRowStyleName = stylename;
    }

    protected StringBuffer getStringBuffer() {
      return buffer;
    }

    @Override
    protected void renderRowImpl(int rowIndex, RowType rowValue,
        RowRenderer<RowType> rowRenderer,
        List<ColumnDefinition<RowType, ?>> visibleColumns) {
      super.renderRowImpl(rowIndex, rowValue, rowRenderer, visibleColumns);
      buffer.append("</tr>");
    }

    @Override
    protected void renderRowsImpl(int startRowIndex,
        final Iterator<RowType> rowValues,
        final RowRenderer<RowType> rowRenderer,
        final List<ColumnDefinition<RowType, ?>> visibleColumns) {
      buffer.append("<table><tbody>");
      if (options.headerRow != null) {
        buffer.append(options.headerRow);
      }

      rowIndex = startRowIndex;
      final int myStamp = ++bulkRenderer.requestStamp;

      class RenderTableCommand implements RepeatingCommand {
        public boolean execute() {
          if (myStamp != bulkRenderer.requestStamp) {
            return false;
          }
          int checkRow = ROWS_PER_TIME_CHECK;
          double endSlice = Duration.currentTimeMillis() + TIME_SLICE;

          while (rowValues.hasNext()) {
            if (options.syncCall == false && --checkRow == 0) {
              checkRow = ROWS_PER_TIME_CHECK;
              double time = Duration.currentTimeMillis();
              if (time > endSlice) {
                return true;
              }
            }

            renderRowImpl(rowIndex, rowValues.next(), rowRenderer, visibleColumns);
            rowIndex++;
          }

          if (options.footerRow != null) {
            buffer.append(options.footerRow);
          }

          buffer.append("</tbody></table>");
          bulkRenderer.renderRows(buffer.toString());

          for (DelayedWidget dw : cellView.delayedWidgets) {
            bulkRenderer.setWidgetRaw(bulkRenderer.getTable(), dw.rowIndex,
                dw.cellIndex, dw.widget);
          }

          if (options.callback != null) {
            options.callback.onRendered();
          }
          return false;
        }
      }

      RenderTableCommand renderTable = new RenderTableCommand();
      if (renderTable.execute()) {
        Scheduler.get().scheduleIncremental(renderTable);
      }
    }

    @Override
    protected void renderRowValue(RowType rowValue, RowRenderer<RowType> rowRenderer) {
      curRowStyleName = null;
      curRowStyles.clear();
      super.renderRowValue(rowValue, rowRenderer);

      buffer.append("<tr");
      if (curRowStyleName != null) {
        buffer.append(" class=\"");
        buffer.append(curRowStyleName);
        buffer.append("\"");
      }
      if (curRowStyles.size() > 0) {
        buffer.append(" style=\"");
        for (Map.Entry<String, String> entry : curRowStyles.entrySet()) {
          buffer.append(entry.getKey());
          buffer.append(":");
          buffer.append(entry.getValue());
          buffer.append(";");
        }
        buffer.append("\"");
      }
      buffer.append(">");
    }
  }

  protected static class RenderingOptions {
    public int startRow = 0;
    public int numRows = MutableTableModel.ALL_ROWS;
    public boolean syncCall = false;
    public String headerRow = null;
    public String footerRow = null;
    public RendererCallback callback = null;
  }

  public static int TIME_SLICE = 1000;

  public static int ROWS_PER_TIME_CHECK = 10;

  private static Element WRAPPER_DIV;

  private int requestStamp = 0;

  private HasTableDefinition<RowType> source = null;

  private final BeeHtmlTable table;

  private TableDefinition<RowType> tableDefinition;

  public TableBulkRenderer(BeeHtmlTable table, TableDefinition<RowType> tableDefinition) {
    this.table = table;
    this.tableDefinition = tableDefinition;
  }

  public TableBulkRenderer(BeeHtmlTable table, HasTableDefinition<RowType> sourceTableDef) {
    this(table, sourceTableDef.getTableDefinition());
    this.source = sourceTableDef;
  }

  public TableDefinition<RowType> getTableDefinition() {
    return (source == null) ? tableDefinition : source.getTableDefinition();
  }

  public final void renderRows(Iterable<RowType> rows) {
    renderRows(rows, null);
  }

  public final void renderRows(Iterable<RowType> rows, RendererCallback callback) {
    IterableTableModel<RowType> tableModel = new IterableTableModel<RowType>(rows);
    RenderingOptions options = createRenderingOptions();
    options.syncCall = true;
    options.callback = callback;
    renderRows(tableModel, options);
  }

  public final void renderRows(Iterator<RowType> rows, RendererCallback callback) {
    RenderingOptions options = createRenderingOptions();
    options.callback = callback;
    renderRows(rows, options);
  }

  public final void renderRows(MutableTableModel<RowType> tableModel,
      int startRow, int numRows, RendererCallback callback) {
    RenderingOptions options = createRenderingOptions();
    options.startRow = startRow;
    options.numRows = numRows;
    options.callback = callback;
    renderRows(tableModel, options);
  }

  public final void renderRows(MutableTableModel<RowType> tableModel,
      RendererCallback callback) {
    renderRows(tableModel, 0, MutableTableModel.ALL_ROWS, callback);
  }

  protected RenderingOptions createRenderingOptions() {
    return new RenderingOptions();
  }

  protected AbstractRowView<RowType> createRowView(final RenderingOptions options) {
    BulkCellView<RowType> cellView = new BulkCellView<RowType>(this);
    return new BulkRowView<RowType>(cellView, this, options);
  }

  protected BeeHtmlTable getTable() {
    return table;
  }

  protected void renderRows(final Iterator<RowType> rows, final RenderingOptions options) {
    getTableDefinition().renderRows(0, rows, createRowView(options));
  }

  protected final void renderRows(TableModel<RowType> tableModel, final RenderingOptions options) {
    TableModel.Callback<RowType> requestCallback = new TableModel.Callback<RowType>() {
      public void onFailure(Throwable caught) {
      }

      public void onRowsReady(Request request, final Response<RowType> response) {
        final Iterator<RowType> rows = response.getRowValues();
        renderRows(rows, options);
      }
    };

    tableModel.requestRows(new Request(options.startRow, options.numRows),
        requestCallback);
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

  private native Element replaceBodyElement(Element table, Element thatBody) /*-{
    table.removeChild(table.tBodies[0]);
    var thatChild = thatBody.tBodies[0];
    table.appendChild(thatChild);
    return thatChild;
  }-*/;

  private void setBodyElement(BeeHtmlTable table, Element newBody) {
    table.setBodyElement(newBody);
  }

  private void setWidgetRaw(BeeHtmlTable table, int row, int cell, Widget widget) {
    table.setWidget(row, cell, widget);
  }
}

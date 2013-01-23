package com.butent.bee.client.grid;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.impl.ElementMapperImpl;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.IsHtmlTable;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.ui.CssUnit;

import java.util.ArrayList;
import java.util.Iterator;

public class HtmlTable extends Panel implements IdentifiableWidget, IsHtmlTable {

  public class CellFormatter {

    protected CellFormatter() {
      super();
    }

    public void addStyleName(int row, int column, String styleName) {
      ensureElement(row, column).addClassName(styleName);
    }

    public int getColSpan(int row, int column) {
      return DomUtils.getColSpan(getElement(row, column));
    }

    public Element getElement(int row, int column) {
      checkCellBounds(row, column);
      return getTd(bodyElem, row, column);
    }

    public int getRowSpan(int row, int column) {
      return DomUtils.getRowSpan(getElement(row, column));
    }

    public String getStyleName(int row, int column) {
      return getElement(row, column).getClassName();
    }

    public boolean isVisible(int row, int column) {
      return UIObject.isVisible(getElement(row, column));
    }

    public void removeStyleName(int row, int column, String styleName) {
      getElement(row, column).removeClassName(styleName);
    }

    public void setAlignment(int row, int column,
        HorizontalAlignmentConstant hAlign, VerticalAlignmentConstant vAlign) {
      setHorizontalAlignment(row, column, hAlign);
      setVerticalAlignment(row, column, vAlign);
    }

    public void setColSpan(int row, int column, int colSpan) {
      DomUtils.setColSpan(TableCellElement.as(ensureElement(row, column)), colSpan);
    }

    public void setHeight(int row, int column, double height, CssUnit unit) {
      StyleUtils.setHeight(ensureElement(row, column), height, unit);
    }

    public void setHeight(int row, int column, int height) {
      StyleUtils.setHeight(ensureElement(row, column), height);
    }

    public void setHorizontalAlignment(int row, int column, HorizontalAlignmentConstant align) {
      StyleUtils.setTextAlign(ensureElement(row, column), align);
    }

    public void setRowSpan(int row, int column, int rowSpan) {
      DomUtils.setRowSpan(ensureElement(row, column), rowSpan);
    }

    public void setStyleName(int row, int column, String styleName) {
      ensureElement(row, column).setClassName(styleName);
    }

    public void setStyleName(int row, int column, String styleName, boolean add) {
      if (add) {
        addStyleName(row, column, styleName);
      } else {
        removeStyleName(row, column, styleName);
      }
    }
    
    public void setVerticalAlignment(int row, int column, VerticalAlignmentConstant align) {
      StyleUtils.setVerticalAlign(ensureElement(row, column), align);
    }

    public void setVisible(int row, int column, boolean visible) {
      UIObject.setVisible(getElement(row, column), visible);
    }

    public void setWidth(int row, int column, double width, CssUnit unit) {
      StyleUtils.setWidth(ensureElement(row, column), width, unit);
    }

    public void setWidth(int row, int column, int width) {
      StyleUtils.setWidth(ensureElement(row, column), width);
    }

    public void setWordWrap(int row, int column, boolean wrap) {
      StyleUtils.setWordWrap(ensureElement(row, column), wrap);
    }

    protected Element ensureElement(int row, int column) {
      prepareCell(row, column);
      return getElement(row, column);
    }

    private native Element getTd(Element table, int row, int col) /*-{
      return table.rows[row].cells[col];
    }-*/;
  }

  public class ColumnFormatter {

    private Element columnGroup = null;

    protected ColumnFormatter() {
      super();
    }

    public void addStyleName(int column, String styleName) {
      ensureColumn(column).addClassName(styleName);
    }

    public Element getElement(int column) {
      return ensureColumn(column);
    }

    public String getStyleName(int column) {
      return ensureColumn(column).getClassName();
    }

    public void removeStyleName(int column, String styleName) {
      ensureColumn(column).removeClassName(styleName);
    }

    public void setStyleName(int column, String styleName) {
      ensureColumn(column).setClassName(styleName);
    }

    public void setWidth(int column, double width, CssUnit unit) {
      StyleUtils.setWidth(ensureColumn(column), width, unit);
    }

    public void setWidth(int column, int width) {
      StyleUtils.setWidth(ensureColumn(column), width);
    }

    protected Element ensureColumn(int column) {
      Assert.nonNegative(column, "Column " + column + " must be non-negative");
      ensureColumnGroup();

      int count = columnGroup.getChildCount();
      for (int i = count; i <= column; i++) {
        columnGroup.appendChild(Document.get().createColElement());
      }

      return columnGroup.getChild(column).cast();
    }

    protected void ensureColumnGroup() {
      if (columnGroup == null) {
        columnGroup = DOM.createColGroup();
        DOM.insertChild(tableElem, columnGroup, 0);
        DOM.appendChild(columnGroup, DOM.createCol());
      }
    }
  }

  public class RowFormatter {

    protected RowFormatter() {
      super();
    }

    public void addStyleName(int row, String styleName) {
      ensureElement(row).addClassName(styleName);
    }

    public Element getElement(int row) {
      checkRowBounds(row);
      return getTr(bodyElem, row);
    }

    public String getStyleName(int row) {
      return getElement(row).getClassName();
    }

    public boolean isVisible(int row) {
      return UIObject.isVisible(getElement(row));
    }

    public void removeStyleName(int row, String styleName) {
      getElement(row).removeClassName(styleName);
    }

    public void setStyleName(int row, String styleName) {
      ensureElement(row).setClassName(styleName);
    }

    public void setVerticalAlign(int row, VerticalAlignmentConstant align) {
      StyleUtils.setVerticalAlign(ensureElement(row), align);
    }

    public void setVisible(int row, boolean visible) {
      UIObject.setVisible(getElement(row), visible);
    }

    protected Element ensureElement(int row) {
      prepareRow(row);
      return getElement(row);
    }

    private native Element getTr(Element elem, int row) /*-{
      return elem.rows[row];
    }-*/;
  }

  private final Element tableElem;
  private final Element bodyElem;

  private final ElementMapperImpl<Widget> widgetMap;

  private final CellFormatter cellFormatter;
  private final ColumnFormatter columnFormatter;
  private final RowFormatter rowFormatter;

  private String defaultCellClasses = null;
  private String defaultCellStyles = null;

  public HtmlTable() {
    this.tableElem = DOM.createTable();
    this.bodyElem = DOM.createTBody();
    DOM.appendChild(tableElem, bodyElem);
    setElement(tableElem);

    this.widgetMap = new ElementMapperImpl<Widget>();

    this.cellFormatter = new CellFormatter();
    this.rowFormatter = new RowFormatter();
    this.columnFormatter = new ColumnFormatter();

    setStyleName("bee-HtmlTable");

    init();
  }

  public void alignCenter(int row, int column) {
    getCellFormatter().setHorizontalAlignment(row, column, HasHorizontalAlignment.ALIGN_CENTER);
  }

  public void alignLeft(int row, int column) {
    getCellFormatter().setHorizontalAlignment(row, column, HasHorizontalAlignment.ALIGN_LEFT);
  }

  public void alignRight(int row, int column) {
    getCellFormatter().setHorizontalAlignment(row, column, HasHorizontalAlignment.ALIGN_RIGHT);
  }

  @Override
  public void clear() {
    int numRows = getRowCount();
    for (int i = 0; i < numRows; i++) {
      removeRow(0);
    }
  }

  public int getCellCount(int row) {
    checkRowBounds(row);
    return getDOMCellCount(bodyElem, row);
  }

  public CellFormatter getCellFormatter() {
    return cellFormatter;
  }

  public ColumnFormatter getColumnFormatter() {
    return columnFormatter;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "table";
  }

  public Element getRow(int row) {
    return rowFormatter.getTr(bodyElem, row);
  }

  public int getRowCount() {
    return getDOMRowCount(bodyElem);
  }

  public RowFormatter getRowFormatter() {
    return rowFormatter;
  }

  public Widget getWidget(int row, int column) {
    checkCellBounds(row, column);
    return getWidgetImpl(row, column);
  }
  
  public int insertRow(int beforeRow) {
    if (beforeRow != getRowCount()) {
      checkRowBounds(beforeRow);
    }
    Element tr = DOM.createTR();
    DOM.insertChild(bodyElem, tr, beforeRow);
    return beforeRow;
  }

  public boolean isEmpty() {
    return getRowCount() <= 0;
  }

  @Override
  public Iterator<Widget> iterator() {
    return new Iterator<Widget>() {
      final ArrayList<Widget> widgetList = widgetMap.getObjectList();
      int lastIndex = -1;
      int nextIndex = -1;
      {
        findNext();
      }

      @Override
      public boolean hasNext() {
        return nextIndex < widgetList.size();
      }

      @Override
      public Widget next() {
        if (!hasNext()) {
          Assert.untouchable("no such element");
        }
        Widget result = widgetList.get(nextIndex);
        lastIndex = nextIndex;
        findNext();
        return result;
      }

      @Override
      public void remove() {
        Assert.state(lastIndex >= 0);
        Widget w = widgetList.get(lastIndex);
        w.removeFromParent();
        lastIndex = -1;
      }

      private void findNext() {
        while (++nextIndex < widgetList.size()) {
          if (widgetList.get(nextIndex) != null) {
            return;
          }
        }
      }
    };
  }

  @Override
  public boolean remove(Widget widget) {
    if (widget.getParent() != this) {
      return false;
    }

    try {
      orphan(widget);
    } finally {
      Element elem = widget.getElement();
      DOM.removeChild(DOM.getParent(elem), elem);

      widgetMap.removeByElement(elem);
    }
    return true;
  }

  public void removeRow(int row) {
    int columnCount = getCellCount(row);
    for (int column = 0; column < columnCount; ++column) {
      cleanCell(row, column, false);
    }
    DOM.removeChild(bodyElem, rowFormatter.getElement(row));
  }
  
  @Override
  public void setBorderSpacing(int spacing) {
    StyleUtils.setBorderSpacing(tableElem, spacing);
  }

  @Override
  public void setDefaultCellClasses(String classes) {
    this.defaultCellClasses = classes;
  }

  @Override
  public void setDefaultCellStyles(String styles) {
    this.defaultCellStyles = styles;
  }

  public void setHTML(int row, int column, String html) {
    prepareCell(row, column);
    Element td = cleanCell(row, column, html == null);
    if (html != null) {
      DOM.setInnerHTML(td, html);
    }
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setText(int row, int column, String text) {
    prepareCell(row, column);
    Element td = cleanCell(row, column, text == null);
    if (text != null) {
      DOM.setInnerText(td, text);
    }
  }

  public void setWidget(int row, int column, Widget widget) {
    prepareCell(row, column);
    if (widget != null) {
      widget.removeFromParent();

      Element td = cleanCell(row, column, true);

      widgetMap.put(widget);

      DOM.appendChild(td, widget.getElement());

      adopt(widget);
    }
  }

  public void setWidget(int row, int column, Widget widget, String cellStyleName) {
    setWidget(row, column, widget);
    getCellFormatter().addStyleName(row, column, cellStyleName);
  }

  private void checkCellBounds(int row, int column) {
    checkRowBounds(row);
    Assert.nonNegative(column, "Column " + column + " must be non-negative");
    int cellSize = getCellCount(row);
    Assert.isTrue(cellSize > column, "Column index: " + column + ", Column size: " + cellSize);
  }

  private void checkRowBounds(int row) {
    int rowSize = getRowCount();
    Assert.isTrue(row < rowSize && row >= 0, "Row index: " + row + ", Row size: " + rowSize);
  }

  private Element cleanCell(int row, int column, boolean clearInnerHTML) {
    Element td = getCellFormatter().getElement(row, column);
    internalClearCell(td, clearInnerHTML);
    return td;
  }

  private Element createCell() {
    Element td = DOM.createTD();
    StyleUtils.updateAppearance(td, getDefaultCellClasses(), getDefaultCellStyles());
    return td;
  }

  private Element createRow() {
    return DOM.createTR();
  }

  private String getDefaultCellClasses() {
    return defaultCellClasses;
  }

  private String getDefaultCellStyles() {
    return defaultCellStyles;
  }

  private native int getDOMCellCount(Element tableBody, int row) /*-{
    return tableBody.rows[row].cells.length;
  }-*/;

  private native int getDOMRowCount(Element elem) /*-{
    return elem.rows.length;
  }-*/;

  private Widget getWidgetImpl(int row, int column) {
    Element td = cellFormatter.getElement(row, column);
    Element child = DOM.getFirstChild(td);

    if (child == null) {
      return null;
    } else {
      return widgetMap.get(child);
    }
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }

  private boolean internalClearCell(Element td, boolean clearInnerHTML) {
    if (td == null) {
      return false;
    }

    Element maybeChild = DOM.getFirstChild(td);
    Widget widget = null;
    if (maybeChild != null) {
      widget = widgetMap.get(maybeChild);
    }

    if (widget != null) {
      remove(widget);
      return true;
    } else {
      if (clearInnerHTML) {
        DOM.setInnerHTML(td, BeeConst.STRING_EMPTY);
      }
      return false;
    }
  }

  private void prepareCell(int row, int column) {
    prepareRow(row);
    Assert.nonNegative(column, "Cannot create a column with a negative index: " + column);

    int cellCount = getCellCount(row);
    int required = column + 1 - cellCount;
    if (required > 0) {
      Element tr = getRow(row);
      for (int i = 0; i < required; i++) {
        tr.appendChild(createCell());
      }
    }
  }

  private void prepareRow(int row) {
    Assert.nonNegative(row, "Cannot create a row with a negative index: " + row);

    int rowCount = getRowCount();
    for (int i = rowCount; i <= row; i++) {
      insertRow(i);
    }
  }
}

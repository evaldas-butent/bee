package com.butent.bee.egg.client.grid;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.impl.ElementMapperImpl;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.HasId;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class HtmlTable extends Panel implements HasClickHandlers,
    HasDoubleClickHandlers, HasId {

  public class Cell {
    private final int rowIndex;
    private final int cellIndex;

    protected Cell(int rowIndex, int cellIndex) {
      this.cellIndex = cellIndex;
      this.rowIndex = rowIndex;
    }

    public int getCellIndex() {
      return cellIndex;
    }

    public Element getElement() {
      return getCellFormatter().getElement(rowIndex, cellIndex);
    }

    public int getRowIndex() {
      return rowIndex;
    }
  }

  public class CellFormatter {
    public void addStyleName(int row, int column, String styleName) {
      UIObject.setStyleName(ensureElement(row, column), styleName, true);
    }

    public Element getElement(int row, int column) {
      checkCellBounds(row, column);
      return getRawElement(row, column);
    }

    public String getStyleName(int row, int column) {
      return UIObject.getStyleName(getElement(row, column));
    }

    public String getStylePrimaryName(int row, int column) {
      return UIObject.getStylePrimaryName(getElement(row, column));
    }

    public boolean isVisible(int row, int column) {
      Element e = getElement(row, column);
      return UIObject.isVisible(e);
    }

    public void removeStyleName(int row, int column, String styleName) {
      UIObject.setStyleName(getElement(row, column), styleName, false);
    }

    public void setAlignment(int row, int column,
        HorizontalAlignmentConstant hAlign, VerticalAlignmentConstant vAlign) {
      setHorizontalAlignment(row, column, hAlign);
      setVerticalAlignment(row, column, vAlign);
    }

    public void setHeight(int row, int column, String height) {
      DOM.setElementProperty(ensureElement(row, column), "height", height);
    }

    public void setHorizontalAlignment(int row, int column, HorizontalAlignmentConstant align) {
      DOM.setElementProperty(ensureElement(row, column), "align", align.getTextAlignString());
    }

    public void setStyleName(int row, int column, String styleName) {
      UIObject.setStyleName(ensureElement(row, column), styleName);
    }

    public void setStylePrimaryName(int row, int column, String styleName) {
      UIObject.setStylePrimaryName(ensureElement(row, column), styleName);
    }

    public void setVerticalAlignment(int row, int column, VerticalAlignmentConstant align) {
      DOM.setStyleAttribute(ensureElement(row, column),
          "verticalAlign", align.getVerticalAlignString());
    }

    public void setVisible(int row, int column, boolean visible) {
      Element e = ensureElement(row, column);
      UIObject.setVisible(e, visible);
    }

    public void setWidth(int row, int column, String width) {
      DOM.setElementProperty(ensureElement(row, column), "width",  width);
    }

    public void setWordWrap(int row, int column, boolean wrap) {
      String wrapValue = wrap ? "" : "nowrap";
      DOM.setStyleAttribute(ensureElement(row, column), "whiteSpace", wrapValue);
    }

    protected Element ensureElement(int row, int column) {
      prepareCell(row, column);
      return getRawElement(row, column);
    }

    protected String getAttr(int row, int column, String attr) {
      Element elem = getElement(row, column);
      return DOM.getElementAttribute(elem, attr);
    }

    protected Element getRawElement(int row, int column) {
      return getCellElement(bodyElem, row, column);
    }

    protected void setAttr(int row, int column, String attrName, String value) {
      Element elem = ensureElement(row, column);
      DOM.setElementAttribute(elem, attrName, value);
    }

    private native Element getCellElement(Element table, int row, int col) /*-{
      return table.rows[row].cells[col];
    }-*/;
  }

  public class ColumnFormatter {
    protected Element columnGroup;

    public void addStyleName(int col, String styleName) {
      UIObject.setStyleName(ensureColumn(col), styleName, true);
    }

    public Element getElement(int column) {
      return ensureColumn(column);
    }

    public String getStyleName(int column) {
      return UIObject.getStyleName(ensureColumn(column));
    }

    public String getStylePrimaryName(int column) {
      return UIObject.getStylePrimaryName(ensureColumn(column));
    }

    public void removeStyleName(int column, String styleName) {
      UIObject.setStyleName(ensureColumn(column), styleName, false);
    }

    public void setStyleName(int column, String styleName) {
      UIObject.setStyleName(ensureColumn(column), styleName);
    }

    public void setStylePrimaryName(int column, String styleName) {
      UIObject.setStylePrimaryName(ensureColumn(column), styleName);
    }

    public void setWidth(int column, String width) {
      DOM.setElementProperty(ensureColumn(column), "width", width);
    }

    void resizeColumnGroup(int columns, boolean growOnly) {
      columns = Math.max(columns, 1);

      int num = columnGroup.getChildCount();
      if (num < columns) {
        for (int i = num; i < columns; i++) {
          columnGroup.appendChild(Document.get().createColElement());
        }
      } else if (!growOnly && num > columns) {
        for (int i = num; i > columns; i--) {
          columnGroup.removeChild(columnGroup.getLastChild());
        }
      }
    }

    private Element ensureColumn(int col) {
      prepareColumn(col);
      prepareColumnGroup();
      resizeColumnGroup(col + 1, true);
      return columnGroup.getChild(col).cast();
    }

    private void prepareColumnGroup() {
      if (columnGroup == null) {
        columnGroup = DOM.createColGroup();
        DOM.insertChild(tableElem, columnGroup, 0);
        DOM.appendChild(columnGroup, DOM.createCol());
      }
    }
  }

  public class RowFormatter {
    public void addStyleName(int row, String styleName) {
      UIObject.setStyleName(ensureElement(row), styleName, true);
    }

    public Element getElement(int row) {
      checkRowBounds(row);
      return getRawElement(row);
    }

    public String getStyleName(int row) {
      return UIObject.getStyleName(getElement(row));
    }

    public String getStylePrimaryName(int row) {
      return UIObject.getStylePrimaryName(getElement(row));
    }

    public boolean isVisible(int row) {
      Element e = getElement(row);
      return UIObject.isVisible(e);
    }

    public void removeStyleName(int row, String styleName) {
      UIObject.setStyleName(ensureElement(row), styleName, false);
    }

    public void setStyleName(int row, String styleName) {
      UIObject.setStyleName(ensureElement(row), styleName);
    }

    public void setStylePrimaryName(int row, String styleName) {
      UIObject.setStylePrimaryName(ensureElement(row), styleName);
    }

    public void setVerticalAlign(int row, VerticalAlignmentConstant align) {
      DOM.setStyleAttribute(ensureElement(row), "verticalAlign",
          align.getVerticalAlignString());
    }

    public void setVisible(int row, boolean visible) {
      Element e = ensureElement(row);
      UIObject.setVisible(e, visible);
    }

    protected Element ensureElement(int row) {
      prepareRow(row);
      return getRawElement(row);
    }

    protected Element getRawElement(int row) {
      return getRow(bodyElem, row);
    }
    
    protected native Element getRow(Element elem, int row)/*-{
      return elem.rows[row];
    }-*/;

    protected void setAttr(int row, String attrName, String value) {
      Element elem = ensureElement(row);
      DOM.setElementAttribute(elem, attrName, value);
    }
  }

  private final Element tableElem;
  private Element bodyElem;

  private CellFormatter cellFormatter;
  private ColumnFormatter columnFormatter;
  private RowFormatter rowFormatter;

  private ElementMapperImpl<Widget> widgetMap = new ElementMapperImpl<Widget>();

  private String clearText = BeeConst.STRING_EMPTY;
  
  public HtmlTable() {
    tableElem = DOM.createTable();
    bodyElem = DOM.createTBody();
    DOM.appendChild(tableElem, bodyElem);
    setElement(tableElem);
    
    init();
  }

  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }

  public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
    return addDomHandler(handler, DoubleClickEvent.getType());
  }

  @Override
  public void clear() {
    clear(false);
  }

  public void clear(boolean clearInnerHTML) {
    for (int row = 0; row < getRowCount(); ++row) {
      for (int col = 0; col < getCellCount(row); ++col) {
        cleanCell(row, col, clearInnerHTML);
      }
    }
  }

  public boolean clearCell(int row, int column) {
    Element td = getCellFormatter().getElement(row, column);
    return internalClearCell(td, true);
  }

  public abstract void createId();

  public Element getBodyElement() {
    return bodyElem;
  }

  public abstract int getCellCount(int row);

  public Cell getCellForEvent(ClickEvent event) {
    Element td = getEventTargetCell(Event.as(event.getNativeEvent()));
    if (td == null) {
      return null;
    }

    int row = TableRowElement.as(td.getParentElement()).getSectionRowIndex();
    int column = TableCellElement.as(td).getCellIndex();
    return new Cell(row, column);
  }

  public CellFormatter getCellFormatter() {
    return cellFormatter;
  }

  public int getCellPadding() {
    return DomUtils.getCellPadding(tableElem);
  }

  public int getCellSpacing() {
    return DomUtils.getCellSpacing(tableElem);
  }

  public ColumnFormatter getColumnFormatter() {
    return columnFormatter;
  }

  public int getDOMCellCount(int row) {
    return getDOMCellCount(bodyElem, row);
  }

  public int getDOMRowCount() {
    return getDOMRowCount(bodyElem);
  }

  public Element getEventTargetCell(Event event) {
    Element td = DOM.eventGetTarget(event);
    for (; td != null; td = DOM.getParent(td)) {
      if (DomUtils.isTdElement(td)) {
        Element tr = DOM.getParent(td);
        Element body = DOM.getParent(tr);
        if (body == bodyElem) {
          return td;
        }
      }
      if (td == bodyElem) {
        return null;
      }
    }
    return null;
  }

  public String getHTML(int row, int column) {
    return DOM.getInnerHTML(cellFormatter.getElement(row, column));
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public Element getRow(int row) {
    return rowFormatter.getRow(bodyElem, row);
  }

  public abstract int getRowCount();

  public RowFormatter getRowFormatter() {
    return rowFormatter;
  }

  public String getText(int row, int column) {
    checkCellBounds(row, column);
    Element e = cellFormatter.getElement(row, column);
    return DOM.getInnerText(e);
  }

  public Widget getWidget(int row, int column) {
    checkCellBounds(row, column);
    return getWidgetImpl(row, column);
  }

  public boolean isCellPresent(int row, int column) {
    if ((row >= getRowCount()) || (row < 0)) {
      return false;
    }
    if ((column < 0) || (column >= getCellCount(row))) {
      return false;
    } else {
      return true;
    }
  }

  public Iterator<Widget> iterator() {
    return new Iterator<Widget>() {
      final ArrayList<Widget> widgetList = widgetMap.getObjectList();
      int lastIndex = -1;
      int nextIndex = -1;
      {
        findNext();
      }

      public boolean hasNext() {
        return nextIndex < widgetList.size();
      }

      public Widget next() {
        if (!hasNext()) {
          Assert.untouchable("no such element");
        }
        Widget result = widgetList.get(nextIndex);
        lastIndex = nextIndex;
        findNext();
        return result;
      }

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

  public void setBodyElement(Element element) {
    if (this.bodyElem != null) {
      clearOnlyWidgets();
    }
    this.bodyElem = element;
  }

  public void setBorderWidth(int width) {
    DOM.setElementProperty(tableElem, "border", "" + width);
  }

  public void setCellPadding(int padding) {
    TableElement.as(tableElem).setCellPadding(padding);
  }

  public void setCellSpacing(int spacing) {
    TableElement.as(tableElem).setCellSpacing(spacing);
  }
  
  public void setHTML(int row, int column, SafeHtml html) {
    setHTML(row, column, html.asString());
  }

  public void setHTML(int row, int column, String html) {
    prepareCell(row, column);
    Element td = cleanCell(row, column, html == null);
    if (html != null) {
      DOM.setInnerHTML(td, html);
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setText(int row, int column, String text) {
    prepareCell(row, column);
    Element td;
    td = cleanCell(row, column, text == null);
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

  protected void checkCellBounds(int row, int column) {
    checkRowBounds(row);
    Assert.nonNegative(column, "Column " + column + " must be non-negative");
    int cellSize = getCellCount(row);
    Assert.isTrue(cellSize > column, "Column index: " + column
        + ", Column size: " + cellSize);
  }

  protected void checkRowBounds(int row) {
    int rowSize = getRowCount();
    Assert.isTrue(row < rowSize && row >= 0, "Row index: " + row
        + ", Row size: " + rowSize);
  }

  protected Element createCell() {
    return DOM.createTD();
  }

  protected Element createRow() {
    return DOM.createTR();
  }

  protected int getCellIndex(Element rowElem, Element cellElem) {
    return DOM.getChildIndex(rowElem, cellElem);
  }

  protected native int getDOMCellCount(Element tableBody, int row) /*-{
    return tableBody.rows[row].cells.length;
  }-*/;

  protected native int getDOMRowCount(Element elem) /*-{
    return elem.rows.length;
  }-*/;

  protected int getRowIndex(Element rowElem) {
    return TableRowElement.as(rowElem).getRowIndex();
  }

  protected Element insertCell(int row, int column) {
    Element tr = rowFormatter.getRawElement(row);
    Element td = createCell();
    DOM.insertChild(tr, td, column);
    return td;
  }

  protected void insertCells(int row, int column, int count) {
    Element tr = rowFormatter.getRawElement(row);
    for (int i = column; i < column + count; i++) {
      Element td = createCell();
      DOM.insertChild(tr, td, i);
    }
  }

  protected int insertRow(int beforeRow) {
    if (beforeRow != getRowCount()) {
      checkRowBounds(beforeRow);
    }
    Element tr = DOM.createTR();
    DOM.insertChild(bodyElem, tr, beforeRow);
    return beforeRow;
  }

  protected boolean internalClearCell(Element td, boolean clearInnerHTML) {
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
        DOM.setInnerHTML(td, clearText);
      }
      return false;
    }
  }

  protected abstract void prepareCell(int row, int column);

  protected void prepareColumn(int column) {
    Assert.nonNegative(column, "Cannot access a column with a negative index: " + column);
  }

  protected abstract void prepareRow(int row);

  protected void removeCell(int row, int column) {
    checkCellBounds(row, column);
    Element td = cleanCell(row, column, false);
    Element tr = rowFormatter.getRawElement(row);
    DOM.removeChild(tr, td);
  }

  protected void removeRow(int row) {
    int columnCount = getCellCount(row);
    for (int column = 0; column < columnCount; ++column) {
      cleanCell(row, column, false);
    }
    DOM.removeChild(bodyElem, rowFormatter.getRawElement(row));
  }

  protected void setCellFormatter(CellFormatter cellFormatter) {
    this.cellFormatter = cellFormatter;
  }
  
  protected void setClearText(String clearText) {
    this.clearText = clearText;
  }

  protected void setColumnFormatter(ColumnFormatter formatter) {
    if (columnFormatter != null) {
      formatter.columnGroup = columnFormatter.columnGroup;
    }
    columnFormatter = formatter;
    columnFormatter.prepareColumnGroup();
  }

  protected void setRowFormatter(RowFormatter rowFormatter) {
    this.rowFormatter = rowFormatter;
  }

  private Element cleanCell(int row, int column, boolean clearInnerHTML) {
    Element td = getCellFormatter().getRawElement(row, column);
    internalClearCell(td, clearInnerHTML);
    return td;
  }

  private void clearOnlyWidgets() {
    Iterator<Widget> widgets = iterator();
    while (widgets.hasNext()) {
      orphan(widgets.next());
    }
    widgetMap = new ElementMapperImpl<Widget>();
  }

  private Widget getWidgetImpl(int row, int column) {
    Element e = cellFormatter.getRawElement(row, column);
    Element child = DOM.getFirstChild(e);
    if (child == null) {
      return null;
    } else {
      return widgetMap.get(child);
    }
  }

  private void init() {
    createId();
  }
}

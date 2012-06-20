package com.butent.bee.client.layout;

import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.shared.HasId;

public abstract class CellVector extends ComplexPanel implements HasId, HasIndexedWidgets,
    HasAlignment, InsertPanel {

  private final Element table;
  private final Element body;

  private HorizontalAlignmentConstant horAlign = null;
  private VerticalAlignmentConstant vertAlign = null;

  public CellVector() {
    super();

    table = DOM.createTable();
    body = DOM.createTBody();
    DOM.appendChild(table, body);

    setElement(table);

    DomUtils.createId(table, getIdPrefix());
    
    setCellSpacing(0);
    setCellPadding(0);
    table.setClassName("bee-CellVector");
  }

  public void addStyleToCell(Widget w, String styleName) {
    Element td = getWidgetTd(w);
    if (td != null) {
      td.addClassName(styleName);
    }
  }
  
  public Element getCell(int index) {
    return getWidgetTd(getWidget(index));
  }

  public HorizontalAlignmentConstant getHorizontalAlignment() {
    return horAlign;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public VerticalAlignmentConstant getVerticalAlignment() {
    return vertAlign;
  }

  public void setBorderWidth(int width) {
    table.getStyle().setBorderWidth(width, Unit.PX);
  }

  public void setCellHeight(Widget w, double height, Unit unit) {
    Element td = getWidgetTd(w);
    if (td != null) {
      StyleUtils.setHeight(td, height, unit);
    }
  }

  public void setCellHeight(Widget w, int height) {
    Element td = getWidgetTd(w);
    if (td != null) {
      StyleUtils.setHeight(td, height);
    }
  }

  public void setCellHorizontalAlignment(Widget w, HorizontalAlignmentConstant align) {
    Element td = getWidgetTd(w);
    if (td != null) {
      setCellHorizontalAlignment(td, align);
    }
  }

  public void setCellPadding(int padding) {
    TableElement.as(table).setCellPadding(padding);
  }

  public void setCellSpacing(int spacing) {
    TableElement.as(table).setCellSpacing(spacing);
  }

  public void setCellVerticalAlignment(Widget w, VerticalAlignmentConstant align) {
    Element td = getWidgetTd(w);
    if (td != null) {
      setCellVerticalAlignment(td, align);
    }
  }

  public void setCellWidth(Widget w, double width, Unit unit) {
    Element td = getWidgetTd(w);
    if (td != null) {
      StyleUtils.setWidth(td, width, unit);
    }
  }

  public void setCellWidth(Widget w, int width) {
    Element td = getWidgetTd(w);
    if (td != null) {
      StyleUtils.setWidth(td, width);
    }
  }

  public void setHorizontalAlignment(HorizontalAlignmentConstant align) {
    horAlign = align;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setVerticalAlignment(VerticalAlignmentConstant align) {
    vertAlign = align;
  }

  protected Element createAlignedTd() {
    Element td = DOM.createTD();
    
    if (getHorizontalAlignment() != null) {
      setCellHorizontalAlignment(td, getHorizontalAlignment());
    }
    if (getVerticalAlignment() != null) {
      setCellVerticalAlignment(td, getVerticalAlignment());
    }

    return td;
  }

  protected Element getBody() {
    return body;
  }

  protected Element getTable() {
    return table;
  }

  protected Element getWidgetTd(Widget w) {
    if (w.getParent() != this) {
      return null;
    }
    return DOM.getParent(w.getElement());
  }

  protected void setCellHorizontalAlignment(Element td, HorizontalAlignmentConstant align) {
    if (align != null) {
      StyleUtils.setTextAlign(td, align);
    }
  }

  protected void setCellVerticalAlignment(Element td, VerticalAlignmentConstant align) {
    if (align != null) {
      UiHelper.setVerticalAlignment(td, align.getVerticalAlignString());
    }
  }
}

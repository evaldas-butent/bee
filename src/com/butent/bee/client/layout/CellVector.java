package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.HasIndexedWidgets;

public abstract class CellVector extends ComplexPanel implements IdentifiableWidget, HasIndexedWidgets,
    HasAlignment, InsertPanel, IsHtmlTable {

  private final Element table;
  private final Element body;

  private HorizontalAlignmentConstant horAlign = null;
  private VerticalAlignmentConstant vertAlign = null;
  
  private String defaultCellClasses = null;
  private String defaultCellStyles = null;

  public CellVector() {
    super();

    table = DOM.createTable();
    body = DOM.createTBody();
    DOM.appendChild(table, body);

    setElement(table);

    DomUtils.createId(table, getIdPrefix());
    
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

  @Override
  public HorizontalAlignmentConstant getHorizontalAlignment() {
    return horAlign;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public VerticalAlignmentConstant getVerticalAlignment() {
    return vertAlign;
  }

  @Override
  public boolean isEmpty() {
    return getWidgetCount() <= 0;
  }

  @Override
  public void setBorderSpacing(int spacing) {
    StyleUtils.setBorderSpacing(table, spacing);
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

  @Override
  public void setDefaultCellClasses(String classes) {
    this.defaultCellClasses = classes;
  }

  @Override
  public void setDefaultCellStyles(String styles) {
    this.defaultCellStyles = styles;
  }
  
  @Override
  public void setHorizontalAlignment(HorizontalAlignmentConstant align) {
    this.horAlign = align;
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  public void setVerticalAlignment(VerticalAlignmentConstant align) {
    this.vertAlign = align;
  }

  protected Element createDefaultCell() {
    Element td = DOM.createTD();

    StyleUtils.updateAppearance(td, getDefaultCellClasses(), getDefaultCellStyles());
    
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
      StyleUtils.setVerticalAlign(td, align);
    }
  }

  private String getDefaultCellClasses() {
    return defaultCellClasses;
  }

  private String getDefaultCellStyles() {
    return defaultCellStyles;
  }
}

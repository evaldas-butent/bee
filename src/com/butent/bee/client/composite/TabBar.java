package com.butent.bee.client.composite;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.CellVector;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.IsHtmlTable;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.values.VerticalAlign;
import com.butent.bee.shared.ui.Orientation;

public class TabBar extends TabGroup implements IsHtmlTable {

  private static final String DEFAULT_STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "TabBar-";

  private static final String STYLE_SUFFIX_HORIZONTAL = "-horizontal";
  private static final String STYLE_SUFFIX_VERTICAL = "-vertical";

  private final Orientation orientation;

  public TabBar(Orientation orientation) {
    this(DEFAULT_STYLE_PREFIX, orientation);
  }

  public TabBar(String stylePrefix, Orientation orientation) {
    super(stylePrefix, orientation.isVertical() ? new Vertical() : new Horizontal());
    this.orientation = orientation;

    addStyleName(getStyle(STYLE_PANEL));
  }

  @Override
  public void setBorderSpacing(int spacing) {
    getTable().setBorderSpacing(spacing);
  }

  @Override
  public void setDefaultCellClasses(String classes) {
    getTable().setDefaultCellClasses(classes);
  }

  @Override
  public void setDefaultCellStyles(String styles) {
    getTable().setDefaultCellStyles(styles);
  }

  @Override
  protected String getStyle(String stem) {
    return super.getStyle(stem)
        + (orientation.isVertical() ? STYLE_SUFFIX_VERTICAL : STYLE_SUFFIX_HORIZONTAL);
  }

  @Override
  protected void insertTabWidget(Widget widget, int beforeIndex, String styleName) {
    if (getTable().isEmpty() && getTable().getVerticalAlign() == null) {
      getTable().setVerticalAlign(VerticalAlign.BOTTOM);
    }

    super.insertTabWidget(widget, beforeIndex, styleName);
  }

  private CellVector getTable() {
    return (CellVector) getPanel();
  }
}

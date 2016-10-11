package com.butent.bee.client.grid.cell;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.CellContext;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.ui.CellType;
import com.butent.bee.shared.utils.BeeUtils;

public class SelectionCell extends AbstractCell<Boolean> {

  private static final InputElement INPUT;

  static {
    INPUT = Document.get().createCheckInputElement();
    INPUT.addClassName(BeeConst.CSS_CLASS_PREFIX + "SelectionCell");

    EventUtils.preventClickDebouncer(INPUT);
  }

  public SelectionCell() {
    super();
  }

  @Override
  public CellType getCellType() {
    return CellType.HTML;
  }

  @Override
  public String render(CellContext context, Boolean value) {
    boolean b = BeeUtils.unbox(value);

    INPUT.setChecked(b);
    INPUT.setDefaultChecked(b);

    return INPUT.getString();
  }

  public static void update(Element cellElement, boolean value) {
    if (cellElement != null) {
      DomUtils.setCheckValue(cellElement, value);
    }
  }
}

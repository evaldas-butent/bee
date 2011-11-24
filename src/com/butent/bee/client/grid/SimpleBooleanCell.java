package com.butent.bee.client.grid;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class SimpleBooleanCell extends AbstractCell<Boolean> {
  
  private static final InputElement INPUT = Document.get().createCheckInputElement();
  
  public SimpleBooleanCell() {
    super();
  }
  
  @Override
  public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
    boolean b = BeeUtils.unbox(value);
    if (INPUT.isChecked() != b) {
      INPUT.setChecked(b);
    }
    sb.append(SafeHtmlUtils.fromTrustedString(INPUT.getString()));
  }

  public void update(Element cellElement, boolean value) {
    if (cellElement == null) {
      return;
    }
    DomUtils.setCheckValue(cellElement, value);
  }
}

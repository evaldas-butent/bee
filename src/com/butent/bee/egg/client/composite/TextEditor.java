package com.butent.bee.egg.client.composite;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.ui.Composite;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.layout.BeeLayoutPanel;
import com.butent.bee.egg.client.widget.BeeButton;
import com.butent.bee.egg.client.widget.BeeLabel;
import com.butent.bee.egg.client.widget.BeeTextArea;
import com.butent.bee.egg.shared.BeeResource;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class TextEditor extends Composite implements HasId {

  public TextEditor(BeeResource resource) {
    BeeLayoutPanel p = new BeeLayoutPanel();
    
    String caption = BeeUtils.ifString(resource.getName(), resource.getUri());
    BeeLabel label = new BeeLabel(caption);
    p.add(label);
    p.setWidgetVerticalPosition(label, Layout.Alignment.BEGIN);
    p.setWidgetLeftRight(label, 10, Unit.PCT, 10, Unit.PX);
    
    BeeButton button = new BeeButton("Save");
    p.add(button);
    p.setWidgetVerticalPosition(button, Layout.Alignment.END);
    p.setWidgetLeftWidth(button, 42, Unit.PCT, 16, Unit.PCT);
    
    BeeTextArea area = new BeeTextArea(resource);
    p.add(area);
    p.setWidgetTopBottom(area, 2, Unit.EM, 2, Unit.EM);
    
    initWidget(p);
  }

  public void createId() {
    DomUtils.createId(this, "texteditor");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}

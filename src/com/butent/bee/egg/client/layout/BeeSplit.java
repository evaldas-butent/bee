package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.utils.JreEmulation;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.HasExtendedInfo;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

import java.util.ArrayList;
import java.util.List;

public class BeeSplit extends SplitLayoutPanel implements HasId, HasExtendedInfo {

  public BeeSplit() {
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "split");
  }

  public List<SubProp> getDirectionInfo(DockLayoutPanel.Direction dir) {
    Assert.notNull(dir);
    List<SubProp> lst = new ArrayList<SubProp>();

    List<Widget> children = getDirectionChildren(dir);
    int c = BeeUtils.length(children);
    
    PropUtils.addSub(lst, dir.toString(), "Widget Count", c);
    
    if (c > 0) {
      int i = 0;
      for (Widget w : children) {
        PropUtils.appendString(lst, BeeUtils.progress(++i, c), getChildInfo(w));
      }
    }
    
    return lst;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public List<SubProp> getInfo() {
    List<SubProp> lst = new ArrayList<SubProp>();
    
    PropUtils.addRoot(lst, JreEmulation.getSimpleName(this), 
        "Id", getId(),
        "Absolute Left", getAbsoluteLeft(),
        "Absolute Top", getAbsoluteTop(),
        "Offset Height", getOffsetHeight(),
        "Offset Width", getOffsetWidth(),
        "Style Name", getStyleName(),
        "Unit", getUnit(),
        "Widget Count", getWidgetCount());
    
    int i = 0;
    int c = getWidgetCount();
    for (Widget w : getChildren()) {
      String name = BeeUtils.concat(1, BeeUtils.progress(++i, c), getWidgetDirection(w));
      PropUtils.appendString(lst, name, getChildInfo(w));
    }
    
    return lst;
  }

  public void setDirectionSize(DockLayoutPanel.Direction direction, double size) {
    Assert.isTrue(validDirection(direction, false));

    for (Widget w : getChildren()) {
      if (getWidgetDirection(w) == direction) {
        setWidgetSize(w, size);
      }
    }
    
    forceLayout();
  }
  
  public boolean setDirectionSize(String s, double size) {
    DockLayoutPanel.Direction dir = DomUtils.getDirection(s);
    
    if (validDirection(dir, false)) {
      setDirectionSize(dir, size);
      return true;
    } else {
      return false;
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public boolean validDirection(DockLayoutPanel.Direction direction,
      boolean center) {
    if (direction == DockLayoutPanel.Direction.CENTER) {
      return center;
    }
    return BeeUtils.inList(direction, DockLayoutPanel.Direction.EAST,
        DockLayoutPanel.Direction.NORTH, DockLayoutPanel.Direction.SOUTH,
        DockLayoutPanel.Direction.WEST);
  }
  
  private List<StringProp> getChildInfo(Widget w) {
    List<StringProp> lst = new ArrayList<StringProp>();
    
    if (w instanceof HasId) {
      PropUtils.addString(lst, "Id", ((HasId) w).getId());
    }
    
    PropUtils.addString(lst, "Class", JreEmulation.getSimpleName(w), 
        "Absolute Left", w.getAbsoluteLeft(),
        "Absolute Top", w.getAbsoluteTop(),
        "Offset Height", w.getOffsetHeight(), "Offset Width",
        w.getOffsetWidth(), "Style Name", w.getStyleName(),
        "Title", w.getTitle(), "Visible", w.isVisible());
    
    if (w instanceof HasWidgets) {
      PropUtils.addString(lst, "Children Count", DomUtils.getWidgetCount((HasWidgets) w));
    }
    
    return lst;
  }
  
  private List<Widget> getDirectionChildren(DockLayoutPanel.Direction dir) {
    List<Widget> lst = new ArrayList<Widget>();
    
    for (Widget w : getChildren()) {
      if (getWidgetDirection(w) == dir) {
        lst.add(w);
      }
    }
    
    return lst;
  }
  
}

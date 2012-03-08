package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.HandlesAfterAdd;
import com.butent.bee.client.ui.HandlesBeforeAdd;
import com.butent.bee.shared.HasId;

/**
 * Contains a panel that stacks its children vertically, displaying only one at a time, with a
 * header for each child which the user can click to display.
 */

public class Stack extends StackLayoutPanel implements HasId {

  public Stack(Unit unit) {
    super(unit);
    DomUtils.createId(this, getIdPrefix());
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "stack";
  }

  @Override
  public void insert(Widget child, Widget header, double headerSize, int beforeIndex) {
    Widget cw = child;
    if (cw instanceof HandlesBeforeAdd) {
      cw = ((HandlesBeforeAdd) cw).onBeforeAdd(this);
    }
    Widget hw = header;
    if (hw instanceof HandlesBeforeAdd) {
      hw = ((HandlesBeforeAdd) hw).onBeforeAdd(this);
    }

    super.insert(cw, hw, headerSize, beforeIndex);

    if (cw instanceof HandlesAfterAdd) {
      ((HandlesAfterAdd) cw).onAfterAdd(this);
    }
    if (hw instanceof HandlesAfterAdd) {
      ((HandlesAfterAdd) hw).onAfterAdd(this);
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}

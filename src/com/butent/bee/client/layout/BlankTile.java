package com.butent.bee.client.layout;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.HasAfterAddHandler;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Handles separate areas(tiles) of the layout.
 */

public class BlankTile extends Composite implements HasAfterAddHandler, HasId,
    RequiresResize {
  private class UpdateCommand extends BeeCommand {
    @Override
    public void execute() {
      updateCaption();
    }
  }

  private BeeLabel caption = new BeeLabel();

  public BlankTile() {
    BeeLayoutPanel p = new BeeLayoutPanel();

    p.add(caption);
    p.setWidgetVerticalPosition(caption, Layout.Alignment.BEGIN);
    p.setWidgetLeftRight(caption, 10, Unit.PX, 10, Unit.PX);

    initWidget(p);
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "blank");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void onAfterAdd(HasWidgets parent) {
    Scheduler.get().scheduleDeferred(new UpdateCommand());
  }

  public void onResize() {
    updateCaption();
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void updateCaption() {
    if (!(getParent() instanceof TilePanel)) {
      return;
    }
    TilePanel t = (TilePanel) getParent();

    if (!Global.isDebug()) {
      caption.setText(BeeUtils.concat(1, t.getWidgetWidth(this),
          t.getWidgetHeight(this)));
      return;
    }

    StringBuilder sb = new StringBuilder();
    int c = 0;
    Widget w = getParent();
    Widget p = w.getParent();

    while (p instanceof TilePanel) {
      Direction direction = ((TilePanel) p).getWidgetDirection(w);
      switch (direction) {
        case CENTER:
          c++;
          break;
        default:
          if (c > 0) {
            sb.insert(0, c);
          }
          c = 0;
          if (direction != null) {
            sb.insert(0, direction.brief().toLowerCase());
          }
      }

      w = p;
      p = w.getParent();
    }
    if (c > 0) {
      sb.insert(0, c);
    }

    caption.setText(BeeUtils.concat(1, t.getId(), sb, t.getWidgetWidth(this),
        t.getWidgetHeight(this)));
  }

}

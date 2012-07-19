package com.butent.bee.client.layout;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Handles separate areas(tiles) of the layout.
 */

public class BlankTile extends Composite implements HasId, RequiresResize {

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
    DomUtils.createId(this, getIdPrefix());
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "blank";
  }

  public void onResize() {
    updateCaption();
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  @Override
  protected void onLoad() {
    super.onLoad();
    Scheduler.get().scheduleDeferred(new UpdateCommand());
  }

  private void updateCaption() {
    if (!(getParent() instanceof TilePanel)) {
      return;
    }
    TilePanel t = (TilePanel) getParent();
    caption.setText(BeeUtils.concat(1, t.getWidgetWidth(this), t.getWidgetHeight(this)));
  }
}

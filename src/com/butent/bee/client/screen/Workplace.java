package com.butent.bee.client.screen;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Place;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.TabbedPages.SelectionOrigin;
import com.butent.bee.client.ui.HandlesHistory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

class Workplace extends Place {

  private static final BeeLogger logger = LogUtils.getLogger(Workplace.class);
  
  private final Workspace workspace;
  
  Workplace(Workspace workspace, String id) {
    super(id);
    this.workspace = Assert.notNull(workspace);
  }

  @Override
  public boolean activate() {
    TilePanel tile = getTile();
    if (tile == null) {
      return false;
    }
    
    if (DomUtils.idEquals(workspace.getActivePanel(), getId())) {
      return true;
    }
    if (!tile.isLeaf()) {
      return false;
    }
    
    int index = BeeConst.UNDEF;
    for (int i = 0; i < workspace.getPageCount(); i++) {
      if (workspace.getContentWidget(i).getElement().isOrHasChild(tile.getElement())) {
        index = i;
        break;
      }
    }
    if (BeeConst.isUndef(index)) {
      logger.warning("page for workplace", getId(), "not found");
      return false;
    }

    workspace.selectPage(index, SelectionOrigin.SCRIPT);
    tile.activate(false);

    return true;
  }

  @Override
  public boolean onHistory(Place place, boolean forward) {
    TilePanel tile = getTile();
    if (tile == null) {
      return false;
    }

    if (!tile.isLeaf()) {
      return false;
    }
    
    IdentifiableWidget content = tile.getContent();
    if (content instanceof HandlesHistory) {
      boolean ok = ((HandlesHistory) content).onHistory(place, forward);
      if (ok) {
        return ok;
      }
    }

    return tile.onHistory(place, forward);
  }
  
  private TilePanel getTile() {
    Widget child = DomUtils.getChildQuietly(workspace, getId());
    if (child instanceof TilePanel) {
      return (TilePanel) child;
    } else {
      logger.warning("workplace", getId(), "not found");
      return null;
    }
  }
}

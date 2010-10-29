package com.butent.bee.egg.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.Panel;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.data.ResponseData;
import com.butent.bee.egg.client.utils.BeeXml;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.utils.BeeUtils;

class GridService extends CompositeService {

  private enum Stages {
    REQUEST_GRID, SHOW_GRID
  }

  private Stages stage = null;
  private Panel destination = null;

  protected GridService() {
  }

  protected GridService(String serviceId) {
    super(serviceId);
    nextStage();
  }

  @Override
  protected CompositeService create(String svcId) {
    return new GridService(svcId);
  }

  @Override
  protected boolean doStage(Object... params) {
    Assert.notNull(stage);
    boolean ok = true;

    switch (stage) {
      case REQUEST_GRID:
        destination = (Panel) params[0];
        String grd = (String) params[1];

        BeeKeeper.getRpc().makePostRequest(
            adoptService("rpc_ui_grid"),
            BeeXml.createString(BeeService.XML_TAG_DATA, "grid_name",
                grd.replaceFirst("['\\[\"](\\w+)['\\]\"][,].*", "$1")));
        break;

      case SHOW_GRID:
        JsArrayString arr = (JsArrayString) params[0];
        int cc = (Integer) params[1];

        BeeView view = new ResponseData(arr, cc);
        destination.add(BeeGlobal.simpleGrid(view));
        break;

      default:
        BeeGlobal.showError("Unhandled stage: " + stage);
        ok = false;
        break;
    }

    if (ok) {
      nextStage();
    } else {
      unregister();
    }
    return ok;
  }

  private void nextStage() {
    int x = 0;

    if (!BeeUtils.isEmpty(stage)) {
      x = stage.ordinal() + 1;
    }

    if (x < Stages.values().length) {
      stage = Stages.values()[x];
    } else {
      unregister();
    }
  }
}

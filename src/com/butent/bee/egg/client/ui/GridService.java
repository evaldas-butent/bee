package com.butent.bee.egg.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.Panel;

import com.butent.bee.egg.client.Global;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.data.ResponseData;
import com.butent.bee.egg.client.utils.XmlUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.data.HasTabularData;
import com.butent.bee.egg.shared.utils.BeeUtils;

class GridService extends CompositeService {

  private enum Stages {
    REQUEST_GRID, SHOW_GRID
  }

  private Stages stage = null;
  private Panel destination = null;

  protected GridService(String... serviceId) {
    super(serviceId);
    nextStage();
  }

  @Override
  protected CompositeService create(String svcId) {
    return new GridService(self(), svcId);
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
            XmlUtils.createString(BeeService.XML_TAG_DATA, "grid_name",
                grd.replaceFirst("['\\[\"](\\w+)['\\]\"][,].*", "$1")));
        break;

      case SHOW_GRID:
        JsArrayString arr = (JsArrayString) params[0];
        int cc = (Integer) params[1];

        HasTabularData view = new ResponseData(arr, cc);
        destination.add(Global.scrollGrid(view));
        break;

      default:
        Global.showError("Unhandled stage: " + stage);
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

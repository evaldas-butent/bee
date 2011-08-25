package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.Panel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements asynchronous creation of grids.
 */

class GridService extends CompositeService {

  @Override
  protected boolean doStage(String stg, Object... params) {
    boolean ok = true;

    final Panel destination = (Panel) params[0];
    String grd = BeeUtils.trim((String) params[1]);

    BeeKeeper.getRpc().makePostRequest(Service.GET_X_GRID,
        XmlUtils.createString(Service.XML_TAG_DATA, "grid_name",
            grd.replaceFirst("['\\[\"](\\w+)['\\]\"][,].*", "$1")),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject resp) {
            Assert.notNull(resp);

            if (resp.hasResponse(BeeRowSet.class)) {
              destination.add(Global.scrollGrid(BeeRowSet.restore((String) resp.getResponse())));
            }
          }
        });
    destroy();
    return ok;
  }

  @Override
  protected CompositeService getInstance() {
    return new GridService();
  }
}

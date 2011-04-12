package com.butent.bee.client.ui;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.Panel;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.utils.BeeUtils;

class GridService extends CompositeService {

  public static final String NAME = PREFIX + "grid";

  protected GridService(String... serviceId) {
    super(serviceId);
  }

  @Override
  protected CompositeService create(String svcId) {
    return new GridService(NAME, svcId);
  }

  @Override
  protected boolean doStage(String stg, Object... params) {
    boolean ok = true;

    final Panel destination = (Panel) params[0];
    String grd = (String) params[1];

    BeeKeeper.getRpc().makePostRequest(Service.GET_GRID,
        XmlUtils.createString(Service.XML_TAG_DATA, "grid_name",
            grd.replaceFirst("['\\[\"](\\w+)['\\]\"][,].*", "$1")),
        new ResponseCallback() {
          @Override
          public void onResponse(JsArrayString arr) {
            Assert.notNull(arr);
            Assert.parameterCount(arr.length(), 1);
            String data = arr.get(0);

            if (!BeeUtils.isEmpty(data)) {
              destination.add(Global.scrollGrid(BeeRowSet.restore(data)));
            }
          }
        });
    destroy();
    return ok;
  }

  @Override
  protected String getName() {
    return NAME;
  }
}

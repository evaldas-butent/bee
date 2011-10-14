package com.butent.bee.client.ui;

import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.BeeWidget;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Variable;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class DsnService extends CompositeService {

  public static final String SVC_GET_DSNS = Service.DATA_SERVICE_PREFIX + "get_dsns";
  public static final String SVC_SWITCH_DSN = Service.DATA_SERVICE_PREFIX + "switch_dsn";
  public static final String VAR_DSN = Service.RPC_VAR_PREFIX + "dsn";

  private Variable dsn;

  @Override
  protected boolean doStage(final String stg, Object... params) {
    boolean ok = true;

    if (stg.equals(SVC_GET_DSNS)) {
      BeeKeeper.getRpc().makeGetRequest(stg,
          new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              Assert.notNull(response);
              String[] dsns = null;

              if (response.hasResponse()) {
                dsns = Codec.beeDeserializeCollection((String) response.getResponse());
              }
              if (!BeeUtils.isEmpty(dsns)) {
                dsn = new Variable("Choose DSN", BeeType.STRING, BeeKeeper.getUser().getDsn(),
                    BeeWidget.LIST, dsns);

                Global.inputVars(getStage(SVC_SWITCH_DSN), "Available DSN's", dsn);
              } else {
                Global.showError("No DSN's available");
                destroy();
              }
            }
          });
      return ok;

    } else if (stg.equals(SVC_SWITCH_DSN)) {
      Global.closeDialog((GwtEvent<?>) params[0]);
      String dsnName = dsn.getValue();

      if (!BeeUtils.isEmpty(dsnName)) {
        ParameterList args = BeeKeeper.getRpc().createParameters(stg);
        args.addQueryItem(VAR_DSN, dsnName);

        BeeKeeper.getRpc().makeGetRequest(args,
            new ResponseCallback() {
              @Override
              public void onResponse(ResponseObject response) {
                Assert.notNull(response);

                if (response.hasResponse(String.class)) {
                  BeeKeeper.getUser().setDsn((String) response.getResponse());
                }
              }
            });
      }

    } else {
      ok = false;
      Global.showError("Unknown service [", name(), "] stage:", stg);
    }
    destroy();
    return ok;
  }

  @Override
  protected CompositeService getInstance() {
    return new DsnService();
  }
}

package com.butent.bee.client.modules.mail;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.commons.ParametersHandler;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.mail.MailConstants;

public class MailKeeper {

  private static final BeeLogger logger = LogUtils.getLogger(MailKeeper.class);

  public static void register() {
    BeeKeeper.getMenu().registerMenuCallback("mail_parameters", new MenuManager.MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        GridFactory.openGrid("Parameters", new ParametersHandler(parameters));
      }
    });
    BeeKeeper.getMenu().registerMenuCallback(MailConstants.SVC_RESTART_PROXY,
        new MenuManager.MenuCallback() {
          @Override
          public void onSelection(String parameters) {
            BeeKeeper.getRpc().makeGetRequest(createArgs(MailConstants.SVC_RESTART_PROXY),
                new ResponseCallback() {
                  @Override
                  public void onResponse(ResponseObject response) {
                    Assert.notNull(response);
                    response.log(logger);
                  }
                });
          }
        });
  }

  static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(MailConstants.MAIL_MODULE);
    args.addQueryItem(MailConstants.MAIL_METHOD, name);
    return args;
  }

  private MailKeeper() {
  }
}

package com.butent.bee.client.modules.mail;

import com.google.common.collect.ImmutableMap;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.commons.ParametersHandler;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.modules.mail.MailConstants;

import java.util.Map;

public class MailKeeper {

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
            BeeKeeper.getRpc().makeGetRequest(createArgs(MailConstants.SVC_RESTART_PROXY));
          }
        });

    FormFactory.registerFormCallback("Mail", new MailHandler());
    FormFactory.registerFormCallback("Message", new MessageHandler());

    GridFactory.registerGridCallback("Messages", new AbstractGridCallback() {
      @Override
      public Map<String, Filter> getInitialFilters() {
        return ImmutableMap.of("vvv",
            ComparisonFilter.isEqual("Recipient", new TextValue("test1@butent.lt")));
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

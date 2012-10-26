package com.butent.bee.client.modules.mail;

import com.google.common.collect.ImmutableMap;

import static com.butent.bee.shared.modules.mail.MailConstants.COL_USER;
import static com.butent.bee.shared.modules.mail.MailConstants.MAIL_METHOD;
import static com.butent.bee.shared.modules.mail.MailConstants.MAIL_MODULE;
import static com.butent.bee.shared.modules.mail.MailConstants.SVC_RESTART_PROXY;
import static com.butent.bee.shared.modules.mail.MailConstants.TBL_ACCOUNTS;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.commons.ParametersHandler;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;

import java.util.Map;

public class MailKeeper {

  public static void register() {
    BeeKeeper.getMenu().registerMenuCallback("mail_parameters", new MenuManager.MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        GridFactory.openGrid("Parameters", new ParametersHandler(parameters));
      }
    });
    BeeKeeper.getMenu().registerMenuCallback(SVC_RESTART_PROXY,
        new MenuManager.MenuCallback() {
          @Override
          public void onSelection(String parameters) {
            BeeKeeper.getRpc().makeGetRequest(createArgs(SVC_RESTART_PROXY));
          }
        });

    FormFactory.registerFormCallback("Mail", new MailHandler());

    GridFactory.registerGridCallback(TBL_ACCOUNTS, new AbstractGridCallback() {
      @Override
      public Map<String, Filter> getInitialFilters() {
        return ImmutableMap.of("UserFilter",
            ComparisonFilter.isEqual(COL_USER, new LongValue(BeeKeeper.getUser().getUserId())));
      }

      @Override
      public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
        newRow.setValue(DataUtils.getColumnIndex(COL_USER, gridView.getDataColumns()),
            BeeKeeper.getUser().getUserId());
        return true;
      }
    });
  }

  static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(MAIL_MODULE);
    args.addQueryItem(MAIL_METHOD, name);
    return args;
  }

  private MailKeeper() {
  }
}

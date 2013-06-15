package com.butent.bee.client.modules;

import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.client.modules.commons.CommonsKeeper;
import com.butent.bee.client.modules.crm.CrmKeeper;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.mail.MailKeeper;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.modules.transport.TransportHandler;
import com.butent.bee.client.utils.Command;

public class ModuleManager {

  public static void maybeInitialize(final Command command) {
    CalendarKeeper.ensureData(command);
  }

  public static void onLoad() {
    CommonsKeeper.register();
    TransportHandler.register();

    CrmKeeper.register();
    CalendarKeeper.register();
    MailKeeper.register();
    TradeKeeper.register();

    EcKeeper.register();
  }

  private ModuleManager() {
  }
}

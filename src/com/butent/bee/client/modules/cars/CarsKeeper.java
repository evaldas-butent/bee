package com.butent.bee.client.modules.cars;

import static com.butent.bee.shared.modules.cars.CarsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.rights.Module;

public final class CarsKeeper {

  public static ParameterList createSvcArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.CARS, method);
  }

  public static void register() {
    FormFactory.registerFormInterceptor(TBL_CONF_PRICELIST, new ConfPricelistForm());
    FormFactory.registerFormInterceptor(FORM_CONF_OPTION, new PhotoHandler());
    FormFactory.registerFormInterceptor(FORM_CAR_ORDER, new CarOrderForm());
  }

  private CarsKeeper() {
  }
}

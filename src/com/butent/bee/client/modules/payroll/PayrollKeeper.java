package com.butent.bee.client.modules.payroll;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.style.ColorStyleProvider;
import com.butent.bee.client.style.ConditionalStyle;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.rights.Module;

public final class PayrollKeeper {

  static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "payroll-";

  public static ParameterList createArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.PAYROLL, method);
  }

  public static void register() {
    ColorStyleProvider csp = ColorStyleProvider.createDefault(VIEW_TIME_CARD_CODES);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TIME_CARD_CODES, COL_BACKGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TIME_CARD_CODES, COL_FOREGROUND, csp);

    csp = ColorStyleProvider.createDefault(VIEW_TIME_RANGES);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TIME_RANGES, COL_BACKGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TIME_RANGES, COL_FOREGROUND, csp);

    csp = ColorStyleProvider.createDefault(VIEW_TIME_CARD_CHANGES);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TIME_CARD_CHANGES,
        COL_TIME_CARD_FROM, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TIME_CARD_CHANGES,
        COL_TIME_CARD_UNTIL, csp);

    FormFactory.registerFormInterceptor(FORM_WORK_SCHEDULE, new WorkScheduleForm());
  }

  private PayrollKeeper() {
  }
}

package com.butent.bee.client.modules.trade.acts;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.style.ColorStyleProvider;
import com.butent.bee.client.style.ConditionalStyle;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.EnablableWidget;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.ViewFactory;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.menu.MenuHandler;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

public final class TradeActKeeper {

  static final String STYLE_PREFIX = StyleUtils.CLASS_NAME_PREFIX + "ta-";

  private static final String STYLE_COMMAND_PREFIX = STYLE_PREFIX + "command-";
  private static final String STYLE_COMMAND_DISABLED = STYLE_COMMAND_PREFIX + "disabled";

  private static final BeeLogger logger = LogUtils.getLogger(TradeActKeeper.class);

  private static final String GRID_ALL_ACTS_KEY = GRID_TRADE_ACTS + BeeConst.STRING_UNDER
      + BeeConst.ALL;

  public static void register() {
    for (TradeActKind kind : TradeActKind.values()) {
      if (kind.getGridSupplierKey() != null) {
        GridFactory.registerGridSupplier(kind.getGridSupplierKey(), GRID_TRADE_ACTS,
            new TradeActGrid(kind));
      }
    }

    GridFactory.registerGridSupplier(GRID_ALL_ACTS_KEY, GRID_TRADE_ACTS, new TradeActGrid(null));

    MenuService.TRADE_ACT_NEW.setHandler(new MenuHandler() {
      @Override
      public void onSelection(String parameters) {
        TradeActKind kind = EnumUtils.getEnumByName(TradeActKind.class, parameters);

        if (kind == null) {
          logger.severe(MenuService.TRADE_ACT_NEW.name(), "kind not recognized", parameters);

        } else {
          DataInfo dataInfo = Data.getDataInfo(VIEW_TRADE_ACTS);

          BeeRow row = RowFactory.createEmptyRow(dataInfo, true);
          row.setValue(dataInfo.getColumnIndex(COL_TA_KIND), kind.ordinal());

          RowFactory.createRow(dataInfo, row);
        }
      }
    });

    MenuService.TRADE_ACT_LIST.setHandler(new MenuHandler() {
      @Override
      public void onSelection(String parameters) {
        if (BeeUtils.same(parameters, BeeConst.ALL)) {
          ViewFactory.createAndShow(GRID_ALL_ACTS_KEY);

        } else {
          TradeActKind kind = EnumUtils.getEnumByName(TradeActKind.class, parameters);

          if (kind != null && kind.getGridSupplierKey() != null) {
            ViewFactory.createAndShow(kind.getGridSupplierKey());
          } else {
            logger.severe(GRID_TRADE_ACTS, "kind not recognized", parameters);
          }
        }
      }
    });

    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_ACTS, COL_TA_OPERATION,
        ColorStyleProvider.create(VIEW_TRADE_ACTS,
            ALS_OPERATION_BACKGROUND, ALS_OPERATION_FOREGROUND));
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_ACTS, COL_TA_STATUS,
        ColorStyleProvider.create(VIEW_TRADE_ACTS,
            ALS_STATUS_BACKGROUND, ALS_STATUS_FOREGROUND));

    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_ACT_TEMPLATES, COL_TA_OPERATION,
        ColorStyleProvider.create(VIEW_TRADE_ACT_TEMPLATES,
            ALS_OPERATION_BACKGROUND, ALS_OPERATION_FOREGROUND));
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_ACT_TEMPLATES, COL_TA_STATUS,
        ColorStyleProvider.create(VIEW_TRADE_ACT_TEMPLATES,
            ALS_STATUS_BACKGROUND, ALS_STATUS_FOREGROUND));

    FormFactory.registerFormInterceptor(FORM_TRADE_ACT, new TradeActForm());
  }

  static void addCommandStyle(Widget command, String suffix) {
    command.addStyleName(STYLE_COMMAND_PREFIX + suffix);
  }

  static ParameterList createArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.TRADE, SubModule.ACTS, method);
  }

  static void setCommandEnabled(EnablableWidget command, boolean enabled) {
    command.setEnabled(enabled);
    command.setStyleName(STYLE_COMMAND_DISABLED, !enabled);
  }

  private TradeActKeeper() {
  }
}

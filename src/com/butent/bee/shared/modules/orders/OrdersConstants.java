package com.butent.bee.shared.modules.orders;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.EnumUtils;

public final class OrdersConstants {
  public enum OrdersStatus implements HasLocalizedCaption {
    APPROVED {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.ordApproved();
      }
    },
    CANCELED {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.ordCanceled();
      }
    },
    PREPARED {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.ordPrepared();
      }
    },
    SENT {
      @Override
      public String getCaption(LocalizableConstants constants) {
        return constants.ordSent();
      }
    };

    @Override
    public String getCaption() {
      return getCaption(Localized.getConstants());
    }

    public boolean is(Integer status) {
      return status != null && ordinal() == status;
    }
  }

  public static void register() {
    EnumUtils.register(OrdersStatus.class);
  }

  public static final String SVC_GET_TEMPLATE_ITEMS = "GetTemplateItems";

  public static final String TBL_ORDER_ITEMS = "OrderItems";

  public static final String VIEW_ORDERS = "Orders";
  public static final String VIEW_ORDERS_TEMPLATES = "OrdersTemplates";
  public static final String VIEW_ORDER_TMPL_ITEMS = "OrderTmplItems";

  public static final String COL_ORDER = "Order";
  public static final String COL_ORDERS_STATUS = "Status";
  public static final String COL_TEMPLATE = "Template";

  private OrdersConstants() {
  }
}

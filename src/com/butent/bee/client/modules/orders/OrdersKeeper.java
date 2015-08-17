package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.orders.OrdersConstants.COL_ORDER;
import static com.butent.bee.shared.modules.orders.OrdersConstants.VIEW_ORDER_SALES;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.shared.rights.Module;

/**
 * Client-side projects module handler.
 */
public final class OrdersKeeper {

  /**
   * Creates rpc parameters of orders module.
   * 
   * @param method name of method.
   * @return rpc parameters to call queries of server-side.
   */
  public static ParameterList createSvcArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.ORDERS, method);
  }

  /**
   * Register orders client-side module handler.
   */
  public static void register() {
    FormFactory.registerFormInterceptor(COL_ORDER, new OrderForm());
    FormFactory.registerFormInterceptor("OrderInvoice", new OrderInvoiceForm());

    GridFactory.registerGridInterceptor(VIEW_ORDER_SALES, new OrderInvoiceBuilder());

    SelectorEvent.register(new OrdersSelectorHandler());
  }

  private OrdersKeeper() {

  }
}
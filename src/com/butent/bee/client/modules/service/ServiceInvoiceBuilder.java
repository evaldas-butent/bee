package com.butent.bee.client.modules.service;

import static com.butent.bee.shared.modules.service.ServiceConstants.SVC_CREATE_RESERVATION_INVOICE_ITEMS;

import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.modules.orders.OrderInvoiceBuilder;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

public class ServiceInvoiceBuilder extends OrderInvoiceBuilder {

  @Override
  public GridInterceptor getInstance() {
    return new ServiceInvoiceBuilder();
  }

  @Override
  public ParameterList getRequestArgs() {
    return ServiceKeeper.createArgs(SVC_CREATE_RESERVATION_INVOICE_ITEMS);
  }
}
package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.HandlesUpdateEvents;
import com.butent.bee.shared.data.event.ModificationEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class OrdersObserver implements RowInsertEvent.Handler, HandlesUpdateEvents {

  public static OrdersObserver register() {
    return new OrdersObserver();
  }

  private final String viewName = OrdersConstants.VIEW_ORDERS;

  private OrdersObserver() {
    BeeKeeper.getBus().registerUpdateHandler(this, false);
    BeeKeeper.getBus().registerRowInsertHandler(this, false);
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (isTalkative(event)) {
      sayHello(event.getRowId());
    }
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
    if (isTalkative(event)) {
      sayHello(event.getRowId());
    }
  }

  private boolean isTalkative(ModificationEvent<?> event) {
    return event.hasView(viewName) && !event.isSpookyActionAtADistance();
  }

  private void sayHello(Long id) {
    if (!DataUtils.isId(id)) {
      return;
    }
    ParameterList args = OrdersKeeper.createSvcArgs(SVC_GET_CREDIT_INFO);
    args.addDataItem(viewName, id);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (response.hasErrors()) {
          return;
        }
        Map<String, String> result = Codec.deserializeMap(response.getResponseAsString());
        double limit = BeeUtils.toDouble(result.get(ClassifierConstants.COL_COMPANY_CREDIT_LIMIT));
        double debt = BeeUtils.toDouble(result.get(TradeConstants.VAR_DEBT));
        double overdue = BeeUtils.toDouble(result.get(TradeConstants.VAR_OVERDUE));
        double income = BeeUtils.toDouble(result.get(VAR_INCOME));

        if (overdue > 0 || (debt + income) > limit) {
          String cap = result.get(ClassifierConstants.COL_COMPANY_NAME);
          List<String> msgs = new ArrayList<>();

          msgs.add(BeeUtils.join(": ", Localized.dictionary().creditLimit(),
              BeeUtils.joinWords(limit, result.get(COL_CURRENCY))));
          msgs.add(BeeUtils.join(": ", Localized.dictionary().trdDebt(), debt));

          if (overdue > 0) {
            msgs.add(BeeUtils.join(": ", Localized.dictionary().trdOverdue(), overdue));
          }
          if (income > 0) {
            msgs.add(BeeUtils.join(": ", Localized.dictionary().trOrders(), income));
          }
          Global.showError(cap, msgs);
        }
      }
    });
  }
}
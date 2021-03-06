package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.COL_CURRENCY;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.screen.BodyPanel;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.HandlesUpdateEvents;
import com.butent.bee.shared.data.event.ModificationEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CargoIncomesObserver implements RowInsertEvent.Handler, HandlesUpdateEvents {

  public static CargoIncomesObserver register() {
    return new CargoIncomesObserver();
  }

  private static final String viewName = TBL_CARGO_INCOMES;

  private CargoIncomesObserver() {
    BeeKeeper.getBus().registerUpdateHandler(this, false);
    BeeKeeper.getBus().registerRowInsertHandler(this, false);
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
    if (isTalkative(event) && BeeUtils.inListSame(event.getSourceName(), COL_DATE, COL_AMOUNT,
        COL_CURRENCY, COL_TRADE_VAT_PLUS, COL_TRADE_VAT, COL_TRADE_VAT_PERC)) {
      sayHello(event.getRowId());
    }
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
    if (isTalkative(event)) {
      sayHello(event.getRowId());
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    if (isTalkative(event)) {
      sayHello(event.getRowId());
    }
  }

  private static boolean isTalkative(ModificationEvent<?> event) {
    return event.hasView(viewName) && !event.isSpookyActionAtADistance();
  }

  private static void sayHello(Long id) {
    if (!DataUtils.isId(id)) {
      return;
    }
    String company = null;

    for (GridView grid : ViewHelper.getGrids(BodyPanel.get())) {
      if (Objects.equals(grid.getViewName(), viewName)
          && grid.getRowData().stream().anyMatch(o -> Objects.equals(o.getId(), id))) {

        FormView form = ViewHelper.getForm(grid);

        if (Objects.nonNull(form)) {
          company = BeeUtils.notEmpty(form.getStringValue(COL_PAYER),
              form.getStringValue(COL_CUSTOMER));
          break;
        }
      }
    }
    ParameterList args = TransportHandler.createArgs(SVC_GET_CREDIT_INFO);
    args.addDataItem(COL_CARGO_INCOME, id);
    args.addNotEmptyData(ClassifierConstants.COL_COMPANY, company);

    BeeKeeper.getRpc().makePostRequest(args, response -> {
      response.notify(BeeKeeper.getScreen());

      if (response.hasErrors()) {
        return;
      }
      Map<String, String> result = Codec.deserializeLinkedHashMap(response.getResponseAsString());
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
    });
  }
}

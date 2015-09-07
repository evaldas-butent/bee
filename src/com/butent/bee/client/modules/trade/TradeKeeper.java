package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.Notification;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.trade.acts.TradeActKeeper;
import com.butent.bee.client.modules.trade.acts.TradeSaleInvoiceForm;
import com.butent.bee.client.style.ColorStyleProvider;
import com.butent.bee.client.style.ConditionalStyle;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.grid.interceptor.UniqueChildInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.utils.Codec;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;

public final class TradeKeeper {

  public static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "trade-";

  public static ParameterList createArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.TRADE, method);
  }

  public static IdentifiableWidget createAmountAction(final String viewName, final Filter filter,
      final String salesRelColumn, final NotificationListener listener) {

    Assert.notEmpty(viewName);

    FaLabel summary = new FaLabel(FontAwesome.LINE_CHART);
    summary.setTitle(Localized.getConstants().totalOf());

    summary.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ParameterList args = createArgs(SVC_GET_SALE_AMOUNTS);
        args.addDataItem(VAR_VIEW_NAME, viewName);
        args.addDataItem(Service.VAR_COLUMN, salesRelColumn);

        if (filter != null) {
          args.addDataItem(EcConstants.VAR_FILTER, Codec.beeSerialize(filter));
        }

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            response.notify(listener);
          }
        });
      }
    });

    return summary;
  }

  public static void register() {
    GridFactory.registerGridInterceptor(VIEW_PURCHASE_ITEMS, new TradeItemsGrid());
    GridFactory.registerGridInterceptor(VIEW_SALE_ITEMS, new TradeItemsGrid());

    GridFactory.registerGridInterceptor(GRID_SERIES_MANAGERS,
        UniqueChildInterceptor.forUsers(Localized.getConstants().managers(),
            COL_SERIES, COL_TRADE_MANAGER));
    GridFactory.registerGridInterceptor(GRID_DEBTS, new DebtsGrid());
    GridFactory.registerGridInterceptor(GRID_DEBT_REPORTS, new DebtReportsGrid());
    GridFactory.registerGridInterceptor(GRID_SALES, new SalesGrid());
    GridFactory.registerGridInterceptor(GRID_ERP_SALES, new ERPSalesGrid());

    FormFactory.registerFormInterceptor(FORM_SALES_INVOICE, new SalesInvoiceForm());

    ColorStyleProvider csp = ColorStyleProvider.createDefault(VIEW_TRADE_OPERATIONS);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_OPERATIONS, COL_BACKGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_OPERATIONS, COL_FOREGROUND, csp);

    csp = ColorStyleProvider.createDefault(VIEW_TRADE_STATUSES);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_STATUSES, COL_BACKGROUND, csp);
    ConditionalStyle.registerGridColumnStyleProvider(GRID_TRADE_STATUSES, COL_FOREGROUND, csp);

    FormFactory.registerFormInterceptor(FORM_SALES_INVOICE, new TradeSaleInvoiceForm());

    if (ModuleAndSub.of(Module.TRADE, SubModule.ACTS).isEnabled()) {
      TradeActKeeper.register();
    }
  }

  private TradeKeeper() {
  }
}

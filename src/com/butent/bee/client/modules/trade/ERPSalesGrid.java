package com.butent.bee.client.modules.trade;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.modules.trade.acts.TradeActKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.acts.TradeActConstants;
import com.butent.bee.shared.ui.Action;

class ERPSalesGrid extends AbstractGridInterceptor {

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    HeaderView header = presenter.getHeader();

    header.clearCommandPanel();

    final FaLabel action = new FaLabel(FontAwesome.EXCHANGE);
    action.setTitle(Localized.getConstants().trdSynchronizeWithERP());
    action.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        doSynchronize(presenter, action);
      }
    });

    header.addCommandItem(action);
  }

  @Override
  public GridInterceptor getInstance() {
    return new ERPSalesGrid();
  }

  private static void doSynchronize(final GridPresenter presenter, final FaLabel widget) {
    ParameterList prm = TradeActKeeper.createArgs(TradeActConstants.SVC_SYNCHRONIZE_ERP_DATA);
    widget.setChar(FontAwesome.SPINNER);
    widget.setEnabled(false);
    BeeKeeper.getRpc().makePostRequest(prm, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasNotifications()) {
          presenter.getGridView().notifyInfo(response.getNotifications());
        } else {
          presenter.getGridView().notifySevere(response.getErrors());
        }
        presenter.handleAction(Action.REFRESH);
        widget.setEnabled(true);
        widget.setChar(FontAwesome.EXCHANGE);
      }
    });
  }

}

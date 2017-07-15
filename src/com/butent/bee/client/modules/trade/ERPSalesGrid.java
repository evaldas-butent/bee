package com.butent.bee.client.modules.trade;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.modules.trade.TradeKeeper.FilterCallback;
import com.butent.bee.client.modules.trade.acts.TradeActKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActConstants;
import com.butent.bee.shared.ui.Action;

import java.util.Collection;

class ERPSalesGrid extends AbstractGridInterceptor {

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    HeaderView header = presenter.getHeader();

    header.clearCommandPanel();

    final FaLabel action = new FaLabel(FontAwesome.EXCHANGE);
    action.setTitle(Localized.dictionary().trdSynchronizeWithERP());
    action.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        doSynchronize(presenter, action);
      }
    });

    header.addCommandItem(TradeKeeper.createAmountAction(presenter.getViewName(),
        new FilterCallback() {

          @Override
          public Filter getFilter() {
            return presenter.getDataProvider().getFilter();
          }
        }, Data.getIdColumn(presenter.getViewName()),
        presenter.getGridView()));
    header.addCommandItem(action);
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
                                  Collection<RowInfo> selectedRows, DeleteMode defMode) {
    if (BeeKeeper.getUser().canDeleteData(TradeConstants.TBL_ERP_SALES)) {
      return DeleteMode.SINGLE;
    } else {
      presenter.getGridView().notifySevere(Localized.dictionary().rowIsNotRemovable());
      return DeleteMode.CANCEL;
    }
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

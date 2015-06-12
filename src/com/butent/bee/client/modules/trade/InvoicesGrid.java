package com.butent.bee.client.modules.trade;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InvoicesGrid extends AbstractGridInterceptor implements ClickHandler {

  private final Button action = new Button(Localized.getConstants().trSendToERP(), this);

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    presenter.getHeader().clearCommandPanel();
    presenter.getHeader().addCommandItem(action);
  }

  @Override
  public GridInterceptor getInstance() {
    return new InvoicesGrid();
  }

  @Override
  public void onClick(ClickEvent event) {
    final GridPresenter presenter = getGridPresenter();
    final Set<Long> selIds = new HashSet<>();

    for (RowInfo row : presenter.getGridView().getSelectedRows(SelectedRows.ALL)) {
      selIds.add(row.getId());
    }
    if (selIds.isEmpty()) {
      presenter.getGridView().notifyWarning(Localized.getConstants().selectAtLeastOneRow());
      return;
    }

    Queries.getRowSet(getViewName(), null, Filter.and(Filter.equals(
        TradeConstants.COL_TRADE_SENT_ERP,
        BeeConst.INT_TRUE), Filter.idIn(selIds)), new RowSetCallback() {

      @Override
      public void onSuccess(BeeRowSet result) {
        final List<Long> ids = result.getRowIds();

        if (ids.isEmpty()) {
          presenter.getGridView().notifyWarning(
              Localized.getConstants().trdSelectedRowsNotSentERP());
          return;
        }

        String msg = Localized.getConstants().trSendToERPConfirm();

        if (BeeUtils.size(ids) < BeeUtils.size(selIds)) {
          msg = Localized.getConstants().trdSomeSelectedRowsNotSentQuestion();
        }

        Global.confirm(msg, new ConfirmationCallback() {
          @Override
          public void onConfirm() {
            final HeaderView header = presenter.getHeader();
            header.clearCommandPanel();
            header.addCommandItem(new Image(Global.getImages().loading()));

            ParameterList args = TradeKeeper.createArgs(TradeConstants.SVC_SEND_TO_ERP);
            args.addDataItem(TradeConstants.VAR_VIEW_NAME, getGridPresenter().getViewName());
            args.addDataItem(TradeConstants.VAR_ID_LIST, DataUtils.buildIdList(ids));

            BeeKeeper.getRpc().makePostRequest(args, getERPResponseCallback());
          }
        });
      }
    });

  }

  protected ResponseCallback getERPResponseCallback() {
    final GridPresenter presenter = getGridPresenter();
    final HeaderView header = presenter.getHeader();

    return new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        header.clearCommandPanel();
        header.addCommandItem(action);
        response.notify(BeeKeeper.getScreen());
        Data.onViewChange(presenter.getViewName(), DataChangeEvent.CANCEL_RESET_REFRESH);
      }

    };
  }
}

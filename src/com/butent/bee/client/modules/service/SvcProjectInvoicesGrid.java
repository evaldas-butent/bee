package com.butent.bee.client.modules.service;

import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;

import java.util.Set;

public class SvcProjectInvoicesGrid extends AbstractGridInterceptor implements ClickHandler {
  private static final LocalizableConstants localizableConstants = Localized.getConstants();

  private final Button action = new Button(localizableConstants.trSendToERP(), this);

  public SvcProjectInvoicesGrid() {
  }

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    HeaderView header = presenter.getHeader();
    header.clearCommandPanel();
    header.addCommandItem(action);
  }

  @Override
  public GridInterceptor getInstance() {
    return new SvcProjectInvoicesGrid();
  }

  @Override
  public void onClick(ClickEvent event) {
    final GridPresenter presenter = getGridPresenter();
    final Set<Long> ids = Sets.newHashSet();

    for (RowInfo row : presenter.getGridView().getSelectedRows(SelectedRows.ALL)) {
      ids.add(row.getId());
    }
    if (ids.isEmpty()) {
      presenter.getGridView().notifyWarning(Localized.getConstants().selectAtLeastOneRow());
      return;
    }

    Global.confirm(Localized.getConstants().trSendToERPConfirm(), new ConfirmationCallback() {
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

  private ResponseCallback getERPResponseCallback() {
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
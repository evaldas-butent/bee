package com.butent.bee.client.modules.transport;

import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.SVC_SEND_TO_ERP;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;

import java.util.Set;

public class CargoInvoicesGrid extends AbstractGridInterceptor implements ClickHandler {

  private final Button action = new Button(Localized.getConstants().trSendToERP(), this);

  @Override
  public GridInterceptor getInstance() {
    return new CargoInvoicesGrid();
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

        ParameterList args = TransportHandler.createArgs(SVC_SEND_TO_ERP);
        args.addDataItem("view_name", getGridPresenter().getViewName());
        args.addDataItem("IdList", DataUtils.buildIdList(ids));

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            header.clearCommandPanel();
            header.addCommandItem(action);
            response.notify(presenter.getGridView());
            Data.onViewChange(presenter.getViewName(), DataChangeEvent.CANCEL_RESET_REFRESH);
          }
        });
      }
    });
  }

  @Override
  public void onShow(GridPresenter presenter) {
    presenter.getHeader().clearCommandPanel();
    presenter.getHeader().addCommandItem(action);
  }
}

package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
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
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.rights.RegulatedWidget;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.Set;

public class ERPTripCostsGrid extends AbstractGridInterceptor implements ClickHandler {

  private final Button action = new Button(Localized.dictionary().trSendToERP(), this);

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (BeeKeeper.getUser().isWidgetVisible(RegulatedWidget.TO_ERP)) {
      presenter.getHeader().addCommandItem(action);
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new ERPTripCostsGrid();
  }

  @Override
  public void onClick(ClickEvent event) {
    final GridPresenter presenter = getGridPresenter();
    final Set<Long> ids = new HashSet<>();

    for (RowInfo row : presenter.getGridView().getSelectedRows(SelectedRows.ALL)) {
      ids.add(row.getId());
    }
    if (ids.isEmpty()) {
      presenter.getGridView().notifyWarning(Localized.dictionary().selectAtLeastOneRow());
      return;
    }

    Global.confirm(Localized.dictionary().trSendToERPConfirm(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        final HeaderView header = presenter.getHeader();
        header.clearCommandPanel();
        header.addCommandItem(new Image(Global.getImages().loading()));

        ParameterList args = TransportHandler.createArgs(TransportConstants.SVC_COSTS_TO_ERP);
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

        if (response.hasErrors()) {
          response.notify(getGridView());
          return;
        }
        Data.onViewChange(presenter.getViewName(), DataChangeEvent.CANCEL_RESET_REFRESH);

        SimpleRowSet simple = SimpleRowSet.restore(response.getResponseAsString());

        if (simple.isEmpty()) {
          getGridView().notifyInfo(Localized.dictionary().ok());
        } else {
          BeeRowSet rs = new BeeRowSet();

          for (String col : simple.getColumnNames()) {
            rs.addColumn(ValueType.TEXT, BeeUtils.proper(col, BeeConst.CHAR_UNDER));
          }
          int c = 0;

          for (SimpleRowSet.SimpleRow row : simple) {
            rs.addRow(++c, row.getValues());
          }
          Global.showModalGrid(Localized.dictionary().errors(), rs);
        }
      }
    };
  }
}

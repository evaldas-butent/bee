package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.utils.BeeUtils;

public abstract class PercentEditor extends AbstractGridInterceptor {
  @Override
  public void onEditStart(EditStartEvent event) {
    if (!event.isReadOnly()
        && BeeUtils.inList(event.getColumnId(), COL_TRIP_PERCENT, COL_CARGO_PERCENT)) {

      updateFreight(getGridPresenter(), event.getRowValue().getId(), event.getColumnId());
      event.consume();
      return;
    }
    super.onEditStart(event);
  }

  @Override
  public void onLoad(GridView gridView) {
    gridView.getViewPresenter().getHeader().addCommandItem(new MessageBuilder(gridView));
    super.onLoad(gridView);
  }

  private static void updateFreight(GridPresenter presenter, Long id, String percentColumn) {
    InputNumber amount = new InputNumber();
    InputBoolean percent = new InputBoolean("%");

    Horizontal widget = new Horizontal();
    widget.add(amount);
    widget.add(percent);

    Global.inputWidget(Data.getColumnLabel(presenter.getViewName(), percentColumn), widget, () -> {
      ParameterList args = TransportHandler.createArgs(SVC_UPDATE_PERCENT);
      args.addDataItem(COL_CARGO_TRIP, id);
      args.addDataItem(Service.VAR_COLUMN, percentColumn);
      args.addNotEmptyData(COL_AMOUNT, amount.getValue());
      args.addDataItem(Service.VAR_CHECK, BeeUtils.toString(percent.isChecked()));

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(presenter.getGridView());

          if (!response.hasErrors()) {
            presenter.refresh(true, false);
          }
        }
      });
    });
  }
}

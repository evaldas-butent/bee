package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;

public class TripCostsGrid extends AbstractGridInterceptor implements ClickHandler {

  Long trip;
  FaLabel dailyCosts;

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    dailyCosts = new FaLabel(FontAwesome.MONEY);
    dailyCosts.setTitle(Localized.getConstants().trGenerateDailyCosts());
    dailyCosts.addClickHandler(this);
    presenter.getHeader().addCommandItem(dailyCosts);
    dailyCosts.setVisible(DataUtils.isId(trip));

    super.afterCreatePresenter(presenter);
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    Global.confirm(Localized.getConstants().trGenerateDailyCosts(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        ParameterList args = TransportHandler.createArgs(SVC_GENERATE_DAILY_COSTS);
        args.addDataItem(COL_TRIP, trip);

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            response.notify(getGridView());

            if (response.hasErrors()) {
              return;
            }
            getGridPresenter().refresh(false);
          }
        });
      }
    });
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    trip = event.getRowId();

    if (dailyCosts != null) {
      dailyCosts.setVisible(DataUtils.isId(trip));
    }
    super.onParentRow(event);
  }
}

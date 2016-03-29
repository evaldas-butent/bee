package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.Codec;

public class AssessmentRequestsGrid extends AbstractGridInterceptor {

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    FaLabel summary = new FaLabel(FontAwesome.LINE_CHART);
    summary.setTitle(Localized.dictionary().totalOf());

    summary.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        ParameterList args =
            TransportHandler.createArgs(TransportConstants.SVC_GET_ASSESSMENT_AMOUNTS);

        args.addDataItem(TradeConstants.VAR_VIEW_NAME, presenter.getViewName());
        args.addDataItem(EcConstants.VAR_FILTER,
            Codec.beeSerialize(presenter.getDataProvider().getFilter()));

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            response.notify(presenter.getGridView());
          }
        });
      }
    });
    presenter.getHeader().addCommandItem(summary);
    super.afterCreatePresenter(presenter);
  }

  @Override
  public GridInterceptor getInstance() {
    return new AssessmentRequestsGrid();
  }
}

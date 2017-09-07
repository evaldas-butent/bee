package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CustomAction;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.Codec;

public class AssessmentRequestsGrid extends AbstractGridInterceptor {

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    Holder<CustomAction> button = Holder.absent();

    CustomAction summary = new CustomAction(FontAwesome.LINE_CHART, clickEvent -> {
      button.get().running();
      ParameterList args = TransportHandler.createArgs(SVC_GET_ASSESSMENT_AMOUNTS);

      args.addDataItem(TradeConstants.VAR_VIEW_NAME, presenter.getViewName());
      args.addDataItem(EcConstants.VAR_FILTER,
          Codec.beeSerialize(presenter.getDataProvider().getFilter()));

      BeeKeeper.getRpc().makePostRequest(args, response -> {
        response.notify(presenter.getGridView());
        button.get().idle();
      });
    });
    button.set(summary);
    summary.setTitle(Localized.dictionary().totalOf());
    presenter.getHeader().addCommandItem(summary);
    super.afterCreatePresenter(presenter);
  }

  @Override
  public GridInterceptor getInstance() {
    return new AssessmentRequestsGrid();
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow, boolean copy) {
    newRow.setValue(gridView.getDataIndex(COL_ASSESSMENT_STATUS), AssessmentStatus.NEW.ordinal());
    newRow.setValue(gridView.getDataIndex(ALS_ORDER_STATUS), OrderStatus.REQUEST.ordinal());
    return super.onStartNewRow(gridView, oldRow, newRow, copy);
  }
}

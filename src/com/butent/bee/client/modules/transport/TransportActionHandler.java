package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.event.RowActionEvent.Handler;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;

public class TransportActionHandler implements Handler {

  @Override
  public void onRowAction(RowActionEvent event) {
    if (!event.hasService(Service.CELL_ACTION)) {
      return;
    }
    if (BeeUtils.inListSame(event.getViewName(), VIEW_CARGO_TRIPS, VIEW_ALL_CARGO)) {
      event.consume();
      Long tripId = Data.getLong(event.getViewName(), event.getRow(), COL_TRIP);

      if (DataUtils.isId(tripId)) {
        DataInfo data = Data.getDataInfo(BeeUtils
            .isEmpty(Data.getString(event.getViewName(), event.getRow(), "ExpeditionType"))
            ? VIEW_TRIPS : VIEW_EXPEDITION_TRIPS);

        RowEditor.openRow(data.getEditForm(), data, tripId);
      }
    } else if (BeeUtils.inListSame(event.getViewName(),
        TBL_CARGO_ASSESSORS, "AssessmentForwarders")) {
      event.consume();
      AssessmentForm.doRowAction(event);
    }
  }
}

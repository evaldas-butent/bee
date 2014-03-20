package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.transport.TransportConstants.AssessmentStatus;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.ui.GridDescription;

public class AssessmentRequestsGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new AssessmentRequestsGrid();
  }

  @Override
  public boolean onLoad(GridDescription gridDescription) {
    gridDescription.setFilter(Filter.and(Filter.equals(COL_ORDER_MANAGER,
        BeeKeeper.getUser().getUserId()), Filter.notNull(COL_ASSESSMENT_STATUS)));
    return true;
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    newRow.setValue(gridView.getDataIndex(COL_ASSESSMENT_STATUS), AssessmentStatus.NEW.ordinal());
    newRow.setValue(gridView.getDataIndex(ALS_ORDER_STATUS), OrderStatus.REQUEST.ordinal());
    return true;
  }
}

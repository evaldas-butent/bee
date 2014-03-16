package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.transport.TransportConstants.AssessmentStatus;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.ui.GridDescription;

import java.util.Collection;

public class AssessmentRequestsGrid extends AbstractGridInterceptor {

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {

    GridView gridView = presenter.getGridView();

    boolean primary = !DataUtils.isId(activeRow.getLong(gridView.getDataIndex(COL_ASSESSMENT)));
    boolean owner = Objects.equal(activeRow.getLong(gridView.getDataIndex(COL_ORDER_MANAGER)),
        BeeKeeper.getUser().getUserId());
    boolean validState = AssessmentStatus.NEW
        .is(activeRow.getInteger(gridView.getDataIndex(COL_STATUS)));

    if (!primary || !owner || !validState) {
      Global.showError("No way");
      return DeleteMode.CANCEL;
    } else {
      return DeleteMode.SINGLE;
    }
  }

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

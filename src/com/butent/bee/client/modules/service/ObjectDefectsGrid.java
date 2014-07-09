package com.butent.bee.client.modules.service;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

public class ObjectDefectsGrid extends AbstractGridInterceptor {

  ObjectDefectsGrid() {
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    if (action == Action.ADD) {
      DefectBuilder.start(getGridView());
      return false;
    } else {
      return super.beforeAction(action, presenter);
    }
  }
  
  @Override
  public void beforeRefresh(GridPresenter presenter) {
    super.beforeRefresh(presenter);

    FormView form = UiHelper.getForm(presenter.getMainView().asWidget());
    if (form != null && !BeeUtils.isEmpty(form.getViewName()) && form.getActiveRow() != null) {
      DataInfo dataInfo = Data.getDataInfo(form.getViewName());

      if (dataInfo != null) {
        Integer objStatus =
            form.getActiveRow().getInteger(dataInfo.getColumnIndex(COL_OBJECT_STATUS));

        Filter currStatusFilter = Filter.isEqual(COL_OBJECT_STATUS, Value.getValue(objStatus));
        Filter otherStatusFilter = Filter.isNull(COL_OBJECT_STATUS);

        if (objStatus != null && objStatus.intValue() > ObjectStatus.SERVICE_OBJECT.ordinal()) {
          otherStatusFilter =
              Filter.isMore(COL_OBJECT_STATUS, Value
                  .getValue(ObjectStatus.SERVICE_OBJECT.ordinal()));
        }

        presenter.getDataProvider().setParentFilter(COL_OBJECT_STATUS, Filter.or(currStatusFilter,
            otherStatusFilter));
      }
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new ObjectDefectsGrid();
  }

}

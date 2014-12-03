package com.butent.bee.client.modules.tasks;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.tasks.TaskType;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;

class ChildTasksGrid extends TasksGrid {

  ChildTasksGrid() {
    super(TaskType.RELATED, null);
  }

  @Override
  public boolean beforeAddRow(final GridPresenter presenter, boolean copy) {
    if (copy) {
      return true;
    }

    presenter.getGridView().ensureRelId(new IdCallback() {
      @Override
      public void onSuccess(Long relId) {
        DataInfo dataInfo = Data.getDataInfo(presenter.getViewName());

        BeeRow newRow = RowFactory.createEmptyRow(dataInfo, true);
        String relColumn = presenter.getGridView().getRelColumn();

        FormView form = ViewHelper.getForm(presenter.getMainView());
        if (form != null) {
          RelationUtils.updateRow(dataInfo, relColumn, newRow,
              Data.getDataInfo(form.getViewName()), form.getActiveRow(), true);
        }

        RowFactory.createRow(dataInfo, newRow, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            presenter.handleAction(Action.REFRESH);
          }
        });
      }
    });

    return false;
  }

  @Override
  public GridInterceptor getInstance() {
    return new ChildTasksGrid();
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    return true;
  }
}

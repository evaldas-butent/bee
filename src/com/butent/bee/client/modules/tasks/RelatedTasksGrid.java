package com.butent.bee.client.modules.tasks;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.event.logical.RowActionEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.tasks.TaskType;
import com.butent.bee.shared.modules.tasks.TaskUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class RelatedTasksGrid extends TasksGrid {

  RelatedTasksGrid() {
    super(TaskType.RELATED, TaskType.RELATED.getCaption());
  }

  @Override
  public boolean beforeAddRow(final GridPresenter presenter, boolean copy) {
    if (copy) {
      String caption = presenter.getActiveRow().getString(getDataIndex(COL_SUMMARY));
      List<String> messages = Lists.newArrayList(Localized.getConstants().crmRTCopyQuestion());

      Global.confirm(caption, Icon.QUESTION, messages, Localized.getConstants().actionCopy(),
          Localized.getConstants().actionCancel(), new ConfirmationCallback() {
            @Override
            public void onConfirm() {
              if (presenter.getActiveRow() == null) {
                return;
              }
              Long rtId = presenter.getActiveRow().getLong(getDataIndex(COL_TASK));

              ParameterList params = TasksKeeper.createArgs(SVC_RT_COPY);
              params.addQueryItem(VAR_RT_ID, rtId);

              BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
                @Override
                public void onResponse(ResponseObject response) {
                  if (Queries.checkRowResponse(SVC_RT_COPY, VIEW_RECURRING_TASKS, response)) {
                    BeeRow row = BeeRow.restore(response.getResponseAsString());
                    RowInsertEvent.fire(BeeKeeper.getBus(), VIEW_RECURRING_TASKS, row, null);
                    
                    presenter.handleAction(Action.REFRESH);
                    openTask(row.getId());
                  }
                }
              });
            }
          });

    } else {
      presenter.getGridView().ensureRelId(new IdCallback() {
        @Override
        public void onSuccess(Long relId) {
          DataInfo dataInfo = Data.getDataInfo(VIEW_TASKS);

          BeeRow row = RowFactory.createEmptyRow(dataInfo, true);
          RowActionEvent.fireCreateRow(VIEW_TASKS, row, presenter.getWidget().getId());

          String relColumn = presenter.getGridView().getRelColumn();
          String property = TaskUtils.translateRelationToTaskProperty(relColumn);
          
          if (!BeeUtils.isEmpty(property) && BeeUtils.isEmpty(row.getProperty(property))) {
            row.setProperty(property, relId.toString());
          }

          RowFactory.createRow(dataInfo, row, new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              presenter.handleAction(Action.REFRESH);
            }
          });
        }
      });
    }

    return false;
  }

  @Override
  public GridInterceptor getInstance() {
    return new RelatedTasksGrid();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    event.consume();

    int index = getDataIndex(COL_TASK);
    if (!BeeConst.isUndef(index) && event.getRowValue() != null) {
      openTask(event.getRowValue().getLong(index));
    }
  }
  
  private void openTask(Long id) {
    if (DataUtils.isId(id)) {
      RowEditor.openRow(VIEW_TASKS, id, true, new RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          getGridPresenter().handleAction(Action.REFRESH);
        }
      });
    }
  }
  
}

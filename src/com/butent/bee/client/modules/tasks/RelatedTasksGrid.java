package com.butent.bee.client.modules.tasks;

import static com.butent.bee.client.composite.Relations.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.event.logical.RowActionEvent;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.validation.ValidationHelper;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.modules.tasks.TaskType;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.WindowType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class RelatedTasksGrid extends TasksGrid {

  private static final String NAME_MODE = "Mode";
  private static final String NAME_SLACK = "Slack";

  RelatedTasksGrid() {
    super(TaskType.RELATED, null);
  }

  @Override
  public boolean afterCreateColumn(String columnId, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {

    if (BeeUtils.same(columnId, NAME_MODE) && column instanceof HasCellRenderer) {
      ((HasCellRenderer) column).setRenderer(new ModeRenderer());

    } else if (BeeUtils.same(columnId, NAME_SLACK) && column instanceof HasCellRenderer) {
      ((HasCellRenderer) column).setRenderer(new TaskSlackRenderer(dataColumns,
          VIEW_RELATED_TASKS));

    } else if (BeeUtils.inListSame(columnId, COL_FINISH_TIME, COL_EXECUTOR)) {
      editableColumn.addCellValidationHandler(ValidationHelper.DO_NOT_VALIDATE);
    }

    return true;
  }

  @Override
  public boolean beforeAddRow(final GridPresenter presenter, boolean copy) {
    presenter.getGridView().ensureRelId(relId -> {
      DataInfo dataInfo = Data.getDataInfo(VIEW_TASKS);

      BeeRow row = RowFactory.createEmptyRow(dataInfo, true);
      RowActionEvent.fireCreateRow(VIEW_TASKS, row, presenter.getMainView().getId());
      FormView parentForm = ViewHelper.getForm(presenter.getMainView());

      if (parentForm != null) {
        String relViewName = parentForm.getViewName();
        String relColumn = presenter.getGridView().getRelColumn();

        if (!BeeUtils.isEmpty(relViewName) && BeeUtils.isEmpty(row.getProperty(PFX_RELATED
            + relViewName))) {
          row.setProperty(PFX_RELATED + relViewName, DataUtils.buildIdList(relId));
        }

        if (BeeUtils.same(relColumn, ServiceConstants.COL_SERVICE_MAINTENANCE)
            && presenter.getMainView() != null) {
          if (parentForm != null && parentForm.getActiveRow() != null) {
            int objectColumnIndex = Data.getColumnIndex(parentForm.getViewName(),
                ServiceConstants.COL_SERVICE_OBJECT);
            Long objectId = parentForm.getActiveRow().getLong(objectColumnIndex);

            if (DataUtils.isId(objectId)) {
              row.setProperty(PFX_RELATED + ServiceConstants.TBL_SERVICE_OBJECTS,
                  DataUtils.buildIdList(objectId));
            }
          }
        }

        WindowType windowType = getNewRowWindowType();
        Opener opener = Opener.maybeCreate(windowType);

        RowFactory.createRow(dataInfo, row, opener, result -> {
          if (isAttached()) {
            presenter.handleAction(Action.REFRESH);
          }
        });
      }
    });

    return false;
  }

  @Override
  public DeleteMode beforeDeleteRow(final GridPresenter presenter, final IsRow row) {
    final Long taskId = getTaskId(row);

    if (DataUtils.isId(taskId)) {
      Queries.deleteRow(VIEW_TASKS, taskId, result -> {
        RowDeleteEvent.fire(BeeKeeper.getBus(), VIEW_TASKS, taskId);
        presenter.handleAction(Action.REFRESH);
      });
    }

    return DeleteMode.CANCEL;
  }

  @Override
  public GridInterceptor getInstance() {
    return new RelatedTasksGrid();
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    return true;
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (!maybeEditStar(event)) {
      event.consume();

      int index = getDataIndex(COL_TASK);
      if (!BeeConst.isUndef(index) && event.getRowValue() != null) {
        openTask(event.getRowValue().getLong(index));
      }
    }
  }

  @Override
  protected void afterCopyAsRecurringTask() {
    DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_RELATED_RECURRING_TASKS);
  }

  @Override
  protected void afterCopyTask() {
    if (isAttached() && getGridPresenter() != null) {
      getGridPresenter().handleAction(Action.REFRESH);
    }
  }

  @Override
  protected Long getTaskId(IsRow row) {
    return (row == null) ? null : row.getLong(getDataIndex(COL_TASK));
  }

  private void openTask(Long id) {
    if (DataUtils.isId(id)) {
      WindowType windowType = getEditWindowType();
      Opener opener = Opener.maybeCreate(windowType);

      RowEditor.open(VIEW_TASKS, id, opener);
    }
  }
}

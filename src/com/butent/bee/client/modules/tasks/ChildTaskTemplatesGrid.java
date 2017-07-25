package com.butent.bee.client.modules.tasks;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

class ChildTaskTemplatesGrid extends AbstractGridInterceptor {

  @Override
  public boolean beforeAddRow(final GridPresenter presenter, boolean copy) {
    if (copy) {
      return true;
    }

    presenter.getGridView().ensureRelId(relId -> {
      DataInfo childTaskDataInfo = Data.getDataInfo(presenter.getViewName());

      BeeRow childTemplateTask = RowFactory.createEmptyRow(childTaskDataInfo, true);
      String relColumn = presenter.getGridView().getRelColumn();

      FormView parentForm = ViewHelper.getForm(presenter.getMainView());
      if (parentForm != null) {
        DataInfo parentFormDataInfo = Data.getDataInfo(parentForm.getViewName());
        IsRow parentFormRow = parentForm.getActiveRow();

        RelationUtils.updateRow(childTaskDataInfo, relColumn, childTemplateTask,
            parentFormDataInfo, parentFormRow, true);

        if (BeeUtils.same(parentForm.getViewName(), ProjectConstants.VIEW_PROJECT_TEMPLATES)) {
          fillProjectData(childTaskDataInfo, childTemplateTask, parentFormDataInfo,
              parentFormRow);
        }

        if (BeeUtils.same(parentForm.getViewName(),
            ProjectConstants.VIEW_PROJECT_TEMPLATE_STAGES)) {
          fillProjectStageData(childTaskDataInfo, childTemplateTask, parentFormDataInfo,
              parentFormRow);
          fillProjectData(childTaskDataInfo, childTemplateTask, parentFormDataInfo,
              parentFormRow);
        }
      }

      RowFactory.createRow(childTaskDataInfo, childTemplateTask, result -> {
        if (isAttached()) {
          presenter.handleAction(Action.REFRESH);
        }
      });
    });

    return false;
  }

  @Override
  public GridInterceptor getInstance() {
    return new ChildTaskTemplatesGrid();
  }

  private static void fillProjectData(DataInfo taskTemplateData, IsRow templateRow,
      DataInfo parentFormData, IsRow parentRowData) {
    if (taskTemplateData == null && templateRow == null && parentFormData == null
        && parentRowData == null) {
      return;
    }

    /* Fill company info */
    int idxTaskCompany = taskTemplateData.getColumnIndex(ClassifierConstants.COL_COMPANY);
    int idxProjectCompany = parentFormData.getColumnIndex(ClassifierConstants.COL_COMPANY);

    if (BeeUtils.isNegative(idxTaskCompany) && BeeUtils.isNegative(idxProjectCompany)) {
      return;
    }

    templateRow.setValue(idxTaskCompany, parentRowData.getValue(idxProjectCompany));

    int idxTaskCompanyName = taskTemplateData.getColumnIndex(ClassifierConstants.ALS_COMPANY_NAME);
    int idxProjectCompanyName = parentFormData.getColumnIndex(ClassifierConstants.ALS_COMPANY_NAME);

    if (!BeeUtils.isNegative(idxTaskCompanyName) && !BeeUtils.isNegative(idxProjectCompanyName)) {
      templateRow.setValue(idxTaskCompanyName, parentRowData.getValue(idxProjectCompanyName));
    }
  }

  private static void fillProjectStageData(DataInfo taskTemplateData, IsRow templateRow,
      DataInfo parentFormData, IsRow parentRowData) {
    if (taskTemplateData == null && templateRow == null && parentFormData == null
        && parentRowData == null) {
      return;
    }

    /* Fill project info */
    int idxTaskProject = taskTemplateData.getColumnIndex(ProjectConstants.COL_PROJECT_TEMPLATE);
    int idxStageProject = parentFormData.getColumnIndex(ProjectConstants.COL_PROJECT_TEMPLATE);

    if (BeeUtils.isNegative(idxTaskProject) && BeeUtils.isNegative(idxStageProject)) {
      return;
    }

    templateRow.setValue(idxTaskProject, parentRowData.getValue(idxStageProject));
  }

}

package com.butent.bee.client.modules.service;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.modules.service.ServiceConstants;

import java.util.List;

class ChildServiceObjectsGrid extends AbstractGridInterceptor {

  ChildServiceObjectsGrid() {

  }

  @Override
  public GridInterceptor getInstance() {
    return new ChildServiceObjectsGrid();
  }

  @Override
  public boolean beforeAddRow(final GridPresenter presenter, boolean copy) {
    if (copy) {
      return true;
    }

    presenter.getGridView().ensureRelId(new IdCallback() {
      @Override
      public void onSuccess(Long relId) {
        DataInfo svcObjectView = Data.getDataInfo(presenter.getViewName());
        BeeRow row = RowFactory.createEmptyRow(svcObjectView, true);

        String relColumn = presenter.getGridView().getRelColumn();

        FormView parentForm = ViewHelper.getForm(presenter.getMainView());

        if (parentForm != null) {
          fillParentFormData(svcObjectView, row, parentForm, relColumn);
        }
      }
    });

    return false;
  }

  private static void fillProjectData(DataInfo svcObjectView, IsRow row, DataInfo projectView,
      IsRow prjRow) {
    @SuppressWarnings("unchecked")
    final List<Pair<String, String>> copyCols = Lists.newArrayList(
        Pair.of(ProjectConstants.COL_WORK_PLACE, ServiceConstants.COL_SERVICE_ADDRESS),
        Pair.of(ClassifierConstants.COL_COMPANY, ServiceConstants.COL_SERVICE_CUSTOMER),
        Pair.of(ClassifierConstants.ALS_COMPANY_NAME, ServiceConstants.ALS_SERVICE_CUSTOMER_NAME),
        Pair.of(ProjectConstants.ALS_COMPANY_TYPE_NAME,
            ServiceConstants.ALS_SERVICE_CUSTOMER_TYPE_NAME)
        );

    for (Pair<String, String> col : copyCols) {
      row.setValue(svcObjectView.getColumnIndex(col.getB()),
          prjRow.getValue(projectView.getColumnIndex(col.getA())));
    }

    row.setValue(svcObjectView.getColumnIndex(ServiceConstants.COL_OBJECT_STATUS),
        ServiceConstants.SvcObjectStatus.POTENTIAL_OBJECT.ordinal());
  }

  private static void fillParentFormData(DataInfo svcObjectView, BeeRow row, FormView pForm,
      String relColumn) {
    DataInfo pFormView = Data.getDataInfo(pForm.getViewName());
    IsRow pRow = pForm.getActiveRow();

    RelationUtils.updateRow(svcObjectView, relColumn, row, pFormView, pRow, true);

    switch (pForm.getViewName()) {
      case ProjectConstants.VIEW_PROJECTS:
        fillProjectData(svcObjectView, row, pFormView, pRow);
        break;
    }

    commitData(svcObjectView, row);
  }

  private static void commitData(final DataInfo svcObjectView, BeeRow row) {
    RowFactory.createRow(svcObjectView, row, new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        DataChangeEvent.fireRefresh(BeeKeeper.getBus(), svcObjectView.getViewName());
      }
    });
  }

}
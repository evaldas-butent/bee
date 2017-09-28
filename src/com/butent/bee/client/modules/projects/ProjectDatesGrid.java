package com.butent.bee.client.modules.projects;

import com.google.common.collect.Lists;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

class ProjectDatesGrid extends AbstractGridInterceptor {

  private static final List<String> COPY_COLUMNS = Lists.newArrayList(
      ProjectConstants.COL_DATES_COLOR,
      ProjectConstants.COL_DATES_NOTE);

  @Override
  public void beforeRender(GridView gridView, RenderingEvent event) {
    for (IsRow row : getGridView().getRowData()) {
      if (!BeeUtils.isEmpty(row.getProperty(ProjectConstants.PROP_TEMPLATE))) {
        getGridView().getRowData().remove(row);
      }
    }

    FormView form = ViewHelper.getForm(gridView.asWidget());

    if (form == null) {
      return;
    }

    IsRow formRow = form.getActiveRow();

    if (formRow == null) {
      return;
    }

    String prop = formRow.getProperty(ProjectConstants.VIEW_PROJECT_TEMPLATE_DATES);

    if (BeeUtils.isEmpty(prop)) {
      return;
    }

    BeeRowSet templates = BeeRowSet.restore(prop);
    DataInfo viewProjectDates = Data.getDataInfo(ProjectConstants.VIEW_PROJECT_DATES);

    for (IsRow templRow : templates) {
      BeeRow row = RowFactory.createEmptyRow(viewProjectDates, true);
      for (String col : COPY_COLUMNS) {
        row.setValue(viewProjectDates.getColumnIndex(col),
            templRow.getValue(templates.getColumnIndex(col)));
      }

      row.setProperty(ProjectConstants.PROP_TEMPLATE, BeeConst.STRING_TRUE);

      gridView.getGrid().getRowData().add(0, row);
    }

  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (!BeeUtils.isEmpty(event.getRowValue().getProperty(ProjectConstants.PROP_TEMPLATE))) {
      event.consume();

      IsRow templRow = event.getRowValue();
      final DataInfo viewProjectDates = Data.getDataInfo(ProjectConstants.VIEW_PROJECT_DATES);
      final BeeRow row = RowFactory.createEmptyRow(viewProjectDates, true);

      for (String col : COPY_COLUMNS) {
        row.setValue(viewProjectDates.getColumnIndex(col),
            templRow.getValue(viewProjectDates.getColumnIndex(col)));
      }

      if (getGridView() != null) {
        getGridView().ensureRelId(result -> {

          FormView parentForm = ViewHelper.getForm(getGridView().asWidget());
          if (parentForm != null) {

            if (parentForm.getActiveRow() != null) {
              RelationUtils.updateRow(viewProjectDates, getGridView().getRelColumn(), row,
                  Data.getDataInfo(parentForm.getViewName()), parentForm.getActiveRow(), true);

            }
          }

          RowFactory.createRow(viewProjectDates, row, Opener.MODAL,
              createdDate -> getGridPresenter().handleAction(Action.REFRESH));
        });

      }

      return;
    }

    super.onEditStart(event);
  }

  @Override
  public GridInterceptor getInstance() {
    return new ProjectDatesGrid();
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter,
      IsRow activeRow, Collection<RowInfo> selectedRows, DeleteMode defMode) {

    if (BeeUtils.isEmpty(activeRow.getProperty(ProjectConstants.PROP_TEMPLATE))) {
      return DeleteMode.SINGLE;
    } else {
      return DeleteMode.CANCEL;
    }
  }
}

package com.butent.bee.client.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.trade.InvoicesGrid;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

/**
 * Client-side projects module handler.
 */
public final class ProjectsKeeper {

  public static final String STYLE_PREFIX = "bee-prj-";

  /**
   * Creates rpc parameters of projects module.
   *
   * @param method name of method.
   * @return rpc parameters to call queries of server-side.
   */
  public static ParameterList createSvcArgs(String method) {
    return BeeKeeper.getRpc().createParameters(Module.PROJECTS, method);
  }

  public static void createTemplateTasks(FormView form, final IsRow row, String relTmlColumn,
      ChildGrid childGrid, Filter filter) {
    if (form == null || row == null) {
      return;
    }

    if (!DataUtils.isId(row.getId())) {
      return;
    }

    if (DataUtils.isNewRow(row)) {
      return;
    }

    if (childGrid == null) {
      return;
    }

    final GridView tasksGrid = childGrid.getGridView();
    Filter listFilter = Filter.equals(relTmlColumn, row.getId());

    if (filter != null) {
      listFilter = Filter.and(listFilter, filter);
    }

    Queries.getRowSet(ProjectConstants.VIEW_PROJECT_TEMPLATE_TASK_COPY,
        Data.getDataInfo(ProjectConstants.VIEW_PROJECT_TEMPLATE_TASK_COPY).getColumnNames(false),
        listFilter, new Order(Data.getIdColumn(ProjectConstants.VIEW_PROJECT_TEMPLATE_TASK_COPY),
            false),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            if (result.isEmpty()) {
              return;
            }
            row.setProperty(ProjectConstants.VIEW_PROJECT_TEMPLATE_TASK_COPY,
                Codec.beeSerialize(result));

            if (tasksGrid != null) {
              tasksGrid.refresh(true, false);
            }
          }
        });
  }

  public static void createProjectFromTemplate(IsRow templateRow, RowCallback callback) {
    ProjectTemplatesGrid.createProject(Data.getDataInfo(VIEW_PROJECT_TEMPLATES),
        templateRow, callback);
  }

  /**
   * Register projects client-side module handler.
   */
  public static void register() {
    /* Register grid handlers */
    GridFactory.registerGridInterceptor(GRID_ALL_PROJECTS, new AllProjectsGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECTS, new ProjectsGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_USERS, new ProjectUsersGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_STAGES, new ProjectStagesGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_INCOMES, new ProjectIncomesGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_TEMPLATE_STAGES,
        new ProjectTemplateStagesGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_INVOICES, new InvoicesGrid() {

      private boolean erpConnectionActive;

      @Override
      public GridInterceptor getInstance() {
        return this;
      }

      @Override
      public void afterCreatePresenter(GridPresenter presenter) {
        if (isErpConnectionActive()) {
          super.afterCreatePresenter(presenter);
        }
      }

      @Override
      public void beforeCreateColumns(List<? extends IsColumn> dataColumns,
          final List<ColumnDescription> columnDescriptions) {

        Global.getParameter(AdministrationConstants.PRM_ERP_ADDRESS, new Consumer<String>() {

          @Override
          public void accept(String erpAddress) {
            setErpConnectionActive(!BeeUtils.isEmpty(erpAddress));

            for (ColumnDescription columnDescription : columnDescriptions) {
              if (BeeUtils.inListSame(columnDescription.getId(),
                  TradeConstants.COL_TRADE_PAYMENT_TIME,
                  TradeConstants.COL_TRADE_PAID)) {
                columnDescription.setEditInPlace(!isErpConnectionActive());
              }
            }
          }
        });
      }

      @Override
      public ColumnDescription beforeCreateColumn(GridView gridView,
          ColumnDescription columnDescription) {
        if (BeeUtils.inListSame(columnDescription.getId(),
            TradeConstants.COL_TRADE_PAYMENT_TIME,
            TradeConstants.COL_TRADE_PAID)) {
          columnDescription.setEditInPlace(!isErpConnectionActive());
        }
        return super.beforeCreateColumn(gridView, columnDescription);
      }

      private boolean isErpConnectionActive() {
        return erpConnectionActive;
      }

      private void setErpConnectionActive(boolean active) {
        this.erpConnectionActive = active;
      }

    });

    GridFactory.registerGridInterceptor(GRID_PROJECT_TEMPLATES, new ProjectTemplatesGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_DATES, new ProjectDatesGrid());
    GridFactory.registerGridInterceptor(GRID_PROJECT_ACTIONS, new ProjectActionsGrid());

    /* Register form handlers */
    FormFactory.registerFormInterceptor(FORM_PROJECT, new ProjectForm());
    FormFactory.registerFormInterceptor(FORM_PROJECT_STAGE, new ProjectStageForm());
    FormFactory.registerFormInterceptor(FORM_PROJECT_TEMPLATE, new ProjectTemplateForm());
  }

  private ProjectsKeeper() {

  }
}

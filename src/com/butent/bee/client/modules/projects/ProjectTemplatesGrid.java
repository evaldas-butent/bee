package com.butent.bee.client.modules.projects;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.*;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.data.*;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfoList;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import java.util.List;

public class ProjectTemplatesGrid extends AbstractGridInterceptor {

  private Long selectedDefaultStage;

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    HeaderView headerView = presenter.getHeader();

    headerView.clearCommandPanel();

    if (BeeKeeper.getUser().canCreateData(ProjectConstants.VIEW_PROJECTS)
        && !presenter.getGridView().isChild()) {
      FaLabel createProject = new FaLabel(FontAwesome.ROCKET);
      createProject.setTitle(Localized.getConstants().prjCreateFromTasks());
      createProject.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          createProjectClick();
        }
      });

      presenter.getHeader().addCommandItem(createProject);
    }


    super.afterCreatePresenter(presenter);
  }

  @Override
  public GridInterceptor getInstance() {
    return new ProjectTemplatesGrid();
  }

  private void createProjectClick() {
    final GridView gridView = getGridPresenter().getGridView();

    final IsRow selectedRow = gridView.getActiveRow();

    if (selectedRow == null) {
      gridView.notifyWarning(Localized.getConstants().selectAtLeastOneRow());
      return;
    }

    int idxTMLName = gridView.getDataIndex(ProjectConstants.COL_PROJECT_NAME);
    int idxTMLPriority = gridView.getDataIndex(ProjectConstants.COL_PROJECT_PRIORITY);
    int idxTMLType = gridView.getDataIndex(ProjectConstants.COL_PROJECT_TYPE);
    int idxTMLCompany = gridView.getDataIndex(ClassifierConstants.COL_COMPANY);
    int idxTMLCompanyName = gridView.getDataIndex(ClassifierConstants.ALS_COMPANY_NAME);
//    int idxTMLCompanyType = gridView.getDataIndex(ClassifierConstants.ALS_COMPANY_TYPE);
    int idxTMLCompanyTypeName = gridView.getDataIndex(ProjectConstants.ALS_COMPANY_TYPE_NAME);
    int idxTMLCategory = gridView.getDataIndex(ProjectConstants.COL_PROJECT_CATEGORY);
    int idxTMLCategoryName = gridView.getDataIndex(ProjectConstants.ALS_CATEGORY_NAME);
    int idxTMLExpectedDuration = gridView.getDataIndex(ProjectConstants.COL_EXPECTED_DURATION);
    int idxTMLTimeUnit = gridView.getDataIndex(ProjectConstants.COL_PROJECT_TIME_UNIT);
    int idxTMLPrice = gridView.getDataIndex(ProjectConstants.COL_PROJECT_PRICE);
    int idxTMLContractPrice = gridView.getDataIndex(ProjectConstants.COL_CONTRACT_PRICE);
    int idxTMLCurrency = gridView.getDataIndex(ProjectConstants.COL_PROJECT_CURENCY);
    int idxTMLDescription = gridView.getDataIndex(ProjectConstants.COL_DESCRIPTION);
    int idxTMLDefaultStage =
        gridView.getDataIndex(ProjectConstants.COL_DEFAULT_PROJECT_TEMPLATE_STAGE);

    DataInfo prjDataInfo = Data.getDataInfo(ProjectConstants.VIEW_PROJECTS);
    BeeRow prjRow = RowFactory.createEmptyRow(prjDataInfo, true);

    int idxPrjName = prjDataInfo.getColumnIndex(ProjectConstants.COL_PROJECT_NAME);
    int idxPrjPriority = prjDataInfo.getColumnIndex(ProjectConstants.COL_PROJECT_PRIORITY);
    int idxPrjType = prjDataInfo.getColumnIndex(ProjectConstants.COL_PROJECT_TYPE);
    int idxPrjCompany = prjDataInfo.getColumnIndex(ClassifierConstants.COL_COMPANY);
    int idxPrjCompanyName = prjDataInfo.getColumnIndex(ClassifierConstants.ALS_COMPANY_NAME);
//    int idxPrjCompanyType = prjDataInfo.getColumnIndex(ClassifierConstants.ALS_COMPANY_TYPE);
    int idxPrjCompanyTypeName = prjDataInfo.getColumnIndex(ProjectConstants.ALS_COMPANY_TYPE_NAME);
    int idxPrjCategory = prjDataInfo.getColumnIndex(ProjectConstants.COL_PROJECT_CATEGORY);
    int idxPrjCategoryName = prjDataInfo.getColumnIndex(ProjectConstants.ALS_CATEGORY_NAME);
    int idxPrjExpectedDuration = prjDataInfo.getColumnIndex(ProjectConstants.COL_EXPECTED_DURATION);
    int idxPrjTimeUnit = prjDataInfo.getColumnIndex(ProjectConstants.COL_PROJECT_TIME_UNIT);
    int idxPrjPrice = prjDataInfo.getColumnIndex(ProjectConstants.COL_PROJECT_PRICE);
    int idxPrjContractPrice = prjDataInfo.getColumnIndex(ProjectConstants.COL_CONTRACT_PRICE);
    int idxPrjCurrency = prjDataInfo.getColumnIndex(ProjectConstants.COL_PROJECT_CURENCY);
    int idxPrjDescription = prjDataInfo.getColumnIndex(ProjectConstants.COL_DESCRIPTION);
    int idxPrjTemplate = prjDataInfo.getColumnIndex(ProjectConstants.COL_PROJECT_TEMPLATE);

    prjRow.setValue(idxPrjTemplate, selectedRow.getId());
    prjRow.setValue(idxPrjName, selectedRow.getValue(idxTMLName));
    prjRow.setValue(idxPrjPriority, selectedRow.getValue(idxTMLPriority));
    prjRow.setValue(idxPrjType, selectedRow.getValue(idxTMLType));
    prjRow.setValue(idxPrjCompany, selectedRow.getValue(idxTMLCompany));
    prjRow.setValue(idxPrjCompanyName, selectedRow.getValue(idxTMLCompanyName));
//    prjRow.setValue(idxPrjCompanyType, selectedRow.getValue(idxTMLCompanyType));
    prjRow.setValue(idxPrjCompanyTypeName, selectedRow.getValue(idxTMLCompanyTypeName));
    prjRow.setValue(idxPrjCategory, selectedRow.getValue(idxTMLCategory));
    prjRow.setValue(idxPrjCategoryName, selectedRow.getValue(idxTMLCategoryName));

    prjRow.setValue(idxPrjExpectedDuration, selectedRow.getValue(idxTMLExpectedDuration));

    prjRow.setValue(idxPrjTimeUnit, selectedRow.getValue(idxTMLTimeUnit));
    prjRow.setValue(idxPrjPrice, selectedRow.getValue(idxTMLPrice));
    prjRow.setValue(idxPrjContractPrice, selectedRow.getValue(idxTMLContractPrice));
    prjRow.setValue(idxPrjCurrency, selectedRow.getValue(idxTMLCurrency));
    prjRow.setValue(idxPrjDescription, selectedRow.getValue(idxTMLDescription));
    prjRow.setProperty(ProjectConstants.COL_DEFAULT_PROJECT_TEMPLATE_STAGE,
        selectedRow.getString(idxTMLDefaultStage));

    resetSelectedDefaultStage();

    RowFactory.createRow(ProjectConstants.FORM_NEW_PROJECT_FROM_TEMPLATE,
        prjDataInfo.getNewRowCaption(), prjDataInfo, prjRow, null, getNewProjectInterceptor(gridView, selectedRow),
    new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            createInitialStage(result, selectedRow);
          }
        });
  }

  private void createInitialStage(final BeeRow prjRow, final IsRow tmlRow) {

    if (!DataUtils.isId(getSelectedDefaultStage())) {
      createProjectUsers(prjRow, tmlRow);
      return;
    }

    final List<BeeColumn> stageCols =
        Lists.newArrayList(Data.getColumns(ProjectConstants.VIEW_PROJECT_STAGES,
            Lists.newArrayList(ProjectConstants.COL_PROJECT,
                ProjectConstants.COL_STAGE_NAME, ProjectConstants.COL_EXPECTED_DURATION,
                ProjectConstants.COL_EXPENSES, ProjectConstants.COL_PROJECT_CURENCY,
                ProjectConstants.COL_STAGE_TEMPLATE)));

    final List<String> stageValues = Lists.newArrayList(BeeUtils.toString(prjRow.getId()));

    Queries.getRowSet(ProjectConstants.VIEW_PROJECT_TEMPLATE_STAGES, Lists.newArrayList(
            ProjectConstants.COL_STAGE_NAME, ProjectConstants.COL_EXPECTED_DURATION,
            ProjectConstants.COL_EXPENSES, ProjectConstants.COL_PROJECT_CURENCY),
        Filter.compareId(getSelectedDefaultStage()),
        new Queries.RowSetCallback() {

          @Override
          public void onSuccess(BeeRowSet stageTml) {
            if (stageTml.isEmpty()) {
              createProjectUsers(prjRow, tmlRow);
              return;
            }

            stageValues.add(stageTml.getString(0, ProjectConstants.COL_STAGE_NAME));
            stageValues.add(stageTml.getString(0, ProjectConstants.COL_EXPECTED_DURATION));
            stageValues.add(stageTml.getString(0, ProjectConstants.COL_EXPENSES));
            stageValues.add(stageTml.getString(0, ProjectConstants.COL_PROJECT_CURENCY));
            stageValues.add(BeeUtils.toString(getSelectedDefaultStage()));

            Queries.insert(ProjectConstants.VIEW_PROJECT_STAGES, stageCols, stageValues, null,
                new RowCallback() {
                  @Override
                  public void onSuccess(BeeRow result) {
                    resetSelectedDefaultStage();
                    createProjectUsers(prjRow, tmlRow);
                  }
                });

          }
        }
    );
  }

  private void createProjectContacts(final BeeRow prjRow, IsRow tmlRow) {

    final List<BeeColumn> personCols =
        Lists.newArrayList(Data.getColumns(ProjectConstants.VIEW_PROJECT_CONTACTS,
            Lists.newArrayList(ProjectConstants.COL_PROJECT,
                ClassifierConstants.COL_COMPANY_PERSON)));

    final BeeRowSet persons = new BeeRowSet(ProjectConstants.VIEW_PROJECT_CONTACTS, personCols);


    Queries.getRowSet(ProjectConstants.VIEW_PROJECT_TEMPLATE_CONTACTS, Lists.newArrayList(
        ClassifierConstants.COL_COMPANY_PERSON),
        Filter.equals(ProjectConstants.COL_PROJECT_TEMPLATE, BeeUtils.toString(tmlRow.getId())),
        new Queries.RowSetCallback() {

          @Override
          public void onSuccess(BeeRowSet tmlPersons) {
            if (tmlPersons.isEmpty()) {
              openProjectFullForm(prjRow.getId());
              return;
            }

            for (int i = 0 ; i < tmlPersons.getNumberOfRows(); i++) {
              BeeRow row = persons.addEmptyRow();
              row.setValue(persons.getColumnIndex(ProjectConstants.COL_PROJECT), prjRow.getId());
              row.setValue(persons.getColumnIndex(ClassifierConstants.COL_COMPANY_PERSON),
                  tmlPersons.getLong(i, ClassifierConstants.COL_COMPANY_PERSON));
            }

            Queries.insertRows(persons, new RpcCallback<RowInfoList>() {
              @Override
              public void onSuccess(RowInfoList result) {
                openProjectFullForm(prjRow.getId());
              }
            });
          }
        });
  }

  private void createProjectUsers(final BeeRow prjRow, final IsRow tmlRow) {

    final List<BeeColumn> usersCols =
        Lists.newArrayList(Data.getColumns(ProjectConstants.VIEW_PROJECT_USERS,
            Lists.newArrayList(ProjectConstants.COL_PROJECT,
                AdministrationConstants.COL_USER, ProjectConstants.COL_NOTES,
                ProjectConstants.COL_RATE, ProjectConstants.COL_PROJECT_CURENCY)));

    final BeeRowSet users = new BeeRowSet(ProjectConstants.VIEW_PROJECT_USERS, usersCols);


    Queries.getRowSet(ProjectConstants.VIEW_PROJECT_TEMPLATE_USERS, Lists.newArrayList(
            AdministrationConstants.COL_USER, ProjectConstants.COL_NOTES,
            ProjectConstants.COL_RATE, ProjectConstants.COL_PROJECT_CURENCY),
        Filter.equals(ProjectConstants.COL_PROJECT_TEMPLATE, BeeUtils.toString(tmlRow.getId())),
        new Queries.RowSetCallback() {

          @Override
          public void onSuccess(BeeRowSet tmlUsers) {
            if (tmlUsers.isEmpty()) {
              createProjectContacts(prjRow, tmlRow);
              return;
            }

            for (int i = 0 ; i < tmlUsers.getNumberOfRows(); i++) {
              BeeRow row = users.addEmptyRow();
              row.setValue(users.getColumnIndex(ProjectConstants.COL_PROJECT), prjRow.getId());
              row.setValue(users.getColumnIndex(AdministrationConstants.COL_USER),
                  tmlUsers.getLong(i, AdministrationConstants.COL_USER));
              row.setValue(users.getColumnIndex(ProjectConstants.COL_NOTES),
                  tmlUsers.getLong(i, ProjectConstants.COL_NOTES));
              row.setValue(users.getColumnIndex(ProjectConstants.COL_RATE),
                  tmlUsers.getLong(i, ProjectConstants.COL_RATE));
              row.setValue(users.getColumnIndex(ProjectConstants.COL_PROJECT_CURENCY),
                  tmlUsers.getLong(i, ProjectConstants.COL_PROJECT_CURENCY));
            }

            Queries.insertRows(users, new RpcCallback<RowInfoList>() {
              @Override
              public void onSuccess(RowInfoList result) {
                createProjectContacts(prjRow, tmlRow);
                return;
              }
            });
          }
        });
  }

  public Long getSelectedDefaultStage() {
    return selectedDefaultStage;
  }

  private AbstractFormInterceptor getNewProjectInterceptor(final GridView gridView,
      final IsRow selectedRow) {
    return new AbstractFormInterceptor() {
      UnboundSelector stageSelector;

      @Override
      public void afterCreateWidget(String name, IdentifiableWidget widget,
          FormFactory.WidgetDescriptionCallback callback) {
        if (BeeUtils.same(name, ProjectConstants.COL_DEFAULT_PROJECT_TEMPLATE_STAGE) &&
            widget instanceof DataSelector) {
          stageSelector = (UnboundSelector) widget;
        }

        super.afterCreateWidget(name, widget, callback);
      }

      @Override
      public void afterRefresh(FormView form, IsRow row) {
        if(stageSelector != null) {
          stageSelector.getOracle().setAdditionalFilter(
              Filter.isEqual(ProjectConstants.COL_PROJECT_TEMPLATE,
                  Value.getValue(selectedRow.getId())), true);
          stageSelector.setValue(BeeUtils.toLong(row.getProperty
              (ProjectConstants.COL_DEFAULT_PROJECT_TEMPLATE_STAGE)), true);
          resetSelectedDefaultStage();
        }
      }

      @Override
      public boolean beforeAction(Action action, Presenter presenter) {
        if (stageSelector != null && action.equals(Action.SAVE)) {
         setSelectedDefaultStage(BeeUtils.toLong(stageSelector.getValue()));
        }
        return super.beforeAction(action, presenter);
      }


      @Override
      public FormInterceptor getInstance() {
        return this;
      }
    };
  }

  private void openProjectFullForm(long projectId) {
    RowEditor.openForm(ProjectConstants.FORM_PROJECT,
        Data.getDataInfo(ProjectConstants.VIEW_PROJECTS), projectId, Opener.NEW_TAB);
  }

  public void setSelectedDefaultStage(Long selectedDefaultStage) {
    this.selectedDefaultStage = selectedDefaultStage;
  }

  private void resetSelectedDefaultStage() {
    setSelectedDefaultStage(null);
  }

}

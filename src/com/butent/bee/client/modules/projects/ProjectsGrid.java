package com.butent.bee.client.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

class ProjectsGrid extends AllProjectsGrid {

  private static final Long USER_ID = BeeKeeper.getUser().getUserId();

  @Override
  public GridInterceptor getInstance() {
    return new ProjectsGrid();
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    gridDescription.setFilter(getGridFilter());
    return true;
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {

    if (Action.ADD.equals(action) && BeeKeeper.getUser().isDataVisible(VIEW_PROJECT_TEMPLATES)) {
      FormFactory.createFormView(ProjectConstants.FORM_NEW_PROJECT_FROM_TASK, null, null, false,
          getNewProjectFormInterceptor(),
          new FormFactory.FormViewCallback() {
            @Override
            public void onSuccess(FormDescription formDescription, FormView form) {
              if (form != null) {
                form.start(null);
                Global.showModalWidget(form.getCaption(), form.asWidget());
              }
            }
          });
      return false;
    }

    return true;
  }

  private static Filter getGridFilter() {
    Filter isOwner = Filter.equals(COL_PROJECT_OWNER, USER_ID);
    Filter isProjectUser =
        Filter.in(Data.getIdColumn(VIEW_PROJECTS), VIEW_PROJECT_USERS, COL_PROJECT, Filter.equals(
            AdministrationConstants.COL_USER, USER_ID));
    return Filter.or(isOwner, isProjectUser);
  }

  private  AbstractFormInterceptor getNewProjectFormInterceptor() {
    return new AbstractFormInterceptor() {

      private static final String NAME_PROJECT_TEMPLATE = "ProjectTemplate";
      private static final String NAME_CREATE = "Create";
      private static final String NAME_CANCEL = "Cancel";

      private UnboundSelector templateSelector;
      private Button createButton;
      private Button cancelButton;

      @Override
      public void afterCreateWidget(String name, IdentifiableWidget widget,
          FormFactory.WidgetDescriptionCallback callback) {

        switch (name) {
          case NAME_PROJECT_TEMPLATE:
            templateSelector = widget instanceof UnboundSelector ? (UnboundSelector) widget : null;
            break;
          case NAME_CREATE:
            createButton = widget instanceof Button ? (Button) widget : null;

            if (createButton == null) {
              break;
            }

            createButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                Popup.getActivePopup().close();

                if (templateSelector == null) {
                  createSimpleProject();
                } else if (BeeUtils.isEmpty(templateSelector.getValue())) {
                  createSimpleProject();
                } else {
                  createProjectFromTemplate(templateSelector.getRelatedRow());
                }
              }
            });
            break;
          case NAME_CANCEL:
            cancelButton = widget instanceof Button ? (Button) widget : null;

            if (cancelButton == null) {
              break;
            }

            cancelButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                if (templateSelector != null) {
                  templateSelector.setValue(null, true);
                }
                Popup.getActivePopup().close();
              }
            });
            break;
          default:
            super.afterCreateWidget(name, widget, callback);
        }

      }

      @Override
      public FormInterceptor getInstance() {
        return this;
      }

    };
  }

  private static void createSimpleProject() {
    RowFactory.createRow(VIEW_PROJECTS);
  }

  private void createProjectFromTemplate(final IsRow templateRow) {
    ProjectsKeeper.createProjectFromTemplate(templateRow, new RowCallback() {
      @Override
      public void onSuccess(final BeeRow projectRow) {

        if (getGridView() != null) {
          getGridView().notifyInfo(Localized.getMessages()
              .newProjectCreated(projectRow.getId()));
        }

      }
    });
  }

}

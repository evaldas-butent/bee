package com.butent.bee.client.modules.projects;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.ui.Action;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.projects.ProjectStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;

class AllProjectsGrid extends AbstractGridInterceptor implements SelectionHandler<IsRow> {

  private static final String FILTER_KEY = "f1";

  private final Long userId = BeeKeeper.getUser().getUserId();
  private TreeView treeView;
  private IsRow selectedCategory;

  private static Filter getFilter(Long category) {
    if (category == null) {
      return null;
    } else {
      return Filter.equals(COL_PROJECT_CATEGORY, category);
    }
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof TreeView) {
      setTreeView((TreeView) widget);
      getTreeView().addSelectionHandler(this);
    }
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


  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {
    Provider provider = presenter.getDataProvider();

    int idxStatus = provider.getColumnIndex(COL_PROJECT_STATUS);
    int idxOwner = provider.getColumnIndex(COL_PROJECT_OWNER);

    int statusValue = BeeUtils.unbox(activeRow.getInteger(idxStatus));
    long ownerValue = BeeUtils.unbox(activeRow.getLong(idxOwner));

    boolean active = EnumUtils.getEnumByIndex(ProjectStatus.class, statusValue)
        == ProjectStatus.ACTIVE;

    boolean owner = ownerValue == BeeUtils.unbox(userId);

    if (active) {
      presenter.getGridView().notifyWarning(
          BeeUtils.joinWords(Localized.getConstants().project(), activeRow.getId(),
              Localized.getConstants().prjStatusActive())
          );
      return GridInterceptor.DeleteMode.CANCEL;
    } else if (owner) {
      return GridInterceptor.DeleteMode.SINGLE;
    } else {
      presenter.getGridView().notifyWarning(Localized.getConstants().prjDeleteCanManager());
      return GridInterceptor.DeleteMode.CANCEL;
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new AllProjectsGrid();
  }

  @Override
  public void onSelection(SelectionEvent<IsRow> event) {
    if (event != null && getGridPresenter() != null) {
      Long category = null;
      setSelectedCategory(event.getSelectedItem());

      if (getSelectedCategory() != null) {
        category = getSelectedCategory().getId();
      }

      getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(category));
      getGridPresenter().refresh(true);

    }
  }

  IsRow getSelectedCategory() {
    return selectedCategory;
  }

  private TreeView getTreeView() {
    return treeView;
  }

  private void setSelectedCategory(IsRow selectedCategory) {
    this.selectedCategory = selectedCategory;
  }

  private void setTreeView(TreeView treeView) {
    this.treeView = treeView;
  }

  private AbstractFormInterceptor getNewProjectFormInterceptor() {
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

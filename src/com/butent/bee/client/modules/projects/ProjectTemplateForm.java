package com.butent.bee.client.modules.projects;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.projects.ProjectConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

class ProjectTemplateForm extends AbstractFormInterceptor {

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid && (BeeUtils.same(name,
        ProjectConstants.GRID_PROJECT_TEMPLATE_STAGES))) {
      ChildGrid grid = (ChildGrid) widget;

      grid.setGridInterceptor(new AbstractGridInterceptor() {

        @Override
        public void afterCreatePresenter(GridPresenter presenter) {
          HeaderView header = presenter.getHeader();
          header.clearCommandPanel();

          FaLabel setDefault = new FaLabel(FontAwesome.CHECK);
          setDefault.setTitle(Localized.getConstants().setAsPrimary());
          setDefault.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
              GridView gridView = getGridPresenter().getGridView();

              IsRow selectedRow = gridView.getActiveRow();
              if (selectedRow == null) {
                gridView.notifyWarning(Localized.getConstants().selectAtLeastOneRow());
                return;
              } else {
                setAsPrimary(selectedRow.getId(), false);
              }
            }
          });


          header.addCommandItem(setDefault);
        }

        @Override
        public void afterInsertRow(IsRow row) {
          setAsPrimary(row.getId(), true);
        }

        @Override
        public GridInterceptor getInstance() {
          return null;
        }

        private void setAsPrimary(Long gridRowId, boolean checkDefault) {
          GridView gridView = getGridPresenter().getGridView();
          String gridName = gridView.getGridName();

          if (BeeUtils.same(gridName, ProjectConstants.GRID_PROJECT_TEMPLATE_STAGES)) {
            setAsPrimaryAccount(gridRowId, checkDefault);
          }
        }

        private void setAsPrimaryAccount(final Long companyBankAccount, boolean checkDefault) {
          final IsRow projectRow = getFormView().getActiveRow();
          final IsRow projectRowOld = getFormView().getOldRow();
          final int idxDefTMPStage = Data.getColumnIndex(ProjectConstants.VIEW_PROJECT_TEMPLATES,
              ProjectConstants.COL_DEFAULT_PROJECT_TEMPLATE_STAGE);

          boolean hasDefault =
              DataUtils.isId(projectRow.getLong(idxDefTMPStage));

          boolean canChange = !hasDefault || !checkDefault;

          if (canChange) {
            Queries.update(getFormView().getViewName(), Filter.compareId(projectRow.getId()),
                ProjectConstants.COL_DEFAULT_PROJECT_TEMPLATE_STAGE,
                Value.getValue(companyBankAccount),
                new Queries.IntCallback() {

                  @Override
                  public void onSuccess(Integer result) {
                    projectRow.setValue(idxDefTMPStage, companyBankAccount);
                    projectRowOld.setValue(idxDefTMPStage, companyBankAccount);
                    DataChangeEvent.fireRefresh(BeeKeeper.getBus(), getGridView().getViewName());
                  }
                });
          }
        }

      });
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ProjectTemplateForm();
  }

}

package com.butent.bee.client.modules.projects;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;

public class ProjectTemplateStagesGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new ProjectTemplateStagesGrid();
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
//    FormView form = ViewHelper.getForm(presenter.getMainView());
//    LogUtils.getRootLogger().debug("BBBBBB", form.getFormName());
//
//    super.afterCreatePresenter(presenter);
  }

  @Override
  public Map<String, Filter> getInitialParentFilters(Collection<UiOption> uiOptions) {
//
//    View view = ViewHelper.getActiveView(getGridView().getElement());
//
//
//      getGridView().notifyInfo("a", view.getViewPresenter().getClass().getCanonicalName(), "b",
//          view.getViewPresenter().getMainView().getClass().getCanonicalName());



    return super.getInitialParentFilters(uiOptions);
  }




  @Override
  public void onLoad(GridView gridView) {
//    FormView form = ViewHelper.getForm(gridView.asWidget());
//
//
//    super.onLoad(gridView);
  }
}

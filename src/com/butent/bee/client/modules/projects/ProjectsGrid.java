package com.butent.bee.client.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.data.Provider;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.projects.ProjectStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;

class ProjectsGrid extends AbstractGridInterceptor {

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {
    Provider provider = presenter.getDataProvider();

    int idxStatus = provider.getColumnIndex(COL_PROJECT_STATUS);
    int statusValue = BeeUtils.unbox(activeRow.getInteger(idxStatus));
    boolean active = EnumUtils.getEnumByIndex(ProjectStatus.class, statusValue)
        == ProjectStatus.ACTIVE;

    if (active) {
      presenter.getGridView().notifyWarning(
          BeeUtils.joinWords(Localized.getConstants().project(), activeRow.getId()
              , Localized.getConstants().prjStatusActive())
          );
      return GridInterceptor.DeleteMode.CANCEL;
    } else {
      return GridInterceptor.DeleteMode.SINGLE;
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new ProjectsGrid();
  }

}

package com.butent.bee.client.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

class ProjectUsersGrid extends AbstractGridInterceptor {
  private static final LocalizableConstants LC = Localized.getConstants();

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {

    Provider provider = presenter.getDataProvider();

    int idxTaskCount = provider.getColumnIndex(ALS_TASK_COUNT);
    int idxOwner = provider.getColumnIndex(COL_PROJECT_OWNER);
    int idxUser = provider.getColumnIndex(AdministrationConstants.COL_USER);

    int tasksCount = BeeConst.MAX_SCALE;
    long owner = BeeConst.LONG_UNDEF;
    long user = BeeUtils.unbox(BeeKeeper.getUser().getUserId());

    if (!BeeUtils.isNegative(idxTaskCount)) {
      tasksCount = BeeUtils.unbox(activeRow.getInteger(idxTaskCount));
    } else {
      return GridInterceptor.DeleteMode.CANCEL;
    }

    if (!BeeUtils.isNegative(idxOwner)) {
      owner = BeeUtils.unbox(activeRow.getLong(idxOwner));
    }

    if (!BeeUtils.isNegative(idxUser)) {
      user = BeeUtils.unbox(activeRow.getLong(idxUser));
    }

    if (user == owner) {
      if (BeeUtils.isPositive(tasksCount)) {
        presenter.getGridView().notifySevere(LC.prjUserHasSameTasks());
        return GridInterceptor.DeleteMode.CANCEL;
      }
      return GridInterceptor.DeleteMode.SINGLE;
    } else {
      presenter.getGridView().notifySevere(LC.prjUserCanDeleteManager());
      return GridInterceptor.DeleteMode.CANCEL;
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new ProjectUsersGrid();
  }

  @Override
  public void onLoad(GridView gridView) {
    // TODO Auto-generated method stub
    super.onLoad(gridView);
  }

}

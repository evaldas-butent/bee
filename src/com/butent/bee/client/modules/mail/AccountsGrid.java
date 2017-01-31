package com.butent.bee.client.modules.mail;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class AccountsGrid extends AbstractGridInterceptor {

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {
    return DeleteMode.SINGLE;
  }

  @Override
  public DeleteMode beforeDeleteRow(final GridPresenter presenter, IsRow row) {
    int idx = getDataIndex(MailConstants.COL_ACCOUNT_SYNC_MODE);
    String oldMode = row.getString(idx);
    String newMode = BeeUtils.toString(MailConstants.SyncMode.SYNC_NOTHING.ordinal());

    if (!Objects.equals(oldMode, newMode)) {
      Queries.update(getViewName(), row.getId(), row.getVersion(),
          DataUtils.getColumns(getDataColumns(), idx), Collections.singletonList(oldMode),
          Collections.singletonList(newMode), null, new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              presenter.deleteRow(result, false);
            }
          });
      return DeleteMode.CANCEL;
    }
    return super.beforeDeleteRow(presenter, row);
  }

  @Override
  public GridInterceptor getInstance() {
    return new AccountsGrid();
  }

  @Override
  public boolean isRowEditable(IsRow row) {
    return Objects.equals(row.getLong(getDataIndex(MailConstants.COL_USER)),
        BeeKeeper.getUser().getUserId()) && super.isRowEditable(row);
  }
}

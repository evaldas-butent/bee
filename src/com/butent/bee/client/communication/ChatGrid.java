package com.butent.bee.client.communication;

import static com.butent.bee.shared.communication.ChatConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;

import java.util.Collections;
import java.util.List;

public class ChatGrid extends AbstractGridInterceptor {

  public ChatGrid() {
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    if (action == Action.DELETE) {
      if (presenter.getMainView().isEnabled() && getActiveRow() != null) {
        final IsRow row = getActiveRow();

        if (row.isRemovable() && getGridView().isRowEditable(row, null)) {
          String caption = ChatUtils.getChatCaption(getStringValue(COL_CHAT_NAME),
              DataUtils.parseIdList(row.getProperty(PROP_OTHER_USERS)));

          List<String> messages =
              Collections.singletonList(Localized.getConstants().chatDeleteQuestion());

          Global.confirmDelete(caption, Icon.WARNING, messages, () -> {
            ParameterList params = BeeKeeper.getRpc().createParameters(Service.DELETE_CHAT);
            params.addQueryItem(COL_CHAT, row.getId());

            BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
              @Override
              public void onResponse(ResponseObject response) {
                if (!response.hasErrors()) {
                  Global.getChatManager().removeChat(row.getId());
                  RowDeleteEvent.fire(BeeKeeper.getBus(), getViewName(), row.getId());
                }
              }
            });
          });

        } else {
          getGridView().notifyWarning(Localized.getConstants().rowIsNotRemovable());
        }
      }

      return false;

    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public boolean isRowEditable(IsRow row) {
    if (row == null) {
      return false;
    }

    int index = getDataIndex(COL_CHAT_CREATOR);
    if (BeeConst.isUndef(index)) {
      return false;
    }

    if (BeeKeeper.getUser().is(row.getLong(index))) {
      return super.isRowEditable(row);
    } else {
      return false;
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new ChatGrid();
  }
}

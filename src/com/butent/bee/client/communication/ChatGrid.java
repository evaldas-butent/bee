package com.butent.bee.client.communication;

import com.google.gwt.user.client.ui.FlowPanel;

import static com.butent.bee.shared.communication.ChatConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.event.logical.ActiveRowChangeEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.Chat;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatGrid extends AbstractGridInterceptor {

  FlowPanel chatsFlowWidget;

  public ChatGrid() {
  }

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    presenter.getHeader().clearCommandPanel();

    FaLabel openChat = new FaLabel(FontAwesome.COMMENTS_O);
    openChat.setTitle(Localized.dictionary().actionOpen());

    openChat.addClickHandler(event -> {
      long chatId = presenter.getActiveRowId();

      if (DataUtils.isId(chatId)) {
        Global.getChatManager().enterChat(chatId);
      }
    });

    presenter.getHeader().addCommandItem(openChat);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof FlowPanel & BeeUtils.same(name, "chatFlowWidget")) {
      chatsFlowWidget = (FlowPanel) widget;
      chatsFlowWidget.clear();
    }

  }

  @Override
  public void onActiveRowChange(ActiveRowChangeEvent event) {
    if (event.getRowValue() != null) {
      long chatId = event.getRowValue().getId();
      if (DataUtils.isId(chatId)) {
        chatsFlowWidget.clear();
        Global.getChatManager().enterChat(chatId, chatsFlowWidget);
      }
    }
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    if (action == Action.DELETE) {
      if (presenter.getMainView().isEnabled() && getActiveRow() != null
          && getGridView().isRowEditable(getActiveRow(), presenter.getGridView())) {

        final IsRow row = getActiveRow();

        final Long userId = BeeKeeper.getUser().getUserId();
        List<Long> otherUsers = DataUtils.parseIdList(row.getProperty(PROP_OTHER_USERS));

        final Chat chat = Global.getChatManager().findChat(row.getId());
        boolean owner = isOwner(userId, row) || (chat != null && chat.isOwner(userId));

        String caption = ChatUtils.getChatCaption(getStringValue(COL_CHAT_NAME), otherUsers);

        if (row.isRemovable() && owner) {
          List<String> messages =
              Collections.singletonList(Localized.dictionary().chatDeleteQuestion());

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

        } else if (!owner && chat != null && chat.getUsers().size() > 2 && chat.hasUser(userId)) {
          List<String> messages =
              Collections.singletonList(Localized.dictionary().chatLeaveQuestion());

          Global.confirm(caption, Icon.WARNING, messages, () -> {
            ParameterList params = BeeKeeper.getRpc().createParameters(Service.UPDATE_CHAT);
            params.addQueryItem(COL_CHAT, chat.getId());

            params.addDataItem(TBL_CHAT_USERS,
                DataUtils.buildIdList(ChatUtils.getOtherUsers(chat.getUsers())));

            BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
              @Override
              public void onResponse(ResponseObject response) {
                if (!response.hasErrors()) {
                  Global.getChatManager().removeChat(row.getId());
                  DataChangeEvent.fireRefresh(BeeKeeper.getBus(), getViewName());
                }
              }
            });
          });

        } else {
          getGridView().notifyWarning(Localized.dictionary().rowIsNotRemovable());
        }
      }

      return false;

    } else if (action == Action.ADD) {
      Global.getChatManager().createChat();
      return false;

    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new ChatGrid();
  }

  private boolean isOwner(Long userId, IsRow row) {
    int index = getDataIndex(COL_CHAT_CREATOR);

    if (!BeeConst.isUndef(index) && DataUtils.isId(userId) && row != null) {
      return Objects.equals(userId, row.getLong(index));
    } else {
      return false;
    }
  }
}

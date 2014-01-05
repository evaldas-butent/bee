package com.butent.bee.client.modules.commons;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.OptionElement;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.news.Feed;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;

public class UserFeedsInterceptor extends AbstractGridInterceptor {

  UserFeedsInterceptor() {
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    List<? extends IsRow> data = presenter.getGridView().getRowData();
    int dataIndex = getDataIndex(CommonsConstants.COL_FEED);
    
    List<Feed> feeds = Lists.newArrayList();
    for (Feed feed : Feed.values()) {
      boolean used = false;

      if (!BeeUtils.isEmpty(data)) {
        for (IsRow row : data) {
          if (Objects.equal(row.getInteger(dataIndex), feed.ordinal())) {
            used = true;
            break;
          }
        }
      }
      
      if (!used) {
        feeds.add(feed);
      }
    }
    
    if (feeds.isEmpty()) {
      getGridView().notifyWarning("user is subscribed to all feeds");
      return false;
    }
    
    final ListBox listBox = new ListBox(true);
    for (Feed feed : feeds) {
      listBox.addItem(feed.getCaption(), BeeUtils.toString(feed.ordinal()));
    }
    listBox.setAllVisible();
    
    Global.inputWidget(Localized.getConstants().feedNew(), listBox, new InputCallback() {
      @Override
      public void onSuccess() {
        List<Integer> selection = Lists.newArrayList();

        for (int i = 0; i < listBox.getItemCount(); i++) {
          OptionElement optionElement = listBox.getOptionElement(i);
          if (optionElement.isSelected()) {
            Feed feed = EnumUtils.getEnumByIndex(Feed.class, optionElement.getValue());
            if (feed != null) {
              selection.add(feed.ordinal());
            }
          }
        }
        
        if (!selection.isEmpty()) {
          subscribe(selection);
        }
      }
    }, "bee-UserFeeds-add", presenter.getHeader().getElement());
    
    return false;
  }

  @Override
  public GridInterceptor getInstance() {
    return new UserFeedsInterceptor();
  }
  
  private void subscribe(final List<Integer> feeds) {
    getGridView().ensureRelId(new IdCallback() {
      @Override
      public void onSuccess(final Long result) {
        if (DataUtils.isId(result)) {
          ParameterList params = BeeKeeper.getRpc().createParameters(Service.SUBSCRIBE_TO_FEEDS);
          params.addDataItem(Service.VAR_USER, result);
          params.addDataItem(Service.VAR_FEED, BeeUtils.join(BeeConst.STRING_COMMA, feeds));
          
          BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
            @Override
            public void onResponse(ResponseObject response) {
              if (!response.hasErrors()) {
                getGridPresenter().refresh(false);

                if (BeeKeeper.getUser().is(result)) {
                  Global.getNewsAggregator().refresh();
                }
              }
            }
          });
        }
      }
    });
  }
}

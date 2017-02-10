package com.butent.bee.client.data;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;

public abstract class RowCallback extends RpcCallback<BeeRow> {

  public static RowCallback refreshRow(String viewName) {
    return refreshRow(viewName, false);
  }

  public static RowCallback refreshRow(final String viewName, final boolean refreshChildren) {
    return new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        RowUpdateEvent.fire(BeeKeeper.getBus(), viewName, result, refreshChildren);
      }
    };
  }

  public static RowCallback refreshView(final String viewName) {
    return new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        DataChangeEvent.fireRefresh(BeeKeeper.getBus(), viewName);
      }
    };
  }

  public void onCancel() {
  }
}

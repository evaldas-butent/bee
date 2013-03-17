package com.butent.bee.client.data;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.event.RowInsertEvent;

public class RowInsertCallback extends RowCallback {
  
  private final String viewName;

  public RowInsertCallback(String viewName) {
    super();
    this.viewName = viewName;
  }

  @Override
  public void onSuccess(BeeRow result) {
    BeeKeeper.getBus().fireEvent(new RowInsertEvent(viewName, result));
  }
}

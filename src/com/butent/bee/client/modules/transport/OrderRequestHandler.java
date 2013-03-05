package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;

import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.shared.data.DataUtils;

public class OrderRequestHandler extends AbstractFormInterceptor {

  @Override
  public FormInterceptor getInstance() {
    return this;
  }

  @Override
  public void onSaveChanges(SaveChangesEvent event) {
    int col = DataUtils.getColumnIndex("OrderNo", getFormView().getDataColumns());
    String oldOrder = event.getOldRow().getString(col);
    String newOrder = event.getNewRow().getString(col);

    if (!Objects.equal(oldOrder, newOrder)) {
      event.getColumns().add(DataUtils.getColumn("OrderNo", getFormView().getDataColumns()));
      event.getOldValues().add(oldOrder);
      event.getNewValues().add(newOrder);
    }
  }
}

package com.butent.bee.client.view.search;

import com.google.gwt.dom.client.Element;

import com.butent.bee.client.Callback;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.ArrayUtils;

public class ListFilterSupplier extends AbstractFilterSupplier {

  public ListFilterSupplier(String viewName, Filter immutableFilter, BeeColumn column,
      String options) {
    super(viewName, immutableFilter, column, options);
  }

  @Override
  public String getDisplayHtml() {
    return null;
  }
  
  @Override
  public void onRequest(Element target, NotificationListener notificationListener,
      final Callback<Boolean> callback) {
    getHistogram(new Callback<SimpleRowSet>() {
      @Override
      public void onFailure(String... reason) {
        super.onFailure(reason);
        callback.onFailure(reason);
      }

      @Override
      public void onSuccess(SimpleRowSet result) {
        LogUtils.getLogger("").debug(result.getNumberOfRows(), result.getNumberOfColumns(), ArrayUtils.toString(result.getColumnNames()));
        for (String[] row : result.getRows()) {
          LogUtils.getLogger("").debug(ArrayUtils.toString(row));
        }
        callback.onSuccess(false);
      }
    });
  }
}

package com.butent.bee.client.modules.classifiers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;

class HolidaysForm extends AbstractFormInterceptor {

  private final Multimap<Long, Integer> data = HashMultimap.create();

  HolidaysForm() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public FormInterceptor getInstance() {
    return new HolidaysForm();
  }

  @Override
  public void onLoad(FormView form) {
    Queries.getRowSet(ClassifierConstants.VIEW_HOLIDAYS, null, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (!data.isEmpty()) {
          data.clear();
        }

        if (!DataUtils.isEmpty(result)) {
          int countryIndex = result.getColumnIndex(ClassifierConstants.COL_HOLY_COUNTRY);
          int dayIndex = result.getColumnIndex(ClassifierConstants.COL_HOLY_DAY);

          for (BeeRow row : result) {
            data.put(row.getLong(countryIndex), row.getInteger(dayIndex));
          }
        }
      }
    });
  }
}

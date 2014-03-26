package com.butent.bee.client.modules.administration;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.grid.interceptor.UniqueChildInterceptor;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

public class DepartmentForm extends AbstractFormInterceptor implements SelectorEvent.Handler {

  private DataSelector head;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof DataSelector && BeeUtils.same(name, COL_DEPARTMENT_HEAD)) {
      head = (DataSelector) widget;
      head.addSelectorHandler(this);

    } else if (widget instanceof ChildGrid && BeeUtils.same(name, TBL_DEPARTMENT_EMPLOYEES)) {
      ((ChildGrid) widget).setGridInterceptor(new UniqueChildInterceptor(Localized.getConstants()
          .newDepartmentEmployees(),
          COL_DEPARTMENT, COL_COMPANY_PERSON, TBL_COMPANY_PERSONS,
          Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME),
          Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME, ALS_COMPANY_NAME)));
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new DepartmentForm();
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.isOpened()) {
      head.setAdditionalFilter(Filter.isEqual(COL_DEPARTMENT, new LongValue(getActiveRowId())));
    }
  }
}

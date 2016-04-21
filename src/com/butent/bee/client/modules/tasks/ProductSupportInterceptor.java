package com.butent.bee.client.modules.tasks;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.event.logical.SelectorEvent.Handler;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

abstract class ProductSupportInterceptor extends AbstractFormInterceptor {

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {

    if (BeeUtils.same(editableWidget.getColumnId(), COL_TASK_TYPE)) {
      ((DataSelector) widget).addSelectorHandler(new Handler() {

        @Override
        public void onDataSelector(SelectorEvent event) {
          setProductStyle();
        }
      });
    }
  }

  public boolean isProductRequired(IsRow row, String viewName) {
    return BeeUtils.unbox(row.getBoolean(Data.getColumnIndex(viewName, COL_PRODUCT_REQUIRED)));
  }

  public void setProductStyle() {
    Widget product = getFormView().getWidgetByName(COL_PRODUCT);
    if (product != null) {
      product.setStyleName(StyleUtils.NAME_REQUIRED, isProductRequired(getActiveRow(),
          getViewName()));
    }
  }
}

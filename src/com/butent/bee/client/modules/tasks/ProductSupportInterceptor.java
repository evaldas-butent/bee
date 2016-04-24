package com.butent.bee.client.modules.tasks;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.event.logical.SelectorEvent.Handler;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

abstract class ProductSupportInterceptor extends AbstractFormInterceptor {

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {

    if (BeeUtils.inList(editableWidget.getColumnId(), COL_TASK_TYPE, COL_REQUEST_TYPE)) {
      ((DataSelector) widget).addSelectorHandler(new Handler() {

        @Override
        public void onDataSelector(SelectorEvent event) {
          setProductStyle();
        }
      });
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    setProductStyle();
  }

  public boolean isProductRequired(IsRow row, String viewName) {
    return BeeUtils.unbox(row.getBoolean(Data.getColumnIndex(viewName, COL_PRODUCT_REQUIRED)));
  }

  public boolean maybeNotifyEmptyProduct(RowCallback callback) {

    if (isProductRequired(getActiveRow(), getViewName())
        && Data.isNull(getViewName(), getActiveRow(), COL_PRODUCT)) {

      String msg = Localized.dictionary().crmTaskProduct() + " "
          + Localized.dictionary().valueRequired();

      if (callback == null) {
        getFormView().notifySevere(msg);
      } else {
        callback.onFailure(msg);
      }
      return true;
    }
    return false;
  }

  private void setProductStyle() {
    Widget product = getFormView().getWidgetByName(COL_PRODUCT);
    if (product != null) {
      product.setStyleName(StyleUtils.NAME_REQUIRED, isProductRequired(getActiveRow(),
          getViewName()));
    }
  }
}

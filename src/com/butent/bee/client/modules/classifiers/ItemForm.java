package com.butent.bee.client.modules.classifiers;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.presenter.GridFormPresenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;

class ItemForm extends AbstractFormInterceptor {

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    if (DataUtils.isNewRow(row)) {
      ItemsGrid gridHandler = getItemGridHandler(form);

      if (gridHandler != null && gridHandler.getSelectedCategory() != null) {
        Widget categoryWidget = form.getWidgetByName("Categories");

        if (categoryWidget instanceof MultiSelector) {
          long categoryId = gridHandler.getSelectedCategory().getId();
          ((MultiSelector) categoryWidget).render(BeeUtils.toString(categoryId));
        }
      }
    }

    super.afterRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ItemForm();
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    ItemsGrid gridHandler = getItemGridHandler(form);

    if (gridHandler != null && gridHandler.showServices()) {
      newRow.setValue(form.getDataIndex(ClassifierConstants.COL_ITEM_IS_SERVICE), 1);
    }
  }

  private static ItemsGrid getItemGridHandler(FormView form) {
    if (form.getViewPresenter() instanceof GridFormPresenter) {
      GridInterceptor gic = ((GridFormPresenter) form.getViewPresenter()).getGridInterceptor();

      if (gic instanceof ItemsGrid) {
        return (ItemsGrid) gic;
      }
    }
    return null;
  }
}
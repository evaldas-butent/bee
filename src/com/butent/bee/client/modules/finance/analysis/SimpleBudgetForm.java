package com.butent.bee.client.modules.finance.analysis;

import com.google.gwt.xml.client.Element;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;

public class SimpleBudgetForm extends AbstractFormInterceptor {

  public SimpleBudgetForm() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new SimpleBudgetForm();
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    if (!BeeUtils.isEmpty(name)) {
      Integer dimension = getBudgetShowEntryDimension(name);

      if (dimension != null) {
        if (Dimensions.isObserved(dimension)) {
          description.setAttribute(UiConstants.ATTR_HTML, Dimensions.singular(dimension));
        } else {
          return false;
        }
      }
    }

    return super.beforeCreateWidget(name, description);
  }

  @Override
  public void onSourceChange(IsRow row, String source, String value) {
    if (BeeUtils.inList(source, COL_BUDGET_HEADER_INDICATOR, COL_BUDGET_HEADER_TYPE,
        COL_BUDGET_HEADER_YEAR, COL_BUDGET_SHOW_ENTRY_EMPLOYEE)
        || getBudgetShowEntryDimension(source) != null) {

      GridView gridView = ViewHelper.getChildGrid(getFormView(), GRID_BUDGET_ENTRIES);

      if (gridView != null) {
        gridView.getGrid().render(false);
      }
    }

    super.onSourceChange(row, source, value);
  }
}

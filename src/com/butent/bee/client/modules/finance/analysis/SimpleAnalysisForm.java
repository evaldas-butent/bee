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

import java.util.HashSet;
import java.util.Set;

public class SimpleAnalysisForm extends AbstractFormInterceptor {

  public SimpleAnalysisForm() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new SimpleAnalysisForm();
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    if (!BeeUtils.isEmpty(name)) {
      Integer dimension = getAnalysisShowColumnDimension(name);
      if (dimension == null) {
        dimension = getAnalysisShowRowDimension(name);
      }

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
    if (!BeeUtils.isEmpty(source)) {
      Set<String> gridNames = new HashSet<>();

      switch (source) {
        case COL_ANALYSIS_HEADER_INDICATOR:
        case COL_ANALYSIS_HEADER_BUDGET_TYPE:
          gridNames.add(GRID_ANALYSIS_COLUMNS);
          gridNames.add(GRID_ANALYSIS_ROWS);
          break;

        case COL_ANALYSIS_SHOW_COLUMN_EMPLOYEE:
        case COL_ANALYSIS_COLUMN_SPLIT_LEVELS:
          gridNames.add(GRID_ANALYSIS_COLUMNS);
          break;

        case COL_ANALYSIS_SHOW_ROW_EMPLOYEE:
        case COL_ANALYSIS_ROW_SPLIT_LEVELS:
          gridNames.add(GRID_ANALYSIS_ROWS);
          break;

        default:
          if (getAnalysisShowColumnDimension(source) != null) {
            gridNames.add(GRID_ANALYSIS_COLUMNS);
          } else if (getAnalysisShowRowDimension(source) != null) {
            gridNames.add(GRID_ANALYSIS_ROWS);
          }
      }

      if (!gridNames.isEmpty()) {
        for (String gridName : gridNames) {
          GridView gridView = ViewHelper.getChildGrid(getFormView(), gridName);

          if (gridView != null) {
            gridView.getGrid().render(false);
          }
        }
      }
    }

    super.onSourceChange(row, source, value);
  }
}

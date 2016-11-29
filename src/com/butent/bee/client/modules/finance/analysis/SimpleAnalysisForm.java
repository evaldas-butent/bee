package com.butent.bee.client.modules.finance.analysis;

import com.google.gwt.xml.client.Element;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.modules.finance.FinanceUtils;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleAnalysisForm extends AbstractFormInterceptor {

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "fin-SimpleAnalysis-";
  private static final String STYLE_INFO_SUMMARY = STYLE_PREFIX + "info-summary";

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
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if ("pages".equalsIgnoreCase(name) && widget instanceof TabbedPages) {
      ((TabbedPages) widget).setSummaryRenderer("dimensions",
          summary -> FinanceUtils.summarizeDimensions(getDataColumns(), getActiveRow()));

      ((TabbedPages) widget).setSummaryRenderer("info", summary -> summarizeInfo());
    }

    super.afterCreateWidget(name, widget, callback);
  }

  private String summarizeInfo() {
    List<BeeColumn> columns = getDataColumns();
    IsRow row = getActiveRow();

    if (BeeUtils.isEmpty(columns) || row == null) {
      return BeeConst.STRING_EMPTY;
    }

    List<Div> elements = new ArrayList<>();

    Long indicatorId = DataUtils.getLongQuietly(columns, row, COL_ANALYSIS_HEADER_INDICATOR);
    String indicatorName = DataUtils.getStringQuietly(columns, row, COL_FIN_INDICATOR_NAME);

    if (DataUtils.isId(indicatorId) && !BeeUtils.isEmpty(indicatorName)) {
      Div div = new Div().text(indicatorName.trim());

      FinanceUtils.setColors(div,
          DataUtils.getStringQuietly(columns, row, ALS_INDICATOR_BACKGROUND),
          DataUtils.getStringQuietly(columns, row, ALS_INDICATOR_FOREGROUND));

      elements.add(div);
    }

    Long budgetTypeId = DataUtils.getLongQuietly(columns, row, COL_ANALYSIS_HEADER_BUDGET_TYPE);
    String budgetTypeName = DataUtils.getStringQuietly(columns, row, COL_BUDGET_TYPE_NAME);

    if (DataUtils.isId(budgetTypeId) && !BeeUtils.isEmpty(budgetTypeName)) {
      Div div = new Div().text(budgetTypeName.trim());

      FinanceUtils.setColors(div,
          DataUtils.getStringQuietly(columns, row, ALS_BUDGET_TYPE_BACKGROUND),
          DataUtils.getStringQuietly(columns, row, ALS_BUDGET_TYPE_FOREGROUND));

      elements.add(div);
    }

    Long employeeId = DataUtils.getLongQuietly(columns, row, COL_ANALYSIS_HEADER_EMPLOYEE);
    String employeeName = BeeUtils.joinWords(
        DataUtils.getStringQuietly(columns, row, ALS_EMPLOYEE_FIRST_NAME),
        DataUtils.getStringQuietly(columns, row, ALS_EMPLOYEE_LAST_NAME));

    if (DataUtils.isId(employeeId) && !BeeUtils.isEmpty(employeeName)) {
      Div div = new Div().text(employeeName);
      elements.add(div);
    }

    if (elements.isEmpty()) {
      return BeeConst.STRING_EMPTY;
    } else {
      return new Div().addClass(STYLE_INFO_SUMMARY).append(elements).build();
    }
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

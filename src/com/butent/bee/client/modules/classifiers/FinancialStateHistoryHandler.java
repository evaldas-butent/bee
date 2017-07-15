package com.butent.bee.client.modules.classifiers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.client.data.LocalProvider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.modules.administration.HistoryHandler;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class FinancialStateHistoryHandler extends HistoryHandler {
  private static final String FILTER_KEY = "f1";
  private static final String AUDIT_FLD_PARSED_VALUE = "ParsedValue";

  private final Map<Long, String> finacialStateValues = Maps.newHashMap();

  FinancialStateHistoryHandler(String viewName, Collection<Long> ids) {
    super(viewName, ids);
  }

  private static Filter getFilter() {
    Filter filter =
        Filter.isEqual(AdministrationConstants.COL_RELATION,
            Value.getValue(ClassifierConstants.VIEW_FINANCIAL_STATES));

    filter = Filter.and(filter, Filter.notNull(AdministrationConstants.COL_RELATION));

    return filter;
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (presenter != null && presenter.getDataProvider() instanceof LocalProvider) {
      LocalProvider provider = (LocalProvider) presenter.getDataProvider();
      provider.setUserFilter(getFilter());
      provider.setParentFilter(FILTER_KEY, getFilter());

      super.afterCreatePresenter(presenter);
    }
  }

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {

    if (BeeUtils.same(columnName, AUDIT_FLD_PARSED_VALUE) && column instanceof HasCellRenderer) {
      ((HasCellRenderer) column).setRenderer(getAuditFieldValueRenderer(dataColumns));
    }

    super.afterCreateColumn(columnName, dataColumns, column, header, footer, editableColumn);
    return true;
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    return super.beforeAction(action, presenter);
  }

  @Override
  public void beforeCreate(List<? extends IsColumn> dataColumns, GridDescription gridDescription) {
    loadFinancialStateClassifierData();

    if (!gridDescription.hasColumn(AUDIT_FLD_PARSED_VALUE)) {
      ColumnDescription newCol = new ColumnDescription(ColType.CALCULATED, AUDIT_FLD_PARSED_VALUE);
      newCol.setCaption(Localized.dictionary().value());
      gridDescription.addColumn(newCol);

      gridDescription.getColumn(AdministrationConstants.AUDIT_FLD_VALUE).setVisible(false);
      gridDescription.setFilter(getFilter());
    }
    super.beforeCreate(dataColumns, gridDescription);
  }

  @Override
  public Map<String, Filter> getInitialParentFilters(Collection<UiOption> uiOptions) {
    return ImmutableMap.of(FILTER_KEY, getFilter());
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    gridDescription.setFilter(getFilter());
    return true;
  }

  @Override
  public boolean onClose(GridPresenter presenter) {
    GridDescription gridDescription = presenter.getGridView().getGridDescription();

    if (gridDescription.hasColumn(AUDIT_FLD_PARSED_VALUE)) {
      gridDescription.getColumns().remove(
          gridDescription.getColumn(AUDIT_FLD_PARSED_VALUE));
      gridDescription.getColumn(AdministrationConstants.AUDIT_FLD_VALUE).setVisible(true);
    }
    return super.onClose(presenter);
  }

  private AbstractCellRenderer getAuditFieldValueRenderer(final List<? extends IsColumn> columns) {
    return new AbstractCellRenderer(null) {
      @Override
      public String render(IsRow row) {
        int idxFinancialStateValue = DataUtils
            .getColumnIndex(AdministrationConstants.AUDIT_FLD_VALUE, columns);

        String value = finacialStateValues.get(row.getLong(idxFinancialStateValue));
        return value == null ? row.getString(idxFinancialStateValue) : value;
      }
    };
  }

  private void loadFinancialStateClassifierData() {
    Queries.getRowSet(ClassifierConstants.VIEW_FINANCIAL_STATES,
        Lists.newArrayList(ClassifierConstants.COL_FINANCIAL_STATE_NAME), new RowSetCallback() {

          @Override
          public void onSuccess(BeeRowSet result) {
            if (finacialStateValues == null && result == null) {
              return;
            }

            if (result.isEmpty()) {
              return;
            }

            int idxName = DataUtils.getColumnIndex(ClassifierConstants.COL_FINANCIAL_STATE_NAME,
                result.getColumns());

            finacialStateValues.clear();

            for (IsRow row : result) {
              finacialStateValues.put(row.getId(), row.getString(idxName));
            }
          }
        });
  }
}

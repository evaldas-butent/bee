package com.butent.bee.server.modules.finance;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.modules.finance.NormalBalance;
import com.butent.bee.shared.modules.finance.analysis.AnalysisCellType;
import com.butent.bee.shared.modules.finance.analysis.AnalysisResults;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplitType;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplitValue;
import com.butent.bee.shared.modules.finance.analysis.AnalysisUtils;
import com.butent.bee.shared.modules.finance.analysis.AnalysisValue;
import com.butent.bee.shared.modules.finance.analysis.TurnoverOrBalance;
import com.butent.bee.shared.modules.finance.analysis.IndicatorSource;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.time.MonthRange;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.time.YearQuarter;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class AnalysisBean {

  private static BeeLogger logger = LogUtils.getLogger(AnalysisBean.class);

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  UserServiceBean usr;
  @EJB
  ParamHolderBean prm;

  public ResponseObject calculateForm(long formId) {
    long initStart = System.currentTimeMillis();

    BeeView finView = sys.getView(VIEW_FINANCIAL_RECORDS);
    Long userId = usr.getCurrentUserId();

    ensureDimensions();

    AnalysisFormData formData = getFormData(formId);

    long validateStart = System.currentTimeMillis();
    ResponseObject response = validateFormData(formId, formData, finView, userId);
    if (response.hasMessages()) {
      return response;
    }

    long computeStart = System.currentTimeMillis();
    AnalysisResults results = new AnalysisResults(
        formData.getHeaderIndexes(), formData.getHeader(),
        formData.getColumnIndexes(), formData.getColumns(),
        formData.getRowIndexes(), formData.getRows());

    formData.getColumns().forEach(column ->
        results.addColumnSplitTypes(column.getId(), formData.getColumnSplits(column)));
    formData.getRows().forEach(row ->
        results.addRowSplitTypes(row.getId(), formData.getRowSplits(row)));

    Function<String, Filter> filterParser = input -> finView.parseFilter(input, userId);

    MonthRange headerRange = formData.getHeaderRange();

    Filter headerFilter = formData.getHeaderFilter(COL_FIN_EMPLOYEE, filterParser);
    AnalysisFilter headerAnalysisFilter = formData.getHeaderAnalysisFilter();

    Long headerBudgetType = formData.getHeaderLong(COL_ANALYSIS_HEADER_BUDGET_TYPE);

    for (BeeRow column : formData.getColumns()) {
      if (formData.columnIsPrimary(column)) {
        MonthRange columnRange = AnalysisUtils.intersection(headerRange,
            formData.getColumnRange(column));

        Filter columnFilter = formData.getColumnFilter(column, COL_FIN_EMPLOYEE, filterParser);
        AnalysisFilter columnAnalysisFilter = formData.getColumnAnalysisFilter(column);

        Long columnIndicator = formData.getColumnLong(column, COL_ANALYSIS_COLUMN_INDICATOR);
        TurnoverOrBalance columnTurnoverOrBalance = formData.getColumnEnum(column,
            COL_ANALYSIS_COLUMN_TURNOVER_OR_BALANCE, TurnoverOrBalance.class);

        List<AnalysisCellType> columnCellTypes = AnalysisCellType.normalize(
            AnalysisCellType.decode(formData.getColumnString(column, COL_ANALYSIS_COLUMN_VALUES)));
        Long columnBudgetType = formData.getColumnLong(column, COL_ANALYSIS_COLUMN_BUDGET_TYPE);

        List<AnalysisSplitType> columnSplitTypes = results.getColumnSplitTypes(column.getId());

        for (BeeRow row : formData.getRows()) {
          if (formData.rowIsPrimary(row)) {
            Long rowIndicator = formData.getRowLong(row, COL_ANALYSIS_ROW_INDICATOR);
            Long indicator = DataUtils.isId(columnIndicator) ? columnIndicator : rowIndicator;

            if (DataUtils.isId(indicator)) {
              List<AnalysisCellType> rowCellTypes = AnalysisCellType.normalize(
                  AnalysisCellType.decode(formData.getRowString(row, COL_ANALYSIS_ROW_VALUES)));

              Long rowBudgetType = formData.getRowLong(row, COL_ANALYSIS_ROW_BUDGET_TYPE);
              Long budgetType = BeeUtils.nvl(columnBudgetType, rowBudgetType, headerBudgetType);

              TurnoverOrBalance rowTurnoverOrBalance = formData.getRowEnum(row,
                  COL_ANALYSIS_ROW_TURNOVER_OR_BALANCE, TurnoverOrBalance.class);
              TurnoverOrBalance indicatorTurnoverOrBalance =
                  getIndicatorTurnoverOrBalance(indicator);
              TurnoverOrBalance turnoverOrBalance = BeeUtils.nvl(columnTurnoverOrBalance,
                  rowTurnoverOrBalance, indicatorTurnoverOrBalance);

              NormalBalance normalBalance = getIndicatorNormalBalance(indicator);

              Pair<Filter, Filter> accountFilters =
                  getIndicatorAccountFilters(indicator, turnoverOrBalance, normalBalance);

              MonthRange rowRange = formData.getRowRange(row);
              MonthRange range = AnalysisUtils.intersection(columnRange, rowRange);

              Filter rowFilter = formData.getRowFilter(row, COL_FIN_EMPLOYEE, filterParser);
              Filter parentFilter = Filter.and(headerFilter, columnFilter, rowFilter);

              AnalysisFilter rowAnalysisFilter = formData.getRowAnalysisFilter(row);

              String sourceColumn = getIndicatorSourceColumn(indicator);

              Filter valueFilter = getActualValueFilter(indicator, turnoverOrBalance, range,
                  parentFilter, sourceColumn, finView, userId);

              Filter plusFilter = accountFilters.getA();
              Filter minusFilter = accountFilters.getB();

              List<AnalysisSplitType> rowSplitTypes = results.getRowSplitTypes(row.getId());

              Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues =
                  new EnumMap<>(AnalysisSplitType.class);
              Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues =
                  new EnumMap<>(AnalysisSplitType.class);

              columnSplitTypes.forEach(type -> {
                List<AnalysisSplitValue> splitValues =
                    getSplitValues(type, valueFilter, plusFilter, minusFilter, finView, userId);

                if (!splitValues.isEmpty()) {
                  columnSplitValues.put(type, splitValues);
                  results.addColumnSplitValues(column.getId(), type, splitValues);
                }
              });

              rowSplitTypes.forEach(type -> {
                List<AnalysisSplitValue> splitValues =
                    getSplitValues(type, valueFilter, plusFilter, minusFilter, finView, userId);

                if (!splitValues.isEmpty()) {
                  rowSplitValues.put(type, splitValues);
                  results.addRowSplitValues(row.getId(), type, splitValues);
                }
              });

              results.addValues(computeActualValues(column.getId(), row.getId(),
                  valueFilter, plusFilter, minusFilter, turnoverOrBalance,
                  columnSplitTypes, columnSplitValues, rowSplitTypes, rowSplitValues,
                  sourceColumn, finView, userId));

              String budgetCursor = null;
              if (DataUtils.isId(budgetType)
                  && (AnalysisCellType.needsBudget(columnCellTypes)
                  || AnalysisCellType.needsBudget(rowCellTypes))) {

                budgetCursor = getBudgetCursor(indicator, indicatorTurnoverOrBalance,
                    budgetType, turnoverOrBalance);

                if (!qs.isEmpty(budgetCursor)) {
                  IsCondition budgetCondition =
                      getBudgetCondition(budgetCursor, COL_BUDGET_ENTRY_EMPLOYEE,
                          headerAnalysisFilter, columnAnalysisFilter, rowAnalysisFilter);

                  results.mergeValues(computeBudgetValues(column.getId(), row.getId(),
                      budgetCursor, budgetCondition, range, turnoverOrBalance,
                      columnSplitTypes, columnSplitValues, rowSplitTypes, rowSplitValues));
                }
              }

              if (budgetCursor != null) {
                qs.sqlDropTemp(budgetCursor);
              }
            }
          }
        }
      }
    }

    if (!results.isEmpty()) {
      results.setInitStart(initStart);
      results.setValidateStart(validateStart);
      results.setComputeStart(computeStart);
      results.setComputeEnd(System.currentTimeMillis());

      response.setResponse(results);
    }

    return response;
  }

  public ResponseObject verifyForm(long formId) {
    BeeView finView = sys.getView(VIEW_FINANCIAL_RECORDS);
    Long userId = usr.getCurrentUserId();

    ensureDimensions();

    AnalysisFormData formData = getFormData(formId);
    return validateFormData(formId, formData, finView, userId);
  }

  private void addDimensions(SqlSelect query, String tblName) {
    if (Dimensions.getObserved() > 0) {
      query.addFromLeft(Dimensions.TBL_EXTRA_DIMENSIONS,
          sys.joinTables(Dimensions.TBL_EXTRA_DIMENSIONS, tblName,
              Dimensions.COL_EXTRA_DIMENSIONS));

      for (int ordinal = 1; ordinal <= Dimensions.getObserved(); ordinal++) {
        query.addFields(Dimensions.TBL_EXTRA_DIMENSIONS, Dimensions.getRelationColumn(ordinal));
      }
    }
  }

  private void ensureDimensions() {
    Integer count = prm.getInteger(Dimensions.PRM_DIMENSIONS);

    if (BeeUtils.isPositive(count)) {
      Dimensions.setObserved(count);
      Dimensions.loadNames(qs.getViewData(Dimensions.VIEW_NAMES), usr.getLanguage());
    }
  }

  private AnalysisFormData getFormData(long formId) {
    BeeRowSet headerData = qs.getViewDataById(VIEW_ANALYSIS_HEADERS, formId);
    if (DataUtils.isEmpty(headerData)) {
      logger.warning(VIEW_ANALYSIS_HEADERS, "form", formId, "not found");
      return null;
    }

    BeeRowSet columnData =
        qs.getViewData(VIEW_ANALYSIS_COLUMNS, Filter.equals(COL_ANALYSIS_HEADER, formId));
    BeeRowSet rowData =
        qs.getViewData(VIEW_ANALYSIS_ROWS, Filter.equals(COL_ANALYSIS_HEADER, formId));

    CompoundFilter filter = Filter.or();
    filter.add(Filter.equals(COL_ANALYSIS_HEADER, formId));

    if (!DataUtils.isEmpty(columnData)) {
      filter.add(Filter.any(COL_ANALYSIS_COLUMN, columnData.getRowIds()));
    }
    if (!DataUtils.isEmpty(rowData)) {
      filter.add(Filter.any(COL_ANALYSIS_ROW, rowData.getRowIds()));
    }

    BeeRowSet filterData = qs.getViewData(VIEW_ANALYSIS_FILTERS, filter);

    return new AnalysisFormData(headerData, columnData, rowData, filterData);
  }

  private ResponseObject validateFormData(long formId, AnalysisFormData formData,
      BeeView finView, Long userId) {

    if (formData == null) {
      return ResponseObject.error("form", formId, "not found");
    }

    ResponseObject response = ResponseObject.emptyResponse();

    Predicate<String> extraFilterValidator = input ->
        BeeUtils.isEmpty(input) || finView.parseFilter(input, userId) != null;

    List<String> messages = formData.validate(usr.getDictionary(), extraFilterValidator);
    if (!BeeUtils.isEmpty(messages)) {
      for (String message : messages) {
        response.addWarning(message);
      }
    }

    return response;
  }

  private Filter getIndicatorFilter(long indicator, BeeView finView, Long userId) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_INDICATOR_FILTERS, COL_INDICATOR_FILTER_EMPLOYEE,
            COL_INDICATOR_FILTER_EXTRA, COL_INDICATOR_FILTER_INCLUDE)
        .addFrom(TBL_INDICATOR_FILTERS)
        .setWhere(SqlUtils.equals(TBL_INDICATOR_FILTERS, COL_FIN_INDICATOR, indicator));

    addDimensions(query, TBL_INDICATOR_FILTERS);

    SimpleRowSet filterData = qs.getData(query);
    if (DataUtils.isEmpty(filterData)) {
      return null;
    }

    CompoundFilter include = Filter.or();
    CompoundFilter exclude = Filter.or();

    for (SimpleRow filterRow : filterData) {
      Filter filter = getFilter(filterRow,
          COL_INDICATOR_FILTER_EMPLOYEE, COL_INDICATOR_FILTER_EXTRA, finView, userId);

      if (filter != null) {
        if (filterRow.isTrue(COL_INDICATOR_FILTER_INCLUDE)) {
          include.add(filter);
        } else {
          exclude.add(filter);
        }
      }
    }

    return AnalysisUtils.joinFilters(include, exclude);
  }

  private static Filter getFilter(SimpleRow row, String employeeColumn, String extraFilterColumn,
      BeeView finView, Long userId) {

    CompoundFilter filter = Filter.and();

    if (row.hasColumn(employeeColumn)) {
      Long employee = row.getLong(employeeColumn);
      if (DataUtils.isId(employee)) {
        filter.add(Filter.equals(COL_FIN_EMPLOYEE, employee));
      }
    }

    if (Dimensions.getObserved() > 0) {
      for (int ordinal = 1; ordinal <= Dimensions.getObserved(); ordinal++) {
        String column = Dimensions.getRelationColumn(ordinal);

        if (row.hasColumn(column)) {
          Long value = row.getLong(column);

          if (DataUtils.isId(value)) {
            filter.add(Filter.equals(column, value));
          }
        }
      }
    }

    if (row.hasColumn(extraFilterColumn) && finView != null) {
      String extraFilter = row.getValue(extraFilterColumn);

      if (!BeeUtils.isEmpty(extraFilter)) {
        Filter extra = finView.parseFilter(extraFilter, userId);
        if (extra != null) {
          filter.add(extra);
        }
      }
    }

    return AnalysisUtils.normalize(filter);
  }

  private List<AnalysisSplitValue> getSplitValues(AnalysisSplitType type,
      Filter parentFilter, Filter plusFilter, Filter minusFilter,
      BeeView finView, Long userId) {

    List<AnalysisSplitValue> result = new ArrayList<>();

    String finColumn = type.getFinColumn();
    if (!finView.hasColumn(finColumn)) {
      return result;
    }

    Filter filter = Filter.and(parentFilter, Filter.or(plusFilter, minusFilter));

    SqlSelect sourceQuery = finView.getQuery(userId, filter, null,
        Collections.singletonList(finColumn));
    String sourceQueryAlias = SqlUtils.uniqueName();

    SqlSelect query = new SqlSelect().setDistinctMode(true)
        .addFields(sourceQueryAlias, finColumn)
        .addFrom(sourceQuery, sourceQueryAlias);

    if (type.isPeriod()) {
      query.addEmptyNumeric(BeeConst.YEAR, 4, 0);
      query.addEmptyNumeric(BeeConst.MONTH, 2, 0);
    }

    String tmp = qs.sqlCreateTemp(query);

    if (!qs.isEmpty(tmp)) {
      if (type.isPeriod()) {
        qs.setYearMonth(tmp, finColumn, BeeConst.YEAR, BeeConst.MONTH);

        switch (type) {
          case MONTH:
            qs.getData(new SqlSelect().setDistinctMode(true)
                .addFields(tmp, BeeConst.YEAR, BeeConst.MONTH)
                .addFrom(tmp)
                .setWhere(SqlUtils.positive(tmp, BeeConst.YEAR, BeeConst.MONTH)))
                .getRows().stream()
                .map(row -> YearMonth.parse(row[0], row[1]))
                .sorted()
                .forEach(ym -> result.add(AnalysisSplitValue.of(ym)));
            break;

          case QUARTER:
            qs.getData(new SqlSelect().setDistinctMode(true)
                .addFields(tmp, BeeConst.YEAR, BeeConst.MONTH)
                .addFrom(tmp)
                .setWhere(SqlUtils.positive(tmp, BeeConst.YEAR, BeeConst.MONTH)))
                .getRows().stream()
                .map(row -> YearQuarter.of(YearMonth.parse(row[0], row[1])))
                .distinct()
                .sorted()
                .forEach(yq -> result.add(AnalysisSplitValue.of(yq)));
            break;

          case YEAR:
            Set<Integer> years = qs.getIntSet(new SqlSelect().setDistinctMode(true)
                .addFields(tmp, BeeConst.YEAR)
                .addFrom(tmp));

            years.stream().filter(TimeUtils::isYear).sorted().forEach(year ->
                result.add(AnalysisSplitValue.of(year)));
            break;

          default:
            logger.severe(type, "split not implemented");
        }

      } else if (type.isDimension()) {
        Set<Long> ids = qs.getLongSet(new SqlSelect().addFields(tmp, finColumn).addFrom(tmp));

        if (ids.contains(null)) {
          result.add(AnalysisSplitValue.absent());
          ids.remove(null);
        }

        if (!ids.isEmpty()) {
          int ordinal = type.getIndex();

          String tableName = Dimensions.getTableName(ordinal);

          String idColumn = sys.getIdName(tableName);
          String nameColumn = Dimensions.getNameColumn(ordinal);
          String bgColumn = Dimensions.getBackgroundColumn(ordinal);
          String fgColumn = Dimensions.getForegroundColumn(ordinal);

          SimpleRowSet data = qs.getData(new SqlSelect()
              .addFields(tableName, idColumn, nameColumn, bgColumn, fgColumn)
              .addFrom(tableName)
              .setWhere(SqlUtils.inList(tableName, idColumn, ids))
              .addOrder(tableName, Dimensions.COL_ORDINAL, nameColumn));

          if (!DataUtils.isEmpty(data)) {
            for (SimpleRow row : data) {
              AnalysisSplitValue splitValue = AnalysisSplitValue.of(row.getValue(nameColumn),
                  row.getLong(idColumn));

              splitValue.setBackground(row.getValue(bgColumn));
              splitValue.setForeground(row.getValue(fgColumn));

              result.add(splitValue);
            }
          }
        }

      } else if (type == AnalysisSplitType.EMPLOYEE) {
        Set<Long> ids = qs.getLongSet(new SqlSelect().addFields(tmp, finColumn).addFrom(tmp));

        if (ids.contains(null)) {
          result.add(AnalysisSplitValue.absent());
          ids.remove(null);
        }

        if (!ids.isEmpty()) {
          BeeRowSet rowSet = qs.getViewData(PayrollConstants.VIEW_EMPLOYEES, Filter.idIn(ids));

          if (!DataUtils.isEmpty(rowSet)) {
            int firstNameIndex = rowSet.getColumnIndex(COL_FIRST_NAME);
            int lastNameIndex = rowSet.getColumnIndex(COL_LAST_NAME);

            for (BeeRow row : rowSet) {
              String value = BeeUtils.joinWords(row.getString(firstNameIndex),
                  row.getString(lastNameIndex));

              result.add(AnalysisSplitValue.of(value, row.getId()));
            }
          }
        }

      } else {
        logger.severe(type, "split not implemented");
      }
    }

    qs.sqlDropTemp(tmp);
    return result;
  }

  private Collection<String> getIndicatorAccountCodes(long indicator) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_CHART_OF_ACCOUNTS, COL_ACCOUNT_CODE)
        .addFrom(TBL_INDICATOR_ACCOUNTS)
        .addFromInner(TBL_CHART_OF_ACCOUNTS, sys.joinTables(TBL_CHART_OF_ACCOUNTS,
            TBL_INDICATOR_ACCOUNTS, COL_INDICATOR_ACCOUNT))
        .setWhere(SqlUtils.equals(TBL_INDICATOR_ACCOUNTS, COL_FIN_INDICATOR, indicator));

    return qs.getValueSet(query);
  }

  private TurnoverOrBalance getIndicatorTurnoverOrBalance(long indicator) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_FINANCIAL_INDICATORS, COL_FIN_INDICATOR_TURNOVER_OR_BALANCE)
        .addFrom(TBL_FINANCIAL_INDICATORS)
        .setWhere(sys.idEquals(TBL_FINANCIAL_INDICATORS, indicator));

    TurnoverOrBalance turnoverOrBalance = qs.getEnum(query, TurnoverOrBalance.class);
    return (turnoverOrBalance == null) ? TurnoverOrBalance.DEFAULT : turnoverOrBalance;
  }

  private NormalBalance getIndicatorNormalBalance(long indicator) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_FINANCIAL_INDICATORS, COL_FIN_INDICATOR_NORMAL_BALANCE)
        .addFrom(TBL_FINANCIAL_INDICATORS)
        .setWhere(sys.idEquals(TBL_FINANCIAL_INDICATORS, indicator));

    NormalBalance normalBalance = qs.getEnum(query, NormalBalance.class);

    if (normalBalance == null) {
      SqlSelect accountQuery = new SqlSelect().setDistinctMode(true)
          .addFields(TBL_CHART_OF_ACCOUNTS, COL_ACCOUNT_NORMAL_BALANCE)
          .addFrom(TBL_INDICATOR_ACCOUNTS)
          .addFromInner(TBL_CHART_OF_ACCOUNTS, sys.joinTables(TBL_CHART_OF_ACCOUNTS,
              TBL_INDICATOR_ACCOUNTS, COL_INDICATOR_ACCOUNT))
          .setWhere(SqlUtils.and(
              SqlUtils.equals(TBL_INDICATOR_ACCOUNTS, COL_FIN_INDICATOR, indicator),
              SqlUtils.notNull(TBL_CHART_OF_ACCOUNTS, COL_ACCOUNT_NORMAL_BALANCE)));

      Optional<Integer> optional = qs.getIntSet(accountQuery).stream().findFirst();
      if (optional.isPresent()) {
        normalBalance = EnumUtils.getEnumByIndex(NormalBalance.class, optional.get());
      }
    }

    return (normalBalance == null) ? NormalBalance.DEFAULT : normalBalance;
  }

  private String getIndicatorSourceColumn(long indicator) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_FINANCIAL_INDICATORS, COL_FIN_INDICATOR_SOURCE)
        .addFrom(TBL_FINANCIAL_INDICATORS)
        .setWhere(sys.idEquals(TBL_FINANCIAL_INDICATORS, indicator));

    IndicatorSource indicatorSource = qs.getEnum(query, IndicatorSource.class);
    if (indicatorSource == null) {
      indicatorSource = IndicatorSource.DEFAULT;
    }

    return indicatorSource.getSourceColumn();
  }

  private Collection<AnalysisValue> computeActualValues(long columnId, long rowId,
      Filter filter, Filter plusFilter, Filter minusFilter, TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> columnSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues,
      List<AnalysisSplitType> rowSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues,
      String sourceColumn, BeeView finView, Long userId) {

    List<AnalysisValue> values = new ArrayList<>();

    boolean hasColumnSplits = !columnSplitTypes.isEmpty() && !columnSplitValues.isEmpty();
    boolean hasRowSplits = !rowSplitTypes.isEmpty() && !rowSplitValues.isEmpty();

    if (hasColumnSplits && hasRowSplits) {
      values.addAll(computeActualSplitMatrix(columnId, rowId, null,
          filter, plusFilter, minusFilter, turnoverOrBalance,
          columnSplitTypes, 0, columnSplitValues, 0,
          rowSplitTypes, rowSplitValues, sourceColumn, finView, userId));

    } else if (hasColumnSplits) {
      values.addAll(computeActualSplitVector(columnId, rowId, true, null,
          filter, plusFilter, minusFilter, turnoverOrBalance,
          columnSplitTypes, 0, columnSplitValues, 0,
          sourceColumn, finView, userId));

    } else if (hasRowSplits) {
      values.addAll(computeActualSplitVector(columnId, rowId, false, null,
          filter, plusFilter, minusFilter, turnoverOrBalance,
          rowSplitTypes, 0, rowSplitValues, 0,
          sourceColumn, finView, userId));

    } else {
      double value = getActualValue(filter, plusFilter, minusFilter, sourceColumn, finView, userId);
      values.add(AnalysisValue.actual(columnId, rowId, value));
    }

    return values;
  }

  private Collection<AnalysisValue> computeActualSplitVector(long columnId, long rowId,
      boolean isColumn, Integer parentValueIndex,
      Filter parentFilter, Filter plusFilter, Filter minusFilter,
      TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> splitTypes, int typeIndex,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> splitValues, int valueIndex,
      String sourceColumn, BeeView finView, Long userId) {

    List<AnalysisValue> values = new ArrayList<>();

    AnalysisSplitType splitType = splitTypes.get(typeIndex);

    if (splitValues.containsKey(splitType)) {
      List<AnalysisSplitValue> typeValues = splitValues.get(splitType);
      AnalysisSplitValue splitValue = typeValues.get(valueIndex);

      Filter filter = Filter.and(parentFilter,
          splitType.getFinFilter(splitValue, turnoverOrBalance));

      double value = getActualValue(filter, plusFilter, minusFilter, sourceColumn, finView, userId);
      AnalysisValue av = AnalysisValue.actual(columnId, rowId, value);

      if (isColumn) {
        av.setColumnParentValueIndex(parentValueIndex);
        av.setColumnSplitTypeIndex(typeIndex);
        av.setColumnSplitValueIndex(valueIndex);

      } else {
        av.setRowParentValueIndex(parentValueIndex);
        av.setRowSplitTypeIndex(typeIndex);
        av.setRowSplitValueIndex(valueIndex);
      }

      values.add(av);

      if (typeIndex < splitTypes.size() - 1) {
        values.addAll(computeActualSplitVector(columnId, rowId, isColumn, valueIndex,
            filter, plusFilter, minusFilter, turnoverOrBalance,
            splitTypes, typeIndex + 1, splitValues, 0,
            sourceColumn, finView, userId));
      }

      if (valueIndex < typeValues.size() - 1) {
        values.addAll(computeActualSplitVector(columnId, rowId, isColumn, parentValueIndex,
            parentFilter, plusFilter, minusFilter, turnoverOrBalance,
            splitTypes, typeIndex, splitValues, valueIndex + 1,
            sourceColumn, finView, userId));
      }
    }

    return values;
  }

  private Collection<AnalysisValue> computeActualSplitMatrix(long columnId, long rowId,
      Integer columnParentValueIndex,
      Filter parentFilter, Filter plusFilter, Filter minusFilter,
      TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> columnSplitTypes, int columnTypeIndex,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues, int columnValueIndex,
      List<AnalysisSplitType> rowSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues,
      String sourceColumn, BeeView finView, Long userId) {

    List<AnalysisValue> values = new ArrayList<>();

    AnalysisSplitType columnSplitType = columnSplitTypes.get(columnTypeIndex);

    if (columnSplitValues.containsKey(columnSplitType)) {
      List<AnalysisSplitValue> columnTypeValues = columnSplitValues.get(columnSplitType);
      AnalysisSplitValue columnSplitValue = columnTypeValues.get(columnValueIndex);

      Filter filter = Filter.and(parentFilter,
          columnSplitType.getFinFilter(columnSplitValue, turnoverOrBalance));

      Collection<AnalysisValue> rowValues = computeActualSplitVector(columnId, rowId, false, null,
          filter, plusFilter, minusFilter, turnoverOrBalance,
          rowSplitTypes, 0, rowSplitValues, 0,
          sourceColumn, finView, userId);

      if (!rowValues.isEmpty()) {
        for (AnalysisValue av : rowValues) {
          av.setColumnParentValueIndex(columnParentValueIndex);
          av.setColumnSplitTypeIndex(columnTypeIndex);
          av.setColumnSplitValueIndex(columnValueIndex);

          values.add(av);
        }
      }

      if (columnTypeIndex < columnSplitTypes.size() - 1) {
        values.addAll(computeActualSplitMatrix(columnId, rowId, columnValueIndex,
            filter, plusFilter, minusFilter, turnoverOrBalance,
            columnSplitTypes, columnTypeIndex + 1, columnSplitValues, 0,
            rowSplitTypes, rowSplitValues, sourceColumn, finView, userId));
      }

      if (columnValueIndex < columnTypeValues.size() - 1) {
        values.addAll(computeActualSplitMatrix(columnId, rowId, columnParentValueIndex,
            parentFilter, plusFilter, minusFilter, turnoverOrBalance,
            columnSplitTypes, columnTypeIndex, columnSplitValues, columnValueIndex + 1,
            rowSplitTypes, rowSplitValues, sourceColumn, finView, userId));
      }
    }

    return values;
  }

  private double getActualValue(Filter filter, Filter plusFilter, Filter minusFilter,
      String sourceColumn, BeeView finView, Long userId) {

    double value = BeeConst.DOUBLE_ZERO;

    if (plusFilter != null) {
      Double plus = getSum(finView, userId, Filter.and(filter, plusFilter), sourceColumn);
      if (BeeUtils.nonZero(plus)) {
        value += plus;
      }
    }

    if (minusFilter != null) {
      Double minus = getSum(finView, userId, Filter.and(filter, minusFilter), sourceColumn);
      if (BeeUtils.nonZero(minus)) {
        value -= minus;
      }
    }

    return value;
  }

  private Pair<Filter, Filter> getIndicatorAccountFilters(long indicator,
      TurnoverOrBalance turnoverOrBalance, NormalBalance normalBalance) {

    CompoundFilter plusFilter = CompoundFilter.or();
    CompoundFilter minusFilter = CompoundFilter.or();

    Collection<String> accountCodes = getIndicatorAccountCodes(indicator);

    if (!BeeUtils.isEmpty(accountCodes)) {
      for (String code : accountCodes) {
        plusFilter.add(turnoverOrBalance.getPlusFilter(ALS_DEBIT_CODE, ALS_CREDIT_CODE, code,
            normalBalance));

        minusFilter.add(turnoverOrBalance.getMinusFilter(ALS_DEBIT_CODE, ALS_CREDIT_CODE, code,
            normalBalance));
      }
    }

    return Pair.of(AnalysisUtils.normalize(plusFilter), AnalysisUtils.normalize(minusFilter));
  }

  private Filter getActualValueFilter(long indicator, TurnoverOrBalance turnoverOrBalance,
      MonthRange range, Filter parentFilter, String sourceColumn, BeeView finView, Long userId) {

    Filter indicatorFilter = getIndicatorFilter(indicator, finView, userId);

    Filter rangeFilter = (range == null)
        ? null : turnoverOrBalance.getRangeFilter(COL_FIN_DATE, range);

    Filter sourceFilter = Filter.nonZero(sourceColumn);

    return Filter.and(parentFilter, indicatorFilter, rangeFilter, sourceFilter);
  }

  private Double getSum(BeeView view, Long userId, Filter filter, String column) {
    SqlSelect query = view.getQuery(userId, filter, null, Collections.singleton(column));
    String alias = SqlUtils.uniqueName();

    return qs.getDouble(new SqlSelect().addSum(alias, column).addFrom(query, alias));
  }

  private String getBudgetCursor(long indicator, TurnoverOrBalance indicatorTurnoverOrBalance,
      long budgetType, TurnoverOrBalance parentTurnoverOrBalance) {

    SqlSelect query = new SqlSelect()
        .addExpr(
            SqlUtils.sqlIf(
                SqlUtils.and(
                    SqlUtils.notNull(TBL_BUDGET_HEADERS, COL_BUDGET_SHOW_ENTRY_EMPLOYEE),
                    SqlUtils.notNull(TBL_BUDGET_ENTRIES, COL_BUDGET_ENTRY_EMPLOYEE)),
                SqlUtils.field(TBL_BUDGET_ENTRIES, COL_BUDGET_ENTRY_EMPLOYEE),
                SqlUtils.field(TBL_BUDGET_HEADERS, COL_BUDGET_HEADER_EMPLOYEE)),
            COL_BUDGET_ENTRY_EMPLOYEE)
        .addExpr(
            SqlUtils.nvl(
                SqlUtils.field(TBL_BUDGET_HEADERS, COL_BUDGET_HEADER_YEAR),
                SqlUtils.field(TBL_BUDGET_ENTRIES, COL_BUDGET_ENTRY_YEAR)),
            COL_BUDGET_ENTRY_YEAR)
        .addFields(TBL_BUDGET_ENTRIES, COL_BUDGET_ENTRY_VALUES)
        .addFields(TBL_BUDGET_HEADERS, COL_BUDGET_HEADER_CURRENCY,
            COL_BUDGET_HEADER_BACKGROUND, COL_BUDGET_HEADER_FOREGROUND)
        .addFrom(TBL_BUDGET_ENTRIES)
        .addFromInner(TBL_BUDGET_HEADERS,
            sys.joinTables(TBL_BUDGET_HEADERS, TBL_BUDGET_ENTRIES, COL_BUDGET_HEADER));

    if (Dimensions.getObserved() > 0) {
      String headerExtraAlias = SqlUtils.uniqueName("Bh");
      String entryExtraAlias = SqlUtils.uniqueName("Be");

      String extraIdName = sys.getIdName(Dimensions.TBL_EXTRA_DIMENSIONS);

      query.addFromLeft(Dimensions.TBL_EXTRA_DIMENSIONS, headerExtraAlias,
          SqlUtils.join(headerExtraAlias, extraIdName,
              TBL_BUDGET_HEADERS, Dimensions.COL_EXTRA_DIMENSIONS));
      query.addFromLeft(Dimensions.TBL_EXTRA_DIMENSIONS, entryExtraAlias,
          SqlUtils.join(entryExtraAlias, extraIdName,
              TBL_BUDGET_ENTRIES, Dimensions.COL_EXTRA_DIMENSIONS));

      for (int ordinal = 1; ordinal <= Dimensions.getObserved(); ordinal++) {
        String relationColumn = Dimensions.getRelationColumn(ordinal);

        query.addExpr(
            SqlUtils.sqlIf(
                SqlUtils.and(
                    SqlUtils.notNull(TBL_BUDGET_HEADERS, colBudgetShowEntryDimension(ordinal)),
                    SqlUtils.notNull(entryExtraAlias, relationColumn)),
                SqlUtils.field(entryExtraAlias, relationColumn),
                SqlUtils.field(headerExtraAlias, relationColumn)),
            relationColumn);
      }
    }

    HasConditions conditions = SqlUtils.and(
        SqlUtils.or(
            SqlUtils.equals(TBL_BUDGET_HEADERS, COL_BUDGET_HEADER_INDICATOR, indicator),
            SqlUtils.and(
                SqlUtils.isNull(TBL_BUDGET_HEADERS, COL_BUDGET_HEADER_INDICATOR),
                SqlUtils.equals(TBL_BUDGET_ENTRIES, COL_BUDGET_ENTRY_INDICATOR, indicator))),
        SqlUtils.or(
            SqlUtils.equals(TBL_BUDGET_HEADERS, COL_BUDGET_HEADER_TYPE, budgetType),
            SqlUtils.and(
                SqlUtils.isNull(TBL_BUDGET_HEADERS, COL_BUDGET_HEADER_TYPE),
                SqlUtils.equals(TBL_BUDGET_ENTRIES, COL_BUDGET_ENTRY_TYPE, budgetType))),
        SqlUtils.or(
            SqlUtils.positive(TBL_BUDGET_HEADERS, COL_BUDGET_HEADER_YEAR),
            SqlUtils.positive(TBL_BUDGET_ENTRIES, COL_BUDGET_ENTRY_YEAR))
    );

    HasConditions turnoverOrBalanceConditions =
        SqlUtils.or(
            SqlUtils.equals(TBL_BUDGET_ENTRIES, COL_BUDGET_ENTRY_TURNOVER_OR_BALANCE,
                parentTurnoverOrBalance),
            SqlUtils.and(
                SqlUtils.equals(TBL_BUDGET_HEADERS, COL_BUDGET_HEADER_TURNOVER_OR_BALANCE,
                    parentTurnoverOrBalance),
                SqlUtils.isNull(TBL_BUDGET_ENTRIES, COL_BUDGET_ENTRY_TURNOVER_OR_BALANCE)));

    if (parentTurnoverOrBalance == indicatorTurnoverOrBalance) {
      turnoverOrBalanceConditions.add(
          SqlUtils.and(
              SqlUtils.isNull(TBL_BUDGET_HEADERS, COL_BUDGET_HEADER_TURNOVER_OR_BALANCE),
              SqlUtils.isNull(TBL_BUDGET_ENTRIES, COL_BUDGET_ENTRY_TURNOVER_OR_BALANCE)));
    }

    conditions.add(turnoverOrBalanceConditions);
    query.setWhere(conditions);

    return qs.sqlCreateTemp(query);
  }

  private static IsCondition getBudgetCondition(String source, String employeeColumnName,
      AnalysisFilter headerAnalysisFilter, AnalysisFilter columnAnalysisFilter,
      AnalysisFilter rowAnalysisFilter) {

    HasConditions conditions = SqlUtils.and();

    if (headerAnalysisFilter != null) {
      conditions.add(headerAnalysisFilter.getBudgetCondition(source, employeeColumnName));
    }
    if (columnAnalysisFilter != null) {
      conditions.add(columnAnalysisFilter.getBudgetCondition(source, employeeColumnName));
    }
    if (rowAnalysisFilter != null) {
      conditions.add(rowAnalysisFilter.getBudgetCondition(source, employeeColumnName));
    }

    return conditions.isEmpty() ? null : conditions;
  }

  private Collection<AnalysisValue> computeBudgetValues(long columnId, long rowId,
      String source, IsCondition condition, MonthRange range,
      TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> columnSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues,
      List<AnalysisSplitType> rowSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues) {

    List<AnalysisValue> values = new ArrayList<>();

    boolean hasColumnSplits = !columnSplitTypes.isEmpty() && !columnSplitValues.isEmpty();
    boolean hasRowSplits = !rowSplitTypes.isEmpty() && !rowSplitValues.isEmpty();

    if (hasColumnSplits && hasRowSplits) {
      values.addAll(computeBudgetSplitMatrix(columnId, rowId, null,
          source, condition, range, turnoverOrBalance,
          columnSplitTypes, 0, columnSplitValues, 0,
          rowSplitTypes, rowSplitValues));

    } else if (hasColumnSplits) {
      values.addAll(computeBudgetSplitVector(columnId, rowId, true, null,
          source, condition, range, turnoverOrBalance,
          columnSplitTypes, 0, columnSplitValues, 0));

    } else if (hasRowSplits) {
      values.addAll(computeBudgetSplitVector(columnId, rowId, false, null,
          source, condition, range, turnoverOrBalance,
          rowSplitTypes, 0, rowSplitValues, 0));

    } else {
      Double value = getBudgetValue(source, condition, range, turnoverOrBalance);
      if (BeeUtils.isDouble(value)) {
        values.add(AnalysisValue.budget(columnId, rowId, value));
      }
    }

    return values;
  }

  private Collection<AnalysisValue> computeBudgetSplitVector(long columnId, long rowId,
      boolean isColumn, Integer parentValueIndex,
      String source, IsCondition condition, MonthRange range,
      TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> splitTypes, int typeIndex,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> splitValues, int valueIndex) {

    List<AnalysisValue> values = new ArrayList<>();

    AnalysisSplitType splitType = splitTypes.get(typeIndex);

    if (splitValues.containsKey(splitType)) {
      List<AnalysisSplitValue> typeValues = splitValues.get(splitType);
      AnalysisSplitValue splitValue = typeValues.get(valueIndex);

      IsCondition splitCondition = getBudgetSplitCondition(source, splitType, splitValue);
      MonthRange splitRange = getBudgetSplitRange(range, splitType, splitValue);

      Double value = getBudgetValue(source, SqlUtils.and(condition, splitCondition),
          splitRange, turnoverOrBalance);

      if (BeeUtils.isDouble(value)) {
        AnalysisValue av = AnalysisValue.budget(columnId, rowId, value);

        if (isColumn) {
          av.setColumnParentValueIndex(parentValueIndex);
          av.setColumnSplitTypeIndex(typeIndex);
          av.setColumnSplitValueIndex(valueIndex);

        } else {
          av.setRowParentValueIndex(parentValueIndex);
          av.setRowSplitTypeIndex(typeIndex);
          av.setRowSplitValueIndex(valueIndex);
        }

        values.add(av);
      }

      if (typeIndex < splitTypes.size() - 1) {
        values.addAll(computeBudgetSplitVector(columnId, rowId, isColumn, valueIndex,
            source, condition, range, turnoverOrBalance,
            splitTypes, typeIndex + 1, splitValues, 0));
      }

      if (valueIndex < typeValues.size() - 1) {
        values.addAll(computeBudgetSplitVector(columnId, rowId, isColumn, parentValueIndex,
            source, condition, range, turnoverOrBalance,
            splitTypes, typeIndex, splitValues, valueIndex + 1));
      }
    }

    return values;
  }

  private Collection<AnalysisValue> computeBudgetSplitMatrix(long columnId, long rowId,
      Integer columnParentValueIndex,
      String source, IsCondition condition, MonthRange range,
      TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> columnSplitTypes, int columnTypeIndex,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues, int columnValueIndex,
      List<AnalysisSplitType> rowSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues) {

    List<AnalysisValue> values = new ArrayList<>();

    AnalysisSplitType columnSplitType = columnSplitTypes.get(columnTypeIndex);

    if (columnSplitValues.containsKey(columnSplitType)) {
      List<AnalysisSplitValue> columnTypeValues = columnSplitValues.get(columnSplitType);
      AnalysisSplitValue columnSplitValue = columnTypeValues.get(columnValueIndex);

      IsCondition splitCondition =
          getBudgetSplitCondition(source, columnSplitType, columnSplitValue);
      MonthRange splitRange = getBudgetSplitRange(range, columnSplitType, columnSplitValue);

      Collection<AnalysisValue> rowValues = computeBudgetSplitVector(columnId, rowId, false, null,
          source, SqlUtils.and(condition, splitCondition), splitRange, turnoverOrBalance,
          rowSplitTypes, 0, rowSplitValues, 0);

      if (!rowValues.isEmpty()) {
        for (AnalysisValue av : rowValues) {
          av.setColumnParentValueIndex(columnParentValueIndex);
          av.setColumnSplitTypeIndex(columnTypeIndex);
          av.setColumnSplitValueIndex(columnValueIndex);

          values.add(av);
        }
      }

      if (columnTypeIndex < columnSplitTypes.size() - 1) {
        values.addAll(computeBudgetSplitMatrix(columnId, rowId, columnValueIndex,
            source, condition, range, turnoverOrBalance,
            columnSplitTypes, columnTypeIndex + 1, columnSplitValues, 0,
            rowSplitTypes, rowSplitValues));
      }

      if (columnValueIndex < columnTypeValues.size() - 1) {
        values.addAll(computeBudgetSplitMatrix(columnId, rowId, columnParentValueIndex,
            source, condition, range, turnoverOrBalance,
            columnSplitTypes, columnTypeIndex, columnSplitValues, columnValueIndex + 1,
            rowSplitTypes, rowSplitValues));
      }
    }

    return values;
  }

  private static IsCondition getBudgetSplitCondition(String source,
      AnalysisSplitType splitType, AnalysisSplitValue splitValue) {

    if (splitType == null || splitValue == null || BeeUtils.isEmpty(splitType.getBudgetColumn())) {
      return null;

    } else if (DataUtils.isId(splitValue.getId())) {
      return SqlUtils.equals(source, splitType.getBudgetColumn(), splitValue.getId());

    } else {
      return SqlUtils.isNull(source, splitType.getBudgetColumn());
    }
  }

  private static MonthRange getBudgetSplitRange(MonthRange parentRange,
      AnalysisSplitType splitType, AnalysisSplitValue splitValue) {

    if (parentRange == null || splitType == null || splitValue == null) {
      return parentRange;

    } else {
      MonthRange splitRange = splitType.getMonthRange(splitValue);

      if (splitRange == null) {
        return parentRange;
      } else {
        return splitRange.intersection(parentRange);
      }
    }
  }

  private Double getBudgetValue(String source, IsCondition condition, MonthRange range,
      TurnoverOrBalance turnoverOrBalance) {

    Double result = null;
    if (range == null) {
      return result;
    }

    String yearColumn = COL_BUDGET_ENTRY_YEAR;

    YearMonth lower = range.getMinMonth();
    YearMonth upper = range.getMaxMonth();

    IsCondition yearCondition;
    if (turnoverOrBalance == TurnoverOrBalance.OPENING_BALANCE) {
      yearCondition = SqlUtils.equals(source, yearColumn, lower.getYear());

    } else if (turnoverOrBalance == TurnoverOrBalance.CLOSING_BALANCE) {
      yearCondition = SqlUtils.equals(source, yearColumn, upper.getYear());

    } else {
      yearCondition = SqlUtils.and(
          SqlUtils.moreEqual(source, yearColumn, lower.getYear()),
          SqlUtils.lessEqual(source, yearColumn, upper.getYear()));
    }

    SqlSelect query = new SqlSelect()
        .addFields(source, COL_BUDGET_ENTRY_YEAR)
        .addFields(source, COL_BUDGET_ENTRY_VALUES)
        .addFrom(source)
        .setWhere(SqlUtils.and(condition, yearCondition));

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return result;
    }

    boolean match;

    for (SimpleRow row : data) {
      int year = row.getInt(yearColumn);

      for (int month = 1; month <= 12; month++) {
        Double value = row.getDouble(colBudgetEntryValue(month));

        if (BeeUtils.isDouble(value)) {
          YearMonth ym = new YearMonth(year, month);

          if (turnoverOrBalance == TurnoverOrBalance.OPENING_BALANCE) {
            match = ym.equals(lower);
          } else if (turnoverOrBalance == TurnoverOrBalance.CLOSING_BALANCE) {
            match = ym.equals(upper);
          } else {
            match = range.contains(ym);
          }

          if (match) {
            if (result == null) {
              result = value;
            } else {
              result += value;
            }
          }
        }
      }
    }

    return result;
  }
}

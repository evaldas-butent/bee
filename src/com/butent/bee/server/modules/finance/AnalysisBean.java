package com.butent.bee.server.modules.finance;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
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
import java.util.List;
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
    BeeView finView = sys.getView(VIEW_FINANCIAL_RECORDS);
    Long userId = usr.getCurrentUserId();

    ensureDimensions();

    AnalysisFormData formData = getFormData(formId);
    ResponseObject response = validateFormData(formId, formData, finView, userId);
    if (response.hasMessages()) {
      return response;
    }

    AnalysisResults results = new AnalysisResults();

    Function<String, Filter> filterParser = input -> finView.parseFilter(input, userId);

    MonthRange headerRange = formData.getHeaderRange();
    Filter headerFilter = formData.getHeaderFilter(filterParser);

    for (BeeRow column : formData.getColumns()) {
      if (formData.columnIsPrimary(column)) {
        MonthRange columnRange = AnalysisUtils.intersection(headerRange,
            formData.getColumnRange(column));

        Filter columnFilter = formData.getColumnFilter(column, filterParser);

        Long columnIndicator = formData.getColumnLong(column, COL_ANALYSIS_COLUMN_INDICATOR);
        TurnoverOrBalance columnTurnoverOrBalance = formData.getColumnEnum(column,
            COL_ANALYSIS_COLUMN_TURNOVER_OR_BALANCE, TurnoverOrBalance.class);

        for (BeeRow row : formData.getRows()) {
          if (formData.rowIsPrimary(row)) {
            Long indicator;
            if (DataUtils.isId(columnIndicator)) {
              indicator = columnIndicator;
            } else {
              indicator = formData.getRowLong(row, COL_ANALYSIS_ROW_INDICATOR);
            }

            if (DataUtils.isId(indicator)) {
              TurnoverOrBalance turnoverOrBalance;
              if (columnTurnoverOrBalance == null) {
                turnoverOrBalance = formData.getRowEnum(row,
                    COL_ANALYSIS_ROW_TURNOVER_OR_BALANCE, TurnoverOrBalance.class);
              } else {
                turnoverOrBalance = columnTurnoverOrBalance;
              }

              MonthRange range = AnalysisUtils.intersection(columnRange,
                  formData.getRowRange(row));

              Filter filter = Filter.and(headerFilter, columnFilter,
                  formData.getRowFilter(row, filterParser));

              double value = getActualValue(indicator, turnoverOrBalance, range, filter,
                  finView, userId);
              results.add(AnalysisValue.of(column.getId(), row.getId(), value));
            }
          }
        }
      }
    }

    if (!results.isEmpty()) {
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

  private Filter getIndicatorFilter(long indicator, BeeView sourceView, Long userId) {
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
          COL_INDICATOR_FILTER_EMPLOYEE, COL_INDICATOR_FILTER_EXTRA, sourceView, userId);

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
      BeeView sourceView, Long userId) {

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

    if (row.hasColumn(extraFilterColumn) && sourceView != null) {
      String extraFilter = row.getValue(extraFilterColumn);

      if (!BeeUtils.isEmpty(extraFilter)) {
        Filter extra = sourceView.parseFilter(extraFilter, userId);
        if (extra != null) {
          filter.add(extra);
        }
      }
    }

    return AnalysisUtils.normalize(filter);
  }

  private List<AnalysisSplitValue> getSplitValues(long indicator, Filter parentFilter,
      AnalysisSplitType type, BeeView sourceView, Long userId) {

    List<AnalysisSplitValue> result = new ArrayList<>();

    CompoundFilter filter = Filter.and();
    if (parentFilter != null) {
      filter.add(parentFilter);
    }

    Filter indicatorFilter = getIndicatorFilter(indicator, sourceView, userId);
    if (indicatorFilter != null) {
      filter.add(indicatorFilter);
    }

    Collection<String> indicatorAccountCodes = getIndicatorAccountCodes(indicator);
    if (BeeUtils.isEmpty(indicatorAccountCodes)) {
      return result;
    }

    CompoundFilter accountFilter = Filter.or();

    for (String code : indicatorAccountCodes) {
      accountFilter.add(Filter.startsWith(ALS_DEBIT_CODE, code));
      accountFilter.add(Filter.startsWith(ALS_CREDIT_CODE, code));
    }

    if (!accountFilter.isEmpty()) {
      filter.add(accountFilter);
    }

    String indicatorSourceColumn = getIndicatorSourceColumn(indicator);
    if (!BeeUtils.isEmpty(indicatorSourceColumn)) {
      filter.add(Filter.nonZero(indicatorSourceColumn));
    }

    String finColumn = type.getFinColumn();
    if (!sourceView.hasColumn(finColumn)) {
      return result;
    }

    SqlSelect sourceQuery = sourceView.getQuery(userId, filter, null,
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

  private double getActualValue(long indicator, TurnoverOrBalance parentTurnoverOrBalance,
      MonthRange range, Filter parentFilter, BeeView finView, Long userId) {

    double value = BeeConst.DOUBLE_ZERO;

    Collection<String> accountCodes = getIndicatorAccountCodes(indicator);
    if (BeeUtils.isEmpty(accountCodes)) {
      return value;
    }

    TurnoverOrBalance turnoverOrBalance;
    if (parentTurnoverOrBalance == null) {
      turnoverOrBalance = getIndicatorTurnoverOrBalance(indicator);
    } else {
      turnoverOrBalance = parentTurnoverOrBalance;
    }

    NormalBalance normalBalance = getIndicatorNormalBalance(indicator);

    CompoundFilter plusFilter = CompoundFilter.or();
    CompoundFilter minusFilter = CompoundFilter.or();

    for (String code : accountCodes) {
      plusFilter.add(turnoverOrBalance.getPlusFilter(ALS_DEBIT_CODE, ALS_CREDIT_CODE, code,
          normalBalance));

      minusFilter.add(turnoverOrBalance.getMinusFilter(ALS_DEBIT_CODE, ALS_CREDIT_CODE, code,
          normalBalance));
    }

    Filter indicatorFilter = getIndicatorFilter(indicator, finView, userId);

    Filter rangeFilter = (range == null)
        ? null : turnoverOrBalance.getRangeFilter(COL_FIN_DATE, range);

    String sourceColumn = getIndicatorSourceColumn(indicator);
    Filter sourceFilter = Filter.nonZero(sourceColumn);

    Filter filter = Filter.and(parentFilter, indicatorFilter, rangeFilter, sourceFilter);

    if (!plusFilter.isEmpty()) {
      Double plus = getSum(finView, userId, Filter.and(filter, plusFilter), sourceColumn);
      if (BeeUtils.nonZero(plus)) {
        value += plus;
      }
    }

    if (!minusFilter.isEmpty()) {
      Double minus = getSum(finView, userId, Filter.and(filter, minusFilter), sourceColumn);
      if (BeeUtils.nonZero(minus)) {
        value -= minus;
      }
    }

    return value;
  }

  private Double getSum(BeeView view, Long userId, Filter filter, String column) {
    SqlSelect query = view.getQuery(userId, filter, null, Collections.singleton(column));
    String alias = SqlUtils.uniqueName();

    return qs.getDouble(new SqlSelect().addSum(alias, column).addFrom(query, alias));
  }
}

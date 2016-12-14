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
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplitType;
import com.butent.bee.shared.modules.finance.analysis.AnalysisSplitValue;
import com.butent.bee.shared.modules.finance.analysis.IndicatorSource;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.time.YearQuarter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    AnalysisFormData formData = getFormData(formId);
    ResponseObject response = validateFormData(formId, formData);

    if (response.hasMessages()) {
      return response;
    }
    return response;
  }

  public ResponseObject verifyForm(long formId) {
    AnalysisFormData formData = getFormData(formId);
    return validateFormData(formId, formData);
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

  private ResponseObject validateFormData(long formId, AnalysisFormData formData) {
    if (formData == null) {
      return ResponseObject.error("form", formId, "not found");
    }

    ResponseObject response = ResponseObject.emptyResponse();

    BeeView finView = sys.getView(VIEW_FINANCIAL_RECORDS);
    Long userId = usr.getCurrentUserId();

    Predicate<String> extraFilterValidator = input ->
        BeeUtils.isEmpty(input) || finView.parseFilter(input, userId) != null;

    ensureDimensions();

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

    if (include.isEmpty() && exclude.isEmpty()) {
      return null;

    } else if (exclude.isEmpty()) {
      return include;

    } else if (include.isEmpty()) {
      return Filter.isNot(exclude);

    } else {
      return Filter.and(include, Filter.isNot(exclude));
    }
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

    return filter.isEmpty() ? null : filter;
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

    SimpleRowSet indicatorAccounts = getIndicatorAccounts(indicator);
    if (DataUtils.isEmpty(indicatorAccounts)) {
      if (indicatorFilter == null) {
        return result;
      }

    } else {
      CompoundFilter accountFilter = Filter.or();

      for (SimpleRow row : indicatorAccounts) {
        String debitCode = BeeUtils.trimRight(row.getValue(ALS_DEBIT_CODE));
        String creditCode = BeeUtils.trimRight(row.getValue(ALS_CREDIT_CODE));

        if (!debitCode.isEmpty() && !creditCode.isEmpty()) {
          accountFilter.add(Filter.and(Filter.startsWith(ALS_DEBIT_CODE, debitCode),
              Filter.startsWith(ALS_CREDIT_CODE, creditCode)));

        } else if (!debitCode.isEmpty()) {
          accountFilter.add(Filter.startsWith(ALS_DEBIT_CODE, debitCode));

        } else if (!creditCode.isEmpty()) {
          accountFilter.add(Filter.startsWith(ALS_CREDIT_CODE, creditCode));
        }
      }

      if (!accountFilter.isEmpty()) {
        filter.add(accountFilter);
      }
    }

    String indicatorSourceColumn = getIndicatorSourceColumn(indicator);
    if (!BeeUtils.isEmpty(indicatorSourceColumn)) {
      filter.add(Filter.nonZero(indicatorSourceColumn));
    }

    String finColumn = type.getFinColumnn();
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

  private SimpleRowSet getIndicatorAccounts(long indicator) {
    String debitAlias = SqlUtils.uniqueName();
    String creditAlias = SqlUtils.uniqueName();

    SqlSelect query = new SqlSelect().setDistinctMode(true)
        .addFields(TBL_INDICATOR_ACCOUNTS,
            COL_INDICATOR_ACCOUNT_DEBIT, COL_INDICATOR_ACCOUNT_CREDIT, COL_INDICATOR_ACCOUNT_PLUS)
        .addField(debitAlias, COL_ACCOUNT_CODE, ALS_DEBIT_CODE)
        .addField(creditAlias, COL_ACCOUNT_CODE, ALS_CREDIT_CODE)
        .addFrom(TBL_INDICATOR_ACCOUNTS)
        .addFromLeft(TBL_CHART_OF_ACCOUNTS, debitAlias, sys.joinTables(TBL_CHART_OF_ACCOUNTS,
            TBL_INDICATOR_ACCOUNTS, COL_INDICATOR_ACCOUNT_DEBIT))
        .addFromLeft(TBL_CHART_OF_ACCOUNTS, creditAlias, sys.joinTables(TBL_CHART_OF_ACCOUNTS,
            TBL_INDICATOR_ACCOUNTS, COL_INDICATOR_ACCOUNT_CREDIT))
        .setWhere(SqlUtils.and(
            SqlUtils.equals(TBL_INDICATOR_ACCOUNTS, COL_FIN_INDICATOR, indicator),
            SqlUtils.isDifferent(TBL_INDICATOR_ACCOUNTS,
                COL_INDICATOR_ACCOUNT_DEBIT, COL_INDICATOR_ACCOUNT_CREDIT)));

    return qs.getData(query);
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
}

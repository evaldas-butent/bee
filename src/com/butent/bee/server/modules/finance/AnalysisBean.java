package com.butent.bee.server.modules.finance;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.administration.AdministrationModuleBean;
import com.butent.bee.server.modules.administration.ExchangeUtils;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.ScriptUtils;
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
import com.butent.bee.shared.modules.administration.AdministrationConstants;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.script.Bindings;
import javax.script.ScriptEngine;

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
  @EJB
  AdministrationModuleBean adm;

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

    ScriptEngine engine;
    if (formData.hasScripting()) {
      engine = ScriptUtils.getEngine();
      if (engine == null) {
        response.addError("script engine not available");
        return response;
      }

    } else {
      engine = null;
    }

    long computeStart = System.currentTimeMillis();
    AnalysisResults results = new AnalysisResults(
        formData.getHeaderIndexes(), formData.getHeader(),
        formData.getColumnIndexes(), formData.getColumns(),
        formData.getRowIndexes(), formData.getRows());

    LongSummaryStatistics actualValueStats = new LongSummaryStatistics();

    formData.getColumns().forEach(column ->
        results.addColumnSplitTypes(column.getId(), formData.getColumnSplits(column)));
    formData.getRows().forEach(row ->
        results.addRowSplitTypes(row.getId(), formData.getRowSplits(row)));

    Function<String, Filter> filterParser = input -> finView.parseFilter(input, userId);

    MonthRange headerRange = formData.getHeaderRange();

    Filter headerFilter = formData.getHeaderFilter(COL_FIN_EMPLOYEE, filterParser);
    AnalysisFilter headerAnalysisFilter = formData.getHeaderAnalysisFilter();

    Long headerBudgetType = formData.getHeaderLong(COL_ANALYSIS_HEADER_BUDGET_TYPE);

    Long currency = formData.getHeaderLong(COL_ANALYSIS_HEADER_CURRENCY);
    if (!DataUtils.isId(currency)) {
      currency = getDefaultCurrency();
    }

    for (BeeRow column : formData.getColumns()) {
      MonthRange columnRange = AnalysisUtils.intersection(headerRange,
          formData.getColumnRange(column));

      if (formData.columnIsPrimary(column) && columnRange != null) {
        Filter columnFilter = formData.getColumnFilter(column, COL_FIN_EMPLOYEE, filterParser);
        AnalysisFilter columnAnalysisFilter = formData.getColumnAnalysisFilter(column);

        Long columnIndicator = formData.getColumnLong(column, COL_ANALYSIS_COLUMN_INDICATOR);
        TurnoverOrBalance columnTurnoverOrBalance = formData.getColumnEnum(column,
            COL_ANALYSIS_COLUMN_TURNOVER_OR_BALANCE, TurnoverOrBalance.class);

        List<AnalysisCellType> columnCellTypes = formData.getColumnCellTypes(column);
        Long columnBudgetType = formData.getColumnLong(column, COL_ANALYSIS_COLUMN_BUDGET_TYPE);

        List<AnalysisSplitType> columnSplitTypes = results.getColumnSplitTypes(column.getId());

        Integer columnScale = formData.getColumnScale(column);
        String columnScript = formData.getColumnScript(column);

        for (BeeRow row : formData.getRows()) {
          if (formData.rowIsPrimary(row)) {
            Long rowIndicator = formData.getRowLong(row, COL_ANALYSIS_ROW_INDICATOR);
            Long indicator = DataUtils.isId(columnIndicator) ? columnIndicator : rowIndicator;

            MonthRange rowRange = formData.getRowRange(row);
            MonthRange range = AnalysisUtils.intersection(columnRange, rowRange);

            if (DataUtils.isId(indicator) && range != null) {
              List<AnalysisCellType> rowCellTypes = formData.getRowCellTypes(row);

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

              Filter rowFilter = formData.getRowFilter(row, COL_FIN_EMPLOYEE, filterParser);
              Filter parentFilter = Filter.and(headerFilter, columnFilter, rowFilter);

              AnalysisFilter rowAnalysisFilter = formData.getRowAnalysisFilter(row);

              IndicatorSource indicatorSource = getIndicatorSource(indicator);
              String sourceColumn = indicatorSource.getSourceColumn();
              Long sourceCurrency = indicatorSource.hasCurrency() ? currency : null;

              Filter valueFilter = getActualValueFilter(indicator, parentFilter, sourceColumn,
                  finView, userId);

              Filter plusFilter = accountFilters.getA();
              Filter minusFilter = accountFilters.getB();

              List<AnalysisSplitType> rowSplitTypes = results.getRowSplitTypes(row.getId());

              Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues =
                  new EnumMap<>(AnalysisSplitType.class);
              Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues =
                  new EnumMap<>(AnalysisSplitType.class);

              String budgetCursor = null;
              IsCondition budgetCondition = null;

              if (DataUtils.isId(budgetType)
                  && (AnalysisCellType.needsBudget(columnCellTypes)
                  || AnalysisCellType.needsBudget(rowCellTypes))) {

                budgetCursor = getBudgetCursor(indicator, indicatorTurnoverOrBalance,
                    budgetType, turnoverOrBalance);

                if (qs.isEmpty(budgetCursor)) {
                  if (!BeeUtils.isEmpty(budgetCursor)) {
                    qs.sqlDropTemp(budgetCursor);
                  }
                  budgetCursor = null;

                } else {
                  budgetCondition = getBudgetCondition(budgetCursor, COL_BUDGET_ENTRY_EMPLOYEE,
                      headerAnalysisFilter, columnAnalysisFilter, rowAnalysisFilter);
                }
              }

              if (!BeeUtils.isEmpty(columnSplitTypes)) {
                for (AnalysisSplitType type : columnSplitTypes) {
                  List<AnalysisSplitValue> splitValues = getSplitValues(type,
                      valueFilter, plusFilter, minusFilter, finView, userId,
                      budgetCursor, budgetCondition, range, turnoverOrBalance);

                  if (!splitValues.isEmpty()) {
                    columnSplitValues.put(type, splitValues);
                    results.addColumnSplitValues(column.getId(), type, splitValues);
                  }
                }
              }

              if (!BeeUtils.isEmpty(rowSplitTypes)) {
                for (AnalysisSplitType type : rowSplitTypes) {
                  List<AnalysisSplitValue> splitValues = getSplitValues(type,
                      valueFilter, plusFilter, minusFilter, finView, userId,
                      budgetCursor, budgetCondition, range, turnoverOrBalance);

                  if (!splitValues.isEmpty()) {
                    rowSplitValues.put(type, splitValues);
                    results.addRowSplitValues(row.getId(), type, splitValues);
                  }
                }
              }

              boolean needsActual = !accountFilters.isNull()
                  && (AnalysisCellType.needsActual(columnCellTypes)
                  || AnalysisCellType.needsActual(rowCellTypes));

              if (needsActual) {
                results.addValues(computeActualValues(column.getId(), row.getId(),
                    valueFilter, plusFilter, minusFilter, range, turnoverOrBalance,
                    columnSplitTypes, columnSplitValues, rowSplitTypes, rowSplitValues,
                    indicatorSource, finView, userId, sourceCurrency, actualValueStats));
              }

              boolean needsBudget = budgetCursor != null;

              if (needsBudget) {
                results.mergeValues(computeBudgetValues(column.getId(), row.getId(),
                    budgetCursor, budgetCondition, range, turnoverOrBalance,
                    columnSplitTypes, columnSplitValues, rowSplitTypes, rowSplitValues,
                    sourceCurrency));

                qs.sqlDropTemp(budgetCursor);
              }

              if (results.containsValues(column.getId(), row.getId())) {
                String script;

                if (engine == null) {
                  script = null;

                } else if (AnalysisScripting.isScriptPrimary(columnScript)) {
                  script = columnScript;

                } else {
                  script = formData.getRowScript(row);
                  if (!AnalysisScripting.isScriptPrimary(script)) {
                    script = null;
                  }
                }

                if (script != null) {
                  Bindings actualBindings;
                  Bindings budgetBindings;

                  if (needsActual) {
                    actualBindings = engine.createBindings();
                    actualBindings.put(AnalysisScripting.VAR_IS_BUDGET, false);
                  } else {
                    actualBindings = null;
                  }

                  if (needsBudget) {
                    budgetBindings = engine.createBindings();
                    budgetBindings.put(AnalysisScripting.VAR_IS_BUDGET, true);
                  } else {
                    budgetBindings = null;
                  }

                  if (actualBindings != null || budgetBindings != null) {
                    for (AnalysisValue av : results.getValues(column.getId(), row.getId())) {
                      if (actualBindings != null && av.hasActualValue()) {
                        actualBindings.put(AnalysisScripting.VAR_CURRENT_VALUE,
                            av.getActualNumber());
                        Double value = ScriptUtils.evalToDouble(engine, actualBindings,
                            script, response);

                        if (BeeUtils.isDouble(value)) {
                          av.setActualValue(value);
                        }
                      }

                      if (budgetBindings != null && av.hasBudgetValue()) {
                        budgetBindings.put(AnalysisScripting.VAR_CURRENT_VALUE,
                            av.getBudgetNumber());
                        Double value = ScriptUtils.evalToDouble(engine, budgetBindings,
                            script, response);

                        if (BeeUtils.isDouble(value)) {
                          av.setBudgetValue(value);
                        }
                      }

                      if (response.hasErrors()) {
                        break;
                      }
                    }
                  }
                }

                if (!response.hasErrors()) {
                  Integer scale = columnScale;
                  if (!AnalysisUtils.isValidScale(scale)) {
                    scale = formData.getRowScale(row);
                  }
                  if (!AnalysisUtils.isValidScale(scale)) {
                    scale = getIndicatorScale(indicator);
                  }

                  if (AnalysisUtils.isValidScale(scale)) {
                    for (AnalysisValue av : results.getValues(column.getId(), row.getId())) {
                      av.round(scale);
                    }
                  }
                }
              }
            }
          }

          if (response.hasErrors()) {
            break;
          }
        }
      }

      if (response.hasErrors()) {
        break;
      }
    }

    if (!response.hasErrors() && engine != null) {
      List<BeeRow> secondaryRows = formData.getSecondaryRows(response::addError);

      if (!secondaryRows.isEmpty() && !response.hasErrors()) {
        for (BeeRow row : secondaryRows) {
          MonthRange rowRange = AnalysisUtils.intersection(headerRange, formData.getRowRange(row));

          if (rowRange != null) {
            String script = formData.getRowScript(row);
            Map<Long, String> variables = formData.getRowVariables(row);

            Integer rowScale = formData.getRowScale(row);

            List<AnalysisSplitType> rowSplitTypes = results.getRowSplitTypes(row.getId());
            List<AnalysisCellType> rowCellTypes = formData.getRowCellTypes(row);

            AnalysisFilter rowAnalysisFilter = formData.getRowAnalysisFilter(row);

            Predicate<AnalysisValue> analysisValuePredicate;
            if (rowAnalysisFilter == null) {
              analysisValuePredicate = null;
            } else {
              analysisValuePredicate = rowAnalysisFilter::matches;
            }

            for (BeeRow column : formData.getColumns()) {
              MonthRange range = AnalysisUtils.intersection(rowRange,
                  formData.getColumnRange(column));

              if (formData.columnIsPrimary(column) && range != null) {
                Integer columnScale = formData.getColumnScale(column);

                List<AnalysisSplitType> columnSplitTypes =
                    results.getColumnSplitTypes(column.getId());
                List<AnalysisCellType> columnCellTypes = formData.getColumnCellTypes(column);

                boolean needsActual = AnalysisCellType.needsActual(rowCellTypes)
                    || AnalysisCellType.needsActual(columnCellTypes);
                boolean needsBudget = AnalysisCellType.needsBudget(rowCellTypes)
                    || AnalysisCellType.needsBudget(columnCellTypes);

                Integer scale = AnalysisUtils.getScale(rowScale, columnScale);

                List<AnalysisValue> calculatedValues = new ArrayList<>();

                if (BeeUtils.isEmpty(variables)) {
                  String actualValue;
                  if (needsActual) {
                    actualValue = ScriptUtils.evalToString(engine,
                        AnalysisScripting.createActualBindings(engine), script, response);
                  } else {
                    actualValue = null;
                  }

                  String budgetValue;
                  if (needsBudget) {
                    budgetValue = ScriptUtils.evalToString(engine,
                        AnalysisScripting.createBudgetBindings(engine), script, response);
                  } else {
                    budgetValue = null;
                  }

                  if (BeeUtils.anyNotEmpty(actualValue, budgetValue)) {
                    calculatedValues.add(AnalysisValue.of(column.getId(), row.getId(),
                        actualValue, budgetValue));
                  }

                } else {
                  Multimap<Long, AnalysisValue> inputValues = results.getColumnValuesByRow(
                      column.getId(), variables.keySet(), analysisValuePredicate);

                  if (inputValues != null && !inputValues.isEmpty()) {
                    Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues =
                        new HashMap<>();

                    if (!BeeUtils.isEmpty(rowSplitTypes)) {
                      for (long inputRowId : inputValues.keySet()) {
                        Map<AnalysisSplitType, List<AnalysisSplitValue>> map =
                            results.getRowSplitValues(inputRowId);

                        if (!BeeUtils.isEmpty(map)) {
                          for (AnalysisSplitType splitType : rowSplitTypes) {
                            List<AnalysisSplitValue> splitValues = map.get(splitType);

                            if (!BeeUtils.isEmpty(splitValues)) {
                              AnalysisSplitValue.putSplitValues(map, splitType, splitValues);
                            }
                          }
                        }
                      }

                      if (!rowSplitValues.isEmpty()) {
                        rowSplitValues.forEach((splitType, splitValues) ->
                            results.addRowSplitValues(row.getId(), splitType, splitValues));
                      }
                    }

                    Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues =
                        results.getColumnSplitValues(column.getId());

                    Multimap<String, AnalysisValue> input = ArrayListMultimap.create();
                    for (long inputRowId : inputValues.keySet()) {
                      String variable = variables.get(inputRowId);
                      if (!BeeUtils.isEmpty(variable)) {
                        input.putAll(variable, inputValues.get(inputRowId));
                      }
                    }

                    calculatedValues.addAll(
                        AnalysisScripting.calculateValues(engine, script,
                            column.getId(), row.getId(), variables.values(), input,
                            columnSplitTypes, columnSplitValues, rowSplitTypes, rowSplitValues,
                            needsActual, needsBudget, response));

                  }
                }

                if (!calculatedValues.isEmpty() && !response.hasErrors()) {
                  results.addValues(calculatedValues);
                }
              }

              if (response.hasErrors()) {
                break;
              }
            }

            if (response.hasErrors()) {
              break;
            }
          }
        }
      }
    }

    if (!response.hasErrors() && !results.isEmpty()) {
      results.setInitStart(initStart);
      results.setValidateStart(validateStart);
      results.setComputeStart(computeStart);
      results.setComputeEnd(System.currentTimeMillis());

      if (actualValueStats.getCount() > 0) {
        results.setQueryCount(actualValueStats.getCount());
        results.setQueryDuration(actualValueStats.getSum());
      }

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

  public ResponseObject saveResults(RequestInfo reqInfo) {
    Long analysisHeader = reqInfo.getParameterLong(COL_ANALYSIS_HEADER);
    if (!DataUtils.isId(analysisHeader)) {
      return ResponseObject.parameterNotFound(reqInfo.getSubService(), COL_ANALYSIS_HEADER);
    }

    Long time = reqInfo.getParameterLong(COL_ANALYSIS_RESULT_DATE);
    if (time == null) {
      return ResponseObject.parameterNotFound(reqInfo.getSubService(), COL_ANALYSIS_RESULT_DATE);
    }

    String caption = reqInfo.getParameter(COL_ANALYSIS_RESULT_CAPTION);

    String results = reqInfo.getParameter(COL_ANALYSIS_RESULTS);
    if (results == null || results.isEmpty()) {
      return ResponseObject.parameterNotFound(reqInfo.getSubService(), COL_ANALYSIS_RESULTS);
    }

    SqlInsert insert = new SqlInsert(TBL_ANALYSIS_RESULTS)
        .addConstant(COL_ANALYSIS_HEADER, analysisHeader)
        .addConstant(COL_ANALYSIS_RESULT_DATE, time)
        .addConstant(COL_ANALYSIS_RESULT_SIZE, results.length())
        .addConstant(COL_ANALYSIS_RESULTS, results);

    if (!BeeUtils.isEmpty(caption)) {
      insert.addConstant(COL_ANALYSIS_RESULT_CAPTION, caption);
    }

    return qs.insertDataWithResponse(insert);
  }

  public ResponseObject getResults(long resultId) {
    SimpleRow row = qs.getRow(TBL_ANALYSIS_RESULTS, resultId);
    if (row == null) {
      return ResponseObject.error(TBL_ANALYSIS_RESULTS, resultId, "not found");
    }

    Map<String, String> map = new HashMap<>();

    String caption = row.getValue(COL_ANALYSIS_RESULT_CAPTION);
    if (!BeeUtils.isEmpty(caption)) {
      map.put(COL_ANALYSIS_RESULT_CAPTION, caption);
    }

    map.put(COL_ANALYSIS_RESULTS, row.getValue(COL_ANALYSIS_RESULTS));

    return ResponseObject.response(map);
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
      BeeView finView, Long userId, String budgetCursor, IsCondition budgetCondition,
      MonthRange range, TurnoverOrBalance turnoverOrBalance) {

    List<AnalysisSplitValue> result = new ArrayList<>();

    String finColumn = type.getFinColumn();
    if (!finView.hasColumn(finColumn)) {
      return result;
    }

    CompoundFilter filter = Filter.and();
    filter.add(parentFilter, Filter.or(plusFilter, minusFilter));

    if (!type.isPeriod() && TurnoverOrBalance.isBalance(turnoverOrBalance)) {
      filter.add(TurnoverOrBalance.CLOSING_BALANCE.getRangeFilter(COL_FIN_DATE, range));
    } else {
      filter.add(AnalysisUtils.getFilter(COL_FIN_DATE, range));
    }

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

    String finTmp = qs.sqlCreateTemp(query);
    if (qs.isEmpty(finTmp)) {
      qs.sqlDropTemp(finTmp);
      finTmp = null;
    }

    boolean hasBudget = !BeeUtils.isEmpty(budgetCursor);
    IsCondition budgetYearCondition = null;
    String budgetColumn = type.getBudgetColumn();

    if (hasBudget) {
      budgetYearCondition = getBudgetYearCondition(budgetCursor, COL_BUDGET_ENTRY_YEAR,
          range, turnoverOrBalance);
      hasBudget = qs.sqlExists(budgetCursor, SqlUtils.and(budgetCondition, budgetYearCondition));
    }

    if (finTmp != null || hasBudget) {
      if (type.isPeriod()) {
        if (finTmp != null) {
          qs.setYearMonth(finTmp, finColumn, BeeConst.YEAR, BeeConst.MONTH);
        }

        Collection<YearMonth> budgetMonths;
        if (hasBudget) {
          budgetMonths = getBudgetMonths(budgetCursor, budgetCondition, range, turnoverOrBalance);
        } else {
          budgetMonths = Collections.emptySet();
        }

        switch (type) {
          case MONTH:
            Set<YearMonth> months = new HashSet<>();

            if (finTmp != null) {
              months.addAll(qs.getData(new SqlSelect().setDistinctMode(true)
                  .addFields(finTmp, BeeConst.YEAR, BeeConst.MONTH)
                  .addFrom(finTmp)
                  .setWhere(SqlUtils.positive(finTmp, BeeConst.YEAR, BeeConst.MONTH)))
                  .getRows().stream()
                  .map(row -> YearMonth.parse(row[0], row[1]))
                  .collect(Collectors.toSet()));
            }

            if (!BeeUtils.isEmpty(budgetMonths)) {
              months.addAll(budgetMonths);
            }

            if (!months.isEmpty()) {
              months.stream()
                  .sorted()
                  .forEach(ym -> result.add(AnalysisSplitValue.of(ym)));
            }
            break;

          case QUARTER:
            Set<YearQuarter> quarters = new HashSet<>();

            if (finTmp != null) {
              quarters.addAll(qs.getData(new SqlSelect().setDistinctMode(true)
                  .addFields(finTmp, BeeConst.YEAR, BeeConst.MONTH)
                  .addFrom(finTmp)
                  .setWhere(SqlUtils.positive(finTmp, BeeConst.YEAR, BeeConst.MONTH)))
                  .getRows().stream()
                  .map(row -> YearQuarter.of(YearMonth.parse(row[0], row[1])))
                  .collect(Collectors.toSet()));
            }

            if (!BeeUtils.isEmpty(budgetMonths)) {
              quarters.addAll(budgetMonths.stream()
                  .map(YearQuarter::of)
                  .collect(Collectors.toSet()));
            }

            if (!quarters.isEmpty()) {
              quarters.stream()
                  .sorted()
                  .forEach(yq -> result.add(AnalysisSplitValue.of(yq)));
            }
            break;

          case YEAR:
            Set<Integer> years = new HashSet<>();

            if (finTmp != null) {
              years.addAll(qs.getIntSet(new SqlSelect().setDistinctMode(true)
                  .addFields(finTmp, BeeConst.YEAR)
                  .addFrom(finTmp)));
            }

            if (!BeeUtils.isEmpty(budgetMonths)) {
              years.addAll(budgetMonths.stream()
                  .map(YearMonth::getYear)
                  .collect(Collectors.toSet()));
            }

            if (!years.isEmpty()) {
              years.stream()
                  .filter(TimeUtils::isYear)
                  .sorted()
                  .forEach(year -> result.add(AnalysisSplitValue.of(year)));
            }
            break;

          default:
            logger.severe(type, "split not implemented");
        }

      } else if (type.isDimension()) {
        Set<Long> ids = new HashSet<>();

        if (finTmp != null) {
          ids.addAll(qs.getLongSet(new SqlSelect()
              .addFields(finTmp, finColumn)
              .addFrom(finTmp)));
        }

        if (hasBudget && !BeeUtils.isEmpty(budgetColumn)) {
          ids.addAll(qs.getLongSet(new SqlSelect()
              .addFields(budgetCursor, budgetColumn)
              .addFrom(budgetCursor)
              .setWhere(SqlUtils.and(budgetCondition, budgetYearCondition,
                  SqlUtils.notNull(budgetCursor, budgetColumn)))));
        }

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
        Set<Long> ids = new HashSet<>();

        if (finTmp != null) {
          qs.getLongSet(new SqlSelect()
              .addFields(finTmp, finColumn)
              .addFrom(finTmp));
        }

        if (hasBudget && !BeeUtils.isEmpty(budgetColumn)) {
          ids.addAll(qs.getLongSet(new SqlSelect()
              .addFields(budgetCursor, budgetColumn)
              .addFrom(budgetCursor)
              .setWhere(SqlUtils.and(budgetCondition, budgetYearCondition,
                  SqlUtils.notNull(budgetCursor, budgetColumn)))));
        }

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

    if (finTmp != null) {
      qs.sqlDropTemp(finTmp);
    }

    return result;
  }

  private static MonthRange getSplitRange(MonthRange parentRange,
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

  private IndicatorSource getIndicatorSource(long indicator) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_FINANCIAL_INDICATORS, COL_FIN_INDICATOR_SOURCE)
        .addFrom(TBL_FINANCIAL_INDICATORS)
        .setWhere(sys.idEquals(TBL_FINANCIAL_INDICATORS, indicator));

    IndicatorSource indicatorSource = qs.getEnum(query, IndicatorSource.class);
    if (indicatorSource == null) {
      indicatorSource = IndicatorSource.DEFAULT;
    }

    return indicatorSource;
  }

  private Integer getIndicatorScale(long indicator) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_FINANCIAL_INDICATORS, COL_FIN_INDICATOR_SCALE)
        .addFrom(TBL_FINANCIAL_INDICATORS)
        .setWhere(sys.idEquals(TBL_FINANCIAL_INDICATORS, indicator));

    return qs.getInt(query);
  }

  private static Map<AnalysisSplitType, AnalysisSplitValue> getRootSplit() {
    return new EnumMap<>(AnalysisSplitType.class);
  }

  private static Map<AnalysisSplitType, AnalysisSplitValue> buildSplit(
      Map<AnalysisSplitType, AnalysisSplitValue> parentSplit,
      AnalysisSplitType splitType, AnalysisSplitValue splitValue) {

    Map<AnalysisSplitType, AnalysisSplitValue> split = new EnumMap<>(AnalysisSplitType.class);

    if (!BeeUtils.isEmpty(parentSplit)) {
      split.putAll(parentSplit);
    }
    if (splitType != null && splitValue != null) {
      split.put(splitType, splitValue);
    }

    return split;
  }

  private Collection<AnalysisValue> computeActualValues(long columnId, long rowId,
      Filter filter, Filter plusFilter, Filter minusFilter,
      MonthRange range, TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> columnSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues,
      List<AnalysisSplitType> rowSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues,
      IndicatorSource source, BeeView finView, Long userId,
      Long currency, LongSummaryStatistics stats) {

    List<AnalysisValue> values = new ArrayList<>();

    boolean hasColumnSplits = !columnSplitTypes.isEmpty() && !columnSplitValues.isEmpty();
    boolean hasRowSplits = !rowSplitTypes.isEmpty() && !rowSplitValues.isEmpty();

    if (hasColumnSplits && hasRowSplits) {
      values.addAll(computeActualSplitMatrix(columnId, rowId, getRootSplit(),
          filter, plusFilter, minusFilter, range, turnoverOrBalance,
          columnSplitTypes, 0, columnSplitValues, 0,
          rowSplitTypes, rowSplitValues, source, finView, userId, currency, stats));

    } else if (hasColumnSplits) {
      values.addAll(computeActualSplitVector(columnId, rowId, true, getRootSplit(),
          filter, plusFilter, minusFilter, range, turnoverOrBalance,
          columnSplitTypes, 0, columnSplitValues, 0,
          source, finView, userId, currency, stats));

    } else if (hasRowSplits) {
      values.addAll(computeActualSplitVector(columnId, rowId, false, getRootSplit(),
          filter, plusFilter, minusFilter, range, turnoverOrBalance,
          rowSplitTypes, 0, rowSplitValues, 0,
          source, finView, userId, currency, stats));

    } else {
      Double value = getActualValue(filter, plusFilter, minusFilter, range, turnoverOrBalance,
          source, finView, userId, currency, stats);

      if (BeeUtils.nonZero(value)) {
        values.add(AnalysisValue.actual(columnId, rowId, value));
      }
    }

    return values;
  }

  private Collection<AnalysisValue> computeActualSplitVector(long columnId, long rowId,
      boolean isColumn, Map<AnalysisSplitType, AnalysisSplitValue> parentSplit,
      Filter parentFilter, Filter plusFilter, Filter minusFilter,
      MonthRange range, TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> splitTypes, int typeIndex,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> splitValues, int valueIndex,
      IndicatorSource source, BeeView finView, Long userId,
      Long currency, LongSummaryStatistics stats) {

    List<AnalysisValue> values = new ArrayList<>();
    if (range == null) {
      return values;
    }

    AnalysisSplitType splitType = splitTypes.get(typeIndex);

    if (splitValues.containsKey(splitType)) {
      List<AnalysisSplitValue> typeValues = splitValues.get(splitType);
      AnalysisSplitValue splitValue = typeValues.get(valueIndex);

      Filter splitFilter = Filter.and(parentFilter, getActualSplitFilter(splitType, splitValue));
      MonthRange splitRange = getSplitRange(range, splitType, splitValue);

      if (typeIndex < splitTypes.size() - 1) {
        Map<AnalysisSplitType, AnalysisSplitValue> split =
            buildSplit(parentSplit, splitType, splitValue);

        values.addAll(computeActualSplitVector(columnId, rowId, isColumn, split,
            splitFilter, plusFilter, minusFilter, splitRange, turnoverOrBalance,
            splitTypes, typeIndex + 1, splitValues, 0,
            source, finView, userId, currency, stats));

      } else {
        Double value = getActualValue(splitFilter, plusFilter, minusFilter,
            splitRange, turnoverOrBalance, source, finView, userId, currency, stats);

        if (BeeUtils.nonZero(value)) {
          AnalysisValue av = AnalysisValue.actual(columnId, rowId, value);

          if (isColumn) {
            av.putColumnSplit(parentSplit, splitType, splitValue);
          } else {
            av.putRowSplit(parentSplit, splitType, splitValue);
          }

          values.add(av);
        }
      }

      if (valueIndex < typeValues.size() - 1) {
        values.addAll(computeActualSplitVector(columnId, rowId, isColumn, parentSplit,
            parentFilter, plusFilter, minusFilter, range, turnoverOrBalance,
            splitTypes, typeIndex, splitValues, valueIndex + 1,
            source, finView, userId, currency, stats));
      }
    }

    return values;
  }

  private Collection<AnalysisValue> computeActualSplitMatrix(long columnId, long rowId,
      Map<AnalysisSplitType, AnalysisSplitValue> columnParentSplit,
      Filter parentFilter, Filter plusFilter, Filter minusFilter,
      MonthRange range, TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> columnSplitTypes, int columnTypeIndex,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues, int columnValueIndex,
      List<AnalysisSplitType> rowSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues,
      IndicatorSource source, BeeView finView, Long userId,
      Long currency, LongSummaryStatistics stats) {

    List<AnalysisValue> values = new ArrayList<>();
    if (range == null) {
      return values;
    }

    AnalysisSplitType columnSplitType = columnSplitTypes.get(columnTypeIndex);

    if (columnSplitValues.containsKey(columnSplitType)) {
      List<AnalysisSplitValue> columnTypeValues = columnSplitValues.get(columnSplitType);
      AnalysisSplitValue columnSplitValue = columnTypeValues.get(columnValueIndex);

      Filter splitFilter = Filter.and(parentFilter,
          getActualSplitFilter(columnSplitType, columnSplitValue));
      MonthRange splitRange = getSplitRange(range, columnSplitType, columnSplitValue);

      if (columnTypeIndex < columnSplitTypes.size() - 1) {
        Map<AnalysisSplitType, AnalysisSplitValue> columnSplit =
            buildSplit(columnParentSplit, columnSplitType, columnSplitValue);

        values.addAll(computeActualSplitMatrix(columnId, rowId, columnSplit,
            splitFilter, plusFilter, minusFilter, splitRange, turnoverOrBalance,
            columnSplitTypes, columnTypeIndex + 1, columnSplitValues, 0,
            rowSplitTypes, rowSplitValues, source, finView, userId, currency, stats));

      } else {
        Collection<AnalysisValue> rowValues = computeActualSplitVector(columnId, rowId,
            false, getRootSplit(),
            splitFilter, plusFilter, minusFilter, splitRange, turnoverOrBalance,
            rowSplitTypes, 0, rowSplitValues, 0,
            source, finView, userId, currency, stats);

        if (!rowValues.isEmpty()) {
          for (AnalysisValue av : rowValues) {
            av.putColumnSplit(columnParentSplit, columnSplitType, columnSplitValue);
            values.add(av);
          }
        }
      }

      if (columnValueIndex < columnTypeValues.size() - 1) {
        values.addAll(computeActualSplitMatrix(columnId, rowId, columnParentSplit,
            parentFilter, plusFilter, minusFilter, range, turnoverOrBalance,
            columnSplitTypes, columnTypeIndex, columnSplitValues, columnValueIndex + 1,
            rowSplitTypes, rowSplitValues, source, finView, userId, currency, stats));
      }
    }

    return values;
  }

  private Double getActualValue(Filter filter, Filter plusFilter, Filter minusFilter,
      MonthRange range, TurnoverOrBalance turnoverOrBalance, IndicatorSource source,
      BeeView finView, Long userId, Long currency, LongSummaryStatistics stats) {

    Double value = null;

    if (range == null || turnoverOrBalance == null) {
      return value;
    }
    Filter rangeFilter = getActualRangeFilter(range, turnoverOrBalance);

    if (plusFilter != null) {
      Double plus = getSum(finView, userId, Filter.and(filter, rangeFilter, plusFilter),
          source, currency, stats);

      if (BeeUtils.nonZero(plus)) {
        if (BeeUtils.isDouble(value)) {
          value += plus;
        } else {
          value = plus;
        }
      }
    }

    if (minusFilter != null) {
      Double minus = getSum(finView, userId, Filter.and(filter, rangeFilter, minusFilter),
          source, currency, stats);

      if (BeeUtils.nonZero(minus)) {
        if (BeeUtils.isDouble(value)) {
          value -= minus;
        } else {
          value = -minus;
        }
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

  private Filter getActualValueFilter(long indicator, Filter parentFilter,
      String sourceColumn, BeeView finView, Long userId) {

    Filter indicatorFilter = getIndicatorFilter(indicator, finView, userId);

    Filter sourceFilter = Filter.nonZero(sourceColumn);

    return Filter.and(parentFilter, indicatorFilter, sourceFilter);
  }

  private static Filter getActualRangeFilter(MonthRange range,
      TurnoverOrBalance turnoverOrBalance) {

    if (range == null || turnoverOrBalance == null) {
      return Filter.isFalse();
    } else {
      return turnoverOrBalance.getRangeFilter(COL_FIN_DATE, range);
    }
  }

  private static Filter getActualSplitFilter(AnalysisSplitType splitType,
      AnalysisSplitValue splitValue) {

    if (splitType == null || splitValue == null || splitType.isPeriod()
        || BeeUtils.isEmpty(splitType.getFinColumn())) {

      return null;

    } else if (DataUtils.isId(splitValue.getId())) {
      return Filter.equals(splitType.getFinColumn(), splitValue.getId());

    } else {
      return Filter.isNull(splitType.getFinColumn());
    }
  }

  private Double getSum(BeeView view, Long userId, Filter filter, IndicatorSource source,
      Long currency, LongSummaryStatistics stats) {

    Double sum;

    String sourceAlias = view.getSourceAlias();

    String sourceColumn = source.getSourceColumn();
    String currencyColumn = source.getCurrencyColumn();

    SqlSelect query = view.getQuery(userId, filter, null,
        Collections.singleton(sourceColumn));
    query.resetOrder();

    if (!BeeUtils.isEmpty(currencyColumn) && DataUtils.isId(currency)) {
      IsCondition where = query.getWhere();

      query.resetFields();
      query.addSum(sourceAlias, sourceColumn);

      query.setWhere(SqlUtils.and(where, SqlUtils.equals(sourceAlias, currencyColumn, currency)));
      sum = getValue(query, stats);

      IsExpression expression = ExchangeUtils.exchangeFieldTo(query, sourceAlias,
          sourceColumn, currencyColumn, COL_FIN_DATE, currency);

      query.resetFields();
      query.addSum(SqlUtils.round(expression, view.getColumnScale(sourceColumn)),
          SqlUtils.uniqueName(sourceColumn));

      query.setWhere(SqlUtils.and(where, SqlUtils.notEqual(sourceAlias, currencyColumn, currency)));
      Double value = getValue(query, stats);

      if (BeeUtils.nonZero(value)) {
        if (BeeUtils.nonZero(sum)) {
          sum += value;
        } else {
          sum = value;
        }
      }

    } else {
      query.resetFields();
      query.addSum(sourceAlias, sourceColumn);

      sum = getValue(query, stats);
    }

    return sum;
  }

  private Double getValue(SqlSelect query, LongSummaryStatistics stats) {
    long millis = System.currentTimeMillis();

    Double value = qs.getDouble(query);

    if (stats != null) {
      stats.accept(System.currentTimeMillis() - millis);
    }

    return value;
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

  private static IsCondition getBudgetYearCondition(String source, String column,
      MonthRange range, TurnoverOrBalance turnoverOrBalance) {

    if (range == null) {
      return SqlUtils.sqlFalse();
    }

    YearMonth lower = range.getMinMonth();
    YearMonth upper = range.getMaxMonth();

    if (turnoverOrBalance == TurnoverOrBalance.OPENING_BALANCE) {
      return SqlUtils.equals(source, column, lower.getYear());

    } else if (turnoverOrBalance == TurnoverOrBalance.CLOSING_BALANCE) {
      return SqlUtils.equals(source, column, upper.getYear());

    } else {
      return SqlUtils.and(
          SqlUtils.moreEqual(source, column, lower.getYear()),
          SqlUtils.lessEqual(source, column, upper.getYear()));
    }
  }

  private Collection<AnalysisValue> computeBudgetValues(long columnId, long rowId,
      String source, IsCondition condition, MonthRange range,
      TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> columnSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues,
      List<AnalysisSplitType> rowSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues, Long currency) {

    List<AnalysisValue> values = new ArrayList<>();

    boolean hasColumnSplits = !columnSplitTypes.isEmpty() && !columnSplitValues.isEmpty();
    boolean hasRowSplits = !rowSplitTypes.isEmpty() && !rowSplitValues.isEmpty();

    if (hasColumnSplits && hasRowSplits) {
      values.addAll(computeBudgetSplitMatrix(columnId, rowId, null,
          source, condition, range, turnoverOrBalance,
          columnSplitTypes, 0, columnSplitValues, 0,
          rowSplitTypes, rowSplitValues, currency));

    } else if (hasColumnSplits) {
      values.addAll(computeBudgetSplitVector(columnId, rowId, true, null,
          source, condition, range, turnoverOrBalance,
          columnSplitTypes, 0, columnSplitValues, 0, currency));

    } else if (hasRowSplits) {
      values.addAll(computeBudgetSplitVector(columnId, rowId, false, null,
          source, condition, range, turnoverOrBalance,
          rowSplitTypes, 0, rowSplitValues, 0, currency));

    } else {
      Double value = getBudgetValue(source, condition, range, turnoverOrBalance, currency);
      if (BeeUtils.isDouble(value)) {
        values.add(AnalysisValue.budget(columnId, rowId, value));
      }
    }

    return values;
  }

  private Collection<AnalysisValue> computeBudgetSplitVector(long columnId, long rowId,
      boolean isColumn, Map<AnalysisSplitType, AnalysisSplitValue> parentSplit,
      String source, IsCondition condition, MonthRange range,
      TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> splitTypes, int typeIndex,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> splitValues, int valueIndex,
      Long currency) {

    List<AnalysisValue> values = new ArrayList<>();

    AnalysisSplitType splitType = splitTypes.get(typeIndex);

    if (splitValues.containsKey(splitType)) {
      List<AnalysisSplitValue> typeValues = splitValues.get(splitType);
      AnalysisSplitValue splitValue = typeValues.get(valueIndex);

      IsCondition splitCondition = SqlUtils.and(condition,
          getBudgetSplitCondition(source, splitType, splitValue));
      MonthRange splitRange = getSplitRange(range, splitType, splitValue);

      if (typeIndex < splitTypes.size() - 1) {
        Map<AnalysisSplitType, AnalysisSplitValue> split =
            buildSplit(parentSplit, splitType, splitValue);

        values.addAll(computeBudgetSplitVector(columnId, rowId, isColumn, split,
            source, splitCondition, splitRange, turnoverOrBalance,
            splitTypes, typeIndex + 1, splitValues, 0, currency));

      } else {
        Double value = getBudgetValue(source, splitCondition, splitRange, turnoverOrBalance,
            currency);

        if (BeeUtils.isDouble(value)) {
          AnalysisValue av = AnalysisValue.budget(columnId, rowId, value);

          if (isColumn) {
            av.putColumnSplit(parentSplit, splitType, splitValue);
          } else {
            av.putRowSplit(parentSplit, splitType, splitValue);
          }

          values.add(av);
        }
      }

      if (valueIndex < typeValues.size() - 1) {
        values.addAll(computeBudgetSplitVector(columnId, rowId, isColumn, parentSplit,
            source, condition, range, turnoverOrBalance,
            splitTypes, typeIndex, splitValues, valueIndex + 1, currency));
      }
    }

    return values;
  }

  private Collection<AnalysisValue> computeBudgetSplitMatrix(long columnId, long rowId,
      Map<AnalysisSplitType, AnalysisSplitValue> columnParentSplit,
      String source, IsCondition condition, MonthRange range,
      TurnoverOrBalance turnoverOrBalance,
      List<AnalysisSplitType> columnSplitTypes, int columnTypeIndex,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> columnSplitValues, int columnValueIndex,
      List<AnalysisSplitType> rowSplitTypes,
      Map<AnalysisSplitType, List<AnalysisSplitValue>> rowSplitValues, Long currency) {

    List<AnalysisValue> values = new ArrayList<>();

    AnalysisSplitType columnSplitType = columnSplitTypes.get(columnTypeIndex);

    if (columnSplitValues.containsKey(columnSplitType)) {
      List<AnalysisSplitValue> columnTypeValues = columnSplitValues.get(columnSplitType);
      AnalysisSplitValue columnSplitValue = columnTypeValues.get(columnValueIndex);

      IsCondition splitCondition = SqlUtils.and(condition,
          getBudgetSplitCondition(source, columnSplitType, columnSplitValue));
      MonthRange splitRange = getSplitRange(range, columnSplitType, columnSplitValue);

      if (columnTypeIndex < columnSplitTypes.size() - 1) {
        Map<AnalysisSplitType, AnalysisSplitValue> columnSplit =
            buildSplit(columnParentSplit, columnSplitType, columnSplitValue);

        values.addAll(computeBudgetSplitMatrix(columnId, rowId, columnSplit,
            source, splitCondition, splitRange, turnoverOrBalance,
            columnSplitTypes, columnTypeIndex + 1, columnSplitValues, 0,
            rowSplitTypes, rowSplitValues, currency));

      } else {
        Collection<AnalysisValue> rowValues = computeBudgetSplitVector(columnId, rowId,
            false, getRootSplit(),
            source, splitCondition, splitRange, turnoverOrBalance,
            rowSplitTypes, 0, rowSplitValues, 0, currency);

        if (!rowValues.isEmpty()) {
          for (AnalysisValue av : rowValues) {
            av.putColumnSplit(columnParentSplit, columnSplitType, columnSplitValue);
            values.add(av);
          }
        }
      }

      if (columnValueIndex < columnTypeValues.size() - 1) {
        values.addAll(computeBudgetSplitMatrix(columnId, rowId, columnParentSplit,
            source, condition, range, turnoverOrBalance,
            columnSplitTypes, columnTypeIndex, columnSplitValues, columnValueIndex + 1,
            rowSplitTypes, rowSplitValues, currency));
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

  private static boolean budgetMonthMatch(YearMonth ym, MonthRange range,
      TurnoverOrBalance turnoverOrBalance) {

    if (turnoverOrBalance == TurnoverOrBalance.OPENING_BALANCE) {
      return ym.equals(range.getMinMonth());

    } else if (turnoverOrBalance == TurnoverOrBalance.CLOSING_BALANCE) {
      return ym.equals(range.getMaxMonth());

    } else {
      return range.contains(ym);
    }
  }

  private Double getBudgetValue(String source, IsCondition condition, MonthRange range,
      TurnoverOrBalance turnoverOrBalance, Long currency) {

    Double result = null;
    if (range == null) {
      return result;
    }

    String yearColumn = COL_BUDGET_ENTRY_YEAR;

    IsCondition yearCondition = getBudgetYearCondition(source, yearColumn,
        range, turnoverOrBalance);

    SqlSelect query = new SqlSelect()
        .addFields(source, COL_BUDGET_ENTRY_YEAR, COL_BUDGET_HEADER_CURRENCY)
        .addFields(source, COL_BUDGET_ENTRY_VALUES)
        .addFrom(source)
        .setWhere(SqlUtils.and(condition, yearCondition));

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return result;
    }

    for (SimpleRow row : data) {
      int year = row.getInt(yearColumn);

      for (int month = 1; month <= 12; month++) {
        Double value = row.getDouble(colBudgetEntryValue(month));

        if (BeeUtils.isDouble(value)) {
          YearMonth ym = new YearMonth(year, month);

          if (budgetMonthMatch(ym, range, turnoverOrBalance)) {
            if (DataUtils.isId(currency)) {
              Long budgetCurrency = row.getLong(COL_BUDGET_HEADER_CURRENCY);
              if (!DataUtils.isId(budgetCurrency)) {
                budgetCurrency = getDefaultCurrency();
              }

              if (DataUtils.isId(budgetCurrency) && !Objects.equals(currency, budgetCurrency)) {
                int scale = BeeUtils.getDecimals(BeeUtils.toString(value));
                value = adm.maybeExchange(budgetCurrency, currency, value,
                    ym.getDate().getDateTime());

                if (scale >= 0) {
                  value = BeeUtils.round(value, scale);
                }
              }
            }

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

  private Collection<YearMonth> getBudgetMonths(String source, IsCondition condition,
      MonthRange range, TurnoverOrBalance turnoverOrBalance) {

    Set<YearMonth> result = new HashSet<>();
    if (range == null) {
      return result;
    }

    String yearColumn = COL_BUDGET_ENTRY_YEAR;

    IsCondition yearCondition = getBudgetYearCondition(source, yearColumn,
        range, turnoverOrBalance);

    SqlSelect query = new SqlSelect()
        .addFields(source, COL_BUDGET_ENTRY_YEAR)
        .addFields(source, COL_BUDGET_ENTRY_VALUES)
        .addFrom(source)
        .setWhere(SqlUtils.and(condition, yearCondition));

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return result;
    }

    for (SimpleRow row : data) {
      int year = row.getInt(yearColumn);

      for (int month = 1; month <= 12; month++) {
        Double value = row.getDouble(colBudgetEntryValue(month));

        if (BeeUtils.isDouble(value)) {
          YearMonth ym = new YearMonth(year, month);

          if (budgetMonthMatch(ym, range, turnoverOrBalance)) {
            result.add(ym);
          }
        }
      }
    }

    return result;
  }

  private Long getDefaultCurrency() {
    return prm.getRelation(AdministrationConstants.PRM_CURRENCY);
  }
}

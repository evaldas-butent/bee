package com.butent.bee.egg.server.datasource;

import com.butent.bee.egg.server.datasource.base.DataSourceParameters;
import com.butent.bee.egg.server.datasource.base.LocaleUtil;
import com.butent.bee.egg.server.datasource.base.OutputType;
import com.butent.bee.egg.server.datasource.base.ResponseStatus;
import com.butent.bee.egg.server.datasource.base.StatusType;
import com.butent.bee.egg.server.datasource.query.Query;
import com.butent.bee.egg.server.datasource.query.engine.QueryEngine;
import com.butent.bee.egg.server.datasource.query.parser.QueryBuilder;
import com.butent.bee.egg.server.datasource.render.CsvRenderer;
import com.butent.bee.egg.server.datasource.render.HtmlRenderer;
import com.butent.bee.egg.server.datasource.render.JsonRenderer;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.DataException;
import com.butent.bee.egg.shared.data.DataTable;
import com.butent.bee.egg.shared.data.InvalidQueryException;
import com.butent.bee.egg.shared.data.IsTable;
import com.butent.bee.egg.shared.data.Messages;
import com.butent.bee.egg.shared.data.Reasons;
import com.butent.bee.egg.shared.data.column.AggregationColumn;
import com.butent.bee.egg.shared.data.column.ScalarFunctionColumn;
import com.butent.bee.egg.shared.utils.LogUtils;
import com.ibm.icu.util.ULocale;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DataSourceHelper {

  private static final Logger logger = Logger.getLogger(DataSourceHelper.class.getName());

  static final String LOCALE_REQUEST_PARAMETER = "hl";

  public static IsTable applyQuery(Query query, DataTable dataTable, ULocale locale)
      throws InvalidQueryException, DataException {
    validateQueryAgainstColumnStructure(query, dataTable);
    dataTable = QueryEngine.executeQuery(query, dataTable, locale);
    return dataTable;
  }

  public static void executeDataSourceServletFlow(HttpServletRequest req, HttpServletResponse resp,
      DataTableGenerator dtGenerator) throws IOException {
    executeDataSourceServletFlow(req, resp, dtGenerator, true);
  }

  public static void executeDataSourceServletFlow(HttpServletRequest req, HttpServletResponse resp,
      DataTableGenerator dtGenerator, boolean isRestrictedAccessMode) throws IOException {
    DataSourceRequest dsRequest = null;
    try {
      dsRequest = new DataSourceRequest(req);

      if (isRestrictedAccessMode) {
        DataSourceHelper.verifyAccessApproved(dsRequest);
      }

      QueryPair query = DataSourceHelper.splitQuery(dsRequest.getQuery(),
          dtGenerator.getCapabilities());

      DataTable dataTable = dtGenerator.generateDataTable(query.getDataSourceQuery(), req);

      IsTable newDataTable = DataSourceHelper.applyQuery(query.getCompletionQuery(), dataTable,
          dsRequest.getUserLocale());

      setServletResponse(newDataTable, dsRequest, resp);
    } catch (DataException e) {
      if (dsRequest != null) {
        setServletErrorResponse(e, dsRequest, resp);
      } else {
        DataSourceHelper.setServletErrorResponse(e, req, resp);
      }
    } catch (RuntimeException e) {
      LogUtils.severe(logger, e, "A runtime exception has occured");
      ResponseStatus status = new ResponseStatus(StatusType.ERROR, Reasons.INTERNAL_ERROR,
          e.getMessage());
      if (dsRequest == null) {
        dsRequest = DataSourceRequest.getDefaultDataSourceRequest(req);
      }
      DataSourceHelper.setServletErrorResponse(status, dsRequest, resp);
    }
  }

  public static String generateErrorResponse(DataException dse, DataSourceRequest dsReq) {
    ResponseStatus responseStatus = ResponseStatus.createResponseStatus(dse);
    responseStatus = ResponseStatus.getModifiedResponseStatus(responseStatus);
    return generateErrorResponse(responseStatus, dsReq);
  }

  public static String generateErrorResponse(ResponseStatus responseStatus,
      DataSourceRequest dsRequest) {
    DataSourceParameters dsParameters = dsRequest.getDataSourceParameters();
    CharSequence response;
    switch (dsParameters.getOutputType()) {
      case CSV:
      case TSV_EXCEL:
        response = CsvRenderer.renderCsvError(responseStatus);
        break;
      case HTML:
        response = HtmlRenderer.renderHtmlError(responseStatus);
        break;
      case JSONP:
        response = JsonRenderer.renderJsonResponse(dsParameters, responseStatus, null, true);
        break;
      case JSON:
        response = JsonRenderer.renderJsonResponse(dsParameters, responseStatus, null, false);
        break;
      default:
        Assert.untouchable("Unhandled output type.");
        return null;
    }
    return response.toString();
  }

  public static String generateResponse(IsTable dataTable, DataSourceRequest dataSourceRequest) {
    CharSequence response;
    ResponseStatus responseStatus = null;
    if (!dataTable.getWarnings().isEmpty()) {
      responseStatus = new ResponseStatus(StatusType.WARNING);
    }
    switch (dataSourceRequest.getDataSourceParameters().getOutputType()) {
      case CSV:
        response = CsvRenderer.renderDataTable(dataTable, dataSourceRequest.getUserLocale(), ",");
        break;
      case TSV_EXCEL:
        response = CsvRenderer.renderDataTable(dataTable, dataSourceRequest.getUserLocale(), "\t");
        break;
      case HTML:
        response = HtmlRenderer.renderDataTable(dataTable, dataSourceRequest.getUserLocale());
        break;
      case JSONP:
        response = JsonRenderer.renderJsonResponse(
            dataSourceRequest.getDataSourceParameters(), responseStatus, dataTable, true);
        break;
      case JSON:
        response = JsonRenderer.renderJsonResponse(
            dataSourceRequest.getDataSourceParameters(), responseStatus, dataTable, false);
        break;
      default:
        Assert.untouchable("Unhandled output type.");
        return null;
    }

    return response.toString();
  }

  public static ULocale getLocaleFromRequest(HttpServletRequest req) {
    Locale locale;
    String requestLocale = req.getParameter(LOCALE_REQUEST_PARAMETER);
    if (requestLocale != null) {
      locale = LocaleUtil.getLocaleFromLocaleString(requestLocale);
    } else {
      locale = req.getLocale();
    }
    return ULocale.forLocale(locale);
  }

  public static Query parseQuery(String queryString) throws InvalidQueryException {
    QueryBuilder queryBuilder = QueryBuilder.getInstance();
    Query query = queryBuilder.parseQuery(queryString);

    return query;
  }

  public static void setServletErrorResponse(DataException dataSourceException,
      DataSourceRequest dataSourceRequest, HttpServletResponse res) throws IOException {
    String responseMessage = generateErrorResponse(dataSourceException, dataSourceRequest);
    setServletResponse(responseMessage, dataSourceRequest, res);
  }

  public static void setServletErrorResponse(DataException dataSourceException,
      HttpServletRequest req, HttpServletResponse res) throws IOException {
    DataSourceRequest dataSourceRequest = DataSourceRequest.getDefaultDataSourceRequest(req);
    setServletErrorResponse(dataSourceException, dataSourceRequest, res);
  }

  public static void setServletErrorResponse(ResponseStatus responseStatus,
      DataSourceRequest dataSourceRequest, HttpServletResponse res) throws IOException {
    String responseMessage = generateErrorResponse(responseStatus, dataSourceRequest);
    setServletResponse(responseMessage, dataSourceRequest, res);
  }

  public static void setServletResponse(IsTable dataTable, DataSourceRequest dataSourceRequest,
      HttpServletResponse res) throws IOException {
    String responseMessage = generateResponse(dataTable, dataSourceRequest);
    setServletResponse(responseMessage, dataSourceRequest, res);
  }
  
  public static void setServletResponse(String responseMessage,
      DataSourceRequest dataSourceRequest, HttpServletResponse res) throws IOException {
    DataSourceParameters dataSourceParameters = dataSourceRequest.getDataSourceParameters();
    ResponseWriter.setServletResponse(responseMessage, dataSourceParameters, res);
  }

  public static QueryPair splitQuery(Query query, Capabilities capabilities)
      throws DataException {
    return QuerySplitter.splitQuery(query, capabilities);
  }

  public static void validateQueryAgainstColumnStructure(Query query, DataTable dataTable)
      throws InvalidQueryException {
    Set<String> mentionedColumnIds = query.getAllColumnIds();
    for (String columnId : mentionedColumnIds) {
      if (!dataTable.containsColumn(columnId)) {
        String messageToLogAndUser = Messages.NO_COLUMN.getMessage(columnId);
        LogUtils.severe(logger, messageToLogAndUser);
        throw new InvalidQueryException(messageToLogAndUser);
      }
    }

    Set<AggregationColumn> mentionedAggregations = query.getAllAggregations();
    for (AggregationColumn agg : mentionedAggregations) {
      try {
        agg.validateColumn(dataTable);
      } catch (RuntimeException e) {
        LogUtils.severe(logger, e, "A runtime exception has occured");
        throw new InvalidQueryException(e.getMessage());
      }
    }

    Set<ScalarFunctionColumn> mentionedScalarFunctionColumns =
        query.getAllScalarFunctionsColumns();
    for (ScalarFunctionColumn col : mentionedScalarFunctionColumns) {
      col.validateColumn(dataTable);
    }
  }

  public static void verifyAccessApproved(DataSourceRequest req) throws DataException {
    OutputType outType = req.getDataSourceParameters().getOutputType();
    if (outType != OutputType.CSV && outType != OutputType.TSV_EXCEL
        && outType != OutputType.HTML && !req.isSameOrigin()) {
      throw new DataException(Reasons.ACCESS_DENIED,
          "Unauthorized request. Cross domain requests are not supported.");
    }
  }

  private DataSourceHelper() {
  }
}

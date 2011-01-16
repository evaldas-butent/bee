package com.butent.bee.egg.server.datasource;

import com.butent.bee.egg.server.datasource.base.DataSourceParameters;
import com.butent.bee.egg.server.datasource.base.OutputType;
import com.butent.bee.egg.server.datasource.query.Query;
import com.butent.bee.egg.shared.data.DataException;
import com.butent.bee.egg.shared.data.InvalidQueryException;
import com.ibm.icu.util.ULocale;

import javax.servlet.http.HttpServletRequest;

public class DataSourceRequest {
  public static final String SAME_ORIGIN_HEADER = "X-DataSource-Auth";

  public static final String QUERY_REQUEST_PARAMETER = "tq";
  public static final String DATASOURCE_REQUEST_PARAMETER = "tqx";

  public static boolean determineSameOrigin(HttpServletRequest req) {
    return (req.getHeader(SAME_ORIGIN_HEADER) != null);
  }

  public static DataSourceRequest getDefaultDataSourceRequest(HttpServletRequest req) {
    DataSourceRequest dataSourceRequest = new DataSourceRequest();
    dataSourceRequest.inferLocaleFromRequest(req);
    dataSourceRequest.sameOrigin = determineSameOrigin(req);
    try {
      dataSourceRequest.createDataSourceParametersFromRequest(req);
    } catch (DataException e) {
      if (dataSourceRequest.dsParams == null) {
        dataSourceRequest.dsParams = DataSourceParameters.getDefaultDataSourceParameters();
      }
      if ((dataSourceRequest.dsParams.getOutputType() == OutputType.JSON)
          && (!dataSourceRequest.sameOrigin)) {
        dataSourceRequest.dsParams.setOutputType(OutputType.JSONP);
      }
    }
    try {
      dataSourceRequest.createQueryFromRequest(req);
    } catch (InvalidQueryException e) {
    }
    return dataSourceRequest;
  }

  private Query query;

  private DataSourceParameters dsParams;

  private ULocale userLocale;

  private boolean sameOrigin;
  
  public DataSourceRequest(HttpServletRequest req) throws DataException {
    inferLocaleFromRequest(req);
    sameOrigin = determineSameOrigin(req);
    createDataSourceParametersFromRequest(req);
    createQueryFromRequest(req);
  }

  public DataSourceRequest(Query query, DataSourceParameters dsParams, ULocale userLocale) {
    setUserLocale(userLocale);
    this.dsParams = dsParams;
    this.query = query;
  }

  private DataSourceRequest() {
  }

  public DataSourceParameters getDataSourceParameters() {
    return dsParams;
  }

  public Query getQuery() {
    return query;
  }

  public ULocale getUserLocale() {
    return userLocale;
  }

  public boolean isSameOrigin() {
    return sameOrigin;
  }

  public void setUserLocale(ULocale userLocale) {
    this.userLocale = userLocale;
  }

  private void createDataSourceParametersFromRequest(HttpServletRequest req)
      throws DataException {
    String dataSourceParamsString = req.getParameter(DATASOURCE_REQUEST_PARAMETER);

    dsParams = new DataSourceParameters(dataSourceParamsString);
    if (dsParams.getOutputType() == OutputType.JSON && !sameOrigin) {
      dsParams.setOutputType(OutputType.JSONP);
    }
  }

  private void createQueryFromRequest(HttpServletRequest req) throws InvalidQueryException {
    String queryString = req.getParameter(QUERY_REQUEST_PARAMETER);
    query = DataSourceHelper.parseQuery(queryString);
  }

  private void inferLocaleFromRequest(HttpServletRequest req) {
    userLocale = DataSourceHelper.getLocaleFromRequest(req);
  }
}

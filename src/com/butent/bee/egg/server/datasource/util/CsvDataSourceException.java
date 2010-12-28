package com.butent.bee.egg.server.datasource.util;

import com.butent.bee.egg.server.datasource.base.DataSourceException;
import com.butent.bee.egg.server.datasource.base.ReasonType;

@SuppressWarnings("serial")
public class CsvDataSourceException extends DataSourceException {
  public CsvDataSourceException(ReasonType reasonType, String messageToUser) {
    super(reasonType, messageToUser);
  }
}

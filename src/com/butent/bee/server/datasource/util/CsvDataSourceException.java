package com.butent.bee.server.datasource.util;

import com.butent.bee.shared.data.DataException;
import com.butent.bee.shared.data.Reasons;

@SuppressWarnings("serial")
public class CsvDataSourceException extends DataException {
  public CsvDataSourceException(Reasons reasonType, String messageToUser) {
    super(reasonType, messageToUser);
  }
}

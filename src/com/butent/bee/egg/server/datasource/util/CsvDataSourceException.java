package com.butent.bee.egg.server.datasource.util;

import com.butent.bee.egg.shared.data.DataException;
import com.butent.bee.egg.shared.data.Reasons;

@SuppressWarnings("serial")
public class CsvDataSourceException extends DataException {
  public CsvDataSourceException(Reasons reasonType, String messageToUser) {
    super(reasonType, messageToUser);
  }
}

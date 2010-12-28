package com.butent.bee.egg.server.datasource.base;

@SuppressWarnings("serial")
public class TypeMismatchException extends DataSourceException {
  public TypeMismatchException(String message) {
    super(ReasonType.OTHER, message);
  }
}

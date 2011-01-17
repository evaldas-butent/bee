package com.butent.bee.server.datasource.query;

public class QueryOptions {
  private boolean noValues;
  private boolean noFormat;

  public QueryOptions() {
    noValues = false;
    noFormat = false;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    QueryOptions other = (QueryOptions) obj;
    if (noFormat != other.noFormat) {
      return false;
    }
    if (noValues != other.noValues) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (noFormat ? 1231 : 1237);
    result = prime * result + (noValues ? 1231 : 1237);
    return result;
  }

  public boolean isDefault() {
    return !noFormat && !noValues;
  }

  public boolean isNoFormat() {
    return noFormat;
  }

  public boolean isNoValues() {
    return noValues;
  }

  public void setNoFormat(boolean noFormat) {
    this.noFormat = noFormat;
  }

  public void setNoValues(boolean noValues) {
    this.noValues = noValues;
  }
  
  public String toQueryString() {
     return (noValues ? "NO_VALUES" : "") + (noFormat ? "NO_FORMAT" : "");    
  }
}

package com.butent.bee.shared.data.view;

public class RelationInfo {

  private final String source;
  private final String relTable;
  private final String relField;

  public RelationInfo(String source, String relTable, String relField) {
    this.source = source;
    this.relTable = relTable;
    this.relField = relField;
  }

  public String getRelField() {
    return relField;
  }

  public String getRelTable() {
    return relTable;
  }

  public String getSource() {
    return source;
  }
}

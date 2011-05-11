package com.butent.bee.client.grid.property;

/**
 * Stores information whether a specified column can be truncated.
 */

public class TruncationProperty implements ColumnProperty {
  public static final String NAME = "trunc";

  private boolean isTruncatable;
  private boolean isFooterTruncatable;
  private boolean isHeaderTruncatable;

  public TruncationProperty() {
    this(true);
  }

  public TruncationProperty(boolean isTruncatable) {
    this.isTruncatable = isTruncatable;
    this.isHeaderTruncatable = true;
    this.isFooterTruncatable = true;
  }

  public boolean isColumnTruncatable() {
    return isTruncatable;
  }

  public boolean isFooterTruncatable() {
    return isFooterTruncatable;
  }

  public boolean isHeaderTruncatable() {
    return isHeaderTruncatable;
  }

  public void setColumnTruncatable(boolean isTruncatable) {
    this.isTruncatable = isTruncatable;
  }

  public void setFooterTruncatable(boolean isTruncatable) {
    this.isFooterTruncatable = isTruncatable;
  }

  public void setHeaderTruncatable(boolean isTruncatable) {
    this.isHeaderTruncatable = isTruncatable;
  }
}

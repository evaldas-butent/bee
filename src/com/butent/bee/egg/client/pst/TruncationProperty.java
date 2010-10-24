package com.butent.bee.egg.client.pst;

public class TruncationProperty extends ColumnProperty {
  public static final Type<TruncationProperty> TYPE = new Type<TruncationProperty>() {
    private TruncationProperty instance;

    @Override
    public TruncationProperty getDefault() {
      if (instance == null) {
        instance = new TruncationProperty(true);
      }
      return instance;
    }
  };

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

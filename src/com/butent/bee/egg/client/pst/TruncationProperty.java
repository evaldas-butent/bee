package com.butent.bee.egg.client.pst;

/**
 * A {@link ColumnProperty} that describes whether or not the contents of the
 * column can be truncated.
 */
public class TruncationProperty extends ColumnProperty {
  /**
   * Property type.
   */
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

  /**
   * Construct a new {@link TruncationProperty}.
   */
  public TruncationProperty() {
    this(true);
  }

  /**
   * Construct a new {@link TruncationProperty}.
   * 
   * @param isTruncatable true if the column is truncatable, false if not
   */
  public TruncationProperty(boolean isTruncatable) {
    this.isTruncatable = isTruncatable;
    this.isHeaderTruncatable = true;
    this.isFooterTruncatable = true;
  }

  /**
   * @return true (default) if the column is truncatable
   */
  public boolean isColumnTruncatable() {
    return isTruncatable;
  }

  /**
   * @return true (default) if column in the footer is truncatable
   */
  public boolean isFooterTruncatable() {
    return isFooterTruncatable;
  }

  /**
   * @return true (default) if column in the header is truncatable
   */
  public boolean isHeaderTruncatable() {
    return isHeaderTruncatable;
  }

  /**
   * Set whether or not column are truncatable.
   * 
   * @param isTruncatable true to make truncatable
   */
  public void setColumnTruncatable(boolean isTruncatable) {
    this.isTruncatable = isTruncatable;
  }

  /**
   * Set whether or not the column in the footer is truncatable.
   * 
   * @param isTruncatable true to make truncatable
   */
  public void setFooterTruncatable(boolean isTruncatable) {
    this.isFooterTruncatable = isTruncatable;
  }

  /**
   * Set whether or not the column in the header is truncatable.
   * 
   * @param isTruncatable true to make truncatable
   */
  public void setHeaderTruncatable(boolean isTruncatable) {
    this.isHeaderTruncatable = isTruncatable;
  }
}

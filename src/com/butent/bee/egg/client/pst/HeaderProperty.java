package com.butent.bee.egg.client.pst;

/**
 * A {@link ColumnProperty} that provides information about the headers above a
 * column. The row indexes start from the bottom of the header, such that all
 * headers at the 0th index refer to the row directly above the data table.
 */
public class HeaderProperty extends HeaderPropertyBase {
  /**
   * Property type.
   */
  public static final Type<HeaderProperty> TYPE = new Type<HeaderProperty>() {
    private HeaderProperty instance;

    @Override
    public HeaderProperty getDefault() {
      if (instance == null) {
        instance = new HeaderProperty();
      }
      return instance;
    }
  };

  /**
   * Get the header at the given row index.
   * 
   * @param row the row index from the bottom.
   * @return the header for the given row
   */
  @Override
  public Object getHeader(int row) {
    return super.getHeader(row);
  }

  /**
   * Get the header at the given row and column index. Override this method if
   * your header includes dynamic content that depends on the column index.
   * 
   * @param row the row index from the bottom
   * @param column the column index at runtime
   * @return the header for the given row
   */
  @Override
  public Object getHeader(int row, int column) {
    return getHeader(row);
  }

  /**
   * @return get the number of headers above the column
   */
  @Override
  public int getHeaderCount() {
    return super.getHeaderCount();
  }

  /**
   * Remove the header above this column for the specified row.
   * 
   * @param row the row index from the bottom
   */
  @Override
  public void removeHeader(int row) {
    super.removeHeader(row);
  }

  /**
   * Set the header above this column. The row index starts with the bottom row,
   * which is reverse of a normal table. The headerCount will automatically be
   * increased to accommodate the row.
   * 
   * @param row the row index from the bottom
   * @param header the header
   */
  @Override
  public void setHeader(int row, Object header) {
    super.setHeader(row, header);
  }

  /**
   * Set the number of headers above the column.
   * 
   * @param headerCount the number of headers
   */
  @Override
  public void setHeaderCount(int headerCount) {
    super.setHeaderCount(headerCount);
  }
}

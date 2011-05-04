package com.butent.bee.shared.utils;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;

/**
 * Extends Property implementation with new funcionality.
 */
public class ExtendedProperty extends Property {
  public static String[] COLUMN_HEADERS = new String[]{"Name", "Sub", "Value", "Date"};
  public static int COLUMN_COUNT = COLUMN_HEADERS.length;

  private String sub;
  private DateTime date = new DateTime();

  /**
   * Creates an ExtendedProperty with specified {@code name} and {@code value} values.
   * 
   * @param name the {@code name} to set for the ExtendedProperty
   * @param value the {@code value} to set for the ExtendedProperty
   */
  public ExtendedProperty(String name, String value) {
    super(name, value);
    this.sub = BeeConst.STRING_EMPTY;
  }

  /**
   * Creates an ExtendedProperty with specified {@code name}, {@code sub} and {@code value} values.
   * 
   * @param name the {@code name} to set for the ExtendedProperty
   * @param sub the {@code sub} to set for the ExtendedProperty
   * @param value the {@code value} to set for the ExtendedProperty
   */
  public ExtendedProperty(String name, String sub, String value) {
    super(name, value);
    this.sub = sub;
  }

  /**
   * Creates an ExtendedProperty from another specified ExtendedProperty {@code sp}. Name, Sub and
   * Value are copied from {@code sp}.
   * 
   * @param sp the ExtendedProperty to get parameters(Name, Sub, Value) from.
   */
  public ExtendedProperty(ExtendedProperty sp) {
    this(sp.getName(), sp.getSub(), sp.getValue());
  }

  /**
   * @return the stored date in the ExtendedProperty.
   */
  public DateTime getDate() {
    return date;
  }

  /**
   * 
   * @return the stored sub in the ExtendedProperty.
   */
  public String getSub() {
    return sub;
  }

  /**
   * Sets a date.
   * 
   * @param date a value to set date to
   */
  public void setDate(DateTime date) {
    this.date = date;
  }

  /**
   * Sets the sub.
   * 
   * @param sub a value to set sub.
   */
  public void setSub(String sub) {
    this.sub = sub;
  }

  /**
   * @return a String representation of the current ExtendedProperty. Name is separated with a
   *         default value separator "=", Sub by a default property separator ".".
   *         <p>
   *         E.g An ExtendedProperty with {@code Name}, {@code Sub} and {@code Value} set would be
   *         represented as: {@code Name.Sub=Value}
   *         </p>
   */
  @Override
  public String toString() {
    return BeeUtils.concat(BeeConst.DEFAULT_VALUE_SEPARATOR, BeeUtils.concat(
        BeeConst.DEFAULT_PROPERTY_SEPARATOR, getName(), getSub()),
        BeeUtils.transform(getValue()));
  }

  /**
   * Returns a String representation of the ExtendedPeoperty. See {@link #toString()}
   */
  public String transform() {
    return toString();
  }
}

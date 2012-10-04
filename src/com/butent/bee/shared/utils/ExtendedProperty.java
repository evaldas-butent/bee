package com.butent.bee.shared.utils;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.DateTime;

/**
 * Extends Property implementation with new funcionality.
 */
public class ExtendedProperty extends Property {

  public static String[] COLUMN_HEADERS = new String[] {"Name", "Sub", "Value", "Date"};
  public static int COLUMN_COUNT = COLUMN_HEADERS.length;

  public static ExtendedProperty restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    ExtendedProperty ep = new ExtendedProperty();
    ep.deserialize(s);
    return ep;
  }

  private String sub;
  private DateTime date = new DateTime();

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

  private ExtendedProperty() {
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, COLUMN_COUNT);

    int i = 0;
    setName(arr[i++]);
    setSub(arr[i++]);
    setValue(arr[i++]);
    setDate(DateTime.restore(arr[i]));
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

  @Override
  public String serialize() {
    return Codec.beeSerialize(new Object[] {getName(), getSub(), getValue(), getDate()});
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
    return BeeUtils.join(BeeConst.DEFAULT_VALUE_SEPARATOR,
        BeeUtils.join(BeeConst.DEFAULT_PROPERTY_SEPARATOR, getName(), getSub()), getValue());
  }
}

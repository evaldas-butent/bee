package com.butent.bee.shared;

import com.google.common.collect.Lists;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Implements operations of list.
 * 
 * @param <T> type of object
 */
public class ListSequence<T> extends AbstractSequence<T> {
  private final List<T> values = Lists.newArrayList();

  public ListSequence() {
  }

  public ListSequence(List<T> values) {
    if (values != null) {
      this.values.addAll(values);
    }
  }

  /**
   * Removes all values of the list.
   */
  public void clear() {
    values.clear();
  }

  @Override
  public ListSequence<T> clone() {
    List<T> list = Lists.newArrayList(getList());
    return new ListSequence<T>(list);
  }

  /**
   * Returns the value of the list by {@code index}. The value {@code index} are between 0 and
   * {@code length() - 1}
   * 
   * @param index index of the list field
   * @return the value of the list by index
   */
  public T get(int index) {
    return values.get(index);
  }

  /**
   * Converts list to array.
   * 
   * @return converted array of list
   */
  public Pair<T[], Integer> getArray(T[] a) {
    Assert.notNull(a);
    return Pair.of(values.toArray(a), getLength());
  }

  public int getLength() {
    return values.size();
  }

  /**
   * Returns list of {@code java.util.List} type.
   * 
   * @return list of {@code java.util.List} type
   */
  public List<T> getList() {
    return values;
  }

  /**
   * Inserts a new {@code value} into list by {@code index}. The value of {@code index} are between
   * 0 and {@code length()}
   * 
   * @param index index of value in the list;
   * @param value value there will be insert
   */
  public void insert(int index, T value) {
    assertInsert(index);
    values.add(index, value);
  }

  /**
   * Removes the value of the list by index.
   * 
   * @param index index of value of the list;
   */
  public void remove(int index) {
    assertIndex(index);
    values.remove(index);
  }

  /**
   * Replaces the list values to other {@code value} by {@code index}.
   * 
   * @param index index of value in the list
   * @param value a new value
   */
  public void set(int index, T value) {
    assertIndex(index);
    values.set(index, value);
  }

  /**
   * Sets a values in the list of other {@code lst}.
   * 
   * @param lst the list of new values
   */
  public void setValues(List<T> lst) {
    BeeUtils.overwrite(values, lst);
  }

  /**
   * Sets a value in the list of the array.
   * 
   * @param arr the array of values
   */
  public void setValues(T[] arr) {
    Assert.notNull(arr);
    setValues(Arrays.asList(arr));
  }
}

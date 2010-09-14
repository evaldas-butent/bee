package com.butent.bee.egg.shared.utils;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.Transformable;

public class BeeProp<T> implements Comparable<BeeProp<T>>, Transformable {
  private String name;
  private T value;

  public BeeProp() {
    this.name = null;
    this.value = null;
  }

  public BeeProp(String name) {
    this.name = name;
  }

  public BeeProp(String name, T value) {
    this.name = name;
    this.value = value;
  }

  public int compareTo(BeeProp<T> oth) {
    if (name == null) {
      if (oth.name == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (name == null) {
      return 1;
    } else {
      return name.compareTo(oth.name);
    }
  }

  public String getName() {
    return name;
  }

  public T getValue() {
    return value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(T value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return name + BeeConst.DEFAULT_VALUE_SEPARATOR + BeeUtils.transform(value);
  }

  public String transform() {
    return toString();
  }

}

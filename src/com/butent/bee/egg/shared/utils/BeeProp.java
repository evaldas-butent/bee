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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  @Override
  public int compareTo(BeeProp<T> oth) {
    if (name == null)
      if (oth.name == null)
        return 0;
      else
        return -1;
    else if (name == null)
      return 1;
    else
      return name.compareTo(oth.name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;

    BeeProp<?> other = (BeeProp<?>) obj;

    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return name + BeeConst.DEFAULT_VALUE_SEPARATOR + BeeUtils.transform(value);
  }

  @Override
  public String transform() {
    return toString();
  }

}

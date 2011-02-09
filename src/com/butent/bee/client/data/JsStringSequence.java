package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.shared.AbstractSequence;

import java.util.List;

public class JsStringSequence extends AbstractSequence<String> {
  private JsArrayString values;

  public JsStringSequence(int size) {
    super();
    this.values = JsUtils.createArray(size);
  }

  public JsStringSequence(JsArrayString values) {
    super();
    this.values = values;
  }
  
  @SuppressWarnings("unused")
  private JsStringSequence() {
  }

  public void clear() {
    values.setLength(0);
  }

  public String get(int index) {
    return values.get(index);
  }

  public String[] getArray() {
    String[] arr = new String[length()];
    for (int i = 0; i < length(); i++) {
      arr[i] = get(i); 
    }
    return arr;
  }

  public List<String> getList() {
    List<String> lst = Lists.newArrayListWithCapacity(length());
    for (int i = 0; i < length(); i++) {
      lst.add(get(i)); 
    }
    return lst;
  }

  public void insert(int index, String value) {
    JsUtils.insert(values, index, value);
  }

  public int length() {
    return values.length();
  }

  public void remove(int index) {
    JsUtils.remove(values, index);
  }

  public void set(int index, String value) {
    values.set(index, value);
  }

  public void setValues(List<String> lst) {
    if (length() != lst.size()) {
      this.values = JsUtils.createArray(lst.size());
    }
    for (int i = 0; i < length(); i++) {
      set(i, lst.get(i));
    }
  }

  public void setValues(String[] arr) {
    if (length() != arr.length) {
      this.values = JsUtils.createArray(arr.length);
    }
    for (int i = 0; i < length(); i++) {
      set(i, arr[i]);
    }
  }
}

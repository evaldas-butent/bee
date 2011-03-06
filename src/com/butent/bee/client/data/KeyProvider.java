package com.butent.bee.client.data;

import com.google.gwt.view.client.ProvidesKey;

import com.butent.bee.shared.data.IsRow;

public class KeyProvider implements ProvidesKey<IsRow> {

  public Object getKey(IsRow item) {
    return (item == null) ? null : item.getId();
  }
}

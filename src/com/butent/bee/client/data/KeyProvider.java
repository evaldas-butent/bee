package com.butent.bee.client.data;

import com.google.gwt.view.client.ProvidesKey;

import com.butent.bee.shared.data.IsRow;

/**
 * Provides a key for list items, such that items that are to be treated as distinct have distinct
 * keys.
 */

public class KeyProvider implements ProvidesKey<IsRow> {

  public Object getKey(IsRow item) {
    return (item == null) ? null : item.getId();
  }
}

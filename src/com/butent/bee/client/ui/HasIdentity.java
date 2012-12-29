package com.butent.bee.client.ui;

import com.butent.bee.shared.IsUnique;

public interface HasIdentity extends IsUnique {

  String getIdPrefix();

  void setId(String id);
}

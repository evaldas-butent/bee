package com.butent.bee.client.view;

import com.google.gwt.user.client.ui.IsWidget;

import com.butent.bee.shared.sql.IsCondition;

public interface SearchView extends IsWidget {
  IsCondition getCondition(String tableName);
}

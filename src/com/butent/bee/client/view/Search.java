package com.butent.bee.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface Search extends IsWidget {
  public interface Presenter {
    void updateFilter(String filter);
  }
  
  String getCondition();

  void setCondition(String condition);

  void setPresenter(Presenter presenter);
}

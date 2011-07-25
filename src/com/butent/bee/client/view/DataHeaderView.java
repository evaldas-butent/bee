package com.butent.bee.client.view;

import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;

/**
 * Contains requirements for data header implementing classes.
 */

public interface DataHeaderView extends View, LoadingStateChangeEvent.Handler {
  
  void create(String caption, boolean hasData, boolean readOnly, boolean isChild);
  
  String getCaption();
  
  void setCaption(String caption);
}

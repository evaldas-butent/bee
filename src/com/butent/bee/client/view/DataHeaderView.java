package com.butent.bee.client.view;

import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;

import com.butent.bee.client.ui.UiOption;

import java.util.Collection;

/**
 * Contains requirements for data header implementing classes.
 */

public interface DataHeaderView extends View, LoadingStateChangeEvent.Handler {
  
  void create(String caption, boolean hasData, boolean readOnly, Collection<UiOption> options);
  
  String getCaption();
  
  void setCaption(String caption);
}

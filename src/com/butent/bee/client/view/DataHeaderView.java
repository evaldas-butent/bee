package com.butent.bee.client.view;

import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.ui.IndexedPanel;

import com.butent.bee.client.ui.UiOption;
import com.butent.bee.shared.ui.Action;

import java.util.Collection;
import java.util.Set;

/**
 * Contains requirements for data header implementing classes.
 */

public interface DataHeaderView extends View, LoadingStateChangeEvent.Handler, IndexedPanel {
  
  void create(String caption, boolean hasData, boolean readOnly, Collection<UiOption> options,
      Set<Action> enabledActions, Set<Action> disabledActions);
  
  String getCaption();
  
  void setCaption(String caption);
}

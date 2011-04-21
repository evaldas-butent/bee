package com.butent.bee.client.view;

import com.butent.bee.client.view.navigation.PagerView;

import java.util.Collection;

public interface HasNavigation {
  Collection<PagerView> getPagers();
}

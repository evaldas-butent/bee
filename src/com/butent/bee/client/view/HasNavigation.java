package com.butent.bee.client.view;

import com.butent.bee.client.view.navigation.PagerView;

import java.util.Collection;

/**
 * Requires implementing classes to have pages for navigation.
 */

public interface HasNavigation {
  Collection<PagerView> getPagers();
}

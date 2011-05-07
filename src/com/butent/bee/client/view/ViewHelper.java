package com.butent.bee.client.view;

import com.google.common.collect.Sets;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.search.SearchView;

import java.util.Collection;

/**
 * Gets pagers and searchers for views.
 */

public class ViewHelper {

  public static Collection<PagerView> getPagers(HasWidgets container) {
    Collection<PagerView> pagers = Sets.newHashSet();

    for (Widget widget : container) {
      if (widget instanceof PagerView) {
        pagers.add((PagerView) widget);
      } else if (widget instanceof HasNavigation) {
        Collection<PagerView> pc = ((HasNavigation) widget).getPagers();
        if (pc != null) {
          pagers.addAll(pc);
        }
      } else if (widget instanceof HasWidgets) {
        pagers.addAll(getPagers((HasWidgets) widget));
      }
    }
    return pagers;
  }

  public static Collection<SearchView> getSearchers(HasWidgets container) {
    Collection<SearchView> searchers = Sets.newHashSet();

    for (Widget widget : container) {
      if (widget instanceof SearchView) {
        searchers.add((SearchView) widget);
      } else if (widget instanceof HasSearch) {
        Collection<SearchView> sc = ((HasSearch) widget).getSearchers();
        if (sc != null) {
          searchers.addAll(sc);
        }
      } else if (widget instanceof HasWidgets) {
        searchers.addAll(getSearchers((HasWidgets) widget));
      }
    }
    return searchers;
  }

  private ViewHelper() {
  }
}

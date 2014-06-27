package com.butent.bee.client.view;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.event.logical.ReadyEvent;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class ViewHelper {

  private static final ImmutableSet<String> NO_EXCLUSIONS = ImmutableSet.of();

  public static void delegateReadyEvent(View delegator, View delegate) {
    Assert.notNull(delegate);
    delegateReadyEvent(delegator, Sets.newHashSet(delegate));
  }

  public static void delegateReadyEvent(final View delegator, Collection<View> delegates) {
    Assert.notNull(delegator);
    Assert.notEmpty(delegates);

    final Map<String, HandlerRegistration> registry = new HashMap<>();

    for (View view : delegates) {
      if (view != null) {
        HandlerRegistration registration = view.addReadyHandler(new ReadyEvent.Handler() {
          @Override
          public void onReady(ReadyEvent event) {
            if (event.getSource() instanceof View) {
              HandlerRegistration hr = registry.remove(((View) event.getSource()).getId());
              if (hr != null) {
                hr.removeHandler();

                if (registry.isEmpty()) {
                  ReadyEvent.fire(delegator);
                }
              }
            }
          }
        });

        if (registration != null) {
          registry.put(view.getId(), registration);
        }
      }
    }
  }

  public static Filter getFilter(HasSearch container, Provider dataProvider) {
    return getFilter(container, dataProvider, NO_EXCLUSIONS);
  }

  public static Filter getFilter(HasSearch container, Provider dataProvider,
      ImmutableSet<String> excludeSearchers) {
    Assert.notNull(container);
    Assert.notNull(dataProvider);

    Collection<SearchView> searchers = container.getSearchers();
    if (BeeUtils.isEmpty(searchers)) {
      return null;
    }

    List<Filter> filters = new ArrayList<>();
    for (SearchView search : searchers) {
      Filter flt = search.getFilter(dataProvider.getColumns(), dataProvider.getIdColumnName(),
          dataProvider.getVersionColumnName(), excludeSearchers);
      if (flt != null && !filters.contains(flt)) {
        filters.add(flt);
      }
    }
    return Filter.and(filters);
  }

  public static Collection<GridView> getGrids(Widget root) {
    Collection<GridView> grids = new HashSet<>();

    if (root instanceof GridView) {
      grids.add((GridView) root);

    } else if (root instanceof HasGridView) {
      grids.add(((HasGridView) root).getGridView());

    } else if (root instanceof HasWidgets) {
      for (Widget child : (HasWidgets) root) {
        grids.addAll(getGrids(child));
      }

    } else if (root instanceof HasOneWidget) {
      grids.addAll(getGrids(((HasOneWidget) root).getWidget()));
    }

    return grids;
  }

  public static HeaderView getHeader(Widget container) {
    if (container instanceof HasWidgets) {
      for (Widget child : (HasWidgets) container) {
        if (child instanceof HeaderView) {
          return (HeaderView) child;
        }
      }
    }
    return null;
  }

  public static Collection<View> getImmediateChildViews(Widget parent) {
    Collection<View> views = new HashSet<>();

    if (parent instanceof HasWidgets) {
      for (Widget child : (HasWidgets) parent) {
        if (child instanceof View) {
          views.add((View) child);
        } else {
          views.addAll(getImmediateChildViews(child));
        }
      }

    } else if (parent instanceof HasOneWidget) {
      Widget child = ((HasOneWidget) parent).getWidget();
      if (child instanceof View) {
        views.add((View) child);
      } else {
        views.addAll(getImmediateChildViews(child));
      }
    }

    return views;
  }

  public static Collection<PagerView> getPagers(HasWidgets container) {
    Assert.notNull(container);
    Collection<PagerView> pagers = new HashSet<>();

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

  public static PresenterCallback getPresenterCallback() {
    if (BeeKeeper.getUser().openInNewTab()) {
      return PresenterCallback.SHOW_IN_NEW_TAB;
    } else {
      return PresenterCallback.SHOW_IN_ACTIVE_PANEL;
    }
  }

  public static Collection<SearchView> getSearchers(HasWidgets container) {
    Assert.notNull(container);
    Collection<SearchView> searchers = new HashSet<>();

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

  public static boolean hasHeader(Widget container) {
    return getHeader(container) != null;
  }

  private ViewHelper() {
  }
}

package com.butent.bee.client.view.search;

import com.google.common.collect.ImmutableSet;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Implements a text box for search purposes.
 */

public class SearchBox extends InputText implements SearchView {

  private Presenter presenter;
  private FilterHandler filterHandler;

  public SearchBox() {
    this(null);
  }

  public SearchBox(String placeholder) {
    super();
    DomUtils.setSearch(this);
    if (!BeeUtils.isEmpty(placeholder)) {
      DomUtils.setPlaceholder(this, placeholder);
    }

    sinkEvents(Event.ONKEYDOWN);
  }

  @Override
  public Filter getFilter(List<? extends IsColumn> columns, String idColumnName,
      String versionColumnName, ImmutableSet<String> excludeSearchers) {
    if (BeeUtils.isEmpty(getValue())) {
      return null;
    } else if (!BeeUtils.isEmpty(excludeSearchers) && excludeSearchers.contains(getId())) {
      return null;
    } else {
      return DataUtils.parseCondition(getValue(), columns, idColumnName, versionColumnName);
    }
  }

  @Override
  public String getIdPrefix() {
    return "search";
  }

  @Override
  public Presenter getViewPresenter() {
    return presenter;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (getFilterHandler() != null && EventUtils.isKeyDown(event.getType())
        && event.getKeyCode() == KeyCodes.KEY_ENTER) {
      getFilterHandler().onFilterChange();
    }

    super.onBrowserEvent(event);
  }

  @Override
  public void setFilterHandler(FilterHandler filterHandler) {
    this.filterHandler = filterHandler;
  }

  @Override
  public void setViewPresenter(Presenter viewPresenter) {
    this.presenter = viewPresenter;
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-SearchBox";
  }

  private FilterHandler getFilterHandler() {
    return filterHandler;
  }
}

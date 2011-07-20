package com.butent.bee.client.view.search;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.Filter;

import java.util.List;

/**
 * Implements a text box for search purposes.
 */

public class SearchBox extends InputText implements SearchView {

  private Presenter presenter = null;

  public SearchBox() {
    super();
    DomUtils.setSearch(this);
    DomUtils.setPlaceholder(this, "filter...");
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-SearchBox";
  }

  public Filter getFilter(List<? extends IsColumn> columns) {
    return DataUtils.parseCondition(getValue(), columns);
  }

  @Override
  public String getIdPrefix() {
    return "search";
  }
  
  public Presenter getViewPresenter() {
    return presenter;
  }

  public String getWidgetId() {
    return getId();
  }

  public void setViewPresenter(Presenter presenter) {
    this.presenter = presenter;
  }
}

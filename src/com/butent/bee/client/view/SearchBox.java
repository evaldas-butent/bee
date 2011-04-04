package com.butent.bee.client.view;

import com.google.common.base.Strings;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.widget.BeeTextBox;

public class SearchBox extends BeeTextBox implements Search {
  private Presenter presenter = null;

  public SearchBox() {
    super();
    DomUtils.setSearch(this);
    DomUtils.setPlaceholder(this, "filter...");
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "search");
  }

  public String getCondition() {
    return getValue();
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-SearchBox";
  }

  @Override
  public boolean onValueChange(String value) {
    if (presenter != null) {
      presenter.updateFilter(value);
    }
    return super.onValueChange(value);
  }

  public void setCondition(String condition) {
    setValue(Strings.nullToEmpty(condition));
  }

  public void setPresenter(Presenter presenter) {
    this.presenter = null;
  }
}

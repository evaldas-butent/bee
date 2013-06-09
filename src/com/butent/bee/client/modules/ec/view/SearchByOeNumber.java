package com.butent.bee.client.modules.ec.view;

import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;

class SearchByOeNumber extends EcView {

  SearchByOeNumber() {
    super();
  }

  @Override
  protected void createUi() {
    add(new Label(Localized.constants.ecSearchByOeNumber()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "searchByOeNumber";
  }
}

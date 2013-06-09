package com.butent.bee.client.modules.ec.view;

import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;

class SearchByCar extends EcView {

  SearchByCar() {
    super();
  }

  @Override
  protected void createUi() {
    add(new Label(Localized.constants.ecSearchByCar()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "searchByCar";
  }
}

package com.butent.bee.client.modules.ec.view;

import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;

class SearchByManufacturer extends EcView {

  SearchByManufacturer() {
    super();
  }

  @Override
  protected void createUi() {
    add(new Label(Localized.constants.ecSearchByManufacturer()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "searchByManufacturer";
  }
}

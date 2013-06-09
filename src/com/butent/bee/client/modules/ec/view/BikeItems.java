package com.butent.bee.client.modules.ec.view;

import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;

class BikeItems extends EcView {

  BikeItems() {
    super();
  }

  @Override
  protected void createUi() {
    add(new Label(Localized.constants.ecBikeItems()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "bikeItems";
  }
}

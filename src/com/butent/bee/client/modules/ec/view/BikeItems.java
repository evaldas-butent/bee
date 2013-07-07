package com.butent.bee.client.modules.ec.view;

import com.butent.bee.shared.i18n.Localized;

class BikeItems extends EcView {

  BikeItems() {
    super();
  }

  @Override
  protected void createUi() {
    add(renderNoData(Localized.getConstants().ecBikeItems()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "bikeItems";
  }
}

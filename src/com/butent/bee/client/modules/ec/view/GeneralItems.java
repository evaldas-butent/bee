package com.butent.bee.client.modules.ec.view;

import com.butent.bee.shared.i18n.Localized;

class GeneralItems extends EcView {
  
  GeneralItems() {
    super();
  }

  @Override
  protected void createUi() {
    add(renderNoData(Localized.constants.ecGeneralItems()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "generalItems";
  }
}

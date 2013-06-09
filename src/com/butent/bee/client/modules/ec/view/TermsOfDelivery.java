package com.butent.bee.client.modules.ec.view;

import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;

class TermsOfDelivery extends EcView {

  TermsOfDelivery() {
    super();
  }

  @Override
  protected void createUi() {
    add(new Label(Localized.constants.ecTermsOfDelivery()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "termsOfDelivery";
  }
}

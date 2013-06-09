package com.butent.bee.client.modules.ec.view;

import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;

class FinancialInformation extends EcView {

  FinancialInformation() {
    super();
  }

  @Override
  protected void createUi() {
    add(new Label(Localized.constants.ecFinancialInformation()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "financialInformation";
  }
}

package com.butent.bee.client.modules.ec.view;

import com.butent.bee.shared.i18n.Localized;

class FinancialInformation extends EcView {

  FinancialInformation() {
    super();
  }

  @Override
  protected void createUi() {
    add(renderNoData(Localized.getConstants().ecFinancialInformation()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "financialInformation";
  }
}

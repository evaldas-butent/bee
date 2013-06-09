package com.butent.bee.client.modules.ec.view;

import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;

class Contacts extends EcView {

  Contacts() {
    super();
  }

  @Override
  protected void createUi() {
    add(new Label(Localized.constants.ecContacts()));
  }

  @Override
  protected String getPrimaryStyle() {
    return "contacts";
  }
}

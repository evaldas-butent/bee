package com.butent.bee.client.modules.ec.view;

import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.Frame;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

class Contacts extends EcView {

  Contacts() {
    super();
  }

  @Override
  protected void createUi() {
    String url = EcKeeper.getContactsUrl();
    if (BeeUtils.isEmpty(url)) {
      add(new Label(Localized.constants.ecContacts() + " url not specified"));
    } else {
      Frame frame = new Frame(url);
      EcStyles.add(frame, getPrimaryStyle(), "frame");
      add(frame);
    }
  }

  @Override
  protected String getPrimaryStyle() {
    return "contacts";
  }
}

package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.modules.commons.CommonsUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.utils.BeeUtils;

public class TransportRegistrationForm extends AbstractFormInterceptor {

  @Override
  public void afterRefresh(final FormView form, IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();

    if (header != null) {
      if (DataUtils.hasId(row)) {
        if (!header.hasCommands()) {
          header.addCommandItem(new Button(Localized.getConstants().trCommandCreateNewUser()));

          header.addCommandItem(new Button(Localized.getConstants().trCommandBlockIpAddress(),
              new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  String host = getDataValue(TransportConstants.COL_REGISTRATION_HOST);
                  if (!BeeUtils.isEmpty(host)) {
                    CommonsUtils.blockHost(Localized.getConstants().trCommandBlockIpAddress(),
                        host, getFormView());
                  }
                }
              }));
        }
      } else if (header.hasCommands()) {
        header.clearCommandPanel();
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new TransportRegistrationForm();
  }
}

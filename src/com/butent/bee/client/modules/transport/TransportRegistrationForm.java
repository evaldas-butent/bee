package com.butent.bee.client.modules.transport;

import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;

public class TransportRegistrationForm extends AbstractFormInterceptor {

  @Override
  public void afterRefresh(final FormView form, IsRow row) {
    if (DataUtils.hasId(row)) {
      HeaderView header = form.getViewPresenter().getHeader();
      header.clearCommandPanel();
      
      header.addCommandItem(new Button(Localized.getConstants().trCommandCreateNewUser()));
      header.addCommandItem(new Button(Localized.getConstants().trCommandBlockIpAddress()));
    }
  }
  
  @Override
  public FormInterceptor getInstance() {
    return new TransportRegistrationForm();
  }
}

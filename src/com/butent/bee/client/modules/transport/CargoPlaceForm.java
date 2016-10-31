package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.i18n.Localized;

import java.util.Objects;

public class CargoPlaceForm extends AbstractFormInterceptor {

  @Override
  public String getCaption() {
    if (Objects.equals(getViewName(), TBL_CARGO_LOADING)) {
      return Localized.dictionary().cargoLoadingPlace();
    } else if (Objects.equals(getViewName(), TBL_CARGO_UNLOADING)) {
      return Localized.dictionary().cargoUnloadingPlace();
    } else {
      return super.getCaption();
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoPlaceForm();
  }
}

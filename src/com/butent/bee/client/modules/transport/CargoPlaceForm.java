package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public class CargoPlaceForm extends AbstractFormInterceptor {

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {
    super.afterCreateWidget(name, widget, callback);
    String widgetSuffix = BeeUtils.removePrefix(getViewName(), COL_CARGO);

    if (widget instanceof DataSelector && !BeeUtils.isEmpty(name) && !BeeUtils.isEmpty(widgetSuffix)
        && !name.endsWith(widgetSuffix)) {
      ((DataSelector) widget).setVisible(false);
    }
  }

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

package com.butent.bee.client.modules.transport;

import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

public class NewCargoRequestInterceptor extends AbstractFormInterceptor {

  @Override
  public FormInterceptor getInstance() {
    return new NewCargoRequestInterceptor();
  }

  @Override
  public void onReadyForInsert(ReadyForInsertEvent event) {
    FormView form = getFormView();
    IsRow row = form.getActiveRow();
    int colRouteIndex = form.getDataIndex(COL_CARGO_REQUEST_ROUTE);
    int colLoadingAddressIndex = form.getDataIndex(COL_CARGO_REQUEST_LOADING_ADDRESS);
    int colUnloadingAddressIndex = form.getDataIndex(COL_CARGO_REQUEST_UNLOADING_ADDRESS);
    int colLoadingCountryIndex = form.getDataIndex(COL_CARGO_REQUEST_LOADING_COUNTRY_NAME);
    int colUnloadingCountryIndex =
        form.getDataIndex(COL_CARGO_REQUEST_UNLOADING_COUNTRY_NAME);

    if (row == null) {
      return;
    }
    
    String routeText = row.getString(colRouteIndex);

    if (BeeUtils.isEmpty(routeText)) {
      event.consume();

      routeText = row.getString(colLoadingAddressIndex)
          + ", " + row.getString(colLoadingCountryIndex)
          + " -> " + row.getString(colUnloadingAddressIndex)
          + ", " + row.getString(colUnloadingCountryIndex);

      row.setValue(colRouteIndex, routeText);
      form.getViewPresenter().handleAction(Action.SAVE);
    }
  }
}
